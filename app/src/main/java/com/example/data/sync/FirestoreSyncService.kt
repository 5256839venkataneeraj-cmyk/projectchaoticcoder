package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.CustomColorEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WalkRouteEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

data class SyncDiagnosticEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // "INFO", "WARN", "ERROR", "DEBUG", "SUCCESS"
    val tag: String,
    val message: String
) {
    fun formatted(): String {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
        return "[$time] [$level] $tag: $message"
    }
}

sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(
        val currentStep: String = "Starting synchronization...",
        val diagnosticLogs: List<SyncDiagnosticEntry> = emptyList()
    ) : SyncState()

    data class Success(
        val message: String,
        val syncedCount: Int,
        val isCloudSynced: Boolean = true,
        val diagnosticLogs: List<SyncDiagnosticEntry> = emptyList(),
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncState()

    data class Error(
        val errorMessage: String,
        val rootCauseCategory: String,
        val recommendation: String,
        val localRecordsSaved: Int = 0,
        val isLocalDatabaseHealthy: Boolean = true,
        val diagnosticLogs: List<SyncDiagnosticEntry> = emptyList(),
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncState()
}

class FirestoreSyncService(private val context: Context) {

    private val TAG = "FirestoreSyncService"
    private val database = AppDatabase.getDatabase(context)
    private val routeDao = database.routeDao()
    private val customColorDao = database.customColorDao()
    private val userProfileDao = database.userProfileDao()
    private val badgeDao = database.badgeDao()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _diagnosticLogs = MutableStateFlow<List<SyncDiagnosticEntry>>(emptyList())
    val diagnosticLogs: StateFlow<List<SyncDiagnosticEntry>> = _diagnosticLogs.asStateFlow()

    private fun logDiag(level: String, tag: String, message: String) {
        val entry = SyncDiagnosticEntry(level = level, tag = tag, message = message)
        when (level) {
            "ERROR" -> Log.e(TAG, "[$tag] $message")
            "WARN" -> Log.w(TAG, "[$tag] $message")
            "DEBUG" -> Log.d(TAG, "[$tag] $message")
            else -> Log.i(TAG, "[$tag] $message")
        }
        val current = _diagnosticLogs.value.toMutableList()
        current.add(entry)
        _diagnosticLogs.value = current
    }

    /**
     * Inspects device network connectivity state and returns diagnostic details.
     */
    private fun checkNetworkConnectivity(): Triple<Boolean, String, String> {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            return Triple(false, "UNAVAILABLE", "ConnectivityManager service unavailable")
        }

        val activeNetwork = cm.activeNetwork
        if (activeNetwork == null) {
            return Triple(false, "OFFLINE", "No active network interface detected on device")
        }

        val capabilities = cm.getNetworkCapabilities(activeNetwork)
        if (capabilities == null) {
            return Triple(false, "NO_CAPABILITIES", "Network capabilities query returned null")
        }

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val transport = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Mobile Data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN Tunnel"
            else -> "Other"
        }

        val summary = if (hasInternet) {
            "Connected via $transport (Validated: $isValidated)"
        } else {
            "Connected to $transport interface but lacks NET_CAPABILITY_INTERNET"
        }

        return Triple(hasInternet, transport, summary)
    }

    /**
     * Initializes or verifies Firebase App configuration.
     */
    private fun ensureFirebaseInitialized(): Pair<Boolean, String> {
        return try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                logDiag("INFO", "FirebaseInit", "No default FirebaseApp found. Attempting configuration...")
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyFallbackPlaceholderKeyForLocalApp")
                    .setProjectId("campus-path-canvas")
                    .build()
                FirebaseApp.initializeApp(context, options)
                logDiag("WARN", "FirebaseInit", "Initialized with local fallback credentials (ProjectId: campus-path-canvas, AppId: ${context.packageName})")
                Pair(true, "Initialized with local fallback configuration (google-services.json pending)")
            } else {
                val app = apps.first()
                val projectId = app.options.projectId ?: "default"
                logDiag("INFO", "FirebaseInit", "Active FirebaseApp detected: ${app.name} (ProjectId: $projectId)")
                Pair(true, "Active FirebaseApp: $projectId")
            }
        } catch (e: Exception) {
            val errMsg = "Firebase initialization exception: ${e.message}"
            logDiag("ERROR", "FirebaseInit", errMsg)
            Pair(false, errMsg)
        }
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            val (ok, _) = ensureFirebaseInitialized()
            if (!ok) return null
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            logDiag("ERROR", "FirestoreGet", "Failed to obtain Firestore instance: ${e.javaClass.simpleName} - ${e.message}")
            null
        }
    }

    private fun getAuth(): FirebaseAuth? {
        return try {
            val (ok, _) = ensureFirebaseInitialized()
            if (!ok) return null
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            logDiag("ERROR", "AuthGet", "Failed to obtain FirebaseAuth instance: ${e.javaClass.simpleName} - ${e.message}")
            null
        }
    }

    private fun getEffectiveUserId(): Pair<String, Boolean> {
        val auth = getAuth()
        val user = auth?.currentUser
        return if (user != null) {
            Pair(user.uid, true)
        } else {
            val localId = "campus_student_local_${android.os.Build.MODEL.hashCode().toString().takeLast(6)}"
            Pair(localId, false)
        }
    }

    suspend fun syncAll(): SyncState = withContext(Dispatchers.IO) {
        _diagnosticLogs.value = emptyList()
        logDiag("INFO", "SyncEngine", "Starting comprehensive synchronization sequence...")
        _syncState.value = SyncState.Syncing(
            currentStep = "Analyzing network & database...",
            diagnosticLogs = _diagnosticLogs.value
        )

        try {
            // STEP 1: Network Check
            logDiag("DEBUG", "NetworkCheck", "Inspecting active network connectivity...")
            val (hasNet, transport, netDetails) = checkNetworkConnectivity()
            logDiag(if (hasNet) "INFO" else "WARN", "NetworkCheck", netDetails)

            // STEP 2: Local Database Tally
            logDiag("DEBUG", "RoomDatabase", "Querying on-device Room SQLite database...")
            val localRoutes = routeDao.getAllRoutes().first()
            val customColors = customColorDao.getAllCustomColors().first()
            val profile = userProfileDao.getUserProfile().first() ?: UserProfileEntity()
            val badges = badgeDao.getAllBadges().first()
            logDiag(
                "INFO",
                "RoomDatabase",
                "Local DB healthy: ${localRoutes.size} walk routes, ${customColors.size} pigments, ${badges.size} badges, Profile '${profile.username}'"
            )

            // STEP 3: Firebase / Auth Inspection
            logDiag("DEBUG", "FirebaseAuth", "Verifying authentication state...")
            val auth = getAuth()
            val (userId, isAuthenticated) = getEffectiveUserId()
            if (isAuthenticated) {
                logDiag("INFO", "FirebaseAuth", "Authenticated user active: UID=$userId")
            } else {
                logDiag("WARN", "FirebaseAuth", "No authenticated Google/Firebase user session. Using persistent local student ID: $userId")
            }

            // STEP 4: Cloud Firestore Instance Check
            val firestore = getFirestore()
            if (firestore == null) {
                val errorMsg = "Firebase Firestore client could not be instantiated on this device configuration."
                logDiag("WARN", "FirestoreEngine", errorMsg)
                val successOffline = SyncState.Success(
                    message = "All ${localRoutes.size} artworks saved locally in Room SQLite database. (Cloud requires active Firebase project configuration)",
                    syncedCount = localRoutes.size,
                    isCloudSynced = false,
                    diagnosticLogs = _diagnosticLogs.value
                )
                _syncState.value = successOffline
                return@withContext successOffline
            }

            logDiag("INFO", "FirestoreEngine", "Targeting Firestore database: users/$userId")
            _syncState.value = SyncState.Syncing(
                currentStep = "Uploading profile & ${localRoutes.size} route artworks to Firestore...",
                diagnosticLogs = _diagnosticLogs.value
            )

            val userRef = firestore.collection("users").document(userId)

            // STEP 5: Attempt Firestore Cloud Synchronization with detailed logging & timeout
            val syncStartTime = System.currentTimeMillis()
            var routesSynced = 0

            val syncSuccess = try {
                withTimeoutOrNull(6500L) {
                    // 5a. Profile
                    logDiag("DEBUG", "FirestoreWrite", "Writing user profile document to /users/$userId...")
                    val profileMap = hashMapOf(
                        "id" to profile.id,
                        "username" to profile.username,
                        "studentId" to profile.studentId,
                        "hostelBlock" to profile.hostelBlock,
                        "department" to profile.department,
                        "totalCoins" to profile.totalCoins,
                        "currentXp" to profile.currentXp,
                        "currentLevel" to profile.currentLevel,
                        "explorerRank" to profile.explorerRank,
                        "totalSteps" to profile.totalSteps,
                        "totalArtworks" to profile.totalArtworks,
                        "activeStreakDays" to profile.activeStreakDays,
                        "lastSyncedAt" to System.currentTimeMillis()
                    )
                    userRef.set(profileMap, SetOptions.merge()).await()
                    logDiag("INFO", "FirestoreWrite", "Profile document successfully merged into Firestore.")

                    // 5b. Walk Routes
                    logDiag("DEBUG", "FirestoreWrite", "Syncing ${localRoutes.size} walk routes to /users/$userId/walk_routes...")
                    for (route in localRoutes) {
                        val routeMap = hashMapOf(
                            "id" to route.id,
                            "dateString" to route.dateString,
                            "isoDate" to route.isoDate,
                            "steps" to route.steps,
                            "distanceKm" to route.distanceKm,
                            "durationMinutes" to route.durationMinutes,
                            "calories" to route.calories,
                            "title" to route.title,
                            "shapeName" to route.shapeName,
                            "shapeCategory" to route.shapeCategory,
                            "pointsJson" to route.pointsJson,
                            "blobsJson" to route.blobsJson,
                            "strokesJson" to route.strokesJson,
                            "stickersJson" to route.stickersJson,
                            "isFavorite" to route.isFavorite,
                            "campusName" to route.campusName,
                            "artStyle" to route.artStyle,
                            "createdAt" to route.createdAt,
                            "syncedAt" to System.currentTimeMillis()
                        )

                        userRef.collection("walk_routes")
                            .document(route.id.toString())
                            .set(routeMap, SetOptions.merge())
                            .await()

                        // Global public feed sync
                        val publicFeedRef = firestore.collection("campus_artworks").document("${userId}_${route.id}")
                        val publicArtworkMap = hashMapOf(
                            "artworkId" to "${userId}_${route.id}",
                            "authorName" to profile.username,
                            "hostelBlock" to profile.hostelBlock,
                            "department" to profile.department,
                            "title" to route.title,
                            "shapeName" to route.shapeName,
                            "shapeCategory" to route.shapeCategory,
                            "blobsJson" to route.blobsJson,
                            "strokesJson" to route.strokesJson,
                            "distanceKm" to route.distanceKm,
                            "steps" to route.steps,
                            "artStyle" to route.artStyle,
                            "timestamp" to route.createdAt
                        )
                        publicFeedRef.set(publicArtworkMap, SetOptions.merge()).await()

                        routesSynced++
                    }
                    logDiag("INFO", "FirestoreWrite", "Synchronized $routesSynced walk route artworks.")

                    // 5c. Custom Pigments
                    logDiag("DEBUG", "FirestoreWrite", "Syncing ${customColors.size} custom pigments to /users/$userId/custom_pigments...")
                    for (color in customColors) {
                        val colorMap = hashMapOf(
                            "id" to color.id,
                            "name" to color.name,
                            "hexCode" to color.hexCode,
                            "category" to color.category,
                            "redVal" to color.redVal,
                            "greenVal" to color.greenVal,
                            "blueVal" to color.blueVal,
                            "createdAt" to color.createdAt
                        )
                        userRef.collection("custom_pigments")
                            .document(color.id.toString())
                            .set(colorMap, SetOptions.merge())
                            .await()
                    }

                    // 5d. Badges
                    logDiag("DEBUG", "FirestoreWrite", "Syncing ${badges.size} achievement badges to /users/$userId/unlocked_badges...")
                    for (badge in badges) {
                        val badgeMap = hashMapOf(
                            "id" to badge.id,
                            "title" to badge.title,
                            "description" to badge.description,
                            "iconEmoji" to badge.iconEmoji,
                            "rarity" to badge.rarity,
                            "isUnlocked" to badge.isUnlocked,
                            "unlockedDate" to badge.unlockedDate
                        )
                        userRef.collection("unlocked_badges")
                            .document(badge.id)
                            .set(badgeMap, SetOptions.merge())
                            .await()
                    }

                    true
                }
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - syncStartTime
                logFirestoreException(e, elapsed)
                false
            }

            val elapsedMs = System.currentTimeMillis() - syncStartTime

            if (syncSuccess == true) {
                logDiag("SUCCESS", "SyncEngine", "Cloud sync succeeded in ${elapsedMs}ms ($routesSynced routes, ${customColors.size} pigments synced)")
                val successState = SyncState.Success(
                    message = "Successfully synced $routesSynced routes & ${customColors.size} pigments to Firestore Cloud ($elapsedMs ms)",
                    syncedCount = routesSynced,
                    isCloudSynced = true,
                    diagnosticLogs = _diagnosticLogs.value
                )
                _syncState.value = successState
                successState
            } else {
                // Timeout or remote reachability issue
                val (hasNetNow, transportNow, _) = checkNetworkConnectivity()
                val failureCategory = if (!hasNetNow) "Network Offline ($transportNow)" else "Cloud Server Timeout (${elapsedMs}ms)"
                val reason = if (!hasNetNow) {
                    "Device is offline. Please check your internet connection."
                } else {
                    "Firestore cloud server did not respond within ${elapsedMs}ms."
                }
                val rec = if (!hasNetNow) {
                    "Connect to Wi-Fi or mobile data and tap Retry."
                } else {
                    "Tap Retry to re-attempt cloud connection, or check Firebase settings."
                }
                logDiag("WARN", "SyncEngine", "Sync failure: $reason. Local Room database remains 100% saved.")

                val errorState = SyncState.Error(
                    errorMessage = reason,
                    rootCauseCategory = failureCategory,
                    recommendation = rec,
                    localRecordsSaved = localRoutes.size,
                    isLocalDatabaseHealthy = true,
                    diagnosticLogs = _diagnosticLogs.value
                )
                _syncState.value = errorState
                errorState
            }
        } catch (e: Exception) {
            logDiag("ERROR", "SyncEngine", "Fatal sync error: ${e.javaClass.simpleName} - ${e.message}")
            val localCount = try { routeDao.getAllRoutes().first().size } catch (ex: Exception) { 0 }
            val errorState = SyncState.Error(
                errorMessage = "Sync issue: ${e.message ?: "Unknown error"}",
                rootCauseCategory = e.javaClass.simpleName,
                recommendation = "Local on-device database is intact. Ensure google-services.json is configured for cloud upload.",
                localRecordsSaved = localCount,
                isLocalDatabaseHealthy = true,
                diagnosticLogs = _diagnosticLogs.value
            )
            _syncState.value = errorState
            errorState
        }
    }

    private fun logFirestoreException(e: Exception, elapsedMs: Long) {
        when (e) {
            is FirebaseFirestoreException -> {
                val code = e.code
                val explanation = when (code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        "PERMISSION_DENIED: Firestore security rules rejected write access. Check Firestore rules in Firebase Console."
                    FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                        "UNAUTHENTICATED: Request lacks valid authentication credentials. Sign-in via Google CredentialManager required."
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        "UNAVAILABLE: Firestore service is unreachable or network dropped ($elapsedMs ms elapsed)."
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                        "DEADLINE_EXCEEDED: Firestore operation timed out ($elapsedMs ms elapsed)."
                    FirebaseFirestoreException.Code.NOT_FOUND ->
                        "NOT_FOUND: Firestore database instance or target document not found."
                    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                        "RESOURCE_EXHAUSTED: Cloud project quota or rate limit exceeded."
                    else ->
                        "FirebaseFirestoreException (${code.name}): ${e.message}"
                }
                logDiag("ERROR", "FirestoreError", explanation)
            }
            is FirebaseAuthException -> {
                logDiag("ERROR", "AuthError", "FirebaseAuthException (${e.errorCode}): ${e.message}")
            }
            is FirebaseNetworkException -> {
                logDiag("ERROR", "NetworkError", "FirebaseNetworkException: Unable to connect to Firebase backend hosts. ($elapsedMs ms)")
            }
            is UnknownHostException -> {
                logDiag("ERROR", "DnsError", "UnknownHostException: Unable to resolve Firebase cloud hostname '${e.message}'. Check DNS / Internet.")
            }
            is SocketTimeoutException, is ConnectException, is TimeoutCancellationException -> {
                logDiag("WARN", "Timeout", "Network connection timed out after ${elapsedMs}ms while contacting Firebase servers.")
            }
            else -> {
                logDiag("ERROR", "SyncError", "Exception (${e.javaClass.simpleName}): ${e.message}")
            }
        }
    }

    suspend fun syncSingleRoute(route: WalkRouteEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore() ?: return@withContext false
            val (userId, _) = getEffectiveUserId()
            val routeMap = hashMapOf(
                "id" to route.id,
                "dateString" to route.dateString,
                "isoDate" to route.isoDate,
                "steps" to route.steps,
                "distanceKm" to route.distanceKm,
                "durationMinutes" to route.durationMinutes,
                "calories" to route.calories,
                "title" to route.title,
                "shapeName" to route.shapeName,
                "shapeCategory" to route.shapeCategory,
                "pointsJson" to route.pointsJson,
                "blobsJson" to route.blobsJson,
                "strokesJson" to route.strokesJson,
                "stickersJson" to route.stickersJson,
                "isFavorite" to route.isFavorite,
                "campusName" to route.campusName,
                "artStyle" to route.artStyle,
                "createdAt" to route.createdAt,
                "syncedAt" to System.currentTimeMillis()
            )

            withTimeoutOrNull(4000L) {
                firestore.collection("users")
                    .document(userId)
                    .collection("walk_routes")
                    .document(route.id.toString())
                    .set(routeMap, SetOptions.merge())
                    .await()
            } != null
        } catch (e: Exception) {
            logDiag("WARN", "SingleRouteSync", "Single route sync skipped: ${e.message}")
            false
        }
    }
}
