package com.example.caloriecounternew

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment

class CalorieSuggestDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_calorie_suggester_dialog_fragment, null)

        val builder =   AlertDialog.Builder(requireContext())
        builder.setView(view)
            .setTitle("Suggest Calorie Goal")
            .setPositiveButton("Calculate") { _, _ ->
                val weight = view.findViewById<EditText>(R.id.weight_input).text.toString().toDouble()
                val height = view.findViewById<EditText>(R.id.height_input).text.toString().toDouble()
                val sex = view.findViewById<RadioGroup>(R.id.sex_input).checkedRadioButtonId
                val calories = calculateCalories(weight, height, sex)
                // TODO: return result via callback or ViewModel
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        return builder.create()
    }

    private fun calculateCalories(weight: Double, height: Double, sexId: Int): Int {
        // TODO: Implement calorie calculation formula
        return 2000 // placeholder - adjust based on goal (!?)
    }
}
