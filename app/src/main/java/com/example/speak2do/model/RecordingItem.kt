package com.example.speak2do.model

data class RecordingItem(
    val id: Long,
    val text: String,
    val dateTime: String,
    val duration: String,
    val progress: Float,
    val isCompleted: Boolean = false
)
