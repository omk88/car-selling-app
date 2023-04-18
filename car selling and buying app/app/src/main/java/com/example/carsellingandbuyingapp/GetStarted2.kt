package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class GetStarted2 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started2)

        var continue1 = findViewById<Button>(R.id.continue1)

        val container = findViewById<ConstraintLayout>(R.id.container)
        val slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        container.startAnimation(slideInRight)
        val slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)

        continue1.setOnClickListener {
            container.startAnimation(slideOutLeft)

            slideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val intent = Intent(this@GetStarted2, GetStarted3::class.java)
                    startActivity(intent)
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }
    }
}