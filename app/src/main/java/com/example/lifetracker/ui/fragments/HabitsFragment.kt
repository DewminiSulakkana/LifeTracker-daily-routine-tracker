package com.example.lifetracker.ui.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifetracker.R
import com.example.lifetracker.adapters.HabitAdapter
import com.example.lifetracker.data.models.Habit
import com.example.lifetracker.data.pref.PrefsHelper
import com.example.lifetracker.databinding.FragmentHabitsBinding
import java.util.*

class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var habitAdapter: HabitAdapter
    private val habits = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())
        setupRecyclerView()

        // TEMPORARY: Add a test habit if none exist
        if (prefsHelper.loadHabits().isEmpty()) {
            val testHabit = Habit(
                name = "Drink Water",
                icon = "💧",
                targetCount = 1
            )
            prefsHelper.saveHabit(testHabit)
        }

        loadHabits()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits = habits,
            onHabitClick = { habit ->
                // NEW: Toggle completion status for card design
                habit.toggleCompletion()
                prefsHelper.saveHabit(habit)
                updateHabitList()

                // Show feedback based on new status
                val message = if (habit.completed)
                    "${habit.name} completed! 🎉"
                else
                    "${habit.name} marked as pending"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { habit ->
                // Handle delete with confirmation
                showDeleteConfirmation(habit)
            }
        )

        binding.recyclerHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }

    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                prefsHelper.deleteHabit(habit.id)
                loadHabits()
                Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadHabits() {
        try {
            habits.clear()
            val loadedHabits = prefsHelper.loadHabits()
            habits.addAll(loadedHabits)
            updateHabitList()

            // Debug info
            println("DEBUG: Loaded ${habits.size} habits")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading habits", Toast.LENGTH_SHORT).show()
            // Start with empty list if loading fails
            habits.clear()
            updateHabitList()
        }
    }

    private fun updateHabitList() {
        try {
            habitAdapter.updateHabits(habits)
            updateProgressSummary()

            // Show empty state if no habits
            if (habits.isEmpty()) {
                binding.textEmpty.visibility = View.VISIBLE
                binding.recyclerHabits.visibility = View.GONE
                binding.layoutProgressHeader.visibility = View.GONE
            } else {
                binding.textEmpty.visibility = View.GONE
                binding.recyclerHabits.visibility = View.VISIBLE
                binding.layoutProgressHeader.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading habits", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun updateProgressSummary() {
        if (habits.isEmpty()) {
            binding.textTotalProgress.text = "Today's Progress: 0%"
            binding.progressTotal.progress = 0
            return
        }

        val completedHabits = habits.count { it.completed }
        val totalHabits = habits.size
        val progressPercentage = (completedHabits * 100) / totalHabits

        // NEW: Updated text to match our card design
        binding.textTotalProgress.text = "TODAY'S PROGRESS: $progressPercentage%"
        binding.progressTotal.progress = progressPercentage

        // NEW: Add motivational messages based on progress
        updateMotivationalMessage(progressPercentage)
    }

    private fun updateMotivationalMessage(progress: Int) {
        val message = when {
            progress == 100 -> "Amazing! You completed all habits! 🎉"
            progress >= 75 -> "Great job! Almost there! 💪"
            progress >= 50 -> "Good progress! Keep going! 🔥"
            progress >= 25 -> "You're getting started! 🚀"
            else -> "Every small step counts! 🌟"
        }
        binding.textMotivational.text = message
    }

    private fun setupClickListeners() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val editHabitName = dialogView.findViewById<EditText>(R.id.edit_habit_name)
        val layoutCategories = dialogView.findViewById<LinearLayout>(R.id.layout_categories)
        val layoutIcons = dialogView.findViewById<LinearLayout>(R.id.layout_icons)
        val layoutColors = dialogView.findViewById<LinearLayout>(R.id.layout_colors)
        val btnSetTime = dialogView.findViewById<Button>(R.id.btn_set_time)
        val textSelectedTime = dialogView.findViewById<TextView>(R.id.text_selected_time)
        val checkReminder = dialogView.findViewById<CheckBox>(R.id.check_reminder)
        val textTargetCount = dialogView.findViewById<TextView>(R.id.text_target_count)
        val btnDecrease = dialogView.findViewById<Button>(R.id.btn_decrease)
        val btnIncrease = dialogView.findViewById<Button>(R.id.btn_increase)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

        var selectedIcon = "💧" // Default icon
        var selectedCategory = "General" // Default category
        var selectedTime: String? = null // No time by default
        var targetCount = 1 // NEW: Default to 1 for card-based completion

        // NEW: Card color options
        val cardColors = listOf(
            "#FF7C4DFF", // Purple
            "#FF4DA7FF", // Blue
            "#FFFF6B6B", // Red
            "#FF4CD964", // Green
            "#FFFFD166", // Yellow
            "#FF5AC8FA", // Light Blue
            "#FFFF9FF3", // Pink
            "#FF54C6EB"  // Sky Blue
        )
        var selectedColor = cardColors.first()

        // FIXED: Define setupColorSelection FIRST, then call it
        fun setupColorSelection(layout: LinearLayout, colors: List<String>, onColorSelected: (String) -> Unit) {
            layout.removeAllViews()

            fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

            colors.forEach { color ->
                val view = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(48.dpToPx(), 48.dpToPx()).apply {
                        marginEnd = 8.dpToPx()
                    }
                    // Use fallback drawables if custom ones don't exist
                    val defaultDrawable = try {
                        ContextCompat.getDrawable(requireContext(), R.drawable.color_default)
                    } catch (e: Exception) {
                        // Fallback to a simple shape
                        GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setStroke(2.dpToPx(), android.graphics.Color.GRAY)
                        }
                    }

                    val selectedDrawable = try {
                        ContextCompat.getDrawable(requireContext(), R.drawable.color_selected)
                    } catch (e: Exception) {
                        // Fallback to a simple shape
                        GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setStroke(4.dpToPx(), android.graphics.Color.parseColor("#FF7C4DFF"))
                        }
                    }

                    background = defaultDrawable
                    setOnClickListener {
                        // Reset all backgrounds
                        for (i in 0 until layout.childCount) {
                            layout.getChildAt(i).background = defaultDrawable
                        }
                        // Set selected background
                        background = selectedDrawable
                        onColorSelected(color)
                    }

                    // Set color circle
                    val colorDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(android.graphics.Color.parseColor(color))
                        setStroke(2.dpToPx(), android.graphics.Color.WHITE)
                    }

                    val compoundDrawable = LayerDrawable(arrayOf(colorDrawable))
                    background = compoundDrawable
                }
                layout.addView(view)
            }

            // Select the first color by default
            if (layout.childCount > 0) {
                val selectedDrawable = try {
                    ContextCompat.getDrawable(requireContext(), R.drawable.color_selected)
                } catch (e: Exception) {
                    GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setStroke(4.dpToPx(), android.graphics.Color.parseColor("#FF7C4DFF"))
                    }
                }
                layout.getChildAt(0).background = selectedDrawable
            }
        }

        // NOW call setupColorSelection after it's defined
        setupColorSelection(layoutColors, cardColors) { selected ->
            selectedColor = selected
        }

        // Setup category selection
        val categories = listOf(
            "Morning", "Health", "Work", "Evening",
            "Night", "Social", "Learning", "Fitness",
            "Self Care", "Chores", "Creative", "Financial"
        )
        setupCategorySelection(layoutCategories, categories) { selected ->
            selectedCategory = selected
        }

        // Setup icon selection
        val icons = listOf("💧", "🥗", "🏃", "🧘", "📚", "🎵", "🛌", "☀️","🌙", "💊", "📱", "🎯", "🎨")
        setupIconSelection(layoutIcons, icons) { selected ->
            selectedIcon = selected
        }

        // Setup time picker
        btnSetTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                selectedTime = String.format("%02d:%02d", hour, minute)
                textSelectedTime.text = "$displayHour:${String.format("%02d", minute)} $amPm"
                checkReminder.isEnabled = true
            }
        }

        // Setup reminder checkbox
        checkReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && selectedTime == null) {
                Toast.makeText(requireContext(), "Please set a time first", Toast.LENGTH_SHORT).show()
                checkReminder.isChecked = false
            }
        }

        // NEW: Updated target counter (1-1 for card completion)
        btnDecrease.setOnClickListener {
            if (targetCount > 1) {
                targetCount--
                textTargetCount.text = targetCount.toString()
            }
        }

        btnIncrease.setOnClickListener {
            if (targetCount < 10) {
                targetCount++
                textTargetCount.text = targetCount.toString()
            }
        }

        // Setup button click listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val habitName = editHabitName.text.toString().trim()
            if (habitName.isNotEmpty()) {
                val newHabit = Habit(
                    name = habitName,
                    icon = selectedIcon,
                    targetCount = targetCount,
                    scheduledTime = selectedTime,
                    hasReminder = checkReminder.isChecked && selectedTime != null,
                    category = selectedCategory,
                    customCardColor = selectedColor // NEW: Save card color
                )
                prefsHelper.saveHabit(newHabit)
                loadHabits()
                dialog.dismiss()

                // Show success message
                Toast.makeText(requireContext(), "Habit added successfully!", Toast.LENGTH_SHORT).show()
            } else {
                editHabitName.error = "Please enter a habit name"
            }
        }

        dialog.show()
    }

    private fun setupCategorySelection(layout: LinearLayout, categories: List<String>, onCategorySelected: (String) -> Unit) {
        layout.removeAllViews()

        categories.forEach { category ->
            val button = Button(requireContext()).apply {
                text = category
                setPadding(16, 8, 16, 8)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_200))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

                setOnClickListener {
                    // Reset all backgrounds
                    for (i in 0 until layout.childCount) {
                        layout.getChildAt(i).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_200))
                    }
                    // Set selected background
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    onCategorySelected(category)
                }
            }
            layout.addView(button)
        }

        // Select the first category by default
        if (layout.childCount > 0) {
            layout.getChildAt(0).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent))
            (layout.getChildAt(0) as Button).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun setupIconSelection(layout: LinearLayout, icons: List<String>, onIconSelected: (String) -> Unit) {
        layout.removeAllViews()

        icons.forEach { icon ->
            val textView = TextView(requireContext()).apply {
                text = icon
                textSize = 24f
                setPadding(16, 16, 16, 16)
                // Use fallback if drawables don't exist
                val defaultDrawable = try {
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_selector)
                } catch (e: Exception) {
                    // Fallback
                    GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        setColor(ContextCompat.getColor(requireContext(), R.color.gray_200))
                        cornerRadius = 8f
                    }
                }

                val selectedDrawable = try {
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_selected)
                } catch (e: Exception) {
                    // Fallback
                    GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        setColor(ContextCompat.getColor(requireContext(), R.color.accent))
                        cornerRadius = 8f
                    }
                }

                background = defaultDrawable
                setOnClickListener {
                    // Reset all backgrounds
                    for (i in 0 until layout.childCount) {
                        layout.getChildAt(i).background = defaultDrawable
                    }
                    // Set selected background
                    background = selectedDrawable
                    onIconSelected(icon)
                }
            }
            layout.addView(textView)
        }

        // Select the first icon by default
        if (layout.childCount > 0) {
            val selectedDrawable = try {
                ContextCompat.getDrawable(requireContext(), R.drawable.icon_selected)
            } catch (e: Exception) {
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(ContextCompat.getColor(requireContext(), R.color.accent))
                    cornerRadius = 8f
                }
            }
            layout.getChildAt(0).background = selectedDrawable
        }
    }

    private fun showTimePickerDialog(onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                onTimeSelected(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false // 24-hour format
        )
        timePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}