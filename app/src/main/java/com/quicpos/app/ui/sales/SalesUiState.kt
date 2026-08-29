package com.quicpos.app.ui.sales

import com.quicpos.app.domain.model.Category
import com.quicpos.app.domain.model.Item
import com.quicpos.app.domain.model.TicketLine

data class SalesUiState(
    val items: List<Item> = emptyList(),
    val categories: List<Category> = emptyList(),
    val ticketLines: List<TicketLine> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val currencyCode: String = "KHR",
    val currencySymbol: String = "៛",
    val layoutMode: String = "GRID",
    val isLoading: Boolean = true,
    val businessName: String = "QuicPOS Store",
    val posRegisterName: String = "POS 1",
    val isDualCurrencyEnabled: Boolean = true,
    val secondaryCurrencyCode: String = "USD",
    val secondaryCurrencySymbol: String = "$",
    val exchangeRate: Double = 4000.0,
    val customerName: String? = null,
    val itemSize: String = "MEDIUM",
    val gridColumns: Int = 3,
    val fontSizeScale: Float = 1.0f
) {
    val ticketItemCount: Int
        get() = ticketLines.sumOf { it.quantity.toInt() }

    val subtotal: Double
        get() = ticketLines.sumOf { it.calculatedLineTotal }

    val totalDiscount: Double
        get() = ticketLines.sumOf { it.discountAmount }

    val grandTotal: Double
        get() = subtotal // Tax is added at charge time

    val isTicketEmpty: Boolean
        get() = ticketLines.isEmpty()

    fun convertToSecondary(amountPrimary: Double): Double {
        if (exchangeRate <= 0.0) return 0.0
        return if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
            amountPrimary / exchangeRate
        } else if (currencyCode == "USD" && secondaryCurrencyCode == "KHR") {
            amountPrimary * exchangeRate
        } else {
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

    fun formatPrimary(amount: Double): String {
        val formatted = if (amount == amount.toLong().toDouble()) {
            String.format("%,.0f", amount)
        } else {
            String.format("%,.2f", amount)
        }
        return "$currencyCode $formatted"
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

sealed class SalesIntent {
    data class AddItemToTicket(val item: Item) : SalesIntent()
    data class RemoveTicketLine(val index: Int) : SalesIntent()
    data class UpdateLineQuantity(val index: Int, val quantity: Double) : SalesIntent()
    data class SelectCategory(val categoryId: Long?) : SalesIntent()
    data class SearchItems(val query: String) : SalesIntent()
    data object ClearTicket : SalesIntent()
    data class ApplyLineDiscount(val index: Int, val amount: Double) : SalesIntent()
    data class SetCustomerName(val name: String?) : SalesIntent()
}
