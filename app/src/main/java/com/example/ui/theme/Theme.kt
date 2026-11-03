package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF06090F),
    primaryContainer = Color(0xFF131B2E),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = NeonMint,
    onSecondary = Color(0xFF06090F),
    secondaryContainer = Color(0xFF1A243B),
    onSecondaryContainer = Color(0xFFF8FAFC),
    tertiary = BkashPink,
    onTertiary = Color.White,
    background = Color(0xFF0A0E17),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131B2E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1A243B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = NeonPink,
    onError = Color.White
)

private val CyberLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = BkashPink,
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF263449),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = NeonPink,
    onError = Color.White
)

@Composable
fun TakaVaultTheme(
    isLightTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    isLightThemeState.value = isLightTheme
    MaterialTheme(
        colorScheme = if (isLightTheme) CyberLightColorScheme else CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}
