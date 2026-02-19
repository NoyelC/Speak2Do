package com.example.speak2do.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppThemeState {
    var isDarkMode by mutableStateOf(true)
        private set

    fun updateDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }
}
