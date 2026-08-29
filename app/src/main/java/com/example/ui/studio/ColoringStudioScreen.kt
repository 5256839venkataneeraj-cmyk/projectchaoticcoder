package com.example.ui.studio

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.generator.RouteArtEngine
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.ArtworkShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColoringStudioScreen(
    viewModel: ColoringStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val route = uiState.route

    val currentPalette = remember(uiState.activePaletteIndex) {
        RouteArtEngine.PASTEL_PALETTES[uiState.activePaletteIndex % RouteArtEngine.PASTEL_PALETTES.size]
    }

    Scaffold(
        containerColor = MintBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = route?.shapeName ?: "Coloring Studio",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )
                        Text(
                            text = "${route?.dateString ?: ""} • ${route?.distanceKm ?: 0.0} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("studio_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkSlatePrimary
                        )
                    }
                },
                actions = {
                    // Export Story Button
                    IconButton(
                        onClick = { viewModel.toggleExportDialog(true) },
                        modifier = Modifier.testTag("studio_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Story",
                            tint = AccentLavender
                        )
                    }

                    // Save Artwork Button
                    Button(
                        onClick = { viewModel.saveArtwork() },
                        colors = ButtonDefaults.buttonColors(containerColor = BlackPill),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("save_artwork_button")
                    ) {
                        Text(
                            text = if (uiState.isSavedSuccess) "Saved ✓" else "Save",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MintBackground
                )
            )
        }
    ) { innerPadding ->
        if (route == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentMint)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive Art Canvas Box
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .testTag("interactive_art_canvas")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        ArtCanvasView(
                            pointsJson = route.pointsJson,
                            blobsJson = route.blobsJson,
                            artStyle = route.artStyle,
                            stickersJson = route.stickersJson,
                            brushStyleKey = uiState.selectedBrushStyle,
                            isInteractive = true,
                            onBlobTapped = { blob ->
                                viewModel.onBlobTapped(blob)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating Interactive Tip Overlay
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "👆 Tap any pastel zone to color it with selected palette",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkSlateSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Color Palette Controls
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Color Palette",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )

                        // Palette theme switcher
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Pastel", "Lavender", "Sunshine", "Campus", "Earthy").forEachIndexed { index, name ->
                                val isSelected = uiState.activePaletteIndex == index
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AccentLavenderContainer else SurfaceCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.clickable { viewModel.switchPalette(index) }
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) DarkSlatePrimary else TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    PalettePickerRow(
                        colors = currentPalette,
                        selectedColorHex = uiState.selectedColorHex,
                        onColorSelected = { hex ->
                            viewModel.selectColor(hex)
                        }
                    )
                }

                // Outline Brush Styles
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Outline Brush Style",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkSlatePrimary
                    )

                    BrushStyleSelector(
                        selectedStyle = uiState.selectedBrushStyle,
                        onStyleSelected = { style ->
                            viewModel.selectBrushStyle(style)
                        }
                    )
                }

                // AI Generative Style Engine (PRD Module 2)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "✨", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "AI Route-to-Art Stylizer",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DarkSlatePrimary
                                )
                                Text(
                                    text = "Transform walk polyline into generative aesthetic styles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        if (uiState.isAiGenerating) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentLavender)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Synthesizing generative artwork...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AccentLavender
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val aiStyles = listOf(
                                    "Impressionist Sunrise",
                                    "Cyberpunk Hologram",
                                    "Japanese Botanical",
                                    "Voronoi Geometry"
                                )
                                aiStyles.forEach { style ->
                                    Button(
                                        onClick = { viewModel.applyAiStylization(style) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardMuted),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = style,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = DarkSlatePrimary
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.aiArtDescription.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentMintLight.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = uiState.aiArtDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkSlateSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                // Add Landmark Sticker action
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleStickersSheet(true) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🏷️", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Stamp Campus Landmarks",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DarkSlatePrimary
                                )
                                Text(
                                    text = "Tag Tech Tower, Library, Canteen or Hostels",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add Sticker",
                            tint = AccentMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Landmark Stickers Selector Dialog
    if (uiState.showStickersSheet && route != null) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleStickersSheet(false) },
            title = {
                Text("Select Campus Landmark Stamp", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val landmarks = listOf(
                        "🏫" to "Tech Tower",
                        "📚" to "Central Library",
                        "☕" to "Food Court",
                        "🏢" to "Hostel Block D",
                        "🏃" to "Sports Arena",
                        "🌿" to "Campus Lake Park"
                    )
                    landmarks.forEach { (emoji, name) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceCardMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.addSticker(emoji, name) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                                Text(text = name, style = MaterialTheme.typography.labelLarge, color = DarkSlatePrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleStickersSheet(false) }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp)
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var isSharingImage by remember { mutableStateOf(false) }

    // Story Export Dialog (9:16 vertical card generator)
    if (uiState.showExportDialog && route != null) {
        Dialog(onDismissRequest = { viewModel.toggleExportDialog(false) }) {
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
                            text = "Export & Share Artwork",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )
                        IconButton(onClick = { viewModel.toggleExportDialog(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    // 9:16 Social Story Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(360.dp)
                    ) {
                        StoryCardView(
                            route = route,
                            brushStyleKey = uiState.selectedBrushStyle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Primary Action: Share Story Image & Caption to Social / Messaging Apps
                    Button(
                        onClick = {
                            if (!isSharingImage) {
                                isSharingImage = true
                                coroutineScope.launch {
                                    ArtworkShareHelper.shareArtwork(
                                        context = context,
                                        route = route,
                                        studentName = "Campus Artist",
                                        brushStyleKey = uiState.selectedBrushStyle
                                    ) {
                                        isSharingImage = false
                                        viewModel.toggleExportDialog(false)
                                    }
                                }
                            }
                        },
                        enabled = !isSharingImage,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentLavender),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_share_image_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSharingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Text("Preparing High-Res Image...", color = Color.White)
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Text("Share Story Image & Stats", color = Color.White)
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
                                val caption = ArtworkShareHelper.createShareCaption(route, "Campus Artist")
                                val textIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, caption)
                                    putExtra(Intent.EXTRA_SUBJECT, "My Walk Artwork: ${route.shapeName}")
                                }
                                context.startActivity(Intent.createChooser(textIntent, "Share text summary"))
                                viewModel.toggleExportDialog(false)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Text("Share Text", color = DarkSlatePrimary, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val caption = ArtworkShareHelper.createShareCaption(route, "Campus Artist")
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
                }
            }
        }
    }
}
