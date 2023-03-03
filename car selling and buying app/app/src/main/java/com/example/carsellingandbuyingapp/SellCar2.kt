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
    lateinit var vehicleData: VehicleData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_car2)

        CoroutineScope(Dispatchers.Main).launch {
            vehicleData = fetchRegistrationData()
        }

    }


    private suspend fun fetchRegistrationData(): VehicleData {
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

        vehicleData = service.getVehicleData(apiKey, payload)
        val textView : TextView = findViewById(R.id.textView3)
        textView.text = vehicleData.toString()

        return vehicleData

    }

    fun switchToSellCar(view: View) {
        startActivity(Intent(this@SellCar2,SellCar::class.java))
    }

    fun switchToSellCar3(view: View) {
        val intent = Intent(this, SellCar3::class.java)
        intent.putExtra("registration", vehicleData.registrationNumber)
        intent.putExtra("make", vehicleData.make)
        intent.putExtra("colour", vehicleData.colour)
        intent.putExtra("fuelType", vehicleData.fuelType)
        intent.putExtra("registrationYear", vehicleData.registrationYear)
        intent.putExtra("taxDueDate", vehicleData.taxDueDate)
        startActivity(intent)
    }
}