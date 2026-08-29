package com.quicpos.app.domain.model

data class Modifier(
    val id: Long = 0,
    val name: String = "",
    val price: Double = 0.0,
    val categoryId: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
