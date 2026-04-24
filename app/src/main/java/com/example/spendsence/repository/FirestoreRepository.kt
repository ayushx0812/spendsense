package com.example.spendsence.repository

import com.example.spendsence.data.Account
import com.example.spendsence.data.Budget
import com.example.spendsence.data.CategoryBudget
import com.example.spendsence.data.Expense
import com.example.spendsence.data.Income
import com.example.spendsence.data.RecurringTransaction
import com.example.spendsence.data.SavingsGoal
import com.example.spendsence.data.Transfer
import com.example.spendsence.data.UpcomingExpense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // By default, the workspace is the user's own UID.
    var currentWorkspaceId: String? = auth.currentUser?.uid

    init {
        auth.addAuthStateListener { firebaseAuth ->
            currentWorkspaceId = firebaseAuth.currentUser?.uid
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private fun workspaceDoc() = currentWorkspaceId?.let {
        db.collection("workspaces").document(it)
    }

    // ─── Expenses ────────────────────────────────────────────────────────────

    fun getAllExpenses(): Flow<List<Expense>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("expenses")
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val tasks = snapshot?.documents?.mapNotNull {
                        it.toObject(Expense::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(tasks)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertExpense(expense: Expense) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId).collection("expenses").add(expense).await()
    }

    // ─── Incomes ─────────────────────────────────────────────────────────────

    fun getAllIncomes(): Flow<List<Income>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("incomes")
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(Income::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertIncome(income: Income) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId).collection("incomes").add(income).await()
    }

    // ─── Budget (global monthly limit) ───────────────────────────────────────

    suspend fun insertBudget(budget: Budget) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId).collection("budgets")
            .document(budget.monthYear).set(budget).await()
    }

    fun getBudgetForMonth(monthYear: String): Flow<Budget?> {
        val workspaceId = currentWorkspaceId ?: return flowOf(null)
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("budgets").document(monthYear)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(null); return@addSnapshotListener }
                    if (snapshot != null && snapshot.exists()) {
                        trySend(snapshot.toObject(Budget::class.java)?.copy(id = snapshot.id))
                    } else {
                        trySend(null)
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    // ─── Category Budgets ─────────────────────────────────────────────────────

    fun getAllCategoryBudgets(): Flow<List<CategoryBudget>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("categoryBudgets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(CategoryBudget::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertCategoryBudget(budget: CategoryBudget) {
        val workspaceId = currentWorkspaceId ?: return
        // Use category+monthYear as document ID to avoid duplicates
        val docId = "${budget.category}_${budget.monthYear}"
        db.collection("workspaces").document(workspaceId)
            .collection("categoryBudgets").document(docId).set(budget).await()
    }

    // ─── Savings Goals ────────────────────────────────────────────────────────

    fun getAllSavingsGoals(): Flow<List<SavingsGoal>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("savingsGoals")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(SavingsGoal::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertSavingsGoal(goal: SavingsGoal) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId).collection("savingsGoals").add(goal).await()
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal) {
        val workspaceId = currentWorkspaceId ?: return
        if (goal.id.isEmpty()) return
        db.collection("workspaces").document(workspaceId)
            .collection("savingsGoals").document(goal.id).set(goal).await()
    }

    suspend fun deleteSavingsGoal(goalId: String) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("savingsGoals").document(goalId).delete().await()
    }

    // ─── Accounts ─────────────────────────────────────────────────────────────

    fun getAllAccounts(): Flow<List<Account>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("accounts")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(Account::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertAccount(account: Account) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId).collection("accounts").add(account).await()
    }

    suspend fun updateAccount(account: Account) {
        val workspaceId = currentWorkspaceId ?: return
        if (account.id.isEmpty()) return
        db.collection("workspaces").document(workspaceId)
            .collection("accounts").document(account.id).set(account).await()
    }

    suspend fun deleteAccount(accountId: String) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("accounts").document(accountId).delete().await()
    }

    // ─── Transfers ────────────────────────────────────────────────────────────

    fun getAllTransfers(): Flow<List<Transfer>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("transfers")
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(Transfer::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertTransfer(transfer: Transfer) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId).collection("transfers").add(transfer).await()
    }

    suspend fun deleteTransfer(transferId: String) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("transfers").document(transferId).delete().await()
    }

    // ─── Upcoming Expenses ────────────────────────────────────────────────────

    fun getAllUpcomingExpenses(): Flow<List<UpcomingExpense>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("upcomingExpenses")
                .orderBy("dueDateMillis", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(UpcomingExpense::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertUpcomingExpense(expense: UpcomingExpense) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("upcomingExpenses").add(expense).await()
    }

    suspend fun markUpcomingExpensePaid(expenseId: String) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("upcomingExpenses").document(expenseId)
            .update("isPaid", true).await()
    }

    suspend fun deleteUpcomingExpense(expenseId: String) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("upcomingExpenses").document(expenseId).delete().await()
    }

    // ─── Recurring Transactions ───────────────────────────────────────────────

    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> {
        val workspaceId = currentWorkspaceId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("workspaces").document(workspaceId)
                .collection("recurringTransactions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(RecurringTransaction::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun insertRecurringTransaction(recurring: RecurringTransaction) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("recurringTransactions").add(recurring).await()
    }

    suspend fun updateRecurringTransaction(recurring: RecurringTransaction) {
        val workspaceId = currentWorkspaceId ?: return
        if (recurring.id.isEmpty()) return
        db.collection("workspaces").document(workspaceId)
            .collection("recurringTransactions").document(recurring.id).set(recurring).await()
    }

    suspend fun deleteRecurringTransaction(recurringId: String) {
        val workspaceId = currentWorkspaceId ?: return
        db.collection("workspaces").document(workspaceId)
            .collection("recurringTransactions").document(recurringId).delete().await()
    }
}

