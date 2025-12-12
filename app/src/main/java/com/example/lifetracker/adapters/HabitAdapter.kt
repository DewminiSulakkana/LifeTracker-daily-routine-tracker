package com.example.lifetracker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lifetracker.R
import com.example.lifetracker.data.models.Habit

class HabitAdapter(
    private var habits: List<Habit>,
    private val onHabitClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // SAFE: Initialize with null checks
        private val colorHeader: View? = itemView.findViewById(R.id.color_header)
        private val textAlarmIcon: TextView? = itemView.findViewById(R.id.text_alarm_icon)
        private val textTime: TextView? = itemView.findViewById(R.id.text_time)
        private val textHabitIcon: TextView? = itemView.findViewById(R.id.text_habit_icon)
        private val textHabitName: TextView? = itemView.findViewById(R.id.text_habit_name)
        private val textCompletionStatus: TextView? = itemView.findViewById(R.id.text_completion_status)
        private val textAlarmStatus: TextView? = itemView.findViewById(R.id.text_alarm_status)
        private val progressBar: ProgressBar? = itemView.findViewById(R.id.progress_bar)
        private val btnDelete: ImageButton? = itemView.findViewById(R.id.btn_delete)

        fun bind(habit: Habit) {
            try {
                // SAFE: Set card color header with null check
                colorHeader?.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.accent)
                )

                // SAFE: Set alarm icon and time
                textAlarmIcon?.text = "🔔" // Simple fix for now
                textTime?.text = habit.getTimeForDisplay()

                // SAFE: Set habit icon and name
                textHabitIcon?.text = habit.icon
                textHabitName?.text = habit.name

                // SAFE: Set completion status
                textCompletionStatus?.text = if (habit.completed) "✅ Completed" else "☐ Pending"

                // SAFE: Set alarm status
                textAlarmStatus?.text = if (habit.hasReminder) "Alarm: On" else "Alarm: Off"

                // SAFE: Update progress bar
                progressBar?.max = habit.targetCount
                progressBar?.progress = habit.currentCount

                // SAFE: Set click listeners
                itemView.setOnClickListener {
                    onHabitClick(habit)
                }

                textCompletionStatus?.setOnClickListener {
                    onHabitClick(habit)
                }

                btnDelete?.setOnClickListener {
                    onDeleteClick(habit)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // At least show the habit name if everything else fails
                textHabitName?.text = habit.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        try {
            holder.bind(habits[position])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}