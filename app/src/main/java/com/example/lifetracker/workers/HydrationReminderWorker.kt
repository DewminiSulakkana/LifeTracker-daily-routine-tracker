package com.example.lifetracker.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lifetracker.R
import com.example.lifetracker.data.pref.PrefsHelper
import com.google.gson.Gson

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val prefsHelper = PrefsHelper(applicationContext)

        // Load hydration settings
        val json = prefsHelper.sharedPreferences.getString("hydration_settings", null)
        val settings = if (json != null) {
            Gson().fromJson(json, com.example.lifetracker.data.models.HydrationSettings::class.java)
        } else {
            return Result.success() // No settings configured
        }

        // Check if reminders are enabled
        if (!settings.isEnabled) {
            return Result.success()
        }

        // Show notification
        showHydrationReminder(applicationContext, settings)

        return Result.success()
    }

    private fun showHydrationReminder(context: Context, settings: com.example.lifetracker.data.models.HydrationSettings) {
        val progress = settings.getProgressPercentage()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "hydration_reminders",
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build notification
        val notification = NotificationCompat.Builder(context, "hydration_reminders")
            .setSmallIcon(R.drawable.ic_water_drop) // You can use a different icon
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Progress: ${settings.todayIntake}ml / ${settings.dailyGoal}ml ($progress%)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Stay hydrated for better health! Your body will thank you! 💪\n\n" +
                        "Today's Progress: ${settings.todayIntake}ml / ${settings.dailyGoal}ml ($progress%)\n" +
                        "Next reminder in ${settings.reminderInterval} minutes"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}