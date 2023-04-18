package com.example.carsellingandbuyingapp

data class User(val username : String? = null,
                val password : String? = null,
                val phone : String? = null,
                val address : String? = null,
                val verifiedPhone : String? = null,
                val verifiedEmail : String? = null,
                val eco0 : String? = null,
                val eco1 : String? = null,
                val eco2 : String? = null,
                val sale0 : String? = null,
                val sale1 : String? = null,
                val sale2 : String? = null,
                val sales : Int? = null,
                val ecoSales : Int? = null,
                val completedPreferences : Int? = null)
