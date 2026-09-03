package com.focuslock.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetBagId: Long,
    val startHour: Int,
    val startMinute: Int,
    val durationMinutes: Int,
    val activeDaysMask: Int = 127, // Bitmask for Mon-Sun (1..7 all enabled by default = 0b1111111)
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
