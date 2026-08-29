package com.quicpos.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.printing.DiscoveredPrinter
import com.quicpos.app.printing.PrinterManager
import com.quicpos.app.ui.components.SimpleTopBar
import com.quicpos.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrinterSetupState(
    val printerName: String = "Main Thermal Printer",
    val printerModel: String = "Generic ESC/POS",
    val printerType: String = "BUILT_IN",
    val printerAddress: String = "",
    val printerPort: String = "9100",
    val paperWidth: String = "58mm",
    val printMode: String = "STANDARD",
    val escInitCmd: String = "1B40",
    val escCutCmd: String = "1D5601",
    val escDrawerCmd: String = "1B700019FF",
    val receiptHeader: String = "",
    val receiptFooter: String = "",
    val receiptLogoUri: String = "",
    val showReceiptLogo: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val isSearching: Boolean = false,
    val discoveredPrinters: List<DiscoveredPrinter> = emptyList()
)

@HiltViewModel
class PrinterSetupViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val printerManager: PrinterManager
) : ViewModel() {
    private val _state = MutableStateFlow(PrinterSetupState())
    val state: StateFlow<PrinterSetupState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = PrinterSetupState(
                printerName = settingsRepository.getPrinterName(),
                printerModel = settingsRepository.getPrinterModel(),
                printerType = settingsRepository.getPrinterType(),
                printerAddress = settingsRepository.getPrinterAddress(),
                printerPort = settingsRepository.getPrinterPort().toString(),
                paperWidth = settingsRepository.getPaperWidth(),
                printMode = settingsRepository.getPrintMode(),
                escInitCmd = settingsRepository.getEscInitCmd(),
                escCutCmd = settingsRepository.getEscCutCmd(),
                escDrawerCmd = settingsRepository.getEscDrawerCmd(),
                receiptHeader = settingsRepository.getReceiptHeader(),
                receiptFooter = settingsRepository.getReceiptFooter(),
                receiptLogoUri = settingsRepository.getReceiptLogoUri(),
                showReceiptLogo = settingsRepository.isReceiptLogoEnabled()
            )
        }
    }

    fun setPrinterName(name: String) {
        _state.update { it.copy(printerName = name) }
        viewModelScope.launch { settingsRepository.setPrinterName(name) }
    }

    fun setPrinterModel(model: String) {
        _state.update { it.copy(printerModel = model) }
        viewModelScope.launch { settingsRepository.setPrinterModel(model) }
    }

    fun setPrinterType(type: String) {
        _state.update { it.copy(printerType = type) }
        viewModelScope.launch { settingsRepository.setPrinterType(type) }
    }

    fun setPrinterAddress(address: String) {
        _state.update { it.copy(printerAddress = address) }
        viewModelScope.launch { settingsRepository.setPrinterAddress(address) }
    }

    fun setPrinterPort(port: String) {
        _state.update { it.copy(printerPort = port) }
        viewModelScope.launch { settingsRepository.setPrinterPort(port.toIntOrNull() ?: 9100) }
    }

    fun setPaperWidth(width: String) {
        _state.update { it.copy(paperWidth = width) }
        viewModelScope.launch { settingsRepository.setPaperWidth(width) }
    }

    fun setPrintMode(mode: String) {
        _state.update { it.copy(printMode = mode) }
        viewModelScope.launch { settingsRepository.setPrintMode(mode) }
    }

    fun setEscInitCmd(cmd: String) {
        _state.update { it.copy(escInitCmd = cmd) }
        viewModelScope.launch { settingsRepository.setEscInitCmd(cmd) }
    }

    fun setEscCutCmd(cmd: String) {
        _state.update { it.copy(escCutCmd = cmd) }
        viewModelScope.launch { settingsRepository.setEscCutCmd(cmd) }
    }

    fun setEscDrawerCmd(cmd: String) {
        _state.update { it.copy(escDrawerCmd = cmd) }
        viewModelScope.launch { settingsRepository.setEscDrawerCmd(cmd) }
    }

    fun setReceiptHeader(header: String) {
        _state.update { it.copy(receiptHeader = header) }
        viewModelScope.launch { settingsRepository.setReceiptHeader(header) }
    }

    fun setReceiptFooter(footer: String) {
        _state.update { it.copy(receiptFooter = footer) }
        viewModelScope.launch { settingsRepository.setReceiptFooter(footer) }
    }

    fun setReceiptLogoUri(uri: String) {
        _state.update { it.copy(receiptLogoUri = uri) }
        viewModelScope.launch { settingsRepository.setReceiptLogoUri(uri) }
    }

    fun setShowReceiptLogo(show: Boolean) {
        _state.update { it.copy(showReceiptLogo = show) }
        viewModelScope.launch { settingsRepository.setReceiptLogoEnabled(show) }
    }

    fun searchPrinters() {
        _state.update { it.copy(isSearching = true, discoveredPrinters = emptyList()) }
        viewModelScope.launch {
            val list = printerManager.searchPrinters(_state.value.printerType)
            _state.update { it.copy(isSearching = false, discoveredPrinters = list) }
        }
    }

    fun selectDiscoveredPrinter(printer: DiscoveredPrinter) {
        _state.update {
            it.copy(
                printerName = printer.name,
                printerAddress = printer.address,
                printerPort = printer.port.toString(),
                printerType = printer.type
            )
        }
        viewModelScope.launch {
            settingsRepository.setPrinterName(printer.name)
            settingsRepository.setPrinterAddress(printer.address)
            settingsRepository.setPrinterPort(printer.port)
            settingsRepository.setPrinterType(printer.type)
        }
    }

    fun testPrint() {
        _state.update { it.copy(isTesting = true, testResult = null) }
        viewModelScope.launch {
            val result = printerManager.testPrint()
            if (result.isSuccess) {
                _state.update { it.copy(isTesting = false, testResult = "Test print sent successfully!") }
            } else {
                _state.update { it.copy(isTesting = false, testResult = "Error: ${result.exceptionOrNull()?.message ?: "Print failed"}") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrinterSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSearchDialog by remember { mutableStateOf(false) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    val printerModels = listOf(
        "Generic ESC/POS",
        "Sunmi V2 / T2 POS Terminal",
        "Epson TM-T88 / TM-T20",
        "Star Micronics TSP100 / TSP650",
        "Xprinter XP-N160II / XP-80C",
        "Rongta RP80 / RPP02",
        "Goojprt PT-210 (Portable)"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setReceiptLogoUri(uri.toString())
            viewModel.setShowReceiptLogo(true)
        }
    }

    Scaffold(
        topBar = {
            SimpleTopBar(title = "Printer & Receipt Setup", onBackClick = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ─── Basic Printer Info ───
            Text(
                "Printer Profile",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = state.printerName,
                onValueChange = { viewModel.setPrinterName(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Printer Name") },
                placeholder = { Text("e.g. Counter Thermal Printer") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Label, contentDescription = null) }
            )

            // Printer Model Dropdown
            ExposedDropdownMenuBox(
                expanded = modelDropdownExpanded,
                onExpandedChange = { modelDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.printerModel,
                    onValueChange = { viewModel.setPrinterModel(it) },
                    readOnly = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Printer Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Filled.Print, contentDescription = null) }
                )
                ExposedDropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false }
                ) {
                    printerModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                viewModel.setPrinterModel(model)
                                modelDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // ─── Connection Type & Discovery ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Connection Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (state.printerType != "BUILT_IN") {
                    FilledTonalButton(
                        onClick = {
                            viewModel.searchPrinters()
                            showSearchDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Search, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Search Nearby", fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.printerType == "BUILT_IN",
                    onClick = { viewModel.setPrinterType("BUILT_IN") },
                    label = { Text("Built-in") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (state.printerType == "BUILT_IN") Icons.Filled.Check else Icons.Filled.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green500,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = state.printerType == "BLUETOOTH",
                    onClick = { viewModel.setPrinterType("BLUETOOTH") },
                    label = { Text("Bluetooth") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (state.printerType == "BLUETOOTH") Icons.Filled.Check else Icons.Filled.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green500,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = state.printerType == "TCP",
                    onClick = { viewModel.setPrinterType("TCP") },
                    label = { Text("Wi-Fi / LAN") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (state.printerType == "TCP") Icons.Filled.Check else Icons.Filled.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green500,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            if (state.printerType == "BUILT_IN") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Green500)
                        Column {
                            Text("Built-in POS Thermal Printer", fontWeight = FontWeight.Bold)
                            Text(
                                "Using terminal internal thermal printer / Sunmi hardware",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = state.printerAddress,
                    onValueChange = { viewModel.setPrinterAddress(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            if (state.printerType == "BLUETOOTH") "Bluetooth MAC Address (e.g. 00:11:22:33:44:55)"
                            else "Printer IP Address (e.g. 192.168.1.100)"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (state.printerType == "TCP") {
                    OutlinedTextField(
                        value = state.printerPort,
                        onValueChange = { viewModel.setPrinterPort(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Port (Standard: 9100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // ─── Paper Width (58mm vs 80mm) ───
            Text(
                "Paper Width",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 58mm
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setPaperWidth("58mm") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.paperWidth == "58mm") Green500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (state.paperWidth == "58mm") CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Green500), width = 2.dp) else null
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("58 mm (2 inch)", fontWeight = FontWeight.Bold, color = if (state.paperWidth == "58mm") Green500 else MaterialTheme.colorScheme.onSurface)
                        Text("32 Chars / 384 Dots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // 80mm
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setPaperWidth("80mm") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.paperWidth == "80mm") Green500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (state.paperWidth == "80mm") CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Green500), width = 2.dp) else null
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("80 mm (3 inch)", fontWeight = FontWeight.Bold, color = if (state.paperWidth == "80mm") Green500 else MaterialTheme.colorScheme.onSurface)
                        Text("48 Chars / 576 Dots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // ─── Header Logo ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Print Logo on Receipt", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Include graphic logo image at top of receipt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.showReceiptLogo,
                    onCheckedChange = { viewModel.setShowReceiptLogo(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = Green500)
                )
            }

            if (state.showReceiptLogo) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.receiptLogoUri.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = state.receiptLogoUri,
                                        contentDescription = "Logo",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(4.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Current Logo", fontWeight = FontWeight.Bold)
                                    Text(
                                        "Image will be rasterized to monochrome thermal print",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.setReceiptLogoUri("") }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove Logo", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green500)
                        ) {
                            Icon(Icons.Filled.Image, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.receiptLogoUri.isNotBlank()) "Change Logo Image" else "Choose Logo from Gallery")
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // ─── Advanced Settings Accordion ───
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvancedSettings = !showAdvancedSettings },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Advanced ESC/POS Commands & Modes", fontWeight = FontWeight.SemiBold)
                        }
                        Icon(
                            imageVector = if (showAdvancedSettings) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showAdvancedSettings) {
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Print Mode
                            Text("Print Mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("STANDARD" to "Standard Text", "RASTER" to "Raster Image", "RAW" to "Raw Stream").forEach { (mode, label) ->
                                    FilterChip(
                                        selected = state.printMode == mode,
                                        onClick = { viewModel.setPrintMode(mode) },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Green500,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Initial ESC/POS Hex
                            OutlinedTextField(
                                value = state.escInitCmd,
                                onValueChange = { viewModel.setEscInitCmd(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Initial Command Hex (ESC @)") },
                                placeholder = { Text("1B40") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Cutter Command Hex
                            OutlinedTextField(
                                value = state.escCutCmd,
                                onValueChange = { viewModel.setEscCutCmd(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Cutter Command Hex (GS V 1)") },
                                placeholder = { Text("1D5601") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Cash Drawer Command Hex
                            OutlinedTextField(
                                value = state.escDrawerCmd,
                                onValueChange = { viewModel.setEscDrawerCmd(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Drawer Kick Command Hex") },
                                placeholder = { Text("1B700019FF") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // ─── Receipt Header / Footer Text ───
            Text(
                "Receipt Text Template",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = state.receiptHeader,
                onValueChange = { viewModel.setReceiptHeader(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Receipt Header Text") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = state.receiptFooter,
                onValueChange = { viewModel.setReceiptFooter(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Receipt Footer Text") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // Test Print
            Button(
                onClick = { viewModel.testPrint() },
                modifier = Modifier.fillMaxWidth(),
                enabled = (state.printerType == "BUILT_IN" || state.printerAddress.isNotBlank()) && !state.isTesting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green500)
            ) {
                if (state.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Printing Test...")
                } else {
                    Icon(Icons.Filled.Print, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Print Test Receipt", fontWeight = FontWeight.Bold)
                }
            }

            state.testResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.startsWith("Error"))
                            MaterialTheme.colorScheme.errorContainer
                        else Green500.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // ─── Search Printers Dialog ───
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nearby Printers")
                    if (state.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (state.isSearching) {
                        Text(
                            "Scanning for Bluetooth and Network thermal printers...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (state.discoveredPrinters.isEmpty()) {
                        Text(
                            "No printers found. Make sure your printer is turned on and paired in Android Bluetooth settings, or on the same Wi-Fi subnet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            items(state.discoveredPrinters) { printer ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectDiscoveredPrinter(printer)
                                            showSearchDialog = false
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            if (printer.type == "BLUETOOTH") Icons.Filled.Bluetooth else Icons.Filled.Wifi,
                                            contentDescription = null,
                                            tint = Green500
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(printer.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${printer.address}${if (printer.isPaired) " (Paired)" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.searchPrinters() }) {
                    Text("Rescan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
