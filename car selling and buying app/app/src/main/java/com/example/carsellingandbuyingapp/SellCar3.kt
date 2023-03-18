package com.example.carsellingandbuyingapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SellCar3 : AppCompatActivity(), View.OnClickListener {

    private var RESULT_LOAD_IMAGE: Int = 1
    private var imageUri0: Uri? = null
    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null
    private var imageNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_car3)

        val image0 = findViewById<ImageView>(R.id.upload0)
        image0.setOnClickListener(this)
        val image1 = findViewById<ImageView>(R.id.upload1)
        image1.setOnClickListener(this)
        val image2 = findViewById<ImageView>(R.id.upload2)
        image2.setOnClickListener(this)

        val sellCarButton = findViewById<Button>(R.id.postCar)
        sellCarButton.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.upload0-> {
                imageNumber = 0
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
            R.id.upload1-> {
                imageNumber = 1
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
            R.id.upload2-> {
                imageNumber = 2
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
            R.id.postCar-> {
                uploadImage()
                addCar()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMAGE && data != null) {

            val image0 = findViewById<ImageView>(R.id.upload0)
            val image1 = findViewById<ImageView>(R.id.upload1)
            val image2 = findViewById<ImageView>(R.id.upload2)

            when (imageNumber) {
                0 -> {
                    imageUri0 = data?.data
                    image0.setImageURI(imageUri0)
                }
                1 -> {
                    imageUri1 = data?.data
                    image1.setImageURI(imageUri1)
                }
                2 -> {
                    imageUri2 = data?.data
                    image2.setImageURI(imageUri2)
                }
            }
        }
    }

    private fun uploadImage() {
        val intent = intent
        val registration = intent.getStringExtra("registration")
        println("REG"+intent.getStringExtra("registration"))
        var storageRef = Firebase.storage.reference
        var pd = ProgressDialog(this@SellCar3)
        pd.setTitle("Posting Car...")
        pd.show()

        val carImage0 = storageRef.child("images/image0-$registration")
        val carImage1 = storageRef.child("images/image1-$registration")
        val carImage2 = storageRef.child("images/image2-$registration")

        val uris = arrayOf(imageUri0, imageUri1, imageUri2)
        val images = arrayOf(carImage0, carImage1, carImage2)

        for (i in 0..2) {
            uris[i]?.let {
                images[i].putFile(it).addOnSuccessListener {
                    if(i == 2) {
                        val loggedInUser = application as Username
                        val user = loggedInUser.username
                        val database = Firebase.database.getReference("users")
                        database.child(user).get().addOnSuccessListener {
                            if(it.exists()) {
                                println("EXISTS")
                                val address = it.child("address").toString()
                                val intent = Intent(this, MainPage::class.java)
                                intent.putExtra("address", address)
                                startActivity(intent)
                                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_out, androidx.appcompat.R.anim.abc_fade_in)
                                pd.dismiss()
                                Toast.makeText(this, "Car Posted!", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                }
            }?.addOnFailureListener {
                pd.dismiss()
                Toast.makeText(this, "Failed to Post Car.", Toast.LENGTH_SHORT).show()
            }?.addOnProgressListener {
                var progressPercent: Double = (100.0 * it.bytesTransferred / it.totalByteCount)
                pd.setMessage("Percentage: " + progressPercent.toInt() + "%")
            }
        }
    }

    private fun addCar() {
        println("AAAAAAAAAAAAA1")
        val databaseUsers = Firebase.database.getReference("users")
        val loggedInUser = application as Username
        val user = loggedInUser.username
        databaseUsers.child(user).get().addOnSuccessListener {
            println("AAAAAAAAAAAAA2"+loggedInUser.username)
            if(it.exists()) {
                val address = it.child("address").value.toString()
                println("ADDRESS"+address)
                val intent = intent
                val databaseCars = Firebase.database.getReference("cars")

                val registration = intent.getStringExtra("registration")
                val make = intent.getStringExtra("make")
                val colour = intent.getStringExtra("colour")
                val fuelType = intent.getStringExtra("fuelType")
                val registrationYear = intent.getStringExtra("registrationYear")
                val taxDueDate = intent.getStringExtra("taxDueDate")
                val mileage = intent.getStringExtra("mileage")
                val yearOfManufacture = intent.getStringExtra("yearOfManufacture")
                val price = intent.getStringExtra("price")
                val model = intent.getStringExtra("model")
                val co2Emissions = intent.getStringExtra("co2Emissions")
                val engineCapacity = intent.getStringExtra("engineCapacity")
                val username = user
                val condition = intent.getStringExtra("condition")

                val car = Car(registration, make, colour, fuelType, registrationYear, taxDueDate, mileage.toString(), yearOfManufacture, price, model, username, address, condition, co2Emissions, engineCapacity)
                databaseCars.child(registration.toString()).setValue(car).addOnSuccessListener { println("DONE!!") }.addOnFailureListener { println("FAILED :(") }
            }
        }.addOnFailureListener{println("FAILLLLLLLLL")}
    }
}


