package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainPage : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val database = Firebase.database.getReference("cars")

        val arrayAdapter: ArrayAdapter<*>
        val cars = mutableListOf("")

        var mListView = findViewById<ListView>(R.id.carList)
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, cars)
        mListView.adapter = arrayAdapter

        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
            snapshot: DataSnapshot, @Nullable previousChildName: String?
        ) {
            var value = snapshot.getValue().toString()
            cars.add(value)
            arrayAdapter.notifyDataSetChanged()
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
