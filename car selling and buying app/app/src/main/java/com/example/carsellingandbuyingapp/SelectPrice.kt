package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SelectPrice : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_price)

        val minPrice = findViewById<EditText>(R.id.minPrice)
        val maxPrice = findViewById<EditText>(R.id.maxPrice)
        val setPriceButton = findViewById<Button>(R.id.setPrice)

        setPriceButton.setOnClickListener {
            val intent = Intent(this@SelectPrice, Search::class.java)
            intent.putExtra("selected_minPrice", minPrice.text.toString())
            intent.putExtra("selected_maxPrice", maxPrice.text.toString())
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        minPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) == "£" && count == 1 && after == 0) {
                    minPrice.setText("£")
                    minPrice.setSelection(minPrice.text.length)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) != "£") {
                    minPrice.setText("£" + s.toString())
                    minPrice.setSelection(minPrice.text.length)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        maxPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) == "£" && count == 1 && after == 0) {
                    maxPrice.setText("£")
                    maxPrice.setSelection(minPrice.text.length)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) != "£") {
                    maxPrice.setText("£" + s.toString())
                    maxPrice.setSelection(maxPrice.text.length)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }
}