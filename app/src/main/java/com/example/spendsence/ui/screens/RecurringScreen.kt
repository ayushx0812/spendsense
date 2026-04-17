package com.example.spendsence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.data.RecurringTransaction
import com.example.spendsence.ui.theme.ExpenseRed
import com.example.spendsence.ui.theme.IncomeGreen
import com.example.spendsence.ui.theme.PrimaryBlue
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val recurring by viewModel.allRecurringTransactions.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    var showDialog by remember { mutableStateOf(false) }
    var typeInput by remember { mutableStateOf("expense") }
    var amountInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var sourceInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var frequencyInput by remember { mutableStateOf("30") }

    val active = recurring.filter { it.isActive }
    val inactive = recurring.filter { !it.isActive }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Transactions") },
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
                    typeInput = "expense"; amountInput = ""; categoryInput = ""
                    sourceInput = ""; noteInput = ""; frequencyInput = "30"
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Active (${active.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
            }

            if (active.isEmpty()) {
                item {
                    Text(
                        "No active recurring transactions.\nTap + to add salary, rent, subscriptions etc.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            items(active) { r ->
                RecurringCard(r, dateFormat, viewModel)
            }

            if (inactive.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Inactive (${inactive.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                items(inactive) { r ->
                    RecurringCard(r, dateFormat, viewModel)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Recurring Transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Type selector
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = typeInput == "expense",
                            onClick = { typeInput = "expense" },
                            label = { Text("Expense") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExpenseRed,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = typeInput == "income",
                            onClick = { typeInput = "income" },
                            label = { Text("Income") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IncomeGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    if (typeInput == "expense") {
                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = sourceInput,
                            onValueChange = { sourceInput = it },
                            label = { Text("Source (e.g. Salary)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    // Frequency
                    Text("Frequency:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("7" to "Weekly", "14" to "Biweekly", "30" to "Monthly").forEach { (days, label) ->
                            FilterChip(
                                selected = frequencyInput == days,
                                onClick = { frequencyInput = days },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountInput.toDoubleOrNull()
                    if (amount != null) {
                        viewModel.insertRecurringTransaction(
                            type = typeInput,
                            amount = amount,
                            category = categoryInput.trim(),
                            source = sourceInput.trim(),
                            note = noteInput.trim(),
                            frequencyDays = frequencyInput.toIntOrNull() ?: 30
                        )
                        showDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RecurringCard(
    r: RecurringTransaction,
    dateFormat: SimpleDateFormat,
    viewModel: AppViewModel
) {
    val isExpense = r.type == "expense"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isExpense) r.category else r.source,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "${if (r.frequencyDays == 7) "Weekly" else if (r.frequencyDays == 14) "Biweekly" else "Monthly"}" +
                                " • Next: ${dateFormat.format(Date(r.nextDueDateMillis))}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text(
                    "₹${"%.2f".format(r.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isExpense) ExpenseRed else IncomeGreen
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.applyRecurringTransaction(r) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Apply Now", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        viewModel.updateRecurringTransaction(r.copy(isActive = !r.isActive))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(if (r.isActive) "Deactivate" else "Activate", fontSize = 12.sp)
                }
                IconButton(
                    onClick = { viewModel.deleteRecurringTransaction(r.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
