package com.focuslock.app.ui.screens.bags

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.R
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.data.model.InstalledApp
import com.focuslock.app.ui.screens.bags.components.ThreeBagsRowDisplay
import com.focuslock.app.ui.screens.home.components.TopHeaderBar
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.ui.theme.SurfaceVariant
import java.util.Locale

@Composable
fun BagsScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: BagsViewModel = viewModel()
) {
    val bags by viewModel.bags.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    val totalParachutes by viewModel.totalParachutes.collectAsState()
    val isWeeklyAvailable by viewModel.isWeeklyAvailable.collectAsState()
    val weeklyRemainingMillis by viewModel.weeklyRemainingMillis.collectAsState()
    val isRequestActive by viewModel.isRequestActive.collectAsState()
    val isRequestReady by viewModel.isRequestReady.collectAsState()
    val requestRemainingMillis by viewModel.requestRemainingMillis.collectAsState()

    var selectedBagIndex by remember { mutableIntStateOf(0) }
    var selectedSlotIndex by remember { mutableIntStateOf(-1) }
    var showSlotActionSheet by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }

    val currentBag = bags.getOrNull(selectedBagIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // 1. TOP HEADER (Live Streaks + Total Parachutes)
        TopHeaderBar(
            streakCount = viewModel.currentStreak,
            parachuteCount = totalParachutes,
            onProfileClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. TOP WHITE CARD (3 Pixel-Art Bags, All Unlocked)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            shape = RoundedCornerShape(38.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ThreeBagsRowDisplay(selectedIndex = selectedBagIndex)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. MIDDLE SECTION (3 Bag Tabs + 6 App Slots Grid)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // 3 Bag Selector Pill Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Bag 1", "Bag 2", "Bag 3").forEachIndexed { index, title ->
                        val isSelected = index == selectedBagIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (isSelected) AccentOrange else Color.Transparent)
                                .clickable { selectedBagIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) SurfaceBright else SecondaryGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6 App Spaces (2 Rows of 3 Slots, 100% Unlocked, Zero Locks)
                val currentPackages = currentBag?.allowedPackages ?: emptyList()

                // Row 1: Slots 0, 1, 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (slot in 0..2) {
                        val pkg = currentPackages.getOrNull(slot)
                        AppSlotItem(
                            packageName = pkg,
                            slotNumber = slot + 1,
                            installedApps = installedApps,
                            onClick = {
                                selectedSlotIndex = slot
                                if (pkg.isNullOrBlank()) {
                                    showAppPicker = true
                                } else {
                                    showSlotActionSheet = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Slots 3, 4, 5
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (slot in 3..5) {
                        val pkg = currentPackages.getOrNull(slot)
                        AppSlotItem(
                            packageName = pkg,
                            slotNumber = slot + 1,
                            installedApps = installedApps,
                            onClick = {
                                selectedSlotIndex = slot
                                if (pkg.isNullOrBlank()) {
                                    showAppPicker = true
                                } else {
                                    showSlotActionSheet = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. BOTTOM PARACHUTE SECTION (100% Free - Weekly + 5h Cooldown, Zero Payments)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Parachute Balance Card
            Card(
                modifier = Modifier
                    .width(96.dp)
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Small orange status dot at top-right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(6.dp)
                            .background(AccentOrange)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 14.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CharcoalPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_parachute),
                                contentDescription = "Parachute",
                                tint = SurfaceBright,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(-25f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$totalParachutes",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = CharcoalPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Parachutes",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = SecondaryGray
                        )
                    }
                }
            }

            // Right 2-Card Status & Request Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Weekly Parachute Status Card (1 Free / Week)
                val weeklyDaysRemaining = (weeklyRemainingMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isWeeklyAvailable) Color(0xFF22C55E) else SecondaryGray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Weekly Parachute",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CharcoalPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isWeeklyAvailable) "1 free available" else "Used • Resets in ${weeklyDaysRemaining}d",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = SecondaryGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isWeeklyAvailable) AccentOrange.copy(alpha = 0.15f) else ScreenBackground)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isWeeklyAvailable) "Active" else "1/week",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isWeeklyAvailable) AccentOrange else SecondaryGray
                            )
                        }
                    }
                }

                // 2. Extra Parachute Request (5-Hour Countdown Timer)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        when {
                            isRequestReady -> {
                                // Parachute ready to claim!
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "+1 Parachute Ready!",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = CharcoalPrimary
                                        )
                                        Text(
                                            text = "5-hour wait completed",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = SecondaryGray
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.claimParachute() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Claim",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            isRequestActive -> {
                                // Active 5-hour countdown ticking
                                val totalSeconds = (requestRemainingMillis / 1000).toInt()
                                val hours = totalSeconds / 3600
                                val mins = (totalSeconds % 3600) / 60
                                val secs = totalSeconds % 60
                                val timeText = String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)

                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Preparing Parachute",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CharcoalPrimary
                                        )
                                        Text(
                                            text = "Takes 5 hours",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp,
                                            color = SecondaryGray
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CharcoalPrimary)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = timeText,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = SurfaceBright
                                        )
                                    }
                                }
                            }

                            else -> {
                                // Idle state: user can click to request parachute (starts 5h countdown)
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "+1 Extra Parachute",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = CharcoalPrimary
                                        )
                                        Text(
                                            text = "5-hour countdown",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = SecondaryGray
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.requestParachute() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalPrimary),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Request",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = SurfaceBright
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // App Picker Sheet
    if (showAppPicker && currentBag != null && selectedSlotIndex in 0..5) {
        AppPickerSheet(
            initialSelectedPackages = currentBag.allowedPackages.filter { it.isNotBlank() },
            installedApps = installedApps,
            onDismiss = {
                showAppPicker = false
                selectedSlotIndex = -1
            },
            onSave = { updatedPackages ->
                viewModel.updateBagAllowedPackages(currentBag, updatedPackages)
                showAppPicker = false
                selectedSlotIndex = -1
            }
        )
    }

    // Slot Action Sheet (Change / Clear slot)
    if (showSlotActionSheet && currentBag != null && selectedSlotIndex in 0..5) {
        SlotOptionsModalSheet(
            packageName = currentBag.allowedPackages.getOrNull(selectedSlotIndex) ?: "",
            installedApps = installedApps,
            onChangeApp = {
                showSlotActionSheet = false
                showAppPicker = true
            },
            onRemoveApp = {
                viewModel.removeAppFromSlot(currentBag, selectedSlotIndex)
                showSlotActionSheet = false
                selectedSlotIndex = -1
            },
            onDismiss = {
                showSlotActionSheet = false
                selectedSlotIndex = -1
            }
        )
    }
}

