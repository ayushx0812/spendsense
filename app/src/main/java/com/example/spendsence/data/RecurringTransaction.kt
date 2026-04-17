package com.example.spendsence.data

data class RecurringTransaction(
    val id: String = "",
    val type: String = "expense",     // "expense" or "income"
    val amount: Double = 0.0,
    val category: String = "",        // for expense
    val source: String = "",          // for income
    val note: String = "",
    val frequencyDays: Int = 30,      // 7=weekly, 14=biweekly, 30=monthly
    val nextDueDateMillis: Long = 0L,
    val isActive: Boolean = true
)
