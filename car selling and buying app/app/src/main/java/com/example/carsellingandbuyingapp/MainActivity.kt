package com.example.carsellingandbuyingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                    startActivity(Intent(this@MainActivity,MainPage::class.java))
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

    fun switchToRegister(view: View) {
        startActivity(Intent(this@MainActivity,register::class.java))
    }

}