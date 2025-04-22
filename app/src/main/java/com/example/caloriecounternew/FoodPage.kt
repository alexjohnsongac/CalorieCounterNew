package com.example.caloriecounternew

import LocalFoodManager
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
    private lateinit var localFoodManager: LocalFoodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_page)
        localFoodManager = LocalFoodManager(this)

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
                val newFoods = mutableListOf<FoodItem>()
                //testing commented out foodList.clear()

                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val foodItemMap = it.value as? Map<String, Any?>
                        foodItemMap?.let { map ->
                            newFoods.add(FoodItem.fromSnapshot(map))
                        }
                    }
                    // Add local custom foods
                    newFoods.addAll(localFoodManager.getCustomFoods())

                    // Efficient update
                    val oldSize = foodList.size
                    foodList.clear()
                    foodList.addAll(newFoods)

                    if (oldSize == 0) {
                        adapter.notifyItemRangeInserted(0, newFoods.size)
                    } else {
                        adapter.notifyItemRangeChanged(0, oldSize)
                        adapter.notifyItemRangeInserted(oldSize, newFoods.size - oldSize)
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
                Toast.makeText(this@FoodPage, "Failed to load food data", Toast.LENGTH_SHORT).show()
                // Load only local foods if Firebase fails
                foodList.clear()
                foodList.addAll(localFoodManager.getCustomFoods())
                adapter.notifyDataSetChanged()
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
        val customFood = FoodItem(
            itemName = foodName,
            calories = "$calories kcal",
            isCustom = true
        )

        // Add to beginning of list
        foodList.add(0, customFood)

        // Save to local storage
        val currentCustomFoods = localFoodManager.getCustomFoods().toMutableList()
        currentCustomFoods.add(0, customFood)
        localFoodManager.saveCustomFoods(currentCustomFoods)

        // Optimized UI updates
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)

        // Handle selection
        adapter.deselectAll()
        adapter.selectItem(0)

        confirmButton.visibility = View.VISIBLE
        Toast.makeText(this, "$foodName added to your foods", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}