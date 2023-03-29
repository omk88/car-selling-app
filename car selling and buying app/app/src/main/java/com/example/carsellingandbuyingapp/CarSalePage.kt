package com.example.carsellingandbuyingapp

import android.content.ContentValues
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.IOException

class CarSalePage : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val geocoderCache = mutableMapOf<String, LatLng?>()
    private var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_sale_page)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val database = Firebase.database.getReference("cars")
        val storageRef = Firebase.storage.reference

        val textView = findViewById<TextView>(R.id.textView2)
        val registrationText = findViewById<TextView>(R.id.regText)
        val mileageText = findViewById<TextView>(R.id.mileageText)
        val userText = findViewById<TextView>(R.id.userText)
        val fuelTypeText = findViewById<TextView>(R.id.fuelTypeText)
        val colourText = findViewById<TextView>(R.id.colourText)
        val price = findViewById<TextView>(R.id.priceText)
        val co2EmissionsText = findViewById<TextView>(R.id.co2Text)
        val engineCapacity = findViewById<TextView>(R.id.engineText)

        userText.setOnClickListener{
            val username = userText.text.toString()
            val intent = Intent(this@CarSalePage, Profile::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }


        val registration = intent.getStringExtra("carData").toString()

        val image0 = storageRef.child("images/image0-"+registration)
        val image1 = storageRef.child("images/image1-"+registration)
        val image2 = storageRef.child("images/image2-"+registration)

        val image0View = findViewById<ImageView>(R.id.image0)
        val image1View = findViewById<ImageView>(R.id.image1)
        val image2View = findViewById<ImageView>(R.id.image2)

        val callSellerButton: Button = findViewById(R.id.callSeller)

        callSellerButton.setOnClickListener{
            database.child(registration).get().addOnSuccessListener {
                if(it.exists()) {
                    val username = it.child("seller").value.toString()
                    println(username)
                    val database = Firebase.database.getReference("users")
                    database.child(username).get().addOnSuccessListener {
                        if(it.exists()) {
                            val phoneNumber = it.child("phone").value.toString()
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                            }

                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }

        image0.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(image0View)
        }

        image1.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(image1View)
        }

        image2.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(image2View)
        }


        database.child(registration).get().addOnSuccessListener {
            if(it.exists()) {
                address = it.child("address").value.toString()
                val condition = it.child("condition").value.toString()
                price.setText(it.child("price").value.toString())
                registrationText.setText(it.child("registration").value.toString())
                mileageText.setText(it.child("mileage").value.toString())
                userText.setText(it.child("seller").value.toString())
                fuelTypeText.setText(it.child("fuelType").value.toString().lowercase().capitalize())
                colourText.setText(it.child("colour").value.toString().lowercase().capitalize())
                co2EmissionsText.setText(it.child("co2Emissions").value.toString())
                engineCapacity.setText(it.child("engineCapacity").value.toString())

                if(it.child("make").value.toString().split(" ").size == 2) {
                    textView.setText(
                        it.child("make").value.toString().split(" ")[0].lowercase()
                            .capitalize() + " " +
                                it.child("make").value.toString().split(" ")[1].lowercase()
                                    .capitalize() + " " + it.child("model").value.toString() + " (" +
                                it.child("yearOfManufacture").value.toString() + ") "
                    )
                } else if(it.child("make").value.toString().split(" ").size == 1) {
                    textView.setText(it.child("make").value.toString().lowercase().capitalize() + " " + it.child("model").value.toString() + " (" +
                            it.child("yearOfManufacture").value.toString() + ") ")
                }
            }
        }
    }

    private fun addMarkerForAddress(address: String, latLng: LatLng) {
        mMap.addMarker(MarkerOptions().position(latLng).title("Car for Sale at $address"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }

    private fun addressToLatLng(address: String): LatLng? {
        geocoderCache[address]?.let { return it }

        val geocoder = Geocoder(this)
        return try {
            val addressList = geocoder.getFromLocationName(address, 1)
            if (addressList.isNotEmpty()) {
                val location = addressList[0]
                val latLng = LatLng(location.latitude, location.longitude)
                geocoderCache[address] = latLng
                latLng
            } else {
                geocoderCache[address] = null
                null
            }
        } catch (e: IOException) {
            Log.e(ContentValues.TAG, "Geocoding error", e)
            geocoderCache[address] = null
            null
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latLng = addressToLatLng(address)
        Pair(address, latLng)

        if (latLng != null) {
            addMarkerForAddress(address, latLng)
        }
    }
}