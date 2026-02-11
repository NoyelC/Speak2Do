package com.example.speak2do.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Tasks : BottomNavItem("tasks", "Tasks", Icons.Default.CheckCircle)
    object Stats : BottomNavItem("stats", "Stats", Icons.Default.BarChart)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}
