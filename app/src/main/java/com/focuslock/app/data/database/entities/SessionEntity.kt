package com.focuslock.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
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
    val dateString: String // e.g. "2026-09-03" for easy day grouping
)
