package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.generator.RouteArtEngine
import com.example.data.model.ColorBlob
import com.example.data.model.PointF
import com.example.data.model.WalkRouteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.hypot

object ArtworkShareHelper {

    private const val TAG = "ArtworkShareHelper"

    /**
     * Builds a rich descriptive caption for sharing to social media or messaging platforms.
     */
    fun createShareCaption(route: WalkRouteEntity, studentName: String = "Campus Artist"): String {
        return buildString {
            append("✨ Look at what I walked into existence on campus! 🎨\n\n")
            append("🖼️ \"${route.shapeName}\" (${route.shapeCategory})\n")
            append("📍 Campus: ${route.campusName}\n")
            append("👟 Steps: ${route.steps} | 🛣️ Distance: ${route.distanceKm} km | ⏱️ Duration: ${route.durationMinutes} min | 🔥 ${route.calories} kcal\n")
            append("🎨 Art Style: ${route.artStyle}\n\n")
            append("Transformed my daily walk into generative digital art with #PathCanvas #CampusArt #WalkToArt #CampusCreatives")
        }
    }

    /**
     * Renders a 1080x1920 (9:16) Story Card Bitmap representing the walking route artwork.
     */
    fun renderStoryCardBitmap(
        context: Context,
        route: WalkRouteEntity,
        studentName: String = "Campus Artist",
        brushStyleKey: String = "INK"
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    android.graphics.Color.rgb(250, 251, 249),
                    android.graphics.Color.rgb(238, 246, 241),
                    android.graphics.Color.rgb(229, 241, 235)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Border Frame
        val borderPaint = Paint().apply {
            color = android.graphics.Color.rgb(220, 230, 225)
            style = Paint.Style.STROKE
            strokeWidth = 12f
            isAntiAlias = true
        }
        val margin = 50f
        val frameRect = RectF(margin, margin, width - margin, height - margin)
        canvas.drawRoundRect(frameRect, 60f, 60f, borderPaint)

        // 2. Header: Logo & Campus Badge
        val logoBgPaint = Paint().apply {
            color = android.graphics.Color.rgb(188, 234, 213)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(130f, 140f, 44f, logoBgPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(26, 32, 44)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 50f
        }
        canvas.drawText("🎨", 108f, 158f, textPaint)

        val brandPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(26, 32, 44)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 46f
        }
        canvas.drawText("PathCanvas", 195f, 135f, brandPaint)

        val sloganPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(113, 128, 150)
            textSize = 28f
        }
        canvas.drawText("Every walk. Every art.", 195f, 172f, sloganPaint)

        // Campus Badge Top-Right
        val badgeBgPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val badgeRect = RectF(width - 450f, 100f, width - 90f, 175f)
        canvas.drawRoundRect(badgeRect, 30f, 30f, badgeBgPaint)

