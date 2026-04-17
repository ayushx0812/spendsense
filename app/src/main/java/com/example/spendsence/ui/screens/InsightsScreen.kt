package com.example.spendsence.ui.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.ui.theme.ExpenseRed
import com.example.spendsence.ui.theme.IncomeGreen
import com.example.spendsence.ui.theme.PrimaryBlue
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

// Moved to file-level so LazyColumn items() can correctly infer the type
private data class Insight(
    val emoji: String,
    val title: String,
    val body: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allIncomes by viewModel.allIncomes.collectAsState()
    val allSavingsGoals by viewModel.allSavingsGoals.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val totalAccountBalance by viewModel.totalAccountBalance.collectAsState()
    val allCategoryBudgets by viewModel.allCategoryBudgets.collectAsState()

    val monthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
    val cal = Calendar.getInstance()
    val currentMonth = monthFormat.format(Date())
    cal.add(Calendar.MONTH, -1)
    val lastMonth = monthFormat.format(cal.time)

    // This month
    val thisMonthExpenses = allExpenses.filter { monthFormat.format(Date(it.dateMillis)) == currentMonth }
    val thisMonthIncomes = allIncomes.filter { monthFormat.format(Date(it.dateMillis)) == currentMonth }
    val thisMonthTotal = thisMonthExpenses.sumOf { it.amount }
    val thisMonthIncome = thisMonthIncomes.sumOf { it.amount }

    // Last month
    val lastMonthTotal = allExpenses.filter { monthFormat.format(Date(it.dateMillis)) == lastMonth }.sumOf { it.amount }
    val lastMonthIncome = allIncomes.filter { monthFormat.format(Date(it.dateMillis)) == lastMonth }.sumOf { it.amount }

    // Category breakdown
    val catSpending = thisMonthExpenses.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }
    val topCat = catSpending.maxByOrNull { it.value }
    val topCatPct = if (thisMonthTotal > 0 && topCat != null) ((topCat.value / thisMonthTotal) * 100).toInt() else 0

    // Biggest single expense
    val biggestExpense = thisMonthExpenses.maxByOrNull { it.amount }

    // Budget adherence
    val currentMonthBudgets = allCategoryBudgets.filter { it.monthYear == currentMonth }
    val underBudgetCount = currentMonthBudgets.count { cb ->
        (catSpending[cb.category] ?: 0.0) < cb.limitAmount
    }

    // Income/expense change %
    val incomeChangePct = if (lastMonthIncome > 0) ((thisMonthIncome - lastMonthIncome) / lastMonthIncome * 100).toInt() else null
    val expenseChangePct = if (lastMonthTotal > 0) ((thisMonthTotal - lastMonthTotal) / lastMonthTotal * 100).toInt() else null

    // Net worth
    val netWorth = totalAccountBalance + totalSavings

    // Use Color(0xFF424242) instead of MaterialTheme.colorScheme.onSurface since buildList is non-composable
    val mutedColor = Color(0xFF424242)

    val insights: List<Insight> = buildList {
        if (incomeChangePct != null) {
            if (incomeChangePct > 0) add(Insight("📈", "Income Up!", "Your income is up $incomeChangePct% compared to last month.", IncomeGreen))
            else if (incomeChangePct < 0) add(Insight("📉", "Income Down", "Your income dropped by ${kotlin.math.abs(incomeChangePct)}% vs last month.", ExpenseRed))
        }
        if (expenseChangePct != null) {
            if (expenseChangePct > 10) add(Insight("⚠️", "Spending Spike", "You spent ${expenseChangePct}% more than last month. Watch out!", ExpenseRed))
            else if (expenseChangePct < -10) add(Insight("✅", "Spending Down!", "Great job! Spending is down ${kotlin.math.abs(expenseChangePct)}% vs last month.", IncomeGreen))
        }
        if (topCat != null && topCatPct > 0) {
            add(Insight("💸", "Top Spending: ${topCat.key}", "${topCat.key} is $topCatPct% of total spending this month (₹${"%.0f".format(topCat.value)}).", PrimaryBlue))
        }
        if (underBudgetCount > 0) {
            add(Insight("🎯", "Budget Goals Met", "You stayed under budget in $underBudgetCount ${if (underBudgetCount == 1) "category" else "categories"} this month!", IncomeGreen))
        }
        if (biggestExpense != null) {
            add(Insight("💰", "Biggest Expense", "₹${"%.0f".format(biggestExpense.amount)} on ${biggestExpense.category}${if (biggestExpense.note.isNotBlank()) " (${biggestExpense.note})" else ""}.", mutedColor))
        }
        val goalsOnTrack = allSavingsGoals.count { it.savedAmount >= it.targetAmount * 0.5 }
        if (goalsOnTrack > 0) {
            add(Insight("🏦", "Savings Progress", "$goalsOnTrack savings ${if (goalsOnTrack == 1) "goal is" else "goals are"} more than 50% complete!", PrimaryBlue))
        }
        if (isEmpty()) {
            add(Insight("💡", "Start Tracking!", "Add transactions to see personalized financial insights here.", PrimaryBlue))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Insights") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Net Worth card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Net Worth", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(
                            "₹${"%.2f".format(netWorth)}",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Accounts", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                Text("₹${"%.0f".format(totalAccountBalance)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Savings Goals", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                Text("₹${"%.0f".format(totalSavings)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Text("This Month's Insights", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(items = insights) { insight ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(insight.emoji, fontSize = 28.sp, modifier = Modifier.padding(top = 2.dp, end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(insight.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = insight.color)
                            Spacer(Modifier.height(4.dp))
                            Text(insight.body, fontSize = 13.sp, color = Color(0xFF757575))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
