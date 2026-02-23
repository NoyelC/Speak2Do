package com.example.speak2do.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
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
import com.example.speak2do.calendar.CalendarDayEvent
import com.example.speak2do.components.MainScreen
import com.example.speak2do.components.CalendarSyncOption
import com.example.speak2do.components.NotificationsScreen
import com.example.speak2do.components.TasksScreen
import com.example.speak2do.data.VoiceRecordEntity
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AppNavGraph(
    onMicClick: () -> Unit,
    onCancelRecording: () -> Unit = {},
    userName: String = "User",
    onSignOut: () -> Unit = {},
    onUpdateName: (String) -> Unit = {},
    isDarkMode: Boolean = true,
    onDarkModeChange: (Boolean) -> Unit = {},
    profileImageUri: Uri? = null,
    onPickProfileImage: () -> Unit = {},
    onRemoveProfileImage: () -> Unit = {},
    onSyncEventToDeviceCalendar: (LocalDate, String, String, String) -> Unit = { _, _, _, _ -> },
    onGetDeviceCalendarEventsForDay: suspend (LocalDate) -> Result<List<CalendarDayEvent>> = { Result.success(emptyList()) },
    onAddNoteToDeviceCalendarEvent: suspend (Long, String) -> Result<Unit> = { _, _ -> Result.success(Unit) }
) {
    val navController = rememberNavController()
    val viewModel: VoiceRecordViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val voiceRecordEntities by viewModel.voiceRecords.collectAsState()
    val spokenText by viewModel.spokenText.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingTime by viewModel.recordingTime.collectAsState()
    val voiceLevel by viewModel.voiceLevel.collectAsState()
    val notificationHistory by viewModel.notificationHistory.collectAsState()
    val unreadNotifications by viewModel.unreadNotifications.collectAsState()
    var taskSearchOverride by remember { mutableStateOf<String?>(null) }

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
            snackbarHostState.currentSnackbarData?.dismiss()
            val backup = viewModel.getRecordById(id)
            viewModel.deleteRecord(id)
            val result = snackbarHostState.showSnackbar(
                message = "Task deleted",
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && backup != null) {
                viewModel.insertRecord(backup)
            }
        }
    }

    val scaffoldBg = if (isDarkMode) DarkBackground else Color(0xFFF4F8FF)
    val snackbarContainer = if (isDarkMode) CardBackground else Color(0xFFEAF1FF)
    val snackbarContent = if (isDarkMode) Color.White else Color(0xFF1A3150)
    val snackbarAction = if (isDarkMode) PrimaryCyan else Color(0xFF2E77D0)

    Scaffold(
        containerColor = scaffoldBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = snackbarContainer,
                    contentColor = snackbarContent,
                    actionColor = snackbarAction,
                    shape = RoundedCornerShape(Dimens.SmallCornerRadius)
                )
            }
        },
        bottomBar = {
            val pendingCount = recordings.count { !it.isCompleted }
            BottomNavigationBar(
                navController = navController,
                pendingCount = pendingCount,
                isDarkMode = isDarkMode
            )
        },
        floatingActionButton = {
            if (currentRoute == BottomNavItem.Home.route) {
                FloatingActionButton(
                    onClick = onMicClick,
                    containerColor = Color(0xFF7A5AF8),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .size(62.dp)
                        .semantics { contentDescription = "Record new task" }
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
                    isDarkMode = isDarkMode,
                    voiceLevel = voiceLevel,
                    onMicClick = onMicClick,
                    onCancelRecording = onCancelRecording,
                    userName = userName,
                    profileImageUri = profileImageUri,
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
                    isDarkMode = isDarkMode,
                    searchQueryOverride = taskSearchOverride,
                    onSearchQueryOverrideApplied = { taskSearchOverride = null },
                    onToggleCompleted = { id, completed ->
                        viewModel.toggleCompleted(id, completed)
                    },
                    onDelete = onDeleteWithUndo,
                    onAddEvent = { date, title, time, notes, syncOption ->
                        val localTime = try {
                            LocalTime.parse(time)
                        } catch (_: Exception) {
                            LocalTime.of(9, 0)
                        }
                        val dateTime = date.atTime(localTime)
                        val timestamp = dateTime
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        val displayTime = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                        val fullDateTime = dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy | hh:mm a"))
                        val content = if (notes.isNotBlank()) "$title - $notes" else title

                        viewModel.insertRecord(
                            VoiceRecordEntity(
                                text = content,
                                dateTime = displayTime,
                                fullDateTime = fullDateTime,
                                duration = "EVENT",
                                progress = 1f,
                                createdAt = timestamp
                            )
                        )
                        if (syncOption == CalendarSyncOption.APP_AND_DEVICE_CALENDAR) {
                            onSyncEventToDeviceCalendar(date, title, time, notes)
                        }
                    },
                    onGetEventsForDay = onGetDeviceCalendarEventsForDay,
                    onAddNoteToCalendarEvent = onAddNoteToDeviceCalendarEvent
                )
            }
            composable(BottomNavItem.Stats.route) {
                com.example.speak2do.components.StatsScreen(
                    recordings = recordings,
                    isDarkMode = isDarkMode
                )
            }
            composable(BottomNavItem.Profile.route) {
                com.example.speak2do.components.ProfileScreen(
                    recordings = recordings,
                    userName = userName,
                    profileImageUri = profileImageUri,
                    onPickImage = onPickProfileImage,
                    onRemoveProfileImage = onRemoveProfileImage,
                    onSignOut = onSignOut,
                    onUpdateName = onUpdateName,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onOpenNotifications = {
                        navController.navigate("notifications")
                    }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    notifications = notificationHistory,
                    unreadCount = unreadNotifications,
                    isDarkMode = isDarkMode,
                    onMarkRead = { id -> viewModel.markNotificationRead(id) },
                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                    onDelete = { id -> viewModel.deleteNotification(id) },
                    onClearAll = { viewModel.clearAllNotifications() },
                    onOpenTask = { taskId ->
                        val match = recordings.firstOrNull { it.id == taskId }
                        taskSearchOverride = match?.text?.takeIf { it.isNotBlank() }
                        navController.navigate(BottomNavItem.Tasks.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    pendingCount: Int = 0,
    isDarkMode: Boolean = true
) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Tasks,
        BottomNavItem.Stats,
        BottomNavItem.Profile
    )

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val glassBg = if (isDarkMode) Color(0x26FFFFFF) else Color(0x80FFFFFF)
    val glassBorder = if (isDarkMode) Color(0x40FFFFFF) else Color(0x669BC3FF)
    val selectedColor = if (isDarkMode) Color(0xFF67D7FF) else Color(0xFF2E77D0)
    val unselectedColor = if (isDarkMode) Color(0xB3D2DDF5) else Color(0xFF6B7FA0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(glassBg)
            .border(width = 1.dp, color = glassBorder, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (item == BottomNavItem.Tasks && pendingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = BadgeRed,
                                    contentColor = Color.White
                                ) {
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
                                tint = if (selected) selectedColor else unselectedColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (selected) selectedColor else unselectedColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        item.label,
                        color = if (selected) selectedColor else unselectedColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
