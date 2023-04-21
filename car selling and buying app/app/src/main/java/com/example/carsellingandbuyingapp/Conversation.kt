package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Conversation : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val loggedInUser = application as Username

        val user = "user1"

        val message = findViewById<EditText>(R.id.message)
        val sendMessage = findViewById<Button>(R.id.sendMessage)
        val messageText = message.text

        sendMessage.setOnClickListener {
            val database = Firebase.database.getReference("conversations")
            val conversation = user+":"+intent.getStringExtra("user")
            val msg = messageText.toString()

            database.child(conversation).child(user+"|"+getCurrentDateTime()).setValue(msg)
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return currentDateTime.format(formatter)
    }
}