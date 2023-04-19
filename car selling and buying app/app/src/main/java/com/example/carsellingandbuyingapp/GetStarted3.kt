package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class GetStarted3 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started3)

        var continue1 = findViewById<Button>(R.id.continue1)
        val container = findViewById<ConstraintLayout>(R.id.container)

        var next = findViewById<LinearLayout>(R.id.nextLayout)
        var prev = findViewById<LinearLayout>(R.id.prevLayout)

        val slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
        val slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        val slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)

        if (intent.getStringExtra("prev") == "prev") {
            container.startAnimation(slideInLeft)
        } else {
            container.startAnimation(slideInRight)
        }

        next.setOnClickListener {
            intent.putExtra("next", "next")
            container.startAnimation(slideOutLeft)

            slideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val intent = Intent(this@GetStarted3, GetStarted4::class.java)
                    startActivity(intent)
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }

        prev.setOnClickListener {
            intent.putExtra("prev", "prev")
            container.startAnimation(slideOutRight)

            slideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val intent = Intent(this@GetStarted3, GetStarted2::class.java)
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
                    val intent = Intent(this@GetStarted3, GetStarted4::class.java)
                    startActivity(intent)
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
        }
    }
}