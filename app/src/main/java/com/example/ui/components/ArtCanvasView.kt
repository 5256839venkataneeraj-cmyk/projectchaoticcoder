package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.generator.RouteArtEngine
import com.example.data.model.ColorBlob
import com.example.data.model.LandmarkSticker
import com.example.data.model.PointF
import org.json.JSONArray
import kotlin.math.*

@Composable
fun ArtCanvasView(
    pointsJson: String,
    blobsJson: String,
    artStyle: String = "Pastel Bloom",
    stickersJson: String = "[]",
    modifier: Modifier = Modifier,
    isInteractive: Boolean = false,
    onBlobTapped: ((ColorBlob) -> Unit)? = null,
    onCanvasTapped: ((Offset) -> Unit)? = null,
    brushStyleKey: String = "INK"
) {
    val points = remember(pointsJson) { RouteArtEngine.jsonToPoints(pointsJson) }
    val blobs = remember(blobsJson) { RouteArtEngine.jsonToBlobs(blobsJson) }
    val stickers = remember(stickersJson) { parseStickers(stickersJson) }

    // Subtle gentle breathing animation for organic feel
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isInteractive) {
                    Modifier.pointerInput(blobs) {
                        detectTapGestures { tapOffset ->
                            onCanvasTapped?.invoke(tapOffset)
                            if (onBlobTapped != null) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val scaleX = canvasWidth / 800f
                                val scaleY = canvasHeight / 800f

                                // Check which blob was tapped
                                for (blob in blobs.reversed()) {
                                    val blobCenterX = blob.x * scaleX
                                    val blobCenterY = blob.y * scaleY
                                    val rx = blob.radiusX * scaleX
                                    val ry = blob.radiusY * scaleY
                                    val dist = hypot(
                                        (tapOffset.x - blobCenterX) / rx,
                                        (tapOffset.y - blobCenterY) / ry
                                    )
                                    if (dist <= 1.2f) {
                                        onBlobTapped(blob)
                                        return@detectTapGestures
                                    }
                                }
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        val width = size.width
        val height = size.height
        val scaleX = width / 800f
        val scaleY = height / 800f

        // 1. Draw Organic Pastel Blobs
        blobs.forEachIndexed { index, blob ->
            val animatedPulse = if (isInteractive) 1f else (1f + (index % 2 * 0.02f) * (pulseScale - 1f))
            drawOrganicBlob(
                blob = blob,
                scaleX = scaleX * animatedPulse,
                scaleY = scaleY * animatedPulse,
                brushStyle = brushStyleKey
            )
        }

        // 2. Draw Smoothed Walking Art Path
        if (points.size >= 2) {
            drawSmoothedWalkingPath(
                points = points,
                scaleX = scaleX,
                scaleY = scaleY,
                brushStyle = brushStyleKey,
                artStyle = artStyle
            )
        }

        // 3. Draw Sparkles & Start/End Milestones
        if (points.isNotEmpty()) {
            val startPt = points.first()
            val endPt = points.last()

            // Start Dot (Mint)
            drawCircle(
                color = Color(0xFF56B386),
                radius = 6.dp.toPx() * scaleX,
                center = Offset(startPt.x * scaleX, startPt.y * scaleY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx() * scaleX,
                center = Offset(startPt.x * scaleX, startPt.y * scaleY)
            )

            // End Dot (Lavender)
            drawCircle(
                color = Color(0xFF866FB3),
                radius = 5.dp.toPx() * scaleX,
                center = Offset(endPt.x * scaleX, endPt.y * scaleY)
            )

            // Tiny sparkles around art
            drawSparkle(Offset((points[points.size / 3].x + 30f) * scaleX, (points[points.size / 3].y - 40f) * scaleY), Color(0xFF76C893))
            if (points.size > 8) {
                drawSparkle(Offset((points[points.size * 2 / 3].x - 40f) * scaleX, (points[points.size * 2 / 3].y + 30f) * scaleY), Color(0xFF9F86C0))
            }
        }
    }
}

private fun DrawScope.drawOrganicBlob(
    blob: ColorBlob,
    scaleX: Float,
    scaleY: Float,
    brushStyle: String
) {
    val center = Offset(blob.x * scaleX, blob.y * scaleY)
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(blob.colorHex))
    } catch (_: Exception) {
        Color(0xFFBCEAD5)
    }

    val alpha = when (brushStyle) {
        "NEON" -> 0.45f
        "WATERCOLOR" -> 0.75f
        else -> 0.65f
    }

    val rx = blob.radiusX * scaleX
    val ry = blob.radiusY * scaleY

    rotate(degrees = blob.rotation, pivot = center) {
        // Draw multi-layered soft organic oval to give watercolor texture
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    parsedColor.copy(alpha = alpha),
                    parsedColor.copy(alpha = alpha * 0.7f),
                    parsedColor.copy(alpha = 0f)
                ),
                center = center,
                radius = max(rx, ry) * 1.15f
            ),
            topLeft = Offset(center.x - rx, center.y - ry),
            size = Size(rx * 2f, ry * 2f)
        )

        // Inner richer watercolor core
        drawOval(
            color = parsedColor.copy(alpha = alpha * 0.4f),
            topLeft = Offset(center.x - rx * 0.6f, center.y - ry * 0.6f),
            size = Size(rx * 1.2f, ry * 1.2f)
        )
    }
}

