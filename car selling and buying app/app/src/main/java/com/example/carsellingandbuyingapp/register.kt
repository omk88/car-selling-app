package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class register : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signInButton = findViewById<TextView>(R.id.signInNow)
        val signUpButton = findViewById<Button>(R.id.button3)

        signUpButton.setOnClickListener{
            addToDatabase()
        }

        signInButton.setOnClickListener{
            startActivity(Intent(this@register,MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    fun addToDatabase() {
        val editTxt = findViewById<EditText>(R.id.editTextTextPersonName)
        val username = editTxt.text.toString()

        val passTxt = findViewById<EditText>(R.id.editTextTextPassword2)
        val password = passTxt.text.toString()

        val phoneTxt = findViewById<EditText>(R.id.editTextPhoneNumber)
        val phone = phoneTxt.text.toString()

        val database = Firebase.database.getReference("users")
        val user = User(username, password, phone)
        database.child(username).setValue(user)

        Toast.makeText(this, "Successfully Registered!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@register,MainActivity::class.java))

    }
}