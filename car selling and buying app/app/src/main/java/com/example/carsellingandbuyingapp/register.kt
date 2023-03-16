package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.VoicemailContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class register : AppCompatActivity() {
    private val AUTOCOMPLETE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val customAutocompleteEditText = findViewById<EditText>(R.id.custom_autocomplete_edit_text)
                customAutocompleteEditText.setText(place.address)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.e(TAG, "Error: Status = ${status.statusMessage}")
            }
        }


        val signInButton = findViewById<TextView>(R.id.signInNow)
        val signUpButton = findViewById<Button>(R.id.button3)

        signUpButton.setOnClickListener{
            addToDatabase()
        }

        signInButton.setOnClickListener{
            startActivity(Intent(this@register,MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    fun addToDatabase() {
        val customAutocompleteEditText = findViewById<EditText>(R.id.custom_autocomplete_edit_text)
        val address = customAutocompleteEditText.text.toString()

        val editTxt = findViewById<EditText>(R.id.editTextTextPersonName)
        val username = editTxt.text.toString()

        val passTxt = findViewById<EditText>(R.id.editTextTextPassword2)
        val password = passTxt.text.toString()

        val phoneTxt = findViewById<EditText>(R.id.editTextPhoneNumber)
        val phone = phoneTxt.text.toString()

        val database = Firebase.database.getReference("users")
        val user = User(username, password, phone, address)
        database.child(username).setValue(user)

        Toast.makeText(this, "Successfully Registered!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@register,MainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

    }
}