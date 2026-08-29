package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM daily_routes ORDER BY createdAt DESC")
    fun getAllRoutes(): Flow<List<WalkRouteEntity>>

    @Query("SELECT * FROM daily_routes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRoutes(): Flow<List<WalkRouteEntity>>

    @Query("SELECT * FROM daily_routes WHERE id = :id")
    suspend fun getRouteById(id: Long): WalkRouteEntity?

    @Query("SELECT * FROM daily_routes WHERE isoDate = :isoDate LIMIT 1")
    suspend fun getRouteByDate(isoDate: String): WalkRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: WalkRouteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<WalkRouteEntity>)

    @Update
    suspend fun updateRoute(route: WalkRouteEntity)

    @Query("UPDATE daily_routes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE daily_routes SET blobsJson = :blobsJson, artStyle = :artStyle, stickersJson = :stickersJson WHERE id = :id")
    suspend fun updateArtworkCustomization(id: Long, blobsJson: String, artStyle: String, stickersJson: String)

    @Delete
    suspend fun deleteRoute(route: WalkRouteEntity)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Query("UPDATE challenges SET currentValue = :value, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateProgress(id: String, value: Int, isCompleted: Boolean)

    @Query("UPDATE challenges SET isClaimed = 1 WHERE id = :id")
    suspend fun claimReward(id: String)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Query("UPDATE badges SET isUnlocked = 1, unlockedDate = :date WHERE id = :id")
    suspend fun unlockBadge(id: String, date: String)
}

@Dao
interface StoreDao {
    @Query("SELECT * FROM store_items")
    fun getAllStoreItems(): Flow<List<StoreItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreItems(items: List<StoreItemEntity>)

    @Query("UPDATE store_items SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockStoreItem(id: String)
}

@Dao
interface CustomColorDao {
    @Query("SELECT * FROM custom_colors ORDER BY createdAt DESC")
    fun getAllCustomColors(): Flow<List<CustomColorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomColor(color: CustomColorEntity): Long

    @Delete
    suspend fun deleteCustomColor(color: CustomColorEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET totalCoins = totalCoins + :coins WHERE id = 1")
    suspend fun addCoins(coins: Int)

    @Query("UPDATE user_profile SET currentXp = currentXp + :xp WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_profile SET currentLevel = :level, explorerRank = :rank WHERE id = 1")
    suspend fun updateLevelAndRank(level: Int, rank: String)

    @Query("UPDATE user_profile SET totalCoins = totalCoins - :coins WHERE id = 1 AND totalCoins >= :coins")
    suspend fun deductCoins(coins: Int): Int

    @Query("UPDATE user_profile SET totalSteps = totalSteps + :steps, totalArtworks = totalArtworks + 1 WHERE id = 1")
    suspend fun recordCompletedWalk(steps: Int)

    @Query("UPDATE user_profile SET username = :username, studentId = :studentId, hostelBlock = :hostelBlock, department = :department WHERE id = 1")
    suspend fun updateProfileInfo(username: String, studentId: String, hostelBlock: String, department: String)
}

@Database(
    entities = [
        WalkRouteEntity::class,
        ChallengeEntity::class,
        BadgeEntity::class,
        StoreItemEntity::class,
        UserProfileEntity::class,
        CustomColorEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun badgeDao(): BadgeDao
    abstract fun storeDao(): StoreDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun customColorDao(): CustomColorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pathcanvas_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
