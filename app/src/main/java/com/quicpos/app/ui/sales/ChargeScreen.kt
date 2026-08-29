package com.quicpos.app.ui.sales

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.ui.components.ReceiptPreviewDialog
import com.quicpos.app.ui.components.SimpleTopBar
import com.quicpos.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChargeScreen(
    onNavigateBack: () -> Unit,
    onSaleComplete: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedPayment by remember { mutableStateOf("CASH") }
    var tenderCurrency by remember { mutableStateOf(uiState.currencyCode) } // Pay in KHR or USD
    var cashTendered by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showReceiptPreview by remember { mutableStateOf(false) }
    var completedReceipt by remember { mutableStateOf<Receipt?>(null) }
    var receiptNumber by remember { mutableStateOf("") }

    val totalPrimary = uiState.grandTotal
    val totalSecondary = uiState.convertToSecondary(totalPrimary)

    // Calculate amounts depending on tender currency
    val enteredAmount = cashTendered.toDoubleOrNull() ?: 0.0

    val (cashAmountPrimary, isAmountSufficient) = if (tenderCurrency == uiState.currencyCode) {
        enteredAmount to (enteredAmount >= totalPrimary)
    } else {
        val converted = uiState.convertToPrimary(enteredAmount)
        converted to (enteredAmount >= totalSecondary - 0.001)
    }

    val changeAmountPrimary = (cashAmountPrimary - totalPrimary).coerceAtLeast(0.0)
    val changeAmountSecondary = uiState.convertToSecondary(changeAmountPrimary)

    // Preview receipt object
    val previewReceipt = Receipt(
        receiptNumber = if (receiptNumber.isNotBlank()) receiptNumber else "#PREVIEW",
        timestamp = System.currentTimeMillis(),
        subtotalAmount = uiState.subtotal,
        taxAmount = 0.0,
        discountAmount = uiState.totalDiscount,
        totalAmount = uiState.grandTotal,
        paymentMethod = selectedPayment,
        cashTendered = cashAmountPrimary,
        changeGiven = changeAmountPrimary,
        customerName = uiState.customerName,
        status = "COMPLETED",
        posRegister = uiState.posRegisterName
    )

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Charge",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showReceiptPreview = true }) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = "Preview Receipt",
                            tint = Green500
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Total Amount Display Card (with Dual Currency)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Green800.copy(alpha = 0.18f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Due",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.currencyCode} ${formatAmount(totalPrimary)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green500
                    )
                    if (uiState.isDualCurrencyEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "≈ ${uiState.secondaryCurrencyCode} ${formatAmount(totalSecondary)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = WarningAmber
                        )
                    }
                }
            }

            // Payment Method Selection
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethodButton(
                    icon = Icons.Filled.Money,
                    label = "Cash",
                    isSelected = selectedPayment == "CASH",
                    color = CashColor,
                    onClick = { selectedPayment = "CASH" },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    icon = Icons.Filled.CreditCard,
                    label = "Card",
                    isSelected = selectedPayment == "CARD",
                    color = CardColor,
                    onClick = { selectedPayment = "CARD" },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    icon = Icons.Filled.QrCode2,
                    label = "QR / Other",
                    isSelected = selectedPayment == "OTHER",
                    color = OtherPaymentColor,
                    onClick = { selectedPayment = "OTHER" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Cash Tendered Input (only for cash)
            AnimatedVisibility(visible = selectedPayment == "CASH") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Currency Selector if Dual Currency is Enabled
                    if (uiState.isDualCurrencyEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = tenderCurrency == uiState.currencyCode,
                                onClick = {
                                    tenderCurrency = uiState.currencyCode
                                    cashTendered = ""
                                },
                                label = { Text("Pay in ${uiState.currencyCode} (${uiState.currencySymbol})") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Green500,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = tenderCurrency == uiState.secondaryCurrencyCode,
                                onClick = {
                                    tenderCurrency = uiState.secondaryCurrencyCode
                                    cashTendered = ""
                                },
                                label = { Text("Pay in ${uiState.secondaryCurrencyCode} (${uiState.secondaryCurrencySymbol})") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Green500,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cashTendered,
                        onValueChange = { cashTendered = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cash Tendered ($tenderCurrency)") },
                        prefix = { Text("$tenderCurrency ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Cash Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickAmounts = if (tenderCurrency == uiState.currencyCode) {
                            listOf(
                                totalPrimary,
                                (totalPrimary / 1000).toLong() * 1000.0 + 1000.0,
                                (totalPrimary / 5000).toLong() * 5000.0 + 5000.0,
                                (totalPrimary / 10000).toLong() * 10000.0 + 10000.0
                            ).distinct().take(4)
                        } else {
                            val roundedSec = kotlin.math.ceil(totalSecondary)
                            listOf(
                                totalSecondary,
                                roundedSec,
                                roundedSec + 5.0,
                                roundedSec + 10.0
                            ).distinct().take(4)
                        }

                        quickAmounts.forEach { amount ->
                            OutlinedButton(
                                onClick = {
                                    cashTendered = if (tenderCurrency == "KHR") amount.toLong().toString() else String.format("%.2f", amount)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = formatAmount(amount),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Change Display
                    if (enteredAmount > 0 && isAmountSufficient) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Change Due",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${uiState.currencyCode} ${formatAmount(changeAmountPrimary)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                }

                                if (uiState.isDualCurrencyEnabled && changeAmountPrimary > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "≈ ${uiState.secondaryCurrencyCode} ${formatAmount(changeAmountSecondary)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Preview & Complete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showReceiptPreview = true },
                    modifier = Modifier
                        .weight(0.4f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview")
                }

                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            try {
                                val number = viewModel.completeSale(
                                    paymentMethod = selectedPayment,
                                    cashTendered = cashAmountPrimary,
                                    customerName = uiState.customerName
                                )
                                receiptNumber = number
                                completedReceipt = previewReceipt.copy(receiptNumber = number)
                                showSuccessDialog = true
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.6f)
                        .height(56.dp),
                    enabled = !isProcessing && (selectedPayment != "CASH" || isAmountSufficient),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green500,
                        contentColor = Color.White
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Charge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Receipt Preview Dialog
    if (showReceiptPreview) {
        ReceiptPreviewDialog(
            receipt = completedReceipt ?: previewReceipt,
            lines = uiState.ticketLines,
            businessName = uiState.businessName,
            currencyCode = uiState.currencyCode,
            isDualCurrencyEnabled = uiState.isDualCurrencyEnabled,
            secondaryCurrencyCode = uiState.secondaryCurrencyCode,
            exchangeRate = uiState.exchangeRate,
            onDismiss = { showReceiptPreview = false }
        )
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Green500,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Sale Complete!",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Receipt $receiptNumber has been created.",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Total Paid: ${uiState.currencyCode} ${formatAmount(totalPrimary)}" +
                                if (uiState.isDualCurrencyEnabled) " (${uiState.secondaryCurrencyCode} ${formatAmount(totalSecondary)})" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green500,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSaleComplete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                ) {
                    Text("New Sale")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccessDialog = false
                        showReceiptPreview = true
                    }
                ) {
                    Icon(Icons.Filled.Receipt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View Receipt")
                }
            }
        )
    }
}

@Composable
private fun PaymentMethodButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(color)
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
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
