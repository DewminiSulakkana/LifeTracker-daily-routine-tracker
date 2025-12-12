package com.example.lifetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifetracker.R
import com.example.lifetracker.data.models.MoodEntry

class MoodHistoryAdapter(
    private var moodEntries: List<MoodEntry>,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder>() {

    inner class MoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMoodEmoji: TextView = itemView.findViewById(R.id.text_mood_emoji)
        private val textMoodDate: TextView = itemView.findViewById(R.id.text_mood_date)
        private val textMoodNote: TextView = itemView.findViewById(R.id.text_mood_note)
        private val btnDeleteMood: ImageButton = itemView.findViewById(R.id.btn_delete_mood)

        fun bind(moodEntry: MoodEntry) {
            textMoodEmoji.text = moodEntry.emoji
            textMoodDate.text = moodEntry.getFormattedDate()

            // Show note or placeholder
            if (moodEntry.note.isNotEmpty()) {
                textMoodNote.text = moodEntry.note
                textMoodNote.visibility = View.VISIBLE
            } else {
                textMoodNote.visibility = View.GONE
            }

            btnDeleteMood.setOnClickListener {
                onDeleteClick(moodEntry)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodHistoryViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    fun updateMoodEntries(newEntries: List<MoodEntry>) {
        moodEntries = newEntries
        notifyDataSetChanged()
    }
}