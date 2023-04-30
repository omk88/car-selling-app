package com.example.carsellingandbuyingapp

import kotlin.math.sqrt

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*

class MainPage : AppCompatActivity() {

    private lateinit var inputPreferences: List<Pair<String, String>>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

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

        val rankedCarBrandNamesIndexMap = mutableMapOf<String, Int>()

        main(object : OnRankedCarBrandsReceived {
            override fun onReceived(rankedCarBrands: List<Unit>) {
                val rankedCarBrandNames = rankedCarBrands
                rankedCarBrandNames.forEachIndexed { index, brand ->
                    rankedCarBrandNamesIndexMap[brand.toString()] = index
                }

                println("RANKKED"+ "")
            }
        })




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
                val priceType = snapshot.child("priceType").getValue().toString()

                val regex = Regex(",\\s*([a-zA-Z]+)\\s*[a-zA-Z]*\\s*\\d")
                val matchResult = regex.find(snapshot.child("address").getValue().toString())
                var address = ""

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
                                registration,
                                priceType
                            )

                            var flag = false


                            if ((selectedMake == null) && (selectedModel == null) && (selectedMinPrice == null)
                                && (selectedMaxPrice == null) && (selectedMinYear == null) && (selectedMaxYear == null)
                                && (selectedFuelType == null) && (selectedColour == null) && (selectedMinEmissions == null)
                                && (selectedMaxEmissions == null)) {
                                flag = true

                                val insertIndex = findCarInsertIndex(rankedCarBrandNamesIndexMap, cars, item)

                                println("INDEXXX"+ insertIndex)

                                cars.add(insertIndex, item)
                                adapter.notifyDataSetChanged()
                                adapter.applyFadeInAnimation(insertIndex)
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

                                val insertIndex = findCarInsertIndex(rankedCarBrandNamesIndexMap, cars, item)

                                println("INDEXXX"+ insertIndex)

                                cars.add(insertIndex, item)
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

    fun mapInput(input: String, category: String): Double {
        val useEncoding = mapOf("Leisure and Recreation" to 1.0, "Commuting" to 0.0, "General Use" to 0.5, "Work Vehicle" to 0.75)
        val priceEncoding = mapOf("Below £1000" to 0.0, "£1000 to £10,000" to 0.0, "£10,000 to £50,000" to 0.5, "£50,000+" to 1.0)
        val bodyEncoding = mapOf("Saloon" to 0.0, "SUV" to 0.25, "Estate" to 0.5, "Coupe" to 0.75, "Hatchback" to 1.0, "Other / No preference" to 0.625)
        val ecoPerfLuxResaleEncoding = mapOf("Very Important" to 1.0, "Somewhat Important" to 0.5, "Not Important" to 0.0)

        val encodingDict = mapOf(
            "Use" to useEncoding,
            "Price" to priceEncoding,
            "Body" to bodyEncoding,
            "Eco" to ecoPerfLuxResaleEncoding,
            "Performance" to ecoPerfLuxResaleEncoding,
            "Luxury" to ecoPerfLuxResaleEncoding,
            "Resale" to ecoPerfLuxResaleEncoding
        )

        return encodingDict[category]?.get(input) ?: 0.0
    }

    fun cosineSimilarity(v1: List<Double>, v2: List<Double>): Double {
        val dotProduct = v1.zip(v2).sumByDouble { (a, b) -> a * b }
        val mag1 = sqrt(v1.sumByDouble { it * it })
        val mag2 = sqrt(v2.sumByDouble { it * it })

        return dotProduct / (mag1 * mag2)
    }

    fun predictCarBrands(inputPreferences: List<Pair<String, String>>, carBrands: List<CarBrand>): List<CarBrand> {
        val inputVector = inputPreferences.map { mapInput(it.second, it.first) }
        val similarities = carBrands.map { carBrand ->
            val carBrandVector = listOf(carBrand.use, carBrand.price, carBrand.body, carBrand.eco, carBrand.performance, carBrand.luxury, carBrand.resale)
            val similarity = cosineSimilarity(inputVector, carBrandVector)
            carBrand.copy(name = carBrand.name, use = similarity) // Using 'use' field to store similarity, you can create another field if needed
        }
        return similarities.sortedByDescending { it.use }
    }

    fun convertToCarBrandList(data: List<Map<String, String>>): List<CarBrand> {
        return data.map { item ->
            CarBrand(
                name = item["Car Brand"] ?: "",
                use = mapInput(item["Use"] ?: "", "Use"),
                price = mapInput(item["Price"] ?: "", "Price"),
                body = mapInput(item["Body"] ?: "", "Body"),
                eco = mapInput(item["Eco"] ?: "", "Eco"),
                performance = mapInput(item["Performance"] ?: "", "Performance"),
                luxury = mapInput(item["Luxury"] ?: "", "Luxury"),
                resale = mapInput(item["Resale"] ?: "", "Resale")
            )
        }
    }

    private fun findCarInsertIndex(rankedCarBrandNamesIndexMap: Map<String, Int>, cars: MutableList<Item>, car: Item): Int {
        val carRank = rankedCarBrandNamesIndexMap[car.text2] ?: Int.MAX_VALUE
        var insertIndex = 0

        for (i in cars.indices) {
            val otherCar = cars[i]
            val otherCarRank = rankedCarBrandNamesIndexMap[otherCar.text2] ?: Int.MAX_VALUE
            if (carRank < otherCarRank) {
                break
            }
            insertIndex++
        }

        return insertIndex
    }



    fun main(callback: OnRankedCarBrandsReceived): Unit {
        val data = listOf(
            mapOf(
                "Car Brand" to "Abarth",
                "Use" to "Leisure and Recreation",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Ac",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Aixam",
                "Use" to "Commuting",
                "Price" to "£1000 to £10,000",
                "Eco" to "Very Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Alfa Romeo",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Alpine",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Ariel",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Very Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Aston Martin",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Very Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Audi",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Austin",
                "Use" to "General Use",
                "Price" to "£1000 to £10,000",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Bentley",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Very Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Bmw",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Bugatti",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Very Important",
                "Resale" to "Very Important"
            ),
            mapOf(
                "Car Brand" to "Cadillac",
                "Use" to "General Use",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Very Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Chevrolet",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Chrysler",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Citroen",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Corvette",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Cupra",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Dacia",
                "Use" to "Commuting",
                "Price" to "£1000 to £10,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Daewoo",
                "Use" to "Commuting",
                "Price" to "£1000 to £10,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Daf",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Daihatsu",
                "Use" to "Commuting",
                "Price" to "£1000 to £10,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Daimler",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Datsun",
                "Use" to "Commuting",
                "Price" to "£1000 to £10,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "De Tomaso",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Dfsk",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Dodge",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Ds Automobiles",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Ferrari",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Very Important"
            ),
            mapOf(
                "Car Brand" to "Fiat",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Ford",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Genesis Motor",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Gmc",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Great Wall",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Gwm Ora",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Very Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Hillman",
                "Use" to "General Use",
                "Price" to "Below £1000",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Holden",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Honda",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Hummer",
                "Use" to "Work Vehicle",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Hyundai",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Infiniti",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Isuzu",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Jaguar",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Very Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Jeep",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Jensen",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Kia",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Lagonda",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Very Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Lamborghini",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Very Important"
            ),
            mapOf(
                "Car Brand" to "Land Rover",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Very Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Levc",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Very Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Lexus",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Very Important",
                "Resale" to "Very Important"
            ),
            mapOf(
                "Car Brand" to "Lincoln",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Very Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Lotus",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Maserati",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Very Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Maxus",
                "Use" to "Work Vehicle",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Maybach",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Very Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Mazda",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Mclaren",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Very Important"
            ),
            mapOf(
                "Car Brand" to "Mg",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Mini",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Mitsubishi",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Moke",
                "Use" to "Leisure and Recreation",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Morgan",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Nissan",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Noble",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Opel",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Panther",
                "Use" to "Leisure and Recreation",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Perodua",
                "Use" to "Commuting",
                "Price" to "Below £1000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Peugeot",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Polaris",
                "Use" to "Leisure and Recreation",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Polestar",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Very Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Pontiac",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Porsche",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Very Important"
            ),
            mapOf(
                "Car Brand" to "Proton",
                "Use" to "Commuting",
                "Price" to "£1000 to £10,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Radical",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Very Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Renault",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Rolls-Royce",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Very Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Rover",
                "Use" to "General Use",
                "Price" to "£1000 to £10,000",
                "Eco" to "Not Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Saab",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Saloon",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Seat",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Shelby",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Coupe",
                "Performance" to "Very Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Skoda",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Smart",
                "Use" to "Commuting",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Very Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Ssangyong",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Subaru",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Somewhat Important",
                "Luxury" to "Not Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Suzuki",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Hatchback",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            ),
            mapOf(
                "Car Brand" to "Tesla",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Very Important",
                "Body" to "Saloon",
                "Performance" to "Somewhat Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Volvo",
                "Use" to "General Use",
                "Price" to "£10,000 to £50,000",
                "Eco" to "Somewhat Important",
                "Body" to "Estate",
                "Performance" to "Not Important",
                "Luxury" to "Somewhat Important",
                "Resale" to "Somewhat Important"
            ),
            mapOf(
                "Car Brand" to "Zimmer",
                "Use" to "Leisure and Recreation",
                "Price" to "£50,000+",
                "Eco" to "Not Important",
                "Body" to "Other / No preference",
                "Performance" to "Not Important",
                "Luxury" to "Not Important",
                "Resale" to "Not Important"
            )
        )

        var rankedCarBrandNames = "".map {}

        val carBrands = convertToCarBrandList(data)

        val database = Firebase.database.getReference("preferences")

        val loggedInUser = application as Username

        val userPreferencesRef = database.child(loggedInUser.username)

        val preferencesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userPreferencesData = dataSnapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})

                    if (userPreferencesData != null) {

                        val body = userPreferencesData["body"].toString()
                        val eco = userPreferencesData["eco"].toString()
                        val performance = userPreferencesData["performance"].toString()
                        val price = userPreferencesData["price"].toString()
                        val use = userPreferencesData["use"].toString()
                        val luxury = userPreferencesData["luxury"].toString()

                        inputPreferences = listOf(
                            Pair("Use", use),
                            Pair("Price", price),
                            Pair("Eco", eco),
                            Pair("Body", body),
                            Pair("Performance", performance),
                            Pair("Luxury", luxury)
                        )

                        val rankedCarBrands = predictCarBrands(inputPreferences, carBrands)
                        rankedCarBrandNames = rankedCarBrands.map { it.name }
                        callback.onReceived(rankedCarBrandNames)

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        userPreferencesRef.addValueEventListener(preferencesListener)

    }

    interface OnRankedCarBrandsReceived {
        fun onReceived(rankedCarBrands: List<Unit>)
    }

}

data class CarBrand(
    val name: String,
    val use: Double,
    val price: Double,
    val body: Double,
    val eco: Double,
    val performance: Double,
    val luxury: Double,
    val resale: Double
)