package com.example.data.repository

import android.content.Context
import com.example.data.generator.SampleCampusData
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.sync.FirestoreSyncService
import com.example.data.sync.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RouteRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val routeDao = database.routeDao()
    private val challengeDao = database.challengeDao()
    private val badgeDao = database.badgeDao()
    private val storeDao = database.storeDao()
    private val userProfileDao = database.userProfileDao()
    private val customColorDao = database.customColorDao()
    private val syncService = FirestoreSyncService(context)

    val syncState: StateFlow<SyncState> = syncService.syncState

    suspend fun syncWithFirestore(): SyncState {
        return syncService.syncAll()
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val existingRoutes = routeDao.getAllRoutes().first()
        if (existingRoutes.isEmpty()) {
            routeDao.insertRoutes(SampleCampusData.getInitialRoutes())
            challengeDao.insertChallenges(SampleCampusData.getInitialChallenges())
            badgeDao.insertBadges(SampleCampusData.getInitialBadges())
            storeDao.insertStoreItems(SampleCampusData.getInitialStoreItems())
            userProfileDao.insertUserProfile(UserProfileEntity())
            SampleCampusData.getInitialCustomColors().forEach {
                customColorDao.insertCustomColor(it)
            }
        }
    }

    // Routes
    val allRoutes: Flow<List<WalkRouteEntity>> = routeDao.getAllRoutes()
    val favoriteRoutes: Flow<List<WalkRouteEntity>> = routeDao.getFavoriteRoutes()

    suspend fun getRouteById(id: Long): WalkRouteEntity? = routeDao.getRouteById(id)

    suspend fun insertRoute(route: WalkRouteEntity): Long {
        val id = routeDao.insertRoute(route)
        userProfileDao.recordCompletedWalk(route.steps)
        userProfileDao.addCoins(50) // reward for completing walk
        addXpAndCheckLevelUp(150 + (route.steps / 100))
        CoroutineScope(Dispatchers.IO).launch {
            syncService.syncSingleRoute(route.copy(id = id))
        }
        return id
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        routeDao.toggleFavorite(id, isFavorite)
        CoroutineScope(Dispatchers.IO).launch {
            routeDao.getRouteById(id)?.let { syncService.syncSingleRoute(it) }
        }
    }

    suspend fun updateCustomization(id: Long, blobsJson: String, artStyle: String, stickersJson: String) {
        routeDao.updateArtworkCustomization(id, blobsJson, artStyle, stickersJson)
        addXpAndCheckLevelUp(30) // XP for art customization
        CoroutineScope(Dispatchers.IO).launch {
            routeDao.getRouteById(id)?.let { syncService.syncSingleRoute(it) }
        }
    }

    suspend fun deleteRoute(route: WalkRouteEntity) {
        routeDao.deleteRoute(route)
    }

    // Challenges & Badges
    val allChallenges: Flow<List<ChallengeEntity>> = challengeDao.getAllChallenges()
    val allBadges: Flow<List<BadgeEntity>> = badgeDao.getAllBadges()

    suspend fun claimChallengeReward(challenge: ChallengeEntity) {
        challengeDao.claimReward(challenge.id)
        userProfileDao.addCoins(challenge.rewardCoins)
        addXpAndCheckLevelUp(120)
    }

    // Custom Pigments & Color Lab
    val allCustomColors: Flow<List<CustomColorEntity>> = customColorDao.getAllCustomColors()

    suspend fun saveCustomColor(name: String, hexCode: String, category: String, r: Int, g: Int, b: Int): Long {
        val id = customColorDao.insertCustomColor(
            CustomColorEntity(
                name = name,
                hexCode = hexCode,
                category = category,
                redVal = r,
                greenVal = g,
                blueVal = b
            )
        )
        addXpAndCheckLevelUp(50) // 50 XP for mixing a new pigment
        return id
    }

    suspend fun deleteCustomColor(color: CustomColorEntity) {
        customColorDao.deleteCustomColor(color)
    }

    // XP & Level-up Progression System
    suspend fun addXpAndCheckLevelUp(xpEarned: Int) {
        userProfileDao.addXp(xpEarned)
        val profile = userProfileDao.getUserProfile().first() ?: return
        val currentTotalXp = profile.currentXp + xpEarned

        val (calculatedLevel, calculatedRank) = when {
            currentTotalXp >= 2500 -> Pair(5, "Celestial Route Artist")
            currentTotalXp >= 1500 -> Pair(4, "Master Cartographer")
            currentTotalXp >= 800 -> Pair(3, "Palette Alchemist")
            currentTotalXp >= 300 -> Pair(2, "Path Pioneer")
            else -> Pair(1, "Campus Wanderer")
        }

        if (calculatedLevel != profile.currentLevel || calculatedRank != profile.explorerRank) {
            userProfileDao.updateLevelAndRank(calculatedLevel, calculatedRank)
        }
    }

    // Store & Profile
    val allStoreItems: Flow<List<StoreItemEntity>> = storeDao.getAllStoreItems()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun buyStoreItem(item: StoreItemEntity): Boolean {
        val profile = userProfileDao.getUserProfile().first() ?: return false
        if (profile.totalCoins >= item.costCoins) {
            val deducted = userProfileDao.deductCoins(item.costCoins)
            if (deducted > 0) {
                storeDao.unlockStoreItem(item.id)
                addXpAndCheckLevelUp(75)
                return true
            }
        }
        return false
    }
}

