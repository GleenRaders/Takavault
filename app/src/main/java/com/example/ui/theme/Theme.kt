package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianBg,
    primaryContainer = Color(0xFF0C1524),
    onPrimaryContainer = NeonCyan,
    secondary = NeonMint,
    onSecondary = ObsidianBg,
    secondaryContainer = Color(0xFF0A181C),
    onSecondaryContainer = NeonMint,
    tertiary = BkashPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF240B18),
    onTertiaryContainer = Color(0xFFFBCFE8),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SlateCardBg,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurface,
    onSurfaceVariant = TextSecondary,
    error = NeonPink,
    onError = Color.White
)

@Composable
fun TakaVaultTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}
