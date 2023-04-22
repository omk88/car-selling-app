package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class StartConversation : AppCompatActivity() {

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var conversationListView: ListView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        val loggedInUser = application as Username
        loggedInUser.username = "user4"

        val username = findViewById<EditText>(R.id.username)
        val startConversation = findViewById<Button>(R.id.startConversation)
        val usernameText = username.text
        val databaseConversation = Firebase.database.getReference("conversations")

        conversationListView = findViewById(R.id.messageList)
        conversationAdapter = ConversationAdapter(this, ArrayList())
        conversationListView.adapter = conversationAdapter

        databaseConversation.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val conversationKey = snapshot.key
                conversationKey?.let {
                    val firstUser = it.split(":")[0]
                    val secondUser = it.split(":")[1]

                    if (loggedInUser.username == firstUser) {
                        conversationAdapter.add(secondUser)
                        conversationAdapter.notifyDataSetChanged()
                    } else if (loggedInUser.username == secondUser) {
                        conversationAdapter.add(firstUser)
                        conversationAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {} })

        startConversation.setOnClickListener {
            val convIntent = Intent(this, Conversation::class.java)
            convIntent.putExtra("user",usernameText.toString())
            startActivity(convIntent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }
    }
}

class ConversationAdapter(context: Context, private val messages: ArrayList<String>) :
    ArrayAdapter<String>(context, R.layout.message_item, messages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.message_item, parent, false)

        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = messages[position]

        messageTextView.setOnClickListener {
            val activity = context as Activity
            val convIntent = Intent(activity, Conversation::class.java)
            convIntent.putExtra("user", messageTextView.text)
            activity.startActivity(convIntent)
            activity.overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        return view
    }
}
