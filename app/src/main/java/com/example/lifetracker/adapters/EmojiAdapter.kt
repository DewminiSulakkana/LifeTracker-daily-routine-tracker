package com.example.lifetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifetracker.R

class EmojiAdapter(
    private val emojis: List<String>,
    private val onEmojiSelected: (String) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    private var selectedPosition = -1

    inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textEmoji: TextView = itemView.findViewById(R.id.text_emoji)

        fun bind(emoji: String, position: Int) {
            textEmoji.text = emoji

            // Set selection state
            textEmoji.isSelected = position == selectedPosition

            textEmoji.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position

                // Update previous selection
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
                // Update new selection
                notifyItemChanged(selectedPosition)

                onEmojiSelected(emoji)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emoji, parent, false)
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(emojis[position], position)
    }

    override fun getItemCount(): Int = emojis.size

    fun getSelectedEmoji(): String? {
        return if (selectedPosition != -1) emojis[selectedPosition] else null
    }

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = -1
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
    }
}