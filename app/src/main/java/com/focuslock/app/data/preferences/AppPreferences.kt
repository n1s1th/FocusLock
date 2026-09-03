package com.focuslock.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("FocusLockPrefs", Context.MODE_PRIVATE)

    private val _isSessionActiveFlow = MutableStateFlow(isSessionActive())
    val isSessionActiveFlow: StateFlow<Boolean> = _isSessionActiveFlow.asStateFlow()

    fun isSessionActive(): Boolean {
        val endTime = prefs.getLong(KEY_SESSION_END_MILLIS, 0L)
        return endTime > System.currentTimeMillis()
    }

    fun getSessionEndTimeMillis(): Long = prefs.getLong(KEY_SESSION_END_MILLIS, 0L)

    fun getSessionTotalDurationMinutes(): Int = prefs.getInt(KEY_SESSION_TOTAL_MINUTES, 25)

    fun getActiveBagId(): Long = prefs.getLong(KEY_ACTIVE_BAG_ID, 1L)

    fun getActiveBagName(): String = prefs.getString(KEY_ACTIVE_BAG_NAME, "Deep Focus") ?: "Deep Focus"

    fun getDistractionsBlockedCount(): Int = prefs.getInt(KEY_DISTRACTIONS_BLOCKED, 0)

    fun isBlockRecentsEnabled(): Boolean = prefs.getBoolean(KEY_BLOCK_RECENTS, true)

    fun isBlockShadeEnabled(): Boolean = prefs.getBoolean(KEY_BLOCK_SHADE, true)

    fun isHapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)

    fun getCurrentStreak(): Int = prefs.getInt(KEY_CURRENT_STREAK, 0)

    fun getLastActiveDate(): String = prefs.getString(KEY_LAST_ACTIVE_DATE, "") ?: ""

    fun startSession(endTimeMillis: Long, durationMinutes: Int, bagId: Long, bagName: String) {
        prefs.edit()
            .putLong(KEY_SESSION_END_MILLIS, endTimeMillis)
            .putInt(KEY_SESSION_TOTAL_MINUTES, durationMinutes)
            .putLong(KEY_ACTIVE_BAG_ID, bagId)
            .putString(KEY_ACTIVE_BAG_NAME, bagName)
            .putInt(KEY_DISTRACTIONS_BLOCKED, 0)
            .apply()
        _isSessionActiveFlow.value = true
        recordStreakActivity()
    }

    fun stopSession() {
        prefs.edit()
            .putLong(KEY_SESSION_END_MILLIS, 0L)
            .apply()
        _isSessionActiveFlow.value = false
    }

    fun incrementDistractionsBlocked(): Int {
        val current = getDistractionsBlockedCount() + 1
        prefs.edit().putInt(KEY_DISTRACTIONS_BLOCKED, current).apply()
        return current
    }

    fun setBlockRecents(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_RECENTS, enabled).apply()
    }

    fun setBlockShade(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_SHADE, enabled).apply()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    private fun recordStreakActivity() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lastDate = getLastActiveDate()

        if (lastDate == today) {
            return
        }

        val streak = getCurrentStreak()
        val isConsecutive = isYesterday(lastDate)

        val newStreak = if (isConsecutive || lastDate.isEmpty()) streak + 1 else 1

        prefs.edit()
            .putString(KEY_LAST_ACTIVE_DATE, today)
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .apply()
    }

    private fun isYesterday(dateStr: String): Boolean {
        if (dateStr.isEmpty()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val lastDate = sdf.parse(dateStr) ?: return false
            val diff = System.currentTimeMillis() - lastDate.time
            val diffDays = diff / (1000 * 60 * 60 * 24)
            diffDays in 1..2
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val KEY_SESSION_END_MILLIS = "session_end_millis"
        private const val KEY_SESSION_TOTAL_MINUTES = "session_total_minutes"
        private const val KEY_ACTIVE_BAG_ID = "active_bag_id"
        private const val KEY_ACTIVE_BAG_NAME = "active_bag_name"
        private const val KEY_DISTRACTIONS_BLOCKED = "distractions_blocked"
        private const val KEY_BLOCK_RECENTS = "block_recents"
        private const val KEY_BLOCK_SHADE = "block_shade"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
    }
}
