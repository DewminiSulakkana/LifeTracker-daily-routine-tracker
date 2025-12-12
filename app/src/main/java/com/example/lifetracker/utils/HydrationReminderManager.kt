package com.example.lifetracker.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lifetracker.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

class HydrationReminderManager {

    companion object {
        private const val WORK_NAME = "hydration_reminder_work"

        fun scheduleReminder(context: Context, intervalHours: Long = 2) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .build()

            val reminderWorkRequest: PeriodicWorkRequest =
                PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                    intervalHours, TimeUnit.HOURS,
                    15, TimeUnit.MINUTES // Flexible interval
                )
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                reminderWorkRequest
            )
        }

        fun cancelReminder(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun updateReminderInterval(context: Context, intervalHours: Long) {
            cancelReminder(context)
            scheduleReminder(context, intervalHours)
        }
    }
}