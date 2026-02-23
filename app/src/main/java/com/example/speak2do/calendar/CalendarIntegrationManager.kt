package com.example.speak2do.calendar

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.ZoneId

data class CalendarDayEvent(
    val eventId: Long,
    val title: String,
    val description: String,
    val startMillis: Long,
    val endMillis: Long
)

class CalendarIntegrationManager(
    private val context: Context
) {
    fun hasCalendarPermissions(): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        return readGranted && writeGranted
    }

    @SuppressLint("MissingPermission")
    fun createDeadlineEvent(
        deadline: ParsedDeadline,
        preferLocalCalendar: Boolean = true
    ): Result<Long> = runCatching {
        check(hasCalendarPermissions()) { "Calendar permission not granted" }
        val calendarId = findBestCalendarId(preferLocalCalendar)
            ?: error("No writable calendar found on this device")

        val eventValues = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, deadline.title)
            put(CalendarContract.Events.DESCRIPTION, deadline.description)
            put(CalendarContract.Events.DTSTART, deadline.startMillis)
            put(CalendarContract.Events.DTEND, deadline.endMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, deadline.timezone)
            // Keep reminder ownership inside this app (WorkManager-based),
            // so external calendar apps do not trigger duplicate alerts.
            put(CalendarContract.Events.HAS_ALARM, 0)
        }

        val eventUri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)
            ?: error("Failed to insert calendar event")
        val eventId = android.content.ContentUris.parseId(eventUri)
        context.contentResolver.delete(
            CalendarContract.Reminders.CONTENT_URI,
            "${CalendarContract.Reminders.EVENT_ID}=?",
            arrayOf(eventId.toString())
        )

        eventId
    }

    @SuppressLint("MissingPermission")
    private fun findBestCalendarId(preferLocalCalendar: Boolean): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )
        val selection = "${CalendarContract.Calendars.VISIBLE}=1 AND ${CalendarContract.Calendars.SYNC_EVENTS}=1"

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            var fallbackId: Long? = null
            var localPrimaryId: Long? = null
            var googlePrimaryId: Long? = null
            var fallbackGoogleId: Long? = null
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val isPrimary = cursor.getInt(1) == 1
                val accountType = cursor.getString(2).orEmpty()
                val accessLevel = cursor.getInt(3)
                val isWritable = accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR
                val isGoogleAccount = accountType.contains("google", ignoreCase = true)

                if (fallbackId == null && accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    fallbackId = id
                }
                if (fallbackGoogleId == null && isWritable && isGoogleAccount) {
                    fallbackGoogleId = id
                }
                if (isPrimary && isWritable) {
                    if (isGoogleAccount) {
                        if (googlePrimaryId == null) googlePrimaryId = id
                    } else {
                        if (localPrimaryId == null) localPrimaryId = id
                    }
                }
            }
            return if (preferLocalCalendar) {
                localPrimaryId ?: fallbackId ?: googlePrimaryId ?: fallbackGoogleId
            } else {
                googlePrimaryId ?: fallbackGoogleId ?: fallbackId ?: localPrimaryId
            }
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun getEventsForDay(date: LocalDate): Result<List<CalendarDayEvent>> = runCatching {
        check(hasCalendarPermissions()) { "Calendar permission not granted" }

        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.DESCRIPTION
        )
        val selection = "${CalendarContract.Instances.VISIBLE}=1"

        val events = mutableListOf<CalendarDayEvent>()
        context.contentResolver.query(
            builder.build(),
            projection,
            selection,
            null,
            "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val title = cursor.getString(1).orEmpty().ifBlank { "Untitled event" }
                val begin = cursor.getLong(2)
                val end = cursor.getLong(3)
                val description = cursor.getString(4).orEmpty()
                events += CalendarDayEvent(
                    eventId = id,
                    title = title,
                    description = description,
                    startMillis = begin,
                    endMillis = end
                )
            }
        }
        events
    }

    @SuppressLint("MissingPermission")
    fun addNoteToEvent(eventId: Long, note: String): Result<Unit> = runCatching {
        check(hasCalendarPermissions()) { "Calendar permission not granted" }
        val cleanedNote = note.trim()
        check(cleanedNote.isNotEmpty()) { "Note cannot be empty" }

        val existingDescription = context.contentResolver.query(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
            arrayOf(CalendarContract.Events.DESCRIPTION),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0).orEmpty() else ""
        }.orEmpty()

        val updatedDescription = if (existingDescription.isBlank()) {
            cleanedNote
        } else {
            "$existingDescription\n$cleanedNote"
        }

        val updatedRows = context.contentResolver.update(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
            ContentValues().apply {
                put(CalendarContract.Events.DESCRIPTION, updatedDescription)
            },
            null,
            null
        )
        check(updatedRows > 0) { "Failed to update event note" }
    }
}
