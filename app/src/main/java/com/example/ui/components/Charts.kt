package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.util.CurrencyUtils
import kotlin.math.atan2

data class SliceData(
    val label: String,
    val valueCents: Long,
    val color: Color
)

@Composable
fun DonutBreakdownChart(
    slices: List<SliceData>,
    totalCents: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
    onSliceSelected: (SliceData) -> Unit = {}
) {
    if (slices.isEmpty() || totalCents <= 0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses recorded this month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(190.dp)
                    .pointerInput(slices) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val angleRad = atan2(offset.y - center.y, offset.x - center.x)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            if (angleDeg < 0) angleDeg += 360f
                            // Align with startAngle = -90
                            var relativeAngle = (angleDeg + 90f) % 360f

                            var accumulated = 0f
                            for (slice in slices) {
                                val sweep = (slice.valueCents.toFloat() / totalCents.toFloat()) * 360f
                                if (relativeAngle in accumulated..(accumulated + sweep)) {
                                    onSliceSelected(slice)
                                    break
                                }
                                accumulated += sweep
                            }
                        }
                    }
            ) {
                val strokeWidth = 28.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
                val arcSize = Size(radius * 2, radius * 2)

                var startAngle = -90f
                for (slice in slices) {
                    val sweepAngle = (slice.valueCents.toFloat() / totalCents.toFloat()) * 360f * animatedProgress.value
                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f, // subtle gap
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatCents(totalCents, currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

data class MonthlyTrendItem(
    val monthLabel: String,
    val incomeCents: Long,
    val expenseCents: Long
)

@Composable
fun MonthlyTrendBarChart(
    trendData: List<MonthlyTrendItem>,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    if (trendData.isEmpty()) return

    val maxAmount = trendData.maxOfOrNull { maxOf(it.incomeCents, it.expenseCents) }?.coerceAtLeast(100L) ?: 100L

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            trendData.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(130.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Income bar
                            val incRatio = (item.incomeCents.toFloat() / maxAmount.toFloat()).coerceIn(0.04f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height((120 * incRatio).dp)
                                    .androidx.compose.foundation.background(
                                        IncomeGreenDark,
                                        androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Expense bar
                            val expRatio = (item.expenseCents.toFloat() / maxAmount.toFloat()).coerceIn(0.04f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height((120 * expRatio).dp)
                                    .androidx.compose.foundation.background(
                                        ExpenseRedDark,
                                        androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .androidx.compose.foundation.background(IncomeGreenDark, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Income",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .androidx.compose.foundation.background(ExpenseRedDark, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Expense",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
