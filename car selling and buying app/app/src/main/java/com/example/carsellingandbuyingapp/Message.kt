package com.example.carsellingandbuyingapp

data class Message(
    val user: String,
    val text: String,
    val timestamp: String,
    val seen: String,
    val replyText: String,
    val imageLocation: String,
){

    val messageKey = "$user|$timestamp"
}
