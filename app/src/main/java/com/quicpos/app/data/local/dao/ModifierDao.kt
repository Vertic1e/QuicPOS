package com.quicpos.app.data.local.dao

import androidx.room.*
import com.quicpos.app.data.local.entity.ModifierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModifierDao {

    @Query("SELECT * FROM modifiers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllModifiers(): Flow<List<ModifierEntity>>

    @Query("SELECT * FROM modifiers WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name ASC")
    fun getModifiersByCategory(categoryId: Long): Flow<List<ModifierEntity>>

    @Query("SELECT * FROM modifiers WHERE id = :id")
    suspend fun getModifierById(id: Long): ModifierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModifier(modifier: ModifierEntity): Long

    @Update
    suspend fun updateModifier(modifier: ModifierEntity)

    @Query("UPDATE modifiers SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteModifier(id: Long)

    @Delete
    suspend fun deleteModifier(modifier: ModifierEntity)
}
