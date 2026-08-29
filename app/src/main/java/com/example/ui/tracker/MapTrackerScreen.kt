package com.example.ui.tracker

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.core.content.ContextCompat
import com.example.ui.components.CampusMapView
import com.example.ui.components.ContinuousGpsPathOverlay
import com.example.ui.components.StatsChip
import com.example.ui.theme.*

@Composable
fun MapTrackerScreen(
    viewModel: MapTrackerViewModel,
    onNavigateBack: () -> Unit,
    onArtworkGenerated: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSimulationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.startTracking()
        }
    }

    fun handleStartClick() {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.startTracking()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        containerColor = MintBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .clickable { onNavigateBack() }
                        .testTag("tracker_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkSlatePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Campus Route Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkSlatePrimary
                    )
                    val statusText = when {
                        uiState.isSimulatingDemo -> "⚡ 15s Simulation Active"
                        uiState.isTracking && uiState.isUsingRealGps -> if (uiState.gpsAccuracyMeters > 0) "🛰️ Fused GPS (±${uiState.gpsAccuracyMeters.toInt()}m)" else "🛰️ Fused GPS Active"
                        uiState.isTracking -> "🔴 Live Tracking Active"
                        else -> "Ready to Walk"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isTracking) AccentMint else TextMuted
                    )
                }

                // Route Preset Simulator Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .clickable { showSimulationDialog = true }
                        .testTag("route_presets_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = "Presets",
                        tint = DarkSlatePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Live Metrics Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsChip(
                    icon = Icons.Default.DirectionsWalk,
                    valueText = "${uiState.stepCount}",
                    labelText = "steps",
                    modifier = Modifier.weight(1f),
                    iconTint = AccentMint
                )
                StatsChip(
                    icon = Icons.Default.Place,
                    valueText = uiState.formattedDistanceText,
                    labelText = if (uiState.preferMetersUnit) "distance (tap for km)" else "distance (tap for m)",
                    modifier = Modifier.weight(1.1f),
                    iconTint = AccentLavender,
                    onClick = { viewModel.toggleDistanceUnit() }
                )
                StatsChip(
                    icon = Icons.Default.Timer,
                    valueText = formatTimer(uiState.durationSeconds),
                    labelText = "active time",
                    modifier = Modifier.weight(1.1f),
                    iconTint = AccentPeach
                )
            }

            // Mode Switcher: Campus Map vs Continuous GPS Path vs Offline Campus Navigation
            var activeTrackerTab by remember { mutableIntStateOf(0) }
            var selectedLandmark by remember { mutableStateOf<com.example.ui.components.CampusLandmark?>(null) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (activeTrackerTab == 0) BlackPill else SurfaceCard,
                    border = if (activeTrackerTab == 0) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTrackerTab = 0 }
                ) {
                    Text(
                        text = "🎨 Campus",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (activeTrackerTab == 0) Color.White else DarkSlatePrimary,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (activeTrackerTab == 1) BlackPill else SurfaceCard,
                    border = if (activeTrackerTab == 1) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .weight(1.3f)
                        .clickable { activeTrackerTab = 1 }
                ) {
                    Text(
                        text = "🛰️ GPS Path Canvas",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (activeTrackerTab == 1) Color.White else DarkSlatePrimary,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (activeTrackerTab == 2) BlackPill else SurfaceCard,
                    border = if (activeTrackerTab == 2) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTrackerTab = 2 }
                ) {
                    Text(
                        text = "🧭 Offline Nav",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (activeTrackerTab == 2) Color.White else DarkSlatePrimary,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Core Philosophy Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AccentLavenderLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "✨", fontSize = 14.sp)
                    Text(
                        text = if (activeTrackerTab == 1) "Continuous latitude/longitude polyline canvas generated in real-time." else "Live your normal day — your everyday steps unexpectedly turn into art!",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkSlatePrimary
                    )
                }
            }

            // Speed filter / Shape preview indicator (Art mode) OR Waypoint guidance (Offline Nav mode)
            if (activeTrackerTab == 0 || activeTrackerTab == 1) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = if (activeTrackerTab == 1) "🛰️" else "🎨", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = if (activeTrackerTab == 1) "Real-Time Path Projection" else "Detected Shape Motif",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = if (activeTrackerTab == 1) "${uiState.gpsCoordinates.size} Waypoints Collected" else "${uiState.detectedShapeName} (${uiState.detectedCategory})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = DarkSlatePrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.isSpeedExceeded) Color(0xFFFFEBEE) else AccentMintLight
                        ) {
                            Text(
                                text = if (uiState.isSpeedExceeded) "⚠️ Speed > 15 km/h" else "🚶 ${uiState.speedKmh} km/h (Walking)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isSpeedExceeded) Color(0xFFC62828) else DarkSlatePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // Offline Navigation Waypoint & Compass Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentLavenderContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "🧭", fontSize = 18.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = if (selectedLandmark != null) "Navigating to ${selectedLandmark?.name}" else "Offline Campus Compass",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DarkSlatePrimary
                                )
                                val distMeters = if (selectedLandmark != null) 380 else 120
                                val minsWalk = if (selectedLandmark != null) 5 else 2
                                Text(
                                    text = if (selectedLandmark != null) "Direct Bearing: NE 42° • ~$distMeters m (~$minsWalk min walk)" else "Tap any campus building on map for offline direction",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        if (selectedLandmark != null) {
                            IconButton(onClick = { selectedLandmark = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear target", tint = TextMuted)
                            }
                        }
                    }
                }
            }

            // Interactive Map View Container (Campus Vector Map vs Continuous GPS Path Overlay)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (activeTrackerTab == 1) {
                    ContinuousGpsPathOverlay(
                        coordinates = uiState.gpsCoordinates,
                        isTracking = uiState.isTracking,
                        showGrid = true,
                        showTelemetryHud = true,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CampusMapView(
                        walkPath = uiState.walkPoints,
                        isTracking = uiState.isTracking,
                        selectedLandmarkId = selectedLandmark?.id,
                        onLandmarkClick = { lm ->
                            selectedLandmark = lm
                            activeTrackerTab = 2
                        },
                        onPointAdded = { pt ->
                            viewModel.addPoint(pt)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Live Simulation Status Banner if active
            if (uiState.isSimulatingDemo) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AccentPeachLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "⚡", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "15-Second Demo Walk Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DarkSlatePrimary
                                )
                                Text(
                                    text = "Streaming live GPS points into Bezier canvas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                        Text(
                            text = "${uiState.demoSecondsRemaining}s left",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentCoral
                        )
                    }
                }
            }

            // Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!uiState.isTracking) {
                    // Start Tracking Button
                    Button(
                        onClick = { handleStartClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = BlackPill),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("start_walk_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Text(
                                text = "Start GPS Walk",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }

                    // 15s Demo Simulation Quick Button
                    Button(
                        onClick = { viewModel.startDemoSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLavender),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("simulate_demo_walk_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "⚡", fontSize = 16.sp)
                            Text(
                                text = "15s Demo Walk",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Pause / Resume
                    Button(
                        onClick = {
                            if (uiState.isPaused) viewModel.resumeTracking() else viewModel.pauseTracking()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(52.dp)
                            .testTag("pause_walk_button")
                    ) {
                        Text(
                            text = if (uiState.isPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.labelLarge,
                            color = DarkSlatePrimary
                        )
                    }

                    // Finish Walk & Generate Art
                    Button(
                        onClick = {
                            viewModel.finishWalkAndGenerateArt { newId ->
                                onArtworkGenerated(newId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLavender),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(52.dp)
                            .testTag("finish_walk_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "✨", fontSize = 16.sp)
                            Text(
                                text = "Convert to Art",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Simulation / Campus Route Preset Dialog
    if (showSimulationDialog) {
        AlertDialog(
            onDismissRequest = { showSimulationDialog = false },
            title = {
                Text(
                    text = "Campus Route Presets",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkSlatePrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select a simulated route across campus to test creative shape generation:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    val presets = listOf(
                        "🌸 Hostel D to Tech Tower Bloom",
                        "🦋 Library Quad Butterfly Loop",
                        "🎗️ Sports Arena Infinity Wave",
                        "🌿 Campus Perimeter Botanical Stride"
                    )

                    presets.forEachIndexed { index, name ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceCardMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loadPresetRoute(index)
                                    showSimulationDialog = false
                                }
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkSlatePrimary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimulationDialog = false }) {
                    Text("Close", color = AccentMint)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

private fun formatTimer(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
