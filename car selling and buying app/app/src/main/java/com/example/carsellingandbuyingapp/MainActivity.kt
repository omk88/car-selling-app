package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signUpButton = findViewById<TextView>(R.id.signUpNow)
        signUpButton.setOnClickListener{
            startActivity(Intent(this@MainActivity,register::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    fun checkData(view: View) {
        val database = Firebase.database.getReference("users")

        val editTxt = findViewById<EditText>(R.id.editTextTextPersonUsername)
        val username = editTxt.text.toString()

        val passTxt = findViewById<EditText>(R.id.editTextTextPassword)
        val password = passTxt.text.toString()

        database.child(username).get().addOnSuccessListener {
            if(it.exists()) {
                val username = it.child("username").value
                if(password == it.child("password").value) {
                    val password = it.child("password").value
                    Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
                    val loggedInUser = application as Username
                    loggedInUser.username = it.child("username").value.toString()
                    val intent = Intent(this, MainPage::class.java)
                    intent.putExtra("username",it.child("username").value.toString())
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                } else {
                    Toast.makeText(this, "Password Incorrect", Toast.LENGTH_SHORT).show()
                }


            } else {
                Toast.makeText(this, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }

    }
}