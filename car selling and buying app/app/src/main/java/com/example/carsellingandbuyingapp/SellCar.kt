package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*

class SellCar : AppCompatActivity() {

    private var condition: CharSequence = "Spares or Repairs - Includes cars that are non-functional or have major faults that prevent the car from working as intended."

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_car)

        val loggedInUser = application as Username

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        val goBack = findViewById<LinearLayout>(R.id.goBack)

        goBack.setOnClickListener {
            val intent = Intent(this, MainPage::class.java)
            intent.putExtra("username", loggedInUser.username)
            startActivity(intent)
            overridePendingTransition(
                androidx.appcompat.R.anim.abc_fade_in,
                androidx.appcompat.R.anim.abc_fade_out
            )
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            condition = radioButton.text
        }

        //val priceText = findViewById<EditText>(R.id.editTextCarPrice)

        /*priceText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) == "£" && count == 1 && after == 0) {
                    priceText.setText("£")
                    priceText.setSelection(priceText.text.length)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) != "£") {
                    priceText.setText("£" + s.toString())
                    priceText.setSelection(priceText.text.length)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })*/
    }

    fun switchToSellCar2(view: View) {
        val regText = findViewById<EditText>(R.id.editTextCarRegistration)
        val mileageText = findViewById<EditText>(R.id.editTextCarMileage)
        //val priceText = findViewById<EditText>(R.id.editTextCarPrice)
        val description = findViewById<EditText>(R.id.description)

        val username = intent.getStringExtra("username")

        val intent = Intent(this, SellCar2::class.java)
        intent.putExtra("registration", regText.text.toString())
        intent.putExtra("mileage", mileageText.text.toString())
        //intent.putExtra("price", priceText.text.toString())
        intent.putExtra("username", username)
        intent.putExtra("description", description.text.toString())

        val regex = Regex("^[^-]+")
        condition = regex.find(condition)?.value?.trim() ?: ""

        intent.putExtra("condition", condition)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


}