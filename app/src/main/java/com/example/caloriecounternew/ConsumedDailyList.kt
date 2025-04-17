    package com.example.caloriecounternew

    import java.util.Calendar

    object ConsumedDailyList {
        private val consumedFoods: MutableList<FoodItem> = mutableListOf()
        private var lastResetDate: Int = getCurrentDayOfYear()

        init {
            checkAndReset()
        }

        // Add a food item with automatic day check
        fun addFoodItem(foodItem: FoodItem) {
            checkAndReset()
            consumedFoods.add(foodItem)
        }

        // All other functions should call checkAndReset() first
        fun getConsumedFoods(): List<FoodItem> {
            checkAndReset()
            return consumedFoods.toList()
        }

        fun getConsumedFoodsString(): List<String> {
            checkAndReset()
            return consumedFoods.map { it.itemName ?: "Unknown" }
        }

        fun getDailyCalories(): Int {
            checkAndReset()
            return consumedFoods.sumOf { foodItem ->
                val caloriesString = foodItem.calories ?: "0 kcal"
                caloriesString.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
            }
        }

        // Manual reset function
        fun reset() {
            consumedFoods.clear()
            lastResetDate = getCurrentDayOfYear()
        }

        // Private function to check if we need to reset
        private fun checkAndReset() {
            if (getCurrentDayOfYear() != lastResetDate) {
                consumedFoods.clear()
                lastResetDate = getCurrentDayOfYear()
            }
        }

        // Helper function to get current day of year (1-365)
        private fun getCurrentDayOfYear(): Int {
            return Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        }
    }