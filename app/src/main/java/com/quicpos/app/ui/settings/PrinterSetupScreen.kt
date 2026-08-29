package com.quicpos.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.printing.PrinterManager
import com.quicpos.app.ui.components.SimpleTopBar
import com.quicpos.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrinterSetupState(
    val printerType: String = "BUILT_IN",
    val printerAddress: String = "",
    val printerPort: String = "9100",
    val receiptHeader: String = "",
    val receiptFooter: String = "",
    val receiptLogoUri: String = "",
    val showReceiptLogo: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: String? = null
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
                printerType = settingsRepository.getPrinterType(),
                printerAddress = settingsRepository.getPrinterAddress(),
                printerPort = settingsRepository.getPrinterPort().toString(),
                receiptHeader = settingsRepository.getReceiptHeader(),
                receiptFooter = settingsRepository.getReceiptFooter(),
                receiptLogoUri = settingsRepository.getReceiptLogoUri(),
                showReceiptLogo = settingsRepository.isReceiptLogoEnabled()
            )
        }
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

@Composable
fun PrinterSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrinterSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Type
            Text(
                "Printer Connection Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Built-In Printer
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

                // Bluetooth
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

                // Network / TCP
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

            // Connection Details
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
                            Text("Built-in POS Printer Active", fontWeight = FontWeight.Bold)
                            Text(
                                "Using terminal internal thermal printer / Sunmi hardware",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Address / MAC
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

                // Port (TCP only)
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

            // Receipt Header Logo Section
            Text(
                "Receipt Header Logo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

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

            // Receipt Template Text
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

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
}
