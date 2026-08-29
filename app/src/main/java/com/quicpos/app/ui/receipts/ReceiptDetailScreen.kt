package com.quicpos.app.ui.receipts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.ReceiptRepository
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.domain.model.TicketLine
import com.quicpos.app.printing.PrinterManager
import com.quicpos.app.ui.components.ReceiptPreviewDialog
import com.quicpos.app.ui.components.SimpleTopBar
import com.quicpos.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptDetailUiState(
    val receipt: Receipt? = null,
    val lines: List<TicketLine> = emptyList(),
    val businessName: String = "QuicPOS Store",
    val headerText: String = "",
    val footerText: String = "",
    val currencyCode: String = "KHR",
    val isDualCurrencyEnabled: Boolean = false,
    val secondaryCurrencyCode: String = "USD",
    val exchangeRate: Double = 4000.0,
    val receiptLogoUri: String = "",
    val showReceiptLogo: Boolean = false,
    val isLoading: Boolean = true,
    val isPrinting: Boolean = false,
    val printMessage: String? = null
)

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val settingsRepository: SettingsRepository,
    private val printerManager: PrinterManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val receiptNumber: String = savedStateHandle.get<String>("receiptNumber") ?: ""

    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()

    init {
        loadReceipt()
    }

    private fun loadReceipt() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            val result = receiptRepository.getReceiptWithLines(receiptNumber)
            _uiState.update {
                it.copy(
                    receipt = result?.first,
                    lines = result?.second ?: emptyList(),
                    businessName = settings.businessName,
                    headerText = settings.receiptHeader,
                    footerText = settings.receiptFooter,
                    currencyCode = settings.currencyCode,
                    isDualCurrencyEnabled = settings.isDualCurrencyEnabled,
                    secondaryCurrencyCode = settings.secondaryCurrencyCode,
                    exchangeRate = settings.exchangeRate,
                    receiptLogoUri = settings.receiptLogoUri,
                    showReceiptLogo = settings.showReceiptLogo,
                    isLoading = false
                )
            }
        }
    }

    fun refundReceipt() {
        viewModelScope.launch {
            receiptRepository.refundReceipt(receiptNumber)
            loadReceipt()
        }
    }

    fun printReceipt() {
        val receipt = uiState.value.receipt ?: return
        val lines = uiState.value.lines
        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true, printMessage = null) }
            val result = printerManager.printReceipt(receipt, lines)
            _uiState.update {
                it.copy(
                    isPrinting = false,
                    printMessage = if (result.isSuccess) "Print successful" else "Print failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearPrintMessage() {
        _uiState.update { it.copy(printMessage = null) }
    }
}

@Composable
fun ReceiptDetailScreen(
    receiptNumber: String,
    onNavigateBack: () -> Unit,
    viewModel: ReceiptDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRefundDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.printMessage) {
        uiState.printMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearPrintMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopBar(
                title = "Receipt $receiptNumber",
                onBackClick = onNavigateBack,
                actions = {
                    if (uiState.receipt != null && !uiState.receipt!!.isRefunded) {
                        IconButton(onClick = { showRefundDialog = true }) {
                            Icon(Icons.Filled.Undo, "Refund", tint = ErrorRed)
                        }
                    }
                    IconButton(onClick = { showPreviewDialog = true }) {
                        Icon(Icons.Filled.ReceiptLong, "Preview Thermal Receipt", tint = Green500)
                    }
                    IconButton(onClick = { viewModel.printReceipt() }) {
                        Icon(Icons.Filled.Print, "Print")
                    }
                }
            )
        }
    )
 { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val receipt = uiState.receipt ?: return@Scaffold

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Card
                if (receipt.isRefunded) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Info, null, tint = ErrorRed)
                            Text("This receipt has been refunded", color = ErrorRed)
                        }
                    }
                }

                // Receipt Header
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(receipt.formattedDateTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(receipt.paymentMethod, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Register", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(receipt.posRegister, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        if (receipt.customerName != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Customer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(receipt.customerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Line Items
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.lines.forEachIndexed { index, line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${line.displayQuantity}× ${line.itemName}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "@ ${uiState.currencyCode} ${formatAmount(line.unitPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${uiState.currencyCode} ${formatAmount(line.lineTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (index < uiState.lines.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }

                // Totals
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Green800.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text("${uiState.currencyCode} ${formatAmount(receipt.subtotalAmount)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (receipt.discountAmount > 0) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Discount", style = MaterialTheme.typography.bodyMedium, color = ErrorRed)
                                Text("-${uiState.currencyCode} ${formatAmount(receipt.discountAmount)}", color = ErrorRed)
                            }
                        }
                        if (receipt.taxAmount > 0) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Tax", style = MaterialTheme.typography.bodyMedium)
                                Text("${uiState.currencyCode} ${formatAmount(receipt.taxAmount)}")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Grand Total
                        Row(
                            Modifier.fillMaxWidth(),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (uiState.isDualCurrencyEnabled && uiState.exchangeRate > 0) {
                                    val secTotal = if (uiState.currencyCode == "KHR" && uiState.secondaryCurrencyCode == "USD") {
                                        receipt.totalAmount / uiState.exchangeRate
                                    } else {
                                        receipt.totalAmount * uiState.exchangeRate
                                    }
                                    Text(
                                        "≈ ${uiState.secondaryCurrencyCode} ${formatAmount(secTotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WarningAmber
                                    )
                                }
                            }
                            Text(
                                "${uiState.currencyCode} ${formatAmount(receipt.totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Green500
                            )
                        }

                        if (receipt.paymentMethod == "CASH" && receipt.cashTendered > 0) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Cash Tendered", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${uiState.currencyCode} ${formatAmount(receipt.cashTendered)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Change", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                                Text("${uiState.currencyCode} ${formatAmount(receipt.changeGiven)}", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Paper Preview Dialog
    if (showPreviewDialog && uiState.receipt != null) {
        ReceiptPreviewDialog(
            receipt = uiState.receipt!!,
            lines = uiState.lines,
            businessName = uiState.businessName,
            headerText = uiState.headerText,
            footerText = uiState.footerText,
            currencyCode = uiState.currencyCode,
            isDualCurrencyEnabled = uiState.isDualCurrencyEnabled,
            secondaryCurrencyCode = uiState.secondaryCurrencyCode,
            exchangeRate = uiState.exchangeRate,
            logoUri = uiState.receiptLogoUri,
            showLogo = uiState.showReceiptLogo,
            onDismiss = { showPreviewDialog = false },
            onPrint = {
                viewModel.printReceipt()
                showPreviewDialog = false
            }
        )
    }

    // Refund Dialog
    if (showRefundDialog) {
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            icon = { Icon(Icons.Filled.Undo, null, tint = ErrorRed) },
            title = { Text("Refund Receipt") },
            text = { Text("Are you sure you want to refund receipt $receiptNumber? This will restore stock quantities.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.refundReceipt()
                        showRefundDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Refund") }
            },
            dismissButton = {
                TextButton(onClick = { showRefundDialog = false }) { Text("Cancel") }
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
