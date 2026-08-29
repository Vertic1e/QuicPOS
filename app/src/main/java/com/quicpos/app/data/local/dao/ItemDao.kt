package com.quicpos.app.data.local.dao

import androidx.room.*
import com.quicpos.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE isActive = 1 ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isActive = 1 AND categoryId = :categoryId ORDER BY name ASC")
    fun getItemsByCategory(categoryId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE isActive = 1 AND isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteItems(): Flow<List<ItemEntity>>

    @Query("""
        SELECT * FROM items 
        WHERE isActive = 1 
        AND (name LIKE '%' || :query || '%' 
            OR sku LIKE '%' || :query || '%' 
            OR barcode LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchItems(query: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?

    @Query("SELECT * FROM items WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): ItemEntity?

    @Query("SELECT * FROM items WHERE sku = :sku AND isActive = 1 LIMIT 1")
    suspend fun getItemBySku(sku: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Query("UPDATE items SET stockQuantity = stockQuantity - :quantity WHERE id = :itemId AND trackStock = 1")
    suspend fun decrementStock(itemId: Long, quantity: Double)

    @Query("UPDATE items SET stockQuantity = :quantity, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun updateStockQuantity(itemId: Long, quantity: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE items SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :itemId")
    suspend fun setFavorite(itemId: Long, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE items SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteItem(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("SELECT COUNT(*) FROM items WHERE isActive = 1")
    fun getActiveItemCount(): Flow<Int>
}
