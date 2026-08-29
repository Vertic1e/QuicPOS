package com.quicpos.app.data.local.dao

import androidx.room.*
import com.quicpos.app.data.local.entity.TaxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxDao {

    @Query("SELECT * FROM taxes ORDER BY name ASC")
    fun getAllTaxes(): Flow<List<TaxEntity>>

    @Query("SELECT * FROM taxes WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveTaxes(): Flow<List<TaxEntity>>

    @Query("SELECT * FROM taxes WHERE id = :id")
    suspend fun getTaxById(id: Long): TaxEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTax(tax: TaxEntity): Long

    @Update
    suspend fun updateTax(tax: TaxEntity)

    @Delete
    suspend fun deleteTax(tax: TaxEntity)

    @Query("UPDATE taxes SET isActive = :isActive WHERE id = :id")
    suspend fun setTaxActive(id: Long, isActive: Boolean)
}
