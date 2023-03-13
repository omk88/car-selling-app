package com.example.carsellingandbuyingapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CarSalePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_sale_page)

        val textView = findViewById<TextView>(R.id.textView2)
        val carData = intent.getStringExtra("carData").toString()
        val carDataArray = carData.split(",")
        println("FFFF"+(carDataArray[4].split("="))[1])
        textView.setText((carDataArray[4].split("="))[1]+" ")
    }
}