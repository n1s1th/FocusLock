package com.focuslock.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focuslock.app.FocusLockApp
import com.focuslock.app.ui.screens.lock.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var lastInspectionTime = 0L
    private var lastBlockedPackage: String? = null

    // Essential system packages that must never be blocked (emergency calls, input methods, system UI/permissions)
    private val systemEssentialPackages = setOf(
        "android",
        "com.android.phone",
        "com.google.android.dialer",
        "com.android.dialer",
        "com.android.server.telecom",
        "com.android.incallui",
        "com.samsung.android.incallui",
        "com.samsung.android.dialer",
        "com.miui.incallui",
        "com.coloros.incallui",
        "com.coloros.telephony",
        "com.vivo.incallui",
        "com.oneplus.dialer",
        "com.oppo.dialer",
        "com.transsion.incallui",
        "com.huawei.android.incallui",
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.documentsui",
        "com.google.android.documentsui",
        "com.google.android.settings.intelligence",
        "com.google.android.gms",
        "com.google.android.gsf"
    )

    private fun isPhoneCallPackage(packageName: String): Boolean {
        val lower = packageName.lowercase(Locale.ROOT)
        return lower.contains("incallui") ||
                lower.contains("telecom") ||
                lower.contains("telephony") ||
                (lower.contains("dialer") && !lower.contains("game")) ||
                lower.contains("calling")
    }

    private fun isPhoneCallActiveOrRinging(): Boolean {
        return try {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            @Suppress("DEPRECATION")
            val state = tm?.callState ?: TelephonyManager.CALL_STATE_IDLE
            state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        var isServiceRunning: Boolean = false
            private set

        @Volatile
        var allowedPackagesCache: Set<String> = emptySet()
            private set

        @Volatile
        var lastAllowedLaunchTime: Long = 0L
            private set

        @Volatile
        var currentlyLaunchingPackage: String? = null
            private set

        fun updateAllowedPackages(packages: Set<String>) {
            allowedPackagesCache = packages
            Log.d("FocusA11y", "Updated allowedPackagesCache: ${packages.size} items: $packages")
        }

        fun notifyAppLaunching(packageName: String) {
            currentlyLaunchingPackage = packageName
            lastAllowedLaunchTime = SystemClock.uptimeMillis()
            Log.d("FocusA11y", "App launching initiated for: $packageName at $lastAllowedLaunchTime")
        }
    }

    private var defaultLauncherPackage: String? = null

    private fun isLauncherPackage(packageName: String): Boolean {
        if (defaultLauncherPackage == null) {
            try {
                val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                defaultLauncherPackage = packageManager.resolveActivity(homeIntent, 0)?.activityInfo?.packageName
            } catch (e: Exception) {
                // Ignore
            }
        }
        return defaultLauncherPackage == packageName ||
                packageName == "com.google.android.apps.nexuslauncher" ||
                packageName == "com.android.launcher3" ||
                packageName == "com.sec.android.app.launcher" ||
                packageName.contains("launcher", ignoreCase = true)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        refreshAllowedPackages()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val app = FocusLockApp.instance
        if (!app.preferences.isSessionActive()) {
            return
        }

        // Only enforce distraction blocking on real window state / window hierarchy changes
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // Never intercept, block, or redirect when a phone call is incoming/ringing or active
        if (isPhoneCallActiveOrRinging() || isPhoneCallPackage(packageName)) {
            Log.d("FocusA11y", "Allowing active/ringing phone call interaction: $packageName ($className)")
            return
        }

        // Check for SystemUI shade or recents suppression
        if (packageName == "com.android.systemui") {
            handleSystemUiInteractions(className, app)
            return
        }

        // If package is FocusLock itself, allow
        if (packageName == applicationContext.packageName) {
            return
        }

        // Check if package is system essential (Emergency phone, dialer, keyboards, alarms)
        if (systemEssentialPackages.contains(packageName)) {
            return
        }

        // If an allowed app is currently launching, ignore launcher transition events (2.5s window)
        val timeSinceLaunch = SystemClock.uptimeMillis() - lastAllowedLaunchTime
        if (timeSinceLaunch < 2500L) {
            if (packageName == currentlyLaunchingPackage || isLauncherPackage(packageName)) {
                Log.d("FocusA11y", "Allowing transition window event: $packageName (launching: $currentlyLaunchingPackage)")
                return
            }
        }

        // Rate limit inspections slightly to avoid CPU spikes
        val now = SystemClock.uptimeMillis()
        if (now - lastInspectionTime < 150 && packageName == lastBlockedPackage) {
            return
        }
        lastInspectionTime = now

        // Check against active bag whitelist
        if (!isPackageAllowed(packageName)) {
            lastBlockedPackage = packageName
            Log.d("FocusA11y", "Distraction detected: $packageName - Enforcing Focus Lock")
            app.preferences.incrementDistractionsBlocked()
            redirectToLockOverlay()
        }
    }

    override fun onInterrupt() {
        Log.w("FocusA11y", "Accessibility Service interrupted")
    }

    private fun handleSystemUiInteractions(className: String, app: FocusLockApp) {
        // If a call is ringing or active, do not dismiss notification shade or go home
        if (isPhoneCallActiveOrRinging()) {
            Log.d("FocusA11y", "Phone call active/ringing; bypassing SystemUI dismissal")
            return
        }

        val lowerClass = className.lowercase(Locale.ROOT)

        // Block multitasking / recents switcher
        if (app.preferences.isBlockRecentsEnabled()) {
            val isRecents = lowerClass.contains("recent") ||
                    lowerClass.contains("overview") ||
                    lowerClass.contains("taskview") ||
                    lowerClass.contains("splitscreen") ||
                    lowerClass.contains("multiwindow")

            if (isRecents) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                redirectToLockOverlay()
                return
            }
        }

        // Block notification shade pull-downs
        if (app.preferences.isBlockShadeEnabled()) {
            val isShade = lowerClass.contains("notification") ||
                    lowerClass.contains("shade") ||
                    lowerClass.contains("quicksettings") ||
                    lowerClass.contains("panel")

            if (isShade) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
                } else {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
            }
        }
    }

    private fun isPackageAllowed(packageName: String): Boolean {
        // 0. Phone calls and dialer UI
        if (isPhoneCallActiveOrRinging() || isPhoneCallPackage(packageName)) return true

        // 1. Essential system apps
        if (systemEssentialPackages.contains(packageName)) return true

        // 2. Currently launching allowed app within 3 seconds
        if (packageName == currentlyLaunchingPackage && (SystemClock.uptimeMillis() - lastAllowedLaunchTime < 3000L)) {
            return true
        }

        // 3. Fast in-memory cache
        if (allowedPackagesCache.contains(packageName)) return true

        // 4. Live SharedPreferences check
        val activeAllowed = FocusLockApp.instance.preferences.getActiveAllowedPackages()
        if (activeAllowed.contains(packageName)) {
            allowedPackagesCache = activeAllowed
            return true
        }

        // 5. Fallback refresh from database
        val activeBagId = FocusLockApp.instance.preferences.getActiveBagId()
        if (activeAllowed.isEmpty() && activeBagId > 0) {
            refreshAllowedPackages()
        }

        return false
    }

    fun refreshAllowedPackages() {
        serviceScope.launch {
            val app = FocusLockApp.instance
            val activeBagId = app.preferences.getActiveBagId()
            val bag = app.database.bagDao().getBagById(activeBagId)
            val bagPackages = (bag?.allowedPackages ?: emptyList()).filter { it.isNotBlank() }.toSet()
            app.preferences.setActiveAllowedPackages(bagPackages)
            allowedPackagesCache = bagPackages
            Log.d("FocusA11y", "Refreshed allowed packages for bag $activeBagId: $bagPackages")
        }
    }

    private fun redirectToLockOverlay() {
        val lockIntent = Intent(this, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(lockIntent)
    }
}
