package com.example.spendsence.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.spendsence.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val incomes by viewModel.allIncomes.collectAsState()
    val context = LocalContext.current
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val csvBuilder = StringBuilder()
            csvBuilder.append("Type,Amount,Category/Source,Date,Note\n")
            
            incomes.forEach { inc ->
                val dateStr = dateFormat.format(Date(inc.dateMillis))
                csvBuilder.append("Income,${inc.amount},${inc.source.replace(",", " ")},$dateStr,${inc.note.replace(",", " ")}\n")
            }
            expenses.forEach { exp ->
                val dateStr = dateFormat.format(Date(exp.dateMillis))
                csvBuilder.append("Expense,${exp.amount},${exp.category.replace(",", " ")},$dateStr,${exp.note.replace(",", " ")}\n")
            }
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvBuilder.toString().toByteArray())
            }
        }
    }

    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
    
    val totalExpense = categoryTotals.values.sum()

    // Assigning colors for pie chart
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF43A047), // Green
        Color(0xFFE53935), // Red
        Color(0xFFFBC02D), // Yellow
        Color(0xFF8E24AA), // Purple
        Color(0xFF00ACC1)  // Cyan
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Expenses by Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            if (totalExpense > 0) {
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = 0f
                        categoryTotals.entries.forEachIndexed { index, entry ->
                            val sweepAngle = ((entry.value / totalExpense) * 360).toFloat()
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Legend
                categoryTotals.entries.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(16.dp),
                            color = colors[index % colors.size],
                            shape = MaterialTheme.shapes.small
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(entry.key, modifier = Modifier.weight(1f))
                        Text("₹${"%.2f".format(entry.value)}", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text("No data available for analytics.")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { exportLauncher.launch("SpendSense_Report.csv") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Export Report as CSV")
            }
        }
    }
}
