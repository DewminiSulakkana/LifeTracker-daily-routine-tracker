package com.example.lifetracker.data.models

import android.graphics.Color
import java.util.*
import androidx.core.graphics.toColorInt

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var icon: String = "💧",
    var targetCount: Int = 5,
    var currentCount: Int = 0,
    var completed: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),

    // TIMELINE & ALARMS
    var scheduledTime: String? = null,        // "08:00" format
    var hasReminder: Boolean = false,         // Whether to show notifications
    var category: String = "General",         // Morning, Health, Work, Evening
    var colorCategory: Int = 0,               // For color coding
    var daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Sunday, 7=Saturday

    // NEW: Card color system - FIXED: Renamed to avoid clash
    var customCardColor: String = "#FF6200EE", // ← CHANGED FROM cardColor to customCardColor

    // Daily progress tracking
    var dailyProgress: MutableMap<String, Int> = mutableMapOf(), // "2024-11-12" -> 3
) {
    // Existing functions remain the same
    fun getProgress(): Float {
        return if (targetCount > 0) {
            currentCount.toFloat() / targetCount.toFloat()
        } else {
            0f
        }
    }

    fun getProgressPercentage(): Int {
        return (getProgress() * 100).toInt()
    }

    fun incrementProgress() {
        if (currentCount < targetCount) {
            currentCount++
            updatedAt = System.currentTimeMillis()
        }
        if (currentCount >= targetCount) {
            completed = true
        }
    }

    fun decrementProgress() {
        if (currentCount > 0) {
            currentCount--
            updatedAt = System.currentTimeMillis()
        }
        completed = false
    }

    fun resetProgress() {
        currentCount = 0
        completed = false
        updatedAt = System.currentTimeMillis()
    }

    // ENHANCED FUNCTIONS FOR CARD DESIGN
    fun getTimeForDisplay(): String {
        return scheduledTime ?: "Anytime"
    }

    fun getAlarmStatus(): String {
        return if (hasReminder && scheduledTime != null) "🔔" else "🔕"
    }

    fun getCompletionStatus(): String {
        return if (completed) "✅ Completed" else "☐ Pending"
    }

    // FIXED: Renamed function to avoid name clash
    fun getDisplayCardColor(): String {
        // Use custom card color if set, otherwise fallback to category colors
        return if (customCardColor.isNotEmpty() && customCardColor != "#FF6200EE") {
            customCardColor
        } else {
            getCategoryColorHex()
        }
    }

    private fun getCategoryColorHex(): String {
        return when (category) {
            "Morning" -> "#FFFFF9C4"  // Light Yellow
            "Health" -> "#FFC8E6C9"   // Light Green
            "Work" -> "#FFE3F2FD"     // Light Blue
            "Evening" -> "#FFE1BEE7"  // Light Purple
            else -> "#FFF5F5F5"       // Light Gray
        }
    }

    private fun isValidColor(colorHex: String): Boolean {
        return try {
            colorHex.toColorInt()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCategoryIcon(): String {
        return when (category) {
            "Morning" -> "☀️"
            "Health" -> "💪"
            "Work" -> "💼"
            "Evening" -> "🌙"
            else -> "⏰"
        }
    }

    // Check if habit is scheduled for a specific day (1=Sunday, 7=Saturday)
    fun isScheduledForDay(dayOfWeek: Int): Boolean {
        return daysOfWeek.contains(dayOfWeek)
    }

    // NEW: Toggle completion for card-based interaction
    fun toggleCompletion() {
        completed = !completed
        if (completed && currentCount < targetCount) {
            currentCount = targetCount // Complete all progress when toggled
        } else if (!completed) {
            currentCount = 0 // Reset progress when un-toggled
        }
        updatedAt = System.currentTimeMillis()
    }
}