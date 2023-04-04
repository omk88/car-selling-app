package com.example.carsellingandbuyingapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class SelectColour : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_colour)

        val linearLayout = findViewById<LinearLayout>(R.id.verticalLinearLayout)

        val selectedMake = intent.getStringExtra("selected_make")
        val selectedModel = intent.getStringExtra("selected_model")
        val selectedMinPrice = intent.getStringExtra("selected_minPrice")
        val selectedMaxPrice = intent.getStringExtra("selected_maxPrice")
        val selectedMinYear = intent.getStringExtra("selected_minYear")
        val selectedMaxYear = intent.getStringExtra("selected_maxYear")
        val selectedMinEmissions = intent.getStringExtra("selected_minEmissions")
        val selectedMaxEmissions = intent.getStringExtra("selected_maxEmissions")
        val selectedFuelType = intent.getStringExtra("selected_fuelType")

        val items = resources.getStringArray(R.array.colour_items)

        for (item in items) {

            val textView = TextView(this)
            textView.text = item
            textView.textSize = 16f
            textView.setPadding(0, 20, 0, 20)
            linearLayout.addView(textView)

            textView.setOnClickListener {
                val intent = Intent(this@SelectColour, Search::class.java)
                intent.putExtra("selected_colour", textView.text.toString())
                intent.putExtra("selected_make", selectedMake)
                intent.putExtra("selected_model", selectedModel)
                intent.putExtra("selected_minPrice", selectedMinPrice)
                intent.putExtra("selected_maxPrice", selectedMaxPrice)
                intent.putExtra("selected_minYear", selectedMinYear)
                intent.putExtra("selected_maxYear", selectedMaxYear)
                intent.putExtra("selected_minEmissions", selectedMinEmissions)
                intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
                intent.putExtra("selected_fuelType", selectedFuelType)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }
}