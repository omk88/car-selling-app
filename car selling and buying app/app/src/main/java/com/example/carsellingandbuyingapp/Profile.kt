package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class Profile : AppCompatActivity() {
    private lateinit var adapter: ItemAdapter2
    var address: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val shimmerContainer1 = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container1)
        val shimmerContainer2 = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container2)
        shimmerContainer1.startShimmer()
        shimmerContainer2.startShimmer()

        val profile = this

        val verifiedPhone = findViewById<ImageView>(R.id.verifiedPhone)
        val verifiedEmail = findViewById<ImageView>(R.id.verifiedEmail)
        val ecoBadge1 = findViewById<ImageView>(R.id.ecoBadge1)
        val ecoBadge2 = findViewById<ImageView>(R.id.ecoBadge2)
        val ecoBadge3 = findViewById<ImageView>(R.id.ecoBadge3)
        val salesBadge1 = findViewById<ImageView>(R.id.salesBadge1)
        val salesBadge2 = findViewById<ImageView>(R.id.salesBadge2)
        val salesBadge3 = findViewById<ImageView>(R.id.salesBadge3)

        verifiedPhone.visibility = View.GONE
        verifiedEmail.visibility = View.GONE
        ecoBadge1.visibility = View.GONE
        ecoBadge2.visibility = View.GONE
        ecoBadge3.visibility = View.GONE
        salesBadge1.visibility = View.GONE
        salesBadge2.visibility = View.GONE
        salesBadge3.visibility = View.GONE

        val loggedInUser = application as Username

        val username = intent.getStringExtra("username").toString()

        val userText = findViewById<TextView>(R.id.username)
        val locationText = findViewById<TextView>(R.id.location)
        userText.text = username
        val databaseUsers = Firebase.database.getReference("users")
        val userRef = databaseUsers.child(username)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                if (snapshot.child("sale0").value.toString().toInt() == 1) {
                    salesBadge1.visibility = View.VISIBLE
                } else {
                    salesBadge1.visibility = View.GONE
                }

                if (snapshot.child("sale1").value.toString().toInt() == 1) {
                    salesBadge2.visibility = View.VISIBLE
                } else {
                    salesBadge2.visibility = View.GONE
                }

                if (snapshot.child("sale2").value.toString().toInt() == 1) {
                    salesBadge3.visibility = View.VISIBLE
                } else {
                    salesBadge3.visibility = View.GONE
                }

                if (snapshot.child("eco0").value.toString().toInt() == 1) {
                    ecoBadge1.visibility = View.VISIBLE
                } else {
                    ecoBadge1.visibility = View.GONE
                }

                if (snapshot.child("eco1").value.toString().toInt() == 1) {
                    ecoBadge2.visibility = View.VISIBLE
                } else {
                    ecoBadge2.visibility = View.GONE
                }

                if (snapshot.child("eco2").value.toString().toInt() == 1) {
                    ecoBadge3.visibility = View.VISIBLE
                } else {
                    ecoBadge3.visibility = View.GONE
                }
            }
        }

        val banner = findViewById<ImageView>(R.id.banner)
        val profilePicture = findViewById<ImageView>(R.id.profilePicture)

        val bannerUri = loggedInUser.bannerUri
        val profilePictureUri = loggedInUser.profilePictureUri

        val bannerImageUri = Uri.parse(bannerUri)

        val fadeInAnim = AlphaAnimation(0.0f, 1.0f)
        fadeInAnim.duration = 500

        Glide.with(this)
            .load(bannerImageUri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    shimmerContainer1.stopShimmer()
                    shimmerContainer1.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    shimmerContainer1.stopShimmer()
                    shimmerContainer1.visibility = View.GONE
                    banner.startAnimation(fadeInAnim)
                    return false
                }
            })
            .error(R.drawable.banner)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .thumbnail(0.1f)
            .into(banner)


        val profilePictureImageUri = Uri.parse(profilePictureUri)

        Glide.with(this)
            .load(profilePictureImageUri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    shimmerContainer2.stopShimmer()
                    shimmerContainer2.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    shimmerContainer2.stopShimmer()
                    shimmerContainer2.visibility = View.GONE
                    profilePicture.startAnimation(fadeInAnim)
                    return false
                }
            })
            .error(R.drawable.profile_picture)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .thumbnail(0.1f)
            .into(profilePicture)


        val editButton = findViewById<ImageView>(R.id.editButton)
        editButton.visibility = View.INVISIBLE

        val database = Firebase.database.getReference("cars")
        val cars = mutableListOf<Item>()


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navBar)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> {
                    val intent = Intent(this@Profile, Profile::class.java)
                    intent.putExtra("username", loggedInUser.username)
                    intent.putExtra("bannerUri", bannerUri)
                    intent.putExtra("profilePictureUri", profilePictureUri)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.map -> {
                    val intent = Intent(this@Profile, MapsPage::class.java)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.browse -> {
                    val intent = Intent(this, MainPage::class.java)
                    intent.putExtra("address", address)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                R.id.sellCar -> {
                    val intent = Intent(this@Profile, SellCar::class.java)
                    intent.putExtra("username", loggedInUser.username)
                    startActivity(intent)
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    true
                }
                else -> false
            }
        }

        val showPopupButton = findViewById<LinearLayout>(R.id.callSeller)

        val ecoSalesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val ecoSales = dataSnapshot.getValue(Int::class.java) ?: 0

                when {
                    ecoSales in 1..19 -> {
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                if (snapshot.child("eco0").value.toString().toInt() == 0) {
                                    userRef.child("eco0").setValue("1")
                                    showPopupWindow(showPopupButton, profile, 1)
                                }

                                ecoBadge1.visibility = View.VISIBLE
                            }
                        }
                    }
                    ecoSales in 20..99 -> {
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                if (snapshot.child("eco1").value.toString().toInt() == 0) {
                                    userRef.child("eco1").setValue("1")
                                    showPopupWindow(showPopupButton, profile, 2)
                                }

                                ecoBadge2.visibility = View.VISIBLE
                            }
                        }
                    }
                    ecoSales >= 100-> {
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                if (snapshot.child("eco2").value.toString().toInt() == 0) {
                                    userRef.child("eco2").setValue("1")
                                    showPopupWindow(showPopupButton, profile, 3)
                                }

                                ecoBadge3.visibility = View.VISIBLE
                            }
                        }
                    }
                    else -> {
                        ecoBadge1.visibility = View.GONE
                        ecoBadge2.visibility = View.GONE
                        ecoBadge3.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        }

        userRef.child("ecoSales").addValueEventListener(ecoSalesListener)

        val salesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sales = dataSnapshot.getValue(Int::class.java) ?: 0

                when {
                    sales in 10..49 -> {
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                if (snapshot.child("sale0").value.toString().toInt() == 0) {
                                    userRef.child("sale0").setValue("1")
                                    showPopupWindow(showPopupButton, profile, 4)
                                }

                                salesBadge1.visibility = View.VISIBLE
                            }
                        }
                    }
                    sales in 50..199 -> {
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                if (snapshot.child("sale1").value.toString().toInt() == 0) {
                                    userRef.child("sale1").setValue("1")
                                    showPopupWindow(showPopupButton, profile, 5)
                                }

                                salesBadge2.visibility = View.VISIBLE
                            }
                        }
                    }
                    sales >= 200 -> {
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                if (snapshot.child("sale2").value.toString().toInt() == 0) {
                                    userRef.child("sale2").setValue("1")
                                    showPopupWindow(showPopupButton, profile, 6)
                                }

                                salesBadge3.visibility = View.VISIBLE
                            }
                        }
                    }
                    else -> {
                        salesBadge1.visibility = View.GONE
                        salesBadge2.visibility = View.GONE
                        salesBadge3.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        }
        userRef.child("sales").addValueEventListener(salesListener)


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

            adapter = ItemAdapter2(cars, true, loggedInUser, username)
        } else {
            adapter = ItemAdapter2(cars, false, loggedInUser, username)
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

    private fun startConfettiEffect(konfettiView: nl.dionsegijn.konfetti.KonfettiView) {
        konfettiView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                konfettiView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                konfettiView.build()
                    .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE)
                    .setDirection(0.0, 359.0)
                    .setSpeed(2f, 7f)
                    .setFadeOutEnabled(true)
                    .setTimeToLive(750L)
                    .addShapes(Shape.Square, Shape.Circle)
                    .addSizes(Size(6), Size(8, 4f))
                    .setPosition(konfettiView.width / 2f, konfettiView.width / 2f, -50f, -50f) // Adjusted position to top center
                    .streamFor(300, 1000L)
            }
        })
    }






    private fun showPopupWindow(showPopupButton: View, profile: Profile, award: Int) {

        val fadeInAnimation = AnimationUtils.loadAnimation(profile, androidx.appcompat.R.anim.abc_fade_in)
        val fadeOutAnimation = AnimationUtils.loadAnimation(profile, androidx.appcompat.R.anim.abc_fade_out)

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_layout, null)

        val awardImageView = popupView.findViewById<ImageView>(R.id.award)
        val awardTextView = popupView.findViewById<TextView>(R.id.awardText)

        if (award == 1) {
            awardImageView.setImageResource(R.drawable.badge)
            awardTextView.text = "You have been awarded the eco-friendly badge for selling your first low emissions car!"
        } else if (award == 2) {
            awardTextView.text = "You have been awarded the 20 eco sales badge for selling your twentieth low emissions car!"
            awardImageView.setImageResource(R.drawable.earth)
        } else if (award == 3) {
            awardTextView.text = "You have been awarded the 100 eco sales badge for selling your one-hundredth low emissions car!"
            awardImageView.setImageResource(R.drawable.ecofriendly)
        } else if (award == 4) {
            awardTextView.text = "You have been awarded the 10 sales badge for selling your tenth car!"
            awardImageView.setImageResource(R.drawable.trophy0)
        } else if (award == 5) {
            awardTextView.text = "You have been awarded the 50 sales badge for selling your fifthtieth car!"
            awardImageView.setImageResource(R.drawable.trophy1)
        } else if (award == 6) {
            awardTextView.text = "You have been awarded the 200 sales badge for selling your two-hundredth car!"
            awardImageView.setImageResource(R.drawable.trophy2)
        }

        popupView.startAnimation(fadeInAnimation)

        val dimmedBackground = View(this)
        dimmedBackground.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        dimmedBackground.setBackgroundColor(Color.parseColor("#80000000"))

        val frameLayout = FrameLayout(this)
        frameLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        frameLayout.addView(dimmedBackground)
        frameLayout.addView(popupView)

        val konfettiView = nl.dionsegijn.konfetti.KonfettiView(profile)
        konfettiView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        frameLayout.addView(konfettiView)

        startConfettiEffect(konfettiView)

        val popupWindow = PopupWindow(frameLayout, WRAP_CONTENT, WRAP_CONTENT, true)

        popupWindow.setBackgroundDrawable(BitmapDrawable())
        popupWindow.showAtLocation(showPopupButton, Gravity.CENTER, 0, 0)

        val dismissButton = popupView.findViewById<Button>(R.id.claim)

        dismissButton.setOnClickListener {
            popupView.startAnimation(fadeOutAnimation)

            fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    popupWindow.dismiss()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }

        dimmedBackground.setOnTouchListener { _, _ ->
            popupView.startAnimation(fadeOutAnimation)

            fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    popupWindow.dismiss()
                }
                override fun onAnimationRepeat(animation: Animation) {}
            })
            true
        }
    }

}
