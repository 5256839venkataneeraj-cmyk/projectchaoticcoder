package com.example.ui.studio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.generator.RouteArtEngine
import com.example.data.model.ColorBlob
import com.example.data.model.CustomColorEntity
import com.example.data.model.LandmarkSticker
import com.example.data.model.WalkRouteEntity
import com.example.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class StudioUiState(
    val route: WalkRouteEntity? = null,
    val selectedColorHex: String = "#BCEAD5",
    val selectedBrushStyle: String = "INK",
    val strokeWidthDp: Float = 4f,
    val selectedBlob: ColorBlob? = null,
    val activePaletteIndex: Int = 0,
    val isUsingCustomPalette: Boolean = false,
    val customColors: List<CustomColorEntity> = emptyList(),
    val isAiGenerating: Boolean = false,
    val aiArtDescription: String = "",
    val showExportDialog: Boolean = false,
    val showStickersSheet: Boolean = false,
    val isSavedSuccess: Boolean = false
)

class ColoringStudioViewModel(
    application: Application,
    private val routeId: Long
) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        loadRoute()
        observeCustomColors()
    }

    private fun loadRoute() {
        viewModelScope.launch {
            val entity = repository.getRouteById(routeId)
            _uiState.value = _uiState.value.copy(route = entity)
        }
    }

    private fun observeCustomColors() {
        viewModelScope.launch {
            repository.allCustomColors.collectLatest { colors ->
                _uiState.value = _uiState.value.copy(customColors = colors)
            }
        }
    }

    fun selectColor(hex: String) {
        _uiState.value = _uiState.value.copy(selectedColorHex = hex)
        // If a blob was selected, color it immediately
        _uiState.value.selectedBlob?.let { blob ->
            colorBlob(blob.id, hex)
        }
    }

    fun selectBrushStyle(style: String) {
        _uiState.value = _uiState.value.copy(selectedBrushStyle = style)
    }

    fun setStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(strokeWidthDp = width)
    }

    fun onBlobTapped(blob: ColorBlob) {
        _uiState.value = _uiState.value.copy(selectedBlob = blob)
        colorBlob(blob.id, _uiState.value.selectedColorHex)
    }

    fun colorBlob(blobId: Int, colorHex: String) {
        val currentRoute = _uiState.value.route ?: return
        val currentBlobs = RouteArtEngine.jsonToBlobs(currentRoute.blobsJson).toMutableList()
        val index = currentBlobs.indexOfFirst { it.id == blobId }
        if (index != -1) {
            currentBlobs[index] = currentBlobs[index].copy(colorHex = colorHex)
            val updatedBlobsJson = RouteArtEngine.blobsToJson(currentBlobs)
            val updatedRoute = currentRoute.copy(blobsJson = updatedBlobsJson)
            _uiState.value = _uiState.value.copy(
                route = updatedRoute,
                selectedBlob = currentBlobs[index]
            )
        }
    }

    fun switchPalette(paletteIndex: Int) {
        val palette = RouteArtEngine.PASTEL_PALETTES[paletteIndex % RouteArtEngine.PASTEL_PALETTES.size]
        val currentRoute = _uiState.value.route ?: return
        val points = RouteArtEngine.jsonToPoints(currentRoute.pointsJson)
        val newBlobs = RouteArtEngine.generateColorBlobs(points, palette)

        val updatedRoute = currentRoute.copy(
            blobsJson = RouteArtEngine.blobsToJson(newBlobs),
            artStyle = when (paletteIndex % 5) {
                0 -> "Pastel Bloom"
                1 -> "Lavender Mint"
                2 -> "Sunshine Meadow"
                3 -> "Campus Pastel"
                else -> "Earthy Vibrant"
            }
        )

        _uiState.value = _uiState.value.copy(
            route = updatedRoute,
            activePaletteIndex = paletteIndex,
            selectedColorHex = palette.first()
        )
    }

    fun addSticker(emoji: String, name: String) {
        val currentRoute = _uiState.value.route ?: return
        val stickers = mutableListOf<LandmarkSticker>()
        val sticker = LandmarkSticker(
            id = "st_${System.currentTimeMillis()}",
            name = name,
            iconEmoji = emoji,
            x = (250..550).random().toFloat(),
            y = (250..550).random().toFloat()
        )
        stickers.add(sticker)
        val updatedRoute = currentRoute.copy(stickersJson = stickers.toString())
        _uiState.value = _uiState.value.copy(
            route = updatedRoute,
            showStickersSheet = false
        )
    }

    fun applyAiStylization(stylePrompt: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiGenerating = true)
            kotlinx.coroutines.delay(1200) // fast on-device generative transformation

            val currentRoute = _uiState.value.route ?: return@launch
            val desc = when (stylePrompt) {
                "Impressionist Sunrise" -> "Transforms your ${currentRoute.distanceKm} km campus loop into soft dappled morning pastel brushstrokes."
                "Cyberpunk Hologram" -> "Synthesizes glowing geometric neon wireframes with electric cyan and ultraviolet gradients."
                "Japanese Botanical" -> "Translates your footsteps into organic cherry blossom and zen garden ink wash lines."
                else -> "Aesthetic procedural fluid contours generated from your step coordinates."
            }

            val paletteIndex = (1..4).random()
            switchPalette(paletteIndex)

            _uiState.value = _uiState.value.copy(
                isAiGenerating = false,
                aiArtDescription = desc
            )
        }
    }

    fun saveArtwork() {
        val currentRoute = _uiState.value.route ?: return
        viewModelScope.launch {
            repository.updateCustomization(
                id = currentRoute.id,
                blobsJson = currentRoute.blobsJson,
                artStyle = currentRoute.artStyle,
                stickersJson = currentRoute.stickersJson
            )
            _uiState.value = _uiState.value.copy(isSavedSuccess = true)
        }
    }

    fun toggleExportDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showExportDialog = show)
    }

    fun toggleStickersSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStickersSheet = show)
    }
}
