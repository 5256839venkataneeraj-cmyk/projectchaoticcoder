package com.example.ui.store

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
import com.example.data.model.StoreItemEntity
import com.example.ui.theme.*

@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var redSlider by remember { mutableFloatStateOf(0.5f) }
    var greenSlider by remember { mutableFloatStateOf(0.84f) }
    var blueSlider by remember { mutableFloatStateOf(0.78f) }

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
            // Header with Coin Wallet & Explorer Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Art Store & Color Lab",
                        style = MaterialTheme.typography.displayLarge,
                        color = DarkSlatePrimary
                    )
                    Text(
                        text = "Unlock palettes, mix custom pigments & level up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

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

            // Explorer Rank & Level Progress Card
            val userProfile = uiState.userProfile
            val currentLvl = userProfile?.currentLevel ?: 2
            val currentXp = userProfile?.currentXp ?: 680
            val rankTitle = userProfile?.explorerRank ?: "Path Pioneer"
            val nextLvlXp = when (currentLvl) {
                1 -> 300
                2 -> 800
                3 -> 1500
                4 -> 2500
                else -> 4000
            }
            val xpProgress = (currentXp.toFloat() / nextLvlXp.toFloat()).coerceIn(0f, 1f)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentLavenderLight
                            ) {
                                Text(
                                    text = "⭐ Lvl $currentLvl",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentLavender,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = rankTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkSlatePrimary
                            )
                        }

                        Text(
                            text = "$currentXp / $nextLvlXp XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AccentLavender,
                        trackColor = SurfaceCardMuted
                    )

                    Text(
                        text = "Walk to earn XP, level up ranks & unlock exclusive palettes & custom mixer slots!",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkSlateSecondary
                    )
                }
            }

            // Purchase Message Banner if any
            if (uiState.purchaseSuccessMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentMintLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.purchaseSuccessMessage ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = DarkSlatePrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All Items",
                    "OUTLINE" to "Brushes",
                    "PALETTE" to "Palettes",
                    "CUSTOM" to "My Pigments"
                ).forEach { (key, label) ->
                    val isSelected = uiState.selectedCategory == key
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) BlackPill else SurfaceCard,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.clickable { viewModel.selectCategory(key) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else DarkSlatePrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (uiState.selectedCategory != "CUSTOM") {
                // Store Items List
                uiState.storeItems.forEach { item ->
                    StoreItemCard(
                        item = item,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        onBuy = { viewModel.buyItem(item) }
                    )
                }
            }

            // Custom Color Pigment Lab
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🧪", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Custom Pigment & Color Laboratory",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkSlatePrimary
                            )
                            Text(
                                text = "Blend RGB shades, create named pigments & paint your walks",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    // Mixed Color Preview Swatch
                    val mixedColor = try {
                        Color(android.graphics.Color.parseColor(uiState.mixedColorHex))
                    } catch (_: Exception) {
                        AccentMint
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(mixedColor)
                                .border(2.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        )
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(
                                text = uiState.mixedColorHex,
                                style = MaterialTheme.typography.titleLarge,
                                color = DarkSlatePrimary
                            )
                            Text(
                                text = "RGB(${uiState.redVal}, ${uiState.greenVal}, ${uiState.blueVal})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    // Quick Tint Presets
                    Text(text = "Quick Blends:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Cyber Neon", 0f, 0.96f) to 0.83f,
                            Triple("Sunset Gold", 1.0f, 0.72f) to 0.01f,
                            Triple("Dorm Violet", 0.78f, 0.58f) to 0.95f,
                            Triple("Campus Jade", 0.32f, 0.72f) to 0.53f
                        ).forEach { (pair, bVal) ->
                            val (tintName, rVal, gVal) = pair
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceCardMuted,
                                modifier = Modifier.clickable {
                                    redSlider = rVal
                                    greenSlider = gVal
                                    blueSlider = bVal
                                    viewModel.mixColor(rVal, gVal, bVal)
                                }
                            ) {
                                Text(
                                    text = tintName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkSlatePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Red Channel: ${(redSlider * 255).toInt()}", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = redSlider,
                            onValueChange = {
                                redSlider = it
                                viewModel.mixColor(redSlider, greenSlider, blueSlider)
                            },
                            colors = SliderDefaults.colors(thumbColor = AccentCoral, activeTrackColor = AccentCoral)
                        )

                        Text(text = "Green Channel: ${(greenSlider * 255).toInt()}", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = greenSlider,
                            onValueChange = {
                                greenSlider = it
                                viewModel.mixColor(redSlider, greenSlider, blueSlider)
                            },
                            colors = SliderDefaults.colors(thumbColor = AccentMint, activeTrackColor = AccentMint)
                        )

                        Text(text = "Blue Channel: ${(blueSlider * 255).toInt()}", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = blueSlider,
                            onValueChange = {
                                blueSlider = it
                                viewModel.mixColor(redSlider, greenSlider, blueSlider)
                            },
                            colors = SliderDefaults.colors(thumbColor = AccentLavender, activeTrackColor = AccentLavender)
                        )
                    }

                    // Pigment Name Input & Save Button
                    var pigmentNameInput by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = pigmentNameInput,
                        onValueChange = { pigmentNameInput = it },
                        placeholder = { Text("Name this pigment (e.g., Block D Twilight)", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.saveCurrentPigment(
                                name = pigmentNameInput.ifBlank { "Custom Tint #${(100..999).random()}" }
                            )
                            pigmentNameInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Pigment to Palette (+50 XP) ✨", color = DarkSlatePrimary, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Saved Custom Pigments List
            if (uiState.customColors.isNotEmpty()) {
                Text(
                    text = "My Custom Pigments (${uiState.customColors.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkSlatePrimary
                )

                uiState.customColors.forEach { customColor ->
                    val colorObj = try {
                        Color(android.graphics.Color.parseColor(customColor.hexCode))
                    } catch (_: Exception) {
                        AccentMint
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colorObj)
                                        .border(2.dp, BorderSubtle, CircleShape)
                                )
                                Column {
                                    Text(
                                        text = customColor.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DarkSlatePrimary
                                    )
                                    Text(
                                        text = "${customColor.hexCode} • ${customColor.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.deleteCustomColor(customColor) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItemEntity,
    userCoins: Int,
    onBuy: () -> Unit
) {
    val previewColor = try {
        Color(android.graphics.Color.parseColor(item.previewHex))
    } catch (_: Exception) {
        AccentMint
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(previewColor.copy(alpha = 0.25f))
                        .border(1.dp, previewColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkSlatePrimary
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (item.isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentMintLight
                ) {
                    Text(
                        text = "Unlocked ✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkSlatePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = userCoins >= item.costCoins,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLavender),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${item.costCoins} 🪙",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
