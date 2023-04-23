package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

interface WorldTimeApiService {
    @GET("api/ip")
    fun getCurrentUtcTime(): Call<WorldTimeApiResponse>
}

data class WorldTimeApiResponse(
    val utc_datetime: String
)


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

        user = loggedInUser.username
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
            messageText = message.text

            getCurrentDateTime { currentDateTimeString ->
                conversation?.let { it1 ->
                    database.child(it1).child(user + "|" + currentDateTimeString).setValue(messageText.toString() + ":" + "0")
                }
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
                    //println("MESSAGE"+messageKey+messageTextAndSeenMarker)
                    if (messageTextAndSeenMarker != null) {
                        messageText = messageTextAndSeenMarker.split(":")[0]
                        seenMarker = messageTextAndSeenMarker.split(":")[1]
                    }

                    if (messageKey != null && messageTextAndSeenMarker != null) {
                        val messageUser = messageKey.split("|")[0]
                        val timestamp = messageKey.split("|")[1]

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
                            messageAdapter.notifyDataSetChanged()
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
    suspend fun getCurrentUtcTimeFromApi(): LocalDateTime {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://worldtimeapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WorldTimeApiService::class.java)
        val response = service.getCurrentUtcTime().execute()

        if (response.isSuccessful) {
            val result = response.body()
            if (result != null) {
                val utcDateTimeString = result.utc_datetime
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                return LocalDateTime.parse(utcDateTimeString, formatter)
            }
        }
        throw Exception("Error getting time from API")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTime(completionHandler: (String) -> Unit) {
        val currentDateTime = LocalDateTime.now()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val utcDateTime = getCurrentUtcTimeFromApi()
                val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                val currentDateTimeString = utcDateTime.format(formatter)
                completionHandler(currentDateTimeString)
            } catch (e: Exception) {
                Log.e("Conversation", "Error getting time from API: $e")
            }
        }
    }


}

class MessageAdapter(context: Context, val messages: ArrayList<Message>, private val user: String) :
    ArrayAdapter<Message>(context, R.layout.message_item, messages) {

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocalDateTime(timestamp: String): LocalDateTime {
        val inputDateString = timestamp.toString()
        val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val localDateTime = LocalDateTime.parse(inputDateString, inputFormatter)

        val londonZoneId = ZoneId.of("Europe/London")
        val zonedDateTime = localDateTime.atZone(ZoneOffset.UTC)
        val londonZonedDateTime = zonedDateTime.withZoneSameInstant(londonZoneId)

        return londonZonedDateTime.toLocalDateTime()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sortMessages() {
        val messageTimestampMap = mutableMapOf<Message, ZonedDateTime>()

        messages.forEach { message ->
            val inputDateString = message.timestamp
            val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val utcDateTime = ZonedDateTime.parse(inputDateString.toString(), inputFormatter.withZone(ZoneOffset.UTC))

            messageTimestampMap[message] = utcDateTime
        }

        val sortedMessages = messageTimestampMap.entries.sortedWith { entry1, entry2 ->
            entry1.value.compareTo(entry2.value)
        }.map { it.key }

        messages.clear()
        messages.addAll(sortedMessages)
        println("MESSAGESSS" + messages)

        notifyDataSetChanged()
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
        return 3
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

    fun messageExists(messageKey: String): Boolean {
        return messages.any { it.messageKey == messageKey }
    }
}




