package com.example.speak2do.components

import android.net.Uri
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val MainBg = Color(0xFF0A1020)
private val MainHeroStart = Color(0xFF113B6F)
private val MainHeroEnd = Color(0xFF24A6B9)
private val MainGlass = Color(0x1FFFFFFF)
private val MainGlassBorder = Color(0x4DFFFFFF)
private val MainCard = Color(0xFF151E34)
private val MainPrimaryText = Color(0xFFFFFFFF)
private val MainSecondaryText = Color(0xFFB6C5E5)
private val MainAccent = Color(0xFF67D7FF)
private val MainPending = Color(0xFF8E7CFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    spokenText: String,
    isRecording: Boolean,
    recordingTime: Int,
    recordings: List<RecordingItem>,
    isLoading: Boolean,
    voiceLevel: Float = 0f,
    onMicClick: () -> Unit,
    onCancelRecording: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onToggleCompleted: (Long, Boolean) -> Unit = { _, _ -> },
    onDelete: (Long) -> Unit = {},
    userName: String = "User",
    profileImageUri: Uri? = null,
    isDarkMode: Boolean = true
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSnapshotTask by remember { mutableStateOf<RecordingItem?>(null) }

    val today = LocalDate.now()
    val pendingRecordings = recordings.filter { !it.isCompleted }
    val baseSections = listOf(
        "Due Today" to pendingRecordings
            .filter { item ->
                Instant.ofEpochMilli(item.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate() == today
            }
            .sortedBy { it.createdAt }
    )

    val filteredSections = if (searchQuery.isBlank()) {
        baseSections
    } else {
        baseSections.map { (title, items) ->
            title to items.filter { item ->
                item.text.contains(searchQuery, ignoreCase = true) ||
                    item.dateTime.contains(searchQuery, ignoreCase = true) ||
                    item.duration.contains(searchQuery, ignoreCase = true)
            }
        }
    }.filter { it.second.isNotEmpty() }

    val filteredCount = filteredSections.sumOf { it.second.size }

    // Animated stat values
    val total = recordings.size
    val completed = recordings.count { it.isCompleted }
    val pending = total - completed

    val animatedTotal by animateIntAsState(
        targetValue = total,
        animationSpec = tween(400),
        label = "animTotal"
    )
    val animatedCompleted by animateIntAsState(
        targetValue = completed,
        animationSpec = tween(400),
        label = "animCompleted"
    )
    val animatedPending by animateIntAsState(
        targetValue = pending,
        animationSpec = tween(400),
        label = "animPending"
    )

    // Pull to refresh
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(600)
            isRefreshing = false
        }
    }

    val bgColor = if (isDarkMode) MainBg else Color(0xFFF4F8FF)
    val heroStart = if (isDarkMode) MainHeroStart else Color(0xFF2F5E96)
    val heroEnd = if (isDarkMode) MainHeroEnd else Color(0xFF6CB8E9)
    val glassColor = if (isDarkMode) MainGlass else Color(0xCCFFFFFF)
    val glassBorderColor = if (isDarkMode) MainGlassBorder else Color(0x668DB7FF)
    val cardColor = if (isDarkMode) MainCard else Color(0xFFEAF1FF)
    val primaryTextColor = if (isDarkMode) MainPrimaryText else Color(0xFF0F2744)
    val secondaryTextColor = if (isDarkMode) MainSecondaryText else Color(0xFF5C7391)
    val accentColor = if (isDarkMode) MainAccent else Color(0xFF2E77D0)
    val pendingColor = if (isDarkMode) MainPending else Color(0xFF6E63D9)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .pullToRefresh(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                onRefresh = { isRefreshing = true }
            )
    ) {
        val heroShape = GenericShape { size, _ ->
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.84f)
            quadraticBezierTo(size.width * 0.75f, size.height, size.width * 0.48f, size.height * 0.88f)
            quadraticBezierTo(size.width * 0.2f, size.height * 0.76f, 0f, size.height * 0.9f)
            close()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .clip(heroShape)
                .background(Brush.linearGradient(listOf(heroStart, heroEnd)))
                .drawWithCache {
                    onDrawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color(0x66FFFFFF), Color.Transparent)),
                            radius = size.minDimension * 0.46f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.18f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color(0x6657E8FF), Color.Transparent)),
                            radius = size.minDimension * 0.32f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.14f)
                        )
                    }
                }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 26.dp, bottom = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(glassColor)
                        .border(BorderStroke(1.dp, glassBorderColor), RoundedCornerShape(24.dp))
                        .padding(14.dp)
                ) {
                    HeaderSection(
                        userName = userName,
                        profileImageUri = profileImageUri,
                        greetingColor = if (isDarkMode) Color(0xFFEAF3FF) else Color(0xFF183A62),
                        nameColor = if (isDarkMode) Color.White else Color(0xFF0F2744),
                        dateColor = if (isDarkMode) Color(0xCCFFFFFF) else Color(0xFF4D6790),
                        onAvatarClick = onAvatarClick
                    )
                }
            }

            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    resultCount = if (searchQuery.isNotBlank()) filteredCount else -1
                )
            }

            item {
                VoiceAssistantCard(
                    isRecording = isRecording,
                    recordingTime = recordingTime,
                    spokenText = spokenText,
                    voiceLevel = voiceLevel,
                    isDarkMode = isDarkMode,
                    onMicClick = onMicClick,
                    onCancelRecording = onCancelRecording
                )
            }

            // Quick stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Quick stats: $total total, $completed done, $pending pending" },
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickStatChip(
                        modifier = Modifier.weight(1f),
                        value = "$animatedTotal",
                        label = "Total",
                        color = accentColor,
                        isDarkMode = isDarkMode
                    )
                    QuickStatChip(
                        modifier = Modifier.weight(1f),
                        value = "$animatedCompleted",
                        label = "Done",
                        color = SuccessGreen,
                        isDarkMode = isDarkMode
                    )
                    QuickStatChip(
                        modifier = Modifier.weight(1f),
                        value = "$animatedPending",
                        label = "Pending",
                        color = pendingColor,
                        isDarkMode = isDarkMode
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Today Snapshot",
                        color = primaryTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "See all",
                        color = accentColor,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(glassColor)
                            .clickable { onSeeAllClick() }
                            .padding(Dimens.SpacingXs)
                            .semantics { contentDescription = "See all tasks" }
                    )
                }
            }

            if (isLoading) {
                item {
                    ShimmerTaskList(count = 3)
                }
            } else if (filteredSections.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.Mic,
                            contentDescription = "No tasks icon",
                            tint = secondaryTextColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(Dimens.MinTouchTarget)
                        )
                        Spacer(Modifier.height(Dimens.SpacingMd))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching tasks" else "No snapshot tasks",
                            color = secondaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try a different search term"
                            else "You are clear for now. Open My Tasks for the full list.",
                            color = secondaryTextColor.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                filteredSections.forEach { (sectionTitle, sectionItems) ->
                    item(key = "section-$sectionTitle") {
                        SnapshotSectionHeader(
                            title = sectionTitle,
                            count = sectionItems.size,
                            isDarkMode = isDarkMode
                        )
                    }
                    itemsIndexed(sectionItems, key = { _, item -> "$sectionTitle-${item.id}" }) { index, item ->
                        AnimatedListItem(index = index) {
                            SwipeableRecordingCard(
                                item = item,
                                onToggleCompleted = onToggleCompleted,
                                onDelete = onDelete,
                                searchQuery = searchQuery,
                                useTasksStyle = true,
                                isDarkMode = isDarkMode,
                                onCardClick = { selectedSnapshotTask = it }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(Dimens.SpacingXl))
            }
        }

        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            color = accentColor
        )
    }

    if (selectedSnapshotTask != null) {
        TaskDetailsDialog(
            item = selectedSnapshotTask!!,
            onDismiss = { selectedSnapshotTask = null },
            isDarkMode = isDarkMode,
            title = "Today Snapshot"
        )
    }
}

