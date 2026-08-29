package com.example.ui.store

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CustomColorEntity
import com.example.data.model.StoreItemEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.RouteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StoreUiState(
    val storeItems: List<StoreItemEntity> = emptyList(),
    val customColors: List<CustomColorEntity> = emptyList(),
    val userProfile: UserProfileEntity? = null,
    val selectedCategory: String = "ALL",
    val mixedColorHex: String = "#80D6C6",
    val redVal: Int = 128,
    val greenVal: Int = 214,
    val blueVal: Int = 198,
    val customPigmentName: String = "",
    val purchaseSuccessMessage: String? = null
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _selectedCategory = MutableStateFlow("ALL")
    private val _mixedColor = MutableStateFlow("#80D6C6")
    private val _redVal = MutableStateFlow(128)
    private val _greenVal = MutableStateFlow(214)
    private val _blueVal = MutableStateFlow(198)
    private val _customName = MutableStateFlow("")
    private val _purchaseMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StoreUiState> = combine(
        combine(repository.allStoreItems, repository.allCustomColors, repository.userProfile) { items, customColors, profile ->
            Triple(items, customColors, profile)
        },
        combine(_selectedCategory, _mixedColor, _purchaseMessage) { cat, mixed, msg ->
            Triple(cat, mixed, msg)
        }
    ) { (items, customColors, profile), (cat, mixed, msg) ->
        val filtered = if (cat == "ALL") items else items.filter { it.category == cat }
        StoreUiState(
            storeItems = filtered,
            customColors = customColors,
            userProfile = profile,
            selectedCategory = cat,
            mixedColorHex = mixed,
            redVal = _redVal.value,
            greenVal = _greenVal.value,
            blueVal = _blueVal.value,
            customPigmentName = _customName.value,
            purchaseSuccessMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoreUiState()
    )

    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun updatePigmentName(name: String) {
        _customName.value = name
    }

    fun mixColor(red: Float, green: Float, blue: Float) {
        val r = (red * 255).toInt().coerceIn(0, 255)
        val g = (green * 255).toInt().coerceIn(0, 255)
        val b = (blue * 255).toInt().coerceIn(0, 255)
        _redVal.value = r
        _greenVal.value = g
        _blueVal.value = b
        _mixedColor.value = String.format("#%02X%02X%02X", r, g, b)
    }

    fun saveCurrentPigment(name: String = "Campus Custom Pigment", category: String = "Campus Mixed") {
        viewModelScope.launch {
            val finalName = if (name.isNotBlank()) name else "Custom Pigment #${(100..999).random()}"
            repository.saveCustomColor(
                name = finalName,
                hexCode = _mixedColor.value,
                category = category,
                r = _redVal.value,
                g = _greenVal.value,
                b = _blueVal.value
            )
            _purchaseMessage.value = "🧪 Saved pigment '$finalName' (+50 XP)!"
            _customName.value = ""
            kotlinx.coroutines.delay(2500)
            _purchaseMessage.value = null
        }
    }

    fun deleteCustomColor(color: CustomColorEntity) {
        viewModelScope.launch {
            repository.deleteCustomColor(color)
        }
    }

    fun buyItem(item: StoreItemEntity) {
        viewModelScope.launch {
            val success = repository.buyStoreItem(item)
            if (success) {
                _purchaseMessage.value = "Unlocked ${item.title}! (+75 XP)"
                kotlinx.coroutines.delay(2500)
                _purchaseMessage.value = null
            } else {
                _purchaseMessage.value = "Insufficient coins! Complete walks to earn more."
                kotlinx.coroutines.delay(2500)
                _purchaseMessage.value = null
            }
        }
    }
}

