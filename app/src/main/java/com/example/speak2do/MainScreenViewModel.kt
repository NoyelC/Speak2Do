package com.example.speak2do

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speak2do.calendar.DeadlineExtractionInput
import com.example.speak2do.network.gemini.ExtractedTask
import com.example.speak2do.network.gemini.GeminiRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainScreenViewModel(private val repository: GeminiRepository) : ViewModel() {
    companion object {
        private const val FIRESTORE_DATABASE = "speak2do_database"
        private const val FIRESTORE_USERS_DOCUMENT = "users"
        private const val FIRESTORE_USER_ACCOUNTS_COLLECTION = "accounts"
        private const val FIRESTORE_TASKS_COLLECTION = "tasks"
    }

    private val _geminiResponse = MutableStateFlow("")
    val geminiResponse: StateFlow<String> = _geminiResponse

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _deadlineExtractionInput = MutableSharedFlow<DeadlineExtractionInput>(extraBufferCapacity = 1)
    val deadlineExtractionInput: SharedFlow<DeadlineExtractionInput> = _deadlineExtractionInput

    private val firestore = FirebaseFirestore.getInstance()
    private val json = Json { encodeDefaults = true }

    fun onVoiceStart() {
        _error.value = null
        _geminiResponse.value = ""
    }

    fun onVoiceResult(
        currentDate: String,
        transcript: String,
        userId: String?,
        userDisplayName: String?,
        userPhone: String?,
        userEmail: String?
    ) {
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
                    _deadlineExtractionInput.tryEmit(
                        DeadlineExtractionInput(
                            currentDate = currentDate,
                            transcript = transcript,
                            extractedTask = task
                        )
                    )
                    saveResultToFirestore(
                        userId = userId,
                        userDisplayName = userDisplayName,
                        userPhone = userPhone,
                        userEmail = userEmail,
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
        userDisplayName: String?,
        userPhone: String?,
        userEmail: String?,
        currentDate: String,
        transcript: String,
        task: ExtractedTask,
        taskJson: String
    ) {
        val resolvedUserName = userDisplayName?.takeIf { it.isNotBlank() } ?: "Unknown User"
        val payload = hashMapOf(
            "userId" to userId,
            "userDisplayName" to resolvedUserName,
            "userPhone" to userPhone,
            "userEmail" to userEmail,
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
        val userProfilePayload = hashMapOf(
            "userId" to userId,
            "displayName" to resolvedUserName,
            "phone" to userPhone,
            "email" to userEmail,
            "lastTaskAt" to System.currentTimeMillis()
        )

        suspendCancellableCoroutine<Unit> { cont ->
            firestore
                .collection(FIRESTORE_DATABASE)
                .document(FIRESTORE_USERS_DOCUMENT)
                .collection(FIRESTORE_USER_ACCOUNTS_COLLECTION)
                .document(userId)
                .set(userProfilePayload)
                .addOnSuccessListener {
                    firestore
                        .collection(FIRESTORE_DATABASE)
                        .document(FIRESTORE_USERS_DOCUMENT)
                        .collection(FIRESTORE_USER_ACCOUNTS_COLLECTION)
                        .document(userId)
                        .collection(FIRESTORE_TASKS_COLLECTION)
                        .document()
                        .set(payload)
                        .addOnSuccessListener {
                            if (cont.isActive) cont.resume(Unit)
                        }
                        .addOnFailureListener { e: Exception ->
                            if (cont.isActive) cont.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e: Exception ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }
    }
}
