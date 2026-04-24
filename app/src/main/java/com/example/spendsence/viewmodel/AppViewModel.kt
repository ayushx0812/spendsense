package com.example.spendsence.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spendsence.data.Account
import com.example.spendsence.data.Budget
import com.example.spendsence.data.CategoryBudget
import com.example.spendsence.data.Expense
import com.example.spendsence.data.Income
import com.example.spendsence.data.RecurringTransaction
import com.example.spendsence.data.SavingsGoal
import com.example.spendsence.data.Transfer
import com.example.spendsence.data.UpcomingExpense
import com.example.spendsence.repository.AuthRepository
import com.example.spendsence.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.spendsence.util.seedDemoData
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser

    // Triggers a refresh of all flows whenever workspace ID changes
    private val _workspaceId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                val newId = user?.uid
                if (_workspaceId.value == null || newId == null) {
                    _workspaceId.value = newId
                }
                firestoreRepository.currentWorkspaceId = _workspaceId.value
            }
        }
    }

    // ─── Expenses ─────────────────────────────────────────────────────────────

    val allExpenses: StateFlow<List<Expense>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else firestoreRepository.getAllExpenses()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalExpense: StateFlow<Double> = allExpenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    /** Expense totals grouped by category for the current month */
    val thisMonthCategorySpending: StateFlow<Map<String, Double>> = allExpenses.map { list ->
        val monthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        val currentMonth = monthFormat.format(Date())
        list.filter { monthFormat.format(Date(it.dateMillis)) == currentMonth }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    /** Expense totals grouped by category for the current year */
    val thisYearCategorySpending: StateFlow<Map<String, Double>> = allExpenses.map { list ->
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val currentYear = yearFormat.format(Date())
        list.filter { yearFormat.format(Date(it.dateMillis)) == currentYear }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun insertExpense(amount: Double, category: String, dateMillis: Long, note: String, paymentMethod: String = "") {
        viewModelScope.launch {
            firestoreRepository.insertExpense(
                Expense(amount = amount, category = category, dateMillis = dateMillis, note = note, paymentMethod = paymentMethod)
            )
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch { firestoreRepository.deleteExpense(expenseId) }
    }

    // ─── Incomes ──────────────────────────────────────────────────────────────

    val allIncomes: StateFlow<List<Income>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else firestoreRepository.getAllIncomes()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalIncome: StateFlow<Double> = allIncomes.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    /** Year income broken down by source */
    val thisYearIncomeBySource: StateFlow<Map<String, Double>> = allIncomes.map { list ->
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val currentYear = yearFormat.format(Date())
        list.filter { yearFormat.format(Date(it.dateMillis)) == currentYear }
            .groupBy { it.source }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun insertIncome(amount: Double, source: String, dateMillis: Long, note: String) {
        viewModelScope.launch {
            firestoreRepository.insertIncome(Income(amount = amount, source = source, dateMillis = dateMillis, note = note))
        }
    }

    fun deleteIncome(incomeId: String) {
        viewModelScope.launch { firestoreRepository.deleteIncome(incomeId) }
    }

    // ─── Balance ──────────────────────────────────────────────────────────────

    val totalBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // ─── Global Budget ────────────────────────────────────────────────────────

    fun insertBudget(monthYear: String, limitAmount: Double) {
        viewModelScope.launch {
            firestoreRepository.insertBudget(Budget(monthYear = monthYear, limitAmount = limitAmount))
        }
    }

    fun getBudgetForMonth(monthYear: String): Flow<Budget?> {
        return firestoreRepository.getBudgetForMonth(monthYear)
    }

    // ─── Category Budgets ─────────────────────────────────────────────────────

    val allCategoryBudgets: StateFlow<List<CategoryBudget>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else firestoreRepository.getAllCategoryBudgets()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertCategoryBudget(category: String, limitAmount: Double, monthYear: String) {
        viewModelScope.launch {
            firestoreRepository.insertCategoryBudget(
                CategoryBudget(category = category, limitAmount = limitAmount, monthYear = monthYear)
            )
        }
    }

    // ─── Savings Goals ────────────────────────────────────────────────────────

    val allSavingsGoals: StateFlow<List<SavingsGoal>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else firestoreRepository.getAllSavingsGoals()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalSavings: StateFlow<Double> = allSavingsGoals.map { list ->
        list.sumOf { it.savedAmount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun insertSavingsGoal(name: String, targetAmount: Double, savedAmount: Double) {
        viewModelScope.launch {
            firestoreRepository.insertSavingsGoal(
                SavingsGoal(name = name, targetAmount = targetAmount, savedAmount = savedAmount)
            )
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch { firestoreRepository.updateSavingsGoal(goal) }
    }

    fun deleteSavingsGoal(goalId: String) {
        viewModelScope.launch { firestoreRepository.deleteSavingsGoal(goalId) }
    }

    // ─── Accounts ─────────────────────────────────────────────────────────────

    val allAccounts: StateFlow<List<Account>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else firestoreRepository.getAllAccounts()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalAccountBalance: StateFlow<Double> = allAccounts.map { list ->
        list.sumOf { it.balance }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun insertAccount(name: String, balance: Double) {
        viewModelScope.launch {
            firestoreRepository.insertAccount(Account(name = name, balance = balance))
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch { firestoreRepository.updateAccount(account) }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch { firestoreRepository.deleteAccount(accountId) }
    }

    // ─── Transfers ────────────────────────────────────────────────────────────

    val allTransfers: StateFlow<List<Transfer>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else firestoreRepository.getAllTransfers()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertTransfer(fromAccount: String, toAccount: String, amount: Double, note: String) {
        viewModelScope.launch {
            firestoreRepository.insertTransfer(
                Transfer(
                    fromAccount = fromAccount,
                    toAccount = toAccount,
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    note = note
                )
            )
        }
    }

    fun deleteTransfer(transferId: String) {
        viewModelScope.launch { firestoreRepository.deleteTransfer(transferId) }
    }

    // ─── Upcoming Expenses ────────────────────────────────────────────────────

    val allUpcomingExpenses: StateFlow<List<UpcomingExpense>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else firestoreRepository.getAllUpcomingExpenses()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertUpcomingExpense(title: String, amount: Double, dueDateMillis: Long, category: String) {
        viewModelScope.launch {
            firestoreRepository.insertUpcomingExpense(
                UpcomingExpense(title = title, amount = amount, dueDateMillis = dueDateMillis, category = category)
            )
        }
    }

    fun markUpcomingExpensePaid(expenseId: String) {
        viewModelScope.launch { firestoreRepository.markUpcomingExpensePaid(expenseId) }
    }

    fun deleteUpcomingExpense(expenseId: String) {
        viewModelScope.launch { firestoreRepository.deleteUpcomingExpense(expenseId) }
    }

    // ─── Recurring Transactions ───────────────────────────────────────────────

    val allRecurringTransactions: StateFlow<List<RecurringTransaction>> = _workspaceId.flatMapLatest { id ->
        if (id == null) flowOf<List<RecurringTransaction>>(emptyList()) else firestoreRepository.getAllRecurringTransactions()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertRecurringTransaction(
        type: String, amount: Double, category: String, source: String,
        note: String, frequencyDays: Int
    ) {
        viewModelScope.launch {
            val nextDue = System.currentTimeMillis() + (frequencyDays * 24 * 60 * 60 * 1000L)
            firestoreRepository.insertRecurringTransaction(
                RecurringTransaction(
                    type = type, amount = amount, category = category,
                    source = source, note = note, frequencyDays = frequencyDays,
                    nextDueDateMillis = nextDue, isActive = true
                )
            )
        }
    }

    /** Instantly apply a recurring transaction (add it to expenses/incomes) */
    fun applyRecurringTransaction(recurring: RecurringTransaction) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (recurring.type == "expense") {
                firestoreRepository.insertExpense(
                    Expense(amount = recurring.amount, category = recurring.category,
                        dateMillis = now, note = recurring.note)
                )
            } else {
                firestoreRepository.insertIncome(
                    Income(amount = recurring.amount, source = recurring.source,
                        dateMillis = now, note = recurring.note)
                )
            }
            // Advance next due date
            val updated = recurring.copy(
                nextDueDateMillis = now + (recurring.frequencyDays * 24 * 60 * 60 * 1000L)
            )
            firestoreRepository.updateRecurringTransaction(updated)
        }
    }

    fun updateRecurringTransaction(recurring: RecurringTransaction) {
        viewModelScope.launch { firestoreRepository.updateRecurringTransaction(recurring) }
    }

    fun deleteRecurringTransaction(recurringId: String) {
        viewModelScope.launch { firestoreRepository.deleteRecurringTransaction(recurringId) }
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    /** Returns null on success, or a human-readable error message on failure. */
    suspend fun login(email: String, pass: String): String? {
        val error = authRepository.login(email, pass)
        if (error == null) _workspaceId.value = authRepository.currentUser.value?.uid
        return error
    }

    /** Returns null on success, or a human-readable error message on failure. */
    suspend fun signup(email: String, pass: String): String? {
        val error = authRepository.signup(email, pass)
        if (error == null) _workspaceId.value = authRepository.currentUser.value?.uid
        return error
    }

    fun logout() {
        authRepository.logout()
        _workspaceId.value = null
    }

    fun linkToWorkspace(uid: String) {
        _workspaceId.value = uid
        firestoreRepository.currentWorkspaceId = uid
    }

    val currentWorkspaceIdFlow: StateFlow<String?> = _workspaceId.stateIn(
        viewModelScope, SharingStarted.Lazily, null
    )

    // ─── Demo Data ────────────────────────────────────────────────────────────

    private val _isSeeding = MutableStateFlow(false)
    val isSeeding: StateFlow<Boolean> = _isSeeding

    /** Clears all data and seeds the workspace with realistic demo data. */
    fun seedDemoData() {
        val workspaceId = _workspaceId.value ?: return
        viewModelScope.launch {
            _isSeeding.value = true
            try {
                seedDemoData(workspaceId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSeeding.value = false
            }
        }
    }
}

class AppViewModelFactory(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(firestoreRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
