package com.quicpos.app.data.local.dao

import androidx.room.*
import com.quicpos.app.data.local.entity.ReceiptEntity
import com.quicpos.app.data.local.entity.ReceiptLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM receipts ORDER BY timestamp DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getReceiptsByDateRange(startTime: Long, endTime: Long): Flow<List<ReceiptEntity>>

    @Query("""
        SELECT DISTINCT r.* FROM receipts r
        LEFT JOIN receipt_lines rl ON r.receiptNumber = rl.receiptNumber
        WHERE r.receiptNumber LIKE '%' || :query || '%'
            OR rl.itemName LIKE '%' || :query || '%'
        ORDER BY r.timestamp DESC
    """)
    fun searchReceipts(query: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE receiptNumber = :receiptNumber")
    suspend fun getReceiptByNumber(receiptNumber: String): ReceiptEntity?

    @Query("SELECT * FROM receipt_lines WHERE receiptNumber = :receiptNumber ORDER BY id ASC")
    suspend fun getReceiptLines(receiptNumber: String): List<ReceiptLineEntity>

    @Query("SELECT * FROM receipt_lines WHERE receiptNumber = :receiptNumber ORDER BY id ASC")
    fun getReceiptLinesFlow(receiptNumber: String): Flow<List<ReceiptLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptLines(lines: List<ReceiptLineEntity>)

    @Transaction
    suspend fun insertReceiptWithLines(receipt: ReceiptEntity, lines: List<ReceiptLineEntity>) {
        insertReceipt(receipt)
        insertReceiptLines(lines)
    }

    @Query("UPDATE receipts SET status = :status WHERE receiptNumber = :receiptNumber")
    suspend fun updateReceiptStatus(receiptNumber: String, status: String)

    @Query("SELECT COUNT(*) FROM receipts")
    fun getReceiptCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM receipts WHERE status = 'COMPLETED' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getTotalSalesInRange(startTime: Long, endTime: Long): Double?

    @Query("SELECT * FROM receipts WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getReceiptsByDateRangeSync(startTime: Long, endTime: Long): List<ReceiptEntity>

    @Query("""
        SELECT rl.* FROM receipt_lines rl
        INNER JOIN receipts r ON rl.receiptNumber = r.receiptNumber
        WHERE r.status = 'COMPLETED' AND r.timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun getCompletedReceiptLinesInRange(startTime: Long, endTime: Long): List<ReceiptLineEntity>
}
