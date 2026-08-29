package com.quicpos.app.data.repository

import com.quicpos.app.data.local.dao.ItemDao
import com.quicpos.app.data.local.entity.ItemEntity
import com.quicpos.app.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao
) {
    fun getAllItems(): Flow<List<Item>> =
        itemDao.getAllItems().map { entities -> entities.map { it.toDomain() } }

    fun getItemsByCategory(categoryId: Long): Flow<List<Item>> =
        itemDao.getItemsByCategory(categoryId).map { entities -> entities.map { it.toDomain() } }

    fun getFavoriteItems(): Flow<List<Item>> =
        itemDao.getFavoriteItems().map { entities -> entities.map { it.toDomain() } }

    fun searchItems(query: String): Flow<List<Item>> =
        itemDao.searchItems(query).map { entities -> entities.map { it.toDomain() } }

    suspend fun getItemById(id: Long): Item? =
        itemDao.getItemById(id)?.toDomain()

    suspend fun getItemByBarcode(barcode: String): Item? =
        itemDao.getItemByBarcode(barcode)?.toDomain()

    suspend fun saveItem(item: Item): Long {
        val entity = item.toEntity()
        return if (item.id == 0L) {
            itemDao.insertItem(entity)
        } else {
            itemDao.updateItem(entity.copy(updatedAt = System.currentTimeMillis()))
            item.id
        }
    }

    suspend fun decrementStock(itemId: Long, quantity: Double) {
        itemDao.decrementStock(itemId, quantity)
    }

    suspend fun updateStockQuantity(itemId: Long, quantity: Double) {
        itemDao.updateStockQuantity(itemId, quantity)
    }

    suspend fun setFavorite(itemId: Long, isFavorite: Boolean) {
        itemDao.setFavorite(itemId, isFavorite)
    }

    suspend fun deleteItem(itemId: Long) {
        itemDao.softDeleteItem(itemId)
    }

    fun getActiveItemCount(): Flow<Int> = itemDao.getActiveItemCount()

    companion object {
        fun ItemEntity.toDomain(): Item = Item(
            id = id,
            name = name,
            categoryId = categoryId,
            price = price,
            cost = cost,
            sku = sku,
            barcode = barcode,
            soldBy = soldBy,
            trackStock = trackStock,
            stockQuantity = stockQuantity,
            variablePrice = variablePrice,
            imageUrl = imageUrl,
            colorHex = colorHex,
            isFavorite = isFavorite,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        fun Item.toEntity(): ItemEntity = ItemEntity(
            id = id,
            name = name,
            categoryId = categoryId,
            price = price,
            cost = cost,
            sku = sku,
            barcode = barcode,
            soldBy = soldBy,
            trackStock = trackStock,
            stockQuantity = stockQuantity,
            variablePrice = variablePrice,
            imageUrl = imageUrl,
            colorHex = colorHex,
            isFavorite = isFavorite,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
