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
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }
}

