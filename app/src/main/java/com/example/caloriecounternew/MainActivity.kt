package com.example.caloriecounternew

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the XML layout
        setContentView(R.layout.activity_main)

        val googleAuthClient = GoogleAuthClient(this)

        val signInButton: Button = findViewById(R.id.buttonSignIn)
        val signOutButton: Button = findViewById(R.id.buttonSignOut)
        val goToFoodPageButton: Button = findViewById(R.id.buttonGoToFoodPage)

        val eggView: ImageView = findViewById(R.id.pixelEggView)
        val firstView: ImageView = findViewById(R.id.pixelFirstView)
        val secondView: ImageView = findViewById(R.id.pixelSecondView)


        // Check if the user is signed in
        if (googleAuthClient.isSingedIn()) {
            signInButton.visibility = Button.GONE
            signOutButton.visibility = Button.VISIBLE
            goToFoodPageButton.visibility = Button.VISIBLE
            eggView.visibility = ImageView.VISIBLE
        } else {
            signInButton.visibility = Button.VISIBLE
            signOutButton.visibility = Button.GONE
        }

        // Sign-In button click listener
        signInButton.setOnClickListener {
            lifecycleScope.launch {
                // Use launch to start a coroutine and call the suspend function
                googleAuthClient.signIn()
                if(googleAuthClient.isSingedIn()) {
                    signInButton.visibility = Button.GONE
                    signOutButton.visibility = Button.VISIBLE
                    goToFoodPageButton.visibility = Button.VISIBLE
                    eggView.visibility = ImageView.VISIBLE
                }
            }
        }

        // Sign-Out button click listener
        signOutButton.setOnClickListener {
            lifecycleScope.launch {
                // Call the suspend function inside a coroutine
                googleAuthClient.signOut()
                signInButton.visibility = Button.VISIBLE
                signOutButton.visibility = Button.GONE
                goToFoodPageButton.visibility = Button.GONE
                eggView.visibility = ImageView.GONE
            }
        }

        // Go to FoodPage button click listener
        goToFoodPageButton.setOnClickListener {
            navigateToFoodPage()
        }
    }

    // Navigate to FoodPage
    private fun navigateToFoodPage() {
        val intent = Intent(this, FoodPage::class.java)
        startActivity(intent) //go food
    }
}
