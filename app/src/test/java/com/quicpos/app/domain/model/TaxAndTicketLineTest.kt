package com.quicpos.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaxAndTicketLineTest {

    @Test
    fun testExclusiveTaxCalculation() {
        val tax = Tax(name = "VAT", rate = 10.0, isInclusive = false)
        val taxAmount = tax.calculateTax(100.0)
        assertEquals(10.0, taxAmount, 0.001)
    }

    @Test
    fun testInclusiveTaxCalculation() {
        val tax = Tax(name = "VAT", rate = 10.0, isInclusive = true)
        // Inclusive tax for subtotal 110 at 10% should be 110 - (110 / 1.1) = 10.0
        val taxAmount = tax.calculateTax(110.0)
        assertEquals(10.0, taxAmount, 0.001)
    }

    @Test
    fun testTicketLineModifiersAndDiscount() {
        val line = TicketLine(
            itemId = 1L,
            itemName = "Espresso",
            quantity = 2.0,
            unitPrice = 3.50
        )
        assertEquals(7.00, line.calculatedLineTotal, 0.001)

        val modifier = AppliedModifier(id = 1L, name = "Extra Shot", price = 0.75)
        val lineWithMod = line.withAddedModifier(modifier)
        // (3.50 + 0.75) * 2 = 8.50
        assertEquals(8.50, lineWithMod.calculatedLineTotal, 0.001)

        val lineWithDiscount = lineWithMod.withDiscount(1.00)
        // 8.50 - 1.00 = 7.50
        assertEquals(7.50, lineWithDiscount.calculatedLineTotal, 0.001)
    }

    @Test
    fun testTicketLineQuantityUpdate() {
        val line = TicketLine(
            itemId = 1L,
            itemName = "Iced Latte",
            quantity = 1.0,
            unitPrice = 4.00
        )
        val updatedLine = line.withUpdatedQuantity(3.0)
        assertEquals(3.0, updatedLine.quantity, 0.001)
        assertEquals(12.00, updatedLine.lineTotal, 0.001)
        assertEquals("3", updatedLine.displayQuantity)
    }

    @Test
    fun testReceiptStatus() {
        val normalReceipt = Receipt(status = "COMPLETED")
        assertFalse(normalReceipt.isRefunded)
        assertFalse(normalReceipt.isVoided)

        val refundedReceipt = Receipt(status = "REFUNDED")
        assertTrue(refundedReceipt.isRefunded)
        assertFalse(refundedReceipt.isVoided)

        val voidedReceipt = Receipt(status = "VOIDED")
        assertFalse(voidedReceipt.isRefunded)
        assertTrue(voidedReceipt.isVoided)
    }
}
