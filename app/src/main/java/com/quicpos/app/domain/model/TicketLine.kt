package com.quicpos.app.domain.model

data class TicketLine(
    val itemId: Long = 0,
    val itemName: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val lineTotal: Double = unitPrice * quantity - discountAmount,
    val modifiers: List<AppliedModifier> = emptyList(),
    val notes: String? = null,
    val colorHex: String? = null
) {
    val displayQuantity: String
        get() = if (quantity == quantity.toLong().toDouble()) {
            quantity.toLong().toString()
        } else {
            String.format("%.2f", quantity)
        }

    val modifiersTotal: Double
        get() = modifiers.sumOf { it.price }

    val effectiveUnitPrice: Double
        get() = unitPrice + modifiersTotal

    val calculatedLineTotal: Double
        get() = effectiveUnitPrice * quantity - discountAmount

    fun withUpdatedQuantity(newQty: Double): TicketLine =
        copy(
            quantity = newQty,
            lineTotal = (unitPrice + modifiersTotal) * newQty - discountAmount
        )

    fun withAddedModifier(modifier: AppliedModifier): TicketLine {
        val newModifiers = modifiers + modifier
        val newModTotal = newModifiers.sumOf { it.price }
        return copy(
            modifiers = newModifiers,
            lineTotal = (unitPrice + newModTotal) * quantity - discountAmount
        )
    }

    fun withDiscount(discount: Double): TicketLine =
        copy(
            discountAmount = discount,
            lineTotal = effectiveUnitPrice * quantity - discount
        )
}

data class AppliedModifier(
    val id: Long = 0,
    val name: String = "",
    val price: Double = 0.0
)
