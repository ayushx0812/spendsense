package com.example.spendsence.util

import com.example.spendsence.data.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Seeds the Firestore workspace with realistic demo data.
 * Clears ALL existing collections first.
 */
suspend fun seedDemoData(workspaceId: String) {
    val db = FirebaseFirestore.getInstance()
    val ws = db.collection("workspaces").document(workspaceId)

    // ── 1. Clear all existing collections ────────────────────────────────────
    val collections = listOf(
        "expenses", "incomes", "budgets",
        "categoryBudgets", "savingsGoals", "accounts",
        "transfers", "upcomingExpenses", "recurringTransactions"
    )
    collections.forEach { col ->
        val docs = ws.collection(col).get().await()
        docs.forEach { it.reference.delete().await() }
    }

    val cal = Calendar.getInstance()
    fun daysAgo(days: Int): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -days)
        return c.timeInMillis
    }
    fun daysFromNow(days: Int): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, days)
        return c.timeInMillis
    }

    val monthFmt = java.text.SimpleDateFormat("MM-yyyy", Locale.getDefault())
    val currentMonth = monthFmt.format(Date())

    // ── 2. Incomes ────────────────────────────────────────────────────────────
    val incomes = listOf(
        Income(amount = 55000.0, source = "Salary",     dateMillis = daysAgo(30), note = "April salary"),
        Income(amount = 55000.0, source = "Salary",     dateMillis = daysAgo(60), note = "March salary"),
        Income(amount = 55000.0, source = "Salary",     dateMillis = daysAgo(90), note = "February salary"),
        Income(amount = 8500.0,  source = "Freelance",  dateMillis = daysAgo(15), note = "Website project"),
        Income(amount = 3200.0,  source = "Freelance",  dateMillis = daysAgo(45), note = "Logo design"),
        Income(amount = 2000.0,  source = "Dividends",  dateMillis = daysAgo(10), note = "Q1 dividend payout"),
        Income(amount = 1000.0,  source = "Rental",     dateMillis = daysAgo(5),  note = "Parking space rent"),
        Income(amount = 500.0,   source = "Gift",        dateMillis = daysAgo(7),  note = "Birthday gift")
    )
    incomes.forEach { ws.collection("incomes").add(it).await() }

    // ── 3. Expenses ───────────────────────────────────────────────────────────
    val expenses = listOf(
        // Food & Groceries
        Expense(amount = 1200.0, category = "Food",       dateMillis = daysAgo(2),  note = "Dinner at restaurant",  paymentMethod = "UPI"),
        Expense(amount = 3400.0, category = "Groceries",  dateMillis = daysAgo(3),  note = "Monthly groceries",     paymentMethod = "Cash"),
        Expense(amount = 650.0,  category = "Food",       dateMillis = daysAgo(5),  note = "Lunch with team",       paymentMethod = "Card"),
        Expense(amount = 450.0,  category = "Food",       dateMillis = daysAgo(8),  note = "Pizza night",           paymentMethod = "UPI"),
        Expense(amount = 2800.0, category = "Groceries",  dateMillis = daysAgo(35), note = "Weekly grocery run",    paymentMethod = "Cash"),
        // Transport
        Expense(amount = 180.0,  category = "Transport",  dateMillis = daysAgo(1),  note = "Auto to office",        paymentMethod = "Cash"),
        Expense(amount = 350.0,  category = "Transport",  dateMillis = daysAgo(4),  note = "Cab to airport",        paymentMethod = "UPI"),
        Expense(amount = 1500.0, category = "Transport",  dateMillis = daysAgo(20), note = "Monthly metro pass",    paymentMethod = "Card"),
        // Rent & Utilities
        Expense(amount = 15000.0, category = "Rent",      dateMillis = daysAgo(1),  note = "April rent",           paymentMethod = "Bank Transfer"),
        Expense(amount = 15000.0, category = "Rent",      dateMillis = daysAgo(31), note = "March rent",           paymentMethod = "Bank Transfer"),
        Expense(amount = 1850.0, category = "Utilities",  dateMillis = daysAgo(6),  note = "Electricity bill",     paymentMethod = "UPI"),
        Expense(amount = 599.0,  category = "Utilities",  dateMillis = daysAgo(10), note = "Internet bill",        paymentMethod = "Auto-debit"),
        Expense(amount = 350.0,  category = "Utilities",  dateMillis = daysAgo(12), note = "Mobile recharge",      paymentMethod = "UPI"),
        // Shopping
        Expense(amount = 4500.0, category = "Shopping",   dateMillis = daysAgo(9),  note = "New sneakers",         paymentMethod = "Card"),
        Expense(amount = 1200.0, category = "Shopping",   dateMillis = daysAgo(14), note = "Books & stationery",   paymentMethod = "UPI"),
        Expense(amount = 2200.0, category = "Shopping",   dateMillis = daysAgo(40), note = "Summer clothes",       paymentMethod = "Card"),
        // Health
        Expense(amount = 800.0,  category = "Health",     dateMillis = daysAgo(7),  note = "Doctor consultation",  paymentMethod = "Cash"),
        Expense(amount = 550.0,  category = "Health",     dateMillis = daysAgo(11), note = "Pharmacy",             paymentMethod = "UPI"),
        Expense(amount = 2500.0, category = "Health",     dateMillis = daysAgo(50), note = "Annual health checkup",paymentMethod = "Card"),
        // Entertainment
        Expense(amount = 499.0,  category = "Entertainment", dateMillis = daysAgo(3),  note = "Netflix subscription", paymentMethod = "Auto-debit"),
        Expense(amount = 249.0,  category = "Entertainment", dateMillis = daysAgo(3),  note = "Spotify subscription", paymentMethod = "Auto-debit"),
        Expense(amount = 800.0,  category = "Entertainment", dateMillis = daysAgo(16), note = "Movie + dinner",       paymentMethod = "Card"),
        // Education
        Expense(amount = 3000.0, category = "Education",  dateMillis = daysAgo(22), note = "Online course",        paymentMethod = "Card"),
        Expense(amount = 500.0,  category = "Education",  dateMillis = daysAgo(55), note = "Workshop fee",         paymentMethod = "UPI"),
        // Travel
        Expense(amount = 8500.0, category = "Travel",     dateMillis = daysAgo(25), note = "Weekend trip to Goa",  paymentMethod = "Card"),
        Expense(amount = 1200.0, category = "Travel",     dateMillis = daysAgo(26), note = "Hotel stay",           paymentMethod = "Card")
    )
    expenses.forEach { ws.collection("expenses").add(it).await() }

    // ── 4. Accounts ───────────────────────────────────────────────────────────
    val accounts = listOf(
        Account(name = "HDFC Savings",  balance = 24500.0),
        Account(name = "Cash Wallet",   balance = 3200.0),
        Account(name = "PayPal",        balance = 8750.0),
        Account(name = "FD / Investment", balance = 50000.0)
    )
    accounts.forEach { ws.collection("accounts").add(it).await() }

    // ── 5. Savings Goals ──────────────────────────────────────────────────────
    val goals = listOf(
        SavingsGoal(name = "Emergency Fund",  targetAmount = 100000.0, savedAmount = 42000.0),
        SavingsGoal(name = "Goa Vacation",   targetAmount = 25000.0,  savedAmount = 12500.0),
        SavingsGoal(name = "New Laptop",     targetAmount = 80000.0,  savedAmount = 30000.0),
        SavingsGoal(name = "Wedding Fund",   targetAmount = 500000.0, savedAmount = 60000.0)
    )
    goals.forEach { ws.collection("savingsGoals").add(it).await() }

    // ── 6. Category Budgets (current month) ───────────────────────────────────
    val budgets = listOf(
        CategoryBudget(category = "Food",          limitAmount = 5000.0,  monthYear = currentMonth),
        CategoryBudget(category = "Groceries",     limitAmount = 6000.0,  monthYear = currentMonth),
        CategoryBudget(category = "Transport",     limitAmount = 2000.0,  monthYear = currentMonth),
        CategoryBudget(category = "Entertainment", limitAmount = 1500.0,  monthYear = currentMonth),
        CategoryBudget(category = "Shopping",      limitAmount = 5000.0,  monthYear = currentMonth),
        CategoryBudget(category = "Health",        limitAmount = 2000.0,  monthYear = currentMonth)
    )
    budgets.forEach { ws.collection("categoryBudgets").add(it).await() }

    // ── 7. Transfers ──────────────────────────────────────────────────────────
    val transfers = listOf(
        Transfer(fromAccount = "HDFC Savings", toAccount = "Cash Wallet",   amount = 5000.0,  dateMillis = daysAgo(4),  note = "ATM withdrawal"),
        Transfer(fromAccount = "PayPal",       toAccount = "HDFC Savings",  amount = 8750.0,  dateMillis = daysAgo(10), note = "Client payment received"),
        Transfer(fromAccount = "HDFC Savings", toAccount = "FD / Investment", amount = 10000.0, dateMillis = daysAgo(20), note = "Monthly SIP")
    )
    transfers.forEach { ws.collection("transfers").add(it).await() }

    // ── 8. Upcoming Expenses ──────────────────────────────────────────────────
    val upcoming = listOf(
        UpcomingExpense(title = "Car EMI",         amount = 8200.0,  dueDateMillis = daysFromNow(5),  category = "Transport", isPaid = false),
        UpcomingExpense(title = "House Rent",      amount = 15000.0, dueDateMillis = daysFromNow(3),  category = "Rent",      isPaid = false),
        UpcomingExpense(title = "Electricity Bill",amount = 1850.0,  dueDateMillis = daysFromNow(8),  category = "Utilities", isPaid = false),
        UpcomingExpense(title = "Internet Bill",   amount = 599.0,   dueDateMillis = daysFromNow(12), category = "Utilities", isPaid = false),
        UpcomingExpense(title = "Netflix",         amount = 499.0,   dueDateMillis = daysFromNow(15), category = "Entertainment", isPaid = false),
        UpcomingExpense(title = "Insurance Premium", amount = 5000.0, dueDateMillis = daysFromNow(20), category = "Health",  isPaid = false),
        UpcomingExpense(title = "Gym Membership",  amount = 1200.0,  dueDateMillis = daysAgo(2),     category = "Health",    isPaid = true),
        UpcomingExpense(title = "Domain Renewal",  amount = 850.0,   dueDateMillis = daysAgo(5),     category = "Education", isPaid = true)
    )
    upcoming.forEach { ws.collection("upcomingExpenses").add(it).await() }

    // ── 9. Recurring Transactions ─────────────────────────────────────────────
    val recurring = listOf(
        RecurringTransaction(type = "income",   amount = 55000.0, source = "Salary",            note = "Monthly salary",       frequencyDays = 30, nextDueDateMillis = daysFromNow(27), isActive = true),
        RecurringTransaction(type = "expense",  amount = 15000.0, category = "Rent",            note = "House rent",           frequencyDays = 30, nextDueDateMillis = daysFromNow(3),  isActive = true),
        RecurringTransaction(type = "expense",  amount = 499.0,   category = "Entertainment",   note = "Netflix subscription", frequencyDays = 30, nextDueDateMillis = daysFromNow(15), isActive = true),
        RecurringTransaction(type = "expense",  amount = 599.0,   category = "Utilities",       note = "Internet bill",        frequencyDays = 30, nextDueDateMillis = daysFromNow(12), isActive = true),
        RecurringTransaction(type = "expense",  amount = 1200.0,  category = "Health",          note = "Gym membership",       frequencyDays = 30, nextDueDateMillis = daysFromNow(28), isActive = true),
        RecurringTransaction(type = "income",   amount = 3200.0,  source = "Freelance Retainer", note = "Monthly retainer",    frequencyDays = 30, nextDueDateMillis = daysFromNow(10), isActive = true)
    )
    recurring.forEach { ws.collection("recurringTransactions").add(it).await() }
}
