package com.example.carsellingandbuyingapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

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

        saveDetails.setOnClickListener {
            lifecycleScope.launch {
                uploadImage(username)
            }
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

    private suspend fun uploadImage(username: String) = coroutineScope {
        val storageRef = FirebaseStorage.getInstance().reference
        val pd = ProgressDialog(this@EditProfile)
        pd.setTitle("Saving Details...")
        pd.show()

        val banner = storageRef.child("images/banner-$username")
        val profilePicture = storageRef.child("images/profile_picture-$username")

        val uris = arrayOf(bannerUri, profilePictureUri)
        val images = arrayOf(banner, profilePicture)

        val compressedAndResizedImages = uris.map {
            try {
                compressAndResizeImage(it)
            } catch (e: IllegalArgumentException) {
                Log.e("EditProfile", "Failed to compress and resize image: ${e.message}")
                ByteArray(0)
            }
        }.toTypedArray()

        val uploadResults = mutableListOf<Boolean>()

        for (i in 0..1) {
            launch {
                val result = uploadSingleImage(uris[i], images[i], compressedAndResizedImages[i])
                uploadResults.add(result)
            }
        }

        // Wait for all uploads to complete
        val allUploadsSuccessful = withContext(Dispatchers.Default) {
            uploadResults.all { it }
        }

        pd.dismiss() // Dismiss the progress dialog

        if (allUploadsSuccessful) {
            // Handle successful uploads
            val loggedInUser = application as Username
            loggedInUser.profilePictureUri = profilePictureUri.toString()
            loggedInUser.bannerUri = bannerUri.toString()

            val intent = Intent(this@EditProfile, Profile::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_out, androidx.appcompat.R.anim.abc_fade_in)
            Toast.makeText(this@EditProfile, "Saved Details!", Toast.LENGTH_SHORT).show()
        } else {
            // Handle failed uploads
            Toast.makeText(this@EditProfile, "Failed to save details.", Toast.LENGTH_SHORT).show()
        }
    }






    private suspend fun uploadSingleImage(
        uri: Uri?,
        imageRef: StorageReference,
        compressedAndResizedImage: ByteArray
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            imageRef.putBytes(compressedAndResizedImage).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun compressAndResizeImage(uri: Uri?): ByteArray {
        if (uri == null) {
            throw IllegalArgumentException("Uri cannot be null")
        }

        val compressedImage = compressImage(uri, contentResolver)
        val bitmap = BitmapFactory.decodeByteArray(compressedImage, 0, compressedImage.size)
        val resizedBitmap = resizeImage(bitmap, maxWidth = 800, maxHeight = 800)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return outputStream.toByteArray()
    }


    private fun compressImage(uri: Uri, contentResolver: ContentResolver, quality: Int = 75): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    private fun resizeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}