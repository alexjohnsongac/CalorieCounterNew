package com.example.caloriecounternew

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
    private lateinit var confirmButton: Button
    private lateinit var foodList: MutableList<FoodItem>
    private lateinit var adapter: FoodAdapter
    private lateinit var customButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_page)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerViewFoodList)
        confirmButton = findViewById(R.id.confirmButton)
        customButton = findViewById(R.id.buttonAddCustom)
        foodList = mutableListOf()
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter
        adapter = FoodAdapter(foodList) { _, _ ->
            // Show/hide confirm button based on selection
            confirmButton.visibility =
                if (adapter.getSelectedItems().isNotEmpty()) View.VISIBLE else View.GONE
        }
        recyclerView.adapter = adapter

        // Fetch food data
        fetchFoodData()

        // Set up confirm button
        confirmButton.setOnClickListener {
            val (itemCount, totalCalories) = adapter.addToDaily()

            if (itemCount > 0) {
                Toast.makeText(
                    this,
                    "Added $itemCount items ($totalCalories calories)",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(
                    this,
                    "No items selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        customButton.setOnClickListener {
            showCustomFoodDialog()
        }
    }

        private fun fetchFoodData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodList.clear()

                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val foodItemMap = it.value as? Map<String, Any?>
                        foodItemMap?.let { map ->
                            foodList.add(FoodItem.fromSnapshot(map))
                        }
                    }
                } else {
                    Toast.makeText(
                        this@FoodPage,
                        "No food data available",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@FoodPage,
                    "Failed to load food data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    private fun showCustomFoodDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_food, null)
        val editFoodName = dialogView.findViewById<EditText>(R.id.editTextCustomFood)
        val editCustomCals = dialogView.findViewById<EditText>(R.id.editTextCustomCals)

        AlertDialog.Builder(this)
            .setTitle("Create New Food Item")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val foodName = editFoodName.text.toString().trim()
                val calories = editCustomCals.text.toString().trim()

                if (foodName.isNotEmpty() && calories.isNotEmpty() && calories.toIntOrNull() != null) {
                    saveCustomFood(foodName, calories.toInt())
                    Toast.makeText(this, "$foodName added to list", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter valid name and calories", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCustomFood(foodName: String, calories: Int) {
        // Create minimal FoodItem with just name and calories
        val customFood = FoodItem(
            itemName = foodName,
            calories = "$calories kcal",
            isCustom = true
        )

        // Add to beginning of list
        foodList.add(0, customFood)

        // Update adapter
        adapter.notifyItemInserted(0)

        // Clear previous selections and select new item
        adapter.deselectAll()
        adapter.selectItem(customFood)

        // Scroll to show new item and enable confirm button
        recyclerView.scrollToPosition(0)
        confirmButton.visibility = View.VISIBLE
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}