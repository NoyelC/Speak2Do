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
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.example.speak2do.data.VoiceRecordEntity
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryPurple
import com.example.speak2do.ui.theme.SecondaryIndigo
import com.example.speak2do.ui.theme.Speak2DoTheme
import com.example.speak2do.ui.theme.WhiteText
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

data class RecordingItem(
    val id: Long,
    val text: String,
    val dateTime: String,
    val duration: String,
    val progress: Float,
    val isCompleted: Boolean = false
)

data class VoiceRecord(
    val text: String,
    val dateTime: String,
    val fullDateTime: String,
    val duration: String,
    val progress: Float
)


@Composable
fun MainScreen(
    spokenText: String,
    isRecording: Boolean,
    recordingTime: Int,
    recordings: List<RecordingItem>,
    voiceRecords: List<VoiceRecord>,
    onMicClick: () -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit = { _, _ -> }
) {
    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onMicClick,
                containerColor = PrimaryPurple,
                modifier = Modifier.size(64.dp)
            ) {
                Text("🎙️", fontSize = 28.sp)
            }
        }
    ) { padding ->
        var searchQuery by remember { mutableStateOf("") }

        val filteredVoiceRecords = if (searchQuery.isBlank()) voiceRecords
            else voiceRecords.filter { it.text.contains(searchQuery, ignoreCase = true) }

        val filteredRecordings = if (searchQuery.isBlank()) recordings
            else recordings.filter { it.text.contains(searchQuery, ignoreCase = true) }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderSection()
            }

            item {
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            }

            item {
                VoiceAssistantCard(
                    isRecording = isRecording,
                    recordingTime = recordingTime,
                    spokenText = spokenText,
                    onMicClick = onMicClick
                )
            }

            // Voice Records History Section
            if (filteredVoiceRecords.isNotEmpty()) {
                item {
                    Text(
                        "Voice Records",
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(filteredVoiceRecords) { voiceRecord ->
                    VoiceRecordCard(voiceRecord)
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Tasks", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("View Calendar", color = PrimaryPurple, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            items(filteredRecordings) { item ->
                RecordingCard(item = item, onToggleCompleted = onToggleCompleted)
            }

            item {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun VoiceAssistantCard(
    isRecording: Boolean,
    recordingTime: Int,
    spokenText: String,
    onMicClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(SecondaryIndigo, PrimaryPurple)
                ),
                RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(PrimaryPurple, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎙️", fontSize = 32.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text("How can I help?", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "\"Hey Speak2Do, add a meeting...\"",
                    color = WhiteText.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SiriWaveform(isRecording)

        if (isRecording) {
            Text(
                text = "⏱️ ${formatTime(recordingTime)}",
                color = WhiteText,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 14.sp
            )
        }

        if (spokenText.isNotEmpty()) {
            Text(
                text = "\"$spokenText\"",
                color = WhiteText,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    WhiteText.copy(alpha = 0.1f),
                    RoundedCornerShape(16.dp)
                )
                .padding(vertical = 12.dp)
                .clickable { onMicClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRecording) "🎙️  Listening..." else "🎙️  Tap to Speak",
                color = WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun VoiceRecordCard(voiceRecord: VoiceRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Text content
        Text(
            text = voiceRecord.text,
            color = WhiteText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Time and Duration Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🕐 ${voiceRecord.dateTime}",
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = "•",
                color = MutedText,
                fontSize = 11.sp
            )

            Text(
                text = "⏱️ ${voiceRecord.duration}",
                color = PrimaryPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Progress indicator
        LinearProgressIndicator(
            progress = voiceRecord.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp)),
            color = PrimaryPurple,
            trackColor = CardBackground.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun HeaderSection() {
    val currentHour = LocalDateTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting, Noyel",
                fontSize = 24.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = todayDate,
                fontSize = 14.sp,
                color = MutedText
            )
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    Brush.linearGradient(listOf(PrimaryPurple, SecondaryIndigo)),
                    RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                color = WhiteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(color = WhiteText, fontSize = 16.sp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(CardBackground, RoundedCornerShape(26.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text("Search tasks...", color = MutedText, fontSize = 16.sp)
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun RecordingCard(item: RecordingItem, onToggleCompleted: (Long, Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggleCompleted(item.id, item.isCompleted) },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryPurple,
                    uncheckedColor = MutedText,
                    checkmarkColor = WhiteText
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(10.dp))

            // Title Text
            Text(
                text = item.text,
                color = if (item.isCompleted) MutedText else WhiteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        // Date and Duration Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 34.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🕐 ${item.dateTime}",
                color = MutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = "•",
                color = MutedText,
                fontSize = 12.sp
            )

            Text(
                text = item.duration,
                color = PrimaryPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SiriWaveform(
    isRecording: Boolean,
    barCount: Int = 18,
    barWidth: Dp = 6.dp,
    barSpacing: Dp = 4.dp,
    maxBarHeight: Dp = 60.dp,
    minBarHeight: Dp = 12.dp,
    color: Color = Color(0xFF4FC3F7)
) {
    if (!isRecording) return

    val transition = rememberInfiniteTransition(label = "siriWave")

    val bars = List(barCount) { index ->
        transition.animateFloat(
            initialValue = minBarHeight.value,
            targetValue = maxBarHeight.value,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 500 + index * 35,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }

    Row(
        modifier = Modifier
            .height(maxBarHeight)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        bars.forEach { height ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height.value.dp)
                    .background(color, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(barSpacing))
        }
    }
}

fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%02d:%02d".format(min, sec)
}