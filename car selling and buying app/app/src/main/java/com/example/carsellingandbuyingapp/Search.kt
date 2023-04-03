package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class Search : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val fuelTypes = arrayOf("PETROL", "DIESEL", "HYBRID", "ELECTRICITY")
        val fuelType = findViewById<TextView>(R.id.fuelTypeText)

        var selectedMake = intent.getStringExtra("selected_make")
        var selectedModel = intent.getStringExtra("selected_model")

        var selectedMinPrice = intent.getStringExtra("selected_minPrice")
        var selectedMaxPrice = intent.getStringExtra("selected_maxPrice")

        var selectedMinYear = intent.getStringExtra("selected_minYear")
        var selectedMaxYear = intent.getStringExtra("selected_maxYear")

        var selectedMinEmissions = intent.getStringExtra("selected_minEmissions")
        var selectedMaxEmissions = intent.getStringExtra("selected_maxEmissions")

        var selectedColour = intent.getStringExtra("selected_colour")

        if (selectedColour == null) {
            selectedColour = "None"
        } else {
            intent.getStringExtra("selected_colour").toString()
        }

        if (selectedMinEmissions == null) {
            selectedMinEmissions = "None"
        } else {
            intent.getStringExtra("selected_minEmissions").toString()
        }

        if (selectedMaxEmissions == null) {
            selectedMaxEmissions = "None"
        } else {
            intent.getStringExtra("selected_maxEmissions").toString()
        }

        if (selectedMinYear == null) {
            selectedMinYear = "None"
        } else {
            intent.getStringExtra("selected_minYear").toString()
        }

        if (selectedMaxYear == null) {
            selectedMaxYear = "None"
        } else {
            intent.getStringExtra("selected_maxYear").toString()
        }

        if (selectedMinPrice == null) {
            selectedMinPrice = "None"
        } else {
            intent.getStringExtra("selected_minPrice").toString()
        }

        if (selectedMaxPrice == null) {
            selectedMaxPrice = "None"
        } else {
            intent.getStringExtra("selected_maxPrice").toString()
        }

        selectedMake = if (selectedMake == null) {
            "ALL"
        } else {
            intent.getStringExtra("selected_make").toString()
        }

        selectedModel = if (selectedModel == null) {
            "ANY"
        } else {
            intent.getStringExtra("selected_model").toString()
        }

        val selectPrice = findViewById<LinearLayout>(R.id.selectPrice)
        val selectYear = findViewById<LinearLayout>(R.id.selectAge)
        val selectEmissions = findViewById<LinearLayout>(R.id.selectEmissions)
        val selectColour = findViewById<LinearLayout>(R.id.selectColour)

        selectColour.setOnClickListener {
            startActivity(Intent(this@Search,SelectColour::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        selectEmissions.setOnClickListener {
            startActivity(Intent(this@Search,SelectEmissions::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        selectPrice.setOnClickListener {
            startActivity(Intent(this@Search,SelectPrice::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        selectYear.setOnClickListener {
            startActivity(Intent(this@Search,SelectYear::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        val selectMake = findViewById<LinearLayout>(R.id.selectMake)
        val makeText = findViewById<TextView>(R.id.makeText)
        val modelText = findViewById<TextView>(R.id.modelText)
        val minPriceText = findViewById<TextView>(R.id.priceMinText)
        val maxPriceText = findViewById<TextView>(R.id.priceMaxText)
        val minYearText = findViewById<TextView>(R.id.yearMinText)
        val maxYearText = findViewById<TextView>(R.id.yearMaxText)
        val minEmissionsText = findViewById<TextView>(R.id.emissionsMinText)
        val maxEmissionsText = findViewById<TextView>(R.id.emissionsMaxText)
        val colourText = findViewById<TextView>(R.id.colourText)

        colourText.text = selectedColour
        minEmissionsText.text = "Minimum: "+selectedMinEmissions
        maxEmissionsText.text = "Maximum: "+selectedMaxEmissions
        minPriceText.text = "Minimum: "+selectedMinPrice
        maxPriceText.text = "Maximum: "+selectedMaxPrice
        minYearText.text = "Minimum: "+selectedMinYear
        maxYearText.text = "Maximum: "+selectedMaxYear
        makeText.text = selectedMake
        modelText.text = selectedModel
        selectMake.setOnClickListener {
            startActivity(Intent(this@Search,SelectMake::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        if(makeText.text != "ALL") {
            val selectModel = findViewById<LinearLayout>(R.id.selectModel)
            selectModel.setOnClickListener {
                val intent = Intent(this@Search, SelectModel::class.java)
                intent.putExtra("selected_make", selectedMake)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        fuelType.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose fuel type")

            builder.setSingleChoiceItems(fuelTypes, -1) { dialog, which ->
                fuelType.text = fuelTypes[which]
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
}