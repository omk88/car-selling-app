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

        val minEmissions = findViewById<EditText>(R.id.minEmissions)
        val maxEmissions = findViewById<EditText>(R.id.maxEmissions)
        val setEmissionsButton = findViewById<Button>(R.id.setEmissions)

        setEmissionsButton.setOnClickListener {
            val intent = Intent(this@SelectEmissions, Search::class.java)
            intent.putExtra("selected_minEmissions", minEmissions.text.toString())
            intent.putExtra("selected_maxEmissions", maxEmissions.text.toString())
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }
}