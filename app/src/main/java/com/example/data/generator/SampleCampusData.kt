package com.example.data.generator

import com.example.data.model.*

object SampleCampusData {

    fun getInitialRoutes(): List<WalkRouteEntity> {
        val list = mutableListOf<WalkRouteEntity>()

        // 23 Feb 2024 (Today's Hero Artwork from screenshot)
        val (points0, blobs0) = RouteArtEngine.generateSampleWalk(0)
        list.add(
            WalkRouteEntity(
                id = 1,
                dateString = "23 February 2024",
                isoDate = "2024-02-23",
                steps = 7842,
                distanceKm = 5.6,
                durationMinutes = 68,
                calories = 310,
                title = "Today's Campus Bloom",
                shapeName = "Whimsical Bloom",
                shapeCategory = "Floral",
                pointsJson = RouteArtEngine.pointsToJson(points0),
                blobsJson = RouteArtEngine.blobsToJson(blobs0),
                strokesJson = "[]",
                stickersJson = """[{"id":"s1","name":"Tech Tower","iconEmoji":"🏫","x":320.0,"y":340.0},{"id":"s2","name":"Food Court","iconEmoji":"☕","x":490.0,"y":470.0}]""",
                isFavorite = false,
                campusName = "VIT Main Campus",
                artStyle = "Pastel Bloom",
                createdAt = 1708684800000L // 23 Feb 2024
            )
        )

        // 22 Feb 2024 (Butterfly motif)
        val (points1, blobs1) = RouteArtEngine.generateSampleWalk(1)
        list.add(
            WalkRouteEntity(
                id = 2,
                dateString = "22 February 2024",
                isoDate = "2024-02-22",
                steps = 9120,
                distanceKm = 6.4,
                durationMinutes = 75,
                calories = 360,
                title = "Hostel to Library Stride",
                shapeName = "Cosmic Butterfly",
                shapeCategory = "Fauna",
                pointsJson = RouteArtEngine.pointsToJson(points1),
                blobsJson = RouteArtEngine.blobsToJson(blobs1),
                strokesJson = "[]",
                isFavorite = true,
                campusName = "Central Library Block",
                artStyle = "Lavender Glow",
                createdAt = 1708598400000L
            )
        )

        // 21 Feb 2024 (Ribbon flow)
        val (points2, blobs2) = RouteArtEngine.generateSampleWalk(2)
        list.add(
            WalkRouteEntity(
                id = 3,
                dateString = "21 February 2024",
                isoDate = "2024-02-21",
                steps = 6450,
                distanceKm = 4.8,
                durationMinutes = 52,
                calories = 270,
                title = "Sports Complex Loop",
                shapeName = "Infinity Ribbon",
                shapeCategory = "Ribbon",
                pointsJson = RouteArtEngine.pointsToJson(points2),
                blobsJson = RouteArtEngine.blobsToJson(blobs2),
                strokesJson = "[]",
                isFavorite = false,
                campusName = "Outdoor Arena",
                artStyle = "Pastel Flow",
                createdAt = 1708512000000L
            )
        )

        // 20 Feb 2024 (Leaf shape)
        val (points3, blobs3) = RouteArtEngine.generateSampleWalk(3)
        list.add(
            WalkRouteEntity(
                id = 4,
                dateString = "20 February 2024",
                isoDate = "2024-02-20",
                steps = 8300,
                distanceKm = 5.9,
                durationMinutes = 64,
                calories = 330,
                title = "Botanical Green Path",
                shapeName = "Leaf of Knowledge",
                shapeCategory = "Floral",
                pointsJson = RouteArtEngine.pointsToJson(points3),
                blobsJson = RouteArtEngine.blobsToJson(blobs3),
                strokesJson = "[]",
                isFavorite = true,
                campusName = "Academic Quad",
                artStyle = "Botanical Ivy",
                createdAt = 1708425600000L
            )
        )

        // 19 Feb 2024
        val (points4, blobs4) = RouteArtEngine.generateSampleWalk(4)
        list.add(
            WalkRouteEntity(
                id = 5,
                dateString = "19 February 2024",
                isoDate = "2024-02-19",
                steps = 5200,
                distanceKm = 3.9,
                durationMinutes = 45,
                calories = 210,
                title = "Evening Canteen Run",
                shapeName = "Amber Loop",
                shapeCategory = "Abstract",
                pointsJson = RouteArtEngine.pointsToJson(points4),
                blobsJson = RouteArtEngine.blobsToJson(blobs4),
                strokesJson = "[]",
                isFavorite = false,
                campusName = "Gazebo & Cafeteria",
                artStyle = "Sunset Glow",
                createdAt = 1708339200000L
            )
        )

        // 18 Feb 2024
        val (points5, blobs5) = RouteArtEngine.generateSampleWalk(5)
        list.add(
            WalkRouteEntity(
                id = 6,
                dateString = "18 February 2024",
                isoDate = "2024-02-18",
                steps = 7100,
                distanceKm = 5.1,
                durationMinutes = 58,
                calories = 285,
                title = "Hostel Block Exploration",
                shapeName = "Emerald Tri-Star",
                shapeCategory = "Geometric",
                pointsJson = RouteArtEngine.pointsToJson(points5),
                blobsJson = RouteArtEngine.blobsToJson(blobs5),
                strokesJson = "[]",
                isFavorite = true,
                campusName = "Hostel Ring Road",
                artStyle = "Spring Bloom",
                createdAt = 1708252800000L
            )
        )

        // 17 Feb 2024
        val (points6, blobs6) = RouteArtEngine.generateSampleWalk(0)
        list.add(
            WalkRouteEntity(
                id = 7,
                dateString = "17 February 2024",
                isoDate = "2024-02-17",
                steps = 6800,
                distanceKm = 4.9,
                durationMinutes = 50,
                calories = 265,
                title = "Classroom Switch Stride",
                shapeName = "Pink Coral Blossom",
                shapeCategory = "Floral",
                pointsJson = RouteArtEngine.pointsToJson(points6),
                blobsJson = RouteArtEngine.blobsToJson(blobs6),
                strokesJson = "[]",
                isFavorite = false,
                campusName = "Silver Jubilee Tower",
                artStyle = "Sunset Glow",
                createdAt = 1708166400000L
            )
        )

        // 16 Feb 2024
        val (points7, blobs7) = RouteArtEngine.generateSampleWalk(2)
        list.add(
            WalkRouteEntity(
                id = 8,
                dateString = "16 February 2024",
                isoDate = "2024-02-16",
                steps = 8900,
                distanceKm = 6.2,
                durationMinutes = 70,
                calories = 345,
                title = "Perimeter Campus Trek",
                shapeName = "Azure Cloud Ribbon",
                shapeCategory = "Ribbon",
                pointsJson = RouteArtEngine.pointsToJson(points7),
                blobsJson = RouteArtEngine.blobsToJson(blobs7),
                strokesJson = "[]",
                isFavorite = false,
                campusName = "Perimeter Pathway",
                artStyle = "Pastel Bloom",
                createdAt = 1708080000000L
            )
        )

        return list
    }

