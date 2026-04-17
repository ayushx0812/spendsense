package com.example.spendsence.data

data class CategoryBudget(
    val id: String = "",
    val category: String = "",
    val limitAmount: Double = 0.0,
    val monthYear: String = "" // e.g. "04-2026"
)
