package com.example.lifetracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.lifetracker.R
import com.example.lifetracker.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        animateFeatureCards()
    }

    private fun setupClickListeners() {
        // Get Started button - navigate to Habits
        binding.btnGetStarted.setOnClickListener {
            navigateToHabits()
        }

        // Make all feature cards clickable
        setupCardClicks()
    }

    private fun setupCardClicks() {
        val cards = listOf(
            binding.cardProgressTracking,
            binding.cardGoalSetting,
            binding.cardSmartReminders,
            binding.cardMoodTracker,
            binding.cardHydrationGoal,
            binding.cardDailyAnalytics
        )

        cards.forEach { card ->
            card?.setOnClickListener {
                navigateToHabits()
            }
        }
    }

    private fun animateFeatureCards() {
        val cards = listOf(
            binding.cardProgressTracking,
            binding.cardGoalSetting,
            binding.cardSmartReminders,
            binding.cardMoodTracker,
            binding.cardHydrationGoal,
            binding.cardDailyAnalytics
        )

        // Simple fade-in animation without external XML files
        cards.forEachIndexed { index, card ->
            card?.alpha = 0f
            card?.translationY = 50f // Start slightly below

            card?.postDelayed({
                card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start()
            }, (index * 150).toLong()) // Stagger animation
        }
    }

    private fun navigateToHabits() {
        // Simple fragment transaction without Navigation Component
        val habitsFragment = HabitsFragment()

        parentFragmentManager.commit {
            replace(R.id.fragment_container, habitsFragment)
            addToBackStack("home") // Allows back navigation to home
            setReorderingAllowed(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}