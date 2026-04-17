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
import com.example.spendsence.data.SavingsGoal
import com.example.spendsence.ui.theme.IncomeGreen
import com.example.spendsence.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val savingsGoals by viewModel.allSavingsGoals.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var nameInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }
    var savedInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Goals") },
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
                    editingGoal = null
                    nameInput = ""; targetInput = ""; savedInput = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Savings", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(
                            "₹${"%.2f".format(totalSavings)}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (savingsGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No savings goals yet.\nTap + to add one.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(savingsGoals) { goal ->
                    val pct = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat() else 0f
                    val barColor = if (pct >= 1f) IncomeGreen else MaterialTheme.colorScheme.primary

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
                                Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row {
                                    TextButton(onClick = {
                                        editingGoal = goal
                                        nameInput = goal.name
                                        targetInput = goal.targetAmount.toString()
                                        savedInput = goal.savedAmount.toString()
                                        showAddDialog = true
                                    }) { Text("Edit", fontSize = 12.sp) }
                                    IconButton(
                                        onClick = { viewModel.deleteSavingsGoal(goal.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE0E0E0))
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Saved: ₹${"%.2f".format(goal.savedAmount)}", fontSize = 13.sp, color = IncomeGreen)
                                Text("Target: ₹${"%.2f".format(goal.targetAmount)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { pct.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
                                color = barColor,
                                trackColor = Color(0xFFE0E0E0)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (pct >= 1f) "✓ Goal achieved!" else "${(pct * 100).toInt()}% complete",
                                fontSize = 11.sp,
                                color = if (pct >= 1f) IncomeGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editingGoal != null) "Edit Goal" else "Add Savings Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Goal Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it },
                        label = { Text("Target Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = savedInput,
                        onValueChange = { savedInput = it },
                        label = { Text("Amount Saved So Far (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val target = targetInput.toDoubleOrNull() ?: 0.0
                    val saved = savedInput.toDoubleOrNull() ?: 0.0
                    if (nameInput.isNotBlank()) {
                        if (editingGoal != null) {
                            viewModel.updateSavingsGoal(editingGoal!!.copy(name = nameInput.trim(), targetAmount = target, savedAmount = saved))
                        } else {
                            viewModel.insertSavingsGoal(nameInput.trim(), target, saved)
                        }
                        showAddDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
