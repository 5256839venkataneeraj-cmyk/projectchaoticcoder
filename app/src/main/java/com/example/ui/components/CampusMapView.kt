package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.PointF
import com.example.ui.theme.*

data class CampusLandmark(
    val id: String,
    val name: String,
    val emoji: String,
    val x: Float, // normalized 0..1000
    val y: Float,
    val description: String = "Campus Landmark"
)

val VIT_CAMPUS_LANDMARKS = listOf(
    CampusLandmark("lm1", "Academic Block 1", "🏫", 250f, 280f, "Engineering labs & Lecture Halls"),
    CampusLandmark("lm2", "Central Library", "📚", 600f, 250f, "4-Floor Study Center & Digital Archive"),
    CampusLandmark("lm3", "Food Court & Mess", "☕", 480f, 500f, "Student cafeteria & snacks plaza"),
    CampusLandmark("lm4", "Hostel Block D", "🏢", 200f, 720f, "Men's Dormitory Wing"),
    CampusLandmark("lm5", "Hostel Block A", "🏢", 720f, 700f, "Hostel Residency"),
    CampusLandmark("lm6", "Sports Arena", "🏃", 750f, 420f, "Track, Gym & Basketball Courts"),
    CampusLandmark("lm7", "Campus Lake Park", "🌿", 350f, 400f, "Botanical green walkway & gazebos")
)

@Composable
fun CampusMapView(
    walkPath: List<PointF>,
    isTracking: Boolean,
    modifier: Modifier = Modifier,
    selectedLandmarkId: String? = null,
    onLandmarkClick: ((CampusLandmark) -> Unit)? = null,
    onPointAdded: ((PointF) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val currentPoint = walkPath.lastOrNull() ?: PointF(200f, 720f) // default to hostel
    val selectedLandmark = VIT_CAMPUS_LANDMARKS.find { it.id == selectedLandmarkId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE9F1EC))
            .pointerInput(isTracking) {
                if (isTracking) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val normX = (change.position.x / size.width) * 1000f
                        val normY = (change.position.y / size.height) * 1000f
                        onPointAdded?.invoke(PointF(normX, normY))
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val scaleX = width / 1000f
            val scaleY = height / 1000f

            // 1. Draw Campus Roads and Green Spaces
            // Green park zone
            drawRoundRect(
                color = Color(0xFFD3E8DC),
                topLeft = Offset(280f * scaleX, 320f * scaleY),
                size = Size(200f * scaleX, 160f * scaleY),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f * scaleX)
            )

            // Campus lake
            drawOval(
                color = Color(0xFFCCE4F5),
                topLeft = Offset(320f * scaleX, 360f * scaleY),
                size = Size(100f * scaleX, 70f * scaleY)
            )

            // Roads (subtle white tracks)
            val roadPath = Path().apply {
                // Ring Road
                moveTo(150f * scaleX, 200f * scaleY)
                lineTo(850f * scaleX, 200f * scaleY)
                lineTo(850f * scaleX, 800f * scaleY)
                lineTo(150f * scaleX, 800f * scaleY)
                close()

                // Central Crossings
                moveTo(500f * scaleX, 200f * scaleY)
                lineTo(500f * scaleX, 800f * scaleY)
                moveTo(150f * scaleX, 500f * scaleY)
                lineTo(850f * scaleX, 500f * scaleY)
            }
            drawPath(
                path = roadPath,
                color = Color.White.copy(alpha = 0.9f),
                style = Stroke(width = 14f * scaleX, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Direct offline line of sight to selected landmark if active
            if (selectedLandmark != null) {
                val startX = currentPoint.x * scaleX
                val startY = currentPoint.y * scaleY
                val endX = selectedLandmark.x * scaleX
                val endY = selectedLandmark.y * scaleY

                drawLine(
                    color = AccentLavender,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            }

            // 2. Draw Landmark Zones & Buildings
            VIT_CAMPUS_LANDMARKS.forEach { lm ->
                val cx = lm.x * scaleX
                val cy = lm.y * scaleY
                val isSelected = lm.id == selectedLandmarkId

                if (isSelected) {
                    drawCircle(
                        color = AccentLavender.copy(alpha = 0.3f),
                        radius = 24f * scaleX,
                        center = Offset(cx, cy)
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 18f * scaleX else 14f * scaleX,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = if (isSelected) AccentLavender else Color(0xFF56B386),
                    radius = if (isSelected) 12f * scaleX else 10f * scaleX,
                    center = Offset(cx, cy)
                )
            }

            // 3. Draw Live Walk Path
            if (walkPath.size >= 2) {
                val path = Path()
                path.moveTo(walkPath[0].x * scaleX, walkPath[0].y * scaleY)
                for (i in 1 until walkPath.size) {
                    path.lineTo(walkPath[i].x * scaleX, walkPath[i].y * scaleY)
                }

                // Glowing background stroke
                drawPath(
                    path = path,
                    color = Color(0x6656B386),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Foreground path
                drawPath(
                    path = path,
                    color = Color(0xFF1E2A23),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Start Marker (Hostel / Origin)
                drawCircle(
                    color = Color(0xFF56B386),
                    radius = 8.dp.toPx(),
                    center = Offset(walkPath.first().x * scaleX, walkPath.first().y * scaleY)
                )

                // Current Live Position Marker (pulsing)
                val currentPt = walkPath.last()
                drawCircle(
                    color = AccentLavender.copy(alpha = pulseAlpha),
                    radius = 16.dp.toPx(),
                    center = Offset(currentPt.x * scaleX, currentPt.y * scaleY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(currentPt.x * scaleX, currentPt.y * scaleY)
                )
                drawCircle(
                    color = AccentLavender,
                    radius = 5.dp.toPx(),
                    center = Offset(currentPt.x * scaleX, currentPt.y * scaleY)
                )
            }
        }

        // Landmark Clickable Overlays
        VIT_CAMPUS_LANDMARKS.forEach { lm ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = ((lm.x / 1000f) * 310).dp,
                        top = ((lm.y / 1000f) * 330).dp
                    )
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (lm.id == selectedLandmarkId) AccentLavenderContainer else Color.White.copy(alpha = 0.85f),
                    border = if (lm.id == selectedLandmarkId) androidx.compose.foundation.BorderStroke(1.dp, AccentLavender) else null,
                    modifier = Modifier.clickable { onLandmarkClick?.invoke(lm) }
                ) {
                    Text(
                        text = "${lm.emoji} ${lm.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkSlatePrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Live Drawing Tip if tracking
        if (isTracking && walkPath.size < 5) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🚶 Walk naturally across campus or tap landmarks for offline guidance",
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkSlatePrimary
                )
            }
        }
    }
}

