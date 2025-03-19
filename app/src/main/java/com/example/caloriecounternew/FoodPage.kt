package com.example.caloriecounternew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.caloriecounternew.ui.theme.CalorieCounterNewTheme

class FoodPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalorieCounterNewTheme {
                FoodPageContent()
            }
        }
    }

    @Composable
    fun FoodPageContent() {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Welcome to the Food Page")
            // You can add more UI components related to food list and calorie counter here.
        }
    }
}
