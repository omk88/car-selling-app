package com.example.carsellingandbuyingapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CarSalePage : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_sale_page)
        val database = Firebase.database.getReference("cars")
        val storageRef = Firebase.storage.reference

        val textView = findViewById<TextView>(R.id.textView2)
        val registrationText = findViewById<TextView>(R.id.regText)
        val mileageText = findViewById<TextView>(R.id.mileageText)
        val userText = findViewById<TextView>(R.id.userText)
        val fuelTypeText = findViewById<TextView>(R.id.fuelTypeText)
        val colourText = findViewById<TextView>(R.id.colourText)
        val price = findViewById<TextView>(R.id.priceText)
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
                price.setText(it.child("price").value.toString())
                registrationText.setText(it.child("registration").value.toString())
                mileageText.setText(it.child("mileage").value.toString())
                userText.setText(it.child("seller").value.toString())
                fuelTypeText.setText(it.child("fuelType").value.toString().lowercase().capitalize())
                colourText.setText(it.child("colour").value.toString().lowercase().capitalize())
                if(it.child("make").value.toString().split(" ").size == 2) {
                    textView.setText(
                        it.child("make").value.toString().split(" ")[0].lowercase()
                            .capitalize() + " " +
                                it.child("make").value.toString().split(" ")[1].lowercase()
                                    .capitalize() + " " + it.child("model").value.toString() + " (" +
                                it.child("yearOfManufacture").value.toString() + ") "
                    )
                } else if(it.child("make").value.toString().split(" ").size == 1) {
                    textView.setText(it.child("make").value.toString().lowercase().capitalize() + it.child("model").value.toString() + " (" +
                            it.child("yearOfManufacture").value.toString() + ") ")
                }
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {}
}