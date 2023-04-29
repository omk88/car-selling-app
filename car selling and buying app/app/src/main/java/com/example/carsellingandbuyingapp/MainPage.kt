package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class MainPage : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val tfliteModel = loadModelFile(this, "price_recommendation_model.tflite")
        val tflite = Interpreter(tfliteModel)

        var colour = "Red"
        var condition = "Spares Or Repairs"
        var make = "Ford"
        var model = "Focus"
        var mileage = 0
        var yearOfManufacture = 2000
        

        val colours = listOf("Black", "Blue", "Grey", "Red", "Silver", "White")
        val conditions = listOf("Good Condition", "Perfect Condition", "Poor Condition", "Spares Or Repairs")
        val makes = resources.getStringArray(R.array.make_items_2).toList()
        val models = resources.getStringArray(R.array.model_items).toList()

        val colourEncoded = oneHotEncode(colour, colours)
        val conditionEncoded = oneHotEncode(condition, conditions)
        val makeEncoded = oneHotEncode(make, makes)
        val modelEncoded = oneHotEncode(model, models)

        val scaledMileage = standardize(mileage.toDouble(), 356243.1805, 171069.5643724576)
        val scaledYearOfManufacture = standardize(yearOfManufacture.toDouble(), 1987.391, 12.732875519693106)


        println("Colour encoded length: ${colourEncoded.size}")
        println("Condition encoded length: ${conditionEncoded.size}")
        println("Make encoded length: ${makeEncoded.size}")
        println("Model encoded length: ${modelEncoded.size}")

        val mileageMean = 354108.950375
        val mileageStd = 171900.9895738319
        val yearOfManufactureMean = 1987.5845
        val yearOfManufactureStd = 12.89848284683125

        //val scaledMileage = (mileage - mileageMean) / mileageStd
        //val scaledYearOfManufacture = (yearOfManufacture - yearOfManufactureMean) / yearOfManufactureStd

        val currentYear = 2023
        val age = currentYear - yearOfManufacture
        val ageWeight = 0
        val weightedAge = age * ageWeight
        val ageSquared = weightedAge * weightedAge



        val inputArray = floatArrayOf(
            *colourEncoded.copyOfRange(0, colourEncoded.size),
            *conditionEncoded.copyOfRange(0, conditionEncoded.size),
            *makeEncoded.copyOfRange(0, makeEncoded.size),
            *modelEncoded.copyOfRange(0, modelEncoded.size),
            scaledMileage,
            scaledYearOfManufacture
        )

        println("TEST"+modelEncoded.contentToString())


        val correctedInputArray = floatArrayOf(
            *colourEncoded.copyOfRange(0, colourEncoded.size),
            *conditionEncoded.copyOfRange(0, conditionEncoded.size),
            *makeEncoded.copyOfRange(0, makeEncoded.size),
            *modelEncoded.copyOfRange(0, modelEncoded.size),
            scaledMileage,
            scaledYearOfManufacture
        )



        val inputTensor = tflite.getInputTensor(0)
        val inputShape = inputTensor.shape()
        val inputSize = inputTensor.numBytes()


        println("Input tensor shape: ${inputShape.contentToString()}")
        println("Input array shape: ${inputArray.size}")


        val inputBuffer = ByteBuffer.allocateDirect(inputSize)
            .order(ByteOrder.nativeOrder())

        val expectedInputArrayLength = inputShape[1]

        if (correctedInputArray.size != expectedInputArrayLength) {
            throw RuntimeException("Input array length (${inputArray.size}) does not match the expected length ($expectedInputArrayLength)")
        }


        inputBuffer.asFloatBuffer().put(inputArray)
        //inputBuffer.rewind()


        val outputShape = tflite.getOutputTensor(0).shape()
        val outputDataType = tflite.getOutputTensor(0).dataType()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)

        tflite.run(inputBuffer, outputBuffer.buffer)
        val outputArray = outputBuffer.floatArray

/*
        if (condition == "Poor Condition") {
            println("Predicted price: ${outputArray[0]*100}")
        } else if (condition == "Spares Or Repairs") {
            println("Predicted price: ${outputArray[0]*10}")
        } else if (condition == "Good Condition") {

        }
*/

        println("Predicted price: ${outputArray[0]}")

        println("Kotlin preprocessed input array: ${correctedInputArray.contentToString()}")

        println("Colour encoded: ${colourEncoded.contentToString()}")
        println("Condition encoded: ${conditionEncoded.contentToString()}")
        println("Make encoded: ${makeEncoded.contentToString()}")
        println("Model encoded: ${modelEncoded.contentToString()}")





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
                R.id.messages -> {
                    val intent = Intent(this@MainPage, StartConversation::class.java)
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
        }.addOnFailureListener { exception -> }

        profilePictureRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            profilePictureUri = getImageUriFromBytes(bytes)
            loggedInUser.profilePictureUri = profilePictureUri.toString()
            username?.let {}
        }.addOnFailureListener { exception -> }


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

    fun oneHotEncode(value: String, categories: List<String>): FloatArray {
        val index = categories.indexOf(value)
        val encoded = FloatArray(categories.size) { 0.0f }
        if (index >= 0 && index < categories.size) {
            encoded[index] = 1.0f
        }
        return encoded
    }

    fun standardize(value: Double, mean: Double, std: Double): Float {
        return ((value - mean) / std).toFloat()
    }



    fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

}