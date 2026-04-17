package com.example.spendsence.data

data class Income(
    val id: String = "",
    val amount: Double = 0.0,
    val source: String = "",
    val dateMillis: Long = 0L,
    val note: String = ""
)
