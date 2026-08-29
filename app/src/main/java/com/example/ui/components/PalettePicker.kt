package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun PalettePickerRow(
    colors: List<String>,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { hex ->
            val color = try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (_: Exception) {
                Color(0xFF56B386)
            }
            val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) DarkSlatePrimary else Color.Transparent,
                animationSpec = spring(),
                label = "border"
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) borderColor else BorderSubtle, CircleShape)
                    .clickable { onColorSelected(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (isLightColor(color)) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BrushStyleSelector(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val styles = listOf(
        "INK" to "🖋️ Fine Ink",
        "NEON" to "⚡ Cyber Glow",
        "WATERCOLOR" to "🎨 Aquarelle",
        "CHALK" to "🖍️ Chalk"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        styles.forEach { (key, label) ->
            val isSelected = selectedStyle == key
            FilterChip(
                selected = isSelected,
                onClick = { onStyleSelected(key) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BlackPill,
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceCard,
                    labelColor = DarkSlatePrimary
                ),
                shape = RoundedCornerShape(20.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BorderSubtle,
                    selectedBorderColor = BlackPill
                )
            )
        }
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.6
}
