package com.focuslock.app.ui.screens.lock

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.FocusLockApp
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.service.FocusLockService
import com.focuslock.app.ui.screens.home.components.SlideToStart
import com.focuslock.app.ui.theme.AccentAmber
import com.focuslock.app.ui.theme.AccentRose
import com.focuslock.app.ui.theme.BackgroundDark
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SecondaryEmerald
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.SurfaceDark
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun LockScreenContent(
    onExitLock: () -> Unit
) {
    val context = LocalContext.current
    val app = FocusLockApp.instance

    var remainingSeconds by remember { mutableIntStateOf(0) }
    var bagEntity by remember { mutableStateOf<BagEntity?>(null) }
    var distractionsCount by remember { mutableIntStateOf(0) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    // Live countdown loop
    LaunchedEffect(Unit) {
        val bagId = app.preferences.getActiveBagId()
        bagEntity = app.database.bagDao().getBagById(bagId)

        while (true) {
            val remainingMillis = app.preferences.getSessionEndTimeMillis() - System.currentTimeMillis()
            if (remainingMillis <= 0) {
                onExitLock()
                break
            }
            remainingSeconds = (remainingMillis / 1000).toInt()
            distractionsCount = app.preferences.getDistractionsBlockedCount()
            delay(500L)
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bagEntity?.name ?: "Deep Focus",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Distractions Blocked Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Blocked",
                        tint = SecondaryEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$distractionsCount blocked",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Center Big Digital Clock
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    color = TextPrimary,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "Stay focused on what matters",
                    color = TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom Whitelist App Dock & Emergency Unlock
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ALLOWED APPS",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Allowed Apps Row
                val allowedPackages = bagEntity?.allowedPackages ?: emptyList()
                if (allowedPackages.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(allowedPackages) { pkg ->
                            AllowedAppItem(
                                packageName = pkg,
                                onClick = {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Minimal mode (no extra apps allowed)",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Emergency Exit Button (Free, No Tokens Required)
                TextButton(
                    onClick = { showEmergencyDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergency",
                        tint = AccentRose,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Emergency Unlock",
                        color = AccentRose,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Emergency Unlock Dialog
        if (showEmergencyDialog) {
            AlertDialog(
                onDismissRequest = { showEmergencyDialog = false },
                containerColor = SurfaceDark,
                title = {
                    Text("Emergency Unlock", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Need to exit early for an emergency? BlockIT is 100% free with no penalties.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEmergencyDialog = false
                            FocusLockService.stopService(context)
                            onExitLock()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                    ) {
                        Text("End Session", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmergencyDialog = false }) {
                        Text("Keep Focusing", color = PrimaryIndigo)
                    }
                }
            )
        }
    }
}

@Composable
fun AllowedAppItem(
    packageName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    var appLabel by remember { mutableStateOf(packageName) }
    LaunchedEffect(packageName) {
        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            appLabel = pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            appLabel = packageName.substringAfterLast(".")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appLabel.take(1).uppercase(),
                color = PrimaryIndigo,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = appLabel,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(60.dp)
        )
    }
}
