package com.quicpos.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.domain.model.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    init {
        loadSettings()
        observeTheme()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settings.value = settingsRepository.getSettings()
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            settingsRepository.getThemeFlow().collect { theme ->
                _settings.update { it.copy(theme = theme) }
            }
        }
    }

    fun setBusinessName(name: String) {
        viewModelScope.launch {
            settingsRepository.setBusinessName(name)
            _settings.update { it.copy(businessName = name) }
        }
    }

    fun setPosRegisterName(name: String) {
        viewModelScope.launch {
            settingsRepository.setPosRegisterName(name)
            _settings.update { it.copy(posRegisterName = name) }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            _settings.update { it.copy(theme = theme) }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        setTheme(if (enabled) "NIGHT_BLUE" else "LIGHT")
    }

    fun setLayoutMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setLayoutMode(mode)
            _settings.update { it.copy(layoutMode = mode) }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(lang)
            _settings.update { it.copy(language = lang) }
        }
    }

    fun setBarcodeScannerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBarcodeScannerEnabled(enabled)
            _settings.update { it.copy(useBarcodeSanner = enabled) }
        }
    }

    fun setCurrencyCode(code: String) {
        viewModelScope.launch {
            val symbol = when (code) {
                "KHR" -> "៛"
                "USD" -> "$"
                "THB" -> "฿"
                "VND" -> "₫"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                else -> code
            }
            settingsRepository.setCurrencyCode(code)
            settingsRepository.setCurrencySymbol(symbol)
            _settings.update { it.copy(currencyCode = code, currencySymbol = symbol) }
        }
    }

    fun setDualCurrencyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDualCurrencyEnabled(enabled)
            _settings.update { it.copy(isDualCurrencyEnabled = enabled) }
        }
    }

    fun setSecondaryCurrency(code: String) {
        viewModelScope.launch {
            val symbol = when (code) {
                "KHR" -> "៛"
                "USD" -> "$"
                "THB" -> "฿"
                "VND" -> "₫"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                else -> code
            }
            settingsRepository.setSecondaryCurrencyCode(code)
            settingsRepository.setSecondaryCurrencySymbol(symbol)
            _settings.update { it.copy(secondaryCurrencyCode = code, secondaryCurrencySymbol = symbol) }
        }
    }

    fun setExchangeRate(rate: Double) {
        viewModelScope.launch {
            settingsRepository.setExchangeRate(rate)
            _settings.update { it.copy(exchangeRate = rate) }
        }
    }

    fun setReceiptHeader(header: String) {
        viewModelScope.launch {
            settingsRepository.setReceiptHeader(header)
            _settings.update { it.copy(receiptHeader = header) }
        }
    }

    fun setReceiptFooter(footer: String) {
        viewModelScope.launch {
            settingsRepository.setReceiptFooter(footer)
            _settings.update { it.copy(receiptFooter = footer) }
        }
    }

    fun setReceiptLogoUri(uri: String) {
        viewModelScope.launch {
            settingsRepository.setReceiptLogoUri(uri)
            _settings.update { it.copy(receiptLogoUri = uri) }
        }
    }

    fun setReceiptLogoEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReceiptLogoEnabled(enabled)
            _settings.update { it.copy(showReceiptLogo = enabled) }
        }
    }

    // Accessibility
    fun setItemSize(size: String) {
        viewModelScope.launch {
            settingsRepository.setItemSize(size)
            _settings.update { it.copy(itemSize = size) }
        }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsRepository.setGridColumns(columns)
            _settings.update { it.copy(gridColumns = columns) }
        }
    }

    fun setFontSizeScale(scale: Float) {
        viewModelScope.launch {
            settingsRepository.setFontSizeScale(scale)
            _settings.update { it.copy(fontSizeScale = scale) }
        }
    }

    // Back Office PIN
    fun setBackOfficePin(pin: String) {
        viewModelScope.launch {
            settingsRepository.setBackOfficePin(pin)
        }
    }

    suspend fun verifyBackOfficePin(pin: String): Boolean =
        settingsRepository.verifyBackOfficePin(pin)
}
