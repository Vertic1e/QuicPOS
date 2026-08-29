package com.quicpos.app.ui.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.CategoryRepository
import com.quicpos.app.data.repository.ItemRepository
import com.quicpos.app.domain.model.Category
import com.quicpos.app.domain.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ItemEditUiState(
    val item: Item = Item(),
    val categories: List<Category> = emptyList(),
    val isNew: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ItemEditViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: 0L

    private val _uiState = MutableStateFlow(ItemEditUiState())
    val uiState: StateFlow<ItemEditUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        if (itemId > 0L) {
            loadItem(itemId)
        } else {
            _uiState.update {
                it.copy(
                    item = Item(sku = generateSku()),
                    isNew = true
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadItem(id: Long) {
        viewModelScope.launch {
            val item = itemRepository.getItemById(id)
            if (item != null) {
                _uiState.update { it.copy(item = item, isNew = false) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(item = it.item.copy(name = name)) }
    }

    fun updateCategory(categoryId: Long?) {
        _uiState.update { it.copy(item = it.item.copy(categoryId = categoryId)) }
    }

    fun updatePrice(price: String) {
        val priceValue = price.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(item = it.item.copy(price = priceValue)) }
    }

    fun updateCost(cost: String) {
        val costValue = cost.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(item = it.item.copy(cost = costValue)) }
    }

    fun updateSku(sku: String) {
        _uiState.update { it.copy(item = it.item.copy(sku = sku)) }
    }

    fun updateBarcode(barcode: String) {
        _uiState.update { it.copy(item = it.item.copy(barcode = barcode.ifBlank { null })) }
    }

    fun updateSoldBy(soldBy: String) {
        _uiState.update { it.copy(item = it.item.copy(soldBy = soldBy)) }
    }

    fun updateVariablePrice(variablePrice: Boolean) {
        _uiState.update { it.copy(item = it.item.copy(variablePrice = variablePrice)) }
    }

    fun updateTrackStock(trackStock: Boolean) {
        _uiState.update { it.copy(item = it.item.copy(trackStock = trackStock)) }
    }

    fun updateStockQuantity(quantity: String) {
        val qty = quantity.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(item = it.item.copy(stockQuantity = qty)) }
    }

    fun updateColor(colorHex: String) {
        _uiState.update { it.copy(item = it.item.copy(colorHex = colorHex)) }
    }

    fun updateImageUrl(imageUrl: String?) {
        _uiState.update { it.copy(item = it.item.copy(imageUrl = imageUrl?.ifBlank { null })) }
    }

    fun saveItem() {
        val item = _uiState.value.item
        if (item.name.isBlank()) {
            _uiState.update { it.copy(error = "Item name is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                itemRepository.saveItem(item)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun generateSku(): String {
        return "ITM-${UUID.randomUUID().toString().take(6).uppercase()}"
    }
}
