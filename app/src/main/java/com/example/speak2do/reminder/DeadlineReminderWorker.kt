package com.example.speak2do.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DeadlineReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val payload = DeadlineReminderScheduler.parseWorkerData(inputData) ?: return Result.failure()
        DeadlineReminderScheduler.showNotification(
            context = applicationContext,
            taskId = payload.taskId,
            taskTitle = payload.taskTitle,
            dueAtMillis = payload.dueAtMillis
        )
        return Result.success()
    }
}
