package com.example.carsellingandbuyingapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException
import kotlinx.coroutines.*


class MapsPage : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var flag = false
    private val geocoderCache = mutableMapOf<String, LatLng?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val loggedInUser = application as Username

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navBar)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    val username = loggedInUser.username
                    val intent = Intent(this@MapsPage, Profile::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.map -> {
                    startActivity(Intent(this, MapsPage::class.java))
                    true
                }
                R.id.browse -> {
                    val intent = Intent(this, MainPage::class.java)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_out, androidx.appcompat.R.anim.abc_fade_in)
                    true
                    true
                }
                R.id.sellCar -> {
                    val intent = Intent(this@MapsPage, SellCar::class.java)
                    intent.putExtra("username", loggedInUser.username)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                else -> false
            }
        }

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
            Log.e(TAG, "Geocoding error", e)
            geocoderCache[address] = null
            null
        }
    }


    private fun addMarkerForAddress(address: String, latLng: LatLng) {
        mMap.addMarker(MarkerOptions().position(latLng).title("Car for Sale at $address"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }


    fun getAllElementsFromCollection(collectionName: String, onDataLoaded: (List<String>) -> Unit) {
        val database = Firebase.database
        val collectionReference = database.getReference(collectionName)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addressList = mutableListOf<String>()

                for (childSnapshot in snapshot.children) {
                    val addressSnapshot = childSnapshot.child("address")
                    val addressValue = addressSnapshot.value.toString()

                    if (addressValue.isNotBlank()) {
                        addressList.add(addressValue)
                    }
                }

                onDataLoaded(addressList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Error retrieving data: ", error.toException())
            }
        }

        collectionReference.addListenerForSingleValueEvent(valueEventListener)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        flag = true

        val collectionName = "cars"

        getAllElementsFromCollection(collectionName) { elements ->
            CoroutineScope(Dispatchers.Main).launch {
                elements.map { element ->
                    async(Dispatchers.IO) {
                        val address = element
                        val latLng = addressToLatLng(address)
                        Pair(address, latLng)
                    }
                }.awaitAll().forEach { (address, latLng) ->
                    if (latLng != null) {
                        addMarkerForAddress(address, latLng)
                    }
                }
            }
        }
    }

}