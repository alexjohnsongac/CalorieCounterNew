package com.example.caloriecounternew

data class FoodItem(//saving all fooditems properties
    val itemName: String? = null,
    val stationName: String? = null,
    val calories: String? = null,
    val cholesterol: String? = null,
    val ingredients: String? = null,
    val meal: String? = null,
    val permanent: Long? = null,
    val portionSize: String? = null,
    val price: Double? = null,
    val protein: String? = null,
    val sodium: String? = null,
    val totalCarbohydrates: String? = null,
    val totalFat: String? = null
) {
    companion object {
        //custom method to create a FoodItem from a DataSnapshot
        fun fromSnapshot(snapshot: Map<String, Any?>): FoodItem { //allow the string to be anything
            return FoodItem(
                itemName = snapshot["item_name"] as? String,
                stationName = snapshot["station_name"] as? String, //could be used for sorting.
                calories = snapshot["calories"] as? String,
                cholesterol = parseFlexibleValue(snapshot["cholesterol"]), //string or double
                ingredients = snapshot["ingredients"] as? String,
                meal = snapshot["meal"] as? String, //breakfast, lunch, dinner sorting
                permanent = snapshot["permanent"] as? Long,
                portionSize = snapshot["portion_size"] as? String,
                price = snapshot["price"] as? Double, //attempt to save price as double if not string
                protein = snapshot["protein"] as? String,
                sodium = snapshot["sodium"] as? String,
                totalCarbohydrates = parseFlexibleValue(snapshot["total_carbohydrates"]), //string or double
                totalFat = snapshot["total_fat"] as? String
            )
        }

        // Helper function to handle fields that can be String or Double
        private fun parseFlexibleValue(value: Any?): String? {
            return when (value) {
                is String -> value
                is Double -> value.toString()
                is Long -> value.toString() //save all as strings for consistency, can parse later
                else -> null
            }
        }
    }
}