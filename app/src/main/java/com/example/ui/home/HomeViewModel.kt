package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfileEntity
import com.example.data.model.WalkRouteEntity
import com.example.data.repository.RouteRepository
import com.example.data.sync.SyncState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val routes: List<WalkRouteEntity> = emptyList(),
    val favoriteRoutes: List<WalkRouteEntity> = emptyList(),
    val selectedFilter: HomeFilter = HomeFilter.ALL,
    val todaysRoute: WalkRouteEntity? = null,
    val userProfile: UserProfileEntity? = null,
    val syncState: SyncState = SyncState.Idle,
    val isLoading: Boolean = false
)

enum class HomeFilter {
    ALL,
    FAVORITES
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _selectedFilter = MutableStateFlow(HomeFilter.ALL)
    val selectedFilter: StateFlow<HomeFilter> = _selectedFilter.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allRoutes,
        repository.favoriteRoutes,
        _selectedFilter,
        repository.userProfile,
        repository.syncState
    ) { allRoutes, favRoutes, filter, profile, syncState ->
        HomeUiState(
            routes = allRoutes,
            favoriteRoutes = favRoutes,
            selectedFilter = filter,
            todaysRoute = allRoutes.firstOrNull(),
            userProfile = profile,
            syncState = syncState,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun setFilter(filter: HomeFilter) {
        _selectedFilter.value = filter
    }

    fun syncFirestore() {
        viewModelScope.launch {
            repository.syncWithFirestore()
        }
    }

    fun toggleFavorite(routeId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(routeId, isFavorite)
        }
    }
}
