package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppTab
import com.example.ui.components.BudgetFlowBottomBar
import com.example.ui.screens.AddTransactionSheet
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.RecurringBillsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionDetailDialog
import com.example.ui.screens.TransactionsListScreen
import com.example.ui.theme.BudgetFlowTheme
import com.example.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val budgetViewModel: BudgetViewModel = viewModel()
            val userSettings by budgetViewModel.userSettings.collectAsState()
            val currentTheme = userSettings["theme"] ?: "system"

            BudgetFlowTheme(themeSetting = currentTheme) {
                BudgetFlowApp(viewModel = budgetViewModel)
            }
        }
    }
}

@Composable
fun BudgetFlowApp(viewModel: BudgetViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeSubScreen by viewModel.activeSubScreen.collectAsState()
    val isAddTransactionOpen by viewModel.isAddTransactionOpen.collectAsState()
    val detailTx by viewModel.detailTransactionTarget.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect undo & notification events
    LaunchedEffect(Unit) {
        viewModel.undoEvents.collect { message ->
            if (message.contains("deleted", ignoreCase = true)) {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                }
            } else {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // System BackHandler
    BackHandler(enabled = activeSubScreen != null || currentTab != AppTab.HOME) {
        if (activeSubScreen != null) {
            viewModel.closeSubScreen()
        } else if (currentTab != AppTab.HOME) {
            viewModel.selectTab(AppTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (activeSubScreen == null) {
                BudgetFlowBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    onAddClick = { viewModel.openAddTransaction() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeSubScreen != null) {
                when (activeSubScreen) {
                    "transactions_list" -> TransactionsListScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                    "savings_goals" -> SavingsGoalsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                    "recurring_bills" -> RecurringBillsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                    "reports" -> ReportsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                    else -> HomeScreen(viewModel = viewModel)
                }
            } else {
                when (currentTab) {
                    AppTab.HOME -> HomeScreen(viewModel = viewModel)
                    AppTab.CALENDAR -> CalendarScreen(viewModel = viewModel)
                    AppTab.ADD -> {
                        // Triggers bottom sheet and shows Home underneath
                        LaunchedEffect(Unit) {
                            viewModel.openAddTransaction()
                        }
                        HomeScreen(viewModel = viewModel)
                    }
                    AppTab.NOTES -> NotesScreen(viewModel = viewModel)
                    AppTab.BUDGET -> BudgetScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Add / Edit Transaction Bottom Sheet
    if (isAddTransactionOpen) {
        AddTransactionSheet(viewModel = viewModel)
    }

    // Transaction Detail Dialog
    if (detailTx != null) {
        TransactionDetailDialog(
            transaction = detailTx!!,
            viewModel = viewModel
        )
    }
}
