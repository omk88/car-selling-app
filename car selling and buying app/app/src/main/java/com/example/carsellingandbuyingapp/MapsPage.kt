package com.example.carsellingandbuyingapp

import android.content.ContentValues.TAG
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

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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