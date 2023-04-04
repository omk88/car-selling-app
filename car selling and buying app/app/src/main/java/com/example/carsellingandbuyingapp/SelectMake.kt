package com.example.carsellingandbuyingapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class SelectMake : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_make)

        val selectedModel = intent.getStringExtra("selected_model")
        val selectedMinPrice = intent.getStringExtra("selected_minPrice")
        val selectedMaxPrice = intent.getStringExtra("selected_maxPrice")
        val selectedMinYear = intent.getStringExtra("selected_minYear")
        val selectedMaxYear = intent.getStringExtra("selected_maxYear")
        val selectedMinEmissions = intent.getStringExtra("selected_minEmissions")
        val selectedMaxEmissions = intent.getStringExtra("selected_maxEmissions")
        val selectedColour = intent.getStringExtra("selected_colour")
        val selectedFuelType = intent.getStringExtra("selected_fuelType")

        val linearLayout = findViewById<LinearLayout>(R.id.verticalLinearLayout)

        val items = resources.getStringArray(R.array.make_items)

        var previousFirstChar: Char? = null
        for (item in items) {
            val currentFirstChar = item[0].toUpperCase()

            if (previousFirstChar == null || currentFirstChar != previousFirstChar) {
                val divider = TextView(this)
                divider.text = currentFirstChar.toString()
                divider.textSize = 22f
                divider.setTextColor(Color.parseColor("#9A9A9A"))
                divider.setPadding(30, 7, 0, 7)

                divider.setBackgroundResource(R.drawable.divider_background)

                linearLayout.addView(divider)
            }

            val textView = TextView(this)
            textView.text = item
            textView.textSize = 16f
            textView.setPadding(0, 20, 0, 20)
            linearLayout.addView(textView)

            previousFirstChar = currentFirstChar

            textView.setOnClickListener {
                val intent = Intent(this@SelectMake, Search::class.java)
                intent.putExtra("selected_make", textView.text.toString())
                intent.putExtra("selected_minEmissions", selectedMinEmissions)
                intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
                intent.putExtra("selected_colour", selectedColour)
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
}

