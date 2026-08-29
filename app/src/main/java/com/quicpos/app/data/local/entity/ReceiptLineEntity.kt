package com.quicpos.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_lines",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["receiptNumber"],
            childColumns = ["receiptNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["receiptNumber"]),
        Index(value = ["itemId"])
    ]
)
data class ReceiptLineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val itemId: Long,
    val itemName: String,
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val lineTotal: Double = 0.0,
    val modifiersJson: String? = null, // JSON array of applied modifiers
    val notes: String? = null
)
