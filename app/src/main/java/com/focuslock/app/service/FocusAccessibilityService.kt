package com.focuslock.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.SystemClock
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
    private var allowedPackagesCache: Set<String> = emptySet()
    private var lastInspectionTime = 0L
    private var lastBlockedPackage: String? = null

    // Essential system packages that must never be blocked (emergency calls, input methods)
    private val systemEssentialPackages = setOf(
        "android",
        "com.android.phone",
        "com.google.android.dialer",
        "com.android.dialer",
        "com.android.server.telecom",
        "com.android.incallui",
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.google.android.deskclock",
        "com.android.deskclock"
    )

    companion object {
        var isServiceRunning: Boolean = false
            private set
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

        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // Check for SystemUI shade or recents suppression
        if (packageName == "com.android.systemui") {
            handleSystemUiInteractions(className, app)
            return
        }

        // If package is FocusLock itself, allow
        if (packageName == applicationContext.packageName) {
            return
        }

        // Check if package is system essential
        if (systemEssentialPackages.contains(packageName)) {
            return
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
        if (allowedPackagesCache.contains(packageName)) return true

        // Refresh cache in background if empty
        if (allowedPackagesCache.isEmpty()) {
            refreshAllowedPackages()
        }
        return allowedPackagesCache.contains(packageName)
    }

    fun refreshAllowedPackages() {
        serviceScope.launch {
            val app = FocusLockApp.instance
            val activeBagId = app.preferences.getActiveBagId()
            val bag = app.database.bagDao().getBagById(activeBagId)
            allowedPackagesCache = (bag?.allowedPackages ?: emptyList()).toSet() + systemEssentialPackages
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
