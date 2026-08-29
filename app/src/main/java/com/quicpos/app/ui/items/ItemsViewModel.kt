package com.quicpos.app.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.CategoryRepository
import com.quicpos.app.data.repository.ItemRepository
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.domain.model.Category
import com.quicpos.app.domain.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemsUiState(
    val items: List<Item> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val currencyCode: String = "KHR",
    val isDualCurrencyEnabled: Boolean = true,
    val secondaryCurrencyCode: String = "USD",
    val exchangeRate: Double = 4000.0,
    val isLoading: Boolean = true,
    val itemCount: Int = 0
)

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<Long?>(null)

    init {
        loadSettings()
        observeCategories()
        observeItems()
        observeItemCount()
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
                    isDualCurrencyEnabled = settings.isDualCurrencyEnabled,
                    secondaryCurrencyCode = settings.secondaryCurrencyCode,
                    exchangeRate = settings.exchangeRate
                )
            }
        }
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
            combine(_searchQuery, _selectedCategory) { query, categoryId ->
                Pair(query, categoryId)
            }.collectLatest { (query, categoryId) ->
                val itemsFlow = when {
                    query.isNotBlank() -> itemRepository.searchItems(query)
                    categoryId != null -> itemRepository.getItemsByCategory(categoryId)
                    else -> itemRepository.getAllItems()
                }
                itemsFlow.collect { items ->
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
            }
        }
    }

    private fun observeItemCount() {
        viewModelScope.launch {
            itemRepository.getActiveItemCount().collect { count ->
                _uiState.update { it.copy(itemCount = count) }
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun filterByCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            itemRepository.deleteItem(itemId)
        }
    }

    fun toggleFavorite(item: Item) {
        viewModelScope.launch {
            itemRepository.setFavorite(item.id, !item.isFavorite)
        }
    }
}
