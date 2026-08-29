package com.example.ui.home

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WalkRouteEntity
import com.example.ui.components.ArtCanvasView
import com.example.ui.components.ArtworkThumbnailCard
import com.example.ui.components.CreationCardItem
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
    var showMenuDialog by remember { mutableStateOf(false) }
    var sharingRoute by remember { mutableStateOf<WalkRouteEntity?>(null) }
    var isSharingProgress by remember { mutableStateOf(false) }
    var isViewAllGrid by remember { mutableStateOf(false) }

    val displayedRoutes = when (uiState.selectedFilter) {
        HomeFilter.ALL -> uiState.routes
        HomeFilter.FAVORITES -> uiState.favoriteRoutes
    }

    val clipboardManager = LocalClipboardManager.current
    var showLogsExpanded by remember { mutableStateOf(false) }

    // Sync Diagnostics Dialog
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
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
                                                fontWeight = FontWeight.Bold,
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

    // Quick Menu Dialog (triggered by ☰ button)
    if (showMenuDialog) {
        AlertDialog(
            onDismissRequest = { showMenuDialog = false },
            title = {
                Text("Campus Navigation & Tools", style = MaterialTheme.typography.titleMedium, color = DarkSlatePrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCardMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuDialog = false
                                onNavigateToTracker()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = AccentMint)
                            Text("Start GPS Walk Tracker", style = MaterialTheme.typography.labelLarge, color = DarkSlatePrimary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCardMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuDialog = false
                                showSyncDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = AccentLavender)
                            Text("Sync Diagnostics & Cloud", style = MaterialTheme.typography.labelLarge, color = DarkSlatePrimary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCardMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuDialog = false
                                onNavigateToProfile()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = DarkSlateSecondary)
                            Text("Profile & Achievements", style = MaterialTheme.typography.labelLarge, color = DarkSlatePrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenuDialog = false }) {
                    Text("Close", color = DarkSlateSecondary)
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
            // 1. Top App Bar: Circular Back Button (Left) & Circular Menu Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular White Back Button `<`
                Surface(
                    shape = CircleShape,
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToTracker() }
                        .testTag("top_back_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkSlatePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Circular White Hamburger Menu Button `☰`
                Surface(
                    shape = CircleShape,
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { showMenuDialog = true }
                        .testTag("top_menu_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = DarkSlatePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Header Title: "Your artworks ✦"
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Your artworks",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 33.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = DarkSlatePrimary
                )
                // Teal 4-point sparkle star ✦
                Text(
                    text = "✦",
                    fontSize = 20.sp,
                    color = SparkleTeal,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Subtitle: "Every walk. Every art." with green ribbon squiggle ~
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            ) {
                Text(
                    text = "Every walk. Every art.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        color = TextMuted
                    )
                )

                // Stylized green ribbon squiggle curve ~
                Canvas(modifier = Modifier.size(width = 46.dp, height = 12.dp)) {
                    val path = Path().apply {
                        moveTo(2f, size.height * 0.7f)
                        cubicTo(
                            size.width * 0.35f, size.height * 0.1f,
                            size.width * 0.65f, size.height * 1.1f,
                            size.width - 2f, size.height * 0.3f
                        )
                    }
                    drawPath(
                        path = path,
                        color = SparkleTeal,
                        style = Stroke(
                            width = 2.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // 3. Segmented Filter Pills (All / Favorites)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // All Pill
                val isAllSelected = uiState.selectedFilter == HomeFilter.ALL
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isAllSelected) SoftMintPill else SurfaceCard,
                    border = if (isAllSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { viewModel.setFilter(HomeFilter.ALL) }
                        .testTag("filter_all_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = DarkSlatePrimary
                        )
                    }
                }

                // Favorites Pill
                val isFavSelected = uiState.selectedFilter == HomeFilter.FAVORITES
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isFavSelected) SoftMintPill else SurfaceCard,
                    border = if (isFavSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { viewModel.setFilter(HomeFilter.FAVORITES) }
                        .testTag("filter_favorites_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavSelected) AccentLavender else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isFavSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = DarkSlatePrimary
                        )
                        if (uiState.favoriteRoutes.isNotEmpty()) {
                            Text(
                                text = "(${uiState.favoriteRoutes.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // 4. Hero Card: "TODAY'S ARTWORK"
            val todaysRoute = uiState.todaysRoute
            if (todaysRoute != null && uiState.selectedFilter == HomeFilter.ALL) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = HeroCardMint,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("todays_artwork_hero")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Top Section: Left Details & Right Square Artwork Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Details Column
                            Column(
                                modifier = Modifier.weight(1.15f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "TODAY'S ARTWORK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = HeroTealTag
                                )

                                // Bold Multi-line Date
                                val dateParts = todaysRoute.dateString.split(" ")
                                val dateLine1 = if (dateParts.size >= 2) "${dateParts[0]} ${dateParts[1]}" else todaysRoute.dateString
                                val dateLine2 = if (dateParts.size >= 3) dateParts[2] else ""

                                Column {
                                    Text(
                                        text = dateLine1,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp,
                                            lineHeight = 28.sp
                                        ),
                                        color = DarkSlatePrimary
                                    )
                                    if (dateLine2.isNotEmpty()) {
                                        Text(
                                            text = dateLine2,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 24.sp,
                                                lineHeight = 28.sp
                                            ),
                                            color = DarkSlatePrimary
                                        )
                                    }
                                }

                                Text(
                                    text = "Created from your ${todaysRoute.distanceKm} km • ${todaysRoute.durationMinutes} min journey",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Metrics Chips Row (Steps, Distance & Duration)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Steps
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(AccentMintLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "👟", fontSize = 11.sp)
                                        }
                                        Column {
                                            Text(
                                                text = "${todaysRoute.steps}",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                ),
                                                color = DarkSlatePrimary
                                            )
                                            Text(
                                                text = "steps",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    // Distance
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(AccentMintLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "📍", fontSize = 11.sp)
                                        }
                                        Column {
                                            Text(
                                                text = "${todaysRoute.distanceKm}",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                ),
                                                color = DarkSlatePrimary
                                            )
                                            Text(
                                                text = "km",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    // Duration
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(AccentLavenderContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "⏱️", fontSize = 11.sp)
                                        }
                                        Column {
                                            Text(
                                                text = "${todaysRoute.durationMinutes}m",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                ),
                                                color = DarkSlatePrimary
                                            )
                                            Text(
                                                text = "time",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Square Artwork Preview Card
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = SurfaceCard,
                                shadowElevation = 1.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                                modifier = Modifier
                                    .weight(0.95f)
                                    .aspectRatio(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    AccentMintLight.copy(alpha = 0.6f),
                                                    AccentLavenderLight.copy(alpha = 0.6f)
                                                )
                                            )
                                        )
                                        .padding(8.dp)
                                ) {
                                    // Artwork Canvas View
                                    ArtCanvasView(
                                        pointsJson = todaysRoute.pointsJson,
                                        blobsJson = todaysRoute.blobsJson,
                                        artStyle = todaysRoute.artStyle,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Sparkles on canvas frame
                                    Text(
                                        text = "✦",
                                        color = SparkleTeal,
                                        fontSize = 14.sp,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                    Text(
                                        text = "✦",
                                        color = SparkleTeal,
                                        fontSize = 12.sp,
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full-Width Purple Pill Button: "View artwork ->"
                        Button(
                            onClick = { onNavigateToStudio(todaysRoute.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentLavender
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("view_artwork_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "View artwork",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. "Your creations" Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your creations",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = DarkSlatePrimary
                )

                Text(
                    text = if (isViewAllGrid) "Show carousel <" else "View all >",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = AccentLavender,
                    modifier = Modifier
                        .clickable { isViewAllGrid = !isViewAllGrid }
                        .testTag("view_all_creations_toggle")
                )
            }

            // 6. Creations Presentation (Horizontal Carousel matching image.png or 2-column Grid)
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
            } else if (!isViewAllGrid) {
                // Horizontal Carousel matching the 4 cards in image.png
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(displayedRoutes) { index, route ->
                        CreationCardItem(
                            route = route,
                            index = index,
                            onClick = { onNavigateToStudio(route.id) },
                            onFavoriteToggle = { isFav ->
                                viewModel.toggleFavorite(route.id, isFav)
                            }
                        )
                    }
                }
            } else {
                // Render 2-column grid when "View all >" is active
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

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Summary Banner
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

            // "Places you visited" Purple Gradient Card
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
