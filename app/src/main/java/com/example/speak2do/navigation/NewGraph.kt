package com.example.speak2do.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.speak2do.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    onMicClick: () -> Unit,
    onCancelRecording: () -> Unit = {},
    userName: String = "User",
    onSignOut: () -> Unit = {},
    onUpdateName: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    val viewModel: VoiceRecordViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val voiceRecordEntities by viewModel.voiceRecords.collectAsState()
    val spokenText by viewModel.spokenText.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingTime by viewModel.recordingTime.collectAsState()

    // Show shimmer until first real data arrives from Room
    var hasLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(voiceRecordEntities) {
        if (!hasLoaded) {
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
            isCompleted = entity.isCompleted,
            createdAt = entity.createdAt
        )
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val onDeleteWithUndo: (Long) -> Unit = { id ->
        scope.launch {
            val backup = viewModel.getRecordById(id)
            viewModel.deleteRecord(id)
            val result = snackbarHostState.showSnackbar(
                message = "Task deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && backup != null) {
                viewModel.insertRecord(backup)
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CardBackground,
                    contentColor = Color.White,
                    actionColor = PrimaryCyan,
                    shape = RoundedCornerShape(Dimens.SmallCornerRadius)
                )
            }
        },
        bottomBar = {
            val pendingCount = recordings.count { !it.isCompleted }
            BottomNavigationBar(navController, pendingCount = pendingCount)
        },
        floatingActionButton = {
            if (currentRoute == BottomNavItem.Home.route) {
                val pulseTransition = rememberInfiniteTransition(label = "fabPulse")
                val glowAlpha by pulseTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "fabGlowAlpha"
                )
                val glowSize by pulseTransition.animateFloat(
                    initialValue = 72f,
                    targetValue = 84f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "fabGlowSize"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.semantics {
                        contentDescription = "Record new task"
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(glowSize.dp)
                            .background(
                                PrimaryCyan.copy(alpha = glowAlpha * 0.4f),
                                CircleShape
                            )
                    )
                    FloatingActionButton(
                        onClick = onMicClick,
                        containerColor = PrimaryCyan,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Record voice task",
                            tint = Color.White,
                            modifier = Modifier.size(Dimens.IconSizeLg)
                        )
                    }
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
                    onCancelRecording = onCancelRecording,
                    userName = userName,
                    onSeeAllClick = {
                        navController.navigate(BottomNavItem.Tasks.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    onAvatarClick = {
                        navController.navigate(BottomNavItem.Profile.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    onToggleCompleted = { id, completed ->
                        viewModel.toggleCompleted(id, completed)
                    },
                    onDelete = onDeleteWithUndo
                )
            }
            composable(BottomNavItem.Tasks.route) {
                TasksScreen(
                    recordings = recordings,
                    isLoading = isLoading,
                    onToggleCompleted = { id, completed ->
                        viewModel.toggleCompleted(id, completed)
                    },
                    onDelete = onDeleteWithUndo
                )
            }
            composable(BottomNavItem.Stats.route) {
                com.example.speak2do.components.StatsScreen(recordings = recordings)
            }
            composable(BottomNavItem.Profile.route) {
                com.example.speak2do.components.ProfileScreen(
                    recordings = recordings,
                    userName = userName,
                    onSignOut = onSignOut,
                    onUpdateName = onUpdateName
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavController, pendingCount: Int = 0) {

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
                    if (item == BottomNavItem.Tasks && pendingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = BadgeRed,
                                    contentColor = Color.White
                                ) {
                                    // Animated badge count
                                    AnimatedContent(
                                        targetState = pendingCount,
                                        transitionSpec = {
                                            (fadeIn(tween(200)) + scaleIn(tween(200)))
                                                .togetherWith(fadeOut(tween(150)) + scaleOut(tween(150)))
                                        },
                                        label = "badgeCount"
                                    ) { count ->
                                        Text(
                                            "$count",
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = "${item.label}, $pendingCount pending",
                                tint = if (selected) PrimaryCyan else MutedText
                            )
                        }
                    } else {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (selected) PrimaryCyan else MutedText
                        )
                    }
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryCyan.copy(alpha = 0.15f)
                )
            )
        }
    }
}
