package com.example.spendsence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val currentMonthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
    val currentMonth = currentMonthFormat.format(Date())

    val budgetState = remember(currentMonth) { 
        viewModel.getBudgetForMonth(currentMonth) 
    }.collectAsState(initial = null)
    val budget = budgetState.value

    val expenses by viewModel.allExpenses.collectAsState()
    val totalExpenseThisMonth = expenses.filter {
        currentMonthFormat.format(Date(it.dateMillis)) == currentMonth
    }.sumOf { it.amount }

    var newBudgetLimit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budget") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Budget for $currentMonth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (budget != null) {
                val usagePercentage = if (budget.limitAmount > 0) (totalExpenseThisMonth / budget.limitAmount).toFloat() else 0f
                val isNearLimit = usagePercentage >= 0.8f

                Text("Limit: ₹${"%.2f".format(budget.limitAmount)}")
                Text("Spent: ₹${"%.2f".format(totalExpenseThisMonth)}")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { usagePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = if (isNearLimit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                if (isNearLimit) {
                    Text(
                        "⚠️ You have reached 80% or more of your budget!",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text("No budget set for this month.")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))

            Text("Set New Budget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newBudgetLimit,
                onValueChange = { newBudgetLimit = it },
                label = { Text("Budget Limit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val limit = newBudgetLimit.toDoubleOrNull()
                    if (limit != null) {
                        viewModel.insertBudget(currentMonth, limit)
                        newBudgetLimit = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Budget")
            }
        }
    }
}
