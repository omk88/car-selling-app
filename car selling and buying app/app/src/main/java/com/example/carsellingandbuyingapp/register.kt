package com.example.carsellingandbuyingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun switchToLogin(view: View) {
        startActivity(Intent(this@register,MainActivity::class.java))
    }

    fun addToDatabase(view: View) {
        val editTxt = findViewById<EditText>(R.id.editTextTextPersonName)
        val username = editTxt.text.toString()

        val passTxt = findViewById<EditText>(R.id.editTextTextPassword2)
        val password = passTxt.text.toString()

        val database = Firebase.database.getReference("users")
        val user = User(username, password)
        database.child(username).setValue(user)

        Toast.makeText(this, "Successfully Registered!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@register,MainActivity::class.java))

    }
}