package com.focuslock.app.ui.screens.reels

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.focuslock.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val timeInForeground: Long
)

@Composable
fun ReelBlockDashboardScreen(
    onAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    var appUsageList by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val targetPackages = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically", // TikTok
        "com.twitter.android"       // X
    )

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = context.packageManager

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            )

            val usageMap = mutableMapOf<String, Long>()
            usageStats?.forEach { stat ->
                if (targetPackages.contains(stat.packageName)) {
                    usageMap[stat.packageName] = (usageMap[stat.packageName] ?: 0L) + stat.totalTimeInForeground
                }
            }

            val list = targetPackages.mapNotNull { pkg ->
                try {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)
                    AppUsageInfo(pkg, appName, icon, usageMap[pkg] ?: 0L)
                } catch (e: PackageManager.NameNotFoundException) {
                    null // App not installed
                }
            }.sortedByDescending { it.timeInForeground }

            appUsageList = list
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Feature Blocker",
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = CharcoalPrimary
        )
        Text(
            text = "Select an app to configure granular blocking",
            fontFamily = GoogleSans,
            fontSize = 14.sp,
            color = SecondaryGray
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (appUsageList.isEmpty()) {
            Text(
                "No target apps installed (Instagram, YouTube, etc.)",
                color = CharcoalPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(appUsageList) { app ->
                    AppUsageRow(app = app, onClick = { onAppClick(app.packageName) })
                }
            }
        }
    }
}

@Composable
fun AppUsageRow(app: AppUsageInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBright)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(modifier = Modifier.size(48.dp).background(SurfaceVariant, RoundedCornerShape(12.dp)))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = app.appName,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CharcoalPrimary
                )
                val minutes = app.timeInForeground / (1000 * 60)
                Text(
                    text = "Usage today: ${minutes}m",
                    fontFamily = GoogleSans,
                    fontSize = 12.sp,
                    color = SecondaryGray
                )
            }
        }
    }
}
