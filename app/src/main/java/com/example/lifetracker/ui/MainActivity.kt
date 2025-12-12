package com.example.lifetracker.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.lifetracker.R
import com.example.lifetracker.databinding.ActivityMainBinding
import com.example.lifetracker.ui.fragments.HomeFragment
import com.example.lifetracker.ui.fragments.HabitsFragment
import com.example.lifetracker.ui.fragments.HydrationFragment
import com.example.lifetracker.ui.fragments.HydrationSettingsFragment
import com.example.lifetracker.ui.fragments.MoodFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Load Home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.navigation_home
        }
    }


    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_habits -> {
                    loadFragment(HabitsFragment())
                    true
                }
                R.id.navigation_mood -> {
                    loadFragment(MoodFragment())
                    true
                }
                R.id.navigation_hydration -> {
                    loadFragment(HydrationFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(HydrationSettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

//    private fun setupNavigation() {
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//    }
}
