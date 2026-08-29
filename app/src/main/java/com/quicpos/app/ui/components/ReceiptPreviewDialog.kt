package com.quicpos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.domain.model.TicketLine
import com.quicpos.app.ui.theme.Green500

@Composable
fun ReceiptPreviewDialog(
    receipt: Receipt,
    lines: List<TicketLine>,
    businessName: String = "QuicPOS Store",
    headerText: String = "Thank you for shopping!",
    footerText: String = "Please come again",
    currencyCode: String = "KHR",
    isDualCurrencyEnabled: Boolean = false,
    secondaryCurrencyCode: String = "USD",
    exchangeRate: Double = 4000.0,
    logoUri: String = "",
    showLogo: Boolean = false,
    onDismiss: () -> Unit,
    onPrint: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Receipt Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Paper Scrollable Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ReceiptPaperView(
                        receipt = receipt,
                        lines = lines,
                        businessName = businessName,
                        headerText = headerText,
                        footerText = footerText,
                        currencyCode = currencyCode,
                        isDualCurrencyEnabled = isDualCurrencyEnabled,
                        secondaryCurrencyCode = secondaryCurrencyCode,
                        exchangeRate = exchangeRate,
                        logoUri = logoUri,
                        showLogo = showLogo,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Actions Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }

                    if (onPrint != null) {
                        Button(
                            onClick = onPrint,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green500)
                        ) {
                            Icon(Icons.Filled.Print, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Print")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptPaperView(
    receipt: Receipt,
    lines: List<TicketLine>,
    businessName: String,
    headerText: String,
    footerText: String,
    currencyCode: String,
    isDualCurrencyEnabled: Boolean,
    secondaryCurrencyCode: String,
    exchangeRate: Double,
    logoUri: String = "",
    showLogo: Boolean = false,
    modifier: Modifier = Modifier
) {
    val paperBg = Color(0xFFFAF8F5) // Warm thermal paper off-white
    val inkColor = Color(0xFF1E1E1E) // Deep ink black
    val mutedInk = Color(0xFF555555)

    fun formatPrimary(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            String.format("%,.0f", amount)
        } else {
            String.format("%,.2f", amount)
        }
    }

    fun formatSecondary(amountPrimary: Double): String {
        if (exchangeRate <= 0) return ""
        val secondaryAmount = if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
            amountPrimary / exchangeRate
        } else if (currencyCode == "USD" && secondaryCurrencyCode == "KHR") {
            amountPrimary * exchangeRate
        } else {
            amountPrimary / exchangeRate
        }
        val formatted = if (secondaryCurrencyCode == "KHR") {
            String.format("%,.0f", secondaryAmount)
        } else {
            String.format("%,.2f", secondaryAmount)
        }
        return "$secondaryCurrencyCode $formatted"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 380.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = paperBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Logo
            if (showLogo && logoUri.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = logoUri,
                    contentDescription = "Receipt Logo",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier
                        .heightIn(max = 56.dp)
                        .widthIn(max = 130.dp)
                        .padding(bottom = 4.dp)
                )
            }

            // Store Name
            Text(
                text = businessName.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = inkColor,
                textAlign = TextAlign.Center
            )

            // Header Greeting
            if (headerText.isNotBlank()) {
                Text(
                    text = headerText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = mutedInk,
                    textAlign = TextAlign.Center
                )
            }

            ReceiptDashedDivider(color = mutedInk)

            // Receipt Metadata
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                ReceiptRow("Receipt #:", receipt.receiptNumber, inkColor)
                ReceiptRow("Date:", receipt.formattedDateTime, inkColor)
                ReceiptRow("Register:", receipt.posRegister, inkColor)
                receipt.customerName?.let {
                    ReceiptRow("Customer:", it, inkColor)
                }
                ReceiptRow("Status:", receipt.status, if (receipt.isRefunded) Color.Red else inkColor)
            }

            ReceiptDashedDivider(color = mutedInk)

            // Item Lines Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("QTY  ITEM", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = inkColor)
                Text("TOTAL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = inkColor)
            }

            HorizontalDivider(color = mutedInk.copy(alpha = 0.5f), thickness = 1.dp)

            // Item Lines
            lines.forEach { line ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${line.displayQuantity}x ${line.itemName}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = inkColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$currencyCode ${formatPrimary(line.calculatedLineTotal)}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = inkColor
                        )
                    }

                    // Modifier or unit price
                    if (line.quantity > 1 || line.modifiers.isNotEmpty()) {
                        val subInfo = buildString {
                            if (line.quantity > 1) append("@ $currencyCode ${formatPrimary(line.effectiveUnitPrice)}")
                            if (line.modifiers.isNotEmpty()) {
                                if (isNotEmpty()) append(" • ")
                                append(line.modifiers.joinToString { it.name })
                            }
                        }
                        Text(
                            text = "   $subInfo",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = mutedInk
                        )
                    }

                    if (line.discountAmount > 0) {
                        Text(
                            text = "   Disc: -$currencyCode ${formatPrimary(line.discountAmount)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            ReceiptDashedDivider(color = mutedInk)

            // Totals Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ReceiptRow("Subtotal:", "$currencyCode ${formatPrimary(receipt.subtotalAmount)}", inkColor)

                if (receipt.discountAmount > 0) {
                    ReceiptRow("Discount:", "-$currencyCode ${formatPrimary(receipt.discountAmount)}", Color(0xFFC62828))
                }

                if (receipt.taxAmount > 0) {
                    ReceiptRow("Tax:", "$currencyCode ${formatPrimary(receipt.taxAmount)}", inkColor)
                }

                HorizontalDivider(color = inkColor, thickness = 1.5.dp, modifier = Modifier.padding(vertical = 2.dp))

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL:",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = inkColor
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$currencyCode ${formatPrimary(receipt.totalAmount)}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = inkColor
                        )
                        if (isDualCurrencyEnabled) {
                            Text(
                                text = "(${formatSecondary(receipt.totalAmount)})",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = mutedInk
                            )
                        }
                    }
                }

                HorizontalDivider(color = inkColor, thickness = 1.5.dp, modifier = Modifier.padding(vertical = 2.dp))

                // Payment Info
                ReceiptRow("Payment Method:", receipt.paymentMethod, inkColor)

                if (receipt.paymentMethod == "CASH" && receipt.cashTendered > 0) {
                    ReceiptRow("Cash Tendered:", "$currencyCode ${formatPrimary(receipt.cashTendered)}", inkColor)
                    if (isDualCurrencyEnabled) {
                        ReceiptRow(" ", "(${formatSecondary(receipt.cashTendered)})", mutedInk)
                    }

                    ReceiptRow("Change Given:", "$currencyCode ${formatPrimary(receipt.changeGiven)}", inkColor)
                    if (isDualCurrencyEnabled && receipt.changeGiven > 0) {
                        ReceiptRow(" ", "(${formatSecondary(receipt.changeGiven)})", mutedInk)
                    }
                }
            }

            ReceiptDashedDivider(color = mutedInk)

            // Simulated Barcode
            SimulatedBarcode(number = receipt.receiptNumber, color = inkColor)

            // Footer Message
            if (footerText.isNotBlank()) {
                Text(
                    text = footerText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = mutedInk,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "POWERED BY QUICPOS",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = mutedInk.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = color)
        Text(text = value, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun ReceiptDashedDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = color.copy(alpha = 0.5f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    strokeWidth = 2f
                )
            }
    )
}

@Composable
private fun SimulatedBarcode(number: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val widths = listOf(2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 1, 2, 4, 1, 2, 3, 1, 4, 2, 1, 3)
            widths.forEach { w ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(w.dp)
                        .background(color)
                )
            }
        }
        Text(
            text = number,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = color,
            letterSpacing = 2.sp
        )
    }
}
