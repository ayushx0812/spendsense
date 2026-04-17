package com.example.spendsence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// TxRow moved to file-level to avoid type inference issues with items()
private data class TxRow(
    val dateMillis: Long,
    val label: String,
    val amount: Double,
    val isIncome: Boolean,
    val sub: String,
    val id: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: AppViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAddIncome: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allIncomes by viewModel.allIncomes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") }
    var filterPeriod by remember { mutableStateOf("All Time") }

    // Bottom sheet state for the + button
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val monthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    val currentMonth = monthFormat.format(Date())
    val currentYear = yearFormat.format(Date())

    val combined: List<TxRow> = buildList {
        if (filterType != "Expense") {
            allIncomes.forEach { i -> add(TxRow(i.dateMillis, i.source, i.amount, true, i.note, i.id)) }
        }
        if (filterType != "Income") {
            allExpenses.forEach { e -> add(TxRow(e.dateMillis, e.category, e.amount, false, e.note, e.id)) }
        }
    }.filter { row ->
        val q = searchQuery.lowercase()
        val matchesQuery = q.isEmpty() || row.label.lowercase().contains(q) || row.sub.lowercase().contains(q)
        val matchesPeriod = when (filterPeriod) {
            "This Month" -> monthFormat.format(Date(row.dateMillis)) == currentMonth
            "This Year" -> yearFormat.format(Date(row.dateMillis)) == currentYear
            else -> true
        }
        matchesQuery && matchesPeriod
    }.sortedByDescending { it.dateMillis }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search transactions…", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Type filter chips (scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Income", "Expense").forEach { type ->
                    FilterChip(
                        selected = filterType == type,
                        onClick = { filterType = type },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Period filter chips (scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All Time", "This Month", "This Year").forEach { period ->
                    FilterChip(
                        selected = filterPeriod == period,
                        onClick = { filterPeriod = period },
                        label = { Text(period, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (combined.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No transactions found",
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items = combined, key = { "${it.id}_${it.isIncome}" }) { row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (row.isIncome) IncomeGreen.copy(alpha = 0.1f)
                                            else ExpenseRed.copy(alpha = 0.1f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (row.isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (row.isIncome) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(row.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    if (row.sub.isNotBlank()) {
                                        Text(row.sub, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                                    }
                                    Text(
                                        dateFormat.format(Date(row.dateMillis)),
                                        fontSize = 11.sp,
                                        color = Color(0xFFBDBDBD)
                                    )
                                }
                                Text(
                                    "${if (row.isIncome) "+" else "-"}₹${"%.2f".format(row.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (row.isIncome) IncomeGreen else ExpenseRed
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ── Add Transaction Bottom Sheet ──────────────────────────────────────────
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Add Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Add Income option
                Card(
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddIncome()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(IncomeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                "Add Income",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IncomeGreen
                            )
                            Text(
                                "Salary, freelance, gifts…",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = IncomeGreen
                        )
                    }
                }

                // Add Expense option
                Card(
                    onClick = {
                        showAddSheet = false
                        onNavigateToAddExpense()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ExpenseRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                "Add Expense",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ExpenseRed
                            )
                            Text(
                                "Food, rent, shopping…",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = ExpenseRed
                        )
                    }
                }
            }
        }
    }
}
