package com.example.speak2do.model

data class VoiceRecord(
    val text: String,
    val dateTime: String,
    val fullDateTime: String,
    val duration: String,
    val progress: Float
)
