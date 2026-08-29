package com.example.util

import android.location.Location
import com.example.data.model.GpsCoordinate
import kotlin.math.*

/**
 * Real-time geodesic and metric distance calculator for GPS coordinate history.
 * Computes exact cumulative route length, direct displacement, pace, and segment metrics.
 */
object GpsDistanceCalculator {

    // Earth's mean radius in meters (WGS84 spherical approximation)
    private const val EARTH_RADIUS_METERS = 6371008.8

    /**
     * Calculates the great-circle distance between two GPS points using the Haversine formula.
     * Returns distance in meters.
     */
    fun computeDistanceBetween(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2.0).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2.0).pow(2.0)

        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Computes the total cumulative length of the recorded GPS coordinate path in meters.
     * Applies noise-filtering to ignore micro-jitters (< 1.0 meter).
     */
    fun calculateTotalDistanceMeters(
        coordinates: List<GpsCoordinate>,
        minDisplacementFilterMeters: Double = 1.0
    ): Double {
        if (coordinates.size < 2) return 0.0

        var totalMeters = 0.0
        for (i in 0 until coordinates.size - 1) {
            val p1 = coordinates[i]
            val p2 = coordinates[i + 1]
            val segmentDist = computeDistanceBetween(
                p1.latitude, p1.longitude,
                p2.latitude, p2.longitude
            )
            if (segmentDist >= minDisplacementFilterMeters) {
                totalMeters += segmentDist
            }
        }
        return totalMeters
    }

    /**
     * Computes the total path length in kilometers.
     */
    fun calculateTotalDistanceKm(coordinates: List<GpsCoordinate>): Double {
        return calculateTotalDistanceMeters(coordinates) / 1000.0
    }

    /**
     * Computes the direct start-to-end displacement (as the crow flies) in meters.
     */
    fun calculateDisplacementMeters(coordinates: List<GpsCoordinate>): Double {
        if (coordinates.size < 2) return 0.0
        val start = coordinates.first()
        val end = coordinates.last()
        return computeDistanceBetween(start.latitude, start.longitude, end.latitude, end.longitude)
    }

    /**
     * Calculates distance for each consecutive coordinate segment in meters.
     */
    fun calculateSegmentDistances(coordinates: List<GpsCoordinate>): List<Double> {
        if (coordinates.size < 2) return emptyList()
        val segments = mutableListOf<Double>()
        for (i in 0 until coordinates.size - 1) {
            val dist = computeDistanceBetween(
                coordinates[i].latitude, coordinates[i].longitude,
                coordinates[i + 1].latitude, coordinates[i + 1].longitude
            )
            segments.add(dist)
        }
        return segments
    }

    /**
     * Computes cumulative positive elevation gain in meters.
     */
    fun calculateElevationGainMeters(coordinates: List<GpsCoordinate>): Double {
        if (coordinates.size < 2) return 0.0
        var gain = 0.0
        for (i in 0 until coordinates.size - 1) {
            val altDiff = coordinates[i + 1].altitudeMeters - coordinates[i].altitudeMeters
            if (altDiff > 0.5) {
                gain += altDiff
            }
        }
        return gain
    }

    /**
     * Formats distance with auto-switching or explicit unit preference.
     * e.g. "450 m" or "1.35 km"
     */
    fun formatDistance(
        distanceMeters: Double,
        forceMeters: Boolean = false,
        decimals: Int = 2
    ): String {
        return if (forceMeters || distanceMeters < 1000.0) {
            "${distanceMeters.roundToInt()} m"
        } else {
            val km = distanceMeters / 1000.0
            String.format("%.${decimals}f km", km)
        }
    }

    /**
     * Calculates average pace formatted as "MM:SS /km".
     */
    fun calculatePaceFormatted(distanceMeters: Double, durationSeconds: Int): String {
        if (distanceMeters < 10.0 || durationSeconds <= 0) return "--:-- /km"
        val km = distanceMeters / 1000.0
        val secondsPerKm = (durationSeconds / km).roundToInt()
        val minutes = secondsPerKm / 60
        val seconds = secondsPerKm % 60
        return if (minutes > 99) "--:-- /km" else String.format("%02d:%02d /km", minutes, seconds)
    }

    /**
     * Computes path tortuosity (total distance / straight-line displacement).
     * 1.0 = straight line; higher values = meandering/artistic path.
     */
    fun calculateTortuosityRatio(coordinates: List<GpsCoordinate>): Double {
        val displacement = calculateDisplacementMeters(coordinates)
        if (displacement < 5.0) return 1.0
        val totalDist = calculateTotalDistanceMeters(coordinates)
        return totalDist / displacement
    }
}
