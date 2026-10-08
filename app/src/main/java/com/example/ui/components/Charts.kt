package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategorySummary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.FormatUtils
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonutChart(
    categories: List<CategorySummary>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty() || totalExpense <= 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada data pengeluaran bulan ini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(categories) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                val strokeWidth = 32.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                )
                val arcSize = Size(radius * 2, radius * 2)

                var startAngle = -90f

                categories.forEachIndexed { index, cat ->
                    val sweepAngle = ((cat.totalAmount / totalExpense) * 360f).toFloat() * animationProgress.value
                    val isSelected = selectedIndex == index
                    val extraStroke = if (isSelected) 8.dp.toPx() else 0f

                    drawArc(
                        color = Color(cat.colorHex),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle - 2f, // subtle gap
                        useCenter = false,
                        topLeft = Offset(topLeft.x - extraStroke / 2, topLeft.y - extraStroke / 2),
                        size = Size(arcSize.width + extraStroke, arcSize.height + extraStroke),
                        style = Stroke(
                            width = strokeWidth + extraStroke,
                            cap = StrokeCap.Round
                        )
                    )
                    startAngle += sweepAngle
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                if (selectedIndex != null && selectedIndex!! < categories.size) {
                    val selCat = categories[selectedIndex!!]
                    Text(
                        text = selCat.categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = FormatUtils.formatRupiah(selCat.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(selCat.colorHex),
                        textAlign = TextAlign.Center
                    )
                    val pct = (selCat.totalAmount / totalExpense) * 100.0
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f%%", pct),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        text = "Total Pengeluaran",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatRupiah(totalExpense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${categories.size} Kategori",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category interactive legend chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { index, cat ->
                val isSelected = selectedIndex == index
                val pct = (cat.totalAmount / totalExpense) * 100.0

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(cat.colorHex).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(cat.colorHex)) else null,
                    modifier = Modifier.clickable {
                        selectedIndex = if (isSelected) null else index
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(cat.colorHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.categoryName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f%%", pct),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailySpendingBarChart(
    dailyExpenses: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    if (dailyExpenses.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada riwayat pengeluaran harian",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Sort by date key "yyyy-MM-dd"
    val sortedDays = dailyExpenses.toList().sortedBy { it.first }
    val maxAmount = max(sortedDays.maxOfOrNull { it.second } ?: 1.0, 1.0)

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyExpenses) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(700))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tren Pengeluaran Harian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Maks: ${FormatUtils.formatRupiah(maxAmount)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

            Canvas(modifier = Modifier.matchParentSize()) {
                val chartHeight = size.height - 24.dp.toPx()
                val totalBars = sortedDays.size
                val barSpacing = 6.dp.toPx()
                val availableWidth = size.width
                val barWidth = max(((availableWidth - (totalBars * barSpacing)) / totalBars), 8f)

                // Draw horizontal guide lines
                val steps = 3
                for (i in 0..steps) {
                    val lineY = (chartHeight / steps) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw bars
                sortedDays.forEachIndexed { index, pair ->
                    val ratio = (pair.second / maxAmount).toFloat()
                    val barHeight = (ratio * chartHeight) * animProgress.value
                    val x = index * (barWidth + barSpacing) + (barSpacing / 2)
                    val y = chartHeight - barHeight

                    // Bar fill
                    drawRoundRect(
                        color = if (pair.second == maxAmount) ExpenseRed else primaryColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }
        }

        // Days labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (sortedDays.isNotEmpty()) {
                val firstDay = sortedDays.first().first.takeLast(2)
                val midDay = sortedDays[sortedDays.size / 2].first.takeLast(2)
                val lastDay = sortedDays.last().first.takeLast(2)

                Text("Tgl $firstDay", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("Tgl $midDay", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("Tgl $lastDay", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun CategoryBudgetItem(
    summary: CategorySummary,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budget = summary.budgetLimit
    val spent = summary.totalAmount
    val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = budget > 0 && spent > budget
    val isNearBudget = budget > 0 && spent >= budget * 0.8 && !isOverBudget

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(summary.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = CategoryIconHelper.getIcon(summary.iconKey),
                            contentDescription = summary.categoryName,
                            tint = Color(summary.colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = summary.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${summary.transactionCount} transaksi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { onEditBudgetClick() }
                ) {
                    Text(
                        text = if (budget > 0) "Ubah Anggaran" else "+ Anggaran",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Terpakai",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatRupiah(spent),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (budget > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Batas Anggaran",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatRupiah(budget),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (budget > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        isOverBudget -> ExpenseRed
                        isNearBudget -> Color(0xFFF59E0B)
                        else -> IncomeGreen
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val remaining = budget - spent
                    Text(
                        text = when {
                            isOverBudget -> "⚠️ Melebihi anggaran: ${FormatUtils.formatRupiah(-remaining)}"
                            isNearBudget -> "⚡ Mendekati batas (tersisa ${FormatUtils.formatRupiah(remaining)})"
                            else -> "Tersisa: ${FormatUtils.formatRupiah(remaining)}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isOverBudget -> ExpenseRed
                            isNearBudget -> Color(0xFFB45309)
                            else -> IncomeGreen
                        },
                        fontWeight = FontWeight.Medium
                    )

                    val pctInt = (progress * 100).toInt()
                    Text(
                        text = "$pctInt%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
