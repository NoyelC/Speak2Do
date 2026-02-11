package com.example.speak2do.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.model.VoiceRecord
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.PrimaryPurple
import com.example.speak2do.ui.theme.WhiteText

@Composable
fun MainScreen(
    spokenText: String,
    isRecording: Boolean,
    recordingTime: Int,
    recordings: List<RecordingItem>,
    voiceRecords: List<VoiceRecord>,
    onMicClick: () -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit = { _, _ -> }
) {
    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onMicClick,
                containerColor = PrimaryPurple,
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
    ) { padding ->
        var searchQuery by remember { mutableStateOf("") }

        val filteredVoiceRecords = if (searchQuery.isBlank()) voiceRecords
            else voiceRecords.filter { it.text.contains(searchQuery, ignoreCase = true) }

        val filteredRecordings = if (searchQuery.isBlank()) recordings
            else recordings.filter { it.text.contains(searchQuery, ignoreCase = true) }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
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

            // Voice Records History Section
            if (filteredVoiceRecords.isNotEmpty()) {
                item {
                    Text(
                        "Voice Records",
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(filteredVoiceRecords) { voiceRecord ->
                    VoiceRecordCard(voiceRecord)
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Tasks", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("View Calendar", color = PrimaryPurple, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            items(filteredRecordings) { item ->
                RecordingCard(item = item, onToggleCompleted = onToggleCompleted)
            }

            item {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
