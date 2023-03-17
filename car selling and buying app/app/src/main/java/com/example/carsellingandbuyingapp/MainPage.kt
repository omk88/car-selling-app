package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.String

class MainPage : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val username = intent.getStringExtra("username")

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navBar)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    val intent = Intent(this@MainPage, Profile::class.java)
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
    }
}
