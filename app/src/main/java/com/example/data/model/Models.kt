package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_routes")
data class WalkRouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // e.g. "23 February 2024"
    val isoDate: String,    // e.g. "2024-02-23"
    val steps: Int,
    val distanceKm: Double,
    val durationMinutes: Int,
    val calories: Int,
    val title: String,
    val shapeName: String,
    val shapeCategory: String, // "Floral", "Ribbon", "Abstract", "Fauna", "Geometric"
    val pointsJson: String,    // serialized normalized (x,y) points
    val blobsJson: String,     // serialized pastel organic blobs (x, y, radius, colorHex)
    val strokesJson: String,   // serialized stroke paths
    val stickersJson: String = "[]", // serialized landmark stickers
    val isFavorite: Boolean = false,
    val campusName: String = "VIT Campus",
    val artStyle: String = "Pastel Bloom",
    val createdAt: Long = System.currentTimeMillis()
)

data class PointF(
    val x: Float,
    val y: Float
)

data class GpsCoordinate(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val speedKmh: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class ColorBlob(
    val id: Int,
    val x: Float,
    val y: Float,
    val radiusX: Float,
    val radiusY: Float,
    val rotation: Float,
    val colorHex: String,
    val label: String = ""
)

data class LandmarkSticker(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val x: Float,
    val y: Float
)

data class ColorPalette(
    val id: String,
    val name: String,
    val colors: List<String>,
    val isLocked: Boolean = false,
    val priceCoins: Int = 0
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Daily", "Weekly", "Special"
    val targetType: String, // "STEPS", "DISTANCE", "SHAPE", "ZONES"
    val targetValue: Int,
    val currentValue: Int,
    val rewardCoins: Int,
    val iconEmoji: String,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val rarity: String, // "Common", "Rare", "Epic", "Legendary"
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null
)

@Entity(tableName = "store_items")
data class StoreItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "OUTLINE", "PALETTE", "STICKER"
    val costCoins: Int,
    val isUnlocked: Boolean = false,
    val previewHex: String = "#5D9C77",
    val styleKey: String
)

@Entity(tableName = "custom_colors")
data class CustomColorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hexCode: String,
    val category: String = "Campus Mixed", // "Campus Mixed", "Hostel Sunset", "Neon Glow"
    val redVal: Int = 120,
    val greenVal: Int = 200,
    val blueVal: Int = 180,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val username: String = "James",
    val studentId: String = "21BCE1492",
    val hostelBlock: String = "Block D (Men's Hostel)",
    val department: String = "Computer Science & Engg",
    val totalCoins: Int = 1250,
    val currentXp: Int = 680,
    val currentLevel: Int = 2,
    val explorerRank: String = "Path Pioneer",
    val totalSteps: Int = 184520,
    val totalArtworks: Int = 12,
    val activeStreakDays: Int = 7
)
