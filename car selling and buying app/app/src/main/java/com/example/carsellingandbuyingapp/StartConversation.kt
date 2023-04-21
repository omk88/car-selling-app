package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class StartConversation : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        val loggedInUser = application as Username

        val username = findViewById<EditText>(R.id.username)
        val startConversation = findViewById<Button>(R.id.startConversation)
        val usernameText = username.text

        startConversation.setOnClickListener {
            val convIntent = Intent(this, Conversation::class.java)
            convIntent.putExtra("user",usernameText.toString())
            startActivity(convIntent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }
    }
}