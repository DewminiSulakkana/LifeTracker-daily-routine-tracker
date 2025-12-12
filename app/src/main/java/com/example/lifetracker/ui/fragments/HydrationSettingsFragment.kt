package com.example.lifetracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.lifetracker.R
import com.example.lifetracker.data.models.HydrationSettings
import com.example.lifetracker.data.pref.PrefsHelper
import com.example.lifetracker.databinding.FragmentHydrationSettingsBinding
import com.example.lifetracker.utils.ReminderScheduler
import com.google.gson.Gson

class HydrationSettingsFragment : Fragment() {

    private var _binding: FragmentHydrationSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var settings: HydrationSettings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())
        loadSettings()
        setupUI()
        setupClickListeners()
        updateProgress()
    }

    fun loadSettings() {
        val json = prefsHelper.sharedPreferences.getString("hydration_settings", null)
        settings = if (json != null) {
            Gson().fromJson(json, HydrationSettings::class.java)
        } else {
            HydrationSettings()
        }
    }

    private fun saveSettings() {
        val json = Gson().toJson(settings)
        prefsHelper.sharedPreferences.edit().putString("hydration_settings", json).apply()

        if (settings.isEnabled) {
            ReminderScheduler.scheduleHydrationReminders(requireContext(), settings.reminderInterval)
        } else {
            ReminderScheduler.cancelHydrationReminders(requireContext())
        }
    }

    private fun setupUI() {
        binding.switchReminders.isChecked = settings.isEnabled
        binding.textIntervalValue.text = "${settings.reminderInterval} min"
        binding.seekbarInterval.progress = settings.reminderInterval / 5
        binding.textGoalValue.text = "${settings.dailyGoal}ml"
        binding.seekbarGoal.progress = settings.dailyGoal / 250

        updateReminderStatus(settings.isEnabled)
    }

    private fun setupClickListeners() {
        // Reminder toggle
        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            settings = settings.copy(isEnabled = isChecked)
            saveSettings()
            updateReminderStatus(isChecked)

            if (isChecked) {
                showToast("✅ Reminders activated every ${settings.reminderInterval} minutes!")
            } else {
                showToast("❌ Reminders turned off")
            }
        }

        // Interval seekbar
        binding.seekbarInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val interval = progress * 5
                binding.textIntervalValue.text = "$interval min"
                settings = settings.copy(reminderInterval = interval)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveSettings()
                if (settings.isEnabled) {
                    showToast("🔔 Reminder interval set to ${settings.reminderInterval} minutes")
                }
            }
        })

        // Goal seekbar
        binding.seekbarGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val goal = progress * 250
                binding.textGoalValue.text = "${goal}ml"
                settings = settings.copy(dailyGoal = goal)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveSettings()
                updateProgress()
            }
        })

        // Log water buttons
        binding.btnLogSmall.setOnClickListener { logWater(250) }
        binding.btnLogMedium.setOnClickListener { logWater(500) }
        binding.btnLogLarge.setOnClickListener { logWater(750) }
        binding.btnCustomAmount.setOnClickListener { showCustomAmountDialog() }

        // Reset daily intake
        binding.btnResetToday.setOnClickListener {
            settings = settings.resetDailyIntake()
            saveSettings()
            updateProgress()
            showToast("Today's progress reset! 💧")
        }

        // UPDATED: Auto Reminder Button - Starts 2-minute automatic notifications
        binding.btnTestNotification.setOnClickListener {
            startAutoReminders()
        }
    }

    // NEW: Start automatic 2-minute reminders
    private fun startAutoReminders() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🚀 Start Auto Reminders?")
            .setMessage("This will start automatic hydration reminders every 2 minutes.\n\n" +
                    "✅ Notifications will appear every 2 minutes\n" +
                    "✅ Works even when app is closed\n" +
                    "✅ Continues in background\n" +
                    "✅ Perfect for testing reminders!\n\n" +
                    "Click START to begin automatic reminders!")
            .setPositiveButton("START REMINDERS") { dialog, _ ->
                enableAutoReminders()
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // NEW: Enable automatic 2-minute reminders
    private fun enableAutoReminders() {
        // Save current settings first
        val currentInterval = settings.reminderInterval
        val currentEnabled = settings.isEnabled

        // Enable 2-minute reminders
        settings = settings.copy(
            isEnabled = true,
            reminderInterval = 2
        )
        saveSettings()

        // Update UI
        binding.switchReminders.isChecked = true
        binding.textIntervalValue.text = "2 min"
        binding.seekbarInterval.progress = 2 / 5

        updateReminderStatus(true)

        showToast("✅ AUTO REMINDERS STARTED!\n" +
                "You will get notifications every 2 minutes!\n" +
                "You can leave the app - reminders will continue! 🚀")

        // Show stop instructions
        showStopInstructions(currentInterval, currentEnabled)
    }

    // NEW: Show how to stop auto reminders
    private fun showStopInstructions(originalInterval: Int, originalEnabled: Boolean) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔔 Auto Reminders Active!")
            .setMessage("Automatic reminders are now running every 2 minutes!\n\n" +
                    "To STOP auto reminders:\n" +
                    "1. Turn off reminders toggle OR\n" +
                    "2. Change interval to more than 2 minutes\n\n" +
                    "Reminders will work in background even when you close the app! 📱")
            .setPositiveButton("GOT IT!") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("STOP NOW") { dialog, _ ->
                // Restore original settings
                settings = settings.copy(
                    isEnabled = originalEnabled,
                    reminderInterval = originalInterval
                )
                saveSettings()
                updateUI()
                showToast("Auto reminders stopped! 🛑")
                dialog.dismiss()
            }
            .show()
    }

    // NEW: Update UI after settings change
    private fun updateUI() {
        binding.switchReminders.isChecked = settings.isEnabled
        binding.textIntervalValue.text = "${settings.reminderInterval} min"
        binding.seekbarInterval.progress = settings.reminderInterval / 5
        updateReminderStatus(settings.isEnabled)
    }

    private fun logWater(amount: Int) {
        settings = settings.logWater(amount)
        saveSettings()
        updateProgress()
        showToast("Logged ${amount}ml water! 💧")
    }

    private fun showCustomAmountDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            setHint("Enter amount in ml")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Log Custom Amount")
            .setView(input)
            .setPositiveButton("Log") { _, _ ->
                val amount = input.text.toString().toIntOrNull() ?: 0
                if (amount > 0) {
                    logWater(amount)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProgress() {
        val progress = settings.getProgressPercentage()
        binding.textProgress.text = "${settings.todayIntake}ml / ${settings.dailyGoal}ml"
        binding.progressBar.progress = progress
        binding.textPercentage.text = "$progress%"

        binding.textMotivation.text = when {
            settings.isGoalReached() -> "🎉 Amazing! Goal achieved!"
            progress >= 75 -> "💪 Almost there! Keep going!"
            progress >= 50 -> "🔥 Great progress!"
            progress >= 25 -> "🚀 Good start!"
            else -> "💧 Let's get hydrated!"
        }
    }

    private fun updateReminderStatus(isEnabled: Boolean) {
        binding.reminderStatus.text = if (isEnabled) {
            "🔔 Reminders ON (${settings.reminderInterval} min intervals)"
        } else {
            "🔕 Reminders OFF"
        }

        val visibility = if (isEnabled) View.VISIBLE else View.GONE
        binding.reminderSettings.visibility = visibility
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}