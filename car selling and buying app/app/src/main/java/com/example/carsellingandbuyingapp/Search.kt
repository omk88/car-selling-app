package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class Search : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        val fuelTypes = arrayOf("ALL", "PETROL", "DIESEL", "HYBRID", "ELECTRICITY")
        val fuelType = findViewById<TextView>(R.id.fuelTypeText)

        var selectedMake = intent.getStringExtra("selected_make")
        var selectedModel = intent.getStringExtra("selected_model")

        var selectedMinPrice = intent.getStringExtra("selected_minPrice")
        var selectedMaxPrice = intent.getStringExtra("selected_maxPrice")

        var selectedMinYear = intent.getStringExtra("selected_minYear")
        var selectedMaxYear = intent.getStringExtra("selected_maxYear")

        var selectedMinEmissions = intent.getStringExtra("selected_minEmissions")
        var selectedMaxEmissions = intent.getStringExtra("selected_maxEmissions")

        var selectedFuelType = intent.getStringExtra("selected_fuelType")

        var selectedColour = intent.getStringExtra("selected_colour")

        val closeSearch = findViewById<LinearLayout>(R.id.close)


        closeSearch.setOnClickListener {
            startActivity(Intent(this@Search,MainPage::class.java))
            overridePendingTransition(0, R.anim.slide_out_down)
        }

        if (selectedFuelType == null) {
            selectedFuelType = "ALL"
        } else {
            selectedFuelType = intent.getStringExtra("selected_fuelType")
        }

        fuelType.text = selectedFuelType

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

        val searchButton = findViewById<Button>(R.id.searchButton)
        searchButton.setOnClickListener {
            val mainPageIntent = Intent(this@Search, MainPage::class.java)
            mainPageIntent.putExtra("selected_make", selectedMake)
            mainPageIntent.putExtra("selected_model", selectedModel)
            mainPageIntent.putExtra("selected_minPrice", selectedMinPrice)
            mainPageIntent.putExtra("selected_maxPrice", selectedMaxPrice)
            mainPageIntent.putExtra("selected_minYear", selectedMinYear)
            mainPageIntent.putExtra("selected_maxYear", selectedMaxYear)
            mainPageIntent.putExtra("selected_fuelType", fuelType.text)
            mainPageIntent.putExtra("selected_minEmissions", selectedMinEmissions)
            mainPageIntent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            mainPageIntent.putExtra("selected_colour", selectedColour)
            startActivity(mainPageIntent)
            overridePendingTransition(0, R.anim.slide_out_down)
        }

        val selectPrice = findViewById<LinearLayout>(R.id.selectPrice)
        val selectYear = findViewById<LinearLayout>(R.id.selectAge)
        val selectEmissions = findViewById<LinearLayout>(R.id.selectEmissions)
        val selectColour = findViewById<LinearLayout>(R.id.selectColour)

        selectColour.setOnClickListener {
            val intent = Intent(this@Search, SelectColour::class.java)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_minPrice", selectedMinPrice)
            intent.putExtra("selected_maxPrice", selectedMaxPrice)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_minEmissions", selectedMinEmissions)
            intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            intent.putExtra("selected_colour", selectedColour)
            intent.putExtra("selected_fuelType", fuelType.text)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        selectEmissions.setOnClickListener {
            val intent = Intent(this@Search, SelectEmissions::class.java)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_minPrice", selectedMinPrice)
            intent.putExtra("selected_maxPrice", selectedMaxPrice)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_minEmissions", selectedMinEmissions)
            intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            intent.putExtra("selected_colour", selectedColour)
            intent.putExtra("selected_fuelType", fuelType.text)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        selectPrice.setOnClickListener {
            val intent = Intent(this@Search, SelectPrice::class.java)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_minPrice", selectedMinPrice)
            intent.putExtra("selected_maxPrice", selectedMaxPrice)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_minEmissions", selectedMinEmissions)
            intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            intent.putExtra("selected_colour", selectedColour)
            intent.putExtra("selected_fuelType", fuelType.text)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        selectYear.setOnClickListener {
            val intent = Intent(this@Search, SelectYear::class.java)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_minPrice", selectedMinPrice)
            intent.putExtra("selected_maxPrice", selectedMaxPrice)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_minEmissions", selectedMinEmissions)
            intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            intent.putExtra("selected_colour", selectedColour)
            intent.putExtra("selected_fuelType", fuelType.text)
            startActivity(intent)
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

        val reset = findViewById<Button>(R.id.reset)

        reset.setOnClickListener {
            colourText.text = "ALL"
            minEmissionsText.text = "Minimum: None"
            maxEmissionsText.text = "Maximum: None"
            minPriceText.text = "Minimum: None"
            maxPriceText.text = "Maximum: None"
            minYearText.text = "Minimum: None"
            maxYearText.text = "Maximum: None"
            makeText.text = "ALL"
            modelText.text = "ANY"

            selectedMake = "ALL"
            selectedModel = "ANY"
            selectedMinPrice = "None"
            selectedMaxPrice = "None"
            selectedMinYear = "None"
            selectedMaxYear = "None"
            selectedMinEmissions = "None"
            selectedMaxEmissions = "None"
            selectedColour = "ALL"
        }
        
        selectMake.setOnClickListener {
            val intent = Intent(this@Search, SelectMake::class.java)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_minPrice", selectedMinPrice)
            intent.putExtra("selected_maxPrice", selectedMaxPrice)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_minEmissions", selectedMinEmissions)
            intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            intent.putExtra("selected_colour", selectedColour)
            intent.putExtra("selected_fuelType", fuelType.text)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        val model = findViewById<TextView>(R.id.model)

        if(makeText.text != "ALL") {
            model.setTextColor(Color.parseColor("#898989"))
            modelText.setTextColor(Color.parseColor("#898989"))
            val selectModel = findViewById<LinearLayout>(R.id.selectModel)
            selectModel.setOnClickListener {
                val intent = Intent(this@Search, SelectModel::class.java)
                intent.putExtra("selected_make", makeText.text.toString())
                intent.putExtra("selected_make", selectedMake)
                intent.putExtra("selected_model", selectedModel)
                intent.putExtra("selected_minPrice", selectedMinPrice)
                intent.putExtra("selected_maxPrice", selectedMaxPrice)
                intent.putExtra("selected_minYear", selectedMinYear)
                intent.putExtra("selected_maxYear", selectedMaxYear)
                intent.putExtra("selected_minEmissions", selectedMinEmissions)
                intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
                intent.putExtra("selected_colour", selectedColour)
                intent.putExtra("selected_fuelType", fuelType.text)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        } else {
            model.setTextColor(Color.parseColor("#B3B3B3"))
            modelText.setTextColor(Color.parseColor("#B3B3B3"))
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
