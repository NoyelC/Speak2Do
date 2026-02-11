package com.example.speak2do.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceRecordDao {

    @Insert
    suspend fun insert(record: VoiceRecordEntity)

    @Query("SELECT * FROM voice_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<VoiceRecordEntity>>

    @Query("UPDATE voice_records SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM voice_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
