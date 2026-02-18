package com.example.speak2do.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.*

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
    userName: String = "User"
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecordings = if (searchQuery.isBlank()) recordings
    else recordings.filter { it.text.contains(searchQuery, ignoreCase = true) }

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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            item {
                HeaderSection(userName = userName, onAvatarClick = onAvatarClick)
            }

            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    resultCount = if (searchQuery.isNotBlank()) filteredRecordings.size else -1
                )
            }

            item {
                VoiceAssistantCard(
                    isRecording = isRecording,
                    recordingTime = recordingTime,
                    spokenText = spokenText,
                    voiceLevel = voiceLevel,
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
                        color = PrimaryCyan
                    )
                    QuickStatChip(
                        modifier = Modifier.weight(1f),
                        value = "$animatedCompleted",
                        label = "Done",
                        color = SuccessGreen
                    )
                    QuickStatChip(
                        modifier = Modifier.weight(1f),
                        value = "$animatedPending",
                        label = "Pending",
                        color = WarningOrange
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
                        "My Tasks",
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "See all",
                        color = PrimaryCyan,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
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
            } else if (filteredRecordings.isEmpty()) {
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
                            tint = MutedText.copy(alpha = 0.4f),
                            modifier = Modifier.size(Dimens.MinTouchTarget)
                        )
                        Spacer(Modifier.height(Dimens.SpacingMd))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching tasks" else "No tasks yet",
                            color = MutedText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try a different search term"
                            else "Tap the mic to create your first task",
                            color = MutedText.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
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

            item {
                Spacer(Modifier.height(Dimens.SpacingXl))
            }
        }

        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            color = PrimaryCyan
        )
    }
}

@Composable
fun QuickStatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.PillCornerRadius))
            .background(CardBackground)
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
            color = MutedText,
            fontSize = 12.sp
        )
    }
}
