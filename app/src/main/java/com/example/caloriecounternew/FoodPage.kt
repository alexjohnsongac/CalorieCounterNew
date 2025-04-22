package com.example.caloriecounternew

import LocalFoodManager
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caloriecounternew.databinding.ActivityFoodPageBinding
import com.google.firebase.database.*

class FoodPage : ComponentActivity() {

    private lateinit var binding: ActivityFoodPageBinding
    private lateinit var database: DatabaseReference
    private lateinit var foodList: MutableList<FoodItem>
    private lateinit var adapter: FoodAdapter
    private lateinit var localFoodManager: LocalFoodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize LocalFoodManager with your existing implementation
        localFoodManager = LocalFoodManager(this)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI components
        foodList = mutableListOf()
        adapter = FoodAdapter(foodList) { _, _ -> toggleConfirmButtonVisibility() }

        setupRecyclerView()
        setupSearchView()
        setupButtons()

        fetchFoodData()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewFoodList.apply {
            layoutManager = LinearLayoutManager(this@FoodPage)
            adapter = this@FoodPage.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setIconifiedByDefault(false)
            queryHint = "Search food items"

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter(newText?.trim() ?: "")
                    return true
                }
            })
        }
    }

    private fun setupButtons() {
        binding.confirmButton.setOnClickListener { handleConfirmButtonClick() }
        binding.buttonAddCustom.setOnClickListener { showCustomFoodDialog() }
    }

    private fun toggleConfirmButtonVisibility() {
        binding.confirmButton.visibility = if (adapter.getSelectedItems().isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun fetchFoodData() {
        // Start with local custom foods
        val combinedList = localFoodManager.getCustomFoods().toMutableList()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val foodItemMap = it.value as? Map<String, Any?>
                        foodItemMap?.let { map ->
                            FoodItem.fromSnapshot(map)?.let { foodItem ->
                                // Avoid duplicates by checking if item already exists
                                if (!combinedList.any { it.itemName == foodItem.itemName }) {
                                    combinedList.add(foodItem)
                                }
                            }
                        }
                    }
                }
                updateFoodList(combinedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodPage, "Failed to load food data", Toast.LENGTH_SHORT).show()
                updateFoodList(combinedList) // Still show local foods
            }
        })
    }

    private fun updateFoodList(newList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newList)
        adapter.updateOriginalList(newList)
        adapter.notifyDataSetChanged()
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

        // Get current custom foods, add new one, and save
        val currentCustomFoods = localFoodManager.getCustomFoods().toMutableList()
        currentCustomFoods.add(0, customFood) // Add to beginning
        localFoodManager.saveCustomFoods(currentCustomFoods)

        // Update the displayed list
        foodList.add(0, customFood)
        adapter.updateOriginalList(foodList)
        adapter.notifyItemInserted(0)
        binding.recyclerViewFoodList.scrollToPosition(0)

        // Select the new item
        adapter.selectItem(0, keepExisting = true)
        toggleConfirmButtonVisibility()

        Toast.makeText(this, "$foodName added to your foods", Toast.LENGTH_SHORT).show()
    }

    private fun handleConfirmButtonClick() {
        val selectedItems = adapter.getSelectedItems()
        if (selectedItems.isNotEmpty()) {
            // Calculate total calories and prepare food names
            val totalCalories = selectedItems.sumOf { foodItem ->
                foodItem.calories?.replace(" kcal", "")?.toIntOrNull() ?: 0
            }

            val foodNames = selectedItems.joinToString(", ") { it.itemName ?: "" }

            // Return data to MainActivity
            val resultIntent = Intent().apply {
                putExtra("total_calories", totalCalories)
                putExtra("food_names", foodNames)
                putExtra("food_items", ArrayList(selectedItems))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}