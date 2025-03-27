package com.example.caloriecounternew

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(
    private val foodList: List<FoodItem>, //list of fooditems
    private val onItemSelected: (FoodItem, Boolean) -> Unit //for selecting items, would check for isselected, ex: (Fooditem, true)
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    //track selected items
    private val selectedItems = mutableSetOf<FoodItem>()

    //viewHolder for binding food item data to views
    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //this is where we initialize what to bind and display on the foodpage for each item in the recycleview
        val foodNameTextView: TextView = itemView.findViewById(R.id.itemName)
        val caloriesTextView: TextView = itemView.findViewById(R.id.calories)
    }

    //create a new ViewHolder and inflate the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    // Bind the data to the views in the ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position] //setup where everything is displayed by binding each item to position

        //set the food name and calories
        holder.foodNameTextView.text = foodItem.itemName ?: "Unknown"
        holder.caloriesTextView.text = "Calories: ${foodItem.calories ?: "0"}" //display calories underneith our fooditem

        //highlight selected items
        if (selectedItems.contains(foodItem)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.grey))
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
        }

        //handle item clicks
        holder.itemView.setOnClickListener {
            val isSelected = selectedItems.contains(foodItem)
            if (isSelected) {
                selectedItems.remove(foodItem) // Deselect
            } else {
                selectedItems.add(foodItem) // Select
            }
            notifyItemChanged(position) // Update the UI
            onItemSelected(foodItem, !isSelected) // Notify the activity
        }
    }

    //get total number of items in the food list
    override fun getItemCount(): Int {
        return foodList.size
    }

    //get the total calories of selected items and also parses it since its string
    fun getTotalCalories(): Int {
        return selectedItems.sumOf { foodItem ->
            //extract the numeric part of the calories string (e.g., "290 kcal" -> 290)
            val caloriesString = foodItem.calories ?: "0 kcal"
            val caloriesValue = caloriesString.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
            caloriesValue
        }
    }

    //Get the list of selected items
    //to use later if we need
    fun getSelectedItems(): List<FoodItem> {
        return selectedItems.toList()
    }
}