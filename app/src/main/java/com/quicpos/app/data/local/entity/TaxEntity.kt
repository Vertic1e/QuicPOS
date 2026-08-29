package com.quicpos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxes")
data class TaxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rate: Double = 0.0, // Percentage, e.g. 10.0 for 10%
    val isInclusive: Boolean = false, // true = tax included in price, false = added on top
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
