package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SelectEmissions : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_emissions)

        val selectedMake = intent.getStringExtra("selected_make")
        val selectedModel = intent.getStringExtra("selected_model")
        val selectedMinPrice = intent.getStringExtra("selected_minPrice")
        val selectedMaxPrice = intent.getStringExtra("selected_maxPrice")
        val selectedMinYear = intent.getStringExtra("selected_minYear")
        val selectedMaxYear = intent.getStringExtra("selected_maxYear")
        val selectedColour = intent.getStringExtra("selected_colour")
        val selectedFuelType = intent.getStringExtra("selected_fuelType")

        val minEmissions = findViewById<EditText>(R.id.minEmissions)
        val maxEmissions = findViewById<EditText>(R.id.maxEmissions)
        val setEmissionsButton = findViewById<Button>(R.id.setEmissions)

        setEmissionsButton.setOnClickListener {
            val intent = Intent(this@SelectEmissions, Search::class.java)
            intent.putExtra("selected_minEmissions", minEmissions.text.toString())
            intent.putExtra("selected_maxEmissions", maxEmissions.text.toString())
            intent.putExtra("selected_colour", selectedColour)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_minPrice", selectedMinPrice)
            intent.putExtra("selected_maxPrice", selectedMaxPrice)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_fuelType", selectedFuelType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }
}