@Composable
fun AppSlotItem(
    packageName: String?,
    slotNumber: Int,
    installedApps: List<InstalledApp>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appInfo = remember(packageName, installedApps) {
        if (!packageName.isNullOrBlank()) {
            installedApps.find { it.packageName == packageName }
        } else null
    }

    Card(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (appInfo != null) {
                // App is assigned in this slot
                if (appInfo.icon != null) {
                    val bitmap = remember(appInfo.icon) {
                        try {
                            appInfo.icon.toBitmap(96, 96).asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = appInfo.appName,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        FallbackSlotIcon(appInfo.appName)
                    }
                } else {
                    FallbackSlotIcon(appInfo.appName)
                }
            } else if (!packageName.isNullOrBlank()) {
                // Fallback for package without loaded appInfo
                FallbackSlotIcon(packageName.substringAfterLast("."))
            } else {
                // Clean Unlocked Empty Slot (No Lock Icon!)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceBright.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add App to Slot $slotNumber",
                        tint = SecondaryGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FallbackSlotIcon(name: String) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalPrimary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(2).uppercase(),
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = SurfaceBright
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotOptionsModalSheet(
    packageName: String,
    installedApps: List<InstalledApp>,
    onChangeApp: () -> Unit,
    onRemoveApp: () -> Unit,
    onDismiss: () -> Unit
) {
    val appInfo = remember(packageName, installedApps) {
        installedApps.find { it.packageName == packageName }
    }
    val appName = appInfo?.appName ?: packageName.substringAfterLast(".")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceBright
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = appName,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CharcoalPrimary
            )
            Text(
                text = packageName,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = SecondaryGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onChangeApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalPrimary)
            ) {
                Text(
                    text = "Change App",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceBright
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onRemoveApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScreenBackground)
            ) {
                Text(
                    text = "Remove from Slot",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

