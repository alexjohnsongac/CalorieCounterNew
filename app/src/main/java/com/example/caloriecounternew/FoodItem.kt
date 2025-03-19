import com.google.firebase.database.PropertyName

data class FoodItem(
    @PropertyName("item_name") val itemName: String = "",
    @PropertyName("station_name") val stationName: String = "",
    @PropertyName("total_fat") val totalFat: String = "",
    @PropertyName("calories") var calories: Int = 0
) {
    // Firebase data deserialization method to handle String to Int conversion
    fun setCaloriesFromString(caloriesStr: String) {
        calories = caloriesStr.toIntOrNull() ?: 0 // Default to 0 if conversion fails
    }
}
