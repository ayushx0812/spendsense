package com.example.spendsence.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.ui.theme.ExpenseRed
import com.example.spendsence.ui.theme.IncomeGreen
import com.example.spendsence.ui.theme.PrimaryBlue
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSummaryScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allIncomes by viewModel.allIncomes.collectAsState()

    val monthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    val monthLabelFormat = SimpleDateFormat("MMM", Locale.getDefault())
    val currentYear = yearFormat.format(Date())

    // Calculate month-by-month summary for current year
    val monthlyData: List<Triple<String, Double, Double>> = (1..12).map { month ->
        val cal = Calendar.getInstance().apply { set(Calendar.MONTH, month - 1) }
        val monthKey = "${month.toString().padStart(2, '0')}-$currentYear"
        val label = monthLabelFormat.format(cal.time)
        val inc = allIncomes.filter { monthFormat.format(Date(it.dateMillis)) == monthKey }.sumOf { it.amount }
        val exp = allExpenses.filter { monthFormat.format(Date(it.dateMillis)) == monthKey }.sumOf { it.amount }
        Triple(label, inc, exp)
    }

    val maxValue = monthlyData.maxOfOrNull { maxOf(it.second, it.third) }?.coerceAtLeast(1.0) ?: 1.0
    val yearTotalIncome = monthlyData.sumOf { it.second }
    val yearTotalExpense = monthlyData.sumOf { it.third }
    val bestMonth = monthlyData.maxByOrNull { it.second - it.third }
    val worstMonth = monthlyData.filter { it.third > 0 }.minByOrNull { it.second - it.third }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Year Summary $currentYear") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Year totals card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Year $currentYear Overview", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Income", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("₹${"%.0f".format(yearTotalIncome)}", color = IncomeGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Expenses", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("₹${"%.0f".format(yearTotalExpense)}", color = ExpenseRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Savings", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                val savings = yearTotalIncome - yearTotalExpense
                                Text("₹${"%.0f".format(savings)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Highlights
            if (bestMonth != null || worstMonth != null) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        bestMonth?.let { best ->
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.1f)), shape = RoundedCornerShape(10.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🏆 Best Month", fontSize = 11.sp, color = IncomeGreen, fontWeight = FontWeight.Bold)
                                    Text(best.first, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("+₹${"%.0f".format(best.second - best.third)}", color = IncomeGreen, fontSize = 13.sp)
                                }
                            }
                        }
                        worstMonth?.let { worst ->
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f)), shape = RoundedCornerShape(10.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("📉 Hardest Month", fontSize = 11.sp, color = ExpenseRed, fontWeight = FontWeight.Bold)
                                    Text(worst.first, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("₹${"%.0f".format(worst.third - worst.second)}", color = ExpenseRed, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 6-month bar chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monthly Chart", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(IncomeGreen, RoundedCornerShape(2.dp)))
                                Spacer(Modifier.width(4.dp))
                                Text("Income", fontSize = 11.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(ExpenseRed, RoundedCornerShape(2.dp)))
                                Spacer(Modifier.width(4.dp))
                                Text("Expense", fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                            val barWidth = size.width / (monthlyData.size * 3f)
                            monthlyData.forEachIndexed { i, (_, inc, exp) ->
                                val x = i * (size.width / monthlyData.size)
                                val incH = ((inc / maxValue) * size.height).toFloat().coerceAtLeast(0f)
                                val expH = ((exp / maxValue) * size.height).toFloat().coerceAtLeast(0f)
                                drawRect(color = IncomeGreen.copy(alpha = 0.7f), topLeft = Offset(x, size.height - incH), size = Size(barWidth, incH))
                                drawRect(color = ExpenseRed.copy(alpha = 0.7f), topLeft = Offset(x + barWidth + 4, size.height - expH), size = Size(barWidth, expH))
                            }
                        }
                        // Month labels
                        Row(modifier = Modifier.fillMaxWidth()) {
                            monthlyData.forEach { (label, _, _) ->
                                Text(label, fontSize = 9.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Month-by-month table
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Month-by-Month Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        // Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Month", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("Income", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IncomeGreen, modifier = Modifier.weight(1f))
                            Text("Expense", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ExpenseRed, modifier = Modifier.weight(1f))
                            Text("Balance", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        monthlyData.forEach { (label, inc, exp) ->
                            val balance = inc - exp
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Text(label, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("₹${"%.0f".format(inc)}", fontSize = 12.sp, color = if (inc > 0) IncomeGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
                                Text("₹${"%.0f".format(exp)}", fontSize = 12.sp, color = if (exp > 0) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
                                Text(
                                    (if (balance >= 0) "+₹" else "-₹") + "%.0f".format(Math.abs(balance)),
                                    fontSize = 12.sp,
                                    color = if (balance >= 0) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
