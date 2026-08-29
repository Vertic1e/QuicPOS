package com.quicpos.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicpos.app.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateToPrinters: () -> Unit,
    onNavigateToTaxes: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showBusinessNameDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showSecondaryCurrencyDialog by remember { mutableStateOf(false) }
    var showExchangeRateDialog by remember { mutableStateOf(false) }
    var showHeaderDialog by remember { mutableStateOf(false) }
    var showFooterDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Business Section
        SettingsSection("Business") {
            SettingsClickItem(
                icon = Icons.Filled.Store,
                title = "Business Name",
                subtitle = settings.businessName,
                onClick = { showBusinessNameDialog = true }
            )
            SettingsClickItem(
                icon = Icons.Filled.PointOfSale,
                title = "POS Register Name",
                subtitle = settings.posRegisterName,
                onClick = { showRegisterDialog = true }
            )
            SettingsClickItem(
                icon = Icons.Filled.AttachMoney,
                title = "Primary Currency",
                subtitle = "${settings.currencyCode} (${settings.currencySymbol})",
                onClick = { showCurrencyDialog = true }
            )
        }

        // Dual Currency Section
        SettingsSection("Dual Currency") {
            SettingsSwitchItem(
                icon = Icons.Filled.CurrencyExchange,
                title = "Dual Currency Mode",
                subtitle = if (settings.isDualCurrencyEnabled) "Enabled - displays prices & totals in both currencies" else "Disabled",
                checked = settings.isDualCurrencyEnabled,
                onCheckedChange = { viewModel.setDualCurrencyEnabled(it) }
            )
            if (settings.isDualCurrencyEnabled) {
                SettingsClickItem(
                    icon = Icons.Filled.Payments,
                    title = "Secondary Currency",
                    subtitle = "${settings.secondaryCurrencyCode} (${settings.secondaryCurrencySymbol})",
                    onClick = { showSecondaryCurrencyDialog = true }
                )
                val rateDisplay = if (settings.currencyCode == "KHR" && settings.secondaryCurrencyCode == "USD") {
                    "1 USD = ${String.format("%,.0f", settings.exchangeRate)} KHR"
                } else {
                    "1 ${settings.currencyCode} = ${settings.exchangeRate} ${settings.secondaryCurrencyCode}"
                }
                SettingsClickItem(
                    icon = Icons.Filled.SwapHoriz,
                    title = "Exchange Rate",
                    subtitle = rateDisplay,
                    onClick = { showExchangeRateDialog = true }
                )
            }
        }

        // Appearance Section
        SettingsSection("Appearance") {
            SettingsClickItem(
                icon = Icons.Filled.Palette,
                title = "Theme",
                subtitle = AppTheme.fromString(settings.theme).displayName,
                onClick = { showThemeDialog = true }
            )
            SettingsClickItem(
                icon = Icons.Filled.GridView,
                title = "Home Screen Layout",
                subtitle = if (settings.isGridLayout) "Grid" else "List",
                onClick = {
                    viewModel.setLayoutMode(if (settings.isGridLayout) "LIST" else "GRID")
                }
            )
        }

        // Accessibility Section
        SettingsSection("Accessibility") {
            // Grid Columns Selector (2, 3, 4, Auto)
            GridColumnSelector(
                currentColumns = settings.gridColumns,
                onColumnsSelected = { viewModel.setGridColumns(it) }
            )

            // Item Size Selector
            ItemSizeSelector(
                currentSize = settings.itemSize,
                onSizeSelected = { viewModel.setItemSize(it) }
            )

            // Font Size Slider (80% - 180%)
            FontSizeSlider(
                currentScale = settings.fontSizeScale,
                onScaleChanged = { viewModel.setFontSizeScale(it) }
            )
        }

        // Hardware Section
        SettingsSection("Hardware") {
            SettingsSwitchItem(
                icon = Icons.Filled.QrCodeScanner,
                title = "Use camera to scan barcodes",
                subtitle = "Use device camera for barcode scanning",
                checked = settings.useBarcodeSanner,
                onCheckedChange = { viewModel.setBarcodeScannerEnabled(it) }
            )
            SettingsClickItem(
                icon = Icons.Filled.Print,
                title = "Printers",
                subtitle = if (settings.isPrinterConfigured)
                    "${settings.printerType} - ${settings.printerAddress}"
                else "Not configured",
                onClick = onNavigateToPrinters
            )
        }

        // Tax & Receipt Section
        SettingsSection("Tax & Receipt") {
            SettingsClickItem(
                icon = Icons.Filled.Receipt,
                title = "Taxes & Surcharges",
                subtitle = "Configure tax rates",
                onClick = onNavigateToTaxes
            )
            SettingsClickItem(
                icon = Icons.Filled.Description,
                title = "Receipt Header",
                subtitle = settings.receiptHeader,
                onClick = { showHeaderDialog = true }
            )
            SettingsClickItem(
                icon = Icons.Filled.Description,
                title = "Receipt Footer",
                subtitle = settings.receiptFooter,
                onClick = { showFooterDialog = true }
            )
        }

        // Language Section
        SettingsSection("Language") {
            SettingsClickItem(
                icon = Icons.Filled.Language,
                title = "Language",
                subtitle = when (settings.language) {
                    "en" -> "English"
                    "km" -> "ខ្មែរ (Khmer)"
                    else -> settings.language
                },
                onClick = {
                    viewModel.setLanguage(if (settings.language == "en") "km" else "en")
                }
            )
        }

        // About Section
        SettingsSection("About") {
            SettingsClickItem(
                icon = Icons.Filled.Info,
                title = "Version",
                subtitle = "QuicPOS v1.0.0",
                onClick = { }
            )
            SettingsClickItem(
                icon = Icons.Filled.HelpCenter,
                title = "Support",
                subtitle = "Get help and documentation",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ─── Dialogs ───

    if (showBusinessNameDialog) {
        TextInputDialog(
            title = "Business Name",
            initialValue = settings.businessName,
            onDismiss = { showBusinessNameDialog = false },
            onSave = {
                viewModel.setBusinessName(it)
                showBusinessNameDialog = false
            }
        )
    }

    if (showRegisterDialog) {
        TextInputDialog(
            title = "POS Register Name",
            initialValue = settings.posRegisterName,
            onDismiss = { showRegisterDialog = false },
            onSave = {
                viewModel.setPosRegisterName(it)
                showRegisterDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencyDialog(
            title = "Select Primary Currency",
            currentCode = settings.currencyCode,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                viewModel.setCurrencyCode(it)
                showCurrencyDialog = false
            }
        )
    }

    if (showSecondaryCurrencyDialog) {
        CurrencyDialog(
            title = "Select Secondary Currency",
            currentCode = settings.secondaryCurrencyCode,
            onDismiss = { showSecondaryCurrencyDialog = false },
            onSelect = {
                viewModel.setSecondaryCurrency(it)
                showSecondaryCurrencyDialog = false
            }
        )
    }

    if (showExchangeRateDialog) {
        ExchangeRateDialog(
            primaryCurrency = settings.currencyCode,
            secondaryCurrency = settings.secondaryCurrencyCode,
            currentRate = settings.exchangeRate,
            onDismiss = { showExchangeRateDialog = false },
            onSave = {
                viewModel.setExchangeRate(it)
                showExchangeRateDialog = false
            }
        )
    }

    if (showHeaderDialog) {
        TextInputDialog(
            title = "Receipt Header",
            initialValue = settings.receiptHeader,
            onDismiss = { showHeaderDialog = false },
            onSave = {
                viewModel.setReceiptHeader(it)
                showHeaderDialog = false
            }
        )
    }

    if (showFooterDialog) {
        TextInputDialog(
            title = "Receipt Footer",
            initialValue = settings.receiptFooter,
            onDismiss = { showFooterDialog = false },
            onSave = {
                viewModel.setReceiptFooter(it)
                showFooterDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            currentTheme = settings.theme,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                viewModel.setTheme(it)
                showThemeDialog = false
            }
        )
    }
}

// ─── Grid Columns Selector ───

@Composable
private fun GridColumnSelector(
    currentColumns: Int,
    onColumnsSelected: (Int) -> Unit
) {
    val options = listOf(
        2 to "2 Cols",
        3 to "3 Cols",
        4 to "4 Cols",
        0 to "Auto"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.GridView,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Sales Grid Columns", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Choose 2 columns to enlarge item boxes and text",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (cols, label) ->
                    val selected = currentColumns == cols
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) Green500 else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Green500 else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onColumnsSelected(cols) }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Item Size Selector ───

@Composable
private fun ItemSizeSelector(
    currentSize: String,
    onSizeSelected: (String) -> Unit
) {
    val sizes = listOf("SMALL" to "S", "MEDIUM" to "M", "LARGE" to "L")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.ViewComfy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Tile Sizing Preset", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Adjust padding & corner style",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sizes.forEach { (value, label) ->
                    val selected = currentSize == value
                    val sizeVal = when (value) {
                        "SMALL" -> 36.dp
                        "MEDIUM" -> 44.dp
                        else -> 52.dp
                    }
                    Box(
                        modifier = Modifier
                            .size(sizeVal)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) Green500 else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Green500 else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSizeSelected(value) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Font Size Slider ───

@Composable
private fun FontSizeSlider(
    currentScale: Float,
    onScaleChanged: (Float) -> Unit
) {
    var sliderValue by remember(currentScale) { mutableFloatStateOf(currentScale) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.TextFields,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Font Size Scale", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${String.format("%.0f", sliderValue * 100)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green500,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "Enlarge font as big as the item box",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onScaleChanged(sliderValue) },
                    valueRange = 0.8f..1.8f,
                    steps = 10,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Green500,
                        activeTrackColor = Green500
                    )
                )
                Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Live preview
            Text(
                text = "Item Name Preview Text",
                fontSize = (15 * sliderValue).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ─── Theme Picker Dialog ───

@Composable
private fun ThemePickerDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    data class ThemeOption(
        val key: String,
        val name: String,
        val bgColor: Color,
        val surfColor: Color,
        val accentColor: Color
    )

    val themes = listOf(
        ThemeOption("NIGHT_BLUE", "Night Blue", NightBlue950, NightBlue800, NightBlueAccent),
        ThemeOption("DARK_GREEN", "Dark Green", DarkBackground, DarkSurfaceVariant, Green500),
        ThemeOption("LIGHT", "Light", LightBackground, LightSurface, Green800),
        ThemeOption("AMOLED_BLACK", "AMOLED Black", AmoledBlack, AmoledSurfaceVariant, Green500)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                themes.forEach { option ->
                    val selected = currentTheme == option.key
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.key) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (selected) androidx.compose.foundation.BorderStroke(
                            2.dp, Green500
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Color preview swatch
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .width(56.dp)
                                    .height(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(option.bgColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(option.surfColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(option.accentColor)
                                )
                            }
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check, null,
                                    tint = Green500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// ─── Reusable Components ───

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsClickItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight, null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Green500)
        )
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { if (value.isNotBlank()) onSave(value) },
                colors = ButtonDefaults.buttonColors(containerColor = Green500)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CurrencyDialog(
    title: String = "Select Currency",
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currencies = listOf(
        "KHR" to "Cambodian Riel (៛)",
        "USD" to "US Dollar ($)",
        "THB" to "Thai Baht (฿)",
        "VND" to "Vietnamese Dong (₫)",
        "EUR" to "Euro (€)",
        "GBP" to "British Pound (£)",
        "JPY" to "Japanese Yen (¥)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                currencies.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == currentCode,
                            onClick = { onSelect(code) },
                            colors = RadioButtonDefaults.colors(selectedColor = Green500)
                        )
                        Column {
                            Text(code, fontWeight = FontWeight.Bold)
                            Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ExchangeRateDialog(
    primaryCurrency: String,
    secondaryCurrency: String,
    currentRate: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var rateText by remember { mutableStateOf(if (currentRate == currentRate.toLong().toDouble()) currentRate.toLong().toString() else currentRate.toString()) }

    val rateValue = rateText.toDoubleOrNull() ?: 0.0
    val explanation = if (primaryCurrency == "KHR" && secondaryCurrency == "USD") {
        "1 USD = ${if (rateValue > 0) String.format("%,.0f", rateValue) else "..."} KHR"
    } else {
        "1 $primaryCurrency = ${if (rateValue > 0) rateValue.toString() else "..."} $secondaryCurrency"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Exchange Rate") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Specify the conversion rate between $primaryCurrency and $secondaryCurrency.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Exchange Rate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("Rate: ") }
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Green500,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rateValue > 0) {
                        onSave(rateValue)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green500),
                enabled = rateValue > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
