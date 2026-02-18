package com.example.speak2do.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.Dimens
import com.example.speak2do.ui.theme.GrayText
import com.example.speak2do.ui.theme.LightCyan
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan
import com.example.speak2do.ui.theme.SuccessGreen
import com.example.speak2do.ui.theme.WarningOrange
import com.example.speak2do.ui.theme.WhiteText
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    recordings: List<RecordingItem>,
    isLoading: Boolean = false,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit = {},
    onAddEvent: (LocalDate, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("09:00") }
    var eventNotes by remember { mutableStateOf("") }

    val tabs = listOf("All", "Today", "Upcoming")

    val today = LocalDate.now()
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }

    val recordingsByDate = remember(recordings) {
        recordings.groupBy { item ->
            Instant.ofEpochMilli(item.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }
    val selectedDateCount = selectedDate?.let { recordingsByDate[it]?.size ?: 0 } ?: 0

    val filteredRecordings = when (selectedTab) {
        1 -> recordings.filter { item ->
            val itemDate = Instant.ofEpochMilli(item.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            itemDate == today
        }

        2 -> recordings.filter { item -> !item.isCompleted }
        else -> recordings
    }.let { tabFiltered ->
        val dateFiltered = selectedDate?.let { date ->
            tabFiltered.filter { item ->
                val itemDate = Instant.ofEpochMilli(item.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                itemDate == date
            }
        } ?: tabFiltered

        if (searchQuery.isBlank()) dateFiltered
        else dateFiltered.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(600)
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pullToRefresh(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                onRefresh = { isRefreshing = true }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.ScreenPadding)
        ) {
            Text(
                text = "Tasks",
                fontSize = 24.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(Dimens.SpacingLg))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                resultCount = if (searchQuery.isNotBlank()) filteredRecordings.size else -1
            )

            Spacer(Modifier.height(Dimens.SpacingLg))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.PillCornerRadius))
                    .background(CardBackground)
                    .padding(Dimens.SpacingXs),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) PrimaryCyan else CardBackground,
                        animationSpec = tween(250),
                        label = "tabBg$index"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) WhiteText else MutedText,
                        animationSpec = tween(250),
                        label = "tabText$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp)
                            .semantics {
                                contentDescription = "$title tab${if (isSelected) ", selected" else ""}"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingLg))

            if (isLoading) {
                ShimmerTaskList(count = 4)
            } else if (filteredRecordings.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                    contentPadding = PaddingValues(bottom = Dimens.SpacingLg)
                ) {
                    itemsIndexed(filteredRecordings, key = { _, item -> item.id }) { index, item ->
                        AnimatedListItem(index = index) {
                            SwipeableRecordingCard(
                                item = item,
                                onToggleCompleted = onToggleCompleted,
                                onDelete = onDelete,
                                searchQuery = searchQuery
                            )
                        }
                    }
                }
            }

            TasksCalendarCard(
                visibleMonth = visibleMonth,
                selectedDate = selectedDate,
                recordingsByDate = recordingsByDate,
                onPrevMonth = { visibleMonth = visibleMonth.minusMonths(1) },
                onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) },
                onJumpToToday = {
                    visibleMonth = YearMonth.now()
                    selectedDate = LocalDate.now()
                },
                onDateSelected = { clicked ->
                    selectedDate = if (selectedDate == clicked) null else clicked
                }
            )
            
            val overlapHeight = 84.dp
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = -overlapHeight)
                        .zIndex(1f)
                ) {
                    ShimmerTaskList(count = 4)
                }
            } else if (filteredRecordings.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                    contentPadding = PaddingValues(bottom = Dimens.SpacingLg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = -overlapHeight)
                        .zIndex(1f)
                ) {
                    itemsIndexed(filteredRecordings, key = { _, item -> item.id }) { index, item ->
                        AnimatedListItem(index = index) {
                            SwipeableRecordingCard(
                                item = item,
                                onToggleCompleted = onToggleCompleted,
                                onDelete = onDelete,
                                searchQuery = searchQuery
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(overlapHeight))
            AnimatedVisibility(visible = selectedDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.SpacingSm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedDate?.format(DateTimeFormatter.ofPattern("dd MMM"))} • $selectedDateCount task(s)",
                        color = GrayText,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Clear date filter",
                        color = LightCyan,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { selectedDate = null }
                    )
                }
            }
            AnimatedVisibility(visible = selectedDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.SpacingSm),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { showAddEventDialog = true }) {
                        Text("Add Event")
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingLg))

            if (!isLoading && filteredRecordings.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "No tasks",
                        tint = MutedText.copy(alpha = 0.4f),
                        modifier = Modifier.size(Dimens.MinTouchTarget)
                    )
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "No matching tasks"
                            selectedTab == 1 -> "No tasks today"
                            selectedTab == 2 -> "All caught up!"
                            selectedDate != null -> "No tasks for selected date"
                            else -> "No tasks yet"
                        },
                        color = MutedText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "Try a different search term"
                            selectedTab == 1 -> "Tasks created today will appear here"
                            selectedTab == 2 -> "No pending tasks remaining"
                            selectedDate != null -> "Tap another date in calendar"
                            else -> "Use voice to create tasks"
                        },
                        color = MutedText.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            color = PrimaryCyan
        )
    }

    if (showAddEventDialog && selectedDate != null) {
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Add Event", color = WhiteText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        singleLine = true,
                        label = { Text("Title") }
                    )
                    OutlinedTextField(
                        value = eventTime,
                        onValueChange = {
                            eventTime = it.filter { c -> c.isDigit() || c == ':' }.take(5)
                        },
                        singleLine = true,
                        label = { Text("Time (HH:mm)") }
                    )
                    OutlinedTextField(
                        value = eventNotes,
                        onValueChange = { eventNotes = it },
                        label = { Text("Notes") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (eventTitle.trim().isNotEmpty()) {
                            val normalizedTime = try {
                                LocalTime.parse(eventTime).toString().take(5)
                            } catch (_: Exception) {
                                "09:00"
                            }
                            onAddEvent(
                                selectedDate!!,
                                eventTitle.trim(),
                                normalizedTime,
                                eventNotes.trim()
                            )
                            eventTitle = ""
                            eventTime = "09:00"
                            eventNotes = ""
                            showAddEventDialog = false
                        }
                    }
                ) { Text("Save", color = PrimaryCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun TasksCalendarCard(
    visibleMonth: YearMonth,
    selectedDate: LocalDate?,
    recordingsByDate: Map<LocalDate, List<RecordingItem>>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val monthTitle = remember(visibleMonth) {
        visibleMonth.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }

    val firstDayOfMonth = visibleMonth.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = visibleMonth.lengthOfMonth()
    val totalCells = 42
    val today = LocalDate.now()
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.CardCornerRadius))
            .background(CardBackground)
            .padding(Dimens.SpacingLg)
            .pointerInput(visibleMonth) {
                var dragDistance = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        when {
                            dragDistance > 80f -> onPrevMonth()
                            dragDistance < -80f -> onNextMonth()
                        }
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f }
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Calendar",
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Today",
                    color = LightCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onJumpToToday() }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevMonth) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = MutedText
                    )
                }
                Text(
                    text = monthTitle,
                    color = LightCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Next month",
                        tint = MutedText
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingSm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    color = MutedText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (rowStart in 0 until totalCells step 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until 7) {
                        val cellIndex = rowStart + i
                        val dayNumber = cellIndex - startOffset + 1
                        if (dayNumber in 1..daysInMonth) {
                            val date = visibleMonth.atDay(dayNumber)
                            val eventsCount = recordingsByDate[date]?.size ?: 0
                            CalendarDayCell(
                                date = date,
                                isSelected = selectedDate == date,
                                isToday = date == today,
                                eventsCount = eventsCount,
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    eventsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryCyan.copy(alpha = 0.22f) else DarkBackground.copy(alpha = 0.45f),
        animationSpec = tween(180),
        label = "calendarDayBg"
    )
    val cellPadding by animateDpAsState(
        targetValue = if (isSelected) 9.dp else 8.dp,
        animationSpec = tween(180),
        label = "calendarDayPad"
    )
    val textColor = when {
        isSelected -> LightCyan
        isToday -> WarningOrange
        else -> GrayText
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = cellPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .height(4.dp)
                .width(if (eventsCount > 0) 14.dp else 4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    when {
                        eventsCount == 0 -> MutedText.copy(alpha = 0.25f)
                        eventsCount <= 2 -> SuccessGreen
                        else -> WarningOrange
                    }
                )
        )
    }
}
