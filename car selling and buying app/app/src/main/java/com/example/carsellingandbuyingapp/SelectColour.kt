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
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }
}