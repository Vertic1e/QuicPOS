package com.quicpos.app.ui.backoffice

data class HourlySale(
    val hour: Int, // 0..23
    val amount: Double
)

data class ItemSaleSummary(
    val itemName: String,
    val quantity: Double,
    val totalAmount: Double,
    val colorHex: String? = null
)

data class BackOfficeUiState(
    val isAuthenticated: Boolean = false,
    val enteredPin: String = "",
    val pinError: String? = null,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val formattedDate: String = "",
    val receiptsCount: Int = 0,
    val receiptsDiffPercent: Double = 0.0,
    val netSales: Double = 0.0,
    val netSalesDiffPercent: Double = 0.0,
    val averageSale: Double = 0.0,
    val averageSaleDiffPercent: Double = 0.0,
    val currencyCode: String = "KHR",
    val currencySymbol: String = "៛",
    val hourlySales: List<HourlySale> = (0..23).map { HourlySale(it, 0.0) },
    val maxHourlySale: Double = 0.0,
    val itemsSold: List<ItemSaleSummary> = emptyList(),
    val selectedHourTooltip: HourlySale? = null,
    val isLoading: Boolean = false,
    val isChangePinDialogOpen: Boolean = false
) {
    fun formatAmount(amount: Double): String {
        val formatted = if (amount == amount.toLong().toDouble()) {
            String.format("%,.0f", amount)
        } else {
            String.format("%,.2f", amount)
        }
        return "$currencyCode $formatted"
    }

    fun formatShortAmount(amount: Double): String {
        return if (amount >= 1_000_000) {
            String.format("%.1fM", amount / 1_000_000.0)
        } else if (amount >= 1_000) {
            String.format("%.1fK", amount / 1_000.0)
        } else {
            String.format("%,.0f", amount)
        }
    }
}
