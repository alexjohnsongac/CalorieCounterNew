package com.example.caloriecounternew

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
    private lateinit var foodList: MutableList<FoodItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_page)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize RecyclerView and food list
        recyclerView = findViewById(R.id.recyclerViewFoodList)
        foodList = mutableListOf()
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from Firebase
        fetchFoodData()
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
                            Log.d("FoodItem", "Added: ${foodItem.itemName} - ${foodItem.calories}")
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
                Log.e("FirebaseError", error.message)
            }
        })
    }
}