package com.quicpos.app.data.repository

import com.quicpos.app.data.local.dao.ReceiptDao
import com.quicpos.app.data.local.dao.ItemDao
import com.quicpos.app.data.local.entity.ReceiptEntity
import com.quicpos.app.data.local.entity.ReceiptLineEntity
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.domain.model.TicketLine
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val itemDao: ItemDao,
    private val settingsRepository: SettingsRepository,
    private val gson: Gson
) {
    fun getAllReceipts(): Flow<List<Receipt>> =
        receiptDao.getAllReceipts().map { entities -> entities.map { it.toDomain() } }

    fun getReceiptsByDateRange(startTime: Long, endTime: Long): Flow<List<Receipt>> =
        receiptDao.getReceiptsByDateRange(startTime, endTime)
            .map { entities -> entities.map { it.toDomain() } }

    fun searchReceipts(query: String): Flow<List<Receipt>> =
        receiptDao.searchReceipts(query).map { entities -> entities.map { it.toDomain() } }

    suspend fun getReceiptWithLines(receiptNumber: String): Pair<Receipt, List<TicketLine>>? {
        val receipt = receiptDao.getReceiptByNumber(receiptNumber)?.toDomain() ?: return null
        val lines = receiptDao.getReceiptLines(receiptNumber).map { it.toTicketLine() }
        return Pair(receipt, lines)
    }

    suspend fun createReceipt(
        ticketLines: List<TicketLine>,
        subtotal: Double,
        taxAmount: Double,
        discountAmount: Double,
        totalAmount: Double,
        paymentMethod: String,
        cashTendered: Double = 0.0,
        changeGiven: Double = 0.0,
        customerName: String? = null
    ): String {
        val counter = settingsRepository.getAndIncrementReceiptCounter()
        val register = settingsRepository.getPosRegisterName()
            .replace("POS ", "")
            .replace(" ", "")
        val receiptNumber = "#${register}-${counter.toString().padStart(4, '0')}"

        val finalCashTendered = if (cashTendered <= 0.0) totalAmount else cashTendered
        val finalChangeGiven = maxOf(0.0, finalCashTendered - totalAmount)

        val receipt = ReceiptEntity(
            receiptNumber = receiptNumber,
            timestamp = System.currentTimeMillis(),
            subtotalAmount = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            cashTendered = finalCashTendered,
            changeGiven = finalChangeGiven,
            customerName = customerName,
            status = "COMPLETED",
            posRegister = settingsRepository.getPosRegisterName()
        )

        val lines = ticketLines.map { line ->
            ReceiptLineEntity(
                receiptNumber = receiptNumber,
                itemId = line.itemId,
                itemName = line.itemName,
                quantity = line.quantity,
                unitPrice = line.unitPrice,
                discountAmount = line.discountAmount,
                lineTotal = line.lineTotal,
                modifiersJson = if (line.modifiers.isNotEmpty()) gson.toJson(line.modifiers) else null,
                notes = line.notes
            )
        }

        receiptDao.insertReceiptWithLines(receipt, lines)

        // Decrement stock for tracked items
        ticketLines.forEach { line ->
            itemDao.decrementStock(line.itemId, line.quantity)
        }

        return receiptNumber
    }

    suspend fun refundReceipt(receiptNumber: String) {
        receiptDao.updateReceiptStatus(receiptNumber, "REFUNDED")
        // Restore full stock
        val lines = receiptDao.getReceiptLines(receiptNumber)
        lines.forEach { line ->
            itemDao.decrementStock(line.itemId, -line.quantity) // negative = add back
        }
    }

    suspend fun refundPartialReceipt(
        receiptNumber: String,
        refundedItems: Map<Long, Double> // itemId -> quantityToRefund
    ) {
        val (receipt, lines) = getReceiptWithLines(receiptNumber) ?: return
        var totalRefundedAmount = 0.0
        var allItemsFullyRefunded = true

        lines.forEach { line ->
            val refundQty = refundedItems[line.itemId] ?: 0.0
            if (refundQty > 0) {
                val clampedQty = minOf(refundQty, line.quantity)
                itemDao.decrementStock(line.itemId, -clampedQty) // Restore inventory stock
                totalRefundedAmount += clampedQty * line.unitPrice
            }
            val remainingQty = line.quantity - (refundedItems[line.itemId] ?: 0.0)
            if (remainingQty > 0.001) {
                allItemsFullyRefunded = false
            }
        }

        val newStatus = if (allItemsFullyRefunded) "REFUNDED" else "PARTIALLY_REFUNDED"
        val remainingTotal = maxOf(0.0, receipt.totalAmount - totalRefundedAmount)
        receiptDao.updateReceiptStatusAndTotal(receiptNumber, newStatus, remainingTotal)
    }

    fun getReceiptCount(): Flow<Int> = receiptDao.getReceiptCount()

    suspend fun getReceiptsByDateRangeSync(startTime: Long, endTime: Long): List<Receipt> =
        receiptDao.getReceiptsByDateRangeSync(startTime, endTime).map { it.toDomain() }

    suspend fun getCompletedReceiptLinesInRange(startTime: Long, endTime: Long): List<ReceiptLineEntity> =
        receiptDao.getCompletedReceiptLinesInRange(startTime, endTime)

    companion object {
        fun ReceiptEntity.toDomain(): Receipt = Receipt(
            receiptNumber = receiptNumber,
            timestamp = timestamp,
            subtotalAmount = subtotalAmount,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            cashTendered = cashTendered,
            changeGiven = changeGiven,
            customerName = customerName,
            status = status,
            notes = notes,
            posRegister = posRegister
        )

        fun ReceiptLineEntity.toTicketLine(): TicketLine = TicketLine(
            itemId = itemId,
            itemName = itemName,
            quantity = quantity,
            unitPrice = unitPrice,
            discountAmount = discountAmount,
            lineTotal = lineTotal,
            notes = notes
        )
    }
}
