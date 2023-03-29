package com.example.carsellingandbuyingapp

import android.app.Application

class Username : Application() {
    var username: String = ""
    var bannerUri: String = ""
    var profilePictureUri: String = ""

    override fun onCreate() {
        super.onCreate()
    }
}
