package com.example.caloriecounternew

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Color.rgb
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.caloriecounternew.R.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import com.example.caloriecounternew.ui.theme.Blueish
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
//import androidx.appcompat.app.AppCompatActivity

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "CalorieCounterPrefs"
        private const val KEY_CALORIE_GOAL = "daily_calorie_goal"
        private const val DEFAULT_CALORIE_GOAL = 2000
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val googleAuthClient = GoogleAuthClient(this)
        pieChart = findViewById(id.pieChart)

        val streakTextView: TextView = findViewById(id.textViewCalorieStreak)

        // Initialize all views
        val signInButton: Button = findViewById(id.buttonSignIn)
        val signOutButton: Button = findViewById(id.buttonSignOut)
        val goToFoodPageButton: Button = findViewById(id.buttonGoToFoodPage)
        val setCalorieGoalButton: Button = findViewById(id.buttonSetCalorieGoal)
        val eggView: ImageView = findViewById(id.pixelEggView)
        val firstView: ImageView = findViewById(id.pixelFirstView)
        val secondView: ImageView = findViewById(id.pixelSecondView)


        // Check and update UI based on sign-in status
        updateUiVisibility(googleAuthClient.isSingedIn())
        updatePieChart() // Update chart on create
        CalorieStreakManager.checkAndUpdateStreak(this)

        val streak = CalorieStreakManager.getStreak(this)
        findViewById<TextView>(id.textViewCalorieStreak).text = "Weekly Streak: $streak of 7 days"


        // Set up button click listeners
        signInButton.setOnClickListener {
            lifecycleScope.launch {
                googleAuthClient.signIn()
                if (googleAuthClient.isSingedIn()) {
                    updateUiVisibility(true)
                    updatePieChart()
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

        eggView.setOnClickListener{
            eggView.animate().apply {
                duration = 500
                rotationYBy(360f)
            }
        }
        firstView.setOnClickListener{
            firstView.animate().apply {
                duration = 500
                rotationYBy(360f)
            }
        }
        secondView.setOnClickListener{
            secondView.animate().apply {
                duration = 1000
                rotationYBy(360f)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePieChart() // Update chart when returning from FoodPage
    }

    private fun updatePieChart() {
        val calorieGoal = getCalorieGoal().toFloat()
        val caloriesConsumed = ConsumedDailyList.getDailyCalories().toFloat()
        val remainingCalories = (calorieGoal - caloriesConsumed).coerceAtLeast(0f)

        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(caloriesConsumed, "Consumed"))
            add(PieEntry(remainingCalories, "Remaining"))
        }

        val dataSet = PieDataSet(entries, "Calorie Progress").apply {
            colors = listOf(Color.rgb(245,199,186), Color.rgb(208,221,228))
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        pieChart.data = PieData(dataSet)
        pieChart.apply {
            description.isEnabled = false
            centerText = "Daily Calories\n${caloriesConsumed.toInt()}/$calorieGoal"
            setCenterTextSize(14f)
            animateY(1000)
            legend.isEnabled = false
            setEntryLabelColor(Color.BLACK)
            invalidate() // refresh chart
        }
    }

    private fun updateUiVisibility(isSignedIn: Boolean) {
        findViewById<Button>(id.buttonSignIn).visibility = if (isSignedIn) View.GONE else View.VISIBLE
        findViewById<Button>(id.buttonSignOut).visibility = if (isSignedIn) View.VISIBLE else View.GONE
        findViewById<Button>(id.buttonGoToFoodPage).visibility = if (isSignedIn) View.VISIBLE else View.GONE
        findViewById<Button>(id.buttonSetCalorieGoal).visibility = if (isSignedIn) View.VISIBLE else View.GONE
        findViewById<ImageView>(id.pixelEggView).visibility =
            if (isSignedIn && CalorieStreakManager.getStreak(this) < 2) View.VISIBLE else View.GONE
        findViewById<ImageView>(id.pixelFirstView).visibility =
            if (isSignedIn && CalorieStreakManager.getStreak(this) >= 2 &&
                CalorieStreakManager.getStreak(this) < 4) View.VISIBLE else View.GONE
        findViewById<ImageView>(id.pixelSecondView).visibility =
            if (isSignedIn && CalorieStreakManager.getStreak(this) >= 4) View.VISIBLE else View.GONE
        findViewById<TextView>(id.textViewCalorieStreak).visibility = if (isSignedIn) View.VISIBLE else View.GONE
        pieChart.visibility = if (isSignedIn) View.VISIBLE else View.GONE
    }

    private fun showCalorieGoalDialog() {
        val dialogView = layoutInflater.inflate(layout.prompt_calories, null)
        val editTextGoal = dialogView.findViewById<EditText>(id.editTextCalorieGoal)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerStreakOptions) // New Spinner

        // Set up spinner options
        val options = listOf("Maintain weight", "Lose weight", "Gain mass")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val weightPrefs = getSharedPreferences("WeightPrefs", MODE_PRIVATE)
        val savedGoalType = weightPrefs.getString("goal_type", "Maintain weight") // Default
        val savedPosition = options.indexOf(savedGoalType)
        if (savedPosition != -1) {
            spinner.setSelection(savedPosition)
        }

        editTextGoal.setText(getCalorieGoal().toString()) // Runs ignore error

        AlertDialog.Builder(this)// Preset alert builder import
            .setTitle("Set Daily Calorie Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val input = editTextGoal.text.toString()
                val selectedGoalType = spinner.selectedItem.toString()
                if (input.isNotEmpty() && input.toIntOrNull() != null) {
                    val goal = input.toInt()
                    saveCalorieGoal(goal)
                    val weightPrefs = getSharedPreferences("WeightPrefs", MODE_PRIVATE)
                    weightPrefs.edit().putString("goal_type", selectedGoalType).apply()
                    Toast.makeText(this, "Goal set to $goal calories", Toast.LENGTH_SHORT).show()
                    updatePieChart()
                } else {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Suggest Calorie Goal") { _, _ ->
                /*
                val dialog = CalorieSuggestDialog()
                dialog.listener = this@MainActivity
                dialog.show(this@MainActivity.supportFragmentManager, "CalorieSuggestDialog")
                */

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCalorieGoal(goal: Int) {
        sharedPreferences.edit().putInt(KEY_CALORIE_GOAL, goal).apply()
    }

    private fun getCalorieGoal(): Int {
        return sharedPreferences.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)
    }

    private fun navigateToFoodPage() {
        startActivity(Intent(this, FoodPage::class.java))
    }
}