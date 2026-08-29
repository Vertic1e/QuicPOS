package com.quicpos.app.ui.sales

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicpos.app.ui.components.*
import com.quicpos.app.ui.theme.*

@Composable
fun TicketScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCharge: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var discountLineIndex by remember { mutableStateOf<Int?>(null) }
    var showCustomerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Ticket [${uiState.ticketItemCount}]",
                onBackClick = onNavigateBack,
                actions = {
                    if (!uiState.isTicketEmpty) {
                        IconButton(
                            onClick = { showClearDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear Ticket",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Customer Header Bar (if customer assigned or can assign)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomerDialog = true },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (uiState.customerName != null) Green500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = uiState.customerName?.let { "Customer: $it" } ?: "Add Customer / Table",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (uiState.customerName != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.customerName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Customer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (uiState.isTicketEmpty) {
                // Empty State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No items in ticket",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        TextButton(onClick = onNavigateBack) {
                            Text("Add items")
                        }
                    }
                }
            } else {
                // Ticket Lines
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = uiState.ticketLines,
                        key = { index, line -> "${line.itemId}_$index" }
                    ) { index, line ->
                        TicketLineItem(
                            line = line,
                            currencyCode = uiState.currencyCode,
                            isDualCurrencyEnabled = uiState.isDualCurrencyEnabled,
                            secondaryCurrencyCode = uiState.secondaryCurrencyCode,
                            exchangeRate = uiState.exchangeRate,
                            onQuantityChange = { qty ->
                                viewModel.onIntent(SalesIntent.UpdateLineQuantity(index, qty))
                            },
                            onRemove = {
                                viewModel.onIntent(SalesIntent.RemoveTicketLine(index))
                            },
                            onDiscountClick = {
                                discountLineIndex = index
                            }
                        )
                    }
                }

                // Summary Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${uiState.currencyCode} ${formatAmount(uiState.subtotal)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                if (uiState.isDualCurrencyEnabled) {
                                    val secSubtotal = uiState.convertToSecondary(uiState.subtotal)
                                    Text(
                                        text = uiState.formatSecondary(secSubtotal),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Discount (if any)
                        if (uiState.totalDiscount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Discount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ErrorRed
                                )
                                Text(
                                    text = "-${uiState.currencyCode} ${formatAmount(uiState.totalDiscount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ErrorRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.isDualCurrencyEnabled) {
                                    val secTotal = uiState.convertToSecondary(uiState.grandTotal)
                                    Text(
                                        text = uiState.formatSecondary(secTotal),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WarningAmber
                                    )
                                }
                            }
                            Text(
                                text = "${uiState.currencyCode} ${formatAmount(uiState.grandTotal)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Green500
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Charge Button
                        ChargeButton(
                            totalAmount = uiState.grandTotal,
                            currencyCode = uiState.currencyCode,
                            itemCount = uiState.ticketItemCount,
                            onClick = onNavigateToCharge
                        )
                    }
                }
            }
        }
    }

    // Clear Ticket Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Filled.DeleteSweep, null, tint = ErrorRed) },
            title = { Text("Clear Ticket") },
            text = { Text("Are you sure you want to remove all items from this ticket?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onIntent(SalesIntent.ClearTicket)
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Line Discount Dialog
    discountLineIndex?.let { index ->
        val line = uiState.ticketLines.getOrNull(index)
        if (line != null) {
            var discountInput by remember { mutableStateOf(if (line.discountAmount > 0) line.discountAmount.toString() else "") }

            AlertDialog(
                onDismissRequest = { discountLineIndex = null },
                title = { Text("Discount for ${line.itemName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Line price: ${uiState.currencyCode} ${formatAmount(line.effectiveUnitPrice * line.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Discount Amount (${uiState.currencyCode})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Quick percentage discounts
                            val totalBefore = line.effectiveUnitPrice * line.quantity
                            listOf(5, 10, 15, 20).forEach { pct ->
                                val amount = totalBefore * (pct / 100.0)
                                OutlinedButton(
                                    onClick = { discountInput = amount.toString() },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text("$pct%")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val disc = discountInput.toDoubleOrNull() ?: 0.0
                            viewModel.onIntent(SalesIntent.ApplyLineDiscount(index, disc))
                            discountLineIndex = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Green500)
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { discountLineIndex = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // Customer Dialog
    if (showCustomerDialog) {
        var customerInput by remember { mutableStateOf(uiState.customerName ?: "") }

        AlertDialog(
            onDismissRequest = { showCustomerDialog = false },
            title = { Text("Customer / Table") },
            text = {
                OutlinedTextField(
                    value = customerInput,
                    onValueChange = { customerInput = it },
                    label = { Text("Name or Table Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onIntent(SalesIntent.SetCustomerName(customerInput.ifBlank { null }))
                        showCustomerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        String.format("%,.0f", amount)
    } else {
        String.format("%,.2f", amount)
    }
}
