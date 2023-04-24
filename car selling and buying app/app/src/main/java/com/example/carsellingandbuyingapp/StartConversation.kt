package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class StartConversation : AppCompatActivity() {

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var usernamesAdapter: UsernamesAdapter
    private lateinit var conversationListView: ListView
    private lateinit var usernamesListView: ListView
    private val conversationIndexMap = HashMap<String, Int>()
    private lateinit var welcomeTextView: TextView



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        welcomeTextView = findViewById(R.id.welcome)

        val loggedInUser = application as Username

        val username = findViewById<EditText>(R.id.username)
        val close = findViewById<LinearLayout>(R.id.close)
        val dummyView = findViewById<FrameLayout>(R.id.dummy_view)

        close.setOnClickListener {
            dummyView.requestFocus()
        }

        close.visibility = View.GONE

        username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                usernamesAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })


        usernamesListView = findViewById(R.id.messageList2)

        username.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                conversationListView.visibility = View.GONE
                usernamesListView.visibility = View.VISIBLE
                close.visibility = View.VISIBLE

            } else {
                conversationListView.visibility = View.VISIBLE
                usernamesListView.visibility = View.GONE
                close.visibility = View.GONE
                hideKeyboard(username)
            }
        }
        //val startConversation = findViewById<Button>(R.id.startConversation)
        val usernameText = username.text
        val databaseConversation = Firebase.database.getReference("conversations")

        conversationListView = findViewById(R.id.messageList)
        conversationAdapter = ConversationAdapter(this, ArrayList(), welcomeTextView)
        conversationListView.adapter = conversationAdapter

        usernamesListView.visibility = View.GONE
        conversationListView.visibility = View.VISIBLE
        getUsernames()



        databaseConversation.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val conversationKey = snapshot.key ?: return

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

                val index = conversationAdapter.count
                conversationIndexMap[conversationKey] = index

                val firstUser = conversationKey.split(":")[0]
                val secondUser = conversationKey.split(":")[1]
                val targetUsername = if (loggedInUser.username == firstUser) secondUser else firstUser

                conversationAdapter.removeByUsername(targetUsername)
                conversationAdapter.add(targetUsername)
                conversationAdapter.addLastMessage(lastMessage)
                conversationAdapter.addMessageCount(messagesCount)
                conversationAdapter.notifyDataSetChanged()
            }


            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val conversationKey = snapshot.key ?: return

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

                val index = conversationIndexMap[conversationKey] ?: return
                updateConversation(conversationKey, loggedInUser, index, lastMessage, messagesCount)
                conversationAdapter.notifyDataSetChanged()
            }



            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {} })

        /*startConversation.setOnClickListener {
            val convIntent = Intent(this, Conversation::class.java)
            convIntent.putExtra("user",usernameText.toString())
            startActivity(convIntent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }*/
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun updateConversation(conversationKey: String, loggedInUser: Username, index: Int, lastMessage: String, messagesCount: Int) {
        val firstUser = conversationKey.split(":")[0]
        val secondUser = conversationKey.split(":")[1]

        val targetUsername = if (loggedInUser.username == firstUser) secondUser else firstUser

        if (conversationIndexMap.containsKey(conversationKey)) {
            conversationAdapter.update(conversationIndexMap[conversationKey]!!, targetUsername, lastMessage, messagesCount)
        } else {
            conversationAdapter.removeByUsername(targetUsername)
            conversationAdapter.add(targetUsername)
            conversationAdapter.addLastMessage(lastMessage)
            conversationAdapter.addMessageCount(messagesCount)
            conversationAdapter.notifyDataSetChanged()

            val newIndex = conversationAdapter.count - 1
            conversationIndexMap[conversationKey] = newIndex
        }
    }





    fun getUsernames() {
        val database = FirebaseDatabase.getInstance()
        val query: Query = database.reference.child("users")
        val usernames = ArrayList<String>()

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usernames.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val username = userSnapshot.key
                    if (username != null) {
                        usernames.add(username)
                    }
                }
                usernamesAdapter = UsernamesAdapter(this@StartConversation, usernames)
                usernamesListView.adapter = usernamesAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }



}

class ConversationAdapter(context: Context, private val messages: ArrayList<String>, private val welcomeTextView: TextView) :
    ArrayAdapter<String>(context, R.layout.user_item, messages) {

    private val currentMessages = ArrayList<String>()
    private val messageCounts = ArrayList<Int>()

    fun removeByUsername(username: String) {
        val index = messages.indexOf(username)
        if (index != -1) {
            messages.removeAt(index)
            currentMessages.removeAt(index)
            messageCounts.removeAt(index)
        }
    }

    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.substring(0, maxLength) + "..."
        }
    }


    fun addLastMessage(message: String) {
        currentMessages.add(message)
    }

    fun addMessageCount(count: Int) {
        messageCounts.add(count)
    }

    fun update(index: Int, username: String, lastMessage: String, messagesCount: Int) {
        if (index < count) {
            messages[index] = username
            currentMessages[index] = lastMessage
            messageCounts[index] = messagesCount
        } else {
            messages.add(username)
            currentMessages.add(lastMessage)
            messageCounts.add(messagesCount)
        }
        notifyDataSetChanged()
    }




    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        welcomeTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.user_item, parent, false)

        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        val currentMessageTextView = view.findViewById<TextView>(R.id.currentMessage)
        val messagesCountTextView = view.findViewById<TextView>(R.id.messageCount)
        val unseenMessages = view.findViewById<CardView>(R.id.unseenMessages)

        messagesCountTextView.text = messageCounts[position].toString()

        if (messageCounts[position] == 0) {
            unseenMessages.visibility = View.GONE
        } else {
            unseenMessages.visibility = View.VISIBLE
            messagesCountTextView.text = messageCounts[position].toString()
        }

        currentMessageTextView.text = truncateText(currentMessages[position], 15)

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

class UsernamesAdapter(context: Context, private val allUsernames: ArrayList<String>) :
    ArrayAdapter<String>(context, R.layout.user_item2) {

    private var filteredUsernames: ArrayList<String> = ArrayList(allUsernames)

    fun filter(text: String) {
        filteredUsernames.clear()
        if (text.isEmpty()) {
            filteredUsernames.addAll(allUsernames)
        } else {
            for (username in allUsernames) {
                if (username.contains(text, ignoreCase = true)) {
                    filteredUsernames.add(username)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return filteredUsernames.size
    }

    override fun getItem(position: Int): String? {
        return filteredUsernames[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.user_item2, parent, false)

        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)

        messageTextView.text = getItem(position)

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