    fun getInitialChallenges(): List<ChallengeEntity> {
        return listOf(
            ChallengeEntity(
                id = "ch_daily_8k",
                title = "Campus 8,000 Step Stride",
                description = "Walk at least 8,000 steps today navigating between classes and hostels.",
                category = "Daily",
                targetType = "STEPS",
                targetValue = 8000,
                currentValue = 7842,
                rewardCoins = 100,
                iconEmoji = "👟",
                isCompleted = false,
                isClaimed = false
            ),
            ChallengeEntity(
                id = "ch_shape_loop",
                title = "Closed Loop Artist",
                description = "Walk a full loop that connects back to your starting point to generate a closed artwork zone.",
                category = "Daily",
                targetType = "SHAPE",
                targetValue = 1,
                currentValue = 1,
                rewardCoins = 150,
                iconEmoji = "🎨",
                isCompleted = true,
                isClaimed = false
            ),
            ChallengeEntity(
                id = "ch_hostel_derby",
                title = "Hostel Wing Derby: 25 km",
                description = "Accumulate 25 km of walking this week to boost Block D's ranking on the leaderboard.",
                category = "Weekly",
                targetType = "DISTANCE",
                targetValue = 25,
                currentValue = 18,
                rewardCoins = 350,
                iconEmoji = "🏆",
                isCompleted = false,
                isClaimed = false
            ),
            ChallengeEntity(
                id = "ch_zones_4",
                title = "Campus Pioneer",
                description = "Visit 4 distinct campus zones (Library, Canteen, Quad, Sports Oval) in a single day.",
                category = "Special",
                targetType = "ZONES",
                targetValue = 4,
                currentValue = 4,
                rewardCoins = 250,
                iconEmoji = "🗺️",
                isCompleted = true,
                isClaimed = true
            )
        )
    }

