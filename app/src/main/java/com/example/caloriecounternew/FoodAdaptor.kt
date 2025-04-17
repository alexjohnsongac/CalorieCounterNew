package com.example.caloriecounternew

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(private var foodList: List<FoodItem>,
    private val onItemSelected: (FoodItem, Boolean) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private var originalList = foodList.toMutableList()

    // Track selected items
    private val selectedItems = mutableSetOf<FoodItem>()

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodNameTextView: TextView = itemView.findViewById(R.id.itemName)
        val caloriesTextView: TextView = itemView.findViewById(R.id.calories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]

        holder.foodNameTextView.text = foodItem.itemName ?: "Unknown"
        holder.caloriesTextView.text = "Calories: ${foodItem.calories ?: "0"}"

        // Highlight selected items
        if (selectedItems.contains(foodItem)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.grey))
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.transparent))
        }

        holder.itemView.setOnClickListener {
            val isSelected = selectedItems.contains(foodItem)
            if (isSelected) {
                selectedItems.remove(foodItem)
            } else {
                selectedItems.add(foodItem)
            }
            notifyItemChanged(position)
            onItemSelected(foodItem, !isSelected)
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun getTotalCalories(): Int {
        return selectedItems.sumOf { foodItem ->
            val caloriesString = foodItem.calories ?: "0 kcal"
            caloriesString.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
        }
    }

    fun getSelectedItems(): List<FoodItem> = selectedItems.toList()

    /**
     * Adds selected items to daily consumption list
     * @return Pair of (number of items added, total calories added)
     */
    fun addToDaily(): Pair<Int, Int> {
        val itemsToAdd = getSelectedItems()
        val count = itemsToAdd.size
        val calories = getTotalCalories()

        itemsToAdd.forEach { item ->
            ConsumedDailyList.addFoodItem(item)
        }

        // Clear selection after adding
        selectedItems.clear()
        notifyDataSetChanged()

        return Pair(count, calories)
    }

    fun filter(query: String) {
        foodList = if (query.isBlank()) {
            originalList
        } else {
            originalList.filter {
                it.itemName?.contains(query, ignoreCase = true) == true
            }
        }

        notifyDataSetChanged()
    }
}