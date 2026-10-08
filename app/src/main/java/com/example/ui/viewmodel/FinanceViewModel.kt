package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.model.CategoryBudgetEntity
import com.example.model.CategoryItem
import com.example.model.CategorySummary
import com.example.model.DefaultCategories
import com.example.model.MonthlyFinanceSummary
import com.example.model.TransactionEntity
import com.example.model.TransactionType
import com.example.util.ExcelExporter
import com.example.util.FormatUtils
import com.example.util.PdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _selectedMonth = MutableStateFlow(FormatUtils.getCurrentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterType = MutableStateFlow<String?>("ALL") // "ALL", "EXPENSE", "INCOME"
    val selectedFilterType: StateFlow<String?> = _selectedFilterType.asStateFlow()

    private val _exportStatusMessage = MutableStateFlow<String?>(null)
    val exportStatusMessage: StateFlow<String?> = _exportStatusMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db.transactionDao())
    }

    val availableMonths: StateFlow<List<String>> = repository.availableMonths
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(FormatUtils.getCurrentMonthString())
        )

    val currentMonthTransactions: StateFlow<List<TransactionEntity>> = _selectedMonth
        .flatMapLatest { month ->
            repository.getTransactionsByMonth(month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentBudgets: StateFlow<List<CategoryBudgetEntity>> = _selectedMonth
        .flatMapLatest { month ->
            repository.getBudgetsForMonth(month)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Monthly summary with category breakdown, budget comparison, and daily timeline
    val monthlySummary: StateFlow<MonthlyFinanceSummary> = combine(
        _selectedMonth,
        currentMonthTransactions,
        currentBudgets
    ) { month, transactions, budgets ->
        val budgetMap = budgets.associate { it.categoryId to it.monthlyLimit }

        var totalIncome = 0.0
        var totalExpense = 0.0
        val catMap = mutableMapOf<String, MutableList<TransactionEntity>>()
        val dailyMap = mutableMapOf<String, Double>()

        transactions.forEach { tx ->
            if (tx.type == "INCOME") {
                totalIncome += tx.amount
            } else {
                totalExpense += tx.amount
                // Accumulate daily expense
                dailyMap[tx.dateString] = (dailyMap[tx.dateString] ?: 0.0) + tx.amount
                // Group by category
                catMap.getOrPut(tx.categoryId) { mutableListOf() }.add(tx)
            }
        }

        val categorySummaries = catMap.map { (catId, txList) ->
            val totalCat = txList.sumOf { it.amount }
            val meta = DefaultCategories.getById(catId)
            val name = meta?.name ?: txList.firstOrNull()?.categoryName ?: "Kategori"
            val color = meta?.colorHex ?: 0xFF10B981
            val icon = meta?.iconKey ?: "shopping_cart"

            CategorySummary(
                categoryId = catId,
                categoryName = name,
                type = TransactionType.EXPENSE,
                totalAmount = totalCat,
                transactionCount = txList.size,
                budgetLimit = budgetMap[catId] ?: 0.0,
                colorHex = color,
                iconKey = icon
            )
        }.sortedByDescending { it.totalAmount }

        MonthlyFinanceSummary(
            monthString = month,
            displayMonth = FormatUtils.getMonthDisplay(month),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = totalIncome - totalExpense,
            categoryBreakdown = categorySummaries,
            dailyExpenses = dailyMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyFinanceSummary(
            monthString = FormatUtils.getCurrentMonthString(),
            displayMonth = FormatUtils.getMonthDisplay(FormatUtils.getCurrentMonthString()),
            totalIncome = 0.0,
            totalExpense = 0.0,
            netBalance = 0.0,
            categoryBreakdown = emptyList(),
            dailyExpenses = emptyMap()
        )
    )

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: String?) {
        _selectedFilterType.value = type
    }

    fun clearStatusMessage() {
        _exportStatusMessage.value = null
    }

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: CategoryItem,
        note: String,
        paymentMethod: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
            val monthStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp)).take(7)

            val entity = TransactionEntity(
                amount = amount,
                type = type.name,
                categoryId = category.id,
                categoryName = category.name,
                note = note.trim(),
                paymentMethod = paymentMethod,
                timestamp = timestamp,
                dateString = dateStr,
                monthString = monthStr
            )
            repository.insertTransaction(entity)
        }
    }

    fun updateTransaction(
        id: Long,
        amount: Double,
        type: TransactionType,
        category: CategoryItem,
        note: String,
        paymentMethod: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
            val monthStr = dateStr.take(7)

            val entity = TransactionEntity(
                id = id,
                amount = amount,
                type = type.name,
                categoryId = category.id,
                categoryName = category.name,
                note = note.trim(),
                paymentMethod = paymentMethod,
                timestamp = timestamp,
                dateString = dateStr,
                monthString = monthStr
            )
            repository.updateTransaction(entity)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun setBudget(categoryId: String, amount: Double) {
        viewModelScope.launch {
            repository.setBudget(categoryId, _selectedMonth.value, amount)
        }
    }

    fun exportToPdf(context: Context): File? {
        val summary = monthlySummary.value
        val transactions = currentMonthTransactions.value
        val file = PdfExporter.generateAndSharePdf(context, summary, transactions)
        if (file != null) {
            _exportStatusMessage.value = "Laporan PDF berhasil dibuat & siap dibagikan!"
        } else {
            _exportStatusMessage.value = "Gagal membuat laporan PDF."
        }
        return file
    }

    fun exportToExcel(context: Context): File? {
        val summary = monthlySummary.value
        val transactions = currentMonthTransactions.value
        val file = ExcelExporter.generateAndShareExcel(context, summary, transactions)
        if (file != null) {
            _exportStatusMessage.value = "Laporan Excel/CSV berhasil dibuat & siap dibagikan!"
        } else {
            _exportStatusMessage.value = "Gagal membuat laporan Excel/CSV."
        }
        return file
    }
}
