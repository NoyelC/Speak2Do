package com.example.speak2do.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    recordings: List<RecordingItem>,
    isLoading: Boolean = false,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("All", "Today", "Upcoming")

    val today = LocalDate.now()

    val filteredRecordings = when (selectedTab) {
        1 -> recordings.filter { item ->
            val itemDate = Instant.ofEpochMilli(item.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            itemDate == today
        }
        2 -> recordings.filter { item ->
            !item.isCompleted
        }
        else -> recordings
    }.let { list ->
        if (searchQuery.isBlank()) list
        else list.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

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

            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                resultCount = if (searchQuery.isNotBlank()) filteredRecordings.size else -1
            )

            Spacer(Modifier.height(Dimens.SpacingLg))

            // Animated pill tabs
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
            } else if (filteredRecordings.isEmpty()) {
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
                            else -> "Use voice to create tasks"
                        },
                        color = MutedText.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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
        }

        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            color = PrimaryCyan
        )
    }
}
