package com.quicpos.app.domain.model

data class Item(
    val id: Long = 0,
    val name: String = "",
    val categoryId: Long? = null,
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val sku: String = "",
    val barcode: String? = null,
    val soldBy: String = "EACH",
    val trackStock: Boolean = false,
    val stockQuantity: Double = 0.0,
    val variablePrice: Boolean = false,
    val imageUrl: String? = null,
    val colorHex: String? = null,
    val isFavorite: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = trackStock && stockQuantity <= 5.0 && stockQuantity > 0.0

    val isOutOfStock: Boolean
        get() = trackStock && stockQuantity <= 0.0

    val displayPrice: String
        get() = formatPrice(price)

    companion object {
        fun formatPrice(amount: Double): String {
            return if (amount == amount.toLong().toDouble()) {
                String.format("%,.0f", amount)
            } else {
                String.format("%,.2f", amount)
            }
        }
    }
}
