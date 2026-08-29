package com.example.ui.map

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.PointF
import com.example.ui.theme.*

private val CampusNavyHeader = Color(0xFF0F2537)
private val CampusAccentGreen = Color(0xFF38B07D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMapScreen(
    onNavigateToTracker: (String?) -> Unit,
    onNavigateToChallenges: () -> Unit,
    viewModel: CampusMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredLandmarks = remember(uiState.selectedCategory, uiState.searchQuery) {
        viewModel.getFilteredLandmarks()
    }

    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing effect for active location
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        containerColor = MintBackground,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OFFICE OF STUDENT'S WELFARE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = CampusNavyHeader.copy(alpha = 0.7f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "VIT Vellore Campus",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    ),
                                    color = CampusNavyHeader
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AccentMintLight,
                                    border = BorderStroke(1.dp, CampusAccentGreen.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "372 ACRES",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = DarkSlatePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Reset View / Center on Lake Button
                        IconButton(
                            onClick = {
                                zoomScale = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MintBackground)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = "Center Map",
                                tint = DarkSlatePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("campus_map_search"),
                        placeholder = {
                            Text(
                                text = "Search SJT, TT, VIT Lake, Hostels, Gates...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = TextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CampusAccentGreen,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = MintBackground,
                            unfocusedContainerColor = MintBackground
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Layer Switcher Chips (Standard / Route Art / Shuttle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapLayerMode.entries.forEach { mode ->
                            val isSelected = uiState.layerMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CampusNavyHeader else Color(0xFFF1F5F9),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable {
                                    viewModel.setLayerMode(mode)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = mode.icon, fontSize = 12.sp)
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) Color.White else DarkSlatePrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CampusCategory.entries) { category ->
                            val isSelected = uiState.selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(category) },
                                label = {
                                    Text(
                                        text = "${category.emoji} ${category.displayName}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentMintLight,
                                    selectedLabelColor = DarkSlatePrimary,
                                    containerColor = Color.Transparent,
                                    labelColor = TextMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFFCBD5E1),
                                    selectedBorderColor = CampusAccentGreen
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFEBF0EC))
        ) {
            // Interactive Vector Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.75f, 3.5f)
                            panOffsetX += pan.x
                            panOffsetY += pan.y
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("campus_vector_canvas")
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val baseScale = (canvasWidth / 1000f) * zoomScale
                    val centerX = (canvasWidth / 2f) + panOffsetX
                    val centerY = (canvasHeight / 2f) + panOffsetY

                    // Helper to transform normalized (0..1000) coords to canvas space
                    fun toCanvasOffset(normX: Float, normY: Float): Offset {
                        val localX = (normX - 500f) * baseScale + centerX
                        val localY = (normY - 500f) * baseScale + centerY
                        return Offset(localX, localY)
                    }

                    fun toCanvasSize(w: Float, h: Float): Size {
                        return Size(w * baseScale, h * baseScale)
                    }

                    // 1. Base Campus Ground
                    drawRect(
                        color = Color(0xFFF7F4EB), // Warm beige campus background
                        size = size
                    )

                    // 2. Green Lawn Zones & Fields
                    // SJT Ground Induction Venue (South East)
                    val sjtGroundCenter = toCanvasOffset(680f, 700f)
                    drawRoundRect(
                        color = Color(0xFFBBE5B3),
                        topLeft = Offset(sjtGroundCenter.x - 70f * baseScale, sjtGroundCenter.y - 60f * baseScale),
                        size = toCanvasSize(140f, 120f),
                        cornerRadius = CornerRadius(25f * baseScale)
                    )

                    // VIT Fields (East)
                    val vitFieldsCenter = toCanvasOffset(890f, 350f)
                    drawRoundRect(
                        color = Color(0xFF6BBF59),
                        topLeft = Offset(vitFieldsCenter.x - 65f * baseScale, vitFieldsCenter.y - 50f * baseScale),
                        size = toCanvasSize(130f, 100f),
                        cornerRadius = CornerRadius(16f * baseScale)
                    )

                    // Open Stadium Zone (Top Center)
                    val stadiumCenter = toCanvasOffset(510f, 145f)
                    drawRoundRect(
                        color = Color(0xFFE8D7B8),
                        topLeft = Offset(stadiumCenter.x - 55f * baseScale, stadiumCenter.y - 45f * baseScale),
                        size = toCanvasSize(110f, 90f),
                        cornerRadius = CornerRadius(18f * baseScale)
                    )

                    // Helipad
                    val helipadCenter = toCanvasOffset(510f, 80f)
                    drawRoundRect(
                        color = Color(0xFFD4A373),
                        topLeft = Offset(helipadCenter.x - 25f * baseScale, helipadCenter.y - 25f * baseScale),
                        size = toCanvasSize(50f, 50f),
                        cornerRadius = CornerRadius(10f * baseScale)
                    )

                    // 3. Central VIT Lake (Detailed contoured organic water polygon)
                    val lakePath = Path().apply {
                        val pt1 = toCanvasOffset(405f, 680f)
                        val pt2 = toCanvasOffset(450f, 640f)
                        val pt3 = toCanvasOffset(520f, 630f)
                        val pt4 = toCanvasOffset(600f, 660f)
                        val pt5 = toCanvasOffset(650f, 715f)
                        val pt6 = toCanvasOffset(610f, 770f)
                        val pt7 = toCanvasOffset(535f, 785f)
                        val pt8 = toCanvasOffset(465f, 765f)
                        val pt9 = toCanvasOffset(420f, 730f)

                        moveTo(pt1.x, pt1.y)
                        quadraticTo(pt2.x, pt2.y, pt3.x, pt3.y)
                        quadraticTo(pt4.x, pt4.y, pt5.x, pt5.y)
                        quadraticTo(pt6.x, pt6.y, pt7.x, pt7.y)
                        quadraticTo(pt8.x, pt8.y, pt9.x, pt9.y)
                        close()
                    }

                    // Lake shoreline border
                    drawPath(
                        path = lakePath,
                        color = Color(0xFF86BBD8),
                        style = Stroke(width = 6f * baseScale)
                    )
                    // Lake water fill with gradient
                    drawPath(
                        path = lakePath,
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF3A86C8), Color(0xFF22577A)),
                            center = toCanvasOffset(530f, 710f),
                            radius = 120f * baseScale
                        )
                    )

                    // Swimming Pool (Rectangular Blue)
                    val poolCenter = toCanvasOffset(505f, 265f)
                    drawRoundRect(
                        color = Color(0xFF48CAE4),
                        topLeft = Offset(poolCenter.x - 22f * baseScale, poolCenter.y - 18f * baseScale),
                        size = toCanvasSize(44f, 36f),
                        cornerRadius = CornerRadius(6f * baseScale)
                    )

                    // 4. Primary Road & Pathway Network
                    val roadPaintColor = Color.White.copy(alpha = 0.95f)
                    val roadStrokeWidth = 14f * baseScale

                    // Katpadi Main Road (South Axis)
                    val mainRoadPath = Path().apply {
                        val r1 = toCanvasOffset(30f, 620f)
                        val r2 = toCanvasOffset(380f, 870f)
                        val r3 = toCanvasOffset(710f, 925f)
                        val r4 = toCanvasOffset(980f, 930f)
                        moveTo(r1.x, r1.y)
                        lineTo(r2.x, r2.y)
                        lineTo(r3.x, r3.y)
                        lineTo(r4.x, r4.y)
                    }
                    drawPath(path = mainRoadPath, color = Color(0xFFCBD5E1), style = Stroke(width = 20f * baseScale, cap = StrokeCap.Round))
                    drawPath(path = mainRoadPath, color = Color(0xFF64748B), style = Stroke(width = 12f * baseScale, cap = StrokeCap.Round))

                    // Internal Ring & Central Avenues
                    val internalRoads = Path().apply {
                        // Central East-West Spine
                        val p1 = toCanvasOffset(110f, 600f) // Gate 1
                        val p2 = toCanvasOffset(380f, 540f)
                        val p3 = toCanvasOffset(520f, 520f)
                        val p4 = toCanvasOffset(760f, 480f)
                        val p5 = toCanvasOffset(960f, 440f) // Mach & East Gate
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        lineTo(p4.x, p4.y)
                        lineTo(p5.x, p5.y)

                        // North Hostel Road
                        val h1 = toCanvasOffset(270f, 210f)
                        val h2 = toCanvasOffset(500f, 320f)
                        val h3 = toCanvasOffset(720f, 210f)
                        val h4 = toCanvasOffset(840f, 290f)
                        moveTo(h1.x, h1.y)
                        lineTo(h2.x, h2.y)
                        lineTo(h3.x, h3.y)
                        lineTo(h4.x, h4.y)

                        // Cross Connectors
                        val c1 = toCanvasOffset(380f, 540f)
                        val c2 = toCanvasOffset(315f, 200f)
                        moveTo(c1.x, c1.y)
                        lineTo(c2.x, c2.y)

                        val d1 = toCanvasOffset(710f, 570f) // SJT
                        val d2 = toCanvasOffset(740f, 210f) // MH-N
                        moveTo(d1.x, d1.y)
                        lineTo(d2.x, d2.y)

                        val lk1 = toCanvasOffset(400f, 540f)
                        val lk2 = toCanvasOffset(405f, 680f)
                        val lk3 = toCanvasOffset(400f, 860f)
                        moveTo(lk1.x, lk1.y)
                        lineTo(lk2.x, lk2.y)
                        lineTo(lk3.x, lk3.y)
                    }
                    drawPath(path = internalRoads, color = roadPaintColor, style = Stroke(width = roadStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

                    // 5. Shuttle Transit Route Overlay
                    if (uiState.layerMode == MapLayerMode.SHUTTLE_TRANSIT) {
                        val shuttleRoutePath = Path().apply {
                            val s1 = toCanvasOffset(380f, 540f)
                            val s2 = toCanvasOffset(505f, 530f)
                            val s3 = toCanvasOffset(640f, 510f)
                            val s4 = toCanvasOffset(760f, 480f)
                            val s5 = toCanvasOffset(785f, 380f)
                            val s6 = toCanvasOffset(840f, 200f)
                            moveTo(s1.x, s1.y)
                            lineTo(s2.x, s2.y)
                            lineTo(s3.x, s3.y)
                            lineTo(s4.x, s4.y)
                            lineTo(s5.x, s5.y)
                            lineTo(s6.x, s6.y)
                        }
                        drawPath(
                            path = shuttleRoutePath,
                            color = Color(0xFF8338EC),
                            style = Stroke(width = 8f * baseScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // 6. Draw Preset Route Art Overlay
                    if (uiState.layerMode == MapLayerMode.ROUTE_ART || uiState.selectedArtRoute != null) {
                        val activeRoute = uiState.selectedArtRoute ?: VIT_CAMPUS_PRESET_ART_ROUTES.first()
                        if (activeRoute.points.size >= 2) {
                            val artPath = Path()
                            val firstPt = toCanvasOffset(activeRoute.points[0].x, activeRoute.points[0].y)
                            artPath.moveTo(firstPt.x, firstPt.y)
                            for (i in 1 until activeRoute.points.size) {
                                val nextPt = toCanvasOffset(activeRoute.points[i].x, activeRoute.points[i].y)
                                artPath.lineTo(nextPt.x, nextPt.y)
                            }

                            // Glowing background line
                            drawPath(
                                path = artPath,
                                color = Color(activeRoute.themeColorHex).copy(alpha = 0.4f),
                                style = Stroke(width = 16f * baseScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            // Solid art line
                            drawPath(
                                path = artPath,
                                color = Color(activeRoute.themeColorHex),
                                style = Stroke(width = 6f * baseScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // Point markers
                            activeRoute.points.forEach { pt ->
                                val p = toCanvasOffset(pt.x, pt.y)
                                drawCircle(color = Color.White, radius = 6f * baseScale, center = p)
                                drawCircle(color = Color(activeRoute.themeColorHex), radius = 4f * baseScale, center = p)
                            }
                        }
                    }

                    // 7. Draw Building Footprints (Blocks from map)
                    // Academic Blocks (Orange/Terracotta)
                    drawBuildingBlock(toCanvasOffset(240f, 710f), 55f * baseScale, 55f * baseScale, Color(0xFFF4A261), "TT", baseScale)
                    drawBuildingBlock(toCanvasOffset(710f, 570f), 65f * baseScale, 50f * baseScale, Color(0xFFF4A261), "SJT", baseScale)
                    drawBuildingBlock(toCanvasOffset(860f, 520f), 80f * baseScale, 70f * baseScale, Color(0xFFE76F51), "PRP", baseScale)
                    drawBuildingBlock(toCanvasOffset(160f, 650f), 50f * baseScale, 45f * baseScale, Color(0xFFF4A261), "MB", baseScale)
                    drawBuildingBlock(toCanvasOffset(955f, 455f), 45f * baseScale, 38f * baseScale, Color(0xFFF4A261), "MACH", baseScale)

                    // Men's Hostels (Teal / Green)
                    drawBuildingBlock(toCanvasOffset(315f, 200f), 70f * baseScale, 45f * baseScale, Color(0xFF56B386), "MH-B", baseScale)
                    drawBuildingBlock(toCanvasOffset(475f, 360f), 85f * baseScale, 45f * baseScale, Color(0xFF56B386), "MH-F", baseScale)
                    drawBuildingBlock(toCanvasOffset(740f, 195f), 75f * baseScale, 45f * baseScale, Color(0xFF56B386), "MH-N", baseScale)
                    drawBuildingBlock(toCanvasOffset(680f, 300f), 50f * baseScale, 40f * baseScale, Color(0xFF56B386), "MH-Q", baseScale)
                    drawBuildingBlock(toCanvasOffset(660f, 365f), 55f * baseScale, 35f * baseScale, Color(0xFF56B386), "MH-R", baseScale)
                    drawBuildingBlock(toCanvasOffset(830f, 280f), 45f * baseScale, 40f * baseScale, Color(0xFF56B386), "NHJ", baseScale)

                    // Parking Lots (Brown / Slate)
                    drawRoundRect(
                        color = Color(0xFF9E8279),
                        topLeft = Offset(toCanvasOffset(785f, 720f).x - 45f * baseScale, toCanvasOffset(785f, 720f).y - 20f * baseScale),
                        size = toCanvasSize(90f, 40f),
                        cornerRadius = CornerRadius(8f * baseScale)
                    )
                    drawRoundRect(
                        color = Color(0xFF836953),
                        topLeft = Offset(toCanvasOffset(785f, 765f).x - 45f * baseScale, toCanvasOffset(785f, 765f).y - 20f * baseScale),
                        size = toCanvasSize(90f, 40f),
                        cornerRadius = CornerRadius(8f * baseScale)
                    )

                    // 8. Landmark Interactive Pins & Badges
                    filteredLandmarks.forEach { lm ->
                        val pos = toCanvasOffset(lm.x, lm.y)
                        val isSelected = lm.id == uiState.selectedLandmark?.id

                        if (isSelected) {
                            drawCircle(
                                color = CampusAccentGreen.copy(alpha = 0.35f),
                                radius = 28f * baseScale,
                                center = pos
                            )
                        }

                        // Outer ring
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 16f * baseScale else 12f * baseScale,
                            center = pos
                        )
                        // Inner colored node
                        drawCircle(
                            color = Color(lm.colorHex),
                            radius = if (isSelected) 11f * baseScale else 8f * baseScale,
                            center = pos
                        )
                    }

                    // 9. Live User Location Pin ("You Are Here")
                    val userPos = toCanvasOffset(uiState.userPosition.x, uiState.userPosition.y)
                    drawCircle(
                        color = AccentLavender.copy(alpha = pulseAlpha),
                        radius = 26f * baseScale,
                        center = userPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 12f * baseScale,
                        center = userPos
                    )
                    drawCircle(
                        color = AccentLavender,
                        radius = 7f * baseScale,
                        center = userPos
                    )
                }
            }

            // Map Zoom Controls & Quick Center Tools (Right side floating pill)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In", tint = DarkSlatePrimary)
                    }
                }

                // Zoom Out
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.75f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out", tint = DarkSlatePrimary)
                    }
                }

                // My Location Center
                Surface(
                    shape = CircleShape,
                    color = AccentMintLight,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            zoomScale = 1.2f
                            panOffsetX = 0f
                            panOffsetY = 0f
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location", tint = CampusAccentGreen)
                    }
                }
            }

            // Bottom Route Art / Landmark Detail Card
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (uiState.selectedLandmark != null) {
                    val landmark = uiState.selectedLandmark!!
                    val stepsFromUser = viewModel.calculateStepsFromUser(landmark)
                    val distKm = viewModel.calculateDistanceKmFromUser(landmark)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(landmark.colorHex).copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = landmark.emoji, fontSize = 22.sp)
                                    }
                                    Column {
                                        Text(
                                            text = landmark.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = DarkSlatePrimary
                                        )
                                        Text(
                                            text = landmark.category.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = TextMuted
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.selectLandmark(null) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = landmark.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                color = DarkSlatePrimary.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Distance & Steps Metric Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MintBackground
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "👟", fontSize = 14.sp)
                                        Column {
                                            Text(
                                                text = "$stepsFromUser steps",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = DarkSlatePrimary
                                            )
                                            Text(
                                                text = "est. walk",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MintBackground
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "📍", fontSize = 14.sp)
                                        Column {
                                            Text(
                                                text = "$distKm km",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = DarkSlatePrimary
                                            )
                                            Text(
                                                text = "from you",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = AccentMintLight
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "🎨", fontSize = 14.sp)
                                        Column {
                                            Text(
                                                text = landmark.popularArtRoute,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = DarkSlatePrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "popular route",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Start GPS Walk Button
                            Button(
                                onClick = { onNavigateToTracker(landmark.popularArtRoute) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("start_walk_from_landmark"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CampusNavyHeader,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsWalk,
                                        contentDescription = "Walk",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Start Walk Art from ${landmark.shortCode}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                } else if (uiState.layerMode == MapLayerMode.ROUTE_ART) {
                    // Route Art Preset Selector Carousel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎨 Campus Walking Art Routes",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DarkSlatePrimary
                                )
                                Text(
                                    text = "Tap to view on map",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(VIT_CAMPUS_PRESET_ART_ROUTES) { artRoute ->
                                    val isSelected = uiState.selectedArtRoute?.id == artRoute.id
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) Color(artRoute.themeColorHex).copy(alpha = 0.15f) else MintBackground,
                                        border = if (isSelected) BorderStroke(1.5.dp, Color(artRoute.themeColorHex)) else null,
                                        modifier = Modifier
                                            .width(210.dp)
                                            .clickable { viewModel.selectArtRoute(artRoute) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(text = artRoute.emoji, fontSize = 16.sp)
                                                Text(
                                                    text = artRoute.name,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = DarkSlatePrimary,
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${artRoute.distanceKm} km • ~${artRoute.estimatedMinutes} mins",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { onNavigateToTracker(artRoute.name) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(32.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(artRoute.themeColorHex),
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = "Walk This Route",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Draw Building Helper on Canvas
private fun DrawScope.drawBuildingBlock(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    label: String,
    scale: Float
) {
    // Drop shadow
    drawRoundRect(
        color = Color(0x22000000),
        topLeft = Offset(center.x - width / 2 + 2f * scale, center.y - height / 2 + 2f * scale),
        size = Size(width, height),
        cornerRadius = CornerRadius(6f * scale)
    )

    // Building body
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - width / 2, center.y - height / 2),
        size = Size(width, height),
        cornerRadius = CornerRadius(6f * scale)
    )

    // White accent rooftop border
    drawRoundRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(center.x - width / 2 + 3f * scale, center.y - height / 2 + 3f * scale),
        size = Size(width - 6f * scale, height - 6f * scale),
        cornerRadius = CornerRadius(4f * scale),
        style = Stroke(width = 1.5f * scale)
    )
}