private fun DrawScope.drawSmoothedWalkingPath(
    points: List<PointF>,
    scaleX: Float,
    scaleY: Float,
    brushStyle: String,
    artStyle: String
) {
    val path = Path()
    val scaledPoints = points.map { Offset(it.x * scaleX, it.y * scaleY) }

    path.moveTo(scaledPoints[0].x, scaledPoints[0].y)

    for (i in 0 until scaledPoints.size - 1) {
        val p0 = if (i > 0) scaledPoints[i - 1] else scaledPoints[i]
        val p1 = scaledPoints[i]
        val p2 = scaledPoints[i + 1]
        val p3 = if (i + 2 < scaledPoints.size) scaledPoints[i + 2] else p2

        // Catmull-Rom to Cubic Bezier control points
        val cp1x = p1.x + (p2.x - p0.x) / 6f
        val cp1y = p1.y + (p2.y - p0.y) / 6f
        val cp2x = p2.x - (p3.x - p1.x) / 6f
        val cp2y = p2.y - (p3.y - p1.y) / 6f

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }

    val strokeWidth = when (brushStyle) {
        "CHALK" -> 4.5.dp.toPx()
        "NEON" -> 3.8.dp.toPx()
        "WATERCOLOR" -> 3.5.dp.toPx()
        else -> 3.2.dp.toPx()
    }

    when (brushStyle) {
        "NEON" -> {
            // Glowing outer beam
            drawPath(
                path = path,
                color = Color(0x6600F5D4),
                style = Stroke(
                    width = strokeWidth * 2.8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            // Vivid neon core
            drawPath(
                path = path,
                color = Color(0xFF00F5D4),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        "WATERCOLOR" -> {
            drawPath(
                path = path,
                color = Color(0xCC3D314A),
                style = Stroke(
                    width = strokeWidth * 1.3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        "CHALK" -> {
            drawPath(
                path = path,
                color = Color(0xFF435048),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 6f), 0f)
                )
            )
        }
        else -> {
            // Elegant Rich Ink Line (like in mockup design)
            drawPath(
                path = path,
                color = Color(0xFF1E2822),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, color: Color) {
    val size = 6f
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.4f, center.y - size * 0.4f)
        lineTo(center.x + size, center.y)
        lineTo(center.x + size * 0.4f, center.y + size * 0.4f)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size * 0.4f, center.y + size * 0.4f)
        lineTo(center.x - size, center.y)
        lineTo(center.x - size * 0.4f, center.y - size * 0.4f)
        close()
    }
    drawPath(path, color = color)
}

private fun parseStickers(json: String): List<LandmarkSticker> {
    if (json.isBlank()) return emptyList()
    val list = mutableListOf<LandmarkSticker>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                LandmarkSticker(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    iconEmoji = obj.getString("iconEmoji"),
                    x = obj.getDouble("x").toFloat(),
                    y = obj.getDouble("y").toFloat()
                )
            )
        }
    } catch (_: Exception) {}
    return list
}
