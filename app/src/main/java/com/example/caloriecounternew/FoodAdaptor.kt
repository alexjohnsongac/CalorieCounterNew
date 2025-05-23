package com.example.caloriecounternew

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(
    private var foodList: List<FoodItem>,
    private val onItemSelected: (FoodItem, Boolean) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private val originalList = mutableListOf<FoodItem>().apply { addAll(foodList) }
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

    fun selectItem(position: Int, keepExisting: Boolean = false) {
        if (!keepExisting) {
            deselectAll()
        }

        if (position in 0 until foodList.size) {
            val item = foodList[position]
            if (selectedItems.add(item)) {
                notifyItemChanged(position)
                onItemSelected(item, true)
            }
        }
    }

    fun deselectAll() {
        val previouslySelected = selectedItems.toList()
        selectedItems.clear()
        previouslySelected.forEach { item ->
            val pos = foodList.indexOfFirst { it == item }
            if (pos != -1) notifyItemChanged(pos)
        }
    }
    fun getTotalCalories(): Int {
        return selectedItems.sumOf { foodItem ->
            val caloriesString = foodItem.calories ?: "0 kcal"
            caloriesString.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
        }
    }

    fun getSelectedItems(): List<FoodItem> = selectedItems.toList()

    fun addToDaily(): Pair<Int, Int> {
        val itemsToAdd = getSelectedItems()
        val count = itemsToAdd.size
        val calories = getTotalCalories()

        itemsToAdd.forEach { item ->
            ConsumedDailyList.addFoodItem(item)
        }

        selectedItems.clear()
        notifyDataSetChanged()

        return Pair(count, calories)
    }

    fun filter(query: String) {
        foodList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { foodItem ->
                foodItem.itemName?.lowercase()?.contains(query.lowercase()) == true
            }
        }
        notifyDataSetChanged() // Important to refresh the list after filtering
    }

    fun updateOriginalList(newList: List<FoodItem>) {
        originalList.clear()
        originalList.addAll(newList)
        filter("") // Reset to show all items
    }
}