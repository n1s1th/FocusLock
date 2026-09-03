package com.focuslock.app.data.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isSelected: Boolean = false
)
