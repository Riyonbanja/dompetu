package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CategorySummary
import com.example.model.TransactionEntity
import com.example.ui.dialogs.AddEditTransactionDialog
import com.example.ui.dialogs.SetBudgetDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

enum class MainTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Beranda", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    ANALYTICS("Grafik", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    HISTORY("Riwayat", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    EXPORT("Ekspor", Icons.Filled.FileDownload, Icons.Outlined.FileDownload)
}

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: FinanceViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    // Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingBudgetCategory by remember { mutableStateOf<CategorySummary?>(null) }

    // Observe ViewModel state
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val currentMonthTransactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val statusMessage by viewModel.exportStatusMessage.collectAsStateWithLifecycle()

    // Handle Back Button navigation
    if (currentTab != MainTab.HOME) {
        BackHandler {
            currentTab = MainTab.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentTab) {
            MainTab.HOME -> {
                HomeScreen(
                    summary = summary,
                    transactions = currentMonthTransactions,
                    availableMonths = availableMonths,
                    selectedMonth = selectedMonth,
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onAddTransactionClick = { showAddDialog = true },
                    onTransactionClick = { editingTransaction = it },
                    onNavigateToAnalytics = { currentTab = MainTab.ANALYTICS },
                    onNavigateToHistory = { currentTab = MainTab.HISTORY },
                    onNavigateToExport = { currentTab = MainTab.EXPORT },
                    onSetBudgetClick = { editingBudgetCategory = it },
                    modifier = screenModifier
                )
            }
            MainTab.ANALYTICS -> {
                AnalyticsScreen(
                    summary = summary,
                    selectedMonth = selectedMonth,
                    availableMonths = availableMonths,
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onNavigateToExport = { currentTab = MainTab.EXPORT },
                    onSetBudgetClick = { editingBudgetCategory = it },
                    modifier = screenModifier
                )
            }
            MainTab.HISTORY -> {
                TransactionHistoryScreen(
                    allTransactions = allTransactions,
                    selectedMonth = selectedMonth,
                    availableMonths = availableMonths,
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onTransactionClick = { editingTransaction = it },
                    modifier = screenModifier
                )
            }
            MainTab.EXPORT -> {
                ExportScreen(
                    summary = summary,
                    transactions = currentMonthTransactions,
                    selectedMonth = selectedMonth,
                    availableMonths = availableMonths,
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onExportPdf = { ctx -> viewModel.exportToPdf(ctx) },
                    onExportExcel = { ctx -> viewModel.exportToExcel(ctx) },
                    statusMessage = statusMessage,
                    onClearStatusMessage = { viewModel.clearStatusMessage() },
                    modifier = screenModifier
                )
            }
        }

        // Add Transaction Dialog
        if (showAddDialog) {
            AddEditTransactionDialog(
                initialTransaction = null,
                onDismiss = { showAddDialog = false },
                onSave = { amount, type, category, note, paymentMethod, timestamp ->
                    viewModel.addTransaction(amount, type, category, note, paymentMethod, timestamp)
                    showAddDialog = false
                }
            )
        }

        // Edit Transaction Dialog
        if (editingTransaction != null) {
            AddEditTransactionDialog(
                initialTransaction = editingTransaction,
                onDismiss = { editingTransaction = null },
                onSave = { amount, type, category, note, paymentMethod, timestamp ->
                    viewModel.updateTransaction(
                        editingTransaction!!.id,
                        amount,
                        type,
                        category,
                        note,
                        paymentMethod,
                        timestamp
                    )
                    editingTransaction = null
                },
                onDelete = {
                    viewModel.deleteTransaction(editingTransaction!!)
                    editingTransaction = null
                }
            )
        }

        // Set Budget Dialog
        if (editingBudgetCategory != null) {
            SetBudgetDialog(
                categorySummary = editingBudgetCategory!!,
                monthDisplay = summary.displayMonth,
                onDismiss = { editingBudgetCategory = null },
                onSaveBudget = { categoryId, limit ->
                    viewModel.setBudget(categoryId, limit)
                    editingBudgetCategory = null
                }
            )
        }
    }
}
