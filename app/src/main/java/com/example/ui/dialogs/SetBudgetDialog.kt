package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.CategorySummary
import com.example.ui.components.CategoryIconHelper
import com.example.util.FormatUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetBudgetDialog(
    categorySummary: CategorySummary,
    monthDisplay: String,
    onDismiss: () -> Unit,
    onSaveBudget: (categoryId: String, limit: Double) -> Unit
) {
    var budgetText by remember {
        mutableStateOf(
            if (categorySummary.budgetLimit > 0) {
                categorySummary.budgetLimit.toLong().toString()
            } else ""
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Atur Anggaran Bulanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category header banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(categorySummary.colorHex).copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(categorySummary.colorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIconHelper.getIcon(categorySummary.iconKey),
                            contentDescription = categorySummary.categoryName,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = categorySummary.categoryName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Periode $monthDisplay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input ->
                        budgetText = input.filter { it.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Batas Maksimal Belanja (Rp)") },
                    placeholder = { Text("Contoh: 1500000") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else if (budgetText.isNotEmpty()) {
                            val v = budgetText.toDoubleOrNull() ?: 0.0
                            Text("Batas: ${FormatUtils.formatRupiah(v)}")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Presets
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500000L, 1000000L, 1500000L, 2000000L).forEach { amount ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                budgetText = amount.toString()
                                errorMessage = null
                            }
                        ) {
                            Text(
                                text = FormatUtils.formatRupiah(amount.toDouble()),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (categorySummary.budgetLimit > 0) {
                        Button(
                            onClick = {
                                onSaveBudget(categorySummary.categoryId, 0.0)
                            },
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Hapus Batas")
                        }
                    }

                    Button(
                        onClick = {
                            val limit = budgetText.toDoubleOrNull()
                            if (limit == null || limit <= 0.0) {
                                errorMessage = "Masukkan batas anggaran yang valid"
                                return@Button
                            }
                            onSaveBudget(categorySummary.categoryId, limit)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_budget_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
