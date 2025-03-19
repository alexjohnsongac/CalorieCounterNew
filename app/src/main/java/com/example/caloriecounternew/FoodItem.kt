package com.example.caloriecounternew

data class FoodItem(
    val item_name: String = "", // Default empty string in case no data is found
    val calories: Int = 0      // Default 0 in case no data is found
)