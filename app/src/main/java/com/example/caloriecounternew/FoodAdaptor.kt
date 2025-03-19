package com.example.caloriecounternew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for the FoodItems in the RecyclerView
class FoodAdapter(private val foodList: List<FoodItem>) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    // ViewHolder for binding food item data to views
    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodNameTextView: TextView = itemView.findViewById(R.id.itemName)  // Use correct ID from item_food.xml
        val caloriesTextView: TextView = itemView.findViewById(R.id.calories)  // Use correct ID from item_food.xml
    }

    // Create a new ViewHolder and inflate the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    // Bind the data to the views in the ViewHolder
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.foodNameTextView.text = foodItem.itemName  // Set the food name
        holder.caloriesTextView.text = "Calories: ${foodItem.calories}"  // Set the calories text
    }

    // Return the total number of items in the food list
    override fun getItemCount(): Int {
        return foodList.size
    }
}
