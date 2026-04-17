package com.example.spendsence.data

data class Transfer(
    val id: String = "",
    val fromAccount: String = "",   // e.g. "Savings"
    val toAccount: String = "",     // e.g. "Cash"
    val amount: Double = 0.0,
    val dateMillis: Long = 0L,
    val note: String = ""
)
