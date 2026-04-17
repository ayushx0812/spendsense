package com.example.spendsence.data

data class SavingsGoal(
    val id: String = "",
    val name: String = "",          // e.g. "Student Loan", "Emergency Fund"
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0
)
