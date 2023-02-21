package com.example.carsellingandbuyingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.net.URLEncoder

class SellCar2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_car2)

        CoroutineScope(Dispatchers.Main).launch {
            fetchRegistrationData()
        }

    }


    private suspend fun fetchRegistrationData() {
        val gson = GsonBuilder().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://driver-vehicle-licensing.api.gov.uk/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(DVLAService::class.java)

        val intent = intent
        val reg = intent.getStringExtra("registration").toString()

        val payload = mapOf("registrationNumber" to reg)
        val apiKey = "QKL4mvJLbR2dU32AL6oRo4GAl89EZhzQ5omO6aXF"

        val vehicleData = service.getVehicleData(apiKey, payload)
        val textView : TextView = findViewById<TextView>(R.id.textView3)
        textView.text = vehicleData.toString()
    }

    fun switchToSellCar(view: View) {
        startActivity(Intent(this@SellCar2,SellCar::class.java))
    }
}