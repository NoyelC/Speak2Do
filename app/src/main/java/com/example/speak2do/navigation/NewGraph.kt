package com.example.speak2do.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.speak2do.VoiceRecordViewModel
import com.example.speak2do.components.MainScreen
import com.example.speak2do.components.TasksScreen
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan

@Composable
fun AppNavGraph(onMicClick: () -> Unit) {
    val navController = rememberNavController()
    val viewModel: VoiceRecordViewModel = viewModel()

    val voiceRecordEntities by viewModel.voiceRecords.collectAsState()
    val spokenText by viewModel.spokenText.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingTime by viewModel.recordingTime.collectAsState()

    // Show shimmer until first real data arrives from Room
    var hasLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(voiceRecordEntities) {
        if (!hasLoaded) {
            // Small delay to let Room emit real data; once we get any emission, mark loaded
            kotlinx.coroutines.delay(600)
            hasLoaded = true
        }
    }
    val isLoading = !hasLoaded

    val recordings = voiceRecordEntities.map { entity ->
        RecordingItem(
            id = entity.id,
            text = entity.text,
            dateTime = entity.dateTime,
            duration = "VOICE",
            progress = entity.progress,
            isCompleted = entity.isCompleted
        )
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            if (currentRoute == BottomNavItem.Home.route) {
                FloatingActionButton(
                    onClick = onMicClick,
                    containerColor = PrimaryCyan,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Record",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 4 }
            },
            popEnterTransition = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }
            },
            popExitTransition = {
                fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 4 }
            }
        ) {
            composable(BottomNavItem.Home.route) {
                MainScreen(
                    spokenText = spokenText,
                    isRecording = isRecording,
                    recordingTime = recordingTime,
                    recordings = recordings,
                    isLoading = isLoading,
                    onMicClick = onMicClick,
                    onToggleCompleted = { id, completed ->
                        viewModel.toggleCompleted(id, completed)
                    }
                )
            }
            composable(BottomNavItem.Tasks.route) {
                TasksScreen(
                    recordings = recordings,
                    isLoading = isLoading,
                    onToggleCompleted = { id, completed ->
                        viewModel.toggleCompleted(id, completed)
                    }
                )
            }
            composable(BottomNavItem.Stats.route) {
                com.example.speak2do.components.StatsScreen(recordings = recordings)
            }
            composable(BottomNavItem.Profile.route) {
                com.example.speak2do.components.ProfileScreen(recordings = recordings)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Tasks,
        BottomNavItem.Stats,
        BottomNavItem.Profile
    )

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = CardBackground
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) PrimaryCyan else MutedText
                    )
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (selected) PrimaryCyan else MutedText
                    )
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryCyan.copy(alpha = 0.15f)
                )
            )
        }
    }
}

