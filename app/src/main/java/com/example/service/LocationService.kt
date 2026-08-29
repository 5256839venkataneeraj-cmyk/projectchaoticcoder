package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PointF
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class WalkLocationUpdate(
    val location: Location,
    val point: PointF,
    val totalDistanceMeters: Double,
    val currentSpeedKmh: Double,
    val estimatedSteps: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class LocationServiceState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val lastLocation: Location? = null,
    val pointsCount: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val estimatedSteps: Int = 0,
    val currentSpeedKmh: Double = 0.0,
    val gpsAccuracyMeters: Float = 0f
)

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var previousLocation: Location? = null
    private var totalDistanceAccumulated = 0.0
    private var baseLat: Double? = null
    private var baseLng: Double? = null
    private var isPaused = false

    companion object {
        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_PAUSE = "ACTION_PAUSE_TRACKING"
        const val ACTION_RESUME = "ACTION_RESUME_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"

        const val CHANNEL_ID = "walk_route_art_tracking_channel"
        const val NOTIFICATION_ID = 4040

        private val _serviceState = MutableStateFlow(LocationServiceState())
        val serviceState: StateFlow<LocationServiceState> = _serviceState.asStateFlow()

        private val _locationUpdates = MutableSharedFlow<WalkLocationUpdate>(extraBufferCapacity = 64)
        val locationUpdates: SharedFlow<WalkLocationUpdate> = _locationUpdates.asSharedFlow()

        fun start(context: Context) {
            val intent = Intent(context, LocationService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, LocationService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, LocationService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                isPaused = false
                startForegroundTracking()
            }
            ACTION_PAUSE -> {
                isPaused = true
                _serviceState.value = _serviceState.value.copy(isPaused = true)
                updateNotification("Walk Tracking Paused", "Tap to resume turning steps into art")
            }
            ACTION_RESUME -> {
                isPaused = false
                _serviceState.value = _serviceState.value.copy(isPaused = false)
                updateNotification("Live Walk Tracking Active", "Creating generative route art...")
            }
            ACTION_STOP -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (isPaused) return
                for (location in result.locations) {
                    processNewLocation(location)
                }
            }
        }
    }

    private fun processNewLocation(location: Location) {
        // Filter noisy fixes (accuracy worse than 50 meters)
        if (location.hasAccuracy() && location.accuracy > 50f) {
            Log.w("LocationService", "Skipping inaccurate location: ${location.accuracy}m")
            return
        }

        if (baseLat == null || baseLng == null) {
            baseLat = location.latitude
            baseLng = location.longitude
        }

        val prev = previousLocation
        var distDelta = 0.0
        if (prev != null) {
            distDelta = prev.distanceTo(location).toDouble()
            // Ignore micro-jitters (< 1.2 meters)
            if (distDelta >= 1.2) {
                totalDistanceAccumulated += distDelta
                previousLocation = location
            }
        } else {
            previousLocation = location
        }

        // Convert GPS latitude/longitude to 0..1000 campus map coordinates relative to starting anchor
        val currentBaseLat = baseLat ?: location.latitude
        val currentBaseLng = baseLng ?: location.longitude

        val latDiffMeters = (location.latitude - currentBaseLat) * 111320.0
        val lngDiffMeters = (location.longitude - currentBaseLng) * (111320.0 * kotlin.math.cos(Math.toRadians(currentBaseLat)))

        // Map ~500m campus radius to 500 center +- 400 pixels
        val canvasX = (500f + (lngDiffMeters * 1.5f).toFloat()).coerceIn(40f, 960f)
        val canvasY = (500f - (latDiffMeters * 1.5f).toFloat()).coerceIn(40f, 960f)
        val point = PointF(canvasX, canvasY)

        val speedKmh = if (location.hasSpeed()) (location.speed * 3.6) else 0.0
        val estimatedSteps = (totalDistanceAccumulated / 0.72).roundToInt()
        val km = Math.round((totalDistanceAccumulated / 1000.0) * 100.0) / 100.0

        val update = WalkLocationUpdate(
            location = location,
            point = point,
            totalDistanceMeters = totalDistanceAccumulated,
            currentSpeedKmh = Math.round(speedKmh * 10.0) / 10.0,
            estimatedSteps = estimatedSteps
        )

        serviceScope.launch {
            _locationUpdates.emit(update)
        }

        val updatedCount = _serviceState.value.pointsCount + 1
        _serviceState.value = LocationServiceState(
            isRunning = true,
            isPaused = isPaused,
            lastLocation = location,
            pointsCount = updatedCount,
            totalDistanceMeters = totalDistanceAccumulated,
            estimatedSteps = estimatedSteps,
            currentSpeedKmh = Math.round(speedKmh * 10.0) / 10.0,
            gpsAccuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f
        )

        updateNotification(
            title = "Live Walk: $km km • $estimatedSteps steps",
            content = "Transforming your live movement into generative canvas art"
        )
    }

    @SuppressLint("MissingPermission")
    private fun startForegroundTracking() {
        val notification = buildNotification(
            title = "Live Walk Tracking Active",
            content = "FusedLocationClient active • Mapping route art"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2500L // 2.5 seconds interval
        ).apply {
            setMinUpdateIntervalMillis(1500L)
            setMinUpdateDistanceMeters(2.0f) // 2 meters movement
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            _serviceState.value = _serviceState.value.copy(isRunning = true, isPaused = false)
            Log.d("LocationService", "FusedLocationProviderClient started successfully")
        } catch (e: SecurityException) {
            Log.e("LocationService", "Location permission missing: ${e.message}", e)
        }
    }

    private fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("LocationService", "Error removing location updates: ${e.message}", e)
        }
        previousLocation = null
        baseLat = null
        baseLng = null
        totalDistanceAccumulated = 0.0
        _serviceState.value = LocationServiceState(isRunning = false, isPaused = false)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Route Art Walk Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active walk tracking status and distance"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = buildNotification(title, content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }
}
