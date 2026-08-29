package com.quicpos.app.domain.model

data class Tax(
    val id: Long = 0,
    val name: String = "",
    val rate: Double = 0.0,
    val isInclusive: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate tax amount for a given subtotal.
     * - Exclusive: tax = subtotal × (rate / 100)
     * - Inclusive: tax = subtotal - (subtotal / (1 + rate / 100))
     */
    fun calculateTax(subtotal: Double): Double {
        return if (isInclusive) {
            subtotal - (subtotal / (1 + rate / 100.0))
        } else {
            subtotal * (rate / 100.0)
        }
    }

    val displayRate: String
        get() = "${String.format("%.1f", rate)}%"
}
