package com.example.speak2do.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.speak2do.MainActivity
import com.example.speak2do.R
import com.example.speak2do.data.AppDatabase
import com.example.speak2do.data.NotificationHistoryEntity
import com.example.speak2do.data.VoiceRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DeadlineReminderScheduler {

    private const val CHANNEL_ID = "deadline_reminders"
    private const val CHANNEL_NAME = "Deadline reminders"
    private const val CHANNEL_DESCRIPTION = "Alerts before your task deadlines"
    private const val PREFS_NAME = "reminder_prefs"
    private const val PREF_MUTED_TASK_IDS = "muted_task_ids"
    const val ACTION_ACK_PUBLIC = "com.example.speak2do.reminder.ACTION_ACK"
    const val ACTION_MUTE_PUBLIC = "com.example.speak2do.reminder.ACTION_MUTE"
    const val EXTRA_TASK_ID_PUBLIC = "wm_task_id"
    private const val EXTRA_TASK_ID = EXTRA_TASK_ID_PUBLIC
    private const val EXTRA_TASK_TITLE = "wm_task_title"
    private const val EXTRA_DUE_AT = "wm_due_at"
    private const val REMINDER_LEAD_TIME_MS = 15 * 60 * 1000L

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun scheduleReminder(context: Context, record: VoiceRecordEntity) {
        val dueAtMillis = record.createdAt
        if (isTaskMuted(context, record.id)) return
        val triggerAt = (dueAtMillis - REMINDER_LEAD_TIME_MS)
            .coerceAtLeast(System.currentTimeMillis() + 5_000L)
        if (dueAtMillis <= System.currentTimeMillis()) {
            cancelReminder(context, record.id)
            return
        }

        val delayMs = (triggerAt - System.currentTimeMillis()).coerceAtLeast(1_000L)
        val inputData = Data.Builder()
            .putLong(EXTRA_TASK_ID, record.id)
            .putString(EXTRA_TASK_TITLE, extractTaskTitle(record.text))
            .putLong(EXTRA_DUE_AT, dueAtMillis)
            .build()

        val request = OneTimeWorkRequestBuilder<DeadlineReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(workNameForTask(record.id))
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                workNameForTask(record.id),
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun cancelReminder(context: Context, taskId: Long) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(workNameForTask(taskId))
        NotificationManagerCompat.from(context).cancel(taskId.toInt())
    }

    fun syncReminders(context: Context, records: List<VoiceRecordEntity>) {
        records.forEach { record ->
            if (record.duration.equals("EVENT", ignoreCase = true) &&
                !record.isCompleted &&
                record.createdAt > System.currentTimeMillis()
            ) {
                scheduleReminder(context, record)
            } else {
                cancelReminder(context, record.id)
            }
        }
    }

    fun showNotification(context: Context, taskId: Long, taskTitle: String, dueAtMillis: Long) {
        if (isTaskMuted(context, taskId)) return
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val okIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_ACK_PUBLIC
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val muteIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_MUTE_PUBLIC
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val okPendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId.hashCode() * 31) + 7,
            okIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mutePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId.hashCode() * 31) + 13,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dueAtText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dueAtMillis))
        val body = "Due at $dueAtText. Tap to review."

        // Keep in-app history regardless of OS notification permission state.
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(context.applicationContext)
                .notificationHistoryDao()
                .insert(
                    NotificationHistoryEntity(
                        taskId = taskId,
                        title = taskTitle.ifBlank { "Task deadline approaching" },
                        body = body,
                        dueAtMillis = dueAtMillis
                    )
                )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskTitle.ifBlank { "Task deadline approaching" })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "OK, got it", okPendingIntent)
            .addAction(0, "Don't remind again", mutePendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
    }

    fun parseWorkerData(data: Data): ReminderPayload? {
        val taskId = data.getLong(EXTRA_TASK_ID, -1L)
        if (taskId < 0L) return null
        return ReminderPayload(
            taskId = taskId,
            taskTitle = data.getString(EXTRA_TASK_TITLE).orEmpty(),
            dueAtMillis = data.getLong(EXTRA_DUE_AT, 0L)
        )
    }

    private fun workNameForTask(taskId: Long): String {
        return "deadline_reminder_$taskId"
    }

    fun muteTask(context: Context, taskId: Long) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(PREF_MUTED_TASK_IDS, emptySet()).orEmpty().toMutableSet()
        current += taskId.toString()
        prefs.edit().putStringSet(PREF_MUTED_TASK_IDS, current).apply()
    }

    private fun isTaskMuted(context: Context, taskId: Long): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val muted = prefs.getStringSet(PREF_MUTED_TASK_IDS, emptySet()).orEmpty()
        return taskId.toString() in muted
    }

    private fun extractTaskTitle(rawText: String): String {
        val title = rawText.substringBefore(" - ").trim()
        return if (title.isBlank()) "Task deadline approaching" else title
    }
}

data class ReminderPayload(
    val taskId: Long,
    val taskTitle: String,
    val dueAtMillis: Long
)
