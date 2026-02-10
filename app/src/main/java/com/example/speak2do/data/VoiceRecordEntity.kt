package com.example.speak2do.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_records")
data class VoiceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val dateTime: String,
    val fullDateTime: String,
    val duration: String,
    val progress: Float,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
