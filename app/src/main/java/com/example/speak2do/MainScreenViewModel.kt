package com.example.speak2do

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speak2do.data.Task
import com.example.speak2do.data.TaskFirestoreRepository
import com.example.speak2do.network.gemini.ExtractedTask
import com.example.speak2do.network.gemini.GeminiRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainScreenViewModel(private val repository: GeminiRepository) : ViewModel() {
    companion object {
        private const val FIRESTORE_DATABASE = "speak2do_database"
        private const val FIRESTORE_TABLE = "tasks_table"
    }

    private val _geminiResponse = MutableStateFlow("")
    val geminiResponse: StateFlow<String> = _geminiResponse

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val firestore = FirebaseFirestore.getInstance()
    private val taskFirestoreRepository = TaskFirestoreRepository()
    private val json = Json { encodeDefaults = true }

    fun onVoiceStart() {
        _error.value = null
        _geminiResponse.value = ""
    }

    fun onVoiceResult(currentDate: String, transcript: String, userId: String?) {
        if (transcript.isBlank()) {
            _error.value = "Empty voice transcript"
            return
        }
        if (userId.isNullOrBlank()) {
            _error.value = "User not logged in"
            return
        }

        _isProcessing.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repository.generateTaskJson(currentDate, transcript)
                if (result.isSuccess) {
                    val task = result.getOrThrow()
                    val taskJson = json.encodeToString(task)
                    _geminiResponse.value = taskJson
                    taskFirestoreRepository.saveTask(
                        Task(
                            task_title = task.task_title ?: "",
                            description = task.description,
                            date_time = task.date_time ?: "",
                            priority = task.priority,
                            additional_notes = task.additional_notes
                        )
                    )
                    saveResultToFirestore(
                        userId = userId,
                        currentDate = currentDate,
                        transcript = transcript,
                        task = task,
                        taskJson = taskJson
                    )
                    Log.d("MainScreenViewModel", "Gemini task saved for user: $userId")
                } else {
                    val throwable = result.exceptionOrNull()
                    _error.value = throwable?.message ?: "Gemini request failed"
                    Log.e("MainScreenViewModel", "Gemini failed", throwable)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private suspend fun saveResultToFirestore(
        userId: String,
        currentDate: String,
        transcript: String,
        task: ExtractedTask,
        taskJson: String
    ) {
        val payload = hashMapOf(
            "userId" to userId,
            "currentDate" to currentDate,
            "transcript" to transcript,
            "json" to taskJson,
            "task_title" to task.task_title,
            "description" to task.description,
            "date_time" to task.date_time,
            "priority" to task.priority,
            "additional_notes" to task.additional_notes,
            "createdAt" to System.currentTimeMillis()
        )

        suspendCancellableCoroutine<Unit> { cont ->
            firestore
                .collection(FIRESTORE_DATABASE)
                .document(FIRESTORE_TABLE)
                .collection(userId)
                .document()
                .set(payload)
                .addOnSuccessListener {
                    if (cont.isActive) cont.resume(Unit)
                }
                .addOnFailureListener { e: Exception ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }
    }
}
