package com.quicpos.app.ui.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.ReceiptRepository
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.domain.model.Receipt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptsUiState(
    val receipts: List<Receipt> = emptyList(),
    val searchQuery: String = "",
    val currencyCode: String = "KHR",
    val isLoading: Boolean = true,
    val groupedReceipts: Map<String, List<Receipt>> = emptyMap()
)

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptsUiState())
    val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadSettings()
        observeReceipts()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val currencyCode = settingsRepository.getCurrencyCode()
            _uiState.update { it.copy(currencyCode = currencyCode) }
        }
    }

    private fun observeReceipts() {
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                val receiptsFlow = if (query.isNotBlank()) {
                    receiptRepository.searchReceipts(query)
                } else {
                    receiptRepository.getAllReceipts()
                }

                receiptsFlow.collect { receipts ->
                    val grouped = receipts.groupBy { it.displayDateGroup }
                    _uiState.update {
                        it.copy(
                            receipts = receipts,
                            groupedReceipts = grouped,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }
}
