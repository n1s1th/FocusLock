package com.focuslock.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focuslock.app.data.database.entities.ReelBlockAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelBlockAppDao {
    @Query("SELECT * FROM reel_block_apps")
    fun getAllReelBlockApps(): Flow<List<ReelBlockAppEntity>>

    @Query("SELECT * FROM reel_block_apps WHERE packageName = :packageName")
    suspend fun getReelBlockApp(packageName: String): ReelBlockAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: ReelBlockAppEntity)

    @Update
    suspend fun updateApp(app: ReelBlockAppEntity)

    @Delete
    suspend fun deleteApp(app: ReelBlockAppEntity)
}
