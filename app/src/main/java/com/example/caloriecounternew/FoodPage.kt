package com.example.caloriecounternew

import LocalFoodManager
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caloriecounternew.databinding.ActivityFoodPageBinding
import com.google.firebase.database.*

class FoodPage : ComponentActivity() {

    private lateinit var binding: ActivityFoodPageBinding
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: Button
    private lateinit var foodList: MutableList<FoodItem>
    private lateinit var adapter: FoodAdapter
    private lateinit var customButton: Button
    private lateinit var localFoodManager: LocalFoodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_food_page)
        localFoodManager = LocalFoodManager(this)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI components
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
    }

    private fun setupRecyclerView() {
        adapter = FoodAdapter(foodList) { _, _ -> toggleConfirmButtonVisibility() }
        binding.recyclerViewFoodList.apply {
            layoutManager = LinearLayoutManager(this@FoodPage)
            adapter = this@FoodPage.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setIconifiedByDefault(false)
            queryHint = "Search food items"
            clearFocus()

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
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<FoodItem>()

                if (snapshot.exists()) {
                    snapshot.children.forEach { data ->
                        val foodItemMap = data.value as? Map<String, Any?>
                        foodItemMap?.let { map ->
                            FoodItem.fromSnapshot(map)?.let { foodItem ->
                                tempList.add(foodItem)
                            }
                        }
                    }
                    foodList.clear()
                    foodList.addAll(tempList)
                    adapter.updateOriginalList(tempList)
                } else {
                    showToast("No food data available")
                }
            }
        }
        customButton.setOnClickListener {
            showCustomFoodDialog()
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
                            combinedList.add(FoodItem.fromSnapshot(map))
                        }
                    }
                    updateFoodList(combinedList)
                } else {
                    Toast.makeText(
                        this@FoodPage,
                        "No food data available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@FoodPage, "Failed to load food data", Toast.LENGTH_SHORT).show()
                    updateFoodList(combinedList) // Still show local foods
                }
            })
        }

    private fun updateFoodList(newList: List<FoodItem>) {
        val oldSize = foodList.size
        foodList.clear()
        foodList.addAll(newList)

        if (oldSize == 0) {
            adapter.notifyItemRangeInserted(0, newList.size)
        } else {
            val changedCount = minOf(oldSize, newList.size)
            adapter.notifyItemRangeChanged(0, changedCount)
            if (newList.size > oldSize) {
                adapter.notifyItemRangeInserted(oldSize, newList.size - oldSize)
            } else if (newList.size < oldSize) {
                adapter.notifyItemRangeRemoved(newList.size, oldSize - newList.size)
            }
        }
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

        // Remember currently selected items
        val previouslySelected = adapter.getSelectedItems()

        // Add to beginning of list
        foodList.add(0, customFood)

        // Save to local storage (new foods always added to start)
        val currentCustomFoods = localFoodManager.getCustomFoods().toMutableList()
        currentCustomFoods.add(0, customFood)
        localFoodManager.saveCustomFoods(currentCustomFoods)

        // Optimized UI updates
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)

        // Restore previous selections and select new item
        adapter.selectItem(0, keepExisting = true)

        confirmButton.visibility = View.VISIBLE
        Toast.makeText(this, "$foodName added to your foods", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}