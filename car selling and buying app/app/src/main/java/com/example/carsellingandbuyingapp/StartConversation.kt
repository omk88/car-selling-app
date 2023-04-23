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
import androidx.cardview.widget.CardView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class StartConversation : AppCompatActivity() {

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var conversationListView: ListView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        val loggedInUser = application as Username

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
                var lastMessage = ""
                var messagesCount = 0

                for (messageSnapshot in snapshot.children) {
                    val messageData = messageSnapshot.key ?: continue
                    val message = messageSnapshot.getValue(String::class.java) ?: continue
                    val sender = messageData.split("|")[0]
                    val parts = message.split(":")
                    if (parts.size == 2) {
                        lastMessage = parts[0]
                        if (parts[1] == "0" && sender != loggedInUser.username) {
                            messagesCount += 1
                        }
                    }
                }

                conversationKey?.let {
                    val firstUser = it.split(":")[0]
                    val secondUser = it.split(":")[1]

                    val welcome = findViewById<TextView>(R.id.welcome)

                    if (loggedInUser.username == firstUser) {
                        conversationAdapter.add(secondUser)
                        welcome.visibility = View.GONE
                        conversationAdapter.addLastMessage(lastMessage)
                        conversationAdapter.addMessageCount(messagesCount)
                        conversationAdapter.notifyDataSetChanged()
                    } else if (loggedInUser.username == secondUser) {
                        conversationAdapter.add(firstUser)
                        welcome.visibility = View.GONE
                        conversationAdapter.addLastMessage(lastMessage)
                        conversationAdapter.addMessageCount(messagesCount)
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
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}

class ConversationAdapter(context: Context, private val messages: ArrayList<String>) :
    ArrayAdapter<String>(context, R.layout.user_item, messages) {

    private val currentMessages = ArrayList<String>()
    private val messageCounts = ArrayList<Int>()

    fun addLastMessage(message: String) {
        currentMessages.add(message)
    }

    fun addMessageCount(count: Int) {
        messageCounts.add(count)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.user_item, parent, false)

        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        val currentMessageTextView = view.findViewById<TextView>(R.id.currentMessage)
        val messagesCountTextView = view.findViewById<TextView>(R.id.messageCount)
        val unseenMessages = view.findViewById<CardView>(R.id.unseenMessages)

        messagesCountTextView.text = messageCounts[position].toString()

        if (messageCounts[position] == 0) {
            unseenMessages.visibility = View.GONE
        }

        currentMessageTextView.text = currentMessages[position]

        messageTextView.text = messages[position]

        messageTextView.setOnClickListener {
            val activity = context as Activity
            val convIntent = Intent(activity, Conversation::class.java)
            convIntent.putExtra("user", messageTextView.text)
            activity.startActivity(convIntent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        return view
    }

}

