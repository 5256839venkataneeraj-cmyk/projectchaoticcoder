package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GpsCoordinate
import com.example.data.model.PointF
import com.example.ui.theme.*
import com.example.util.GpsDistanceCalculator
import kotlin.math.*

/**
 * A specialized Jetpack Compose Canvas overlay that renders real-time
 * latitude and longitude coordinates collected by LocationService as a continuous,
 * aesthetically styled path with dynamic Mercator projection, accuracy rings,
 * elevation/speed gradient accents, and coordinate telemetry.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ContinuousGpsPathOverlay(
    coordinates: List<GpsCoordinate>,
    modifier: Modifier = Modifier,
    isTracking: Boolean = true,
    showGrid: Boolean = true,
    showTelemetryHud: Boolean = true,
    strokeColor: Color = Color(0xFF1E2A23),
    glowColor: Color = Color(0xFF56B386),
    onCenterOnUser: (() -> Unit)? = null
) {
    var zoomScale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Pulsing animation for the current GPS position head
    val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val textMeasurer = rememberTextMeasurer()
    val lastCoord = coordinates.lastOrNull()

    // Real-time GPS distance calculation
    var preferMeters by remember { mutableStateOf(false) }
    val totalDistanceMeters = remember(coordinates) {
        GpsDistanceCalculator.calculateTotalDistanceMeters(coordinates)
    }
    val totalDistanceKm = totalDistanceMeters / 1000.0
    val displacementMeters = remember(coordinates) {
        GpsDistanceCalculator.calculateDisplacementMeters(coordinates)
    }
    val elevationGain = remember(coordinates) {
        GpsDistanceCalculator.calculateElevationGainMeters(coordinates)
    }
    val formattedDistance = GpsDistanceCalculator.formatDistance(totalDistanceMeters, preferMeters)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF7FBF8))
            .border(1.dp, Color(0xFFE2EDE6), RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5.0f)
                    panOffset += pan
                }
            }
            .testTag("continuous_gps_path_overlay")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw subtle background coordinate grid
            if (showGrid) {
                drawCoordinateGrid(width, height, panOffset, zoomScale, textMeasurer)
            }

            if (coordinates.isEmpty()) {
                // Draw awaiting GPS fix indicator
                drawAwaitingGps(width, height, pulseRadius, pulseAlpha)
                return@Canvas
            }

            // 2. Project GPS Lat/Lng coordinates into Canvas screen coordinates
            val projectedPoints = projectGpsCoordinatesToScreen(
                coordinates = coordinates,
                width = width,
                height = height,
                panOffset = panOffset,
                zoomScale = zoomScale
            )

            // 3. Render continuous path with dynamic incline thickness and vertical displacement
            if (projectedPoints.size >= 2) {
                // Render segment-by-segment with dynamic stroke thickness & elevation shading
                for (i in 1 until projectedPoints.size) {
                    val pPrev = projectedPoints[i - 1]
                    val pCurr = projectedPoints[i]
                    val cPrev = coordinates[i - 1]
                    val cCurr = coordinates[i]

                    val segMultiplier = ((cPrev.strokeThicknessMultiplier + cCurr.strokeThicknessMultiplier) / 2f).coerceIn(0.75f, 3.2f)
                    val baseWidth = 3.5.dp.toPx()
                    val dynamicWidth = baseWidth * segMultiplier

                    val segPath = Path().apply {
                        moveTo(pPrev.x, pPrev.y)
                        lineTo(pCurr.x, pCurr.y)
                    }

                    // Incline-aware coloring & glow: Ascents get warm elevation tint, flats get mint/slate
                    val grade = (cPrev.gradePercentage + cCurr.gradePercentage) / 2.0
                    val segGlowColor = when {
                        grade >= 6.0 -> Color(0xFFFF7043).copy(alpha = 0.45f) // Coral glow for steep climb
                        grade >= 2.0 -> Color(0xFFFFA726).copy(alpha = 0.4f)  // Warm amber for gentle incline
                        grade <= -3.0 -> Color(0xFF26C6DA).copy(alpha = 0.35f) // Cyan for descent
                        else -> glowColor.copy(alpha = 0.35f)
                    }

                    val segStrokeColor = when {
                        grade >= 6.0 -> Color(0xFFD84315) // Deep burnt orange for steep ascent
                        grade >= 2.0 -> Color(0xFF2E7D32) // Forest green for incline
                        else -> strokeColor
                    }

                    // 1. Soft elevation ambient glow aura (scales with incline thickness)
                    drawPath(
                        path = segPath,
                        color = segGlowColor,
                        style = Stroke(
                            width = (14.dp.toPx() * segMultiplier * zoomScale.coerceIn(0.8f, 1.8f)),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 2. Intermediate accent stroke
                    drawPath(
                        path = segPath,
                        color = if (grade >= 2.0) Color(0xFFFFB74D).copy(alpha = 0.7f) else Color(0xFF56B386).copy(alpha = 0.75f),
                        style = Stroke(
                            width = (7.dp.toPx() * segMultiplier * zoomScale.coerceIn(0.8f, 1.5f)),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 3. Crisp foreground path with dynamic incline thickness
                    drawPath(
                        path = segPath,
                        color = segStrokeColor,
                        style = Stroke(
                            width = dynamicWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw elevation contour ticks on prominent inclines
                    if (grade >= 4.0 && i % 2 == 0) {
                        val midX = (pPrev.x + pCurr.x) / 2f
                        val midY = (pPrev.y + pCurr.y) / 2f
                        drawCircle(
                            color = Color(0xFFFF7043),
                            radius = 3.5.dp.toPx(),
                            center = Offset(midX, midY)
                        )
                    }
                }

                // Render distance waypoint nodes along the route
                for (i in 0 until projectedPoints.size step max(1, projectedPoints.size / 6)) {
                    val pt = projectedPoints[i]
                    val coord = coordinates[i]
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(pt.x, pt.y)
                    )
                    drawCircle(
                        color = if (coord.gradePercentage >= 3.0) Color(0xFFFF7043) else strokeColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(pt.x, pt.y)
                    )
                }

                // Render Origin Start Pin (Green flag node)
                val startPt = projectedPoints.first()
                drawCircle(
                    color = Color(0xFF2E7D32),
                    radius = 9.dp.toPx(),
                    center = Offset(startPt.x, startPt.y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(startPt.x, startPt.y)
                )
            }

            // 4. Render Live GPS Head & Accuracy Ring
            val headPt = projectedPoints.last()
            val latestCoord = coordinates.last()

            // Accuracy radius in canvas pixels
            val accuracyPx = (latestCoord.accuracyMeters * 3.0f * zoomScale).coerceIn(16f, 80f)

            // Accuracy envelope
            drawCircle(
                color = AccentLavender.copy(alpha = 0.15f),
                radius = accuracyPx,
                center = Offset(headPt.x, headPt.y)
            )
            drawCircle(
                color = AccentLavender.copy(alpha = 0.4f),
                radius = accuracyPx,
                center = Offset(headPt.x, headPt.y),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
            )

            // Radar pulse ring
            drawCircle(
                color = AccentLavender.copy(alpha = pulseAlpha),
                radius = pulseRadius * 2.2f * zoomScale.coerceIn(0.8f, 1.4f),
                center = Offset(headPt.x, headPt.y)
            )

            // Main head marker
            drawCircle(
                color = Color.White,
                radius = 9.dp.toPx(),
                center = Offset(headPt.x, headPt.y)
            )
            drawCircle(
                color = AccentLavender,
                radius = 6.dp.toPx(),
                center = Offset(headPt.x, headPt.y)
            )
            drawCircle(
                color = Color(0xFF6C5CE7),
                radius = 3.dp.toPx(),
                center = Offset(headPt.x, headPt.y)
            )
        }

        // Top-left Telemetry HUD
        if (showTelemetryHud && lastCoord != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isTracking) AccentMint else TextMuted)
                        )
                        Text(
                            text = "GPS PATH OVERLAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = DarkSlatePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "LAT: ${formatDegree(lastCoord.latitude, true)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = DarkSlateSecondary
                    )
                    Text(
                        text = "LNG: ${formatDegree(lastCoord.longitude, false)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = DarkSlateSecondary
                    )

                    val altDisplay = if (lastCoord.altitudeMeters > 0) "${lastCoord.altitudeMeters.roundToInt()}m" else "-- m"
                    val gainDisplay = if (elevationGain > 0.5) " (▲+${elevationGain.roundToInt()}m)" else ""
                    Text(
                        text = "ALT: $altDisplay$gainDisplay",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (lastCoord.gradePercentage >= 3.0) Color(0xFFD84315) else DarkSlateSecondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Points: ${coordinates.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        if (lastCoord.accuracyMeters > 0) {
                            Text(
                                text = "±${lastCoord.accuracyMeters.toInt()}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentMint
                            )
                        }
                        if (lastCoord.gradePercentage.absoluteValue >= 1.0) {
                            val gradeSign = if (lastCoord.gradePercentage > 0) "+" else ""
                            Text(
                                text = "$gradeSign${String.format("%.1f", lastCoord.gradePercentage)}% slope",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (lastCoord.gradePercentage > 0) Color(0xFFFF7043) else Color(0xFF26C6DA)
                            )
                        }
                    }
                }
            }
        }

        // Top-right Real-Time Distance & Length Calculator Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .testTag("realtime_distance_calculator_hud")
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .widthIn(min = 120.dp, max = 150.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PATH LENGTH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = DarkSlateSecondary
                    )

                    // Unit Toggle Chip (KM / M)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (preferMeters) AccentPeachLight else AccentLavenderLight,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { preferMeters = !preferMeters }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = if (preferMeters) "m" else "km",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (preferMeters) AccentPeach else AccentLavender,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Real-time computed continuous path distance
                Text(
                    text = formattedDistance,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = DarkSlatePrimary
                )

                if (coordinates.size >= 2) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Direct: ${GpsDistanceCalculator.formatDistance(displacementMeters, preferMeters)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Bottom-Right Map Controls (Zoom / Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                IconButton(onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(5f) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = DarkSlatePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                IconButton(onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.5f) }) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = DarkSlatePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = AccentLavenderContainer,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                IconButton(onClick = {
                    zoomScale = 1f
                    panOffset = Offset.Zero
                    onCenterOnUser?.invoke()
                }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center on GPS",
                        tint = AccentLavender,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helper to project GPS Latitude/Longitude coordinates onto the 2D Canvas viewport
 * using dynamic bounding-box scaling and aspect-ratio preservation.
 */
