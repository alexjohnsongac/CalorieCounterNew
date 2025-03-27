package com.example.caloriecounternew

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FoodPage : ComponentActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: Button
    private lateinit var foodList: MutableList<FoodItem>
    private lateinit var adapter: FoodAdapter
    private lateinit var streakManager: StreakManager
    private lateinit var streakTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_page)

        // Initialize StreakManager and Streak TextView
        streakManager = StreakManager(this)
        streakTextView = findViewById(R.id.streakTextView)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewFoodList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize foodList and adapter
        foodList = mutableListOf()
        adapter = FoodAdapter(foodList) { foodItem, isSelected ->
            // Show/hide the confirm button based on selection
            confirmButton.visibility = if (adapter.getSelectedItems().isNotEmpty()) View.VISIBLE else View.GONE
        }
        recyclerView.adapter = adapter

        // Initialize confirmButton
        confirmButton = findViewById(R.id.confirmButton)

        // Fetch data from Firebase
        fetchFoodData()

        // Handle confirm button click
        confirmButton.setOnClickListener {
            // Calculate total calories for selected items
            val totalCalories = adapter.getTotalCalories()
            val selectedItems = adapter.getSelectedItems().joinToString("\n") { it.itemName ?: "Unknown" }

            // Show the total calories and selected items
            Toast.makeText(this, "Total Calories: $totalCalories\nSelected Items:\n$selectedItems", Toast.LENGTH_LONG).show()

            // Update and display streak if total calories are in the valid range
            val updatedStreak = streakManager.updateStreak(totalCalories)
            streakTextView.text = "Calorie Streak: $updatedStreak days"
        }
    }

    private fun fetchFoodData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    foodList.clear()

                    snapshot.children.forEach {
                        val foodItemMap = it.value as? Map<String, Any?>
                        if (foodItemMap != null) {
                            val foodItem = FoodItem.fromSnapshot(foodItemMap)
                            foodList.add(foodItem)
                        }
                    }

                    adapter.notifyDataSetChanged()
                } else {
                    // Handle failure to load data
                    Toast.makeText(this@FoodPage, "No food data available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle Firebase error
                Toast.makeText(this@FoodPage, "Failed to load food data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
