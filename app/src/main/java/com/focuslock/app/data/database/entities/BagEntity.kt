package com.focuslock.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bags")
data class BagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "Work",
    val colorHex: String = "#6366F1",
    val allowedPackages: List<String> = emptyList(),
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
