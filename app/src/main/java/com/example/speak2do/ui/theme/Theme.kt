package com.example.speak2do.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Speak2DoDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C4DFF),        // Purple accent
    secondary = Color(0xFF4F46E5),      // Indigo
    background = Color(0xFF0B1220),     // Dark navy background
    surface = Color(0xFF151C2F),        // Card background
    onPrimary = Color.White,
    onBackground = Color(0xFFEDEFF5),
    onSurface = Color(0xFFEDEFF5)
)


private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Speak2DoTheme(
    darkTheme: Boolean = true,          // FORCE dark theme
    dynamicColor: Boolean = false,      // DISABLE dynamic color
    content: @Composable () -> Unit
) {
    val colorScheme = Speak2DoDarkColorScheme

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBackground,
            surface = DarkBackground
        ),
        typography = Typography,
        content = content
    )
}
