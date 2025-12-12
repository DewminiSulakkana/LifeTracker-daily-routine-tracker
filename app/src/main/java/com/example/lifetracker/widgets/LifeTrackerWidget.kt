package com.example.lifetracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.lifetracker.R
import com.example.lifetracker.data.pref.PrefsHelper
import java.text.SimpleDateFormat
import java.util.*

class LifeTrackerWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget added
    }

    override fun onDisabled(context: Context) {
        // Last widget removed
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefsHelper = PrefsHelper(context)
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Get current data - ONLY INCOMPLETE HABITS
        val allHabits = prefsHelper.loadHabits()
        val incompleteHabits = allHabits.filter { !it.completed }
        val totalHabits = allHabits.size
        val completedHabits = allHabits.count { it.completed }
        val habitProgress = if (totalHabits > 0) {
            (completedHabits.toFloat() / totalHabits * 100).toInt()
        } else {
            0
        }

        val totalWater = prefsHelper.getTodayTotalWater()
        val waterGoal = prefsHelper.getDailyGoal()
        val waterProgress = if (waterGoal > 0) {
            (totalWater.toFloat() / waterGoal * 100).toInt()
        } else {
            0
        }

        val moodEntries = prefsHelper.loadMoodEntries()
        val latestMood = moodEntries.firstOrNull()?.emoji ?: "😊"
        val moodText = when (latestMood) {
            "😊", "😂", "🥰", "😍", "🤩" -> "Happy"
            "😐", "😕", "😔" -> "Neutral"
            "😢", "😭", "😡" -> "Sad"
            else -> "Good"
        }

        // Create date
        val date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())

        // Update basic views
        views.setTextViewText(R.id.widget_date, date)
        views.setProgressBar(R.id.widget_progress_bar, 100, habitProgress, false)
        views.setTextViewText(R.id.widget_progress_text, "$habitProgress% Complete")
        views.setTextViewText(R.id.widget_water_text, "$waterProgress%")
        views.setTextViewText(R.id.widget_mood_text, moodText)
        views.setTextViewText(R.id.widget_pending_count, "${incompleteHabits.size} pending")

        // Clear existing habits
        views.removeAllViews(R.id.widget_habits_container)

        // Add INCOMPLETE habits to the container (max 5 to avoid overflow)
        val habitsToShow = incompleteHabits.take(5)
        habitsToShow.forEachIndexed { index, habit ->
            val habitView = RemoteViews(context.packageName, R.layout.widget_habit_item)

            // Set habit text
            habitView.setTextViewText(R.id.widget_habit_checkbox, "☐")
            habitView.setTextViewText(R.id.widget_habit_text, habit.name)

            // Create click intent for this specific habit
            val completeIntent = Intent(context, LifeTrackerWidget::class.java).apply {
                action = ACTION_COMPLETE_HABIT
                putExtra(EXTRA_HABIT_ID, habit.id)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.id.hashCode(), // Unique request code for each habit
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Set click listener for the entire habit item
            habitView.setOnClickPendingIntent(R.id.widget_habit_checkbox, pendingIntent)
            habitView.setOnClickPendingIntent(android.R.id.background, pendingIntent)

            // Add to container
            views.addView(R.id.widget_habits_container, habitView)
        }

        // Show message if no pending habits
        if (incompleteHabits.isEmpty()) {
            val emptyView = RemoteViews(context.packageName, R.layout.widget_habit_item)
            emptyView.setTextViewText(R.id.widget_habit_checkbox, "🎉")
            emptyView.setTextViewText(R.id.widget_habit_text, "All done! Great job!")
            views.addView(R.id.widget_habits_container, emptyView)
        }

        // Set click intent to open app
        val appIntent = Intent(context, com.example.lifetracker.ui.MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_add_button, appPendingIntent)

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_COMPLETE_HABIT -> {
                val habitId = intent.getStringExtra(EXTRA_HABIT_ID)
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

                if (habitId != null) {
                    completeHabit(context, habitId, appWidgetId)
                }
            }
        }
    }

    private fun completeHabit(context: Context, habitId: String, appWidgetId: Int) {
        val prefsHelper = PrefsHelper(context)
        val habit = prefsHelper.getHabitById(habitId)

        if (habit != null && !habit.completed) {
            // Mark habit as completed
            habit.completed = true
            habit.currentCount = habit.targetCount // Complete all progress
            prefsHelper.saveHabit(habit)

            // Update the widget immediately
            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val ACTION_COMPLETE_HABIT = "COMPLETE_HABIT"
        private const val EXTRA_HABIT_ID = "HABIT_ID"
    }
}