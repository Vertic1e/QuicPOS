package com.quicpos.app.ui.backoffice

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicpos.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BackOfficeScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BackOfficeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!uiState.isAuthenticated) {
        PinAuthScreen(
            enteredPin = uiState.enteredPin,
            errorText = uiState.pinError,
            onDigit = { viewModel.onPinDigit(it) },
            onDelete = { viewModel.onPinDelete() },
            onClear = { viewModel.onPinClear() }
        )
    } else {
        BackOfficeDashboard(
            uiState = uiState,
            onPrevDate = { viewModel.changeDate(-1) },
            onNextDate = { viewModel.changeDate(1) },
            onSelectHour = { viewModel.selectHourTooltip(it) },
            onLock = { viewModel.lockBackOffice() },
            onChangePinClick = { viewModel.setChangePinDialogOpen(true) }
        )
    }

    if (uiState.isChangePinDialogOpen) {
        ChangePinDialog(
            onDismiss = { viewModel.setChangePinDialogOpen(false) },
            onSave = { viewModel.updatePin(it) }
        )
    }
}

// ─── PIN Auth Screen ───

@Composable
private fun PinAuthScreen(
    enteredPin: String,
    errorText: String?,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon & Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lock",
                tint = Green500,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Back Office Access",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Enter 6-digit Owner PIN",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 6 PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) Green500 else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (filled) Green500 else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Error message
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Numeric Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "DEL")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    row.forEach { key ->
                        KeypadButton(
                            label = key,
                            onClick = {
                                when (key) {
                                    "C" -> onClear()
                                    "DEL" -> onDelete()
                                    else -> onDigit(key)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val isSpecial = label == "C" || label == "DEL"

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(if (isSpecial) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == "DEL") {
            Icon(
                imageVector = Icons.Filled.Backspace,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSpecial) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── Dashboard Screen (matching user image) ───

@Composable
private fun BackOfficeDashboard(
    uiState: BackOfficeUiState,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    onSelectHour: (HourlySale?) -> Unit,
    onLock: () -> Unit,
    onChangePinClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Date Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevDate) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Previous Day",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = uiState.formattedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row {
                IconButton(onClick = onNextDate) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Next Day",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onChangePinClick) {
                    Icon(
                        imageVector = Icons.Filled.Pin,
                        contentDescription = "Change PIN",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onLock) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ─── Sales Summary Card ───
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Sales summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Three Circular Arc Gauges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Receipts (Orange)
                    ArcGaugeItem(
                        value = "${uiState.receiptsCount}",
                        label = "Receipts",
                        diffPercent = uiState.receiptsDiffPercent,
                        arcColor = WarningAmber,
                        progress = if (uiState.receiptsCount > 0) 0.6f else 0.05f,
                        size = 85.dp
                    )

                    // 2. Net Sales (Green, larger)
                    ArcGaugeItem(
                        value = "${uiState.currencyCode} ${uiState.formatShortAmount(uiState.netSales)}",
                        label = "Net sales",
                        diffPercent = uiState.netSalesDiffPercent,
                        arcColor = SuccessGreen,
                        progress = if (uiState.netSales > 0) 0.75f else 0.05f,
                        size = 110.dp
                    )

                    // 3. Average sale (Blue)
                    ArcGaugeItem(
                        value = "${uiState.currencyCode} ${uiState.formatShortAmount(uiState.averageSale)}",
                        label = "Average sale",
                        diffPercent = uiState.averageSaleDiffPercent,
                        arcColor = InfoBlue,
                        progress = if (uiState.averageSale > 0) 0.6f else 0.05f,
                        size = 85.dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hourly Sales Bar Chart
                HourlySalesChart(
                    hourlySales = uiState.hourlySales,
                    maxSale = uiState.maxHourlySale,
                    currencyCode = uiState.currencyCode,
                    selectedTooltip = uiState.selectedHourTooltip,
                    onSelectHour = onSelectHour
                )
            }
        }

        // ─── Items Card ───
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.itemsSold.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items sold on this day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    uiState.itemsSold.forEachIndexed { index, item ->
                        ItemSoldRow(
                            item = item,
                            currencyCode = uiState.currencyCode
                        )
                        if (index < uiState.itemsSold.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── Arc Gauge Component ───

@Composable
private fun ArcGaugeItem(
    value: String,
    label: String,
    diffPercent: Double,
    arcColor: Color,
    progress: Float,
    size: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val strokeWidth = 4.dp.toPx()
                val radius = (this.size.minDimension - strokeWidth) / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)

                // Background track arc (from 135 deg to 405 deg = 270 deg span)
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.35f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Foreground active arc
                val sweep = 270f * progress.coerceIn(0f, 1f)
                drawArc(
                    color = arcColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Marker dot at the tip of progress arc
                val endAngleRad = Math.toRadians((135f + sweep).toDouble())
                val dotX = (center.x + radius * cos(endAngleRad)).toFloat()
                val dotY = (center.y + radius * sin(endAngleRad)).toFloat()
                drawCircle(
                    color = arcColor,
                    radius = strokeWidth * 1.2f,
                    center = Offset(dotX, dotY)
                )
            }

            // Value text inside arc
            Text(
                text = value,
                style = if (size > 90.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        // Percentage Difference
        val diffFormatted = if (diffPercent >= 0) "+${String.format("%.2f", diffPercent)}%" else "${String.format("%.2f", diffPercent)}%"
        val diffColor = if (diffPercent < 0) ErrorRed else if (diffPercent > 0) SuccessGreen else WarningAmber

        Text(
            text = if (diffPercent == 0.0) "0%" else diffFormatted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = diffColor
        )
    }
}

// ─── Hourly Sales Chart ───

@Composable
private fun HourlySalesChart(
    hourlySales: List<HourlySale>,
    maxSale: Double,
    currencyCode: String,
    selectedTooltip: HourlySale?,
    onSelectHour: (HourlySale?) -> Unit
) {
    val effectiveMax = if (maxSale <= 0) 100_000.0 else maxSale * 1.2
    val step = effectiveMax / 5.0
    val yLevels = listOf(effectiveMax, step * 4, step * 3, step * 2, step, 0.0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Y-Axis Grid Lines and Labels
            Canvas(modifier = Modifier.fillMaxSize()) {
                val leftLabelWidth = 70.dp.toPx()
                val chartWidth = size.width - leftLabelWidth
                val chartHeight = size.height - 20.dp.toPx()

                // Draw horizontal gridlines
                for (i in 0..5) {
                    val y = (chartHeight / 5f) * i
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(leftLabelWidth, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw green vertical bars
                val barCount = 24
                val barSpacing = chartWidth / barCount
                val barWidth = (barSpacing * 0.7f).coerceAtLeast(3.dp.toPx())

                hourlySales.forEachIndexed { index, sale ->
                    if (sale.amount > 0) {
                        val barHeight = ((sale.amount / effectiveMax) * chartHeight).toFloat()
                        val barX = leftLabelWidth + index * barSpacing + (barSpacing - barWidth) / 2f
                        val barY = chartHeight - barHeight

                        drawRoundRect(
                            color = Green500,
                            topLeft = Offset(barX, barY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }

            // Y-Axis Text Overlay
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yLevels.take(6).forEach { level ->
                    Text(
                        text = "$currencyCode ${formatShortNumber(level)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.width(65.dp)
                    )
                }
            }

            // Interactive touch detection overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val leftWidth = 70.dp.toPx()
                            val chartWidth = size.width - leftWidth
                            if (offset.x >= leftWidth) {
                                val hourIndex = ((offset.x - leftWidth) / (chartWidth / 24)).toInt().coerceIn(0, 23)
                                val sale = hourlySales.getOrNull(hourIndex)
                                onSelectHour(sale)
                            }
                        }
                    }
            )

            // Tooltip Overlay (matching screenshot format: e.g. "18:00\nKHR 193,475")
            if (selectedTooltip != null && selectedTooltip.amount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%02d:00", selectedTooltip.hour),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$currencyCode ${String.format("%,.0f", selectedTooltip.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Green500
                        )
                    }
                }
            }
        }

        // X-Axis Labels (0:00, 10:00, 20:00)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 70.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "10:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "20:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatShortNumber(value: Double): String {
    return if (value >= 1_000_000) {
        String.format("%.0fM", value / 1_000_000.0)
    } else if (value >= 1_000) {
        String.format("%.0fK", value / 1_000.0)
    } else {
        String.format("%.0f", value)
    }
}

// ─── Item Sold Row ───

@Composable
private fun ItemSoldRow(
    item: ItemSaleSummary,
    currencyCode: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Circular avatar placeholder (matching screenshot gray circle)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.itemName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Item Name & Quantity
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "x ${if (item.quantity == item.quantity.toLong().toDouble()) item.quantity.toLong().toString() else String.format("%.1f", item.quantity)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Total Amount
        Text(
            text = "$currencyCode ${String.format("%,.0f", item.totalAmount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── Change PIN Dialog ───

@Composable
private fun ChangePinDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Owner PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter a new 6-digit numeric PIN:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("New PIN (6 digits)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (newPin.length == 6) onSave(newPin) },
                enabled = newPin.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = Green500)
            ) {
                Text("Update PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
