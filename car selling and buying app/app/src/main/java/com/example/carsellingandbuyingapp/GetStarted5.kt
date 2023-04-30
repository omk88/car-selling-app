package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GetStarted5 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started5)

        var next = findViewById<LinearLayout>(R.id.nextLayout)
        var prev = findViewById<LinearLayout>(R.id.prevLayout)

        var continue1 = findViewById<Button>(R.id.continue1)
        val container = findViewById<ConstraintLayout>(R.id.container)
        val slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
        val slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        val slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)

        var skip = findViewById<TextView>(R.id.skip)

        skip.setOnClickListener {
            completePreferences()
            val intent = Intent(this@GetStarted5, MainPage::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        if (intent.getStringExtra("progressButton") == "prev") {
            container.startAnimation(slideInLeft)
        } else {
            container.startAnimation(slideInRight)
        }

        next.setOnClickListener {
            container.startAnimation(slideOutLeft)

            slideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val use = intent.getStringExtra("Use")
                    val seats = intent.getStringExtra("Seats")
                    val price = intent.getStringExtra("Price")
                    var eco = ""

                    val intent = Intent(this@GetStarted5, GetStarted6::class.java)

                    val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
                    val selectedRadioButtonId = radioGroup.checkedRadioButtonId

                    if (selectedRadioButtonId != -1) {
                        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
                        val selectedValue = selectedRadioButton.text.toString()
                        eco = selectedValue
                    } else { }


                    intent.putExtra("Use", use)
                    intent.putExtra("Seats", seats)
                    intent.putExtra("Price", price)
                    intent.putExtra("Eco", eco)
                    intent.putExtra("progressButton", "next")
                    startActivity(intent)
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }

        prev.setOnClickListener {
            container.startAnimation(slideOutRight)

            slideOutRight.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val intent = Intent(this@GetStarted5, GetStarted4::class.java)
                    intent.putExtra("progressButton", "prev")
                    startActivity(intent)
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }

        continue1.setOnClickListener {
            container.startAnimation(slideOutLeft)

            slideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val intent = Intent(this@GetStarted5, GetStarted6::class.java)
                    intent.putExtra("progressButton", "none")
                    startActivity(intent)
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }
    }
    fun completePreferences() {
        val loggedInUser = application as Username
        val database = Firebase.database.getReference("users")
        database.child(loggedInUser.username).get().addOnSuccessListener {
            if(it.exists()) {
                val username = it.child("username").value.toString()
                val address = it.child("address").value.toString()
                val eco0 = it.child("eco0").value.toString()
                val eco1 = it.child("eco1").value.toString()
                val eco2 = it.child("eco2").value.toString()
                val ecoSales = it.child("ecoSales").value.toString().toInt()
                val password = it.child("password").value.toString()
                val phone = it.child("phone").value.toString()
                val sales = it.child("sales").value.toString().toInt()
                val sale0 = it.child("sale0").value.toString()
                val sale1 = it.child("sale1").value.toString()
                val sale2 = it.child("sale2").value.toString()
                val verifiedEmail = it.child("verifiedEmail").value.toString()
                val verifiedPhone = it.child("verifiedPhone").value.toString()

                val user = User(username, password, phone, address, verifiedPhone, verifiedEmail, eco0, eco1, eco2, sale0, sale1, sale2, sales, ecoSales, 1)
                database.child(username).setValue(user)

            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }
}