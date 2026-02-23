package com.example.speak2do.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationHistoryDao {

    @Insert
    suspend fun insert(item: NotificationHistoryEntity): Long

    @Query("SELECT * FROM notification_history ORDER BY createdAt DESC")
    fun getAll(): Flow<List<NotificationHistoryEntity>>

    @Query("SELECT COUNT(*) FROM notification_history WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE notification_history SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE notification_history SET isRead = 1 WHERE taskId = :taskId")
    suspend fun markReadByTaskId(taskId: Long)

    @Query("UPDATE notification_history SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notification_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notification_history")
    suspend fun clearAll()
}
