package com.example.spendsence.data

data class UpcomingExpense(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val dueDateMillis: Long = 0L,
    val category: String = "",
    val isPaid: Boolean = false
)


