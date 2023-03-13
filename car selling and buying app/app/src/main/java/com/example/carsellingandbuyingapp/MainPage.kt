package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class MainPage : AppCompatActivity() {

    private lateinit var listView: ListView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val database = Firebase.database.getReference("cars")
        val cars = mutableListOf(Item("", "text1"))

        var mListView = findViewById<ListView>(R.id.carList)
        val adapter = ItemAdapter(this, cars)
        mListView.adapter = adapter

        mListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val clickedCardView = view.findViewById<CardView>(R.id.cardView)
            val text = clickedCardView.findViewById<TextView>(R.id.textView).text.toString()

            val intent = Intent(this@MainPage, CarSalePage::class.java)
            intent.putExtra("carData", text)
            startActivity(intent)

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
            snapshot: DataSnapshot, @Nullable previousChildName: String?
        ) {
            var value = snapshot.getValue().toString()
            val registration = snapshot.child("registration").getValue().toString()

            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("images/image2-"+registration)

            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val item = Item(uri.toString(), value)
                println("Item"+item.imageUrl+" "+item.text)
                cars.add(item)
                adapter.notifyDataSetChanged()
                }.addOnFailureListener { exception ->
                Toast.makeText(this@MainPage, "Failed to load image: ${exception.message}", Toast.LENGTH_SHORT).show()
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

    fun switchToSellCar(view: View) {
        startActivity(Intent(this@MainPage,SellCar::class.java))
    }
}
