package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    currencyCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type.equals("expense", ignoreCase = true)
    val amountColor = if (isExpense) ExpenseRedDark else IncomeGreenDark
    val sign = if (isExpense) "-" else "+"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(
                categoryName = transaction.category,
                size = 44.dp,
                iconSize = 22.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!transaction.subcategory.isNullOrBlank()) "${transaction.category} • ${transaction.subcategory}" else transaction.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.isRecurring) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Recurring",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (!transaction.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateUtils.formatShortDate(transaction.dateEpochDay),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (!transaction.paymentMethod.isNullOrBlank()) {
                        Text(
                            text = " • ${transaction.paymentMethod.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount is the largest element in any transaction row
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$sign${CurrencyUtils.formatCents(transaction.amountCents, currencyCode)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
        }
    }
}
