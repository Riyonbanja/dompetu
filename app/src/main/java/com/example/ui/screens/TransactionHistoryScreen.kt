package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.FormatUtils

@Composable
fun TransactionHistoryScreen(
    allTransactions: List<TransactionEntity>,
    selectedMonth: String,
    availableMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<String?>("ALL") } // "ALL", "EXPENSE", "INCOME"
    var filterScope by remember { mutableStateOf("MONTH") } // "MONTH" or "ALL_TIME"

    val filteredList = remember(allTransactions, searchQuery, filterType, filterScope, selectedMonth) {
        allTransactions.filter { tx ->
            val matchesScope = if (filterScope == "MONTH") tx.monthString == selectedMonth else true
            val matchesType = when (filterType) {
                "EXPENSE" -> tx.type == "EXPENSE"
                "INCOME" -> tx.type == "INCOME"
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                tx.categoryName.contains(searchQuery, ignoreCase = true) ||
                        tx.note.contains(searchQuery, ignoreCase = true) ||
                        tx.paymentMethod.contains(searchQuery, ignoreCase = true)
            }
            matchesScope && matchesType && matchesQuery
        }
    }

    val groupedTransactions = remember(filteredList) {
        filteredList.groupBy { it.dateString }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Catatan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredList.size} Transaksi ditemukan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (filterScope == "MONTH") {
                MonthSelectorChip(
                    selectedMonth = selectedMonth,
                    displayMonth = FormatUtils.getMonthDisplay(selectedMonth),
                    availableMonths = availableMonths,
                    onSelectMonth = onSelectMonth
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari belanja, kategori, metode...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus teks")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("search_transaction_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filterScope == "MONTH",
                    onClick = { filterScope = "MONTH" },
                    label = { Text("Bulan Ini") }
                )
            }
            item {
                FilterChip(
                    selected = filterScope == "ALL_TIME",
                    onClick = { filterScope = "ALL_TIME" },
                    label = { Text("Semua Periode") }
                )
            }
            item {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("Semua Tipe") }
                )
            }
            item {
                FilterChip(
                    selected = filterType == "EXPENSE",
                    onClick = { filterType = "EXPENSE" },
                    label = { Text("Pengeluaran") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = ExpenseRed
                    )
                )
            }
            item {
                FilterChip(
                    selected = filterType == "INCOME",
                    onClick = { filterType = "INCOME" },
                    label = { Text("Pemasukan") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = IncomeGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tidak ada catatan transaksi yang sesuai",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Coba ubah kata kunci pencarian atau filter tipe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                groupedTransactions.forEach { (dateStr, txList) ->
                    item(key = "header_$dateStr") {
                        Text(
                            text = FormatUtils.getRelativeDateLabel(dateStr),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    items(txList, key = { it.id }) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            onClick = { onTransactionClick(tx) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
