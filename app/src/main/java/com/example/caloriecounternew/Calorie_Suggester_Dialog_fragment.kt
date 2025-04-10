package com.example.caloriecounternew

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

class CalorieSuggestDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_calorie_suggest, null)

        builder.setView(view)
            .setTitle("Suggest Calorie Goal")
            .setPositiveButton("Calculate") { _, _ ->
                val weight = view.findViewById<EditText>(R.id.weight_input).text.toString().toDouble()
                val height = view.findViewById<EditText>(R.id.height_input).text.toString().toDouble()
                val sex = view.findViewById<RadioGroup>(R.id.sex_input).checkedRadioButtonId
                val calories = calculateCalories(weight, height, sex)
                // return result via callback or ViewModel
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        return builder.create()
    }
}
