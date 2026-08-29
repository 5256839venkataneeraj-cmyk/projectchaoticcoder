package com.example.ui.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BadgeEntity
import com.example.data.model.ChallengeEntity
import com.example.ui.theme.*

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MintBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Campus Challenges",
                        style = MaterialTheme.typography.displayLarge,
                        color = DarkSlatePrimary
                    )
                    Text(
                        text = "Compete, earn badges & mint art rewards",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                // Coins Wallet Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AccentPeachLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🪙", fontSize = 16.sp)
                        Text(
                            text = "${uiState.userProfile?.totalCoins ?: 1250}",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )
                    }
                }
            }

            // Segmented Tabs: (1) Challenges & Badges, (2) Campus Leaderboards
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = SurfaceCard,
                contentColor = DarkSlatePrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Quests & Badges", style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = DarkSlatePrimary,
                    unselectedContentColor = TextMuted
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Campus Rankings", style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = DarkSlatePrimary,
                    unselectedContentColor = TextMuted
                )
            }

            if (uiState.selectedTab == 0) {
                // Active Quests Section
                Text(
                    text = "Active Quests",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkSlatePrimary
                )

                uiState.challenges.forEach { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onClaim = { viewModel.claimChallengeReward(challenge) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Badges Trophy Shelf
                Text(
                    text = "Badges & Milestones",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkSlatePrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val half = uiState.badges.chunked(2)
                    half.forEach { columnBadges ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            columnBadges.forEach { badge ->
                                BadgeCard(badge = badge)
                            }
                        }
                    }
                }
            } else {
                // Campus Leaderboard (Hostel Wings & Top Individual Artists)
                Text(
                    text = "Hostel & House Wing Rankings",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkSlatePrimary
                )

                viewModel.getCampusLeaderboard().forEach { item ->
                    LeaderboardRow(item = item)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Top Walking Artists of the Week",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkSlatePrimary
                )

                viewModel.getTopStudentArtists().forEach { item ->
                    LeaderboardRow(item = item)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: ChallengeEntity,
    onClaim: () -> Unit
) {
    val progress = (challenge.currentValue.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentMintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = challenge.iconEmoji, fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkSlatePrimary
                        )
                        Text(
                            text = challenge.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // Reward Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentPeachLight
                ) {
                    Text(
                        text = "+${challenge.rewardCoins} 🪙",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkSlatePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkSlateSecondary
            )

            // Progress Bar & Claim Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentMint,
                        trackColor = SurfaceCardMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${challenge.currentValue} / ${challenge.targetValue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (challenge.isCompleted) {
                    if (challenge.isClaimed) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceCardMuted
                        ) {
                            Text(
                                text = "Claimed ✓",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentLavender),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Claim 🪙", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: BadgeEntity) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (badge.isUnlocked) SurfaceCard else SurfaceCardMuted.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (badge.isUnlocked) BorderSubtle else Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) AccentLavenderLight else Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (badge.isUnlocked) badge.iconEmoji else "🔒", fontSize = 22.sp)
            }
            Text(
                text = badge.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (badge.isUnlocked) DarkSlatePrimary else TextMuted
            )
            Text(
                text = badge.description,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            if (badge.isUnlocked && badge.unlockedDate != null) {
                Text(
                    text = "Unlocked: ${badge.unlockedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentMint
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(item: LeaderboardItem) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (item.isCurrentUser) AccentMintLight.copy(alpha = 0.5f) else SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank number
                Text(
                    text = "#${item.rank}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.rank <= 3) AccentLavender else TextMuted
                )
                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceCardMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.avatarEmoji, fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = item.name + if (item.isCurrentUser) " (You)" else "",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkSlatePrimary
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = item.scoreText,
                style = MaterialTheme.typography.labelLarge,
                color = DarkSlatePrimary
            )
        }
    }
}
