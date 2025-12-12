package com.example.lifetracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifetracker.adapters.EmojiAdapter
import com.example.lifetracker.adapters.MoodHistoryAdapter
import com.example.lifetracker.data.models.MoodEntry
import com.example.lifetracker.data.pref.PrefsHelper
import com.example.lifetracker.databinding.FragmentMoodBinding
import com.google.android.material.textfield.TextInputEditText

class MoodFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var emojiAdapter: EmojiAdapter
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter

    private val emojis = listOf(
        "😊", "😂", "🥰", "😍", "🤩", "😎",
        "😐", "😕", "😔", "😢", "😭", "😡",
        "🤢", "😴", "😨", "🤯", "🥳", "😇"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())
        setupEmojiGrid()
        setupMoodHistory()
        setupClickListeners()
        loadMoodHistory()
    }

    private fun setupEmojiGrid() {
        emojiAdapter = EmojiAdapter(emojis) { selectedEmoji ->
            // Emoji selected - we'll use this when saving
            binding.btnSaveMood.isEnabled = true
        }

        binding.recyclerEmojis.apply {
            layoutManager = GridLayoutManager(requireContext(), 6) // 6 emojis per row
            adapter = emojiAdapter
        }
    }

    private fun setupMoodHistory() {
        moodHistoryAdapter = MoodHistoryAdapter(emptyList()) { moodEntry ->
            // Delete mood entry
            prefsHelper.deleteMoodEntry(moodEntry.id)
            loadMoodHistory()
        }

        binding.recyclerMoodHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodHistoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }

        binding.btnShareMood.setOnClickListener {
            shareMoodSummary()
        }
    }

    private fun saveMoodEntry() {
        val selectedEmoji = emojiAdapter.getSelectedEmoji()
        if (selectedEmoji != null) {
            val note = binding.editMoodNote.text.toString().trim()

            val moodEntry = MoodEntry(
                emoji = selectedEmoji,
                note = note
            )

            prefsHelper.saveMoodEntry(moodEntry)

            // Clear selection and input
            emojiAdapter.clearSelection()
            binding.editMoodNote.text?.clear()
            binding.btnSaveMood.isEnabled = false

            // Reload history
            loadMoodHistory()

            // Show success feedback
            // (We'll add a nice toast or animation here later)
        }
    }

    private fun loadMoodHistory() {
        val moodEntries = prefsHelper.loadMoodEntries()
        moodHistoryAdapter.updateMoodEntries(moodEntries)

        // Show/hide empty state
        if (moodEntries.isEmpty()) {
            binding.textEmptyMood.visibility = View.VISIBLE
            binding.recyclerMoodHistory.visibility = View.GONE
            binding.btnShareMood.visibility = View.GONE
        } else {
            binding.textEmptyMood.visibility = View.GONE
            binding.recyclerMoodHistory.visibility = View.VISIBLE
            binding.btnShareMood.visibility = View.VISIBLE
        }
    }

    private fun shareMoodSummary() {
        val moodEntries = prefsHelper.loadMoodEntries()
        if (moodEntries.isNotEmpty()) {
            val summary = buildMoodSummary(moodEntries)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, summary)
                type = "text/plain"
            }

            startActivity(Intent.createChooser(shareIntent, "Share Mood Summary"))
        }
    }

    private fun buildMoodSummary(moodEntries: List<MoodEntry>): String {
        val recentMoods = moodEntries.take(7) // Last 7 days
        val moodString = recentMoods.joinToString(" ") { it.emoji }

        return """My Mood Summary from LifeTracker 📊
            
Recent Moods: $moodString

Total Entries: ${moodEntries.size}
Last Entry: ${moodEntries.firstOrNull()?.getFormattedDate()}

Download LifeTracker to track your daily wellness! 😊"""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}