package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.indexOf
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*
import kotlin.String

class MainPage : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var carRecommendationModel: CarRecommendationModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        var username = intent.getStringExtra("username")
        val loggedInUser = application as Username

        val bannerRef = Firebase.storage.reference.child("images/banner-$username")
        val profilePictureRef = Firebase.storage.reference.child("images/profile_picture-$username")

        var bannerUri: Uri? = null
        var profilePictureUri: Uri? = null

        bannerRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            bannerUri = getImageUriFromBytes(bytes)
            loggedInUser.bannerUri = bannerUri.toString()
            username?.let {}
        }.addOnFailureListener { exception ->
            // handle error
        }

        profilePictureRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            profilePictureUri = getImageUriFromBytes(bytes)
            loggedInUser.profilePictureUri = profilePictureUri.toString()
            username?.let {}
        }.addOnFailureListener { exception ->
            // handle error
        }

        carRecommendationModel = CarRecommendationModel(this)

        val cardView = findViewById<CardView>(R.id.cardView)
        val search = findViewById<EditText>(R.id.search)

        /*search.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val layoutParams = cardView.layoutParams
                layoutParams.height = (280 * resources.displayMetrics.density).toInt()
                cardView.layoutParams = layoutParams
            }
            false
        }*/


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navBar)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    username = loggedInUser.username
                    val intent = Intent(this@MainPage, Profile::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("bannerUri", bannerUri.toString())
                    intent.putExtra("profilePictureUri", profilePictureUri.toString())
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.map -> {
                    startActivity(Intent(this, MapsPage::class.java))
                    true
                }
                R.id.browse -> {

                    true
                }
                R.id.sellCar -> {
                    val intent = Intent(this@MainPage, SellCar::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                else -> false
            }
        }

        val database = Firebase.database.getReference("cars")
        val cars = mutableListOf<Item>()

        var mListView = findViewById<ListView>(R.id.carList)

        val adapter = ItemAdapter(this, cars)
        mListView.adapter = adapter

        mListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val clickedCardView = view.findViewById<CardView>(R.id.cardView)
            var registration = clickedCardView.findViewById<TextView>(R.id.reg).text.toString()

            val intent = Intent(this@MainPage, CarSalePage::class.java)
            intent.putExtra("carData", registration)
            startActivity(intent)

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot, @Nullable previousChildName: String?
            ) {
                val registration = snapshot.child("registration").getValue().toString()
                val make = snapshot.child("make").getValue().toString()
                val colour = snapshot.child("colour").getValue().toString()
                val mileage = snapshot.child("mileage").getValue().toString()
                val yearOfManufacture = snapshot.child("yearOfManufacture").getValue().toString()
                val price = snapshot.child("price").getValue().toString()
                val model = snapshot.child("model").getValue().toString()
                val condition = snapshot.child("condition").getValue().toString()
                val emissions = snapshot.child("co2Emissions").getValue().toString()
                val engineCapacity = snapshot.child("engineCapacity").getValue().toString()
                val fuelType = snapshot.child("fuelType").getValue().toString()

                val regex = Regex(",\\s*([a-zA-Z]+)\\s*[a-zA-Z]*\\s*\\d")
                val matchResult = regex.find(snapshot.child("address").getValue().toString())
                var address = "Unknown"

                if (matchResult != null) {
                    address = matchResult.groups[1]?.value.toString()
                }

                val storageRef = Firebase.storage.reference
                val image0Ref = storageRef.child("images/image0-" + registration)
                val image1Ref = storageRef.child("images/image1-" + registration)
                val image2Ref = storageRef.child("images/image2-" + registration)

                image2Ref.downloadUrl.addOnSuccessListener { uri ->
                    val image2Uri = uri
                    image1Ref.downloadUrl.addOnSuccessListener { uri ->
                        val image1Uri = uri
                        image0Ref.downloadUrl.addOnSuccessListener { uri ->
                            val image0Uri = uri
                            val item = Item(
                                image2Uri.toString(),
                                image1Uri.toString(),
                                image0Uri.toString(),
                                price,
                                make+" "+model,
                                yearOfManufacture,
                                mileage,
                                address,
                                condition,
                                registration)

                            cars.add(item)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onChildChanged(
                snapshot: DataSnapshot, @Nullable previousChildName: String?
            ) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(
                snapshot: DataSnapshot, @Nullable previousChildName: String?
            ) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val firstChildView = mListView.getChildAt(0)

        if (firstChildView != null) {
            firstChildView.setPadding(0, 100, 0, 0)
        }
    }

    fun preprocessData(carData: String): FloatArray {
        // Split the string into separate values
        val carValues = carData.split(":")

        // Extract the numerical and categorical features
        val numericalFeatures = intArrayOf(carValues[1].toInt(), carValues[4].toInt(), carValues[7].toInt())
        val categoricalFeatures = arrayOf(carValues[2], carValues[3], carValues[5], carValues[6], carValues[8])

        // Normalize the numerical features using MinMaxScaler
        val minMaxValues = arrayOf(
            Pair(0, 1000), // price
            Pair(1900, 2022), // year
            Pair(0, 300000) // mileage
        )

        val normalizedNumericalFeatures = numericalFeatures.mapIndexed { index, value ->
            val (min, max) = minMaxValues[index]
            (value - min).toFloat() / (max - min)
        }

        // One-hot encode the categorical features
        val colorCategories = listOf("BLUE", "RED", "SILVER", "BLACK", "WHITE", "OTHER")
        val conditionCategories = listOf("Perfect Condition", "Good Condition", "Fair Condition", "Poor Condition")
        val fuelCategories = listOf("DIESEL", "PETROL", "HYBRID", "ELECTRIC")
        val makeCategories = listOf("BMW", "MERCEDES", "AUDI", "TOYOTA", "HONDA", "OTHER")
        val modelCategories = listOf("3 Series", "C Class", "A4", "Corolla", "Civic", "OTHER")

        val oneHotColor = oneHotEncode(colorCategories, categoricalFeatures[0])
        val oneHotCondition = oneHotEncode(conditionCategories, categoricalFeatures[1])
        val oneHotFuel = oneHotEncode(fuelCategories, categoricalFeatures[2])
        val oneHotMake = oneHotEncode(makeCategories, categoricalFeatures[3])
        val oneHotModel = oneHotEncode(modelCategories, categoricalFeatures[4])

        // Concatenate the numerical and categorical features
        return floatArrayOf(
            *normalizedNumericalFeatures.toFloatArray(),
            *oneHotColor,
            *oneHotCondition,
            *oneHotFuel,
            *oneHotMake,
            *oneHotModel
        )
    }

    fun oneHotEncode(categories: List<String>, value: String): FloatArray {
        val index = categories.indexOf(value)
        return FloatArray(categories.size) { if (it == index) 1.0f else 0.0f }
    }

    private fun startProfileActivity(username: String, bannerUri: Uri, profilePictureUri: Uri) {
        val intent = Intent(this@MainPage, Profile::class.java)
        intent.putExtra("username", username)
        intent.putExtra("bannerUri", bannerUri.toString())
        intent.putExtra("profilePictureUri", profilePictureUri.toString())
        println("BANNER"+bannerUri.toString())
        println("PROFILE"+profilePictureUri.toString())
        startActivity(intent)
        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
    }

    private fun getImageUriFromBytes(bytes: ByteArray): Uri {
        val file = File.createTempFile("image", "jpg")
        file.writeBytes(bytes)
        return Uri.fromFile(file)
    }

}
