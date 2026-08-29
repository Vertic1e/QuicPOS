package com.quicpos.app.printing

import android.graphics.Bitmap
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.domain.model.TicketLine

/**
 * Formats receipt data into ESC/POS byte commands for thermal printing.
 * Supports 32 (58mm) and 48 (80mm) character width printers and graphic logos.
 */
class ReceiptFormatter(
    private val charWidth: Int = 32 // 32 for 58mm, 48 for 80mm printers
) {
    /**
     * Format a complete receipt into ESC/POS byte data.
     */
    fun formatReceipt(
        receipt: Receipt,
        lines: List<TicketLine>,
        businessName: String,
        headerText: String,
        footerText: String,
        currencyCode: String,
        isDualCurrencyEnabled: Boolean = false,
        secondaryCurrencyCode: String = "USD",
        exchangeRate: Double = 4000.0,
        logoBitmap: Bitmap? = null,
        customInitCmd: String? = null,
        customCutCmd: String? = null
    ): ByteArray {
        val builder = mutableListOf<ByteArray>()

        // Initialize
        val initBytes = customInitCmd?.let { EscPosCommands.hexToBytes(it) }?.takeIf { it.isNotEmpty() }
            ?: EscPosCommands.INIT
        builder.add(initBytes)

        // Logo Image (if provided)
        if (logoBitmap != null) {
            val maxLogoWidth = if (charWidth >= 48) 512 else 360
            val logoBytes = EscPosCommands.bitmapToRasterBitImage(logoBitmap, maxLogoWidth)
            builder.add(logoBytes)
            builder.add(EscPosCommands.LF)
        }

        // Header - Business Name (centered, large)
        builder.add(EscPosCommands.ALIGN_CENTER)
        builder.add(EscPosCommands.TEXT_DOUBLE_SIZE)
        builder.add(EscPosCommands.BOLD_ON)
        builder.add(EscPosCommands.textToBytes(businessName))
        builder.add(EscPosCommands.LF)
        builder.add(EscPosCommands.TEXT_NORMAL)
        builder.add(EscPosCommands.BOLD_OFF)

        // Header Text
        if (headerText.isNotBlank()) {
            builder.add(EscPosCommands.textToBytes(headerText))
            builder.add(EscPosCommands.LF)
        }

        if (receipt.isRefunded) {
            builder.add(EscPosCommands.BOLD_ON)
            builder.add(EscPosCommands.textToBytes("*** REFUND RECEIPT ***"))
            builder.add(EscPosCommands.LF)
            builder.add(EscPosCommands.BOLD_OFF)
        } else if (receipt.status == "PARTIALLY_REFUNDED") {
            builder.add(EscPosCommands.BOLD_ON)
            builder.add(EscPosCommands.textToBytes("*** PARTIALLY REFUNDED ***"))
            builder.add(EscPosCommands.LF)
            builder.add(EscPosCommands.BOLD_OFF)
        }

        builder.add(EscPosCommands.LF)

        // Receipt Info (left aligned)
        builder.add(EscPosCommands.ALIGN_LEFT)
        builder.add(EscPosCommands.textToBytes("Receipt: ${receipt.receiptNumber}"))
        builder.add(EscPosCommands.LF)
        builder.add(EscPosCommands.textToBytes("Date: ${receipt.formattedDateTime}"))
        builder.add(EscPosCommands.LF)
        builder.add(EscPosCommands.textToBytes("Register: ${receipt.posRegister}"))
        builder.add(EscPosCommands.LF)
        if (receipt.customerName != null) {
            builder.add(EscPosCommands.textToBytes("Customer: ${receipt.customerName}"))
            builder.add(EscPosCommands.LF)
        }

        // Separator
        builder.add(EscPosCommands.textToBytes(separator()))
        builder.add(EscPosCommands.LF)

        // Line Items
        lines.forEach { line ->
            val qty = line.displayQuantity
            val name = line.itemName
            val total = formatAmount(line.calculatedLineTotal, currencyCode)

            // Format: qty x name ... total
            val qtyName = "$qty× $name"
            val lineText = padRight(qtyName, total)
            builder.add(EscPosCommands.textToBytes(lineText))
            builder.add(EscPosCommands.LF)

            // Unit price if qty > 1
            if (line.quantity > 1) {
                val unitPriceText = "   @ ${formatAmount(line.unitPrice, currencyCode)}"
                builder.add(EscPosCommands.textToBytes(unitPriceText))
                builder.add(EscPosCommands.LF)
            }

            // Discount
            if (line.discountAmount > 0) {
                val discountText = "   Discount: -${formatAmount(line.discountAmount, currencyCode)}"
                builder.add(EscPosCommands.textToBytes(discountText))
                builder.add(EscPosCommands.LF)
            }
        }

        // Separator
        builder.add(EscPosCommands.textToBytes(separator()))
        builder.add(EscPosCommands.LF)

        // Totals
        builder.add(EscPosCommands.textToBytes(
            padRight("Subtotal:", formatAmount(receipt.subtotalAmount, currencyCode))
        ))
        builder.add(EscPosCommands.LF)

        if (receipt.discountAmount > 0) {
            builder.add(EscPosCommands.textToBytes(
                padRight("Discount:", "-${formatAmount(receipt.discountAmount, currencyCode)}")
            ))
            builder.add(EscPosCommands.LF)
        }

        if (receipt.taxAmount > 0) {
            builder.add(EscPosCommands.textToBytes(
                padRight("Tax:", formatAmount(receipt.taxAmount, currencyCode))
            ))
            builder.add(EscPosCommands.LF)
        }

        builder.add(EscPosCommands.textToBytes(separator()))
        builder.add(EscPosCommands.LF)

        // Grand Total (bold, larger)
        builder.add(EscPosCommands.TEXT_DOUBLE_HEIGHT)
        builder.add(EscPosCommands.BOLD_ON)
        builder.add(EscPosCommands.textToBytes(
            padRight("TOTAL:", formatAmount(receipt.totalAmount, currencyCode))
        ))
        builder.add(EscPosCommands.LF)
        builder.add(EscPosCommands.TEXT_NORMAL)
        builder.add(EscPosCommands.BOLD_OFF)

        if (isDualCurrencyEnabled && exchangeRate > 0) {
            val secAmount = if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
                receipt.totalAmount / exchangeRate
            } else {
                receipt.totalAmount * exchangeRate
            }
            builder.add(EscPosCommands.textToBytes(
                padRight("TOTAL ($secondaryCurrencyCode):", formatAmount(secAmount, secondaryCurrencyCode))
            ))
            builder.add(EscPosCommands.LF)
        }

        // Payment info
        builder.add(EscPosCommands.textToBytes(
            padRight("Payment:", receipt.paymentMethod)
        ))
        builder.add(EscPosCommands.LF)

        if (receipt.paymentMethod == "CASH" && receipt.cashTendered > 0) {
            builder.add(EscPosCommands.textToBytes(
                padRight("Tendered:", formatAmount(receipt.cashTendered, currencyCode))
            ))
            builder.add(EscPosCommands.LF)
            builder.add(EscPosCommands.textToBytes(
                padRight("Change:", formatAmount(receipt.changeGiven, currencyCode))
            ))
            builder.add(EscPosCommands.LF)
        }

        builder.add(EscPosCommands.LF)

        // Footer
        builder.add(EscPosCommands.ALIGN_CENTER)
        if (footerText.isNotBlank()) {
            builder.add(EscPosCommands.textToBytes(footerText))
            builder.add(EscPosCommands.LF)
        }

        // Feed and cut
        builder.add(EscPosCommands.feedLines(4))
        val cutBytes = customCutCmd?.let { EscPosCommands.hexToBytes(it) }?.takeIf { it.isNotEmpty() }
            ?: EscPosCommands.CUT_PAPER_PARTIAL
        builder.add(cutBytes)

        return EscPosCommands.buildCommand(*builder.toTypedArray())
    }

    private fun separator(): String = "─".repeat(charWidth)

    private fun padRight(left: String, right: String): String {
        val spaces = charWidth - left.length - right.length
        return if (spaces > 0) {
            "$left${" ".repeat(spaces)}$right"
        } else {
            "$left $right"
        }
    }

    private fun formatAmount(amount: Double, currencyCode: String): String {
        val formatted = if (amount == amount.toLong().toDouble()) {
            String.format("%,.0f", amount)
        } else {
            String.format("%,.2f", amount)
        }
        return "$currencyCode $formatted"
    }
}
