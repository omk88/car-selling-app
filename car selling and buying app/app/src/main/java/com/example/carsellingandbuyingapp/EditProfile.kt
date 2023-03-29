package com.example.carsellingandbuyingapp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class EditProfile : AppCompatActivity(), View.OnClickListener {

    private var RESULT_LOAD_IMAGE: Int = 1
    private var bannerUri: Uri? = null
    private var profilePictureUri: Uri? = null
    private var imageNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val username = intent.getStringExtra("username").toString()

        val saveDetails = findViewById<Button>(R.id.saveDetails)

        saveDetails.setOnClickListener{
            uploadImage(username)
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