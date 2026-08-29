package com.quicpos.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quicpos.app.domain.model.TicketLine
import com.quicpos.app.ui.theme.*

@Composable
fun TicketLineItem(
    line: TicketLine,
    currencyCode: String = "KHR",
    isDualCurrencyEnabled: Boolean = false,
    secondaryCurrencyCode: String = "USD",
    exchangeRate: Double = 4000.0,
    onQuantityChange: (Double) -> Unit,
    onRemove: () -> Unit,
    onDiscountClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val secondaryLineTotalText = if (isDualCurrencyEnabled && exchangeRate > 0) {
        val secondaryAmount = if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
            line.calculatedLineTotal / exchangeRate
        } else if (currencyCode == "USD" && secondaryCurrencyCode == "KHR") {
            line.calculatedLineTotal * exchangeRate
        } else {
            line.calculatedLineTotal / exchangeRate
        }
        val formatted = if (secondaryCurrencyCode == "KHR") {
            String.format("%,.0f", secondaryAmount)
        } else {
            String.format("%,.2f", secondaryAmount)
        }
        "($secondaryCurrencyCode $formatted)"
    } else ""

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity Controls
            QuantitySelector(
                quantity = line.quantity,
                onQuantityChange = onQuantityChange,
                compact = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Item Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = onDiscountClick != null) { onDiscountClick?.invoke() }
            ) {
                Text(
                    text = line.itemName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Modifiers
                if (line.modifiers.isNotEmpty()) {
                    Text(
                        text = line.modifiers.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Unit Price
                Text(
                    text = "$currencyCode ${Item.formatPrice(line.effectiveUnitPrice)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Line Total & Secondary
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currencyCode ${Item.formatPrice(line.calculatedLineTotal)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Green500
                )

                if (secondaryLineTotalText.isNotBlank()) {
                    Text(
                        text = secondaryLineTotalText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (line.discountAmount > 0) {
                    Text(
                        text = "-$currencyCode ${Item.formatPrice(line.discountAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Using Item.formatPrice helper
private object Item {
    fun formatPrice(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            String.format("%,.0f", amount)
        } else {
            String.format("%,.2f", amount)
        }
    }
}
