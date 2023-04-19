package com.example.carsellingandbuyingapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GetStarted : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started1)

        var getStarted = findViewById<Button>(R.id.getStarted)
        var welcome = findViewById<TextView>(R.id.welcomeTo)
        var autoX = findViewById<TextView>(R.id.AutoXchange)
        var skip = findViewById<TextView>(R.id.skip)
        var logo = findViewById<ImageView>(R.id.logo)

        val container = findViewById<ConstraintLayout>(R.id.container)
        val slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)

        fadeInViews(welcome, autoX, logo, getStarted, skip)

        skip.setOnClickListener {
            completePreferences()
            val intent = Intent(this@GetStarted, MainPage::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        getStarted.setOnClickListener {
            container.startAnimation(slideOutLeft)

            slideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val intent = Intent(this@GetStarted, GetStarted2::class.java)
                    intent.putExtra("progressButton", "none")
                    startActivity(intent)
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }

    }

    private fun fadeInViews(vararg views: View) {
        if (views.isEmpty()) return

        val fadeInAnimation = ObjectAnimator.ofFloat(views.first(), "alpha", 0f, 1f)
        fadeInAnimation.duration = 500
        fadeInAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                fadeInViews(*views.drop(1).toTypedArray())
            }
        })
        fadeInAnimation.start()
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