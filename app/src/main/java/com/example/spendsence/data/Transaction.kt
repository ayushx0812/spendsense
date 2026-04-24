package com.example.spendsence.data

data class Transaction(
    val amount: Double = 0.0,
    val merchant: String = "",
    val category: String = "",
    val type: String = "",
    val source: String = "",
    val timestamp: Long = 0
)
