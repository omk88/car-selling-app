package com.example.carsellingandbuyingapp

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class SelectYear : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_year)

        val selectedMake = intent.getStringExtra("selected_make")
        val selectedModel = intent.getStringExtra("selected_model")
        val selectedMinPrice = intent.getStringExtra("selected_minPrice")
        val selectedMaxPrice = intent.getStringExtra("selected_maxPrice")
        val selectedColour = intent.getStringExtra("selected_colour")
        val selectedMinEmissions = intent.getStringExtra("selected_minEmissions")
        val selectedMaxEmissions = intent.getStringExtra("selected_maxEmissions")
        val selectedFuelType = intent.getStringExtra("selected_fuelType")


        val setYearButton = findViewById<Button>(R.id.setYears)

        val linearLayout = findViewById<LinearLayout>(R.id.verticalLinearLayout)

        val items = resources.getStringArray(R.array.year_items)

        var previousThirdChar: String? = null
        var previouslySelectedTextView: TextView? = null

        var selectedMinYear = ""
        var selectedMaxYear = ""

        for (item in items) {
            val currentThirdChar = item[2].toUpperCase()+"0s"

            if (previousThirdChar == null || currentThirdChar != previousThirdChar) {
                val divider = TextView(this)
                divider.text = currentThirdChar.toString()
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

            previousThirdChar = currentThirdChar

            textView.setOnClickListener {
                selectedMinYear = (textView.text as String?).toString()
                if (previouslySelectedTextView != null && previouslySelectedTextView != textView) {
                    val fadeOut = ObjectAnimator.ofInt(
                        previouslySelectedTextView?.background,
                        "alpha",
                        255,
                        0
                    )
                    fadeOut.duration = 150
                    fadeOut.start()
                }

                textView.background = ContextCompat.getDrawable(this, R.drawable.selected_background)

                if (previouslySelectedTextView != textView) {
                    textView.background.alpha = 0
                    val fadeIn = ObjectAnimator.ofInt(
                        textView.background,
                        "alpha",
                        0,
                        255
                    )
                    fadeIn.duration = 150
                    fadeIn.start()
                }

                previouslySelectedTextView = textView
            }
        }

        val linearLayout2 = findViewById<LinearLayout>(R.id.verticalLinearLayout2)

        var previousThirdChar2: String? = null
        var previouslySelectedTextView2: TextView? = null

        for (item in items) {
            val currentThirdChar2 = item[2].toUpperCase() + "0s"

            if (previousThirdChar2 == null || currentThirdChar2 != previousThirdChar2) {
                val divider = TextView(this)
                divider.text = currentThirdChar2.toString()
                divider.textSize = 22f
                divider.setTextColor(Color.parseColor("#9A9A9A"))
                divider.setPadding(30, 7, 0, 7)

                divider.setBackgroundResource(R.drawable.divider_background)

                linearLayout2.addView(divider)
            }

            val textView = TextView(this)
            textView.text = item
            textView.textSize = 16f
            textView.setPadding(0, 20, 0, 20)
            linearLayout2.addView(textView)

            previousThirdChar2 = currentThirdChar2

            textView.setOnClickListener {
                selectedMaxYear = (textView.text as String?).toString()
                if (previouslySelectedTextView2 != null && previouslySelectedTextView2 != textView) {
                    val fadeOut = ObjectAnimator.ofInt(
                        previouslySelectedTextView2?.background,
                        "alpha",
                        255,
                        0
                    )
                    fadeOut.duration = 150
                    fadeOut.start()
                }

                textView.background = ContextCompat.getDrawable(this, R.drawable.selected_background)

                if (previouslySelectedTextView2 != textView) {
                    textView.background.alpha = 0
                    val fadeIn = ObjectAnimator.ofInt(
                        textView.background,
                        "alpha",
                        0,
                        255
                    )
                    fadeIn.duration = 150
                    fadeIn.start()
                }

                previouslySelectedTextView2 = textView
            }
        }

        setYearButton.setOnClickListener {
            val intent = Intent(this@SelectYear, Search::class.java)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_minPrice", selectedMinPrice)
            intent.putExtra("selected_maxPrice", selectedMaxPrice)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_minEmissions", selectedMinEmissions)
            intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            intent.putExtra("selected_colour", selectedColour)
            intent.putExtra("selected_fuelType", selectedFuelType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}