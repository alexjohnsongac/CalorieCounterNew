package com.example.caloriecounternew

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caloriecounternew.databinding.ActivityFoodPageBinding
import com.google.firebase.database.*

class FoodPage : AppCompatActivity() {

    private lateinit var binding: ActivityFoodPageBinding
    private lateinit var database: DatabaseReference
    private lateinit var foodList: MutableList<FoodItem>
    private lateinit var adapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI components
        foodList = mutableListOf()
        setupRecyclerView()
        setupSearchView()
        setupButtons()

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

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load food data: ${error.message}")
            }
        })
    }

    private fun showCustomFoodDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_food, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Create New Food Item")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val foodName = dialogView.findViewById<EditText>(R.id.editTextCustomFood).text.toString()
                val calories = dialogView.findViewById<EditText>(R.id.editTextCustomCals).text.toString()

                if (foodName.isNotEmpty() && calories.isNotEmpty() && calories.toIntOrNull() != null) {
                    saveCustomFood(foodName, calories)
                } else {
                    showToast("Please enter valid information")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun saveCustomFood(foodName: String, calories: String) {
        val newFoodRef = database.push()
        val foodItem = FoodItem(itemName = foodName, calories = calories)

        newFoodRef.setValue(foodItem)
            .addOnSuccessListener {
                showToast("Custom food added successfully!")
                fetchFoodData() // Refresh the list
            }
            .addOnFailureListener { e ->
                showToast("Failed to save: ${e.message}")
            }
    }

    private fun handleConfirmButtonClick() {
        val (itemCount, totalCalories) = adapter.addToDaily()

        if (itemCount > 0) {
            showToast("Added $itemCount items ($totalCalories calories)")
            navigateToMainActivity()
        } else {
            showToast("No items selected")
        }
    }

    private fun navigateToMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}