package com.example.ui.navigation

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.challenges.ChallengesScreen
import com.example.ui.challenges.ChallengesViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.map.CampusMapScreen
import com.example.ui.map.CampusMapViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.store.StoreScreen
import com.example.ui.store.StoreViewModel
import com.example.ui.studio.ColoringStudioScreen
import com.example.ui.studio.ColoringStudioViewModel
import com.example.ui.tracker.MapTrackerScreen
import com.example.ui.tracker.MapTrackerViewModel
import com.example.ui.theme.*

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val isStudioScreen = currentRoute?.startsWith("studio") == true

    Scaffold(
        containerColor = MintBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isStudioScreen) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Tab
                        val isHome = currentRoute == Screen.Home.route || currentRoute == null
                        BottomNavItem(
                            icon = if (isHome) Icons.Filled.GridView else Icons.Outlined.GridView,
                            label = "Artworks",
                            isSelected = isHome,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            },
                            testTag = "nav_home"
                        )

                        // Tracker Tab
                        val isTracker = currentRoute == Screen.Tracker.route
                        BottomNavItem(
                            icon = if (isTracker) Icons.Filled.DirectionsWalk else Icons.Outlined.DirectionsWalk,
                            label = "Track Walk",
                            isSelected = isTracker,
                            onClick = {
                                navController.navigate(Screen.Tracker.route)
                            },
                            testTag = "nav_tracker"
                        )

                        // Campus Map Tab
                        val isMap = currentRoute == Screen.Map.route
                        BottomNavItem(
                            icon = if (isMap) Icons.Filled.Map else Icons.Outlined.Map,
                            label = "Campus",
                            isSelected = isMap,
                            onClick = {
                                navController.navigate(Screen.Map.route)
                            },
                            testTag = "nav_map"
                        )

                        // Challenges Tab
                        val isChallenges = currentRoute == Screen.Challenges.route
                        BottomNavItem(
                            icon = if (isChallenges) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                            label = "Quests",
                            isSelected = isChallenges,
                            onClick = {
                                navController.navigate(Screen.Challenges.route)
                            },
                            testTag = "nav_challenges"
                        )

                        // Store Tab
                        val isStore = currentRoute == Screen.Store.route
                        BottomNavItem(
                            icon = if (isStore) Icons.Filled.Palette else Icons.Outlined.Palette,
                            label = "Store",
                            isSelected = isStore,
                            onClick = {
                                navController.navigate(Screen.Store.route)
                            },
                            testTag = "nav_store"
                        )

                        // Profile Tab
                        val isProfile = currentRoute == Screen.Profile.route
                        BottomNavItem(
                            icon = if (isProfile) Icons.Filled.Person else Icons.Outlined.Person,
                            label = "Profile",
                            isSelected = isProfile,
                            onClick = {
                                navController.navigate(Screen.Profile.route)
                            },
                            testTag = "nav_profile"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                val homeVm: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeVm,
                    onNavigateToStudio = { routeId ->
                        navController.navigate(Screen.Studio.createRoute(routeId))
                    },
                    onNavigateToTracker = {
                        navController.navigate(Screen.Tracker.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(Screen.Tracker.route) {
                val trackerVm: MapTrackerViewModel = viewModel()
                MapTrackerScreen(
                    viewModel = trackerVm,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onArtworkGenerated = { newId ->
                        navController.navigate(Screen.Studio.createRoute(newId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.Studio.route,
                arguments = listOf(navArgument("routeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routeId = backStackEntry.arguments?.getLong("routeId") ?: 1L
                val studioVm = remember(routeId) {
                    ColoringStudioViewModel(application, routeId)
                }
                ColoringStudioScreen(
                    viewModel = studioVm,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Challenges.route) {
                val challengesVm: ChallengesViewModel = viewModel()
                ChallengesScreen(
                    viewModel = challengesVm,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Map.route) {
                val mapVm: CampusMapViewModel = viewModel()
                CampusMapScreen(
                    viewModel = mapVm,
                    onNavigateToTracker = { presetRouteName ->
                        navController.navigate(Screen.Tracker.route)
                    },
                    onNavigateToChallenges = {
                        navController.navigate(Screen.Challenges.route)
                    }
                )
            }

            composable(Screen.Store.route) {
                val storeVm: StoreViewModel = viewModel()
                StoreScreen(
                    viewModel = storeVm,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                val profileVm: ProfileViewModel = viewModel()
                ProfileScreen(
                    viewModel = profileVm,
                    onNavigateToArtworks = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToColourGallery = {
                        navController.navigate(Screen.Store.route)
                    },
                    onNavigateToQuests = {
                        navController.navigate(Screen.Challenges.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) BlackPill else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) DarkSlatePrimary else TextMuted
        )
    }
}