private fun projectGpsCoordinatesToScreen(
    coordinates: List<GpsCoordinate>,
    width: Float,
    height: Float,
    panOffset: Offset,
    zoomScale: Float
): List<Offset> {
    if (coordinates.isEmpty()) return emptyList()

    var minLat = coordinates.minOf { it.latitude }
    var maxLat = coordinates.maxOf { it.latitude }
    var minLng = coordinates.minOf { it.longitude }
    var maxLng = coordinates.maxOf { it.longitude }

    // Ensure minimum span so single-point or short movements don't divide by zero
    val latSpan = max(0.0004, maxLat - minLat)
    val lngSpan = max(0.0004, maxLng - minLng)

    val midLat = (minLat + maxLat) / 2.0
    val midLng = (minLng + maxLng) / 2.0

    val padding = 60f
    val availableWidth = width - (padding * 2)
    val availableHeight = height - (padding * 2)

    val scaleX = (availableWidth / lngSpan).toFloat()
    val scaleY = (availableHeight / latSpan).toFloat()
    val scale = min(scaleX, scaleY) * zoomScale

    return coordinates.map { coord ->
        val x = (width / 2f) + ((coord.longitude - midLng) * scale).toFloat() + panOffset.x
        val y = (height / 2f) - ((coord.latitude - midLat) * scale).toFloat() + panOffset.y
        Offset(x, y)
    }
}

