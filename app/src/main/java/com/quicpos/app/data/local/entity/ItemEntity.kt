package com.quicpos.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["barcode"]),
        Index(value = ["sku"], unique = true),
        Index(value = ["name"])
    ]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long? = null,
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val sku: String = "",
    val barcode: String? = null,
    val soldBy: String = "EACH", // "EACH" or "WEIGHT"
    val trackStock: Boolean = false,
    val stockQuantity: Double = 0.0,
    val variablePrice: Boolean = false,
    val imageUrl: String? = null,
    val colorHex: String? = null,
    val isFavorite: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
