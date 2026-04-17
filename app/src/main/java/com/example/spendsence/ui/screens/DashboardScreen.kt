package com.example.spendsence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAddIncome: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    val mostSpentCategory = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .maxByOrNull { it.value }?.key ?: "N/A"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SpendSense") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.PieChart, contentDescription = "Analytics", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToBudget) {
                        Icon(Icons.Default.Analytics, contentDescription = "Budget", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = onNavigateToAddIncome,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = "Add Income")
                }
                FloatingActionButton(
                    onClick = onNavigateToAddExpense,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("₹${"%.2f".format(totalBalance)}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryCard(
                    title = "Total Income",
                    amount = totalIncome,
                    color = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Default.CurrencyRupee,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                SummaryCard(
                    title = "Total Expense",
                    amount = totalExpense,
                    color = MaterialTheme.colorScheme.tertiary,
                    icon = Icons.Default.CurrencyRupee,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Most Spent Category:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(mostSpentCategory, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Expenses", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(expenses.take(10)) { expense ->
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dateString = dateFormat.format(Date(expense.dateMillis))

                    ListItem(
                        headlineContent = { Text(expense.category, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(dateString) },
                        trailingContent = {
                            Text(
                                "-₹${"%.2f".format(expense.amount)}",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("₹${"%.2f".format(amount)}", color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
