package com.example.lifetracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifetracker.R
import com.example.lifetracker.adapters.WaterHistoryAdapter
import com.example.lifetracker.data.models.WaterEntry
import com.example.lifetracker.data.pref.PrefsHelper
import com.example.lifetracker.databinding.FragmentHydrationBinding
import com.example.lifetracker.views.CircularProgressView
import java.text.SimpleDateFormat
import java.util.*

class HydrationFragment : Fragment() {

    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var waterHistoryAdapter: WaterHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())
        setupWaterHistory()
        setupClickListeners()
        updateUI()
    }

    private fun setupWaterHistory() {
        waterHistoryAdapter = WaterHistoryAdapter(emptyList())

        binding.recyclerWaterHistory.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = waterHistoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddWater.setOnClickListener {
            showAddWaterDialog()
        }

        binding.btnStats.setOnClickListener {
            // Simple stats for now - can be enhanced later
            showSimpleStats()
        }
    }

    private fun updateUI() {
        // Update date
        val currentDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
        binding.textDate.text = currentDate

        // Update goal
        val dailyGoal = prefsHelper.getDailyGoal()
        binding.textGoal.text = "🎯 HYDRATION GOAL: ${dailyGoal}ml"

        // Get today's data
        val todayEntries = prefsHelper.getTodayWaterEntries()
        val totalWater = prefsHelper.getTodayTotalWater()

        // Update progress
        val progress = (totalWater.toFloat() / dailyGoal) * 100f
        binding.circularProgress.setProgress(progress)

        // Update progress text
        binding.textProgress.text = "${totalWater}ml / ${dailyGoal}ml"

        // Update next reminder (simple calculation)
        val averageInterval = if (todayEntries.size > 1) {
            val timeDiff = todayEntries.first().time - todayEntries.last().time
            (timeDiff / (1000 * 60 * (todayEntries.size - 1))).toInt()
        } else {
            120 // Default 2 hours if not enough data
        }
        binding.textNextReminder.text = "Next: ${averageInterval} minutes"

        // Update water history
        waterHistoryAdapter.updateWaterEntries(todayEntries.reversed()) // Show latest first

        // Show/hide empty state
        if (todayEntries.isEmpty()) {
            binding.textEmptyDrinks.visibility = View.VISIBLE
            binding.recyclerWaterHistory.visibility = View.GONE
        } else {
            binding.textEmptyDrinks.visibility = View.GONE
            binding.recyclerWaterHistory.visibility = View.VISIBLE
        }
    }

    private fun showAddWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_water, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val layoutDrinkTypes = dialogView.findViewById<LinearLayout>(R.id.layout_drink_types)
        val textAmount = dialogView.findViewById<TextView>(R.id.text_amount)
        val btnDecreaseAmount = dialogView.findViewById<Button>(R.id.btn_decrease_amount)
        val btnIncreaseAmount = dialogView.findViewById<Button>(R.id.btn_increase_amount)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

        var selectedDrinkType = "💧"
        var selectedAmount = 250 // Default amount

        // Setup drink types
        val drinkTypes = listOf("💧", "☕", "🥛", "🍵", "🥤")
        setupDrinkTypeSelection(layoutDrinkTypes, drinkTypes) { selected ->
            selectedDrinkType = selected
        }

        // Setup amount controls
        btnDecreaseAmount.setOnClickListener {
            if (selectedAmount > 100) {
                selectedAmount -= 50
                textAmount.text = "${selectedAmount}ml"
            }
        }

        btnIncreaseAmount.setOnClickListener {
            if (selectedAmount < 1000) {
                selectedAmount += 50
                textAmount.text = "${selectedAmount}ml"
            }
        }

        // Setup button click listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val waterEntry = WaterEntry(
                type = selectedDrinkType,
                amount = selectedAmount
            )
            prefsHelper.saveWaterEntry(waterEntry)
            updateUI()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupDrinkTypeSelection(layout: LinearLayout, drinkTypes: List<String>, onDrinkSelected: (String) -> Unit) {
        layout.removeAllViews()

        drinkTypes.forEach { drinkType ->
            val textView = TextView(requireContext()).apply {
                text = drinkType
                textSize = 32f
                setPadding(16, 16, 16, 16)
                background = requireContext().getDrawable(android.R.drawable.btn_default)
                setOnClickListener {
                    // Reset all backgrounds
                    for (i in 0 until layout.childCount) {
                        layout.getChildAt(i).background = requireContext().getDrawable(android.R.drawable.btn_default)
                    }
                    // Set selected background
                    background = requireContext().getDrawable(android.R.drawable.btn_default)
                    onDrinkSelected(drinkType)
                }
            }
            layout.addView(textView)
        }
    }

    private fun showSimpleStats() {
        val todayEntries = prefsHelper.getTodayWaterEntries()
        val totalWater = prefsHelper.getTodayTotalWater()
        val dailyGoal = prefsHelper.getDailyGoal()

        val statsMessage = """
            📊 Today's Hydration Stats:
            
            Total Drunk: ${totalWater}ml
            Daily Goal: ${dailyGoal}ml
            Progress: ${(totalWater.toFloat() / dailyGoal * 100).toInt()}%
            Number of Drinks: ${todayEntries.size}
            
            Keep up the good work! 💧
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Hydration Statistics")
            .setMessage(statsMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}