package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Conversation : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageListView: ListView
    private var conversation: String? = null
    private var user = ""
    private lateinit var sendMessage: Button
    private lateinit var message: EditText
    private lateinit var messageText: CharSequence

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val loggedInUser = application as Username

        user = "user4"
        conversation = user + ":" + intent.getStringExtra("user")

        val usernameTextView = findViewById<TextView>(R.id.username)
        usernameTextView.setText(intent.getStringExtra("user"))

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            val convIntent = Intent(this, StartConversation::class.java)
            startActivity(convIntent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val entryKey = childSnapshot.key
                    if (entryKey != null) {
                        if (entryKey.toString().split(":")[0] == user) {
                            conversation = user + ":" + intent.getStringExtra("user")
                        } else if (entryKey.toString().split(":")[1] == user) {
                            conversation = intent.getStringExtra("user") + ":" + user
                        } else {
                            conversation = user + ":" + intent.getStringExtra("user")
                        }
                    }
                }
                setupConversation()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        message = findViewById(R.id.message)
        sendMessage = findViewById(R.id.sendMessage)
        messageText = message.text

        val database = FirebaseDatabase.getInstance().getReference("conversations")
        database.addValueEventListener(valueEventListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupConversation() {
        val database = FirebaseDatabase.getInstance().getReference("conversations")
        val databaseMessages = conversation?.let { database.child(it) }

        sendMessage.setOnClickListener {
            conversation?.let { it1 ->
                database.child(it1).child(user + "|" + getCurrentDateTime()).setValue(messageText.toString()+":"+"0")
            }
        }

        messageListView = findViewById(R.id.messageList)
        messageAdapter = MessageAdapter(this, ArrayList(), user)
        messageListView.adapter = messageAdapter

        if (databaseMessages != null) {
            databaseMessages.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messageKey = snapshot.key
                    var seenMarker = ""
                    var messageText = ""
                    var messageTextAndSeenMarker = snapshot.getValue(String::class.java).toString()
                    if (messageTextAndSeenMarker != null) {
                        messageText = messageTextAndSeenMarker.split(":")[0]
                        seenMarker = messageTextAndSeenMarker.split(":")[1]
                    }

                    if (messageKey != null && messageTextAndSeenMarker != null) {
                        val messageUser = messageKey.split("|")[0]
                        val timestamp = messageKey.split("|")[1].toLong()

                        if (messageUser != user && seenMarker == "0") {
                            seenMarker = "1"
                            messageTextAndSeenMarker = messageText + ":" + seenMarker
                            conversation?.let { it1 ->
                                database.child(it1).child(messageKey).setValue(messageTextAndSeenMarker)
                            }
                        }

                        val message = Message(messageUser, messageText, timestamp)

                        if (!messageAdapter.messageExists(messageKey)) {
                            messageAdapter.add(message)
                            messageAdapter.sortMessages()
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return currentDateTime.format(formatter)
    }
}

class MessageAdapter(context: Context, private val messages: ArrayList<Message>, private val user: String) :
    ArrayAdapter<Message>(context, R.layout.message_item, messages) {

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.user == user) {
            VIEW_TYPE_MY_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }

    override fun getViewTypeCount(): Int {
        return 3 // we have two view types: my message and other's message
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewType = getItemViewType(position)
        val inflater = LayoutInflater.from(context)

        return when (viewType) {
            VIEW_TYPE_MY_MESSAGE -> {
                val view = convertView ?: inflater.inflate(R.layout.message_item, parent, false)
                val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
                messageTextView.text = messages[position].text
                view
            }
            else -> {
                val view = convertView ?: inflater.inflate(R.layout.message_item_2, parent, false)
                val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
                messageTextView.text = messages[position].text
                view
            }
        }
    }

    fun sortMessages() {
        messages.sortBy { it.timestamp }
        notifyDataSetChanged()
    }

    fun messageExists(messageKey: String): Boolean {
        return messages.any { it.messageKey == messageKey }
    }
}



