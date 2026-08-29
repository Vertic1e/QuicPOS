package com.quicpos.app.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Receipt(
    val receiptNumber: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val subtotalAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "CASH",
    val cashTendered: Double = 0.0,
    val changeGiven: Double = 0.0,
    val customerName: String? = null,
    val status: String = "COMPLETED",
    val notes: String? = null,
    val posRegister: String = "POS 1"
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDateTime: String
        get() = "$formattedDate $formattedTime"

    val dateGroupKey: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val displayDateGroup: String
        get() {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date(System.currentTimeMillis() - 86400000)
            )
            return when (dateGroupKey) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> formattedDate
            }
        }

    val paymentIcon: String
        get() = when (paymentMethod) {
            "CASH" -> "💵"
            "CARD" -> "💳"
            else -> "📱"
        }

    val isRefunded: Boolean get() = status == "REFUNDED"
    val isVoided: Boolean get() = status == "VOIDED"
}