/**
 * Draws soft cartographic coordinate lines across the canvas.
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawCoordinateGrid(
    width: Float,
    height: Float,
    panOffset: Offset,
    zoomScale: Float,
    textMeasurer: TextMeasurer
) {
    val gridSpacing = 64.dp.toPx() * zoomScale.coerceIn(0.6f, 2.0f)
    val gridColor = Color(0xFFE5EEE8)

    var x = (panOffset.x % gridSpacing)
    while (x < width) {
        if (x >= 0) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
        }
        x += gridSpacing
    }

    var y = (panOffset.y % gridSpacing)
    while (y < height) {
        if (y >= 0) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        y += gridSpacing
    }
}

private fun DrawScope.drawAwaitingGps(
    width: Float,
    height: Float,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    val center = Offset(width / 2f, height / 2f)
    drawCircle(
        color = AccentLavender.copy(alpha = pulseAlpha),
        radius = pulseRadius * 2.5f,
        center = center
    )
    drawCircle(
        color = AccentLavender,
        radius = 8.dp.toPx(),
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = center
    )
}

private fun formatDegree(value: Double, isLatitude: Boolean): String {
    val direction = if (isLatitude) {
        if (value >= 0) "N" else "S"
    } else {
        if (value >= 0) "E" else "W"
    }
    val absVal = abs(value)
    return String.format("%.5f° %s", absVal, direction)
}
