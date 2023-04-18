package com.example.carsellingandbuyingapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.random.Random


class VerifyPhone : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)

        val sendOTP = findViewById<Button>(R.id.sendOTP)
        val phoneNumberEdit = findViewById<EditText>(R.id.phoneNumber)

        phoneNumberEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                // Remove this TextWatcher to prevent an infinite loop when modifying the text
                phoneNumberEdit.removeTextChangedListener(this)

                // Add a space after the 5th character, if necessary
                if (s.length == 5) {
                    phoneNumberEdit.setText("$s ")
                    phoneNumberEdit.setSelection(s.length + 1) // Move the cursor to the end
                }

                // Re-add the TextWatcher for future changes
                phoneNumberEdit.addTextChangedListener(this)
            }
        })

        sendOTP.setOnClickListener {

            val phoneNumber = phoneNumberEdit.text.toString()
            val message = "OTP: "+ Random.nextInt(100_000, 1_000_000)

            requestSmsPermission()
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                sendSMS(phoneNumber, message)
            }
        }

    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.SEND_SMS),
                MY_PERMISSIONS_REQUEST_SEND_SMS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_SEND_SMS = 1
    }
}