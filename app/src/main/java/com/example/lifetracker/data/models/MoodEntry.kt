package com.example.lifetracker.data.models

import java.util.*

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String = "😊",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val dateObj = Date(date)
        val today = Date()

        return when {
            isSameDay(dateObj, today) -> "Today"
            isSameDay(dateObj, Date(today.time - 24 * 60 * 60 * 1000)) -> "Yesterday"
            else -> android.text.format.DateFormat.format("MMM dd", dateObj).toString()
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
}