package com.example.spendsence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.ui.theme.ExpenseRed
import com.example.spendsence.ui.theme.IncomeGreen
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingExpensesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val upcomingExpenses by viewModel.allUpcomingExpenses.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    var showDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var dueDateInput by remember { mutableStateOf("") }

    val unpaid = upcomingExpenses.filter { !it.isPaid }
    val paid = upcomingExpenses.filter { it.isPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming Expenses") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    titleInput = ""; amountInput = ""; categoryInput = ""; dueDateInput = ""
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Upcoming Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Unpaid
            item {
                Text(
                    "Upcoming (${unpaid.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ExpenseRed
                )
            }

            if (unpaid.isEmpty()) {
                item {
                    Text(
                        "No upcoming expenses. Great! 🎉",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(unpaid) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "${expense.category} • Due: ${dateFormat.format(Date(expense.dueDateMillis))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "₹${"%.2f".format(expense.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                                Spacer(Modifier.height(4.dp))
                                Row {
                                    IconButton(
                                        onClick = { viewModel.markUpcomingExpensePaid(expense.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Mark Paid", tint = IncomeGreen, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteUpcomingExpense(expense.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Paid
            if (paid.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Paid (${paid.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = IncomeGreen
                    )
                }
                items(paid) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(expense.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("₹${"%.2f".format(expense.amount)}", fontSize = 13.sp, color = IncomeGreen)
                                IconButton(
                                    onClick = { viewModel.deleteUpcomingExpense(expense.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Upcoming Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Title (e.g. Electricity Bill)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Category (e.g. Utilities)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDateInput,
                        onValueChange = { dueDateInput = it },
                        label = { Text("Due Date (DD-MM-YYYY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountInput.toDoubleOrNull()
                    val dueDateMillis = try {
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dueDateInput)?.time
                            ?: System.currentTimeMillis()
                    } catch (e: Exception) { System.currentTimeMillis() }

                    if (titleInput.isNotBlank() && amount != null) {
                        viewModel.insertUpcomingExpense(
                            title = titleInput.trim(),
                            amount = amount,
                            dueDateMillis = dueDateMillis,
                            category = categoryInput.trim().ifBlank { "Other" }
                        )
                        showDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
