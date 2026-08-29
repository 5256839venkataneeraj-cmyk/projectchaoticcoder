package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DarkSlatePrimary,
    onPrimary = Color.White,
    primaryContainer = AccentMintContainer,
    onPrimaryContainer = DarkSlatePrimary,
    secondary = AccentLavender,
    onSecondary = Color.White,
    secondaryContainer = AccentLavenderContainer,
    onSecondaryContainer = DarkSlatePrimary,
    tertiary = AccentPeach,
    background = MintBackground,
    onBackground = DarkSlatePrimary,
    surface = CreamSurface,
    onSurface = DarkSlatePrimary,
    surfaceVariant = SurfaceCardMuted,
    onSurfaceVariant = TextMuted,
    outline = BorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentMint,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E362A),
    onPrimaryContainer = AccentMintLight,
    secondary = AccentLavenderContainer,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2C243B),
    onSecondaryContainer = AccentLavenderLight,
    tertiary = AccentPeachLight,
    background = Color(0xFF121A15),
    onBackground = Color(0xFFE8F2EC),
    surface = Color(0xFF19241E),
    onSurface = Color(0xFFE8F2EC),
    surfaceVariant = Color(0xFF23312A),
    onSurfaceVariant = Color(0xFFA5B7AC),
    outline = Color(0xFF33453B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
