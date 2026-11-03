package com.example.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

// Reactive light/dark mode flag so all custom cards & surfaces adapt seamlessly
val isLightThemeState = mutableStateOf(false)

val ObsidianBg: Color
    get() = if (isLightThemeState.value) Color(0xFFF1F5F9) else Color(0xFF0A0E17)

val ObsidianDark: Color
    get() = if (isLightThemeState.value) Color(0xFFE2E8F0) else Color(0xFF06090F)

val SlateCardBg: Color
    get() = if (isLightThemeState.value) Color(0xFF1E293B) else Color(0xFF131B2E)

val SlateSurface: Color
    get() = if (isLightThemeState.value) Color(0xFF263449) else Color(0xFF1A243B)

val NeonCyan = Color(0xFF00F0FF)
val NeonMint = Color(0xFF00FFA3)
val NeonPink = Color(0xFFFF0055)
val BkashPink = Color(0xFFE2136E)
val BkashDeep = Color(0xFFA80A50)
val DbblBlue = Color(0xFF2563EB)
val DbblDeep = Color(0xFF1E3A8A)
val DbblRed = Color(0xFFEF4444)
val AmberGold = Color(0xFFFACC15)
val ElectricPurple = Color(0xFFA855F7)

val TextPrimary: Color
    get() = if (isLightThemeState.value) Color(0xFF0F172A) else Color(0xFFF8FAFC)

val TextSecondary: Color
    get() = if (isLightThemeState.value) Color(0xFFCBD5E1) else Color(0xFF94A3B8)

val TextMuted: Color
    get() = if (isLightThemeState.value) Color(0xFF94A3B8) else Color(0xFF64748B)
