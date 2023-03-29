package com.example.carsellingandbuyingapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class EditProfile : AppCompatActivity(), View.OnClickListener {

    private var RESULT_LOAD_IMAGE: Int = 1
    private var bannerUri: Uri? = null
    private var profilePictureUri: Uri? = null
    private var imageNumber: Int = 0
    private val AUTOCOMPLETE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val username = intent.getStringExtra("username").toString()

        val saveDetails = findViewById<Button>(R.id.saveDetails)

        saveDetails.setOnClickListener{
            uploadImage(username)
        }

        Places.initialize(applicationContext, "AIzaSyBCTCIpS4t1m9HgmCuUowaoxKSa7vJQShw")

        val customAutocompleteEditText = findViewById<EditText>(R.id.custom_autocomplete_edit_text)

        customAutocompleteEditText.setOnClickListener {
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            )
                .setTypeFilter(TypeFilter.ADDRESS)
                .setCountry("GB")
                .build(this)
            startActivityForResult(intent, 2)
        }

        val banner = findViewById<ImageView>(R.id.banner)
        banner.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)

        val profilePicture = findViewById<ImageView>(R.id.profilePicture)
        profilePicture.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)

        banner.setOnClickListener(this)
        profilePicture.setOnClickListener(this)

        val bannerImageUri = Uri.parse(intent.getStringExtra("bannerUri"))
        Glide.with(this)
            .load(bannerImageUri)
            .into(banner)

        val profilePictureImageUri = Uri.parse(intent.getStringExtra("profilePictureUri"))
        Glide.with(this)
            .load(profilePictureImageUri)
            .into(profilePicture)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.banner-> {
                imageNumber = 0
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
            R.id.profilePicture-> {
                imageNumber = 1
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val customAutocompleteEditText = findViewById<EditText>(R.id.custom_autocomplete_edit_text)
                customAutocompleteEditText.setText(place.address)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.e(ContentValues.TAG, "Error: Status = ${status.statusMessage}")
            }
        }

        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMAGE && data != null) {

            val image0 = findViewById<ImageView>(R.id.banner)
            val image1 = findViewById<ImageView>(R.id.profilePicture)

            when (imageNumber) {
                0 -> {
                    bannerUri = data?.data
                    image0.setImageURI(bannerUri)
                }
                1 -> {
                    profilePictureUri = data?.data
                    image1.setImageURI(profilePictureUri)
                }
            }
        }
    }

    private fun uploadImage(username: String) {
        var storageRef = Firebase.storage.reference
        var pd = ProgressDialog(this@EditProfile)
        pd.setTitle("Saving Details...")
        pd.show()

        val banner = storageRef.child("images/banner-$username")
        val profilePicture = storageRef.child("images/profile_picture-$username")

        val uris = arrayOf(bannerUri, profilePictureUri)
        val images = arrayOf(banner, profilePicture)

        for (i in 0..1) {
            uris[i]?.let {
                images[i].putFile(it).addOnSuccessListener {
                    if(i == 1) {
                        val loggedInUser = application as Username
                        val user = loggedInUser.username
                        val database = Firebase.database.getReference("users")
                        database.child(user).get().addOnSuccessListener {
                            if(it.exists()) {
                                val loggedInUser = application as Username
                                val customAutocompleteEditText = findViewById<EditText>(R.id.custom_autocomplete_edit_text)
                                var address = customAutocompleteEditText.text.toString()

                                val editTxt = findViewById<EditText>(R.id.editTextTextPersonName)
                                var username = editTxt.text.toString()

                                val passTxt = findViewById<EditText>(R.id.editTextTextPassword2)
                                var password = passTxt.text.toString()

                                val phoneTxt = findViewById<EditText>(R.id.editTextPhoneNumber)
                                var phone = phoneTxt.text.toString()

                                if (address == "") {
                                    address = it.child("address").value.toString()
                                }

                                if (username == "") {
                                    username = it.child("username").value.toString()
                                }

                                if (password == "") {
                                    password = it.child("password").value.toString()
                                }

                                if (phone == "") {
                                    phone = it.child("phone").value.toString()
                                }

                                val database = Firebase.database.getReference("users")
                                val values = User(username, password, phone, address)
                                val deleteRef = Firebase.database.getReference("users/"+loggedInUser.username)
                                deleteRef.removeValue()
                                database.child(username).setValue(values)
                                loggedInUser.username = username

                                loggedInUser.profilePictureUri = profilePictureUri.toString()
                                loggedInUser.bannerUri = bannerUri.toString()
                                val intent = Intent(this, Profile::class.java)
                                intent.putExtra("username", username)
                                startActivity(intent)
                                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_out, androidx.appcompat.R.anim.abc_fade_in)
                                pd.dismiss()
                                Toast.makeText(this, "Saved Details!", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                }
            }?.addOnFailureListener {
                pd.dismiss()
                Toast.makeText(this, "Failed to save details.", Toast.LENGTH_SHORT).show()
            }?.addOnProgressListener {
                var progressPercent: Double = (100.0 * it.bytesTransferred / it.totalByteCount)
                pd.setMessage("Percentage: " + progressPercent.toInt() + "%")
            }
        }
    }
}