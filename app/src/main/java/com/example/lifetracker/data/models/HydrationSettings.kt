package com.example.lifetracker.data.models

data class HydrationSettings(
    val isEnabled: Boolean = false,
    val reminderInterval: Int = 2, // minutes
    val dailyGoal: Int = 2000, // ml
    val startTime: String = "08:00", // 24-hour format
    val endTime: String = "22:00",
    val lastDrinkTime: Long = 0L,
    val todayIntake: Int = 0 // ml drank today
) {
    fun shouldShowReminder(currentTime: Long): Boolean {
        if (!isEnabled) return false
        // Simple implementation - always show if enabled
        // We can enhance this later with time checks
        return true
    }

    fun logWater(amount: Int = 250): HydrationSettings {
        return this.copy(
            todayIntake = todayIntake + amount,
            lastDrinkTime = System.currentTimeMillis()
        )
    }

    fun getProgressPercentage(): Int {
        return if (dailyGoal > 0) {
            (todayIntake * 100) / dailyGoal
        } else {
            0
        }
    }

    fun resetDailyIntake(): HydrationSettings {
        return this.copy(todayIntake = 0)
    }

    fun isGoalReached(): Boolean {
        return todayIntake >= dailyGoal
    }
}