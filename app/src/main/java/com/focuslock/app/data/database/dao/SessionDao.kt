package com.focuslock.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focuslock.app.data.database.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE dateString = :dateString ORDER BY startTime DESC")
    fun getSessionsForDateFlow(dateString: String): Flow<List<SessionEntity>>

    @Query("SELECT SUM(actualDurationMinutes) FROM sessions WHERE dateString = :dateString AND isCompleted = 1")
    fun getTotalFocusMinutesForDate(dateString: String): Flow<Int?>

    @Query("SELECT SUM(actualDurationMinutes) FROM sessions WHERE isCompleted = 1")
    fun getTotalLifetimeFocusMinutes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM sessions WHERE isCompleted = 1")
    fun getTotalCompletedSessions(): Flow<Int>

    @Query("SELECT DISTINCT dateString FROM sessions WHERE isCompleted = 1 ORDER BY dateString DESC")
    fun getActiveSessionDatesFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)
}
