package com.example.caloriecounternew

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "CalorieCounterPrefs"
        private const val KEY_CALORIE_GOAL = "daily_calorie_goal"
        private const val DEFAULT_CALORIE_GOAL = 2000
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val googleAuthClient = GoogleAuthClient(this)

        // Initialize all views
        val signInButton: Button = findViewById(R.id.buttonSignIn)
        val signOutButton: Button = findViewById(R.id.buttonSignOut)
        val goToFoodPageButton: Button = findViewById(R.id.buttonGoToFoodPage)
        val setCalorieGoalButton: Button = findViewById(R.id.buttonSetCalorieGoal)
        val eggView: ImageView = findViewById(R.id.pixelEggView)
        val firstView: ImageView = findViewById(R.id.pixelFirstView)
        val secondView: ImageView = findViewById(R.id.pixelSecondView)

        // Check and update UI based on sign-in status
        updateUiVisibility(googleAuthClient.isSingedIn())

        // Set up button click listeners
        signInButton.setOnClickListener {
            lifecycleScope.launch {
                googleAuthClient.signIn()
                if (googleAuthClient.isSingedIn()) {
                    updateUiVisibility(true)
                }
            }
        }

        signOutButton.setOnClickListener {
            lifecycleScope.launch {
                googleAuthClient.signOut()
                updateUiVisibility(false)
            }
        }

        goToFoodPageButton.setOnClickListener {
            navigateToFoodPage()
        }

        setCalorieGoalButton.setOnClickListener {
            showCalorieGoalDialog()
        }
    }

    private fun updateUiVisibility(isSignedIn: Boolean) {
        findViewById<Button>(R.id.buttonSignIn).visibility = if (isSignedIn) View.GONE else View.VISIBLE
        findViewById<Button>(R.id.buttonSignOut).visibility = if (isSignedIn) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.buttonGoToFoodPage).visibility = if (isSignedIn) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.buttonSetCalorieGoal).visibility = if (isSignedIn) View.VISIBLE else View.GONE
        findViewById<ImageView>(R.id.pixelEggView).visibility = if (isSignedIn) View.VISIBLE else View.GONE
    }

    private fun showCalorieGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.prompt_calories, null)
        val editTextGoal = dialogView.findViewById<EditText>(R.id.editTextCalorieGoal)

        // Set current goal if exists
        editTextGoal.setText(getCalorieGoal().toString())

        AlertDialog.Builder(this)
            .setTitle("Set Daily Calorie Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val input = editTextGoal.text.toString()
                if (input.isNotEmpty() && input.toIntOrNull() != null) {
                    val goal = input.toInt()
                    saveCalorieGoal(goal)
                    Toast.makeText(this, "Goal set to $goal calories", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCalorieGoal(goal: Int) {
        sharedPreferences.edit().putInt(KEY_CALORIE_GOAL, goal).apply()
    }

    fun getCalorieGoal(): Int {
        return sharedPreferences.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)
    }

    private fun navigateToFoodPage() {
        startActivity(Intent(this, FoodPage::class.java))
    }
}