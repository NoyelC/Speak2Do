package com.example.speak2do.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Speak2DoDarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    secondary = LightCyan,
    tertiary = SuccessGreen,
    background = DarkBackground,
    surface = CardBackground,
    error = ErrorRed,
    onPrimary = Color.White,
    onBackground = WhiteText,
    onSurface = WhiteText,
    onError = Color.White
)

private val Speak2DoLightColorScheme = lightColorScheme(
    primary = PrimaryCyan,
    secondary = LightCyan,
    tertiary = SuccessGreen,
    background = DarkBackground,
    surface = CardBackground,
    error = ErrorRed,
    onPrimary = Color.White,
    onBackground = WhiteText,
    onSurface = WhiteText,
    onError = Color.White
)

@Composable
fun Speak2DoTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (AppThemeState.isDarkMode) {
        Speak2DoDarkColorScheme
    } else {
        Speak2DoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
