package com.example.spendsence.data

data class Account(
    val id: String = "",
    val name: String = "",      // e.g. "Cash", "Savings", "Paypal"
    val balance: Double = 0.0
)
