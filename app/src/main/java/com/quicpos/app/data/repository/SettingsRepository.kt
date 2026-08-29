package com.quicpos.app.data.repository

import com.quicpos.app.data.local.dao.SettingsDao
import com.quicpos.app.data.local.entity.SettingsEntity
import com.quicpos.app.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    // Business Info
    suspend fun getBusinessName(): String =
        settingsDao.getValue(SettingsEntity.KEY_BUSINESS_NAME) ?: SettingsEntity.DEFAULT_BUSINESS_NAME

    suspend fun setBusinessName(name: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_BUSINESS_NAME, name)

    suspend fun getPosRegisterName(): String =
        settingsDao.getValue(SettingsEntity.KEY_POS_REGISTER_NAME) ?: SettingsEntity.DEFAULT_POS_REGISTER

    suspend fun setPosRegisterName(name: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_POS_REGISTER_NAME, name)

    // Currency
    suspend fun getCurrencyCode(): String =
        settingsDao.getValue(SettingsEntity.KEY_CURRENCY_CODE) ?: SettingsEntity.DEFAULT_CURRENCY_CODE

    suspend fun setCurrencyCode(code: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_CURRENCY_CODE, code)

    suspend fun getCurrencySymbol(): String =
        settingsDao.getValue(SettingsEntity.KEY_CURRENCY_SYMBOL) ?: SettingsEntity.DEFAULT_CURRENCY_SYMBOL

    suspend fun setCurrencySymbol(symbol: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_CURRENCY_SYMBOL, symbol)

    // Dual Currency
    fun isDualCurrencyEnabledFlow(): Flow<Boolean> =
        settingsDao.getValueFlow(SettingsEntity.KEY_DUAL_CURRENCY_ENABLED)
            .map { it?.toBooleanStrictOrNull() ?: true }

    suspend fun isDualCurrencyEnabled(): Boolean =
        settingsDao.getValue(SettingsEntity.KEY_DUAL_CURRENCY_ENABLED)?.toBooleanStrictOrNull() ?: true

    suspend fun setDualCurrencyEnabled(enabled: Boolean) =
        settingsDao.setValueForKey(SettingsEntity.KEY_DUAL_CURRENCY_ENABLED, enabled.toString())

    suspend fun getSecondaryCurrencyCode(): String =
        settingsDao.getValue(SettingsEntity.KEY_SECONDARY_CURRENCY_CODE) ?: SettingsEntity.DEFAULT_SECONDARY_CURRENCY_CODE

    suspend fun setSecondaryCurrencyCode(code: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_SECONDARY_CURRENCY_CODE, code)

    suspend fun getSecondaryCurrencySymbol(): String =
        settingsDao.getValue(SettingsEntity.KEY_SECONDARY_CURRENCY_SYMBOL) ?: SettingsEntity.DEFAULT_SECONDARY_CURRENCY_SYMBOL

    suspend fun setSecondaryCurrencySymbol(symbol: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_SECONDARY_CURRENCY_SYMBOL, symbol)

    suspend fun getExchangeRate(): Double =
        settingsDao.getValue(SettingsEntity.KEY_EXCHANGE_RATE)?.toDoubleOrNull()
            ?: SettingsEntity.DEFAULT_EXCHANGE_RATE.toDouble()

    suspend fun setExchangeRate(rate: Double) =
        settingsDao.setValueForKey(SettingsEntity.KEY_EXCHANGE_RATE, rate.toString())

    // Theme
    fun getThemeFlow(): Flow<String> =
        settingsDao.getValueFlow(SettingsEntity.KEY_THEME)
            .map { it ?: SettingsEntity.DEFAULT_THEME }

    suspend fun getTheme(): String =
        settingsDao.getValue(SettingsEntity.KEY_THEME) ?: SettingsEntity.DEFAULT_THEME

    suspend fun setTheme(theme: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_THEME, theme)

    // Legacy dark mode — now derived from theme
    fun isDarkModeFlow(): Flow<Boolean> =
        getThemeFlow().map { it != "LIGHT" }

    suspend fun isDarkMode(): Boolean = getTheme() != "LIGHT"

    suspend fun setDarkMode(enabled: Boolean) {
        setTheme(if (enabled) "NIGHT_BLUE" else "LIGHT")
    }

    fun getLayoutModeFlow(): Flow<String> =
        settingsDao.getValueFlow(SettingsEntity.KEY_LAYOUT_MODE)
            .map { it ?: SettingsEntity.DEFAULT_LAYOUT_MODE }

    suspend fun getLayoutMode(): String =
        settingsDao.getValue(SettingsEntity.KEY_LAYOUT_MODE) ?: SettingsEntity.DEFAULT_LAYOUT_MODE

    suspend fun setLayoutMode(mode: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_LAYOUT_MODE, mode)

    // Language
    suspend fun getLanguage(): String =
        settingsDao.getValue(SettingsEntity.KEY_LANGUAGE) ?: SettingsEntity.DEFAULT_LANGUAGE

    suspend fun setLanguage(lang: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_LANGUAGE, lang)

    // Barcode Scanner
    suspend fun isBarcodeScannerEnabled(): Boolean =
        settingsDao.getValue(SettingsEntity.KEY_USE_BARCODE_SCANNER)?.toBooleanStrictOrNull() ?: true

    suspend fun setBarcodeScannerEnabled(enabled: Boolean) =
        settingsDao.setValueForKey(SettingsEntity.KEY_USE_BARCODE_SCANNER, enabled.toString())

    // Printer
    suspend fun getPrinterType(): String =
        settingsDao.getValue(SettingsEntity.KEY_PRINTER_TYPE) ?: SettingsEntity.DEFAULT_PRINTER_TYPE

    suspend fun setPrinterType(type: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_PRINTER_TYPE, type)

    suspend fun getPrinterAddress(): String =
        settingsDao.getValue(SettingsEntity.KEY_PRINTER_ADDRESS) ?: ""

    suspend fun setPrinterAddress(address: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_PRINTER_ADDRESS, address)

    suspend fun getPrinterPort(): Int =
        settingsDao.getValue(SettingsEntity.KEY_PRINTER_PORT)?.toIntOrNull()
            ?: SettingsEntity.DEFAULT_PRINTER_PORT.toInt()

    suspend fun setPrinterPort(port: Int) =
        settingsDao.setValueForKey(SettingsEntity.KEY_PRINTER_PORT, port.toString())

    // Receipt Template
    suspend fun getReceiptHeader(): String =
        settingsDao.getValue(SettingsEntity.KEY_RECEIPT_HEADER) ?: SettingsEntity.DEFAULT_RECEIPT_HEADER

    suspend fun setReceiptHeader(header: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_RECEIPT_HEADER, header)

    suspend fun getReceiptFooter(): String =
        settingsDao.getValue(SettingsEntity.KEY_RECEIPT_FOOTER) ?: SettingsEntity.DEFAULT_RECEIPT_FOOTER

    suspend fun setReceiptFooter(footer: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_RECEIPT_FOOTER, footer)

    // Receipt Logo
    suspend fun getReceiptLogoUri(): String =
        settingsDao.getValue(SettingsEntity.KEY_RECEIPT_LOGO_URI) ?: SettingsEntity.DEFAULT_RECEIPT_LOGO_URI

    suspend fun setReceiptLogoUri(uri: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_RECEIPT_LOGO_URI, uri)

    suspend fun isReceiptLogoEnabled(): Boolean =
        settingsDao.getValue(SettingsEntity.KEY_RECEIPT_SHOW_LOGO)?.toBooleanStrictOrNull() ?: false

    suspend fun setReceiptLogoEnabled(enabled: Boolean) =
        settingsDao.setValueForKey(SettingsEntity.KEY_RECEIPT_SHOW_LOGO, enabled.toString())

    // Lock PIN
    suspend fun getLockPin(): String? =
        settingsDao.getValue(SettingsEntity.KEY_LOCK_PIN)

    suspend fun setLockPin(pin: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_LOCK_PIN, pin)

    // Receipt Counter
    suspend fun getAndIncrementReceiptCounter(): Long {
        val current = settingsDao.getValue(SettingsEntity.KEY_RECEIPT_COUNTER)?.toLongOrNull() ?: 0
        val next = current + 1
        settingsDao.setValueForKey(SettingsEntity.KEY_RECEIPT_COUNTER, next.toString())
        return next
    }

    // Accessibility: Item Size
    suspend fun getItemSize(): String =
        settingsDao.getValue(SettingsEntity.KEY_ITEM_SIZE) ?: SettingsEntity.DEFAULT_ITEM_SIZE

    suspend fun setItemSize(size: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_ITEM_SIZE, size)

    // Accessibility: Grid Columns (2, 3, 4, 0)
    fun getGridColumnsFlow(): Flow<Int> =
        settingsDao.getValueFlow(SettingsEntity.KEY_GRID_COLUMNS)
            .map { it?.toIntOrNull() ?: SettingsEntity.DEFAULT_GRID_COLUMNS.toInt() }

    suspend fun getGridColumns(): Int =
        settingsDao.getValue(SettingsEntity.KEY_GRID_COLUMNS)?.toIntOrNull()
            ?: SettingsEntity.DEFAULT_GRID_COLUMNS.toInt()

    suspend fun setGridColumns(columns: Int) =
        settingsDao.setValueForKey(SettingsEntity.KEY_GRID_COLUMNS, columns.toString())

    // Accessibility: Font Size Scale
    suspend fun getFontSizeScale(): Float =
        settingsDao.getValue(SettingsEntity.KEY_FONT_SIZE_SCALE)?.toFloatOrNull()
            ?: SettingsEntity.DEFAULT_FONT_SIZE_SCALE.toFloat()

    suspend fun setFontSizeScale(scale: Float) =
        settingsDao.setValueForKey(SettingsEntity.KEY_FONT_SIZE_SCALE, scale.toString())

    // Back Office PIN
    suspend fun getBackOfficePin(): String =
        settingsDao.getValue(SettingsEntity.KEY_BACK_OFFICE_PIN) ?: SettingsEntity.DEFAULT_BACK_OFFICE_PIN

    suspend fun setBackOfficePin(pin: String) =
        settingsDao.setValueForKey(SettingsEntity.KEY_BACK_OFFICE_PIN, pin)

    suspend fun verifyBackOfficePin(pin: String): Boolean =
        pin == getBackOfficePin()

    // Aggregate settings object
    suspend fun getSettings(): Settings = Settings(
        businessName = getBusinessName(),
        posRegisterName = getPosRegisterName(),
        currencyCode = getCurrencyCode(),
        currencySymbol = getCurrencySymbol(),
        theme = getTheme(),
        layoutMode = getLayoutMode(),
        language = getLanguage(),
        useBarcodeSanner = isBarcodeScannerEnabled(),
        printerType = getPrinterType(),
        printerAddress = getPrinterAddress(),
        printerPort = getPrinterPort(),
        receiptHeader = getReceiptHeader(),
        receiptFooter = getReceiptFooter(),
        receiptLogoUri = getReceiptLogoUri(),
        showReceiptLogo = isReceiptLogoEnabled(),
        isDualCurrencyEnabled = isDualCurrencyEnabled(),
        secondaryCurrencyCode = getSecondaryCurrencyCode(),
        secondaryCurrencySymbol = getSecondaryCurrencySymbol(),
        exchangeRate = getExchangeRate(),
        itemSize = getItemSize(),
        gridColumns = getGridColumns(),
        fontSizeScale = getFontSizeScale()
    )
}
