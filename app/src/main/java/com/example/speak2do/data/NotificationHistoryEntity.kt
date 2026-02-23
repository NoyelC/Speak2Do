package com.example.speak2do.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val title: String,
    val body: String,
    val dueAtMillis: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
