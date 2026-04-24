package com.example.spendsence.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spendsence.ui.screens.*
import com.example.spendsence.viewmodel.AppViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Splash : Screen("splash")
    object BiometricLock : Screen("biometric_lock")
    object Dashboard : Screen("dashboard")
    object MyFinances : Screen("my_finances")
    object Transactions : Screen("transactions")
    object AddExpense : Screen("add_expense")
    object AddIncome : Screen("add_income")
    object Analytics : Screen("analytics")
    object Budget : Screen("budget")
    object Settings : Screen("settings")
    object Transfers : Screen("transfers")
    object Accounts : Screen("accounts")
    object Savings : Screen("savings")
    object UpcomingExpenses : Screen("upcoming_expenses")
    object SpendingBudget : Screen("spending_budget")
    object Recurring : Screen("recurring")
    object YearSummary : Screen("year_summary")
    object Insights : Screen("insights")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        bottomBar = { AppBottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ── Splash ────────────────────────────────────────────────────────
            composable(Screen.Splash.route) {
                val context = LocalContext.current
                SplashScreen(onSplashFinished = {
                    val isLoggedInUser = currentUser != null && currentUser?.isAnonymous == false
                    val prefs = context.getSharedPreferences("spendsense_prefs", Context.MODE_PRIVATE)
                    val biometricEnabled = prefs.getBoolean("biometric_lock_enabled", false)
                    when {
                        isLoggedInUser && biometricEnabled -> navController.navigate(Screen.BiometricLock.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                        isLoggedInUser -> navController.navigate(Screen.MyFinances.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                        else -> navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                })
            }

            // ── Biometric Lock ────────────────────────────────────────────────
            composable(Screen.BiometricLock.route) {
                BiometricLockScreen(onUnlocked = {
                    navController.navigate(Screen.MyFinances.route) {
                        popUpTo(Screen.BiometricLock.route) { inclusive = true }
                    }
                })
            }

            // ── Auth ──────────────────────────────────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.MyFinances.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Signup.route) {
                SignupScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.MyFinances.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Bottom Nav Tabs ───────────────────────────────────────────────
            composable(Screen.MyFinances.route) {
                MyFinancesScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onNavigateToAddIncome = { navController.navigate(Screen.AddIncome.route) },
                    onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                    onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToTransfers = { navController.navigate(Screen.Transfers.route) },
                    onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                    onNavigateToSavings = { navController.navigate(Screen.Savings.route) },
                    onNavigateToUpcoming = { navController.navigate(Screen.UpcomingExpenses.route) },
                    onNavigateToSpendingBudget = { navController.navigate(Screen.SpendingBudget.route) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onNavigateToAddIncome = { navController.navigate(Screen.AddIncome.route) }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToTransfers = { navController.navigate(Screen.Transfers.route) },
                    onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                    onNavigateToSavings = { navController.navigate(Screen.Savings.route) },
                    onNavigateToRecurring = { navController.navigate(Screen.Recurring.route) },
                    onNavigateToYearSummary = { navController.navigate(Screen.YearSummary.route) },
                    onNavigateToInsights = { navController.navigate(Screen.Insights.route) },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Old Dashboard (kept as fallback) ──────────────────────────────
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onNavigateToAddIncome = { navController.navigate(Screen.AddIncome.route) },
                    onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                    onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // ── Transaction Entry ─────────────────────────────────────────────
            composable(Screen.AddExpense.route) {
                AddTransactionScreen(viewModel = viewModel, isExpense = true, onBack = { navController.popBackStack() })
            }
            composable(Screen.AddIncome.route) {
                AddTransactionScreen(viewModel = viewModel, isExpense = false, onBack = { navController.popBackStack() })
            }

            // ── Budget & Settings ─────────────────────────────────────────────
            composable(Screen.Budget.route) {
                BudgetScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLogoutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Finance Screens ───────────────────────────────────────────────
            composable(Screen.Transfers.route) {
                TransfersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Savings.route) {
                SavingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.UpcomingExpenses.route) {
                UpcomingExpensesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.SpendingBudget.route) {
                SpendingBudgetScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            // ── Upgrade Screens ───────────────────────────────────────────────
            composable(Screen.Recurring.route) {
                RecurringScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.YearSummary.route) {
                YearSummaryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Insights.route) {
                InsightsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
