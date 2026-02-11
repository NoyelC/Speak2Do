package com.example.speak2do.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.WhiteText
import com.example.speak2do.ui.theme.PrimaryCyan
@Composable
fun TasksScreen(
    recordings: List<RecordingItem>,
    isLoading: Boolean = false,
    onToggleCompleted: (Long, Boolean) -> Unit
) {

    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("All", "Today", "Upcoming")

    val filteredRecordings = when (selectedTab) {
        1 -> recordings
        2 -> recordings.filter { !it.isCompleted }
        else -> recordings
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {

        Text(
            text = "Tasks",
            fontSize = 24.sp,
            color = WhiteText
        )

        Spacer(Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkBackground,
            contentColor = PrimaryCyan
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index)
                                PrimaryCyan
                            else
                                MutedText
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

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
                    contentDescription = null,
                    tint = MutedText.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = when (selectedTab) {
                        2 -> "All caught up!"
                        else -> "No tasks yet"
                    },
                    color = MutedText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when (selectedTab) {
                        2 -> "No pending tasks remaining"
                        else -> "Use voice to create tasks"
                    },
                    color = MutedText.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(filteredRecordings) { index, item ->
                    AnimatedListItem(index = index) {
                        RecordingCard(item = item, onToggleCompleted = onToggleCompleted)
                    }
                }
            }
        }
    }
}
