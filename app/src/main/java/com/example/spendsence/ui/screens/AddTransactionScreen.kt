package com.example.spendsence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AppViewModel,
    isExpense: Boolean,
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Expense fields
    var category by remember { mutableStateOf("Food") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    val categories = listOf(
        "Food", "Groceries", "Travel", "Transport", "Shopping",
        "Rent", "Utilities", "Health", "Entertainment", "Education", "Others"
    )
    var expandedCategory by remember { mutableStateOf(false) }

    // Income field
    var source by remember { mutableStateOf("") }

    // Date picker state
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val primaryColor = if (isExpense) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
    val title = if (isExpense) "Add Expense" else "Add Income"

    // Quick amount chips
    val quickAmounts = listOf("100", "500", "1000", "5000")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
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
                .verticalScroll(rememberScrollState())
        ) {
            // Amount field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₹", fontWeight = FontWeight.Bold) }
            )

            Spacer(Modifier.height(8.dp))

            // Quick amount chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quickAmounts.forEach { q ->
                    FilterChip(
                        selected = amount == q,
                        onClick = { amount = q },
                        label = { Text("₹$q", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Date picker button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(dateDisplayFormat.format(Date(selectedDateMillis)))
            }

            Spacer(Modifier.height(16.dp))

            if (isExpense) {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = { category = opt; expandedCategory = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source (e.g. Salary, Freelance)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (amountVal > 0) {
                        if (isExpense) {
                            viewModel.insertExpense(amountVal, category, selectedDateMillis, note, paymentMethod)
                        } else {
                            viewModel.insertIncome(amountVal, source.ifBlank { "Other" }, selectedDateMillis, note)
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Transaction", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
