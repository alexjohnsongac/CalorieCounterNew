package com.example.caloriecounternew

import FoodItem
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FoodPage : ComponentActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var foodList: MutableList<FoodItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_page)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView and the food list
        recyclerView = findViewById(R.id.recyclerViewFoodList)
        foodList = mutableListOf()

        // Set up the RecyclerView with a linear layout manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from Firebase
        fetchFoodData()
    }

    private fun fetchFoodData() {
        // Listen for changes in the 'foodItems' node in Firebase
        database.child("foodItems").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    foodList.clear() // Clear any previous data

                    // Loop through each child (food items)
                    snapshot.children.forEach {
                        // Get the FoodItem object
                        val foodItem = it.getValue(FoodItem::class.java)

                        // Ensure the foodItem is not null
                        if (foodItem != null) {
                            // Convert calories string to int using the method you created
                            val caloriesStr = it.child("calories").getValue(String::class.java)
                            foodItem.setCaloriesFromString(caloriesStr ?: "")

                            // Add the food item to the list
                            foodList.add(foodItem)
                        }
                    }

                    // Set up the adapter after data is fetched
                    val adapter = FoodAdapter(foodList)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@FoodPage, "No food data available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodPage, "Failed to load food data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
