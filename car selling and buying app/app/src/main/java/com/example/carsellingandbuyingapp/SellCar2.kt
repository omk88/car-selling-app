package com.example.carsellingandbuyingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.net.URLEncoder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class SellCar2 : AppCompatActivity() {
    lateinit var vehicleData: VehicleData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_car2)

        CoroutineScope(Dispatchers.Main).launch {
            vehicleData = fetchRegistrationData().await()
            updateTextView(vehicleData)
        }
    }

    private fun updateTextView(vehicleData: VehicleData) {
        val textView: TextView = findViewById(R.id.textView3)

        textView.text = "Registration: " + vehicleData.registrationNumber + "\nMake: " + vehicleData.make +
                "\nColour: " + vehicleData.colour + "\nFuel Type " + vehicleData.fuelType +
                "\nTax Due Date: " + vehicleData.taxDueDate + "\nYear Of Manufacture: " +
                vehicleData.yearOfManufacture
    }


    private suspend fun fetchRegistrationData(): Deferred<VehicleData> = coroutineScope {
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

        val vehicleDataDeferred = async { service.getVehicleData(apiKey, payload) }
        vehicleDataDeferred

    }

    fun switchToSellCar(view: View) {
        startActivity(Intent(this@SellCar2,SellCar::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun switchToSellCar3(view: View) {
        val mileage = intent.getStringExtra("mileage").toString()
        val model = intent.getStringExtra("model").toString()
        val price = intent.getStringExtra("price").toString()
        val username = intent.getStringExtra("username").toString()
        val condition = intent.getStringExtra("condition").toString()

        val intent = Intent(this, SellCar3::class.java)
        intent.putExtra("registration", vehicleData.registrationNumber)
        intent.putExtra("make", vehicleData.make)
        intent.putExtra("colour", vehicleData.colour)
        intent.putExtra("fuelType", vehicleData.fuelType)
        intent.putExtra("registrationYear", vehicleData.registrationYear)
        intent.putExtra("taxDueDate", vehicleData.taxDueDate)
        intent.putExtra("mileage", mileage)
        intent.putExtra("yearOfManufacture", vehicleData.yearOfManufacture)
        intent.putExtra("co2Emissions", vehicleData.co2Emissions)
        intent.putExtra("engineCapacity", vehicleData.engineCapacity)
        intent.putExtra("model", model)
        intent.putExtra("price", price)
        intent.putExtra("username", username)
        intent.putExtra("condition", condition)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}