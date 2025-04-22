import android.content.Context
import android.content.SharedPreferences
import com.example.caloriecounternew.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalFoodManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("CustomFoodsPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "custom_foods_list"

    fun saveCustomFoods(foods: List<FoodItem>) {
        val json = gson.toJson(foods)
        sharedPref.edit().putString(key, json).apply()
    }

    fun getCustomFoods(): List<FoodItem> {
        val json = sharedPref.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<FoodItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
}