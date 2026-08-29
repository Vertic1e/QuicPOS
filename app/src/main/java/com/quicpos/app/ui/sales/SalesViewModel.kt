package com.quicpos.app.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.CategoryRepository
import com.quicpos.app.data.repository.ItemRepository
import com.quicpos.app.data.repository.ReceiptRepository
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.domain.model.Item
import com.quicpos.app.domain.model.TicketLine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SalesViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val receiptRepository: ReceiptRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeCategories()
        observeItems()
    }

    fun onIntent(intent: SalesIntent) {
        when (intent) {
            is SalesIntent.AddItemToTicket -> addItemToTicket(intent.item)
            is SalesIntent.RemoveTicketLine -> removeTicketLine(intent.index)
            is SalesIntent.UpdateLineQuantity -> updateLineQuantity(intent.index, intent.quantity)
            is SalesIntent.SelectCategory -> selectCategory(intent.categoryId)
            is SalesIntent.SearchItems -> searchItems(intent.query)
            is SalesIntent.ClearTicket -> clearTicket()
            is SalesIntent.ApplyLineDiscount -> applyLineDiscount(intent.index, intent.amount)
            is SalesIntent.SetCustomerName -> setCustomerName(intent.name)
        }
    }

    fun refreshSettings() {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            _uiState.update {
                it.copy(
                    currencyCode = settings.currencyCode,
                    currencySymbol = settings.currencySymbol,
                    layoutMode = settings.layoutMode,
                    businessName = settings.businessName,
                    posRegisterName = settings.posRegisterName,
                    isDualCurrencyEnabled = settings.isDualCurrencyEnabled,
                    secondaryCurrencyCode = settings.secondaryCurrencyCode,
                    secondaryCurrencySymbol = settings.secondaryCurrencySymbol,
                    exchangeRate = settings.exchangeRate,
                    itemSize = settings.itemSize,
                    gridColumns = settings.gridColumns,
                    fontSizeScale = settings.fontSizeScale
                )
            }
        }

        // Observe layout mode changes
        viewModelScope.launch {
            settingsRepository.getLayoutModeFlow().collect { mode ->
                _uiState.update { it.copy(layoutMode = mode) }
            }
        }

        // Observe grid columns changes
        viewModelScope.launch {
            settingsRepository.getGridColumnsFlow().collect { cols ->
                _uiState.update { it.copy(gridColumns = cols) }
            }
        }
    }

    private fun setCustomerName(name: String?) {
        _uiState.update { it.copy(customerName = name) }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun observeItems() {
        _uiState
            .map { it.selectedCategoryId to it.searchQuery }
            .distinctUntilChanged()
            .flatMapLatest { (categoryId, query) ->
                when {
                    query.isNotBlank() -> itemRepository.searchItems(query)
                    categoryId == -1L -> itemRepository.getFavoriteItems()
                    categoryId != null -> itemRepository.getItemsByCategory(categoryId)
                    else -> itemRepository.getAllItems()
                }
            }
            .onEach { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun addItemToTicket(item: Item) {
        _uiState.update { state ->
            val existingIndex = state.ticketLines.indexOfFirst { it.itemId == item.id }

            val updatedLines = if (existingIndex >= 0) {
                // Item already in ticket - increment quantity
                state.ticketLines.toMutableList().apply {
                    val existing = this[existingIndex]
                    this[existingIndex] = existing.withUpdatedQuantity(existing.quantity + 1)
                }
            } else {
                // Add new line
                state.ticketLines + TicketLine(
                    itemId = item.id,
                    itemName = item.name,
                    quantity = 1.0,
                    unitPrice = item.price,
                    lineTotal = item.price,
                    colorHex = item.colorHex
                )
            }

            state.copy(ticketLines = updatedLines)
        }
    }

    private fun removeTicketLine(index: Int) {
        _uiState.update { state ->
            state.copy(ticketLines = state.ticketLines.toMutableList().apply { removeAt(index) })
        }
    }

    private fun updateLineQuantity(index: Int, quantity: Double) {
        _uiState.update { state ->
            if (quantity <= 0) {
                state.copy(ticketLines = state.ticketLines.toMutableList().apply { removeAt(index) })
            } else {
                val updatedLines = state.ticketLines.toMutableList().apply {
                    this[index] = this[index].withUpdatedQuantity(quantity)
                }
                state.copy(ticketLines = updatedLines)
            }
        }
    }

    private fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, searchQuery = "") }
    }

    private fun searchItems(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun clearTicket() {
        _uiState.update { it.copy(ticketLines = emptyList()) }
    }

    private fun applyLineDiscount(index: Int, amount: Double) {
        _uiState.update { state ->
            val updatedLines = state.ticketLines.toMutableList().apply {
                this[index] = this[index].withDiscount(amount)
            }
            state.copy(ticketLines = updatedLines)
        }
    }

    // Charge flow
    suspend fun completeSale(
        paymentMethod: String,
        cashTendered: Double = 0.0,
        customerName: String? = null
    ): String {
        val state = _uiState.value
        val receiptNumber = receiptRepository.createReceipt(
            ticketLines = state.ticketLines,
            subtotal = state.subtotal,
            taxAmount = 0.0, // Tax calculated at charge screen
            discountAmount = state.totalDiscount,
            totalAmount = state.grandTotal,
            paymentMethod = paymentMethod,
            cashTendered = cashTendered,
            changeGiven = if (cashTendered > state.grandTotal) cashTendered - state.grandTotal else 0.0,
            customerName = customerName
        )
        clearTicket()
        return receiptNumber
    }

    fun addItemByBarcode(barcode: String) {
        viewModelScope.launch {
            val item = itemRepository.getItemByBarcode(barcode)
            if (item != null) {
                addItemToTicket(item)
            }
        }
    }
}
