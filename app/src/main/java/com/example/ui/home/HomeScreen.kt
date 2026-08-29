package com.example.ui.home

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WalkRouteEntity
import com.example.ui.components.ArtCanvasView
import com.example.ui.components.ArtworkThumbnailCard
import com.example.ui.components.StoryCardView
import com.example.ui.theme.*
import com.example.util.ArtworkShareHelper
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToStudio: (Long) -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showSyncDialog by remember { mutableStateOf(false) }
    var sharingRoute by remember { mutableStateOf<WalkRouteEntity?>(null) }
    var isSharingProgress by remember { mutableStateOf(false) }

    val displayedRoutes = when (uiState.selectedFilter) {
        HomeFilter.ALL -> uiState.routes
        HomeFilter.FAVORITES -> uiState.favoriteRoutes
    }

    val clipboardManager = LocalClipboardManager.current
    var showLogsExpanded by remember { mutableStateOf(false) }

    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync Status",
                        tint = AccentMint
                    )
                    Text(
                        text = "Sync Diagnostics & Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkSlatePrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Local SQLite Room Health
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCardMuted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "💾", fontSize = 16.sp)
                                Text(
                                    text = "Local Storage (Room DB): Active & Safe",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                    color = DarkSlatePrimary
                                )
                            }
                            Text(
                                text = "All ${uiState.routes.size} recorded walk routes, custom pigments, explorer XP, and unlocked badges are stored on your device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkSlateSecondary
                            )
                        }
                    }

                    // Cloud Sync State
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (val state = uiState.syncState) {
                            is com.example.data.sync.SyncState.Success -> if (state.isCloudSynced) AccentMintLight else AccentLavenderLight
                            is com.example.data.sync.SyncState.Error -> Color(0xFFFFEBEE)
                            else -> SurfaceCardMuted
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "☁️", fontSize = 16.sp)
                                Text(
                                    text = "Firebase Cloud Sync Status",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                    color = DarkSlatePrimary
                                )
                            }
                            Text(
                                text = when (val state = uiState.syncState) {
                                    is com.example.data.sync.SyncState.Syncing -> "Step: ${state.currentStep}"
                                    is com.example.data.sync.SyncState.Success -> state.message
                                    is com.example.data.sync.SyncState.Error -> "${state.errorMessage}\n\nCause: ${state.rootCauseCategory}\nRecommendation: ${state.recommendation}"
                                    else -> "Tap 'Sync Now' to test connection and synchronize with Firestore Cloud."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkSlateSecondary
                            )
                        }
                    }

                    // Diagnostic Logs Terminal
                    val logs = when (val state = uiState.syncState) {
                        is com.example.data.sync.SyncState.Syncing -> state.diagnosticLogs
                        is com.example.data.sync.SyncState.Success -> state.diagnosticLogs
                        is com.example.data.sync.SyncState.Error -> state.diagnosticLogs
                        else -> emptyList()
                    }

                    if (logs.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "📟", fontSize = 14.sp)
                                        Text(
                                            text = "Diagnostics Log (${logs.size})",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = {
                                                val logText = logs.joinToString("\n") { it.formatted() }
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(logText))
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Copy", color = AccentMint, fontSize = 11.sp)
                                        }

                                        TextButton(
                                            onClick = { showLogsExpanded = !showLogsExpanded },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                if (showLogsExpanded) "Collapse" else "Expand",
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                val displayedLogs = if (showLogsExpanded) logs else logs.takeLast(4)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    displayedLogs.forEach { entry ->
                                        val entryColor = when (entry.level) {
                                            "ERROR" -> Color(0xFFFF8A80)
                                            "WARN" -> Color(0xFFFFD180)
                                            "SUCCESS" -> Color(0xFFB9F6CA)
                                            "DEBUG" -> Color(0xFF90CAF9)
                                            else -> Color(0xFFE2E8F0)
                                        }
                                        Text(
                                            text = entry.formatted(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                color = entryColor
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val isSyncing = uiState.syncState is com.example.data.sync.SyncState.Syncing
                val isError = uiState.syncState is com.example.data.sync.SyncState.Error
                Button(
                    onClick = {
                        viewModel.syncFirestore()
                    },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isError) Color(0xFFD32F2F) else BlackPill
                    ),
                    modifier = Modifier.testTag("dialog_retry_sync_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Text(text = "Syncing...", color = Color.White)
                        } else {
                            Icon(
                                imageVector = if (isError) Icons.Default.Refresh else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isError) "Retry Cloud Sync" else "Run Sync",
                                color = Color.White
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncDialog = false }) {
                    Text(text = "Close", color = DarkSlateSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = MintBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .clickable { onNavigateToTracker() }
                        .testTag("map_shortcut_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Track Walk",
                        tint = DarkSlatePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Decorative stylized green ribbon motif and cloud sync in top corner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cloud Sync Button / Badge
                    val currentSync = uiState.syncState
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentSync) {
                            is com.example.data.sync.SyncState.Syncing -> AccentLavenderLight
                            is com.example.data.sync.SyncState.Success -> if (currentSync.isCloudSynced) AccentMintLight else AccentLavenderLight
                            is com.example.data.sync.SyncState.Error -> Color(0xFFFFEBEE)
                            else -> SurfaceCard
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentSync is com.example.data.sync.SyncState.Error) Color(0xFFFFCDD2) else BorderSubtle
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (currentSync is com.example.data.sync.SyncState.Error || currentSync is com.example.data.sync.SyncState.Idle) {
                                    viewModel.syncFirestore()
                                } else {
                                    showSyncDialog = true
                                }
                            }
                            .testTag("top_bar_sync_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (currentSync is com.example.data.sync.SyncState.Syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = AccentLavender
                                )
                            } else {
                                Icon(
                                    imageVector = when (currentSync) {
                                        is com.example.data.sync.SyncState.Error -> Icons.Default.SyncProblem
                                        is com.example.data.sync.SyncState.Success -> if (currentSync.isCloudSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue
                                        else -> Icons.Default.CloudSync
                                    },
                                    contentDescription = "Sync Cloud",
                                    tint = if (currentSync is com.example.data.sync.SyncState.Error) Color(0xFFD32F2F) else DarkSlatePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = when (currentSync) {
                                    is com.example.data.sync.SyncState.Syncing -> "Syncing..."
                                    is com.example.data.sync.SyncState.Success -> if (currentSync.isCloudSynced) "Cloud Synced" else "Saved Locally"
                                    is com.example.data.sync.SyncState.Error -> "Sync Failed • Tap Retry"
                                    else -> "Sync Cloud"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (currentSync is com.example.data.sync.SyncState.Error) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                ),
                                color = if (currentSync is com.example.data.sync.SyncState.Error) Color(0xFFD32F2F) else DarkSlatePrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .clickable { onNavigateToProfile() }
                            .testTag("more_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More",
                            tint = DarkSlatePrimary
                        )
                    }
                }
            }

            // Header Title & Slogan
            Text(
                text = "Your artworks",
                style = MaterialTheme.typography.displayLarge,
                color = DarkSlatePrimary
            )
            Text(
                text = "Every walk. Every art.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Segmented Filter Pills (All / Favorites)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // All Pill
                val isAllSelected = uiState.selectedFilter == HomeFilter.ALL
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isAllSelected) BlackPill else SurfaceCard,
                    border = if (isAllSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .clickable { viewModel.setFilter(HomeFilter.ALL) }
                        .testTag("filter_all_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GridView,
                            contentDescription = null,
                            tint = if (isAllSelected) Color.White else DarkSlatePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isAllSelected) Color.White else DarkSlatePrimary
                        )
                    }
                }

                // Favorites Pill
                val isFavSelected = uiState.selectedFilter == HomeFilter.FAVORITES
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isFavSelected) BlackPill else SurfaceCard,
                    border = if (isFavSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .clickable { viewModel.setFilter(HomeFilter.FAVORITES) }
                        .testTag("filter_favorites_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavSelected) Color.White else DarkSlatePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isFavSelected) Color.White else DarkSlatePrimary
                        )
                        if (uiState.favoriteRoutes.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isFavSelected) AccentLavender else AccentLavenderContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.favoriteRoutes.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isFavSelected) Color.White else DarkSlatePrimary
                                )
                            }
                        }
                    }
                }
            }

            // Sync Failure & Visual Retry Banner
            AnimatedVisibility(
                visible = uiState.syncState is com.example.data.sync.SyncState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val errorState = uiState.syncState as? com.example.data.sync.SyncState.Error
                if (errorState != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFF1F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("sync_error_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header Row with Icon and Category Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFE4E6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SyncProblem,
                                            contentDescription = "Sync Issue",
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "Cloud Sync Incomplete",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        ),
                                        color = Color(0xFF9F1239)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFE4E6)
                                ) {
                                    Text(
                                        text = errorState.rootCauseCategory,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = Color(0xFFBE123C),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Connection Status & Recommendation Details
                            Text(
                                text = errorState.errorMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                ),
                                color = Color(0xFF881337)
                            )

                            Text(
                                text = "💡 ${errorState.recommendation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4C0519)
                            )

                            // Local Storage Safe Guarantee
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33FFFFFF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "All ${errorState.localRecordsSaved} artworks are safely preserved in on-device database.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }

                            // Action Buttons Row: Retry Sync and View Logs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.syncFirestore() },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("retry_sync_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Retry Sync",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Retry Sync",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showSyncDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("view_sync_logs_button")
                                ) {
                                    Text(
                                        text = "Diagnostics & Logs",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF9F1239)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hero Card: "TODAY'S ARTWORK"
            val todaysRoute = uiState.todaysRoute
            if (todaysRoute != null && uiState.selectedFilter == HomeFilter.ALL) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("todays_artwork_hero")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Details Column
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "TODAY'S ARTWORK",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentTeal
                            )
                            Text(
                                text = todaysRoute.dateString,
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkSlatePrimary
                            )
                            Text(
                                text = "Created from your ${todaysRoute.distanceKm} km journey",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )

                            // Metrics Chips Row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                // Steps
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(AccentMintLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "👟", fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "${todaysRoute.steps}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = DarkSlatePrimary
                                        )
                                        Text(
                                            text = "steps",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                // Distance
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(AccentLavenderLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "📍", fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "${todaysRoute.distanceKm}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = DarkSlatePrimary
                                        )
                                        Text(
                                            text = "km",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            // Action Buttons Row: View Artwork & Quick Share
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToStudio(todaysRoute.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentLavender
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("view_artwork_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "View artwork",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = SurfaceCardMuted,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable { sharingRoute = todaysRoute }
                                        .testTag("hero_share_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share Today's Artwork",
                                            tint = DarkSlatePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Right Canvas Preview
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceCardMuted)
                                .padding(8.dp)
                        ) {
                            ArtCanvasView(
                                pointsJson = todaysRoute.pointsJson,
                                blobsJson = todaysRoute.blobsJson,
                                artStyle = todaysRoute.artStyle,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // "Your creations" Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your creations",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkSlatePrimary
                )
                Text(
                    text = "${displayedRoutes.size} artworks",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentLavender
                )
            }

            // Creations Grid (4 rows x 2 columns or dynamic)
            if (displayedRoutes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🎨", fontSize = 36.sp)
                        Text(
                            text = if (uiState.selectedFilter == HomeFilter.FAVORITES) "No favorite artworks yet" else "No walking art recorded yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )
                        Text(
                            text = "Walk around campus to create your next digital masterpiece!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                // Render 2-column grid chunked
                val chunked = displayedRoutes.chunked(2)
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { route ->
                            ArtworkThumbnailCard(
                                route = route,
                                onClick = { onNavigateToStudio(route.id) },
                                onFavoriteToggle = { isFav ->
                                    viewModel.toggleFavorite(route.id, isFav)
                                },
                                onShareClick = {
                                    sharingRoute = route
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Summary Banner (e.g. "February 2024 • 12 walks • 12 artworks")
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentMintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = AccentMint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "February 2024",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkSlatePrimary
                            )
                            Text(
                                text = "${uiState.routes.size} walks • ${uiState.routes.size} artworks",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                        }
                    }

                    // Green walking path icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentMintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // "Places you visited" Purple Gradient Card (from Mockup 1)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AccentLavender,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTracker() }
                    .testTag("places_visited_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "This month",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Places you visited",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "7 campus zones explored",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLavenderLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationSearching,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Share Story Modal Dialog for Selected Artwork
    if (sharingRoute != null) {
        val routeToShare = sharingRoute!!
        Dialog(onDismissRequest = { sharingRoute = null }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SurfaceCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Share Path Artwork",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )
                        IconButton(onClick = { sharingRoute = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    // 9:16 Social Story Card Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(350.dp)
                    ) {
                        StoryCardView(
                            route = routeToShare,
                            brushStyleKey = routeToShare.artStyle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Primary Action: Share High-Res Story Image & Caption
                    Button(
                        onClick = {
                            if (!isSharingProgress) {
                                isSharingProgress = true
                                coroutineScope.launch {
                                    ArtworkShareHelper.shareArtwork(
                                        context = context,
                                        route = routeToShare,
                                        studentName = "Campus Artist",
                                        brushStyleKey = routeToShare.artStyle
                                    ) {
                                        isSharingProgress = false
                                        sharingRoute = null
                                    }
                                }
                            }
                        },
                        enabled = !isSharingProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLavender),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_modal_share_image_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSharingProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Text("Preparing Image...", color = Color.White)
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Text("Share to WhatsApp / Instagram", color = Color.White)
                            }
                        }
                    }

                    // Secondary Quick Share Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val caption = ArtworkShareHelper.createShareCaption(routeToShare, "Campus Artist")
                                val textIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, caption)
                                    putExtra(Intent.EXTRA_SUBJECT, "My Walk Artwork: ${routeToShare.shapeName}")
                                }
                                context.startActivity(Intent.createChooser(textIntent, "Share text summary"))
                                sharingRoute = null
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Text("Share Text", color = DarkSlatePrimary, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val caption = ArtworkShareHelper.createShareCaption(routeToShare, "Campus Artist")
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(caption))
                                android.widget.Toast.makeText(context, "Caption copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Text("Copy Caption", color = DarkSlatePrimary, fontSize = 12.sp)
                        }
                    }

                    // Open in Coloring Studio
                    TextButton(
                        onClick = {
                            val id = routeToShare.id
                            sharingRoute = null
                            onNavigateToStudio(id)
                        }
                    ) {
                        Text("Customize in Coloring Studio ➔", color = DarkSlateSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
