package com.quicpos.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String = "",
    val colorHex: String = "#4CAF50",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
