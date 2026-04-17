package com.example.spendsence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.ui.theme.PrimaryBlue
import com.example.spendsence.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTransfers: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToYearSummary: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(36.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            currentUser?.email ?: "SpendSense User",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("₹${"%.0f".format(totalBalance)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Savings", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("₹${"%.0f".format(totalSavings)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Transactions", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("${allExpenses.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Quick links
            item {
                Text("Financial Tools", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            item {
                ProfileLinkSection(
                    links = listOf(
                        ProfileLink("Accounts", Icons.Default.AccountBalanceWallet, onNavigateToAccounts),
                        ProfileLink("Savings Goals", Icons.Default.Savings, onNavigateToSavings),
                        ProfileLink("Transfers", Icons.Default.SwapHoriz, onNavigateToTransfers),
                        ProfileLink("Recurring Transactions", Icons.Default.Repeat, onNavigateToRecurring),
                    )
                )
            }

            item {
                Text("Reports & Insights", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            item {
                ProfileLinkSection(
                    links = listOf(
                        ProfileLink("Insights", Icons.Default.TipsAndUpdates, onNavigateToInsights),
                        ProfileLink("Year Summary", Icons.Default.CalendarMonth, onNavigateToYearSummary),
                    )
                )
            }

            item {
                Text("App", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            item {
                ProfileLinkSection(
                    links = listOf(
                        ProfileLink("Settings", Icons.Default.Settings, onNavigateToSettings),
                    )
                )
            }

            // Logout
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private data class ProfileLink(val label: String, val icon: ImageVector, val action: () -> Unit)

@Composable
private fun ProfileLinkSection(links: List<ProfileLink>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            links.forEachIndexed { index, link ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        link.icon,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(link.label, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    IconButton(
                        onClick = link.action,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
                    }
                }
                if (index < links.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = Color(0xFFF0F0F0)
                    )
                }
            }
        }
    }
}
