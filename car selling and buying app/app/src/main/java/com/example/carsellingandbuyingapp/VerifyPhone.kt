package com.example.carsellingandbuyingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.random.Random


class VerifyPhone : AppCompatActivity() {

    var phoneNumber: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)

        val address = intent.getStringExtra("address")
        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        val confirmPassword = intent.getStringExtra("confirmPassword")

        val sendOTP = findViewById<Button>(R.id.sendOTP)
        val verify = findViewById<Button>(R.id.verifyPhone)
        val phoneNumberEdit = findViewById<EditText>(R.id.phoneNumber)
        val passcode = findViewById<EditText>(R.id.OTP)

        phoneNumberEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {

                phoneNumberEdit.removeTextChangedListener(this)

                if (s.length == 5) {
                    phoneNumberEdit.setText("$s ")
                    phoneNumberEdit.setSelection(s.length + 1)
                }
                phoneNumberEdit.addTextChangedListener(this) } })

        val code = Random.nextInt(100_000, 1_000_000)

        sendOTP.setOnClickListener {

            val message = "OTP: "+ code
            phoneNumber = phoneNumberEdit.text.toString()

            requestSmsPermission()
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                sendSMS(phoneNumber, message)
            }
        }

        verify.setOnClickListener {
            if(passcode.text.toString().toInt() == code) {

                Toast.makeText(this, "Phone Number Verified!", Toast.LENGTH_SHORT).show()
                val verifyIntent = Intent(this@VerifyPhone, register::class.java)
                verifyIntent.putExtra("phoneNumber", phoneNumber)
                verifyIntent.putExtra("address", address)
                verifyIntent.putExtra("username", username)
                verifyIntent.putExtra("password", password)
                verifyIntent.putExtra("confirmPassword", confirmPassword)
                startActivity(verifyIntent)
                overridePendingTransition(0, R.anim.slide_out_down)
            } else {
                Toast.makeText(this, "Incorrect Code! Try again.", Toast.LENGTH_SHORT).show()
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