package com.example.carsellingandbuyingapp

import android.util.Base64
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Base64.encodeToString
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

interface WorldTimeApiService {
    @GET("api/ip")
    fun getCurrentUtcTime(): Call<WorldTimeApiResponse>
}

data class WorldTimeApiResponse(
    val utc_datetime: String
)

class Conversation : AppCompatActivity(), MessageAdapter.OnMessageAddedCallback {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageListView: ListView
    private var conversation: String? = null
    private var user = ""
    private lateinit var sendMessage: Button
    private lateinit var message: EditText
    private lateinit var messageText: CharSequence
    lateinit var cardView: CardView
    lateinit var cardView2: CardView
    lateinit var close: ImageView
    lateinit var replyText: TextView
    var swipedMessageText: CharSequence = ""
    private var RESULT_LOAD_IMAGE: Int = 1
    var imageLocation: String = ""
    lateinit var attachImage: ImageView
    var imageUri: Uri? = null
    var secretKey = SecretKeySpec("your-secret-key-here".toByteArray(), "AES")
    var iv = "your-iv-here".toByteArray()

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        /*secretKey = AESHelper.generateKey() as SecretKeySpec

        val ivSize = 16 // AES block size is 128 bits = 16 bytes
        val random = SecureRandom()
        iv = ByteArray(ivSize)
        random.nextBytes(iv)*/

        val hardcodedKey = "a9bicoepvn29dlpa"
        val keyBytes = hardcodedKey.toByteArray(StandardCharsets.UTF_8)
        secretKey = SecretKeySpec(keyBytes, "AES")

        val hardcodedIv = "03lpcmyinqusny07"
        val ivBytes = hardcodedIv.toByteArray(StandardCharsets.UTF_8)
        iv = ivBytes


        val profilePicture = findViewById<ImageView>(R.id.profilePicture)

        val attachImage = findViewById<ImageView>(R.id.attachImage)

        cardView2 = findViewById(R.id.cardView2)
        cardView2.visibility = View.GONE

        attachImage.setOnClickListener {
            var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
        }


        val loggedInUser = application as Username

        cardView = findViewById(R.id.cardView)
        cardView.visibility = View.GONE

        close = findViewById(R.id.close)
        replyText = findViewById(R.id.replyText)

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

        val fadeInAnim = AlphaAnimation(0.0f, 1.0f)
        fadeInAnim.duration = 100

        val profilePictureRef = Firebase.storage.reference.child("images/profile_picture-" + intent.getStringExtra("user"))

        profilePictureRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val profilePictureUri = getImageUriFromBytes(bytes)
            Glide.with(this)
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
    }

    private fun getImageUriFromBytes(bytes: ByteArray): Uri {
        val file = File.createTempFile("image", "jpg")
        file.writeBytes(bytes)
        return Uri.fromFile(file)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupConversation() {
        val database = FirebaseDatabase.getInstance().getReference("conversations")
        val databaseMessages = conversation?.let { database.child(it) }

        sendMessage.setOnClickListener {
            messageText = message.text

            if(messageText != "") {
                if (cardView.visibility == View.GONE) {
                    if (cardView2.visibility != View.GONE) {
                        cardView2.visibility = View.GONE
                        val randomString = uploadImage()
                        getCurrentDateTime { currentDateTimeString ->
                            conversation?.let { it1 ->
                                val encryptedMessage = AESHelper.encrypt(messageText.toString() + ":" + "0" + ":" + "REPLY_TO" + ":" + "" + ":" + "IMAGE_SHOWN" + ":" + randomString, secretKey, iv)
                                val base64EncodedMessage = Base64.encodeToString(encryptedMessage, Base64.NO_WRAP)
                                database.child(it1).child(user + "|" + currentDateTimeString)
                                    .setValue(base64EncodedMessage)
                            }
                        }
                        message.setText("")
                    } else {
                        getCurrentDateTime { currentDateTimeString ->
                            conversation?.let { it1 ->
                                val encryptedMessage = AESHelper.encrypt(messageText.toString() + ":" + "0" + ":" + "REPLY_TO" + ":" + ""  + ":" +  "IMAGE_SHOWN" + ":", secretKey, iv)
                                val base64EncodedMessage = Base64.encodeToString(encryptedMessage, Base64.NO_WRAP)
                                database.child(it1).child(user + "|" + currentDateTimeString)
                                    .setValue(base64EncodedMessage)
                            }
                        }
                        message.setText("")
                    }
                } else {
                    if (cardView2.visibility != View.GONE) {
                        cardView2.visibility = View.GONE
                        val randomString = uploadImage()
                        getCurrentDateTime { currentDateTimeString ->
                            conversation?.let { it1 ->
                                val encryptedMessage = AESHelper.encrypt(messageText.toString() + ":" + "0" + ":" + "REPLY_TO" + ":" + replyText.text.toString() + ":" + "IMAGE_SHOWN" + ":" + randomString, secretKey, iv)
                                val base64EncodedMessage = Base64.encodeToString(encryptedMessage, Base64.NO_WRAP)
                                database.child(it1).child(user + "|" + currentDateTimeString)
                                    .setValue(base64EncodedMessage)
                            }
                        }
                        message.setText("")
                    } else {
                        getCurrentDateTime { currentDateTimeString ->
                            conversation?.let { it1 ->
                                val encryptedMessage = AESHelper.encrypt(messageText.toString() + ":" + "0" + ":" + "REPLY_TO" + ":" + replyText.text.toString() + ":" + "IMAGE_SHOWN" + ":", secretKey, iv)
                                val base64EncodedMessage = Base64.encodeToString(encryptedMessage, Base64.NO_WRAP)
                                database.child(it1).child(user + "|" + currentDateTimeString)
                                    .setValue(base64EncodedMessage)
                            }
                        }
                        message.setText("")
                    }
                }
            }
        }


        messageListView = findViewById(R.id.messageList)
        messageAdapter = MessageAdapter(this, ArrayList(), user, messageListView, this, imageLocation, this)
        messageListView.adapter = messageAdapter

        if (databaseMessages != null) {
            databaseMessages.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messageKey = snapshot.key
                    var seenMarker = ""
                    var messageText = ""
                    var replyText = ""
                    var imageMessageLocation = ""
                    var messageTextAndSeenMarker = snapshot.getValue(String::class.java).toString()
                    val encryptedMessage = Base64.decode(messageTextAndSeenMarker, Base64.NO_WRAP)
                    messageTextAndSeenMarker = AESHelper.decrypt(encryptedMessage, secretKey, iv)
                    if (messageTextAndSeenMarker != null) {
                        messageText = messageTextAndSeenMarker.split(":")[0]
                        seenMarker = messageTextAndSeenMarker.split(":")[1]
                        if(messageTextAndSeenMarker.contains("REPLY_TO", ignoreCase = false)) {
                            replyText =  messageTextAndSeenMarker.split(":")[3]
                        }

                        if(messageTextAndSeenMarker.contains("IMAGE_SHOWN", ignoreCase = false)) {
                            imageMessageLocation =  messageTextAndSeenMarker.split(":")[5]
                        }
                    }

                    if (messageKey != null && messageTextAndSeenMarker != null) {

                        val messageUser = messageKey.split("|")[0]
                        val timestamp = messageKey.split("|")[1]
                        if(messageTextAndSeenMarker.contains("REPLY_TO", ignoreCase = false)) {
                            replyText =  messageTextAndSeenMarker.split(":")[3]
                        }

                        if(messageTextAndSeenMarker.contains("IMAGE_SHOWN", ignoreCase = false)) {
                            imageMessageLocation =  messageTextAndSeenMarker.split(":")[5]
                        }

                        if (messageUser != user && seenMarker == "0") {
                            seenMarker = "1"
                            messageTextAndSeenMarker = messageText + ":" + seenMarker + ":" + "REPLY_TO" + ":" + replyText + ":" + "IMAGE_SHOWN" + ":" + imageMessageLocation
                            conversation?.let { it1 ->
                                val encryptedMessage = AESHelper.encrypt(messageTextAndSeenMarker, secretKey, iv)
                                val base64EncodedMessage = Base64.encodeToString(encryptedMessage, Base64.NO_WRAP)
                                database.child(it1).child(messageKey).setValue(base64EncodedMessage)
                            }
                        }

                        val message = Message(messageUser, messageText, timestamp, seenMarker, replyText, imageMessageLocation)

                        if (!messageAdapter.messageExists(messageKey)) {
                            messageAdapter.addMessage(message)
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

    fun scrollToBottom() {
        val adapter = messageListView.adapter
        messageListView.setSelection(adapter.count - 1)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMAGE && data != null) {

            attachImage = findViewById(R.id.attachedImage)
            imageUri = data?.data
            imageLocation = imageUri.toString()
            attachImage.setImageURI(imageUri)
            cardView2.visibility = View.VISIBLE

        }
    }

    private fun compressAndResizeImage(uri: Uri?): ByteArray {
        if (uri == null) {
            throw IllegalArgumentException("Uri cannot be null")
        }

        val compressedImage = compressImage(uri, contentResolver)
        val bitmap = BitmapFactory.decodeByteArray(compressedImage, 0, compressedImage.size)
        val resizedBitmap = resizeImage(bitmap, maxWidth = 800, maxHeight = 800)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return outputStream.toByteArray()
    }

    private fun compressImage(uri: Uri, contentResolver: ContentResolver, quality: Int = 75): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    private fun resizeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun uploadImage(): String {
        val randomString = generateRandomString(10)
        var storageRef = Firebase.storage.reference
        var pd = ProgressDialog(this@Conversation)
        pd.setTitle("Sending Image...")
        pd.show()

        val image0 = storageRef.child("images/$randomString")

        val uris = arrayOf(imageUri)
        val images = arrayOf(image0)

        for (i in 0..0) {
            uris[i]?.let { uri ->
                val compressedAndResizedImage = compressAndResizeImage(uri)
                val uploadTask = images[i].putBytes(compressedAndResizedImage)

                uploadTask.addOnSuccessListener {
                    images[i].downloadUrl.addOnSuccessListener { _ ->
                        pd.dismiss()
                        messageAdapter.notifyDataSetChanged()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(applicationContext, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return randomString
    }

    fun generateRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    override fun onMessageAdded() {

    }
}

class MessageAdapter(
    context: Context,
    val messages: ArrayList<Message>,
    private val user: String,
    private val messageListView: ListView,
    private val conversation: Conversation,
    private val imageLocationText: String,
    private val onMessageAddedCallback: OnMessageAddedCallback
) :
    ArrayAdapter<Message>(context, R.layout.message_item, messages) {

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }

    fun addMessage(newMessage: Message) {
        messages.add(newMessage)
        notifyDataSetChanged()
        onMessageAddedCallback.onMessageAdded()
    }


    interface OnMessageAddedCallback {
        fun onMessageAdded()
    }

    fun scrollToBottom() {
        val adapter = messageListView.adapter
        messageListView.setSelection(adapter.count - 1)
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
        scrollToBottom()

        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if ((message.user == user)) {
            VIEW_TYPE_MY_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }

    override fun getViewTypeCount(): Int {
        return 3
    }

    private fun getImageUriFromBytes(bytes: ByteArray): Uri {
        val file = File.createTempFile("image", "jpg")
        file.writeBytes(bytes)
        return Uri.fromFile(file)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewType = getItemViewType(position)
        val inflater = LayoutInflater.from(context)
        val fadeInAnimation = createFadeInAnimation(300)

        return when (viewType) {
            VIEW_TYPE_MY_MESSAGE -> {
                val view = convertView ?: inflater.inflate(R.layout.message_item, parent, false)
                val seenMarker = view.findViewById<ImageView>(R.id.seen)
                if (messages[position].seen != "1") {
                    seenMarker.visibility = View.GONE
                }

                val replyText = view.findViewById<TextView>(R.id.replyText)
                val sentImage = view.findViewById<ImageView>(R.id.sentImage)

                if (messages[position].replyText == "") {
                } else {
                    replyText.text = messages[position].replyText
                }

                if(replyText.text == "0") {
                    replyText.visibility = View.GONE
                }

                if(messages[position].imageLocation == "") {
                    sentImage.visibility = View.GONE
                } else {
                    val sentImageRef = Firebase.storage.reference.child("images/" + messages[position].imageLocation)

                    sentImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val profilePictureUri = getImageUriFromBytes(bytes)
                        Glide.with(view)
                            .asBitmap()
                            .load(profilePictureUri)
                            .error(R.drawable.profile_picture)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .thumbnail(0.1f)
                            .into(object : CustomTarget<Bitmap?>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                                    sentImage.setImageBitmap(resource)
                                    //profilePicture.startAnimation(fadeInAnim)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {} })
                    }.addOnFailureListener { exception -> }
                }


                val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
                val timeTextView = view.findViewById<TextView>(R.id.timeTextView)

                val inputFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                val outputFormat = DateTimeFormatter.ofPattern("HH:mm")

                val utcDateTime = LocalDateTime.parse(messages[position].timestamp, inputFormat).atZone(ZoneOffset.UTC)
                val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                val formattedTime = localDateTime.format(outputFormat)

                timeTextView.text = formattedTime
                messageTextView.text = messages[position].text
                view.startAnimation(fadeInAnimation)
                view
            }
            else -> {
                val view = convertView ?: inflater.inflate(R.layout.message_item_2, parent, false)
                val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
                val timeTextView = view.findViewById<TextView>(R.id.timeTextView)

                val replyText = view.findViewById<TextView>(R.id.replyText)
                val sentImage = view.findViewById<ImageView>(R.id.sentImage)

                if(messages[position].imageLocation == "") {
                    sentImage.visibility = View.GONE
                } else {
                    val sentImageRef =
                        Firebase.storage.reference.child("images/" + messages[position].imageLocation)

                    sentImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val profilePictureUri = getImageUriFromBytes(bytes)
                        Glide.with(view)
                            .asBitmap()
                            .load(profilePictureUri)
                            .error(R.drawable.profile_picture)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .thumbnail(0.1f)
                            .into(object : CustomTarget<Bitmap?>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap?>?
                                ) {
                                    sentImage.setImageBitmap(resource)
                                    //profilePicture.startAnimation(fadeInAnim)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    }.addOnFailureListener { exception -> }
                }

                if (messages[position].replyText == "") {
                } else {
                    replyText.text = messages[position].replyText
                }

                if(replyText.text == "0") {
                    replyText.visibility = View.GONE
                }


                view.setOnTouchListener(object : OnSwipeTouchListener(context, messageListView, context as Conversation, view) {
                    override fun onSwipeRight() {
                        super.onSwipeRight()
                        //(context as Conversation).triggerReplyFunction(messages[position])
                    }

                    init {
                        (context as Conversation).swipedMessageText = messageTextView.text
                    }
                })



                val inputFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                val outputFormat = DateTimeFormatter.ofPattern("HH:mm")

                val utcDateTime = LocalDateTime.parse(messages[position].timestamp, inputFormat).atZone(ZoneOffset.UTC)
                val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                val formattedTime = localDateTime.format(outputFormat)

                timeTextView.text = formattedTime
                messageTextView.text = messages[position].text
                view.startAnimation(fadeInAnimation)
                view
            }
        }
    }


    fun messageExists(messageKey: String): Boolean {
        return messages.any { it.messageKey == messageKey }
    }

    private fun createFadeInAnimation(duration: Long): Animation {
        val fadeInAnimation = AlphaAnimation(0f, 1f)
        fadeInAnimation.duration = duration
        return fadeInAnimation
    }

}

open class OnSwipeTouchListener(
    ctx: Context,
    private val listView: ListView,
    private val conversation: Conversation,
    private val replyMessage: View
) : View.OnTouchListener {
    private val gestureDetector: GestureDetector
    private var initialX: Float = 0f
    private var initialTouchX: Float = 0f
    private var screenWidth: Int = 0
    private val context: Context = ctx

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
        screenWidth = ctx.resources.displayMetrics.widthPixels
    }

    private var messageReachedMiddle = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = v.x
                initialTouchX = event.rawX
                messageReachedMiddle = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val newX = initialX + deltaX

                if (newX > 0 && newX < screenWidth / 8) {
                    v.animate().x(newX).setDuration(0).start()

                    val marginOfError = 8
                    if (!messageReachedMiddle && newX >= (screenWidth / 8) - marginOfError) {
                        messageReachedMiddle = true
                        onMessageReachedMiddle()
                    }
                    if (!messageReachedMiddle && newX >= (screenWidth / 10) - marginOfError) {
                        val replyText = conversation.replyText
                        replyText.text = replyMessage.findViewById<TextView>(R.id.messageTextView).text.toString()
                        vibratePhone(context)
                    }

                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate().x(initialX).setDuration(200).start()
                messageReachedMiddle = false
            }
            else -> return false
        }
        return true
    }

    open fun onMessageReachedMiddle() {
        val cardView = conversation.cardView
        cardView.visibility = View.VISIBLE
        val close = conversation.close

        close.setOnClickListener {
            cardView.visibility = View.GONE
        }

        val message = conversation.findViewById<EditText>(R.id.message)
        showKeyboardAndFocusEditText(context, message)
    }

    fun scrollToBottom() {
        val adapter = listView.adapter
        listView.setSelection(adapter.count - 1)
    }

    fun showKeyboardAndFocusEditText(context: Context, editText: EditText) {
        editText.requestFocus()

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun vibratePhone(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom()
                        } else {
                            onSwipeTop()
                        }
                        result = true
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onSwipeTop() {}

    open fun onSwipeBottom() {}

    fun setScrollingEnabled(listView: ListView, enabled: Boolean) {
        listView.isNestedScrollingEnabled = enabled
        if (enabled) {
            listView.setOnTouchListener(null)
        } else {
            listView.setOnTouchListener { _, _ -> true }
        }
    }
}

object AESHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256

    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(KEY_SIZE, SecureRandom())
        return keyGen.generateKey()
    }

    fun encrypt(plainText: String, secretKey: SecretKey, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    }

    fun decrypt(encryptedText: ByteArray, secretKey: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(encryptedText).toString(Charsets.UTF_8)
    }
}








