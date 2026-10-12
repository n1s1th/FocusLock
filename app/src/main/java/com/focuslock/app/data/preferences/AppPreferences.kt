package com.focuslock.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import java.util.UUID
import com.focuslock.app.data.model.FocusProgress
import com.focuslock.app.data.database.entities.SessionEntity

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

    fun isAlwaysOnDisplayEnabled(): Boolean = prefs.getBoolean(KEY_ALWAYS_ON_DISPLAY, false)

    fun setAlwaysOnDisplayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALWAYS_ON_DISPLAY, enabled).apply()
    }

    private val _habitRevision = MutableStateFlow(0)
    val habitRevision: StateFlow<Int> = _habitRevision.asStateFlow()

    val routineRequest = MutableStateFlow<Long?>(null)
    private fun changed() { _habitRevision.value++ }
    fun getWeeklyGoal(): Int = prefs.getInt("weekly_goal", 4)
    fun getFocusDaysMask(): Int = prefs.getInt("focus_days", 127)
    fun getSavedMinutes(): Int = prefs.getInt("saved_minutes", 15)
    fun getSavedTask(bagId: Long = getSelectedBagId()): String = prefs.getString("saved_task_$bagId", "") ?: ""
    fun getSavedPlan(bagId: Long = getSelectedBagId()): String = prefs.getString("saved_plan_$bagId", "") ?: ""
    fun getActiveTask(): String = prefs.getString("active_task", "") ?: ""
    fun getActivePlan(): String = prefs.getString("active_plan", "") ?: ""
    fun getSessionKey(): String = prefs.getString("session_key", null)
        ?: "restored-${getSessionEndTimeMillis()}"
    fun getSessionStartMillis(): Long = prefs.getLong("session_start", getSessionEndTimeMillis() - getSessionTotalDurationMinutes() * 60_000L)
    fun hasStoredSession(): Boolean = getSessionEndTimeMillis() > 0
    fun getLastOutcome(): String = prefs.getString("last_outcome", "") ?: ""
    fun getLastSessionKey(): String = prefs.getString("last_session_key", "") ?: ""
    fun getPendingReasonKey(): String = prefs.getString("pending_reason_key", "") ?: ""
    fun dismissPendingReason() { prefs.edit().remove("pending_reason_key").apply(); changed() }
    fun getAchievements(): Set<String> = prefs.getStringSet("achievements", emptySet())?.toSet() ?: emptySet()

    fun saveHabitSettings(goal: Int, daysMask: Int) {
        prefs.edit().putInt("weekly_goal", goal.coerceIn(1, 14))
            .putInt("focus_days", if (daysMask == 0) 127 else daysMask and 127).apply()
        changed()
    }

    fun savePreset(minutes: Int, task: String, plan: String) {
        prefs.edit().putInt("saved_minutes", minutes.coerceIn(1, 720))
            .putString("saved_task_${getSelectedBagId()}", task.trim().take(120))
            .putString("saved_plan_${getSelectedBagId()}", plan.trim().take(160)).apply()
        changed()
    }

    fun refreshProgress(sessions: List<SessionEntity>) {
        val completed = sessions.filter { it.isCompleted }
        val dates = completed.map { it.dateString }.toSet()
        val earned = getAchievements().toMutableSet()
        for ((count, name) in listOf(1 to "First finish", 5 to "First five", 20 to "Twenty finishes")) {
            if (completed.size >= count) earned.add(name)
        }
        if (dates != prefs.getStringSet("completed_dates", emptySet()) || earned != getAchievements()) {
            prefs.edit().putStringSet("completed_dates", dates).putStringSet("achievements", earned).apply()
            changed()
        }
    }

    fun getCurrentStreak(): Int {
        val dates = (prefs.getStringSet("completed_dates", emptySet()) ?: emptySet())
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        return FocusProgress.streak(dates, LocalDate.now(), getFocusDaysMask())
    }

    fun getResetRemainingMillis(): Long = (prefs.getLong("reset_end", 0) - System.currentTimeMillis()).coerceAtLeast(0)
    fun getRemainingFocusMillis(): Long = FocusProgress.remainingFocusMillis(
        getSessionEndTimeMillis(), prefs.getLong("reset_end", 0), System.currentTimeMillis())

    @Synchronized fun beginReset(): Boolean {
        if (!isSessionActive() || getResetRemainingMillis() > 0) return false
        prefs.edit().putLong(KEY_SESSION_END_MILLIS, getSessionEndTimeMillis() + 60_000)
            .putLong("reset_end", System.currentTimeMillis() + 60_000).apply()
        changed()
        return true
    }

    @Synchronized fun resumeReset() {
        val unused = getResetRemainingMillis()
        prefs.edit().putLong(KEY_SESSION_END_MILLIS, getSessionEndTimeMillis() - unused)
            .putLong("reset_end", 0).apply()
        changed()
    }

    @Synchronized fun extendSession(minutes: Int) {
        if (!isSessionActive()) return
        prefs.edit().putLong(KEY_SESSION_END_MILLIS, getSessionEndTimeMillis() + minutes * 60_000L)
            .putInt(KEY_SESSION_TOTAL_MINUTES, getSessionTotalDurationMinutes() + minutes).apply()
        changed()
    }

    fun finishSessionState(completed: Boolean, key: String) {
        prefs.edit().putString("last_outcome", if (completed) "completed" else "interrupted")
            .putString("last_session_key", key)
            .putString("pending_reason_key", if (completed) "" else key).apply()
        stopSession()
        changed()
    }

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

    @Synchronized fun startSession(
        endTimeMillis: Long,
        durationMinutes: Int,
        bagId: Long,
        bagName: String,
        allowedPackages: List<String> = emptyList(),
        task: String = "",
        copingPlan: String = ""
    ): Boolean {
        if (hasStoredSession()) return false
        val editor = prefs.edit()
            .putString("session_key", UUID.randomUUID().toString())
            .putLong("session_start", System.currentTimeMillis())
            .putLong("reset_end", 0)
            .putString("active_task", task)
            .putString("active_plan", copingPlan)
            .putString("last_outcome", "")
            .putLong(KEY_SESSION_END_MILLIS, endTimeMillis)
            .putInt(KEY_SESSION_TOTAL_MINUTES, durationMinutes)
            .putLong(KEY_ACTIVE_BAG_ID, bagId)
            .putString(KEY_ACTIVE_BAG_NAME, bagName)
            .putInt(KEY_DISTRACTIONS_BLOCKED, 0)

        run {
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
        changed()
        return true
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

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
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
        private const val KEY_ALWAYS_ON_DISPLAY = "always_on_display"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

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
