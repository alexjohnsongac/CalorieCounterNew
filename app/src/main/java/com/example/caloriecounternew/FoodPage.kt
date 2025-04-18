package com.example.caloriecounternew

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class FoodPage : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: Button
    private lateinit var foodList: MutableList<FoodItem>
    private lateinit var adapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_page)

        database = FirebaseDatabase.getInstance().reference
        recyclerView = findViewById(R.id.recyclerViewFoodList)
        confirmButton = findViewById(R.id.confirmButton)

        foodList = mutableListOf()
        adapter = FoodAdapter(foodList) { _, _ -> toggleConfirmButtonVisibility() }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setUpSearchView()
        fetchFoodData()

        confirmButton.setOnClickListener { handleConfirmButtonClick() }
    }

    private fun setUpSearchView() {
        val searchView = findViewById<SearchView>(R.id.searchView).apply {
            setIconifiedByDefault(false)
            queryHint = "Search food items"
            clearFocus()

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter(newText?.trim() ?: "")
                    return true
                }
            })
        }
    }

    private fun toggleConfirmButtonVisibility() {
        confirmButton.visibility = if (adapter.getSelectedItems().isNotEmpty()) {
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
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this@FoodPage, message, Toast.LENGTH_SHORT).show()
    }
}