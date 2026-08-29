package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun StatsChip(
    icon: ImageVector,
    valueText: String,
    labelText: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceCardMuted,
    iconTint: Color = AccentMint,
    onClick: (() -> Unit)? = null
) {
    val chipModifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .background(backgroundColor)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(horizontal = 10.dp, vertical = 6.dp)

    Row(
        modifier = chipModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                color = DarkSlatePrimary
            )
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
