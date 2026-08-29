package com.quicpos.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.local.dao.TaxDao
import com.quicpos.app.data.local.entity.TaxEntity
import com.quicpos.app.ui.components.SimpleTopBar
import com.quicpos.app.ui.theme.Green500
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxSetupViewModel @Inject constructor(
    private val taxDao: TaxDao
) : ViewModel() {
    private val _taxes = MutableStateFlow<List<TaxEntity>>(emptyList())
    val taxes: StateFlow<List<TaxEntity>> = _taxes.asStateFlow()

    init {
        viewModelScope.launch {
            taxDao.getAllTaxes().collect { _taxes.value = it }
        }
    }

    fun saveTax(name: String, rate: Double, isInclusive: Boolean) {
        viewModelScope.launch {
            taxDao.insertTax(TaxEntity(name = name, rate = rate, isInclusive = isInclusive))
        }
    }

    fun toggleTaxActive(tax: TaxEntity) {
        viewModelScope.launch {
            taxDao.setTaxActive(tax.id, !tax.isActive)
        }
    }

    fun deleteTax(tax: TaxEntity) {
        viewModelScope.launch { taxDao.deleteTax(tax) }
    }
}

@Composable
fun TaxSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaxSetupViewModel = hiltViewModel()
) {
    val taxes by viewModel.taxes.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Taxes & Surcharges",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, "Add Tax", tint = Green500)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (taxes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Receipt, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No taxes configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(taxes, key = { it.id }) { tax ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tax.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", tax.rate)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Green500,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (tax.isInclusive) "Inclusive" else "Exclusive",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = tax.isActive,
                                onCheckedChange = { viewModel.toggleTaxActive(tax) },
                                colors = SwitchDefaults.colors(checkedTrackColor = Green500)
                            )
                            IconButton(onClick = { viewModel.deleteTax(tax) }) {
                                Icon(
                                    Icons.Filled.Delete, "Delete",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var rate by remember { mutableStateOf("") }
        var isInclusive by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Tax") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tax Name") },
                        placeholder = { Text("e.g., VAT") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = rate,
                        onValueChange = { rate = it },
                        label = { Text("Rate (%)") },
                        placeholder = { Text("e.g., 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tax Inclusive")
                        Switch(
                            checked = isInclusive,
                            onCheckedChange = { isInclusive = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Green500)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && rate.isNotBlank()) {
                            viewModel.saveTax(name, rate.toDoubleOrNull() ?: 0.0, isInclusive)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
