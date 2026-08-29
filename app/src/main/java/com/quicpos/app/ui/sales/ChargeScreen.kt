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
import com.quicpos.app.domain.model.TicketLine
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
    var showReceiptPreview by remember { mutableStateOf(false) }
    var isSaleFinished by remember { mutableStateOf(false) }
    var completedReceipt by remember { mutableStateOf<Receipt?>(null) }
    var completedLines by remember { mutableStateOf<List<TicketLine>>(emptyList()) }

    val totalPrimary = uiState.grandTotal
    val totalSecondary = uiState.convertToSecondary(totalPrimary)

    // Calculate amounts depending on tender currency
    val enteredAmount = cashTendered.toDoubleOrNull() ?: 0.0

    val (cashAmountPrimary, isAmountSufficient) = if (tenderCurrency == uiState.currencyCode) {
        enteredAmount to (enteredAmount >= totalPrimary || enteredAmount == 0.0)
    } else {
        val converted = uiState.convertToPrimary(enteredAmount)
        converted to (enteredAmount >= totalSecondary - 0.001 || enteredAmount == 0.0)
    }

    val changeAmountPrimary = (cashAmountPrimary - totalPrimary).coerceAtLeast(0.0)
    val changeAmountSecondary = uiState.convertToSecondary(changeAmountPrimary)

    // Fallback preview receipt object
    val previewReceipt = Receipt(
        receiptNumber = "#PREVIEW",
        timestamp = System.currentTimeMillis(),
        subtotalAmount = uiState.subtotal,
        taxAmount = 0.0,
        discountAmount = uiState.totalDiscount,
        totalAmount = uiState.grandTotal,
        paymentMethod = selectedPayment,
        cashTendered = if (cashAmountPrimary > 0) cashAmountPrimary else totalPrimary,
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
                    IconButton(onClick = {
                        isSaleFinished = false
                        showReceiptPreview = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
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
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaymentMethodButton(
                    icon = Icons.Filled.Payments,
                    label = "Cash",
                    isSelected = selectedPayment == "CASH",
                    color = Green500,
                    onClick = { selectedPayment = "CASH" },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    icon = Icons.Filled.QrCode2,
                    label = "KHQR",
                    isSelected = selectedPayment == "KHQR",
                    color = KhqrRed,
                    onClick = { selectedPayment = "KHQR" },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    icon = Icons.Filled.CreditCard,
                    label = "Card",
                    isSelected = selectedPayment == "CARD",
                    color = Blue500,
                    onClick = { selectedPayment = "CARD" },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = "Wallet",
                    isSelected = selectedPayment == "WALLET",
                    color = WarningAmber,
                    onClick = { selectedPayment = "WALLET" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Cash Tender Section (when Cash is selected)
            if (selectedPayment == "CASH") {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Currency Selector for Cash Tender
                        if (uiState.isDualCurrencyEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pay With",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = tenderCurrency == uiState.currencyCode,
                                        onClick = {
                                            tenderCurrency = uiState.currencyCode
                                            cashTendered = ""
                                        },
                                        label = { Text(uiState.currencyCode) }
                                    )
                                    FilterChip(
                                        selected = tenderCurrency == uiState.secondaryCurrencyCode,
                                        onClick = {
                                            tenderCurrency = uiState.secondaryCurrencyCode
                                            cashTendered = ""
                                        },
                                        label = { Text(uiState.secondaryCurrencyCode) }
                                    )
                                }
                            }
                        }

                        // Cash Tendered Input
                        OutlinedTextField(
                            value = cashTendered,
                            onValueChange = { cashTendered = it },
                            label = { Text("Cash Received (Optional - Defaults to Exact Total)") },
                            placeholder = {
                                val exactVal = if (tenderCurrency == uiState.currencyCode) totalPrimary else totalSecondary
                                Text("Exact: $tenderCurrency ${formatAmount(exactVal)}")
                            },
                            prefix = { Text("$tenderCurrency ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Cash Suggestion Buttons
                        val quickAmounts = if (tenderCurrency == "KHR") {
                            listOf(
                                totalPrimary,
                                Math.ceil(totalPrimary / 5000.0) * 5000.0,
                                Math.ceil(totalPrimary / 10000.0) * 10000.0,
                                Math.ceil(totalPrimary / 20000.0) * 20000.0
                            ).distinct().filter { it >= totalPrimary }
                        } else {
                            listOf(
                                totalSecondary,
                                Math.ceil(totalSecondary),
                                Math.ceil(totalSecondary / 5.0) * 5.0,
                                Math.ceil(totalSecondary / 10.0) * 10.0
                            ).distinct().filter { it >= totalSecondary }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickAmounts.take(4).forEach { amount ->
                                AssistChip(
                                    onClick = { cashTendered = formatAmount(amount).replace(",", "") },
                                    label = { Text(formatAmount(amount)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Change Calculation Display
                        if (enteredAmount > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Change Due",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${uiState.currencyCode} ${formatAmount(changeAmountPrimary)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isAmountSufficient) Green500 else MaterialTheme.colorScheme.error
                                    )
                                    if (uiState.isDualCurrencyEnabled && isAmountSufficient) {
                                        Text(
                                            text = "(${uiState.secondaryCurrencyCode} ${formatAmount(changeAmountSecondary)})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = WarningAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Customer Name
            OutlinedTextField(
                value = uiState.customerName ?: "",
                onValueChange = { viewModel.onIntent(SalesIntent.SetCustomerName(it.ifBlank { null })) },
                label = { Text("Customer Name (Optional)") },
                placeholder = { Text("e.g. Table 4 / John") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons: Cancel and 1-Tap Charge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isSaleFinished = false
                        showReceiptPreview = true
                    },
                    modifier = Modifier
                        .weight(0.35f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview")
                }

                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            try {
                                val (receipt, lines) = viewModel.completeSaleAndAutoPrint(
                                    paymentMethod = selectedPayment,
                                    cashTendered = if (cashAmountPrimary > 0) cashAmountPrimary else totalPrimary,
                                    customerName = uiState.customerName
                                )
                                completedReceipt = receipt
                                completedLines = lines
                                isSaleFinished = true
                                showReceiptPreview = true
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.65f)
                        .height(56.dp),
                    enabled = !isProcessing,
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
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Charge & Print",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Receipt Preview / Sale Complete Dialog
    if (showReceiptPreview) {
        val receiptToShow = completedReceipt ?: previewReceipt
        val linesToShow = if (completedLines.isNotEmpty()) completedLines else uiState.ticketLines

        ReceiptPreviewDialog(
            receipt = receiptToShow,
            lines = linesToShow,
            businessName = uiState.businessName,
            currencyCode = uiState.currencyCode,
            isDualCurrencyEnabled = uiState.isDualCurrencyEnabled,
            secondaryCurrencyCode = uiState.secondaryCurrencyCode,
            exchangeRate = uiState.exchangeRate,
            onDismiss = {
                showReceiptPreview = false
                if (isSaleFinished) {
                    onSaleComplete()
                }
            },
            onPrint = {
                viewModel.reprintReceipt(receiptToShow, linesToShow)
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
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color), width = 2.dp) else null
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
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
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
