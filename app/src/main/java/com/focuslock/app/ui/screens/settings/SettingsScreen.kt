package com.focuslock.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.FocusLockApp
import com.focuslock.app.ui.screens.home.components.TopHeaderBar
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SecondaryMuted
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.ui.theme.SurfaceVariant

// Fix 10: Settings screen now uses app theme (light, clean) + TopHeaderBar for consistency
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = FocusLockApp.instance

    var blockRecents by remember { mutableStateOf(app.preferences.isBlockRecentsEnabled()) }
    var blockShade by remember { mutableStateOf(app.preferences.isBlockShadeEnabled()) }
    var hapticsEnabled by remember { mutableStateOf(app.preferences.isHapticsEnabled()) }

    val parachuteCount by app.preferences.parachutesStateFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Fix 10: Add consistent TopHeaderBar like other screens
        TopHeaderBar(
            streakCount = app.preferences.getCurrentStreak(),
            parachuteCount = parachuteCount,
            onProfileClick = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings & Protection",
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = CharcoalPrimary
        )
        Text(
            text = "Fine-tune blocking strictness and system permissions",
            fontFamily = GoogleSans,
            fontSize = 13.sp,
            color = SecondaryGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel("STRICT FOCUS CONTROLS")
        Spacer(modifier = Modifier.height(10.dp))

        SettingToggleCard(
            title = "Block Recents / Multitasking",
            subtitle = "Prevents switching apps using Android navigation gestures or recents button",
            icon = Icons.Default.Lock,
            isChecked = blockRecents,
            onCheckedChange = {
                blockRecents = it
                app.preferences.setBlockRecents(it)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingToggleCard(
            title = "Dismiss Notification Shade",
            subtitle = "Automatically collapses notification shade during locked focus sessions",
            icon = Icons.Default.NotificationsOff,
            isChecked = blockShade,
            onCheckedChange = {
                blockShade = it
                app.preferences.setBlockShade(it)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingToggleCard(
            title = "Tactile Haptics",
            subtitle = "Vibration feedback on timer dial and slide gestures",
            icon = Icons.Default.Vibration,
            isChecked = hapticsEnabled,
            onCheckedChange = {
                hapticsEnabled = it
                app.preferences.setHapticsEnabled(it)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("SYSTEM PERMISSIONS & PERSISTENCE")
        Spacer(modifier = Modifier.height(10.dp))

        SettingActionCard(
            title = "Accessibility Service",
            subtitle = "Core engine for app detection and strict blocking",
            icon = Icons.Default.Security,
            actionLabel = "Open",
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingActionCard(
            title = "Disable Battery Optimization",
            subtitle = "Ensures background timer service is not killed by OS power saving",
            icon = Icons.Default.BatteryChargingFull,
            actionLabel = "Configure",
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Open Source / Free badge
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BlockIT • Stop Phone Addiction",
                    fontFamily = GoogleSans,
                    color = CharcoalPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No in-app purchases, no advertisements, no tracking. All features — unlimited sessions, multi-bag whitelist, auto-routines, parachute system — are permanently free.",
                    fontFamily = GoogleSans,
                    color = SecondaryGray,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = SecondaryMuted
    )
}

@Composable
fun SettingToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceBright),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CharcoalPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = CharcoalPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontFamily = GoogleSans, color = CharcoalPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = subtitle, fontFamily = GoogleSans, color = SecondaryGray, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SurfaceBright,
                    checkedTrackColor = CharcoalPrimary,
                    uncheckedThumbColor = SecondaryGray,
                    uncheckedTrackColor = SurfaceVariant
                )
            )
        }
    }
}

@Composable
fun SettingActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceBright),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontFamily = GoogleSans, color = CharcoalPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = subtitle, fontFamily = GoogleSans, color = SecondaryGray, fontSize = 11.sp)
                }
            }
            TextButton(onClick = onClick) {
                Text(
                    text = actionLabel,
                    fontFamily = GoogleSans,
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
