package com.example.carsellingandbuyingapp

import android.app.Application

class Username : Application() {
    var username: String = ""
    var bannerUri: String = ""
    var profilePictureUri: String = ""
    var sales = 0
    var ecoSales = 0

    override fun onCreate() {
        super.onCreate()
    }
}
