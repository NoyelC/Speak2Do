package com.example.speak2do

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import com.example.speak2do.auth.AuthViewModel
import com.example.speak2do.auth.NameSetupScreen
import com.example.speak2do.auth.OtpVerificationScreen
import com.example.speak2do.auth.PhoneLoginScreen
import com.example.speak2do.data.VoiceRecordEntity
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
    private val authViewModel: AuthViewModel by viewModels()
    private var isListening = false

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
                val authState by authViewModel.authState.collectAsState()
                val currentUser = authState.user
                val hasDisplayName = !currentUser?.displayName.isNullOrBlank()

                if (currentUser == null) {
                    if (authState.isCodeSent) {
                        OtpVerificationScreen(
                            state = authState,
                            onVerifyOtp = { otp -> authViewModel.verifyOtp(otp) },
                            onResendOtp = { phone -> authViewModel.sendOtp(this@MainActivity, phone) },
                            onBack = { authViewModel.backToPhoneEntry() },
                            onClearError = { authViewModel.clearError() }
                        )
                    } else {
                        PhoneLoginScreen(
                            state = authState,
                            onSendOtp = { phone -> authViewModel.sendOtp(this@MainActivity, phone) },
                            onClearError = { authViewModel.clearError() }
                        )
                    }
                } else if (!hasDisplayName) {
                    NameSetupScreen(
                        isLoading = authState.isLoading,
                        error = authState.error,
                        onSaveName = { name -> authViewModel.updateDisplayName(name) },
                        onClearError = { authViewModel.clearError() }
                    )
                } else {
                    AppNavGraph(
                        onMicClick = { toggleListening() },
                        onCancelRecording = { cancelListening() },
                        userName = currentUser?.displayName ?: "User",
                        onSignOut = { authViewModel.signOut() },
                        onUpdateName = { name -> authViewModel.updateDisplayName(name) }
                    )
                }
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

                    viewModel.insertRecord(entity)
                }

                stopRecording()
            }

            override fun onError(error: Int) {
                Log.d("SpeechRecognizer", "Error code: $error")
                when (error) {
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        // Recognizer is busy — cancel first, then retry after a short delay
                        speechRecognizer.cancel()
                        isListening = false
                        viewModel.viewModelScope.launch {
                            delay(300)
                            startListening()
                        }
                    }
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        // No speech detected — just stop cleanly
                        stopRecording()
                    }
                    else -> {
                        stopRecording()
                    }
                }
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

    private fun toggleListening() {
        if (isListening) {
            // Stop current session and restart
            speechRecognizer.cancel()
            stopRecording()
            // Small delay to let the recognizer fully release before restarting
            viewModel.viewModelScope.launch {
                delay(200)
                startListening()
            }
        } else {
            startListening()
        }
    }

    private fun cancelListening() {
        speechRecognizer.cancel()
        stopRecording()
        viewModel.setSpokenText("")
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }

        isListening = true
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
        isListening = false
        viewModel.setIsRecording(false)
        timerJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
