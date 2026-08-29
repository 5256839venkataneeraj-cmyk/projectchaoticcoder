package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.WalkRouteEntity
import com.example.ui.theme.*

@Composable
fun ArtworkThumbnailCard(
    route: WalkRouteEntity,
    onClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (route.isFavorite) AccentLavender else TextMuted,
        animationSpec = spring(),
        label = "heartColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("artwork_card_${route.id}")
    ) {
        // Thumbnail Art Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCardMuted)
                .padding(8.dp)
        ) {
            ArtCanvasView(
                pointsJson = route.pointsJson,
                blobsJson = route.blobsJson,
                artStyle = route.artStyle,
                modifier = Modifier.fillMaxSize()
            )

            // Top Action Buttons: Share (Left) & Favorite (Right)
            if (onShareClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { onShareClick() }
                        .testTag("share_btn_${route.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Artwork",
                        tint = DarkSlateSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Favorite Button Top-Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .clickable { onFavoriteToggle(!route.isFavorite) }
                    .testTag("fav_btn_${route.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (route.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = heartColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date and Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatShortDate(route.dateString),
                style = MaterialTheme.typography.labelMedium,
                color = DarkSlatePrimary
            )
            Text(
                text = "${route.distanceKm} km",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

private fun formatShortDate(dateStr: String): String {
    // converts "23 February 2024" to "23 Feb"
    val parts = dateStr.split(" ")
    return if (parts.size >= 2) {
        "${parts[0]} ${parts[1].take(3)}"
    } else {
        dateStr
    }
}