        val campusPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(45, 55, 72)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
        }
        canvas.drawText("📍 ${route.campusName}", width - 425f, 150f, campusPaint)

        // 3. Central Artwork Card Container
        val artCanvasRect = RectF(90f, 230f, width - 90f, 1250f)
        val artCanvasBgPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(artCanvasRect, 48f, 48f, artCanvasBgPaint)

        val artCanvasStrokePaint = Paint().apply {
            color = android.graphics.Color.rgb(230, 238, 235)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRoundRect(artCanvasRect, 48f, 48f, artCanvasStrokePaint)

        // Draw Artwork inside central container
        val artInnerWidth = artCanvasRect.width()
        val artInnerHeight = artCanvasRect.height()
        val offsetX = artCanvasRect.left
        val offsetY = artCanvasRect.top
        val scaleX = artInnerWidth / 800f
        val scaleY = artInnerHeight / 800f

        val blobs = RouteArtEngine.jsonToBlobs(route.blobsJson)
        val points = RouteArtEngine.jsonToPoints(route.pointsJson)

        // 3a. Draw Blobs
        for (blob in blobs) {
            val blobPaint = Paint().apply {
                val baseColor = try {
                    android.graphics.Color.parseColor(blob.colorHex)
                } catch (_: Exception) {
                    android.graphics.Color.rgb(188, 234, 213)
                }
                val alphaVal = when (brushStyleKey) {
                    "WATERCOLOR" -> 160
                    "NEON" -> 210
                    else -> 180
                }
                color = android.graphics.Color.argb(
                    alphaVal,
                    android.graphics.Color.red(baseColor),
                    android.graphics.Color.green(baseColor),
                    android.graphics.Color.blue(baseColor)
                )
                style = Paint.Style.FILL
                isAntiAlias = true
                maskFilter = BlurMaskFilter(20f * scaleX, BlurMaskFilter.Blur.NORMAL)
            }

            val cx = offsetX + blob.x * scaleX
            val cy = offsetY + blob.y * scaleY
            val rx = blob.radiusX * scaleX
            val ry = blob.radiusY * scaleY
            canvas.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), blobPaint)
        }

        // 3b. Draw Smoothed Walking Art Path
        if (points.size >= 2) {
            val path = Path()
            val first = points.first()
            path.moveTo(offsetX + first.x * scaleX, offsetY + first.y * scaleY)

            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2f
                val midY = (prev.y + curr.y) / 2f
                path.quadTo(
                    offsetX + prev.x * scaleX,
                    offsetY + prev.y * scaleY,
                    offsetX + midX * scaleX,
                    offsetY + midY * scaleY
                )
            }
            val last = points.last()
            path.lineTo(offsetX + last.x * scaleX, offsetY + last.y * scaleY)

            val strokeWidth = when (brushStyleKey) {
                "CALLIGRAPHY" -> 14f
                "NEON" -> 16f
                else -> 12f
            }

            // Path glow
            val glowPaint = Paint().apply {
                color = android.graphics.Color.argb(120, 188, 234, 213)
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth * 2.2f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
                maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawPath(path, glowPaint)

            // Core path stroke
            val pathPaint = Paint().apply {
                color = when (brushStyleKey) {
                    "NEON" -> android.graphics.Color.rgb(0, 230, 150)
                    "LAVENDER" -> android.graphics.Color.rgb(134, 111, 179)
                    else -> android.graphics.Color.rgb(38, 50, 56)
                }
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            canvas.drawPath(path, pathPaint)

            // Start Dot (Mint)
            val startDotPaint = Paint().apply {
                color = android.graphics.Color.rgb(86, 179, 134)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(offsetX + first.x * scaleX, offsetY + first.y * scaleY, 18f, startDotPaint)

            // End Dot (Lavender)
            val endDotPaint = Paint().apply {
                color = android.graphics.Color.rgb(159, 134, 192)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(offsetX + last.x * scaleX, offsetY + last.y * scaleY, 16f, endDotPaint)
        }

        // 4. Bottom Info Section
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(26, 32, 44)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 58f
        }
        canvas.drawText(route.shapeName, 100f, 1340f, titlePaint)

        val datePaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(113, 128, 150)
            textSize = 34f
        }
        canvas.drawText("${route.dateString} • ${route.shapeCategory}", 100f, 1390f, datePaint)

        // 5. Metrics Cards Grid (Steps, Distance, Calories, Duration)
        val metricBgPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardWidth = (width - 240f) / 2f

        // Card 1: Steps
        val card1Rect = RectF(100f, 1430f, 100f + cardWidth, 1560f)
        canvas.drawRoundRect(card1Rect, 28f, 28f, metricBgPaint)
        canvas.drawText("👟 ${route.steps} steps", 130f, 1510f, brandPaint.apply { textSize = 38f })

        // Card 2: Distance
        val card2Rect = RectF(140f + cardWidth, 1430f, width - 100f, 1560f)
        canvas.drawRoundRect(card2Rect, 28f, 28f, metricBgPaint)
        canvas.drawText("📍 ${route.distanceKm} km", 170f + cardWidth, 1510f, brandPaint.apply { textSize = 38f })

        // Card 3: Calories
        val card3Rect = RectF(100f, 1580f, 100f + cardWidth, 1710f)
        canvas.drawRoundRect(card3Rect, 28f, 28f, metricBgPaint)
        canvas.drawText("🔥 ${route.calories} kcal", 130f, 1660f, brandPaint.apply { textSize = 38f })

        // Card 4: Duration
        val card4Rect = RectF(140f + cardWidth, 1580f, width - 100f, 1710f)
        canvas.drawRoundRect(card4Rect, 28f, 28f, metricBgPaint)
        canvas.drawText("⏱️ ${route.durationMinutes} min", 170f + cardWidth, 1660f, brandPaint.apply { textSize = 38f })

        // 6. Footer Signature & Hashtag
        val footerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(100, 116, 139)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 32f
        }
        canvas.drawText("Art by $studentName", 100f, 1790f, footerPaint)

        val hashtagPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(134, 111, 179)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 32f
        }
        canvas.drawText("#PathCanvas #CampusArt", width - 520f, 1790f, hashtagPaint)

        return bitmap
    }

    /**
     * Saves the story bitmap to app cache directory and returns a content Uri via FileProvider.
     */
    suspend fun saveBitmapToCache(context: Context, bitmap: Bitmap, routeId: Long): Uri? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "shared_artworks")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val imageFile = File(cacheDir, "path_artwork_${routeId}_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save and provide artwork bitmap", e)
            null
        }
    }

    /**
     * Executes the Android Share Intent (ACTION_SEND / ACTION_CHOOSER) with the image URI and rich text caption.
     */
    suspend fun shareArtwork(
        context: Context,
        route: WalkRouteEntity,
        studentName: String = "Campus Artist",
        brushStyleKey: String = "INK",
        onComplete: (() -> Unit)? = null
    ) = withContext(Dispatchers.Main) {
        val caption = createShareCaption(route, studentName)

        try {
            // Render bitmap in background
            val bitmap = withContext(Dispatchers.Default) {
                renderStoryCardBitmap(context, route, studentName, brushStyleKey)
            }

            val imageUri = saveBitmapToCache(context, bitmap, route.id)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                if (imageUri != null) {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
                putExtra(Intent.EXTRA_TEXT, caption)
                putExtra(Intent.EXTRA_SUBJECT, "My Walk Artwork: ${route.shapeName}")
            }

            val chooser = Intent.createChooser(shareIntent, "Share Walk Artwork to...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            onComplete?.invoke()
        } catch (e: Exception) {
            Log.e(TAG, "Share failed, attempting text-only fallback", e)
            // Fallback to text plain
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, caption)
                    putExtra(Intent.EXTRA_SUBJECT, "My Walk Artwork: ${route.shapeName}")
                }
                val chooser = Intent.createChooser(fallbackIntent, "Share Walk Artwork to...").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                onComplete?.invoke()
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to launch share dialog: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
