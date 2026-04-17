package com.example.spendsence.data

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val dateMillis: Long = 0L,
    val note: String = "",
    val paymentMethod: String = ""
)
