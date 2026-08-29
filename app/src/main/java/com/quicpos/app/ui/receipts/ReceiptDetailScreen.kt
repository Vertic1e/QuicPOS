package com.quicpos.app.ui.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    fun refundFullReceipt() {
        viewModelScope.launch {
            receiptRepository.refundReceipt(receiptNumber)
            loadReceipt()
            _uiState.update { it.copy(printMessage = "Receipt full refund completed and stock restored.") }
        }
    }

    fun refundPartialReceipt(refundedItems: Map<Long, Double>) {
        viewModelScope.launch {
            receiptRepository.refundPartialReceipt(receiptNumber, refundedItems)
            loadReceipt()
            _uiState.update { it.copy(printMessage = "Item refund processed successfully.") }
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
                    printMessage = if (result.isSuccess) "Print sent successfully" else "Print failed: ${result.exceptionOrNull()?.message}"
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
                    IconButton(onClick = { showPreviewDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = "Preview",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.printReceipt() },
                        enabled = !uiState.isPrinting
                    ) {
                        if (uiState.isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Print,
                                contentDescription = "Print",
                                tint = Green500
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val receipt = uiState.receipt
            if (receipt == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Receipt not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Badge Banner
                    if (receipt.isRefunded || receipt.status == "PARTIALLY_REFUNDED") {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (receipt.isRefunded) MaterialTheme.colorScheme.errorContainer else WarningAmber.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Undo,
                                    contentDescription = null,
                                    tint = if (receipt.isRefunded) MaterialTheme.colorScheme.error else WarningAmber
                                )
                                Column {
                                    Text(
                                        text = if (receipt.isRefunded) "This receipt was fully refunded" else "This receipt has been partially refunded",
                                        fontWeight = FontWeight.Bold,
                                        color = if (receipt.isRefunded) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Item stock quantities have been restored to inventory.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Total Amount Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Paid",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.currencyCode} ${formatAmount(receipt.totalAmount)}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (receipt.isRefunded) MaterialTheme.colorScheme.error else Green500
                            )
                            if (uiState.isDualCurrencyEnabled && uiState.exchangeRate > 0) {
                                val secAmount = if (uiState.currencyCode == "KHR" && uiState.secondaryCurrencyCode == "USD") {
                                    receipt.totalAmount / uiState.exchangeRate
                                } else {
                                    receipt.totalAmount * uiState.exchangeRate
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "≈ ${uiState.secondaryCurrencyCode} ${formatAmount(secAmount)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningAmber
                                )
                            }
                        }
                    }

                    // Receipt Info Card
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
                        text = "Purchased Items",
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.lines.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = line.itemName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${line.displayQuantity} × ${uiState.currencyCode} ${formatAmount(line.unitPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (line.discountAmount > 0) {
                                            Text(
                                                text = "Discount: -${uiState.currencyCode} ${formatAmount(line.discountAmount)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Green500
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${uiState.currencyCode} ${formatAmount(line.calculatedLineTotal)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (line != uiState.lines.last()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }

                    // Totals Summary
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
                                Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${uiState.currencyCode} ${formatAmount(receipt.subtotalAmount)}")
                            }
                            if (receipt.discountAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("-${uiState.currencyCode} ${formatAmount(receipt.discountAmount)}", color = Green500)
                                }
                            }
                            if (receipt.taxAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tax", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${uiState.currencyCode} ${formatAmount(receipt.taxAmount)}")
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${uiState.currencyCode} ${formatAmount(receipt.totalAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (receipt.isRefunded) MaterialTheme.colorScheme.error else Green500
                                )
                            }
                        }
                    }

                    // Refund Button (Active if not already fully refunded)
                    if (!receipt.isRefunded) {
                        Button(
                            onClick = { showRefundDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Undo, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Process Refund (Full or Partial Items)")
                        }
                    }
                }
            }
        }
    }

    // Receipt Preview Dialog
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

    // Refund Modal with Full or Partial Item Options
    if (showRefundDialog && uiState.lines.isNotEmpty()) {
        RefundOptionDialog(
            lines = uiState.lines,
            currencyCode = uiState.currencyCode,
            totalAmount = uiState.receipt?.totalAmount ?: 0.0,
            onDismiss = { showRefundDialog = false },
            onFullRefund = {
                viewModel.refundFullReceipt()
                showRefundDialog = false
            },
            onPartialRefund = { map ->
                viewModel.refundPartialReceipt(map)
                showRefundDialog = false
            }
        )
    }
}

@Composable
private fun RefundOptionDialog(
    lines: List<TicketLine>,
    currencyCode: String,
    totalAmount: Double,
    onDismiss: () -> Unit,
    onFullRefund: () -> Unit,
    onPartialRefund: (Map<Long, Double>) -> Unit
) {
    var isPartialMode by remember { mutableStateOf(false) }
    // Map of itemId to quantity to refund
    val refundQuantities = remember {
        mutableStateMapOf<Long, Double>().apply {
            lines.forEach { this[it.itemId] = 0.0 }
        }
    }

    val selectedRefundTotal = lines.sumOf { line ->
        (refundQuantities[line.itemId] ?: 0.0) * line.unitPrice
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Undo, contentDescription = null, tint = ErrorRed)
                Text(if (isPartialMode) "Select Items to Refund" else "Refund Receipt")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!isPartialMode) {
                    Text(
                        text = "Choose refund type for this transaction. Refunding will return items to inventory stock.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFullRefund() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.RestartAlt, null, tint = ErrorRed)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Full Refund", fontWeight = FontWeight.Bold, color = ErrorRed)
                                Text("Refund entire $currencyCode ${formatAmount(totalAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = ErrorRed)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPartialMode = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Checklist, null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Partial Item Refund", fontWeight = FontWeight.Bold)
                                Text("Pick specific items and quantities to refund", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // Partial mode item list with +/- steppers
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(lines) { line ->
                            val currentRefundQty = refundQuantities[line.itemId] ?: 0.0
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentRefundQty > 0) Green500.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(line.itemName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(
                                            "Sold: ${line.displayQuantity} @ $currencyCode ${formatAmount(line.unitPrice)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Stepper
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (currentRefundQty > 0) {
                                                    refundQuantities[line.itemId] = currentRefundQty - 1.0
                                                }
                                            },
                                            enabled = currentRefundQty > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Remove, null, modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = if (currentRefundQty == currentRefundQty.toLong().toDouble()) currentRefundQty.toLong().toString() else currentRefundQty.toString(),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (currentRefundQty < line.quantity) {
                                                    refundQuantities[line.itemId] = currentRefundQty + 1.0
                                                }
                                            },
                                            enabled = currentRefundQty < line.quantity,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Refund Sum:", fontWeight = FontWeight.Bold)
                        Text(
                            "$currencyCode ${formatAmount(selectedRefundTotal)}",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRefundTotal > 0) ErrorRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isPartialMode) {
                Button(
                    onClick = {
                        val toRefund = refundQuantities.filter { it.value > 0 }
                        onPartialRefund(toRefund)
                    },
                    enabled = selectedRefundTotal > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Confirm Item Refund")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isPartialMode) isPartialMode = false else onDismiss()
            }) {
                Text(if (isPartialMode) "Back" else "Cancel")
            }
        }
    )
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        String.format("%,.0f", amount)
    } else {
        String.format("%,.2f", amount)
    }
}
