package com.example.caloriecounternew

import android.os.Bundle
import android.view.View
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_page)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        //initialize RecyclerView for handling more data and food list
        recyclerView = findViewById(R.id.recyclerViewFoodList)
        confirmButton = findViewById(R.id.confirmButton)
        foodList = mutableListOf() //create foodlist
        recyclerView.layoutManager = LinearLayoutManager(this)

        //initialize adapter with selection callback
        adapter = FoodAdapter(foodList) { foodItem, isSelected ->
            //show/hide the confirm button based on selection
            confirmButton.visibility = if (adapter.getSelectedItems().isNotEmpty()) View.VISIBLE else View.GONE
        }
        recyclerView.adapter = adapter

        //fetch data from Firebase
        fetchFoodData()

        // Handle confirm button click
        confirmButton.setOnClickListener { //when confirm clicked, call functions to get total calories and display in toast
            val totalCalories = adapter.getTotalCalories()
            val selectedItems = adapter.getSelectedItems().joinToString("\n") { it.itemName ?: "Unknown" }
            Toast.makeText(this, "Total Calories: $totalCalories\nSelected Items:\n$selectedItems", Toast.LENGTH_LONG).show()
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
                } else { //if fetch fails / database issue, display no food data available
                    Toast.makeText(this@FoodPage, "No food data available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) { //call error
                Toast.makeText(this@FoodPage, "Failed to load food data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}