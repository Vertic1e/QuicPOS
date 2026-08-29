package com.quicpos.app.domain.model

data class Settings(
    val businessName: String = "QuicPOS Store",
    val posRegisterName: String = "POS 1",
    val currencyCode: String = "KHR",
    val currencySymbol: String = "៛",
    val theme: String = "NIGHT_BLUE",
    val layoutMode: String = "GRID", // "GRID" or "LIST"
    val language: String = "en",
    val useBarcodeSanner: Boolean = true,
    val printerType: String = "BUILT_IN", // "BUILT_IN", "BLUETOOTH", "TCP"
    val printerAddress: String = "",
    val printerPort: Int = 9100,
    val receiptHeader: String = "Thank you for shopping!",
    val receiptFooter: String = "Please come again",
    val receiptLogoUri: String = "",
    val showReceiptLogo: Boolean = false,
    val isDualCurrencyEnabled: Boolean = true,
    val secondaryCurrencyCode: String = "USD",
    val secondaryCurrencySymbol: String = "$",
    val exchangeRate: Double = 4000.0,
    val itemSize: String = "MEDIUM", // "SMALL", "MEDIUM", "LARGE"
    val gridColumns: Int = 3, // 2, 3, 4, or 0 (0 = Auto)
    val fontSizeScale: Float = 1.0f
) {
    /** Backward-compat: isDarkMode is derived from theme. */
    val isDarkMode: Boolean get() = theme != "LIGHT"

    val isGridLayout: Boolean get() = layoutMode == "GRID"
    val isPrinterConfigured: Boolean get() = printerType == "BUILT_IN" || (printerType.isNotBlank() && printerAddress.isNotBlank())

    fun formatAmount(amount: Double): String {
        val formatted = if (amount == amount.toLong().toDouble()) {
            String.format("%,.0f", amount)
        } else {
            String.format("%,.2f", amount)
        }
        return "$currencyCode $formatted"
    }

    fun formatPrimary(amount: Double): String = formatAmount(amount)

    fun convertToSecondary(amountPrimary: Double): Double {
        if (exchangeRate <= 0.0) return 0.0
        return if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
            amountPrimary / exchangeRate
        } else if (currencyCode == "USD" && secondaryCurrencyCode == "KHR") {
            amountPrimary * exchangeRate
        } else {
            // General conversion: Primary / Rate
            amountPrimary / exchangeRate
        }
    }

    fun convertToPrimary(amountSecondary: Double): Double {
        if (exchangeRate <= 0.0) return 0.0
        return if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
            amountSecondary * exchangeRate
        } else if (currencyCode == "USD" && secondaryCurrencyCode == "KHR") {
            amountSecondary / exchangeRate
        } else {
            amountSecondary * exchangeRate
        }
    }

    fun formatSecondary(amountSecondary: Double): String {
        val formatted = if (secondaryCurrencyCode == "KHR") {
            String.format("%,.0f", amountSecondary)
        } else {
            String.format("%,.2f", amountSecondary)
        }
        return "$secondaryCurrencyCode $formatted"
    }

    fun formatDual(amountPrimary: Double): String {
        val primaryStr = formatPrimary(amountPrimary)
        if (!isDualCurrencyEnabled) return primaryStr
        val secondaryAmount = convertToSecondary(amountPrimary)
        val secondaryStr = formatSecondary(secondaryAmount)
        return "$primaryStr ($secondaryStr)"
    }
}
