package com.example.lifetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifetracker.R
import com.example.lifetracker.data.models.WaterEntry

class WaterHistoryAdapter(
    private var waterEntries: List<WaterEntry>
) : RecyclerView.Adapter<WaterHistoryAdapter.WaterHistoryViewHolder>() {

    inner class WaterHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textDrinkEmoji: TextView = itemView.findViewById(R.id.text_drink_emoji)
        private val textDrinkAmount: TextView = itemView.findViewById(R.id.text_drink_amount)
        private val textDrinkTime: TextView = itemView.findViewById(R.id.text_drink_time)

        fun bind(waterEntry: WaterEntry) {
            textDrinkEmoji.text = waterEntry.type
            textDrinkAmount.text = "${waterEntry.amount}ml"
            textDrinkTime.text = waterEntry.getFormattedTime()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_water_history, parent, false)
        return WaterHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaterHistoryViewHolder, position: Int) {
        holder.bind(waterEntries[position])
    }

    override fun getItemCount(): Int = waterEntries.size

    fun updateWaterEntries(newEntries: List<WaterEntry>) {
        waterEntries = newEntries
        notifyDataSetChanged()
    }
}