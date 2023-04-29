package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.nio.charset.StandardCharsets
import javax.crypto.spec.SecretKeySpec

class StartConversation : AppCompatActivity() {

    private var conversationAdapter: ConversationAdapter? = null
    private lateinit var usernamesAdapter: UsernamesAdapter
    private lateinit var conversationListView: ListView
    private lateinit var usernamesListView: ListView
    private val conversationIndexMap = HashMap<String, Int>()
    private lateinit var welcomeTextView: TextView
    var showWelcomeText: Int = 1
    var secretKey = SecretKeySpec("your-secret-key-here".toByteArray(), "AES")
    var iv = "your-iv-here".toByteArray()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        val hardcodedKey = "a9bicoepvn29dlpa"
        val keyBytes = hardcodedKey.toByteArray(StandardCharsets.UTF_8)
        secretKey = SecretKeySpec(keyBytes, "AES")

        val hardcodedIv = "03lpcmyinqusny07"
        val ivBytes = hardcodedIv.toByteArray(StandardCharsets.UTF_8)
        iv = ivBytes

        val loggedInUser = application as Username

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navBar)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    val intent = Intent(this@StartConversation, Profile::class.java)
                    intent.putExtra("username", loggedInUser.username)
                    intent.putExtra("bannerUri", loggedInUser.bannerUri)
                    intent.putExtra("profilePictureUri", loggedInUser.profilePictureUri)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.map -> {
                    val intent = Intent(this@StartConversation, MapsPage::class.java)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.browse -> {
                    val intent = Intent(this, MainPage::class.java)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.sellCar -> {
                    val intent = Intent(this@StartConversation, SellCar::class.java)
                    intent.putExtra("username", loggedInUser.username)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.messages -> {
                    val intent = Intent(this@StartConversation, StartConversation::class.java)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                else -> false
            }
        }

        welcomeTextView = findViewById(R.id.welcome)


        val username = findViewById<EditText>(R.id.username)
        val close = findViewById<LinearLayout>(R.id.close)
        val dummyView = findViewById<FrameLayout>(R.id.dummy_view)

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


        close.setOnClickListener {
            dummyView.requestFocus()
        }

        username.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                welcomeTextView.visibility = View.GONE
                conversationListView.visibility = View.GONE
                usernamesListView.visibility = View.VISIBLE
                close.visibility = View.VISIBLE
            } else {
                welcomeTextView.visibility = View.VISIBLE
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
                    var message = messageSnapshot.getValue(String::class.java) ?: continue

                    val encryptedMessage = Base64.decode(message, Base64.NO_WRAP)
                    message = AESHelper.decrypt(encryptedMessage, secretKey, iv)

                    val sender = messageData.split("|")[0]
                    val parts = message.split(":")
                    if (parts.size >= 3) {
                        lastMessage = parts[0]
                        if (parts[1] == "0" && sender != loggedInUser.username) {
                            messagesCount += 1
                        }
                    }
                }

                val index = conversationAdapter!!.count
                conversationIndexMap[conversationKey] = index

                val firstUser = conversationKey.split(":")[0]
                val secondUser = conversationKey.split(":")[1]
                val targetUsername = if (loggedInUser.username == firstUser) secondUser else firstUser

                conversationAdapter!!.removeByUsername(targetUsername)
                conversationAdapter!!.add(targetUsername)
                conversationAdapter!!.addLastMessage(lastMessage)
                conversationAdapter!!.addMessageCount(messagesCount)
                conversationAdapter!!.notifyDataSetChanged()
                welcomeTextView.visibility = View.GONE
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
                    if (parts.size >= 3) {
                        lastMessage = parts[0]
                        if (parts[1] == "0" && sender != loggedInUser.username) {
                            messagesCount += 1
                        }
                    }
                }

                val index = conversationIndexMap[conversationKey] ?: return
                updateConversation(conversationKey, loggedInUser, index, lastMessage, messagesCount)
                conversationAdapter!!.notifyDataSetChanged()
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
            conversationAdapter?.update(conversationIndexMap[conversationKey]!!, targetUsername, lastMessage, messagesCount)
        } else {
            conversationAdapter?.removeByUsername(targetUsername)
            conversationAdapter?.add(targetUsername)
            conversationAdapter?.addLastMessage(lastMessage)
            conversationAdapter?.addMessageCount(messagesCount)
            conversationAdapter?.notifyDataSetChanged()

            val newIndex = conversationAdapter?.count?.minus(1)
            if (newIndex != null) {
                conversationIndexMap[conversationKey] = newIndex.toInt()
            }
        }
    }

    fun getUsernames() {
        val database = FirebaseDatabase.getInstance()
        val query: Query = database.reference.child("users")
        val usernames = ArrayList<String>()
        val loggedInUser = application as Username

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usernames.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val username = userSnapshot.key
                    if (username != null) {
                        if (username != loggedInUser.username)
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

class ConversationAdapter(
    context: Context,
    private val messages: ArrayList<String>,
    private val welcomeTextView: TextView
) :
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

        val profilePicture = view.findViewById<ImageView>(R.id.profilePicture)

        messageTextView.text = getItem(position)

        val profilePictureRef = Firebase.storage.reference.child("images/profile_picture-" + messageTextView.text)

        val fadeInAnim = AlphaAnimation(0.0f, 1.0f)
        fadeInAnim.duration = 500

        profilePictureRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val profilePictureUri = getImageUriFromBytes(bytes)
            Glide.with(view)
                .asBitmap()
                .load(profilePictureUri)
                .error(R.drawable.profile_picture)
                .override(200, 200)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .thumbnail(0.005f)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        profilePicture.setImageBitmap(resource)
                        profilePicture.startAnimation(fadeInAnim)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }.addOnFailureListener { exception -> }

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

    private fun getImageUriFromBytes(bytes: ByteArray): Uri {
        val file = File.createTempFile("image", "jpg")
        file.writeBytes(bytes)
        return Uri.fromFile(file)
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
        val profilePicture = view.findViewById<ImageView>(R.id.profilePicture)

        var profilePictureUri = Uri.parse("android.resource://your.package.name/" + R.drawable.profile_picture)

        val fadeInAnim = AlphaAnimation(0.0f, 1.0f)
        fadeInAnim.duration = 500

        messageTextView.text = getItem(position)

        val profilePictureRef = Firebase.storage.reference.child("images/profile_picture-" + messageTextView.text)

        profilePictureRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            profilePictureUri = getImageUriFromBytes(bytes)
            Glide.with(view)
                .asBitmap()
                .load(profilePictureUri)
                .error(R.drawable.profile_picture)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .thumbnail(0.1f)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        profilePicture.setImageBitmap(resource)
                        profilePicture.startAnimation(fadeInAnim)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {} })
        }.addOnFailureListener { exception -> }

        messageTextView.setOnClickListener {
            val activity = context as Activity
            val convIntent = Intent(activity, Conversation::class.java)
            convIntent.putExtra("user", messageTextView.text)
            activity.startActivity(convIntent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        return view
    }

    private fun getImageUriFromBytes(bytes: ByteArray): Uri {
        val file = File.createTempFile("image", "jpg")
        file.writeBytes(bytes)
        return Uri.fromFile(file)
    }
}


