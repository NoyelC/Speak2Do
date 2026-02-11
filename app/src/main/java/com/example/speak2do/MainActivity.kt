package com.example.speak2do

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.example.speak2do.components.MainScreen
import com.example.speak2do.data.VoiceRecordEntity
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.model.VoiceRecord
import com.example.speak2do.navigation.AppNavGraph
import com.example.speak2do.ui.theme.Speak2DoTheme
import com.example.speak2do.util.formatTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private var timerJob: Job? = null
    private val viewModel: VoiceRecordViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // Handle permission denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        enableEdgeToEdge()

        setContent {
            Speak2DoTheme {
                val voiceRecordEntities by viewModel.voiceRecords.collectAsState()
                val spokenText by viewModel.spokenText.collectAsState()
                val isRecording by viewModel.isRecording.collectAsState()
                val recordingTime by viewModel.recordingTime.collectAsState()

                val voiceRecords = voiceRecordEntities.map { entity ->
                    VoiceRecord(
                        text = entity.text,
                        dateTime = entity.dateTime,
                        fullDateTime = entity.fullDateTime,
                        duration = entity.duration,
                        progress = entity.progress
                    )
                }

                val recordings = voiceRecordEntities.map { entity ->
                    RecordingItem(
                        id = entity.id,
                        text = entity.text,
                        dateTime = entity.dateTime,
                        duration = "VOICE",
                        progress = entity.progress,
                        isCompleted = entity.isCompleted
                    )
                }

                MainScreen(
                    spokenText = spokenText,
                    isRecording = isRecording,
                    recordingTime = recordingTime,
                    recordings = recordings,
                    voiceRecords = voiceRecords,
                    onMicClick = { startListening() },
                    onToggleCompleted = { id, isCompleted ->
                        viewModel.toggleCompleted(id, isCompleted)
                    }
                )
                AppNavGraph()
            }
        }

        setupSpeechRecognizer()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.getOrNull(0) ?: ""

                viewModel.setSpokenText(text)

                if (text.isNotEmpty()) {
                    val dateTime = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("hh:mm a"))
                    val fullDateTime = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy | hh:mm a"))

                    val entity = VoiceRecordEntity(
                        text = text,
                        dateTime = dateTime,
                        fullDateTime = fullDateTime,
                        duration = formatTime(viewModel.recordingTime.value),
                        progress = 1f
                    )

                    viewModel.insertVoiceRecord(entity)
                }

                stopRecording()
            }

            override fun onError(error: Int) {
                stopRecording()
            }

            override fun onEndOfSpeech() {
                // Don't stop immediately, wait for results
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }

        viewModel.setIsRecording(true)
        viewModel.setRecordingTime(0)
        viewModel.setSpokenText("")
        startTimer()
        speechRecognizer.startListening(intent)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModel.viewModelScope.launch {
            viewModel.setRecordingTime(0)
            while (viewModel.isRecording.value) {
                delay(1000)
                viewModel.incrementRecordingTime()
            }
        }
    }

    private fun stopRecording() {
        viewModel.setIsRecording(false)
        timerJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
