package com.focuslock.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(tableName = "sessions", indices = [Index(value = ["sessionKey"], unique = true)])
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int,
    val bagId: Long,
    val bagName: String,
    val isCompleted: Boolean,
    val distractionsBlockedCount: Int = 0,
    val dateString: String, // Local completion date
    @ColumnInfo(defaultValue = "''") val sessionKey: String = "",
    @ColumnInfo(defaultValue = "''") val task: String = "",
    @ColumnInfo(defaultValue = "''") val copingPlan: String = "",
    @ColumnInfo(defaultValue = "''") val exitReason: String = "",
    @ColumnInfo(defaultValue = "0") val actualFocusMillis: Long = 0
)
