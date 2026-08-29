package com.quicpos.app.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.CategoryRepository
import com.quicpos.app.data.repository.ItemRepository
import com.quicpos.app.data.repository.ReceiptRepository
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.domain.model.Item
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.domain.model.TicketLine
import com.quicpos.app.printing.PrinterManager
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
    private val settingsRepository: SettingsRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private val selectedCategoryFlow = MutableStateFlow<Long?>(null)

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
        viewModelScope.launch {
            combine(
                searchQueryFlow,
                selectedCategoryFlow
            ) { query, categoryId ->
                Pair(query, categoryId)
            }.flatMapLatest { (query, categoryId) ->
                when {
                    query.isNotBlank() -> itemRepository.searchItems(query)
                    categoryId != null -> itemRepository.getItemsByCategory(categoryId)
                    else -> itemRepository.getAllItems()
                }
            }.collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    private fun selectCategory(categoryId: Long?) {
        selectedCategoryFlow.value = categoryId
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    private fun searchItems(query: String) {
        searchQueryFlow.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun addItemToTicket(item: Item) {
        val currentLines = _uiState.value.ticketLines.toMutableList()
        val existingIndex = currentLines.indexOfFirst { it.itemId == item.id }

        if (existingIndex >= 0) {
            val existing = currentLines[existingIndex]
            currentLines[existingIndex] = existing.copy(
                quantity = existing.quantity + 1.0,
                lineTotal = (existing.quantity + 1.0) * existing.unitPrice - existing.discountAmount
            )
        } else {
            currentLines.add(
                TicketLine(
                    itemId = item.id,
                    itemName = item.name,
                    quantity = 1.0,
                    unitPrice = item.price,
                    lineTotal = item.price
                )
            )
        }

        _uiState.update { it.copy(ticketLines = currentLines) }
    }

    private fun removeTicketLine(index: Int) {
        val currentLines = _uiState.value.ticketLines.toMutableList()
        if (index in currentLines.indices) {
            currentLines.removeAt(index)
            _uiState.update { it.copy(ticketLines = currentLines) }
        }
    }

    private fun updateLineQuantity(index: Int, quantity: Double) {
        val currentLines = _uiState.value.ticketLines.toMutableList()
        if (index in currentLines.indices) {
            if (quantity <= 0.0) {
                currentLines.removeAt(index)
            } else {
                val line = currentLines[index]
                currentLines[index] = line.copy(
                    quantity = quantity,
                    lineTotal = quantity * line.unitPrice - line.discountAmount
                )
            }
            _uiState.update { it.copy(ticketLines = currentLines) }
        }
    }

    private fun applyLineDiscount(index: Int, discountAmount: Double) {
        val currentLines = _uiState.value.ticketLines.toMutableList()
        if (index in currentLines.indices) {
            val line = currentLines[index]
            currentLines[index] = line.copy(
                discountAmount = discountAmount,
                lineTotal = line.quantity * line.unitPrice - discountAmount
            )
            _uiState.update { it.copy(ticketLines = currentLines) }
        }
    }

    private fun clearTicket() {
        _uiState.update { it.copy(ticketLines = emptyList(), customerName = null) }
    }

    suspend fun completeSale(
        paymentMethod: String,
        cashTendered: Double,
        customerName: String?
    ): String {
        val state = _uiState.value
        val receiptNumber = receiptRepository.createReceipt(
            ticketLines = state.ticketLines,
            subtotal = state.subtotal,
            taxAmount = 0.0,
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

    suspend fun completeSaleAndAutoPrint(
        paymentMethod: String,
        cashTendered: Double = 0.0,
        customerName: String? = null
    ): Pair<Receipt, List<TicketLine>> {
        val state = _uiState.value
        val lines = state.ticketLines.toList()
        val finalTendered = if (cashTendered <= 0.0) state.grandTotal else cashTendered
        val change = maxOf(0.0, finalTendered - state.grandTotal)

        val receiptNumber = receiptRepository.createReceipt(
            ticketLines = lines,
            subtotal = state.subtotal,
            taxAmount = 0.0,
            discountAmount = state.totalDiscount,
            totalAmount = state.grandTotal,
            paymentMethod = paymentMethod,
            cashTendered = finalTendered,
            changeGiven = change,
            customerName = customerName
        )

        val receipt = Receipt(
            receiptNumber = receiptNumber,
            timestamp = System.currentTimeMillis(),
            subtotalAmount = state.subtotal,
            taxAmount = 0.0,
            discountAmount = state.totalDiscount,
            totalAmount = state.grandTotal,
            paymentMethod = paymentMethod,
            cashTendered = finalTendered,
            changeGiven = change,
            customerName = customerName,
            status = "COMPLETED",
            posRegister = state.posRegisterName
        )

        // Automatically trigger print on terminal/external printer
        viewModelScope.launch {
            try {
                printerManager.printReceipt(receipt, lines)
            } catch (e: Exception) {
                // Background print catch
            }
        }

        clearTicket()
        return Pair(receipt, lines)
    }

    fun reprintReceipt(receipt: Receipt, lines: List<TicketLine>) {
        viewModelScope.launch {
            try {
                printerManager.printReceipt(receipt, lines)
            } catch (e: Exception) {
                // Ignore reprint error
            }
        }
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
