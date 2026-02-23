package com.example.speak2do.calendar

import com.example.speak2do.network.gemini.ExtractedTask
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class DeadlineExtractionInput(
    val currentDate: String,
    val transcript: String,
    val extractedTask: ExtractedTask
)

data class ParsedDeadline(
    val title: String,
    val description: String,
    val startMillis: Long,
    val endMillis: Long,
    val timezone: String,
    val reminderMinutes: Int = 30
)

interface VoiceDeadlineParser {
    fun parse(input: DeadlineExtractionInput): ParsedDeadline?
}

class GeminiDeadlineParser(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : VoiceDeadlineParser {
    private val timeOnlyFormats = listOf(
        DateTimeFormatter.ofPattern("H:mm", Locale.US),
        DateTimeFormatter.ofPattern("HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("h a", Locale.US)
    )

    override fun parse(input: DeadlineExtractionInput): ParsedDeadline? {
        val task = input.extractedTask
        val dateTimeText = task.date_time?.trim().orEmpty()
        if (dateTimeText.isBlank()) return null

        val startMillis = parseDateTimeMillis(dateTimeText, input.currentDate) ?: return null
        val endMillis = startMillis + 30 * 60 * 1000L
        val title = task.task_title?.takeIf { it.isNotBlank() } ?: fallbackTitle(input.transcript)
        val description = task.description.ifBlank { input.transcript }

        return ParsedDeadline(
            title = title,
            description = description,
            startMillis = startMillis,
            endMillis = endMillis,
            timezone = zoneId.id
        )
    }

    private fun fallbackTitle(transcript: String): String {
        val trimmed = transcript.trim()
        return if (trimmed.length <= 50) trimmed else "${trimmed.take(47)}..."
    }

    private fun parseDateTimeMillis(raw: String, currentDate: String): Long? {
        runCatching {
            return OffsetDateTime.parse(raw).toInstant().toEpochMilli()
        }
        runCatching {
            return LocalDateTime.parse(raw).atZone(zoneId).toInstant().toEpochMilli()
        }
        runCatching {
            return LocalDate.parse(raw).atTime(LocalTime.of(9, 0)).atZone(zoneId).toInstant().toEpochMilli()
        }

        val baseDate = runCatching { LocalDate.parse(currentDate) }.getOrNull() ?: return null
        for (formatter in timeOnlyFormats) {
            try {
                val parsedTime = LocalTime.parse(raw, formatter)
                return baseDate.atTime(parsedTime).atZone(zoneId).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }
}
