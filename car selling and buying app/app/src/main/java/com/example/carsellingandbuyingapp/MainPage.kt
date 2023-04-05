package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.ArrayList

class MainPage : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var carRecommendationModel: CarRecommendationModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)



        var selectedMake = intent.getStringExtra("selected_make")
        var selectedModel = intent.getStringExtra("selected_model")
        var selectedMinPrice = intent.getStringExtra("selected_minPrice")?.drop(1)
        var selectedMaxPrice = intent.getStringExtra("selected_maxPrice")?.drop(1)
        var selectedMinYear = intent.getStringExtra("selected_minYear")
        var selectedMaxYear = intent.getStringExtra("selected_maxYear")
        var selectedFuelType = intent.getStringExtra("selected_fuelType")
        var selectedMinEmissions = intent.getStringExtra("selected_minEmissions")
        var selectedMaxEmissions = intent.getStringExtra("selected_maxEmissions")
        var selectedColour = intent.getStringExtra("selected_colour")

        val loggedInUser = application as Username
        var username = loggedInUser.username



        val bannerRef = Firebase.storage.reference.child("images/banner-$username")
        val profilePictureRef = Firebase.storage.reference.child("images/profile_picture-$username")

        var bannerUri: Uri? = null
        var profilePictureUri: Uri? = null

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navBar)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    val intent = Intent(this@MainPage, Profile::class.java)
                    intent.putExtra("username", loggedInUser.username)
                    intent.putExtra("bannerUri", bannerUri)
                    intent.putExtra("profilePictureUri", profilePictureUri)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.map -> {
                    val intent = Intent(this@MainPage, MapsPage::class.java)
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
                    val intent = Intent(this@MainPage, SellCar::class.java)
                    intent.putExtra("username", loggedInUser.username)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                else -> false
            }
        }

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

        search.setOnClickListener {

            var selectedMinPriceIntent = ""
            var selectedMaxPriceIntent = ""

            if ((selectedMinPrice == "one") || (selectedMinPrice == null)) {
                selectedMinPriceIntent = "None"
            } else {
                selectedMinPriceIntent = "£" + selectedMinPrice
            }

            if ((selectedMaxPrice == "one") || (selectedMaxPrice == null)) {
                selectedMaxPriceIntent = "None"
            } else {
                selectedMaxPriceIntent = "£" + selectedMaxPrice
            }

            val intent = Intent(this, Search::class.java)
            intent.putExtra("selected_make", selectedMake)
            intent.putExtra("selected_model", selectedModel)
            intent.putExtra("selected_minPrice", selectedMinPriceIntent)
            intent.putExtra("selected_maxPrice", selectedMaxPriceIntent)
            intent.putExtra("selected_minYear", selectedMinYear)
            intent.putExtra("selected_maxYear", selectedMaxYear)
            intent.putExtra("selected_fuelType", selectedFuelType)
            intent.putExtra("selected_minEmissions", selectedMinEmissions)
            intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
            intent.putExtra("selected_colour", selectedColour)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, 0)
        }

        val database = Firebase.database.getReference("cars")
        val cars = mutableListOf<Item>()

        var mListView = findViewById<ListView>(R.id.carList)

        val adapter = ItemAdapter(this, cars)
        mListView.adapter = adapter

        mListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
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
                                make + " " + model,
                                yearOfManufacture,
                                mileage,
                                address,
                                condition,
                                registration
                            )

                            var flag = false


                            if ((selectedMake == null) && (selectedModel == null) && (selectedMinPrice == null)
                                && (selectedMaxPrice == null) && (selectedMinYear == null) && (selectedMaxYear == null)
                                && (selectedFuelType == null) && (selectedColour == null) && (selectedMinEmissions == null)
                                && (selectedMaxEmissions == null)) {
                                flag = true
                                cars.add(item)
                                adapter.notifyDataSetChanged()
                                val viewPosition = cars.size - 1
                                adapter.applyFadeInAnimation(viewPosition)
                            }

                            var makeFilter = false
                            var modelFilter = false
                            var priceFilter = false
                            var yearFilter = false
                            var fuelTypeFilter = false
                            var emissionsFilter = false
                            var colourFilter = false

                            if (selectedMake != "ALL") {
                                makeFilter = true
                            }

                            if (selectedModel != "ANY") {
                                modelFilter = true
                            }

                            if ((selectedMinPrice != "one") && (selectedMaxPrice != "one")) {
                                priceFilter = true
                            }

                            if ((selectedMinYear != "None") && (selectedMaxYear != "None")) {
                                yearFilter = true
                            }

                            if (selectedFuelType != "ALL") {
                                fuelTypeFilter = true
                            }

                            if (selectedFuelType != "ALL") {
                                colourFilter = true
                            }

                            if ((selectedMinEmissions != "None") && (selectedMaxEmissions != "None")) {
                                emissionsFilter = true
                            }

                            if (!flag &&
                                (!makeFilter || (selectedMake == make)) &&
                                (!modelFilter || (model == selectedModel)) &&
                                (!priceFilter || (selectedMinPrice != null && selectedMaxPrice != null && price.drop(1).toInt() >= selectedMinPrice.toInt() && price.drop(1).toInt() <= selectedMaxPrice.toInt())) &&
                                (!yearFilter || (yearOfManufacture.toInt() >= selectedMinYear?.toInt()?: 0 && yearOfManufacture.toInt() <= selectedMaxYear?.toInt()?: 0)) &&
                                (!fuelTypeFilter || (fuelType == selectedFuelType)) &&
                                (!emissionsFilter || (emissions.toInt() >= selectedMinEmissions?.toInt()?: 0 && emissions.toInt() <= selectedMaxEmissions?.toInt()?: 0)) &&
                                (!colourFilter || (selectedColour == colour))) {
                                flag = true
                                cars.add(item)
                                adapter.notifyDataSetChanged()
                                val viewPosition = cars.size - 1
                                adapter.applyFadeInAnimation(viewPosition)
                            }
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

    private fun fadeIn(view: View) {
        val fadeInAnim = AlphaAnimation(0.0f, 1.0f)
        fadeInAnim.duration = 500
        view.startAnimation(fadeInAnim)
    }

    private fun getImageUriFromBytes(bytes: ByteArray): Uri {
        val file = File.createTempFile("image", "jpg")
        file.writeBytes(bytes)
        return Uri.fromFile(file)
    }
}