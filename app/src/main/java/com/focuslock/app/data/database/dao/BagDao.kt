package com.focuslock.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focuslock.app.data.database.entities.BagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BagDao {
    @Query("SELECT * FROM bags ORDER BY isDefault DESC, createdAt ASC")
    fun getAllBagsFlow(): Flow<List<BagEntity>>

    @Query("SELECT * FROM bags ORDER BY isDefault DESC, createdAt ASC")
    suspend fun getAllBags(): List<BagEntity>

    @Query("SELECT * FROM bags WHERE id = :id LIMIT 1")
    suspend fun getBagById(id: Long): BagEntity?

    @Query("SELECT * FROM bags WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultBag(): BagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBag(bag: BagEntity): Long

    @Update
    suspend fun updateBag(bag: BagEntity)

    @Delete
    suspend fun deleteBag(bag: BagEntity)

    @Query("UPDATE bags SET isDefault = 0 WHERE id != :defaultBagId")
    suspend fun clearOtherDefaults(defaultBagId: Long)
}
