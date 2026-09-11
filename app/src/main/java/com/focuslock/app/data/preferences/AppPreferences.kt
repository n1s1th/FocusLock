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

    fun setSelectedBagId(id: Long) {
        prefs.edit().putLong(KEY_SELECTED_BAG_ID, id).apply()
    }

    fun getSelectedBagId(): Long = prefs.getLong(KEY_SELECTED_BAG_ID, 1L)

    fun setSelectedBagIndex(index: Int) {
        prefs.edit().putInt(KEY_SELECTED_BAG_INDEX, index).apply()
    }

    fun getSelectedBagIndex(): Int = prefs.getInt(KEY_SELECTED_BAG_INDEX, 0)

    fun getDistractionsBlockedCount(): Int = prefs.getInt(KEY_DISTRACTIONS_BLOCKED, 0)

    fun isBlockRecentsEnabled(): Boolean = prefs.getBoolean(KEY_BLOCK_RECENTS, true)

    fun isBlockShadeEnabled(): Boolean = prefs.getBoolean(KEY_BLOCK_SHADE, true)

    fun isHapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)

    fun getCurrentStreak(): Int = prefs.getInt(KEY_CURRENT_STREAK, 0)

    fun getLastActiveDate(): String = prefs.getString(KEY_LAST_ACTIVE_DATE, "") ?: ""

    // -------------------------------------------------------------
    // PARACHUTE SYSTEM (100% Free - Weekly + 5-Hour Countdown)
    // -------------------------------------------------------------
    private val _parachutesStateFlow = MutableStateFlow(getTotalParachutes())
    val parachutesStateFlow: StateFlow<Int> = _parachutesStateFlow.asStateFlow()

    /**
     * Weekly Free Parachute:
     * User gets 1 free parachute per 7-day cycle.
     * Unused parachutes are discarded at the end of each week (they do not accumulate).
     * If used, 0 is available until the next 7-day cycle starts.
     */
    fun getWeeklyParachuteCount(): Int {
        val now = System.currentTimeMillis()
        val weekStart = prefs.getLong(KEY_PARACHUTE_WEEK_START, 0L)
        if (weekStart == 0L || (now - weekStart) >= ONE_WEEK_MILLIS) {
            // New weekly cycle: reset weekly parachute to 1 available
            prefs.edit()
                .putLong(KEY_PARACHUTE_WEEK_START, now)
                .putBoolean(KEY_WEEKLY_PARACHUTE_USED, false)
                .apply()
            return 1
        }
        val isUsed = prefs.getBoolean(KEY_WEEKLY_PARACHUTE_USED, false)
        return if (isUsed) 0 else 1
    }

    fun isWeeklyParachuteAvailable(): Boolean = getWeeklyParachuteCount() > 0

    fun getWeeklyParachuteRemainingMillis(): Long {
        val now = System.currentTimeMillis()
        val weekStart = prefs.getLong(KEY_PARACHUTE_WEEK_START, 0L)
        if (weekStart == 0L) return ONE_WEEK_MILLIS
        val elapsed = now - weekStart
        return maxOf(0L, ONE_WEEK_MILLIS - elapsed)
    }

    /**
     * Extra requested parachutes earned via 5-hour cooldown timer.
     */
    fun getRequestedParachuteCount(): Int {
        return prefs.getInt(KEY_REQUESTED_PARACHUTE_COUNT, 0)
    }

    fun getTotalParachutes(): Int {
        return getWeeklyParachuteCount() + getRequestedParachuteCount()
    }

    fun notifyParachutesChanged() {
        _parachutesStateFlow.value = getTotalParachutes()
    }

    /**
     * Consumes 1 parachute if available (weekly parachute first, then extra requested).
     * Returns true if successfully consumed, false if balance is 0.
     */
    fun useParachute(): Boolean {
        if (getWeeklyParachuteCount() > 0) {
            prefs.edit().putBoolean(KEY_WEEKLY_PARACHUTE_USED, true).apply()
            notifyParachutesChanged()
            return true
        } else if (getRequestedParachuteCount() > 0) {
            val current = getRequestedParachuteCount()
            prefs.edit().putInt(KEY_REQUESTED_PARACHUTE_COUNT, maxOf(0, current - 1)).apply()
            notifyParachutesChanged()
            return true
        }
        return false
    }

    /**
     * 5-Hour Countdown extra parachute request.
     */
    fun getParachuteRequestTimestamp(): Long {
        return prefs.getLong(KEY_PARACHUTE_REQUEST_TIME, 0L)
    }

    fun isParachuteRequestActive(): Boolean {
        val reqTime = getParachuteRequestTimestamp()
        if (reqTime <= 0L) return false
        val elapsed = System.currentTimeMillis() - reqTime
        return elapsed < PARACHUTE_COOLDOWN_MILLIS
    }

    fun isParachuteRequestReady(): Boolean {
        val reqTime = getParachuteRequestTimestamp()
        if (reqTime <= 0L) return false
        val elapsed = System.currentTimeMillis() - reqTime
        return elapsed >= PARACHUTE_COOLDOWN_MILLIS
    }

    fun getParachuteRequestRemainingMillis(): Long {
        val reqTime = getParachuteRequestTimestamp()
        if (reqTime <= 0L) return 0L
        val target = reqTime + PARACHUTE_COOLDOWN_MILLIS
        return maxOf(0L, target - System.currentTimeMillis())
    }

    fun requestParachute(): Boolean {
        if (isParachuteRequestActive()) return false
        prefs.edit()
            .putLong(KEY_PARACHUTE_REQUEST_TIME, System.currentTimeMillis())
            .apply()
        return true
    }

    fun claimRequestedParachute(): Boolean {
        if (!isParachuteRequestReady()) return false
        val current = getRequestedParachuteCount()
        prefs.edit()
            .putInt(KEY_REQUESTED_PARACHUTE_COUNT, current + 1)
            .putLong(KEY_PARACHUTE_REQUEST_TIME, 0L)
            .apply()
        notifyParachutesChanged()
        return true
    }

    fun cancelParachuteRequest() {
        prefs.edit().putLong(KEY_PARACHUTE_REQUEST_TIME, 0L).apply()
    }

    @Volatile
    private var _cachedAllowedPackages: Set<String>? = null

    fun setActiveAllowedPackages(packages: Collection<String>) {
        val clean = packages.filter { it.isNotBlank() }.toSet()
        _cachedAllowedPackages = clean
        prefs.edit().putStringSet(KEY_ACTIVE_ALLOWED_PACKAGES, clean).apply()
        try {
            com.focuslock.app.service.FocusAccessibilityService.updateAllowedPackages(clean)
        } catch (e: Exception) {
            // Ignore if service not ready
        }
    }

    fun getActiveAllowedPackages(): Set<String> {
        val mem = _cachedAllowedPackages
        if (mem != null && mem.isNotEmpty()) {
            return mem
        }
        val fromPrefs = prefs.getStringSet(KEY_ACTIVE_ALLOWED_PACKAGES, emptySet()) ?: emptySet()
        _cachedAllowedPackages = fromPrefs
        return fromPrefs
    }

    fun startSession(
        endTimeMillis: Long,
        durationMinutes: Int,
        bagId: Long,
        bagName: String,
        allowedPackages: List<String> = emptyList()
    ) {
        val editor = prefs.edit()
            .putLong(KEY_SESSION_END_MILLIS, endTimeMillis)
            .putInt(KEY_SESSION_TOTAL_MINUTES, durationMinutes)
            .putLong(KEY_ACTIVE_BAG_ID, bagId)
            .putString(KEY_ACTIVE_BAG_NAME, bagName)
            .putInt(KEY_DISTRACTIONS_BLOCKED, 0)

        if (allowedPackages.isNotEmpty()) {
            val clean = allowedPackages.filter { it.isNotBlank() }.toSet()
            _cachedAllowedPackages = clean
            editor.putStringSet(KEY_ACTIVE_ALLOWED_PACKAGES, clean)
            try {
                com.focuslock.app.service.FocusAccessibilityService.updateAllowedPackages(clean)
            } catch (e: Exception) {
                // Ignore
            }
        }

        editor.apply()
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
        private const val KEY_ACTIVE_ALLOWED_PACKAGES = "active_allowed_packages"
        private const val KEY_SELECTED_BAG_ID = "selected_bag_id"
        private const val KEY_SELECTED_BAG_INDEX = "selected_bag_index"
        private const val KEY_DISTRACTIONS_BLOCKED = "distractions_blocked"
        private const val KEY_BLOCK_RECENTS = "block_recents"
        private const val KEY_BLOCK_SHADE = "block_shade"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"

        // Parachute preferences
        private const val KEY_PARACHUTE_WEEK_START = "parachute_week_start"
        private const val KEY_WEEKLY_PARACHUTE_USED = "weekly_parachute_used"
        private const val KEY_REQUESTED_PARACHUTE_COUNT = "requested_parachute_count"
        private const val KEY_PARACHUTE_REQUEST_TIME = "parachute_request_time"

        // 7 days in milliseconds
        const val ONE_WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000L
        // 5 hours cooldown in milliseconds
        const val PARACHUTE_COOLDOWN_MILLIS = 5L * 60 * 60 * 1000L
    }
}
