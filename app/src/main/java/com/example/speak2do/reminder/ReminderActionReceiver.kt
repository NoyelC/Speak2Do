package com.example.speak2do.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.speak2do.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(DeadlineReminderScheduler.EXTRA_TASK_ID_PUBLIC, -1L)
        if (taskId < 0L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                when (intent.action) {
                    DeadlineReminderScheduler.ACTION_ACK_PUBLIC -> {
                        db.notificationHistoryDao().markReadByTaskId(taskId)
                    }

                    DeadlineReminderScheduler.ACTION_MUTE_PUBLIC -> {
                        db.notificationHistoryDao().markReadByTaskId(taskId)
                        DeadlineReminderScheduler.muteTask(context.applicationContext, taskId)
                        DeadlineReminderScheduler.cancelReminder(context.applicationContext, taskId)
                    }
                }
            } finally {
                NotificationManagerCompat.from(context).cancel(taskId.toInt())
                pendingResult.finish()
            }
        }
    }
}
