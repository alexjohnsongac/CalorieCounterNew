package com.example.caloriecounternew

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class StreakManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CalorieStreakPrefs", Context.MODE_PRIVATE)

    private val STREAK_KEY = "calorie_streak"
    private val LAST_LOG_DATE_KEY = "last_log_date"
    private val TOTAL_CALORIES_KEY = "total_calories"

    // Get the current streak
    fun getStreak(): Int {
        return sharedPreferences.getInt(STREAK_KEY, 0)
    }

    // Set the current streak
    fun setStreak(streak: Int) {
        sharedPreferences.edit().putInt(STREAK_KEY, streak).apply()
    }

    // Get the last logged date
    fun getLastLogDate(): String? {
        return sharedPreferences.getString(LAST_LOG_DATE_KEY, null)
    }

    // Set the last logged date
    fun setLastLogDate(date: String) {
        sharedPreferences.edit().putString(LAST_LOG_DATE_KEY, date).apply()
    }

    // Update the streak based on calorie intake
    fun updateStreak(totalCalories: Int): Int {
        var currentStreak = getStreak()

      //  if (lastLogDate != todayDate) {
            // Only increment streak if calories are between 1700 and 2300
        if (totalCalories in 1700..2300) {
            currentStreak++

        } else {
            currentStreak = 1 // Reset streak if calories are not within the desired range
        }

            // Save the updated streak value
        setStreak(currentStreak)

        return currentStreak
    }

}
