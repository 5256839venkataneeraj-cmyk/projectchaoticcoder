package com.example.ui.challenges

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BadgeEntity
import com.example.data.model.ChallengeEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.RouteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChallengesUiState(
    val challenges: List<ChallengeEntity> = emptyList(),
    val badges: List<BadgeEntity> = emptyList(),
    val userProfile: UserProfileEntity? = null,
    val selectedTab: Int = 0 // 0: Challenges & Badges, 1: Campus Leaderboard
)

data class LeaderboardItem(
    val rank: Int,
    val name: String,
    val subtitle: String,
    val scoreText: String,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)

class ChallengesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _selectedTab = MutableStateFlow(0)

    val uiState: StateFlow<ChallengesUiState> = combine(
        repository.allChallenges,
        repository.allBadges,
        repository.userProfile,
        _selectedTab
    ) { challenges, badges, profile, tab ->
        ChallengesUiState(
            challenges = challenges,
            badges = badges,
            userProfile = profile,
            selectedTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChallengesUiState()
    )

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun claimChallengeReward(challenge: ChallengeEntity) {
        viewModelScope.launch {
            repository.claimChallengeReward(challenge)
        }
    }

    fun getCampusLeaderboard(): List<LeaderboardItem> {
        return listOf(
            LeaderboardItem(1, "Block D Dragons", "Hostel Wing • 412 Artists", "148,920 km", "🐉", true),
            LeaderboardItem(2, "Block A Titans", "Hostel Wing • 380 Artists", "142,310 km", "⚡"),
            LeaderboardItem(3, "Day Scholars Council", "Commuter Wing • 520 Artists", "139,800 km", "🎓"),
            LeaderboardItem(4, "Block B Phoenix", "Hostel Wing • 290 Artists", "118,450 km", "🔥"),
            LeaderboardItem(5, "Ladies Hostel Oasis", "Hostel Wing • 340 Artists", "114,200 km", "🌺")
        )
    }

    fun getTopStudentArtists(): List<LeaderboardItem> {
        return listOf(
            LeaderboardItem(1, "Diya Patel", "Design Dept • 32 Artworks", "214 km", "👩‍🎨"),
            LeaderboardItem(2, "Aarav Sharma", "CS Dept • 12 Artworks", "184 km", "🧑‍💻", true),
            LeaderboardItem(3, "Rohan Verma", "Mech Dept • 18 Artworks", "165 km", "🏃"),
            LeaderboardItem(4, "Sneha Reddy", "Biotech Dept • 15 Artworks", "152 km", "🌸"),
            LeaderboardItem(5, "Karthik Raja", "ECE Dept • 14 Artworks", "141 km", "🎨")
        )
    }
}
