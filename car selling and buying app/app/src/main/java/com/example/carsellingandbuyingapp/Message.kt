package com.example.carsellingandbuyingapp

data class Message(
    val user: String,
    val text: String,
    val timestamp: String){

    val messageKey = "$user|$timestamp"
}