@Composable
private fun SnapshotSectionHeader(
    title: String,
    count: Int,
    isDarkMode: Boolean
) {
    val (icon, accent, helperText) = when (title) {
        "Overdue" -> Triple(
            Icons.Rounded.WarningAmber,
            if (isDarkMode) Color(0xFFFF7676) else Color(0xFFC73636),
            "Needs attention"
        )
        "Due Today" -> Triple(
            Icons.Rounded.Today,
            if (isDarkMode) Color(0xFF67D7FF) else Color(0xFF2E77D0),
            "Do these first"
        )
        else -> Triple(
            Icons.Rounded.Schedule,
            if (isDarkMode) Color(0xFF8E7CFF) else Color(0xFF5F55CE),
            "Coming up next"
        )
    }

    val container = if (isDarkMode) Color(0x261D2A44) else Color(0xFFEFF4FF)
    val label = if (isDarkMode) MainSecondaryText else Color(0xFF5C7391)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = helperText,
                color = label,
                fontSize = 11.sp
            )
        }
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(accent)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun QuickStatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color,
    isDarkMode: Boolean = true
) {
    val chipBg = if (isDarkMode) MainCard else Color(0xFFEAF1FF)
    val chipBorder = if (isDarkMode) Color(0x26FFFFFF) else Color(0x338DB7FF)
    val chipLabel = if (isDarkMode) MainSecondaryText else Color(0xFF5C7391)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.PillCornerRadius))
            .background(chipBg)
            .border(BorderStroke(1.dp, chipBorder), RoundedCornerShape(Dimens.PillCornerRadius))
            .padding(Dimens.SpacingMd)
            .semantics { contentDescription = "$label: $value" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = chipLabel,
            fontSize = 12.sp
        )
    }
}
