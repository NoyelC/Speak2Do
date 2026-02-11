package com.example.speak2do.navigation

sealed class BottomNavItem(
    val route: String,
    val label: String
) {
    object Home : BottomNavItem("home", "Home")
    object Tasks : BottomNavItem("tasks", "Tasks")
    object Stats : BottomNavItem("stats", "Stats")
    object Profile : BottomNavItem("profile", "Profile")
}
