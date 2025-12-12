package com.example.lifetracker.data.models

import java.util.*

data class WaterEntry(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "💧", // 💧, ☕, 🥛, 🍵, 🥤
    val amount: Int, // in ml
    val time: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        return android.text.format.DateFormat.format("HH:mm", Date(time)).toString()
    }

    fun getDrinkName(): String {
        return when (type) {
            "💧" -> "Water"
            "☕" -> "Coffee"
            "🥛" -> "Milk"
            "🍵" -> "Tea"
            "🥤" -> "Juice"
            "🏃" -> "Sports Drink"
            else -> "Drink"
        }
    }

    fun getDrinkColor(): Long {
        return when (type) {
            "💧" -> 0xFF2196F3 // Blue
            "☕" -> 0xFF795548 // Brown
            "🥛" -> 0xFFFFF9C4 // Light Yellow
            "🍵" -> 0xFF4CAF50 // Green
            "🥤" -> 0xFFFF9800 // Orange
            else -> 0xFF9E9E9E // Gray
        }
    }
}