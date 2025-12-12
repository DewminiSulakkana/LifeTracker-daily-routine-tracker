package com.example.lifetracker.data.pref

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.lifetracker.data.models.Habit
import com.example.lifetracker.data.models.MoodEntry
import com.example.lifetracker.data.models.WaterEntry
import com.example.lifetracker.widgets.LifeTrackerWidget
//import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class PrefsHelper(private val context: Context) {

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("LifeTrackerPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS = "habits"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_CURRENT_DATE = "current_date"
        private const val KEY_MOOD_ENTRIES = "mood_entries"// For day tracking
        private const val KEY_HYDRATION_REMINDER_ENABLED = "hydration_reminder_enabled"
        private const val KEY_HYDRATION_REMINDER_INTERVAL = "hydration_reminder_interval"
        private const val KEY_WATER_ENTRIES = "water_entries"
        private const val KEY_DAILY_GOAL = "daily_goal"

        //widget update
        private const val ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE"

    }

    // Save habits list
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        sharedPreferences.edit().putString(KEY_HABITS, json).apply()
    }

    // Load habits list
    fun loadHabits(): List<Habit> {
        val json = sharedPreferences.getString(KEY_HABITS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Save single habit
    fun saveHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        val existingIndex = habits.indexOfFirst { it.id == habit.id }

        if (existingIndex != -1) {
            habits[existingIndex] = habit
        } else {
            habits.add(habit)
        }

        saveHabits(habits)
        updateWidget(context)
    }

    // Delete habit
    fun deleteHabit(habitId: String) {
        val habits = loadHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
        updateWidget(context)
    }

    // Get habit by ID
    fun getHabitById(habitId: String): Habit? {
        return loadHabits().find { it.id == habitId }
    }

    fun saveMoodEntry(moodEntry: MoodEntry) {
        val entries = loadMoodEntries().toMutableList()
        entries.add(0, moodEntry) // Add to beginning for reverse chronological order
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString(KEY_MOOD_ENTRIES, json).apply()
        updateWidget(context)
    }

    fun loadMoodEntries(): List<MoodEntry> {
        val json = sharedPreferences.getString(KEY_MOOD_ENTRIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteMoodEntry(entryId: String) {
        val entries = loadMoodEntries().toMutableList()
        entries.removeAll { it.id == entryId }
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }

    //HydrationReminder
    fun setHydrationReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HYDRATION_REMINDER_ENABLED, enabled).apply()
    }

    fun isHydrationReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_HYDRATION_REMINDER_ENABLED, false)
    }

    fun setHydrationReminderInterval(hours: Int) {
        sharedPreferences.edit().putInt(KEY_HYDRATION_REMINDER_INTERVAL, hours).apply()
    }

    fun getHydrationReminderInterval(): Int {
        return sharedPreferences.getInt(KEY_HYDRATION_REMINDER_INTERVAL, 2) // Default 2 hours
    }

    fun saveWaterEntry(waterEntry: WaterEntry) {
        val entries = loadWaterEntries().toMutableList()
        entries.add(waterEntry)
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString(KEY_WATER_ENTRIES, json).apply()
        updateWidget(context)
    }

    fun loadWaterEntries(): List<WaterEntry> {
        val json = sharedPreferences.getString(KEY_WATER_ENTRIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<WaterEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getDailyGoal(): Int {
        return sharedPreferences.getInt(KEY_DAILY_GOAL, 2000) // Default 2000ml
    }

    fun setDailyGoal(goal: Int) {
        sharedPreferences.edit().putInt(KEY_DAILY_GOAL, goal).apply()
    }

    fun getTodayWaterEntries(): List<WaterEntry> {
        val allEntries = loadWaterEntries()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return allEntries.filter { entry ->
            val entryDate = Calendar.getInstance().apply { timeInMillis = entry.time }
            val todayDate = Calendar.getInstance().apply { timeInMillis = today }
            entryDate.get(Calendar.YEAR) == todayDate.get(Calendar.YEAR) &&
                    entryDate.get(Calendar.MONTH) == todayDate.get(Calendar.MONTH) &&
                    entryDate.get(Calendar.DAY_OF_MONTH) == todayDate.get(Calendar.DAY_OF_MONTH)
        }
    }

    fun getTodayTotalWater(): Int {
        return getTodayWaterEntries().sumOf { it.amount }
    }

    fun deleteWaterEntry(entryId: String) {
        val entries = loadWaterEntries().toMutableList()
        entries.removeAll { it.id == entryId }
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString(KEY_WATER_ENTRIES, json).apply()
    }

    //update widget
    private fun updateWidget(context: Context) {
        val intent = Intent(context, LifeTrackerWidget::class.java)
        intent.action = ACTION_APPWIDGET_UPDATE
        context.sendBroadcast(intent)
    }

}