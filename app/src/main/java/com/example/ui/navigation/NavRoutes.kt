package com.example.ui.navigation

sealed class Screen(val route: String, val title: String, val iconEmoji: String) {
    data object Home : Screen("home", "Artworks", "🎨")
    data object Tracker : Screen("tracker", "Track Walk", "🚶")
    data object Studio : Screen("studio/{routeId}", "Studio", "🖌️") {
        fun createRoute(routeId: Long) = "studio/$routeId"
    }
    data object Challenges : Screen("challenges", "Quests", "🏆")
    data object Store : Screen("store", "Store", "🛍️")
}
