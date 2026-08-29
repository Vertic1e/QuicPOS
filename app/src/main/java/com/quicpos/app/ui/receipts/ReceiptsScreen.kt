package com.quicpos.app.ui.receipts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.ui.components.QuicPOSSearchBar
import com.quicpos.app.ui.theme.*

@Composable
fun ReceiptsScreen(
    onReceiptClick: (String) -> Unit,
    viewModel: ReceiptsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search
        QuicPOSSearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.search(it) },
            placeholder = "Search receipts...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.receipts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Receipt, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) "No receipts found" else "No receipts yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.groupedReceipts.forEach { (dateGroup, receipts) ->
                    // Date Header
                    item(key = "header_$dateGroup") {
                        Text(
                            text = dateGroup,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Receipt Items
                    items(receipts, key = { it.receiptNumber }) { receipt ->
                        ReceiptRow(
                            receipt = receipt,
                            currencyCode = uiState.currencyCode,
                            onClick = { onReceiptClick(receipt.receiptNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    receipt: Receipt,
    currencyCode: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Payment Icon
            Icon(
                imageVector = when (receipt.paymentMethod) {
                    "CASH" -> Icons.Filled.Money
                    "CARD" -> Icons.Filled.CreditCard
                    else -> Icons.Filled.PhoneAndroid
                },
                contentDescription = receipt.paymentMethod,
                tint = when (receipt.paymentMethod) {
                    "CASH" -> CashColor
                    "CARD" -> CardColor
                    else -> OtherPaymentColor
                },
                modifier = Modifier.size(24.dp)
            )

            // Receipt Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = receipt.receiptNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (receipt.isRefunded) TextDecoration.LineThrough else null
                    )
                    if (receipt.isRefunded) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    "Refunded",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = ErrorRed.copy(alpha = 0.1f),
                                labelColor = ErrorRed
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
                Text(
                    text = receipt.formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = "$currencyCode ${formatAmount(receipt.totalAmount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (receipt.isRefunded) ErrorRed else Green500
            )

            // Chevron
            Icon(
                Icons.Filled.ChevronRight, null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        String.format("%,.0f", amount)
    } else {
        String.format("%,.2f", amount)
    }
}
