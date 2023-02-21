package com.example.carsellingandbuyingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText

class SellCar : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_car)
    }

    fun switchToSellCar2(view: View) {
        val regText = findViewById<EditText>(R.id.editTextCarRegistration)
        val intent = Intent(this, SellCar2::class.java)
        intent.putExtra("registration", regText.text.toString())
        startActivity(intent)
    }


}