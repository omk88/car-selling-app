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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

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

        getStarted.setOnClickListener {
            container.startAnimation(slideOutLeft)

            slideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    container.visibility = View.GONE
                    val intent = Intent(this@GetStarted, GetStarted2::class.java)
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
}