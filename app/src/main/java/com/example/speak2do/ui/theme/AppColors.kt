package com.example.speak2do.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette
private val DarkBackgroundDark = Color(0xFF0D1B2A)
private val DarkBackgroundLight = Color(0xFFF4F7FB)
val DarkBackground: Color
    get() = if (AppThemeState.isDarkMode) DarkBackgroundDark else DarkBackgroundLight

private val CardBackgroundDark = Color(0xFF1B2838)
private val CardBackgroundLight = Color(0xFFFFFFFF)
val CardBackground: Color
    get() = if (AppThemeState.isDarkMode) CardBackgroundDark else CardBackgroundLight

val PrimaryCyan = Color(0xFF2979FF)
val LightCyan = Color(0xFF82B1FF)

// Text colors
private val WhiteTextDark = Color(0xFFFFFFFF)
private val WhiteTextLight = Color(0xFF0E1726)
val WhiteText: Color
    get() = if (AppThemeState.isDarkMode) WhiteTextDark else WhiteTextLight

private val GrayTextDark = Color(0xFFB0BEC5)
private val GrayTextLight = Color(0xFF5A6B7B)
val GrayText: Color
    get() = if (AppThemeState.isDarkMode) GrayTextDark else GrayTextLight

private val MutedTextDark = Color(0xFF78909C)
private val MutedTextLight = Color(0xFF6F8192)
val MutedText: Color
    get() = if (AppThemeState.isDarkMode) MutedTextDark else MutedTextLight

// Semantic colors
val SuccessGreen = Color(0xFF4CAF50)
val SuccessGreenDark = Color(0xFF2E7D32)
val WarningOrange = Color(0xFFFFA726)
val ErrorRed = Color(0xFFD32F2F)
val BadgeRed = Color(0xFFF44336)
val DisabledGray = Color(0xFF455A64)

// Card gradient colors
val CompletedGradientStart: Color
    get() = if (AppThemeState.isDarkMode) Color(0xFF1A2E1A) else Color(0xFFE8F5E9)
val PendingGradientStart: Color
    get() = if (AppThemeState.isDarkMode) Color(0xFF0F2A3E) else Color(0xFFEAF3FF)

// Shimmer
val ShimmerHighlight: Color
    get() = if (AppThemeState.isDarkMode) Color(0xFF2A3D52) else Color(0xFFDCE7F3)
