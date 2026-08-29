package com.example.data.generator

import com.example.data.model.ColorBlob
import com.example.data.model.PointF
import com.example.data.model.WalkRouteEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.*

object RouteArtEngine {

    // Palette presets matching mockup visual design
    val PASTEL_PALETTES = listOf(
        listOf("#A7D7C5", "#C8B6E2", "#FFD3B6", "#A8D8EA", "#FFAAA6"), // Spring Bloom
        listOf("#BCEAD5", "#D6C6EE", "#FFE3AA", "#B2DFDB", "#F8BBD0"), // Lavender Mint
        listOf("#C5E1A5", "#E1BEE7", "#FFE082", "#80DEEA", "#FFCCBC"), // Sunshine Meadow
        listOf("#80CBC4", "#B39DDB", "#FFAB91", "#90CAF9", "#FFF59D"), // Campus Pastel
        listOf("#264653", "#2A9D8F", "#E9C46A", "#F4A261", "#E76F51")  // Earthy Vibrant
    )

    /**
     * Ramer-Douglas-Peucker (RDP) algorithm to simplify a polyline
     */
    fun simplifyPoints(points: List<PointF>, epsilon: Float = 4.0f): List<PointF> {
        if (points.size < 3) return points

        var maxDistance = 0f
        var index = 0

        for (i in 1 until points.size - 1) {
            val dist = perpendicularDistance(points[i], points.first(), points.last())
            if (dist > maxDistance) {
                maxDistance = dist
                index = i
            }
        }

        return if (maxDistance > epsilon) {
            val left = simplifyPoints(points.subList(0, index + 1), epsilon)
            val right = simplifyPoints(points.subList(index, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(points.first(), points.last())
        }
    }

    private fun perpendicularDistance(pt: PointF, lineStart: PointF, lineEnd: PointF): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y
        val mag = hypot(dx, dy)
        if (mag == 0f) return hypot(pt.x - lineStart.x, pt.y - lineStart.y)
        val u = ((pt.x - lineStart.x) * dx + (pt.y - lineStart.y) * dy) / (mag * mag)
        val clampedU = u.coerceIn(0f, 1f)
        val ix = lineStart.x + clampedU * dx
        val iy = lineStart.y + clampedU * dy
        return hypot(pt.x - ix, pt.y - iy)
    }

    /**
     * Normalizes a raw set of GPS/screen points into a [0, 1000] x [0, 1000] canvas bounding box,
     * maintaining aspect ratio and centering the centroid.
     */
    fun normalizePoints(rawPoints: List<PointF>, targetSize: Float = 800f, padding: Float = 100f): List<PointF> {
        if (rawPoints.isEmpty()) return emptyList()

        val minX = rawPoints.minOf { it.x }
        val maxX = rawPoints.maxOf { it.x }
        val minY = rawPoints.minOf { it.y }
        val maxY = rawPoints.maxOf { it.y }

        val width = (maxX - minX).coerceAtLeast(1f)
        val height = (maxY - minY).coerceAtLeast(1f)

        val maxDim = max(width, height)
        val scale = (targetSize - padding * 2) / maxDim

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f

        val targetCenterX = targetSize / 2f
        val targetCenterY = targetSize / 2f

        return rawPoints.map { pt ->
            val normX = targetCenterX + (pt.x - centerX) * scale
            val normY = targetCenterY + (pt.y - centerY) * scale
            PointF(normX, normY)
        }
    }

    /**
     * Synthesizes organic watercolor pastel blobs around route turning loops and clusters
     */
    fun generateColorBlobs(points: List<PointF>, palette: List<String> = PASTEL_PALETTES[0]): List<ColorBlob> {
        if (points.isEmpty()) return emptyList()

        val blobs = mutableListOf<ColorBlob>()
        val count = min(6, max(3, points.size / 4))
        val step = points.size / count

        for (i in 0 until count) {
            val ptIndex = (i * step + step / 2).coerceIn(0, points.lastIndex)
            val pt = points[ptIndex]
            
            // Generate aesthetic organic radius
            val radiusX = 90f + (i * 37 % 50)
            val radiusY = 75f + (i * 53 % 60)
            val rotation = (i * 45f) % 180f
            val colorHex = palette[i % palette.size]

            blobs.add(
                ColorBlob(
                    id = i,
                    x = pt.x + (if (i % 2 == 0) 25f else -25f),
                    y = pt.y + (if (i % 3 == 0) -20f else 30f),
                    radiusX = radiusX,
                    radiusY = radiusY,
                    rotation = rotation,
                    colorHex = colorHex,
                    label = "Zone ${i + 1}"
                )
            )
        }
        return blobs
    }

    /**
     * Converts points into JSON string
     */
    fun pointsToJson(points: List<PointF>): String {
        val array = JSONArray()
        points.forEach {
            val obj = JSONObject()
            obj.put("x", it.x.toDouble())
            obj.put("y", it.y.toDouble())
            array.put(obj)
        }
        return array.toString()
    }

    /**
     * Parses JSON string into PointF list
     */
    fun jsonToPoints(json: String): List<PointF> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<PointF>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(PointF(obj.getDouble("x").toFloat(), obj.getDouble("y").toFloat()))
            }
        } catch (_: Exception) {}
        return list
    }

    /**
     * Converts blobs into JSON string
     */
    fun blobsToJson(blobs: List<ColorBlob>): String {
        val array = JSONArray()
        blobs.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("x", it.x.toDouble())
            obj.put("y", it.y.toDouble())
            obj.put("rx", it.radiusX.toDouble())
            obj.put("ry", it.radiusY.toDouble())
            obj.put("rot", it.rotation.toDouble())
            obj.put("color", it.colorHex)
            obj.put("label", it.label)
            array.put(obj)
        }
        return array.toString()
    }

    /**
     * Parses JSON into ColorBlob list
     */
    fun jsonToBlobs(json: String): List<ColorBlob> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<ColorBlob>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ColorBlob(
                        id = obj.optInt("id", i),
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat(),
                        radiusX = obj.getDouble("rx").toFloat(),
                        radiusY = obj.getDouble("ry").toFloat(),
                        rotation = obj.getDouble("rot").toFloat(),
                        colorHex = obj.getString("color"),
                        label = obj.optString("label", "Zone ${i + 1}")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    /**
     * Automatically classifies the route path shape into a creative title & motif
     */
    fun classifyShape(points: List<PointF>, distanceKm: Double): Pair<String, String> {
        if (points.size < 5) return "Simple Stride" to "Minimal"

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        val ratio = (maxX - minX) / (maxY - minY).coerceAtLeast(1f)
        val loopCount = estimateLoops(points)

        return when {
            loopCount >= 3 -> "Campus Flora & Bloom" to "Floral"
            loopCount == 2 -> "Cosmic Butterfly" to "Fauna"
            ratio > 1.8f -> "Infinity Horizon" to "Ribbon"
            ratio < 0.6f -> "Botanical Stem" to "Floral"
            distanceKm > 5.0 -> "Grand Campus Odyssey" to "Abstract"
            else -> "Whimsical Melody" to "Abstract"
        }
    }

    private fun estimateLoops(points: List<PointF>): Int {
        var loops = 0
        for (i in 0 until points.size - 6 step 3) {
            val p1 = points[i]
            for (j in i + 5 until points.size step 2) {
                val p2 = points[j]
                if (hypot(p1.x - p2.x, p1.y - p2.y) < 60f) {
                    loops++
                    break
                }
            }
        }
        return loops
    }

    fun generateSvgPathString(points: List<PointF>): String {
        if (points.isEmpty()) return ""
        if (points.size == 1) return "M ${points[0].x} ${points[0].y}"

        val sb = StringBuilder()
        sb.append("M ${points[0].x.format(2)} ${points[0].y.format(2)}")

        for (i in 0 until points.size - 1) {
            val p0 = if (i > 0) points[i - 1] else points[i]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i + 2 < points.size) points[i + 2] else p2

            // Catmull-Rom spline converted to Cubic Bezier control points
            val cp1x = p1.x + (p2.x - p0.x) / 6f
            val cp1y = p1.y + (p2.y - p0.y) / 6f
            val cp2x = p2.x - (p3.x - p1.x) / 6f
            val cp2y = p2.y - (p3.y - p1.y) / 6f

            sb.append(" C ${cp1x.format(2)} ${cp1y.format(2)}, ${cp2x.format(2)} ${cp2y.format(2)}, ${p2.x.format(2)} ${p2.y.format(2)}")
        }
        return sb.toString()
    }

    /**
     * Generates complete standalone SVG document string scaled to 500x500
     */
    fun generateCompleteSvg(
        points: List<PointF>,
        blobs: List<ColorBlob> = emptyList(),
        currentStreak: Int = 1,
        title: String = "Campus Route-to-Art"
    ): String {
        // Normalize points strictly to 500x500 bounding box
        val normalized500 = normalizePoints(points, targetSize = 500f, padding = 45f)
        val pathData = generateSvgPathString(normalized500)

        // Dynamic stroke styling based on streak
        val (strokeColor, glowColor, strokeWidth, filterDef) = when {
            currentStreak >= 7 -> Quadruple("#FFD700", "#FF007F", 4.5f, "neonGlow")
            currentStreak >= 3 -> Quadruple("#00F5D4", "#7B2CBF", 4.0f, "cyberGlow")
            else -> Quadruple("#2A9D8F", "#E9C46A", 3.5f, "")
        }

        val blobSvg = StringBuilder()
        blobs.forEach { blob ->
            val scale = 500f / 800f
            val bx = blob.x * scale
            val by = blob.y * scale
            val brx = blob.radiusX * scale
            val bry = blob.radiusY * scale
            blobSvg.append(
                """
                <ellipse cx="${bx.format(1)}" cy="${by.format(1)}" rx="${brx.format(1)}" ry="${bry.format(1)}" 
                         fill="${blob.colorHex}" fill-opacity="0.45" transform="rotate(${blob.rotation} ${bx.format(1)} ${by.format(1)})" />
                """.trimIndent()
            )
        }

        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="500" height="500">
              <defs>
                <linearGradient id="neonGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="$strokeColor" />
                  <stop offset="100%" stop-color="$glowColor" />
                </linearGradient>
                <filter id="neonGlow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="4" result="blur" />
                  <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
                <filter id="cyberGlow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="3" result="blur" />
                  <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
              </defs>
              <rect width="100%" height="100%" fill="#0D1117" rx="24" />
              <g id="watercolor-blobs">
                $blobSvg
              </g>
              <g id="route-art">
                ${if (filterDef.isNotEmpty()) """<path d="$pathData" fill="none" stroke="$glowColor" stroke-width="${strokeWidth * 1.8f}" stroke-linecap="round" stroke-linejoin="round" opacity="0.6" filter="url(#$filterDef)" />""" else ""}
                <path d="$pathData" fill="none" stroke="url(#neonGradient)" stroke-width="$strokeWidth" stroke-linecap="round" stroke-linejoin="round" />
              </g>
            </svg>
        """.trimIndent()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun Float.format(decimals: Int): String = "%.${decimals}f".format(Locale.US, this)

    /**
     * Generates a sample organic flower/ribbon walk path for initial mockups
     */
    fun generateSampleWalk(seedType: Int): Pair<List<PointF>, List<ColorBlob>> {
        val points = mutableListOf<PointF>()
        val count = 28
        val center = 400f
        
        when (seedType % 6) {
            0 -> { // Fluid organic loop (like 23 Feb Today's artwork)
                for (i in 0..count) {
                    val t = (i.toFloat() / count) * 2 * PI.toFloat()
                    val r = 180f + 70f * sin(3 * t) + 40f * cos(2 * t)
                    val x = center + r * cos(t) + 30f * sin(5 * t)
                    val y = center + r * sin(t) + 20f * cos(4 * t)
                    points.add(PointF(x, y))
                }
            }
            1 -> { // Butterfly motif (22 Feb)
                for (i in 0..count) {
                    val t = (i.toFloat() / count) * 2 * PI.toFloat()
                    val r = 160f * (sin(t).pow(2) + cos(2 * t).absoluteValue)
                    val x = center + r * cos(t) * 1.2f
                    val y = center + r * sin(t)
                    points.add(PointF(x, y))
                }
            }
            2 -> { // Wavy serpentine Ribbon (21 Feb)
                for (i in 0..count) {
                    val progress = i.toFloat() / count
                    val x = 200f + progress * 400f + 40f * sin(progress * 4 * PI.toFloat())
                    val y = 250f + 300f * sin(progress * 2.5f * PI.toFloat()) + 40f * cos(progress * 5 * PI.toFloat())
                    points.add(PointF(x, y))
                }
            }
            3 -> { // Botanical Leaf (20 Feb)
                for (i in 0..count) {
                    val t = (i.toFloat() / count) * 2 * PI.toFloat()
                    val r = 200f * sin(t).absoluteValue
                    val x = center + r * cos(t) * 0.8f + 20f * sin(3 * t)
                    val y = center + r * sin(t) * 1.3f
                    points.add(PointF(x, y))
                }
            }
            4 -> { // Abstract Quad Loop (19 Feb)
                for (i in 0..count) {
                    val t = (i.toFloat() / count) * 2 * PI.toFloat()
                    val r = 150f + 80f * cos(4 * t)
                    val x = center + r * cos(t)
                    val y = center + r * sin(t)
                    points.add(PointF(x, y))
                }
            }
            else -> { // Campus Stride (18 Feb)
                for (i in 0..count) {
                    val t = (i.toFloat() / count) * 2 * PI.toFloat()
                    val r = 170f + 50f * sin(5 * t)
                    val x = center + r * cos(t)
                    val y = center + r * sin(t)
                    points.add(PointF(x, y))
                }
            }
        }

        val normalized = normalizePoints(points, 800f, 100f)
        val palette = PASTEL_PALETTES[seedType % PASTEL_PALETTES.size]
        val blobs = generateColorBlobs(normalized, palette)
        return normalized to blobs
    }
}
