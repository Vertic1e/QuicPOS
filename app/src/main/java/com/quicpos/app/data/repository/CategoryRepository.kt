package com.quicpos.app.data.repository

import com.quicpos.app.data.local.dao.CategoryDao
import com.quicpos.app.data.local.entity.CategoryEntity
import com.quicpos.app.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { entities -> entities.map { it.toDomain() } }

    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    suspend fun saveCategory(category: Category): Long {
        val entity = category.toEntity()
        return if (category.id == 0L) {
            val maxSort = categoryDao.getMaxSortOrder() ?: -1
            categoryDao.insertCategory(entity.copy(sortOrder = maxSort + 1))
        } else {
            categoryDao.updateCategory(entity)
            category.id
        }
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    fun getCategoryCount(): Flow<Int> = categoryDao.getCategoryCount()

    companion object {
        fun CategoryEntity.toDomain(): Category = Category(
            id = id,
            name = name,
            colorHex = colorHex,
            sortOrder = sortOrder,
            createdAt = createdAt
        )

        fun Category.toEntity(): CategoryEntity = CategoryEntity(
            id = id,
            name = name,
            colorHex = colorHex,
            sortOrder = sortOrder,
            createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt
        )
    }
}
