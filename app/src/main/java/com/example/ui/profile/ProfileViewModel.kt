package com.example.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BadgeEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WalkRouteEntity
import com.example.data.repository.RouteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfileEntity = UserProfileEntity(username = "Neeraj"),
    val recentRoutes: List<WalkRouteEntity> = emptyList(),
    val unlockedBadges: List<BadgeEntity> = emptyList(),
    val totalArtworksCount: Int = 0,
    val selectedTheme: String = "Classic Mint",
    val isLoggedOut: Boolean = false,
    val isLoading: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.userProfile,
                repository.allRoutes,
                repository.allBadges
            ) { profile, routes, badges ->
                ProfileUiState(
                    userProfile = profile ?: UserProfileEntity(username = "Neeraj"),
                    recentRoutes = routes.take(5),
                    unlockedBadges = badges.filter { it.isUnlocked },
                    totalArtworksCount = routes.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun updateProfile(name: String, studentId: String, hostel: String, department: String) {
        viewModelScope.launch {
            repository.updateProfileInfo(
                username = name,
                studentId = studentId,
                hostelBlock = hostel,
                department = department
            )
        }
    }

    fun logout() {
        // Reset or set logged out state
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}
