package com.example.speak2do

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.speak2do.data.AppDatabase
import com.example.speak2do.data.VoiceRecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VoiceRecordViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).voiceRecordDao()

    val voiceRecords: StateFlow<List<VoiceRecordEntity>> =
        dao.getAllRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingTime = MutableStateFlow(0)
    val recordingTime: StateFlow<Int> = _recordingTime.asStateFlow()

    private val _voiceLevel = MutableStateFlow(0f)
    val voiceLevel: StateFlow<Float> = _voiceLevel.asStateFlow()

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
            dao.insert(record)
        }
    }

    fun toggleCompleted(id: Long, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            dao.updateCompleted(id, !currentlyCompleted)
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    suspend fun getRecordById(id: Long) = dao.getById(id)
}
