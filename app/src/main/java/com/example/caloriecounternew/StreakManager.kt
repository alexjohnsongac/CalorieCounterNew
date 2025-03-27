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
        val lastLogDate = getLastLogDate()
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        var currentStreak = getStreak()

        if (lastLogDate != todayDate) {
            // Only increment streak if calories are between 1700 and 2300
            if (totalCalories in 1700..2300) {
                if (isConsecutiveDay(lastLogDate, todayDate)) {
                    currentStreak++
                } else {
                    currentStreak = 1 // Reset streak if not consecutive
                }
            } else {
                currentStreak = 1 // Reset streak if calories are not within the desired range
            }
            // Update the last log date
            setLastLogDate(todayDate)

            // Save the updated streak value
            setStreak(currentStreak)
        }

        return currentStreak
    }

    // Check if the last log was yesterday
    private fun isConsecutiveDay(lastLogDate: String?, todayDate: String): Boolean {
        return if (lastLogDate != null) {
            val lastLogCalendar = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastLogDate) ?: Date()
            }
            val todayCalendar = Calendar.getInstance()
            todayCalendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todayDate) ?: Date()

            // Check if last log date is exactly one day before today
            todayCalendar.get(Calendar.DAY_OF_YEAR) - lastLogCalendar.get(Calendar.DAY_OF_YEAR) == 1 &&
                    todayCalendar.get(Calendar.YEAR) == lastLogCalendar.get(Calendar.YEAR)
        } else {
            false
        }
    }
}
