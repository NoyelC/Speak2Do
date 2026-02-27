package com.example.speak2do

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.lifecycleScope
import com.example.speak2do.calendar.CalendarIntegrationManager
import com.example.speak2do.calendar.CalendarDayEvent
import com.example.speak2do.calendar.DeadlineExtractionInput
import com.example.speak2do.calendar.GeminiDeadlineParser
import com.example.speak2do.calendar.ParsedDeadline
import com.example.speak2do.calendar.VoiceDeadlineParser
import com.example.speak2do.auth.AuthViewModel
import com.example.speak2do.auth.NameSetupScreen
import com.example.speak2do.auth.OtpVerificationScreen
import com.example.speak2do.auth.PhoneLoginScreen
import com.example.speak2do.data.VoiceRecordEntity
import com.example.speak2do.navigation.AppNavGraph
import com.example.speak2do.reminder.DeadlineReminderScheduler
import com.example.speak2do.ui.theme.AppThemeState
import com.example.speak2do.ui.theme.Speak2DoTheme
import com.example.speak2do.util.TaskCategorizer
import com.example.speak2do.util.formatTime
import com.example.speak2do.network.gemini.Gemini
import com.example.speak2do.network.gemini.GeminiRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private var timerJob: Job? = null
    private lateinit var appPrefs: android.content.SharedPreferences
    private lateinit var calendarPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val calendarIntegrationManager by lazy { CalendarIntegrationManager(this) }
    private val deadlineParser: VoiceDeadlineParser = GeminiDeadlineParser()
    private var pendingDeadlineInput: DeadlineExtractionInput? = null
    private var pendingDeviceCalendarDeadline: ParsedDeadline? = null
    private var lastCalendarEventSignature: String? = null
    private var profileImageUri by mutableStateOf<Uri?>(null)
    private var lastLoadedProfileUserId: String? = null
    private val viewModel: VoiceRecordViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val mainScreenViewModel: MainScreenViewModel by viewModels {
        val apiKey = GeminiApiKeyProvider.getGeminiApiKey()
        val repo = GeminiRepository(Gemini.create(apiKey))
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainScreenViewModel(repo) as T
            }
        }
    }
    private var isListening = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // Handle permission denied
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    private val pickProfileImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                runCatching {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                profileImageUri = uri
                val uid = authViewModel.authState.value.user?.uid
                if (!uid.isNullOrBlank()) {
                    lifecycleScope.launch {
                        uploadProfileImage(uid, uri)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        DeadlineReminderScheduler.createNotificationChannel(this)
        appPrefs = getSharedPreferences("speak2do_prefs", MODE_PRIVATE)
        AppThemeState.updateDarkMode(appPrefs.getBoolean("dark_mode", true))
        calendarPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val readGranted = result[Manifest.permission.READ_CALENDAR] == true
                val writeGranted = result[Manifest.permission.WRITE_CALENDAR] == true
                val pending = pendingDeadlineInput
                pendingDeadlineInput = null
                val pendingDevice = pendingDeviceCalendarDeadline
                pendingDeviceCalendarDeadline = null
                if (readGranted && writeGranted && pending != null) {
                    createCalendarEventForInput(pending)
                } else if (readGranted && writeGranted && pendingDevice != null) {
                    createCalendarEvent(
                        deadline = pendingDevice,
                        preferLocalCalendar = true
                    )
                } else if (!(readGranted && writeGranted)) {
                    Toast.makeText(
                        this,
                        "Calendar permission denied, deadline not added",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        maybeRequestNotificationPermission()

        enableEdgeToEdge()

        setContent {
            Crossfade(
                targetState = AppThemeState.isDarkMode,
                animationSpec = tween(durationMillis = 450),
                label = "themeCrossfade"
            ) {
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
                            onUpdateName = { name -> authViewModel.updateDisplayName(name) },
                            isDarkMode = AppThemeState.isDarkMode,
                            onDarkModeChange = { enabled ->
                                AppThemeState.updateDarkMode(enabled)
                                appPrefs.edit().putBoolean("dark_mode", enabled).apply()
                            },
                            profileImageUri = profileImageUri,
                            onPickProfileImage = {
                                pickProfileImageLauncher.launch(arrayOf("image/*"))
                            },
                            onRemoveProfileImage = {
                                val uid = authViewModel.authState.value.user?.uid
                                if (!uid.isNullOrBlank()) {
                                    lifecycleScope.launch { removeProfileImage(uid) }
                                } else {
                                    profileImageUri = null
                                }
                            },
                            onSyncEventToDeviceCalendar = { date, title, time, notes ->
                                syncTaskEventToDeviceCalendar(date, title, time, notes)
                            },
                            onGetDeviceCalendarEventsForDay = { date ->
                                getDeviceCalendarEventsForDay(date)
                            },
                            onAddNoteToDeviceCalendarEvent = { eventId, note ->
                                addNoteToDeviceCalendarEvent(eventId, note)
                            }
                        )
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            authViewModel.authState.collect { state ->
                val uid = state.user?.uid
                if (uid.isNullOrBlank()) {
                    lastLoadedProfileUserId = null
                    profileImageUri = null
                } else if (lastLoadedProfileUserId != uid) {
                    lastLoadedProfileUserId = uid
                    val local = appPrefs.getString("profile_image_uri_$uid", null)
                    profileImageUri = local?.let { Uri.parse(it) }
                    val remote = fetchRemoteProfileImage(uid)
                    if (remote != null) {
                        profileImageUri = remote
                        appPrefs.edit().putString("profile_image_uri_$uid", remote.toString()).apply()
                    }
                }
            }
        }

        setupSpeechRecognizer()
        lifecycleScope.launchWhenStarted {
            mainScreenViewModel.geminiResponse.collect { result ->
                if (result.isNotBlank()) {
                    Toast.makeText(this@MainActivity, "Gemini: $result", Toast.LENGTH_LONG).show()
                    Log.d("Gemini", "Result: $result")
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            mainScreenViewModel.error.collect { err ->
                if (!err.isNullOrBlank()) {
                    Toast.makeText(this@MainActivity, "Gemini error: $err", Toast.LENGTH_LONG).show()
                    Log.e("Gemini", "Error: $err")
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            mainScreenViewModel.deadlineExtractionInput.collect { input ->
                handleCalendarDeadline(input)
            }
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.getOrNull(0) ?: ""

                viewModel.setSpokenText(text)
                if (text.isNotEmpty()) {
                    val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val user = authViewModel.authState.value.user
                    val userId = user?.uid
                    mainScreenViewModel.onVoiceResult(
                        currentDate = currentDate,
                        transcript = text,
                        userId = userId,
                        userDisplayName = user?.displayName,
                        userPhone = user?.phoneNumber,
                        userEmail = user?.email
                    )
                }

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
                        category = TaskCategorizer.categorize(text),
                        progress = 1f
                    )

                    viewModel.insertRecord(entity)
                    Toast.makeText(this@MainActivity, "\u2714 Task added successfully", Toast.LENGTH_SHORT).show()
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
            override fun onRmsChanged(rmsdB: Float) {
                viewModel.setVoiceLevel(rmsdB)
            }
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
        mainScreenViewModel.onVoiceStart()
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

    private fun handleCalendarDeadline(input: DeadlineExtractionInput) {
        val signature = "${input.transcript}|${input.extractedTask.task_title}|${input.extractedTask.date_time}"
        if (signature == lastCalendarEventSignature) return
        createCalendarEventForInput(input)
    }

    private fun createCalendarEventForInput(input: DeadlineExtractionInput) {
        val parsedDeadline = deadlineParser.parse(input) ?: return
        val signature = "${input.transcript}|${input.extractedTask.task_title}|${input.extractedTask.date_time}"
        val eventDateTime = Instant.ofEpochMilli(parsedDeadline.startMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val displayTime = eventDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        val fullDateTime = eventDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy | hh:mm a"))
        val content = if (parsedDeadline.description.isNotBlank()) {
            "${parsedDeadline.title} - ${parsedDeadline.description}"
        } else {
            parsedDeadline.title
        }
        viewModel.insertRecord(
            VoiceRecordEntity(
                text = content,
                dateTime = displayTime,
                fullDateTime = fullDateTime,
                duration = "EVENT",
                category = TaskCategorizer.categorize(content, "EVENT"),
                progress = 1f,
                createdAt = parsedDeadline.startMillis
            )
        )
        lastCalendarEventSignature = signature

        if (hasCalendarPermissions()) {
            createCalendarEvent(
                deadline = parsedDeadline,
                preferLocalCalendar = true,
                onSuccess = { eventId ->
                    Toast.makeText(
                        this@MainActivity,
                        "Deadline saved in app and calendar (event #$eventId)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            Toast.makeText(
                this@MainActivity,
                "Deadline saved in app reminders",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createCalendarEvent(
        deadline: ParsedDeadline,
        preferLocalCalendar: Boolean,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                calendarIntegrationManager.createDeadlineEvent(
                    deadline = deadline,
                    preferLocalCalendar = preferLocalCalendar
                )
            }
            result.onSuccess { eventId ->
                onSuccess?.invoke(eventId)
            }.onFailure { error ->
                Log.e("CalendarIntegration", "Failed to create event", error)
                Toast.makeText(

                    this@MainActivity,
                    "Failed to add deadline: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun syncTaskEventToDeviceCalendar(
        date: LocalDate,
        title: String,
        time: String,
        notes: String
    ) {
        val localTime = try {
            LocalTime.parse(time)
        } catch (_: Exception) {
            LocalTime.of(9, 0)
        }
        val startMillis = date
            .atTime(localTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val deadline = ParsedDeadline(
            title = title,
            description = notes,
            startMillis = startMillis,
            endMillis = startMillis + 30 * 60 * 1000L,
            timezone = ZoneId.systemDefault().id,
            reminderMinutes = 30
        )

        if (!hasCalendarPermissions()) {
            pendingDeviceCalendarDeadline = deadline
            calendarPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
            return
        }

        createCalendarEvent(
            deadline = deadline,
            preferLocalCalendar = true,
            onSuccess = { eventId ->
                Toast.makeText(
                    this,
                    "Synced to your calendar (event #$eventId)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun hasCalendarPermissions(): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        return readGranted && writeGranted && calendarIntegrationManager.hasCalendarPermissions()
    }

    private suspend fun getDeviceCalendarEventsForDay(
        date: LocalDate
    ): Result<List<CalendarDayEvent>> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermissions()) {
            Result.failure(IllegalStateException("Calendar permission not granted"))
        } else {
            calendarIntegrationManager.getEventsForDay(date)
        }
    }

    private suspend fun addNoteToDeviceCalendarEvent(
        eventId: Long,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermissions()) {
            Result.failure(IllegalStateException("Calendar permission not granted"))
        } else {
            calendarIntegrationManager.addNoteToEvent(eventId, note)
        }
    }

    private suspend fun uploadProfileImage(uid: String, localUri: Uri) {
        runCatching {
            val ref = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
            contentResolver.openInputStream(localUri)?.use { stream ->
                ref.putStream(stream).await()
            } ?: throw IllegalStateException("Unable to read selected image")
            val remote = ref.downloadUrl.await().toString()
            profileImageUri = Uri.parse(remote)
            appPrefs.edit().putString("profile_image_uri_$uid", remote).apply()
        }.onFailure { throwable ->
            Log.e("ProfileImage", "Upload failed", throwable)
            appPrefs.edit().putString("profile_image_uri_$uid", localUri.toString()).apply()
        }
    }

    private suspend fun fetchRemoteProfileImage(uid: String): Uri? {
        return runCatching {
            val ref = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
            Uri.parse(ref.downloadUrl.await().toString())
        }.getOrNull()
    }

    private suspend fun removeProfileImage(uid: String) {
        runCatching {
            FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg").delete().await()
        }.onFailure {
            Log.w("ProfileImage", "Remote photo delete skipped: ${it.message}")
        }
        appPrefs.edit().remove("profile_image_uri_$uid").apply()
        profileImageUri = null
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    private fun maybeRequestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
