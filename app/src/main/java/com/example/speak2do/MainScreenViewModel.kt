package com.example.speak2do

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speak2do.network.gemini.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(private val repository: GeminiRepository) : ViewModel() {
    private val _geminiResponse = MutableStateFlow("")
    val geminiResponse: StateFlow<String> = _geminiResponse

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onVoiceStart() {
        _error.value = null
        _geminiResponse.value = ""
    }

    fun onVoiceResult(currentDate: String, transcript: String) {
        if (transcript.isBlank()) {
            _error.value = "Empty voice transcript"
            return
        }
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val result = repository.generateTaskJson(currentDate, transcript)
                _geminiResponse.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
