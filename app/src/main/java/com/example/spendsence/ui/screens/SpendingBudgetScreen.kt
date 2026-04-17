package com.example.spendsence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.ui.theme.ExpenseRed
import com.example.spendsence.ui.theme.PrimaryBlue
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingBudgetScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val categoryBudgets by viewModel.allCategoryBudgets.collectAsState()
    val monthlySpending by viewModel.thisMonthCategorySpending.collectAsState()

    val monthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
    val currentMonth = monthFormat.format(Date())

    val thisMonthBudgets = categoryBudgets.filter { it.monthYear == currentMonth }

    var showDialog by remember { mutableStateOf(false) }
    var categoryInput by remember { mutableStateOf("") }
    var limitInput by remember { mutableStateOf("") }

    val categoryColors = listOf(
        PrimaryBlue, Color(0xFF43A047), Color(0xFF8E24AA),
        Color(0xFFFBC02D), Color(0xFF00ACC1), Color(0xFFFF6D00)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Budget") },
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
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Budget for $currentMonth",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (thisMonthBudgets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No category budgets set.\nTap + to add one.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(thisMonthBudgets.sortedBy { it.category }.withIndex().toList()) { (index, cb) ->
                    val spent = monthlySpending[cb.category] ?: 0.0
                    val pct = if (cb.limitAmount > 0) (spent / cb.limitAmount).toFloat() else 0f
                    val barColor = when {
                        pct >= 1f -> ExpenseRed
                        pct >= 0.8f -> Color(0xFFFFA000)
                        else -> categoryColors[index % categoryColors.size]
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cb.category, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "₹${"%.0f".format(spent)} / ₹${"%.0f".format(cb.limitAmount)}",
                                    fontSize = 13.sp,
                                    color = if (pct >= 1f) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { pct.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
                                color = barColor,
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (pct >= 1f) "⚠ Over budget!" else "${(pct * 100).toInt()}% used",
                                    fontSize = 11.sp,
                                    color = if (pct >= 1f) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    "₹${"%.0f".format((cb.limitAmount - spent).coerceAtLeast(0.0))} remaining",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
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
            title = { Text("Add Category Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Category (e.g. Groceries)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        label = { Text("Monthly Limit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val limit = limitInput.toDoubleOrNull()
                    if (categoryInput.isNotBlank() && limit != null) {
                        viewModel.insertCategoryBudget(categoryInput.trim(), limit, currentMonth)
                        categoryInput = ""
                        limitInput = ""
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
