package com.example.ui.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.generator.RouteArtEngine
import com.example.data.model.GpsCoordinate
import com.example.data.model.PointF
import com.example.data.model.WalkRouteEntity
import com.example.data.repository.RouteRepository
import com.example.service.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.util.GpsDistanceCalculator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.hypot

data class TrackerUiState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val isSimulatingDemo: Boolean = false,
    val demoSecondsRemaining: Int = 0,
    val walkPoints: List<PointF> = emptyList(),
    val gpsCoordinates: List<GpsCoordinate> = emptyList(),
    val isPathOverlayMode: Boolean = false,
    val stepCount: Int = 0,
    val distanceKm: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val displacementMeters: Double = 0.0,
    val formattedDistanceText: String = "0 m",
    val averagePaceText: String = "--:-- /km",
    val preferMetersUnit: Boolean = false,
    val durationSeconds: Int = 0,
    val speedKmh: Double = 0.0,
    val isSpeedExceeded: Boolean = false,
    val isUsingRealGps: Boolean = false,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val gpsAccuracyMeters: Float = 0f,
    val selectedPresetName: String = "Live Campus Walk",
    val newlyGeneratedRouteId: Long? = null,
    val showCompletionCelebration: Boolean = false,
    val detectedShapeName: String = "Campus Ribbon",
    val detectedCategory: String = "Ribbon"
)

class MapTrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var simulationJob: Job? = null
    private var locationCollectorJob: Job? = null

    init {
        // Collect real-time location updates from FusedLocationProviderClient via LocationService
        locationCollectorJob = viewModelScope.launch {
            LocationService.locationUpdates.collect { update ->
                if (_uiState.value.isTracking && !_uiState.value.isPaused && !_uiState.value.isSimulatingDemo) {
                    val currentPoints = _uiState.value.walkPoints.toMutableList()
                    currentPoints.add(update.point)

                    val currentGpsList = _uiState.value.gpsCoordinates.toMutableList()
                    val newGps = GpsCoordinate(
                        latitude = update.location.latitude,
                        longitude = update.location.longitude,
                        altitudeMeters = if (update.location.hasAltitude()) update.location.altitude else 0.0,
                        accuracyMeters = if (update.location.hasAccuracy()) update.location.accuracy else 0f,
                        speedKmh = update.currentSpeedKmh,
                        timestamp = update.timestamp
                    )
                    currentGpsList.add(newGps)

                    val calculatedDistanceMeters = GpsDistanceCalculator.calculateTotalDistanceMeters(currentGpsList)
                    val km = Math.round((calculatedDistanceMeters / 1000.0) * 100.0) / 100.0
                    val displacement = GpsDistanceCalculator.calculateDisplacementMeters(currentGpsList)
                    val formattedDist = GpsDistanceCalculator.formatDistance(calculatedDistanceMeters, _uiState.value.preferMetersUnit)
                    val currentDuration = if (update.sessionDurationSeconds > 0) update.sessionDurationSeconds else _uiState.value.durationSeconds
                    val pace = GpsDistanceCalculator.calculatePaceFormatted(calculatedDistanceMeters, currentDuration)
                    val (shape, category) = RouteArtEngine.classifyShape(currentPoints, km)

                    _uiState.value = _uiState.value.copy(
                        walkPoints = currentPoints,
                        gpsCoordinates = currentGpsList,
                        stepCount = update.estimatedSteps,
                        distanceKm = km,
                        distanceMeters = calculatedDistanceMeters,
                        displacementMeters = displacement,
                        formattedDistanceText = formattedDist,
                        averagePaceText = pace,
                        durationSeconds = currentDuration,
                        speedKmh = update.currentSpeedKmh,
                        isSpeedExceeded = update.currentSpeedKmh > 15.0,
                        isUsingRealGps = true,
                        currentLat = update.location.latitude,
                        currentLng = update.location.longitude,
                        gpsAccuracyMeters = if (update.location.hasAccuracy()) update.location.accuracy else 0f,
                        detectedShapeName = shape,
                        detectedCategory = category
                    )
                }
            }
        }

        // Collect continuous timer ticks and state from LocationService
        viewModelScope.launch {
            LocationService.serviceState.collect { sState ->
                if (_uiState.value.isTracking && !_uiState.value.isSimulatingDemo && _uiState.value.isUsingRealGps) {
                    val currentMeters = _uiState.value.distanceMeters
                    val pace = if (currentMeters > 0) GpsDistanceCalculator.calculatePaceFormatted(currentMeters, sState.durationSeconds) else _uiState.value.averagePaceText
                    _uiState.value = _uiState.value.copy(
                        durationSeconds = sState.durationSeconds,
                        isPaused = sState.isPaused,
                        averagePaceText = pace
                    )
                }
            }
        }
    }

    fun togglePathOverlayMode() {
        _uiState.value = _uiState.value.copy(isPathOverlayMode = !_uiState.value.isPathOverlayMode)
    }

    fun startDemoSimulation(onCompleted: (() -> Unit)? = null) {
        LocationService.stop(getApplication())
        timerJob?.cancel()
        simulationJob?.cancel()

        // Generate full path points to stream over 15 seconds
        val (samplePoints, _) = RouteArtEngine.generateSampleWalk((0..4).random())
        val totalStepsTarget = (4200..7800).random()
        val totalDistTarget = (totalStepsTarget * 0.72) / 1000.0

        // Base anchor location for simulated walk
        val baseLat = 12.97159
        val baseLng = 79.15892

        val initialGps = samplePoints.firstOrNull()?.let { pt ->
            listOf(
                GpsCoordinate(
                    latitude = baseLat + ((500f - pt.y) * 0.000008),
                    longitude = baseLng + ((pt.x - 500f) * 0.000008),
                    altitudeMeters = 184.0,
                    accuracyMeters = 4.2f,
                    speedKmh = 4.6
                )
            )
        } ?: emptyList()

        _uiState.value = TrackerUiState(
            isTracking = true,
            isSimulatingDemo = true,
            demoSecondsRemaining = 15,
            selectedPresetName = "Live 15s Campus Walk Simulation",
            walkPoints = if (samplePoints.isNotEmpty()) listOf(samplePoints.first()) else emptyList(),
            gpsCoordinates = initialGps,
            stepCount = 120,
            distanceKm = 0.1,
            durationSeconds = 1,
            speedKmh = 4.6,
            isUsingRealGps = false,
            currentLat = initialGps.firstOrNull()?.latitude,
            currentLng = initialGps.firstOrNull()?.longitude,
            gpsAccuracyMeters = 4.2f
        )

        simulationJob = viewModelScope.launch {
            for (sec in 1..15) {
                delay(1000)
                val remaining = 15 - sec
                val progress = sec / 15.0f
                val pointCountToTake = ((progress * samplePoints.size).toInt()).coerceIn(1, samplePoints.size)
                val currentPts = samplePoints.take(pointCountToTake)
                val currentGps = currentPts.mapIndexed { idx, pt ->
                    GpsCoordinate(
                        latitude = baseLat + ((500f - pt.y) * 0.000008),
                        longitude = baseLng + ((pt.x - 500f) * 0.000008),
                        altitudeMeters = 184.0 + (idx * 0.2),
                        accuracyMeters = 3.5f + (idx % 2),
                        speedKmh = 4.8,
                        timestamp = System.currentTimeMillis() - ((currentPts.size - idx) * 1000L)
                    )
                }
                val calculatedMeters = GpsDistanceCalculator.calculateTotalDistanceMeters(currentGps)
                val dist = Math.round((calculatedMeters / 1000.0) * 100.0) / 100.0
                val steps = (totalStepsTarget * progress).toInt().coerceAtLeast(100)
                val (shape, cat) = RouteArtEngine.classifyShape(currentPts, dist)
                val displacement = GpsDistanceCalculator.calculateDisplacementMeters(currentGps)
                val formattedDist = GpsDistanceCalculator.formatDistance(calculatedMeters, _uiState.value.preferMetersUnit)
                val pace = GpsDistanceCalculator.calculatePaceFormatted(calculatedMeters, sec * 45)

                _uiState.value = _uiState.value.copy(
                    demoSecondsRemaining = remaining,
                    walkPoints = currentPts,
                    gpsCoordinates = currentGps,
                    stepCount = steps,
                    distanceKm = dist,
                    distanceMeters = calculatedMeters,
                    displacementMeters = displacement,
                    formattedDistanceText = formattedDist,
                    averagePaceText = pace,
                    durationSeconds = sec * 45,
                    speedKmh = 4.8,
                    currentLat = currentGps.lastOrNull()?.latitude,
                    currentLng = currentGps.lastOrNull()?.longitude,
                    gpsAccuracyMeters = 3.5f,
                    detectedShapeName = shape,
                    detectedCategory = cat
                )
            }

            _uiState.value = _uiState.value.copy(
                isSimulatingDemo = false,
                demoSecondsRemaining = 0,
                showCompletionCelebration = true
            )
            onCompleted?.invoke()
        }
    }

    fun startTracking() {
        if (_uiState.value.isTracking) return
        _uiState.value = _uiState.value.copy(
            isTracking = true,
            isPaused = false,
            isSimulatingDemo = false,
            showCompletionCelebration = false,
            newlyGeneratedRouteId = null,
            isUsingRealGps = true,
            durationSeconds = 0
        )

        // Start FusedLocationProviderClient foreground service
        LocationService.start(getApplication())
    }

    fun pauseTracking() {
        _uiState.value = _uiState.value.copy(isPaused = true)
        LocationService.pause(getApplication())
    }

    fun resumeTracking() {
        _uiState.value = _uiState.value.copy(isPaused = false)
        LocationService.resume(getApplication())
    }

    fun addPoint(pt: PointF) {
        if (!_uiState.value.isTracking) {
            startTracking()
        }
        val currentList = _uiState.value.walkPoints.toMutableList()
        currentList.add(pt)

        val stepsInc = 15
        val totalSteps = _uiState.value.stepCount + stepsInc
        val distance = (totalSteps * 0.72) / 1000.0

        val (shape, category) = RouteArtEngine.classifyShape(currentList, distance)

        _uiState.value = _uiState.value.copy(
            walkPoints = currentList,
            stepCount = totalSteps,
            distanceKm = (Math.round(distance * 100.0) / 100.0),
            detectedShapeName = shape,
            detectedCategory = category
        )
    }

    fun loadPresetRoute(presetIndex: Int) {
        LocationService.stop(getApplication())
        val (pts, _) = RouteArtEngine.generateSampleWalk(presetIndex)
        val presetNames = listOf(
            "Hostel to Academic Block Flora",
            "Central Library Butterfly Stride",
            "Sports Arena Infinity Loop",
            "Campus Perimeter Botanical Trail"
        )
        val name = presetNames.getOrElse(presetIndex) { "Custom Campus Route" }

        val steps = (3500..8500).random()
        val distKm = (steps * 0.72) / 1000.0
        val distMeters = steps * 0.72
        val (shape, cat) = RouteArtEngine.classifyShape(pts, distKm)

        val baseLat = 12.97159
        val baseLng = 79.15892
        val presetGps = pts.mapIndexed { idx, pt ->
            GpsCoordinate(
                latitude = baseLat + ((500f - pt.y) * 0.000008),
                longitude = baseLng + ((pt.x - 500f) * 0.000008),
                altitudeMeters = 184.0,
                accuracyMeters = 3.0f,
                speedKmh = 4.5
            )
        }
        val calculatedMeters = GpsDistanceCalculator.calculateTotalDistanceMeters(presetGps).coerceAtLeast(distMeters)
        val calculatedKm = Math.round((calculatedMeters / 1000.0) * 100.0) / 100.0
        val displacement = GpsDistanceCalculator.calculateDisplacementMeters(presetGps)
        val formattedDist = GpsDistanceCalculator.formatDistance(calculatedMeters, _uiState.value.preferMetersUnit)

        _uiState.value = _uiState.value.copy(
            walkPoints = pts,
            gpsCoordinates = presetGps,
            stepCount = steps,
            distanceKm = calculatedKm,
            distanceMeters = calculatedMeters,
            displacementMeters = displacement,
            formattedDistanceText = formattedDist,
            selectedPresetName = name,
            durationSeconds = (steps / 110) * 60,
            detectedShapeName = shape,
            detectedCategory = cat,
            isUsingRealGps = false
        )
    }

    fun toggleDistanceUnit() {
        val newPreferMeters = !_uiState.value.preferMetersUnit
        val formatted = GpsDistanceCalculator.formatDistance(_uiState.value.distanceMeters, newPreferMeters)
        _uiState.value = _uiState.value.copy(
            preferMetersUnit = newPreferMeters,
            formattedDistanceText = formatted
        )
    }

    fun finishWalkAndGenerateArt(onFinished: (Long) -> Unit) {
        val serviceDurationSec = LocationService.serviceState.value.durationSeconds
        LocationService.stop(getApplication())
        timerJob?.cancel()
        viewModelScope.launch {
            val state = _uiState.value
            val rawPoints = if (state.walkPoints.size >= 4) state.walkPoints else RouteArtEngine.generateSampleWalk(0).first

            // Normalize points to standard canvas
            val normalized = RouteArtEngine.normalizePoints(rawPoints)
            val simplified = RouteArtEngine.simplifyPoints(normalized, 3.5f)
            val (shapeName, shapeCat) = RouteArtEngine.classifyShape(simplified, state.distanceKm.coerceAtLeast(1.2))
            val blobs = RouteArtEngine.generateColorBlobs(simplified)

            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val now = Date()

            val steps = state.stepCount.coerceAtLeast(2400)
            val distance = state.distanceKm.coerceAtLeast(1.8)
            val calories = (distance * 55).toInt().coerceAtLeast(15)
            val actualSeconds = if (serviceDurationSec > 0) serviceDurationSec else state.durationSeconds
            val durationMinutes = if (actualSeconds > 0) {
                ((actualSeconds + 30) / 60).coerceAtLeast(1)
            } else {
                1
            }

            val newEntity = WalkRouteEntity(
                dateString = dateFormat.format(now),
                isoDate = isoFormat.format(now),
                steps = steps,
                distanceKm = distance,
                durationMinutes = durationMinutes,
                calories = calories,
                title = state.selectedPresetName,
                shapeName = shapeName,
                shapeCategory = shapeCat,
                pointsJson = RouteArtEngine.pointsToJson(simplified),
                blobsJson = RouteArtEngine.blobsToJson(blobs),
                strokesJson = "[]",
                stickersJson = "[]",
                isFavorite = false,
                campusName = "VIT Main Campus",
                artStyle = "Pastel Bloom",
                createdAt = System.currentTimeMillis()
            )

            val newId = repository.insertRoute(newEntity)
            _uiState.value = _uiState.value.copy(
                isTracking = false,
                showCompletionCelebration = true,
                newlyGeneratedRouteId = newId
            )
            onFinished(newId)
        }
    }

    fun resetState() {
        LocationService.stop(getApplication())
        _uiState.value = TrackerUiState()
    }

    override fun onCleared() {
        LocationService.stop(getApplication())
        super.onCleared()
    }
}

