package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class Profile : AppCompatActivity() {
    private lateinit var adapter: ItemAdapter2

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val loggedInUser = application as Username

        val emptyList = findViewById<TextView>(R.id.emptyMessage)
        emptyList.visibility = View.VISIBLE

        val username = intent.getStringExtra("username").toString()

        val banner = findViewById<ImageView>(R.id.banner)
        val profilePicture = findViewById<ImageView>(R.id.profilePicture)

        val bannerUri = loggedInUser.bannerUri
        val profilePictureUri = loggedInUser.profilePictureUri

        val bannerImageUri = Uri.parse(bannerUri)
        Glide.with(this)
            .load(bannerImageUri)
            .into(banner)

        val profilePictureImageUri = Uri.parse(profilePictureUri)
        Glide.with(this)
            .load(profilePictureImageUri)
            .into(profilePicture)

        val editButton = findViewById<ImageView>(R.id.editButton)
        editButton.visibility = View.INVISIBLE

        val database = Firebase.database.getReference("cars")
        val cars = mutableListOf<Item>()

        val userText = findViewById<TextView>(R.id.username)
        val locationText = findViewById<TextView>(R.id.location)
        userText.text = username

        val databaseUsers = Firebase.database.getReference("users")

        databaseUsers.child(username).get().addOnSuccessListener {
            if (it.exists()) {
                val address = it.child("address").value.toString()

                val regex = Regex(",\\s*([a-zA-Z]+)\\s*[a-zA-Z]*\\s*\\d")
                val matchResult = regex.find(address)

                if (matchResult != null) {
                    val location = matchResult.groups[1]?.value.toString()
                    locationText.text = location
                }

            } else {
                Toast.makeText(this, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
            }
        }

        val mListView = findViewById<RecyclerView>(R.id.carList)
        mListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        if(loggedInUser.username == username)
        {
            editButton.visibility = View.VISIBLE

            editButton.setOnClickListener {
                val intent = Intent(this@Profile, EditProfile::class.java)
                intent.putExtra("username", username)
                intent.putExtra("bannerUri", bannerUri)
                intent.putExtra("profilePictureUri", profilePictureUri)
                startActivity(intent)
            }

            adapter = ItemAdapter2(cars, true)
        } else {
            adapter = ItemAdapter2(cars, false)
        }

        mListView.adapter = adapter

        adapter.setOnItemClickListener(object : ItemAdapter2.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val clickedItem = cars[position]
                val registration = clickedItem.text7

                val intent = Intent(this@Profile, CarSalePage::class.java)
                intent.putExtra("carData", registration)
                startActivity(intent)

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })


        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                val registration = snapshot.child("registration").value.toString()
                val make = snapshot.child("make").value.toString()
                val colour = snapshot.child("colour").value.toString()
                val mileage = snapshot.child("mileage").value.toString()
                val yearOfManufacture = snapshot.child("yearOfManufacture").value.toString()
                val price = snapshot.child("price").value.toString()
                val model = snapshot.child("model").value.toString()
                val condition = snapshot.child("condition").value.toString()
                val emissions = snapshot.child("co2Emissions").value.toString()
                val engineCapacity = snapshot.child("engineCapacity").value.toString()
                val fuelType = snapshot.child("fuelType").value.toString()

                val regex = Regex(",\\s*([a-zA-Z]+)\\s*[a-zA-Z]*\\s*\\d")
                val matchResult = regex.find(snapshot.child("address").value.toString())
                var address = "Unknown"

                if (matchResult != null) {
                    address = matchResult.groups[1]?.value.toString()
                }

                val storageRef = Firebase.storage.reference
                val image0Ref = storageRef.child("images/image0-$registration")
                val image1Ref = storageRef.child("images/image1-$registration")
                val image2Ref = storageRef.child("images/image2-$registration")

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

                            database.child(item.text7).get().addOnSuccessListener {
                                if (it.exists()) {
                                    val seller = it.child("seller").value.toString()
                                    if (seller == username) {
                                        cars.add(item)
                                        emptyList.visibility = View.INVISIBLE
                                        adapter.notifyDataSetChanged()
                                    }
                                }
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

}
