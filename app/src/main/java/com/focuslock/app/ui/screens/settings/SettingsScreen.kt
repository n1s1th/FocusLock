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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.focuslock.app.ui.theme.BackgroundDark
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SecondaryEmerald
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = FocusLockApp.instance

    var blockRecents by remember { mutableStateOf(app.preferences.isBlockRecentsEnabled()) }
    var blockShade by remember { mutableStateOf(app.preferences.isBlockShadeEnabled()) }
    var hapticsEnabled by remember { mutableStateOf(app.preferences.isHapticsEnabled()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Settings & Protection",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Fine-tune blocking strictness and system permissions",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "STRICT FOCUS CONTROLS", style = MaterialTheme.typography.labelLarge, color = TextMuted)
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

        Text(text = "SYSTEM PERMISSIONS & PERSISTENCE", style = MaterialTheme.typography.labelLarge, color = TextMuted)
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

        Spacer(modifier = Modifier.height(36.dp))

        // Open Source License / Free badge
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BlockIT • Stop Phone Addiction",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No in-app purchases, no advertisements, no tracking. All features (unlimited sessions, multi-bag whitelist, auto-routines) are unlocked permanently.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
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
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
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
                        .background(PrimaryIndigo.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = subtitle, color = TextMuted, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo)
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
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
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
                        .background(SecondaryEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = SecondaryEmerald, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = subtitle, color = TextMuted, fontSize = 12.sp)
                }
            }
            TextButton(onClick = onClick) {
                Text(text = actionLabel, color = SecondaryEmerald, fontWeight = FontWeight.Bold)
            }
        }
    }
}
