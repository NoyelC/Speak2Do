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
    primary = PrimaryCyan,
    secondary = LightCyan,
    background = DarkBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onBackground = WhiteText,
    onSurface = WhiteText
)


private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun Speak2DoTheme(
    darkTheme: Boolean = true,          // FORCE dark theme
    dynamicColor: Boolean = false,      // DISABLE dynamic color
    content: @Composable () -> Unit
) {
    val colorScheme = Speak2DoDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
