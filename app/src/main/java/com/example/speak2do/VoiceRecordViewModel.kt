package com.example.speak2do

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.speak2do.data.AppDatabase
import com.example.speak2do.data.NotificationHistoryEntity
import com.example.speak2do.data.VoiceRecordEntity
import com.example.speak2do.reminder.DeadlineReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VoiceRecordViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.voiceRecordDao()
    private val notificationDao = db.notificationHistoryDao()

    val voiceRecords: StateFlow<List<VoiceRecordEntity>> =
        dao.getAllRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val notificationHistory: StateFlow<List<NotificationHistoryEntity>> =
        notificationDao.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val unreadNotifications: StateFlow<Int> =
        notificationDao.getUnreadCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingTime = MutableStateFlow(0)
    val recordingTime: StateFlow<Int> = _recordingTime.asStateFlow()

    private val _voiceLevel = MutableStateFlow(0f)
    val voiceLevel: StateFlow<Float> = _voiceLevel.asStateFlow()

    init {
        viewModelScope.launch {
            val eventRecords = dao.getActiveEventRecords()
            DeadlineReminderScheduler.syncReminders(appContext, eventRecords)
        }
    }

    fun setSpokenText(text: String) {
        _spokenText.value = text
    }

    fun setIsRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setRecordingTime(time: Int) {
        _recordingTime.value = time
    }

    fun incrementRecordingTime() {
        _recordingTime.value++
    }

    fun setVoiceLevel(level: Float) {
        _voiceLevel.value = level.coerceIn(0f, 10f)
    }

    fun insertRecord(record: VoiceRecordEntity) {
        viewModelScope.launch {
            val newId = dao.insert(record)
            val saved = record.copy(id = newId)
            if (isNotifiableEvent(saved)) {
                DeadlineReminderScheduler.scheduleReminder(appContext, saved)
            }
        }
    }

    fun toggleCompleted(id: Long, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            val updatedCompleted = !currentlyCompleted
            dao.updateCompleted(id, updatedCompleted)
            val updated = dao.getById(id) ?: return@launch
            if (updatedCompleted || !isNotifiableEvent(updated)) {
                DeadlineReminderScheduler.cancelReminder(appContext, id)
            } else {
                DeadlineReminderScheduler.scheduleReminder(appContext, updated)
            }
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
            DeadlineReminderScheduler.cancelReminder(appContext, id)
        }
    }

    fun updateRecordText(id: Long, newText: String) {
        val normalized = newText.trim()
        if (normalized.isBlank()) return
        viewModelScope.launch {
            dao.updateText(id, normalized)
            val updated = dao.getById(id) ?: return@launch
            if (isNotifiableEvent(updated)) {
                DeadlineReminderScheduler.scheduleReminder(appContext, updated)
            } else {
                DeadlineReminderScheduler.cancelReminder(appContext, id)
            }
        }
    }

    suspend fun getRecordById(id: Long) = dao.getById(id)

    fun markNotificationRead(notificationId: Long) {
        viewModelScope.launch {
            notificationDao.markRead(notificationId)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            notificationDao.markAllRead()
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationDao.deleteById(notificationId)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationDao.clearAll()
        }
    }

    private fun isNotifiableEvent(record: VoiceRecordEntity): Boolean {
        return record.duration.equals("EVENT", ignoreCase = true) &&
            !record.isCompleted &&
            record.createdAt > System.currentTimeMillis()
    }
}
