package com.example.spendsence.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.ui.theme.IncomeGreen
import com.example.spendsence.ui.theme.ExpenseRed
import com.example.spendsence.ui.theme.PrimaryBlue
import com.example.spendsence.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Shared row model used in both recent list and the detail sheet
private data class TxItem(
    val title: String,
    val category: String,
    val amount: Double,
    val isIncome: Boolean,
    val dateMillis: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFinancesScreen(
    viewModel: AppViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAddIncome: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTransfers: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToUpcoming: () -> Unit,
    onNavigateToSpendingBudget: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Sheet states
    var showAddSheet by remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // "Income" or "Expenses" detail sheet — null = closed
    var detailSheetType by remember { mutableStateOf<String?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val totalBalance by viewModel.totalBalance.collectAsState()
    val allIncomes by viewModel.allIncomes.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()

    // This-month figures
    val monthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
    val currentMonth = monthFormat.format(Date())
    val thisMonthIncome = allIncomes.filter {
        monthFormat.format(Date(it.dateMillis)) == currentMonth
    }.sumOf { it.amount }
    val thisMonthExpense = allExpenses.filter {
        monthFormat.format(Date(it.dateMillis)) == currentMonth
    }.sumOf { it.amount }

    // All income rows
    val allIncomeTx: List<TxItem> = remember(allIncomes) {
        allIncomes.map { TxItem(it.source, it.source, it.amount, true, it.dateMillis) }
            .sortedByDescending { it.dateMillis }
    }

    // All expense rows
    val allExpenseTx: List<TxItem> = remember(allExpenses) {
        allExpenses.map { TxItem(it.note.ifBlank { it.category }, it.category, it.amount, false, it.dateMillis) }
            .sortedByDescending { it.dateMillis }
    }

    // Recent transactions: top 10 combined
    val recentTx: List<TxItem> = remember(allIncomeTx, allExpenseTx) {
        (allIncomeTx + allExpenseTx).sortedByDescending { it.dateMillis }.take(10)
    }

    val drawerItems = listOf(
        DrawerItem("Income", Icons.Default.TrendingUp, onNavigateToAddIncome),
        DrawerItem("Expenses", Icons.Default.MoneyOff, onNavigateToAddExpense),
        DrawerItem("Spending Budget", Icons.Default.AccountBalance, onNavigateToSpendingBudget),
        DrawerItem("Budget", Icons.Default.Analytics, onNavigateToBudget),
        DrawerItem("Upcoming Expenses", Icons.Default.Schedule, onNavigateToUpcoming),
        DrawerItem("Transfers", Icons.Default.SwapHoriz, onNavigateToTransfers),
        DrawerItem("Accounts", Icons.Default.AccountBalanceWallet, onNavigateToAccounts),
        DrawerItem("Savings Goals", Icons.Default.Savings, onNavigateToSavings),
        DrawerItem("Analytics", Icons.Default.PieChart, onNavigateToAnalytics),
        DrawerItem("Settings", Icons.Default.Settings, onNavigateToSettings),
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primary) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "SpendSense",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Text(
                    "My Finances",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label, tint = Color.White) },
                        label = { Text(item.label, color = Color.White, fontSize = 14.sp) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            item.action()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SpendSense") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
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
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ── Balance Hero Card ─────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(PrimaryBlue, PrimaryBlue.copy(alpha = 0.8f))
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Total Balance",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "₹${"%.2f".format(totalBalance)}",
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(24.dp))

                            // ── Tappable Income / Expense stats ──────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Income — tap to open income detail sheet
                                MonthStat(
                                    label = "Income",
                                    amount = thisMonthIncome,
                                    icon = Icons.Default.TrendingUp,
                                    color = IncomeGreen,
                                    onClick = { detailSheetType = "Income" }
                                )

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(48.dp)
                                        .background(Color.White.copy(alpha = 0.3f))
                                )

                                // Expenses — tap to open expense detail sheet
                                MonthStat(
                                    label = "Expenses",
                                    amount = thisMonthExpense,
                                    icon = Icons.Default.TrendingDown,
                                    color = ExpenseRed,
                                    onClick = { detailSheetType = "Expenses" }
                                )
                            }
                        }
                    }
                }

                // ── Recent Transactions header ────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (recentTx.isNotEmpty()) {
                            TextButton(
                                onClick = onNavigateToAnalytics,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("See All", fontSize = 13.sp, color = PrimaryBlue)
                            }
                        }
                    }
                }

                // ── Transactions List ─────────────────────────────────────────
                if (recentTx.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("No transactions yet", color = Color.Gray, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Tap + to add income or expense",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(recentTx) { tx ->
                        TxCard(tx = tx)
                    }
                }
            }
        }
    }

    // ── Income / Expense Detail Bottom Sheet ─────────────────────────────────
    if (detailSheetType != null) {
        val isIncome = detailSheetType == "Income"
        val sheetList = if (isIncome) allIncomeTx else allExpenseTx
        val sheetColor = if (isIncome) IncomeGreen else ExpenseRed
        val sheetIcon = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown
        val sheetTotal = if (isIncome) allIncomes.sumOf { it.amount } else allExpenses.sumOf { it.amount }

        ModalBottomSheet(
            onDismissRequest = { detailSheetType = null },
            sheetState = detailSheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Sheet header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(sheetColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(sheetIcon, contentDescription = null, tint = sheetColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                detailSheetType!!,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Total: ₹${"%.2f".format(sheetTotal)}",
                                fontSize = 12.sp,
                                color = sheetColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    // Add button
                    FilledTonalButton(
                        onClick = {
                            detailSheetType = null
                            if (isIncome) onNavigateToAddIncome() else onNavigateToAddExpense()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = sheetColor.copy(alpha = 0.12f),
                            contentColor = sheetColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                if (sheetList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                sheetIcon,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "No ${detailSheetType!!.lowercase()} records yet",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sheetList) { tx ->
                            TxCard(tx = tx)
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    // ── Add Transaction Bottom Sheet ──────────────────────────────────────────
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = addSheetState,
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

                // Add Income
                Card(
                    onClick = { showAddSheet = false; onNavigateToAddIncome() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(IncomeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Add Income", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IncomeGreen)
                            Text("Salary, freelance, gifts…", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = IncomeGreen)
                    }
                }

                // Add Expense
                Card(
                    onClick = { showAddSheet = false; onNavigateToAddExpense() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(ExpenseRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Add Expense", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ExpenseRed)
                            Text("Food, rent, shopping…", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ExpenseRed)
                    }
                }
            }
        }
    }
}

// ─── Supporting Composables ───────────────────────────────────────────────────

private data class DrawerItem(val label: String, val icon: ImageVector, val action: () -> Unit)

@Composable
private fun MonthStat(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "₹${"%.0f".format(amount)}",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text("This month", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)

        // Tap hint indicator
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("View all", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun TxCard(tx: TxItem) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateStr = dateFormat.format(Date(tx.dateMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (tx.isIncome) IncomeGreen.copy(alpha = 0.12f)
                        else ExpenseRed.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (tx.isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                Text("${tx.category} • $dateStr", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                "${if (tx.isIncome) "+" else "-"}₹${"%.2f".format(tx.amount)}",
                color = if (tx.isIncome) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
