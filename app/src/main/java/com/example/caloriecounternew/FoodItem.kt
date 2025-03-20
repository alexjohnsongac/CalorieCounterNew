package com.example.caloriecounternew

data class FoodItem(
    val itemName: String? = null,
    val stationName: String? = null,
    val calories: String? = null,
    val cholesterol: String? = null, // Can be String or Double
    val ingredients: String? = null,
    val meal: String? = null,
    val permanent: Long? = null,
    val portionSize: String? = null,
    val price: Double? = null,
    val protein: String? = null,
    val sodium: String? = null,
    val totalCarbohydrates: String? = null, // Can be String or Double
    val totalFat: String? = null
) {
    companion object {
        // Custom method to create a FoodItem from a DataSnapshot
        fun fromSnapshot(snapshot: Map<String, Any?>): FoodItem {
            return FoodItem(
                itemName = snapshot["item_name"] as? String,
                stationName = snapshot["station_name"] as? String,
                calories = snapshot["calories"] as? String,
                cholesterol = parseFlexibleValue(snapshot["cholesterol"]), // Handle String or Double
                ingredients = snapshot["ingredients"] as? String,
                meal = snapshot["meal"] as? String,
                permanent = snapshot["permanent"] as? Long,
                portionSize = snapshot["portion_size"] as? String,
                price = snapshot["price"] as? Double,
                protein = snapshot["protein"] as? String,
                sodium = snapshot["sodium"] as? String,
                totalCarbohydrates = parseFlexibleValue(snapshot["total_carbohydrates"]), // Handle String or Double
                totalFat = snapshot["total_fat"] as? String
            )
        }

        // Helper function to handle fields that can be String or Double
        private fun parseFlexibleValue(value: Any?): String? {
            return when (value) {
                is String -> value
                is Double -> value.toString()
                is Long -> value.toString()
                else -> null
            }
        }
    }
}