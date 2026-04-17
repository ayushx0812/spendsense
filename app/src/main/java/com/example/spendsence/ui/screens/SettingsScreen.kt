package com.example.spendsence.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentWorkspaceId by viewModel.currentWorkspaceIdFlow.collectAsState()
    val isSeeding by viewModel.isSeeding.collectAsState()

    var shareUid by remember { mutableStateOf("") }
    var showSeedConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Sharing") },
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ── Account Details ───────────────────────────────────────────────
            Text("Account Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Email: ${currentUser?.email ?: "Not logged in"}")
            Text("User ID: ${currentUser?.uid ?: "Unknown"}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // ── Demo Data Section ─────────────────────────────────────────────
            Text("Demo Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Clears ALL your current data and populates the app with realistic sample data — expenses, income, accounts, budgets, savings goals, transfers, and more.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showSeedConfirm = true },
                enabled = !isSeeding,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                if (isSeeding) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Loading demo data…", color = Color.White)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Load Demo Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // ── Shared Workspace ──────────────────────────────────────────────
            Text("Shared Workspace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Currently viewing workspace: ${currentWorkspaceId ?: "None"}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter another user's ID to view their shared data (e.g. family member).",
                style = MaterialTheme.typography.bodySmall, color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = shareUid,
                onValueChange = { shareUid = it },
                label = { Text("Enter User ID to link") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { if (shareUid.isNotBlank()) viewModel.linkToWorkspace(shareUid) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) { Text("View Workspace") }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { currentUser?.uid?.let { viewModel.linkToWorkspace(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Return to My Own Workspace") }

            Spacer(modifier = Modifier.weight(1f))

            // ── Logout ────────────────────────────────────────────────────────
            Button(
                onClick = { viewModel.logout(); onLogoutSuccess() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Confirmation Dialog ───────────────────────────────────────────────────
    if (showSeedConfirm) {
        AlertDialog(
            onDismissRequest = { showSeedConfirm = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear & Load Demo Data?") },
            text = {
                Text(
                    "This will permanently DELETE all your current expenses, income, accounts, savings goals, budgets, and transfers, then replace them with sample demo data.\n\nThis cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSeedConfirm = false
                        viewModel.seedDemoData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Yes, Clear & Load", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
