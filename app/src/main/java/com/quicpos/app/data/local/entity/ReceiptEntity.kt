package com.quicpos.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipts",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class ReceiptEntity(
    @PrimaryKey
    val receiptNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val subtotalAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "CASH", // "CASH", "CARD", "OTHER"
    val cashTendered: Double = 0.0,
    val changeGiven: Double = 0.0,
    val customerName: String? = null,
    val status: String = "COMPLETED", // "COMPLETED", "REFUNDED", "VOIDED"
    val notes: String? = null,
    val posRegister: String = "POS 1"
)