    fun getInitialBadges(): List<BadgeEntity> {
        return listOf(
            BadgeEntity(
                id = "bdg_first_canvas",
                title = "First Masterpiece",
                description = "Minted your first daily walk into a digital artwork.",
                iconEmoji = "🖼️",
                rarity = "Common",
                isUnlocked = true,
                unlockedDate = "16 Feb 2024"
            ),
            BadgeEntity(
                id = "bdg_color_master",
                title = "Color Maestro",
                description = "Customized and colored 5 walk artworks in the Studio.",
                iconEmoji = "🎨",
                rarity = "Rare",
                isUnlocked = true,
                unlockedDate = "20 Feb 2024"
            ),
            BadgeEntity(
                id = "bdg_100k_club",
                title = "100k Campus Titan",
                description = "Surpassed 100,000 total steps on campus grounds.",
                iconEmoji = "⚡",
                rarity = "Epic",
                isUnlocked = true,
                unlockedDate = "22 Feb 2024"
            ),
            BadgeEntity(
                id = "bdg_night_walker",
                title = "Nocturnal Stargazer",
                description = "Completed an art walk after 9:00 PM.",
                iconEmoji = "🌙",
                rarity = "Rare",
                isUnlocked = false
            ),
            BadgeEntity(
                id = "bdg_loop_legend",
                title = "Infinity Loop Master",
                description = "Created a multi-petal floral walking geometry.",
                iconEmoji = "🌸",
                rarity = "Legendary",
                isUnlocked = false
            )
        )
    }

    fun getInitialStoreItems(): List<StoreItemEntity> {
        return listOf(
            StoreItemEntity(
                id = "br_ink_black",
                title = "Fine Ink Pen",
                description = "Crisp, classic architectural black line with smooth tapering.",
                category = "OUTLINE",
                costCoins = 0,
                isUnlocked = true,
                previewHex = "#1E2A24",
                styleKey = "INK"
            ),
            StoreItemEntity(
                id = "br_cyber_glow",
                title = "Cyber Neon Pulse",
                description = "Electrifying glowing cyan/violet vector stroke for night routes.",
                category = "OUTLINE",
                costCoins = 250,
                isUnlocked = true,
                previewHex = "#00F5D4",
                styleKey = "NEON"
            ),
            StoreItemEntity(
                id = "br_watercolor",
                title = "Aquarelle Flow",
                description = "Soft textured watercolor stroke that bleeds gently into the canvas.",
                category = "OUTLINE",
                costCoins = 300,
                isUnlocked = false,
                previewHex = "#7B2CBF",
                styleKey = "WATERCOLOR"
            ),
            StoreItemEntity(
                id = "br_chalk",
                title = "Campus Chalk",
                description = "Textured pavement chalk outline resembling campus graffiti art.",
                category = "OUTLINE",
                costCoins = 200,
                isUnlocked = false,
                previewHex = "#F4A261",
                styleKey = "CHALK"
            ),
            StoreItemEntity(
                id = "pal_pastel_bloom",
                title = "Pastel Bloom Palette",
                description = "Soft mints, delicate lavenders, and honey peach highlights.",
                category = "PALETTE",
                costCoins = 0,
                isUnlocked = true,
                previewHex = "#BCEAD5",
                styleKey = "PALETTE_PASTEL"
            ),
            StoreItemEntity(
                id = "pal_sunset_glow",
                title = "Golden Hour Dusk",
                description = "Rich terracotta, warm saffron, and soothing twilight plum.",
                category = "PALETTE",
                costCoins = 200,
                isUnlocked = true,
                previewHex = "#F4A261",
                styleKey = "PALETTE_SUNSET"
            ),
            StoreItemEntity(
                id = "pal_cyber_matrix",
                title = "Tokyo Cyber Matrix",
                description = "Futuristic electric greens, deep obsidian, and ultraviolet.",
                category = "PALETTE",
                costCoins = 350,
                isUnlocked = false,
                previewHex = "#00F5D4",
                styleKey = "PALETTE_CYBER"
            )
        )
    }

    fun getInitialCustomColors(): List<CustomColorEntity> {
        return listOf(
            CustomColorEntity(
                id = 1,
                name = "Sunset Hostel Gold",
                hexCode = "#FFB703",
                category = "Hostel Sunset",
                redVal = 255,
                greenVal = 183,
                blueVal = 3
            ),
            CustomColorEntity(
                id = 2,
                name = "Cyber Library Cyan",
                hexCode = "#00F5D4",
                category = "Neon Glow",
                redVal = 0,
                greenVal = 245,
                blueVal = 212
            ),
            CustomColorEntity(
                id = 3,
                name = "Botanical Green",
                hexCode = "#52B788",
                category = "Campus Mixed",
                redVal = 82,
                greenVal = 183,
                blueVal = 136
            ),
            CustomColorEntity(
                id = 4,
                name = "Lavender Lawn Mist",
                hexCode = "#CDB4DB",
                category = "Campus Mixed",
                redVal = 205,
                greenVal = 180,
                blueVal = 219
            )
        )
    }
}
