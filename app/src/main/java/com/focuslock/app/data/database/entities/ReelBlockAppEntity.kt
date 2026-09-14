package com.focuslock.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reel_block_apps")
data class ReelBlockAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val blockReels: Boolean = true,
    val blockStories: Boolean = false,
    val blockMarketplace: Boolean = false,
    val blockGaming: Boolean = false,
    val dailyLimitMinutes: Int = 0 // 0 means no limit
)
