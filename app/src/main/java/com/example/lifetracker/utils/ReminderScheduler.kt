package com.example.lifetracker.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lifetracker.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val HYDRATION_WORK_NAME = "hydration_reminder_work"

    fun scheduleHydrationReminders(context: Context, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)

        workManager.cancelUniqueWork(HYDRATION_WORK_NAME)

        // ALLOW 2-MINUTE INTERVALS FOR AUTO REMINDERS
        val actualInterval = intervalMinutes.coerceAtLeast(2)

        val reminderRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            actualInterval.toLong(), TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            HYDRATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderRequest
        )

        android.util.Log.d("ReminderScheduler", "✅ Scheduled auto reminders every $actualInterval minutes")
    }

    fun cancelHydrationReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(HYDRATION_WORK_NAME)
        android.util.Log.d("ReminderScheduler", "❌ Cancelled all reminders")
    }
}