package com.example.speak2do.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan
import com.example.speak2do.ui.theme.WhiteText

@Composable
fun MainScreen(
    spokenText: String,
    isRecording: Boolean,
    recordingTime: Int,
    recordings: List<RecordingItem>,
    onMicClick: () -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecordings = if (searchQuery.isBlank()) recordings
        else recordings.filter { it.text.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderSection()
        }

        item {
            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
        }

        item {
            VoiceAssistantCard(
                isRecording = isRecording,
                recordingTime = recordingTime,
                spokenText = spokenText,
                onMicClick = onMicClick
            )
        }

        // Quick stats row
        item {
            val total = recordings.size
            val completed = recordings.count { it.isCompleted }
            val pending = total - completed

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStatChip(
                    modifier = Modifier.weight(1f),
                    value = "$total",
                    label = "Total",
                    color = PrimaryCyan
                )
                QuickStatChip(
                    modifier = Modifier.weight(1f),
                    value = "$completed",
                    label = "Done",
                    color = Color(0xFF4CAF50)
                )
                QuickStatChip(
                    modifier = Modifier.weight(1f),
                    value = "$pending",
                    label = "Pending",
                    color = Color(0xFFFFA726)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Tasks", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("See all", color = PrimaryCyan, fontSize = 14.sp)
            }
        }

        if (filteredRecordings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = MutedText.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No tasks yet",
                        color = MutedText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tap the mic to create your first task",
                        color = MutedText.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            itemsIndexed(filteredRecordings) { index, item ->
                AnimatedListItem(index = index) {
                    RecordingCard(item = item, onToggleCompleted = onToggleCompleted)
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
        }
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
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .padding(12.dp),
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
