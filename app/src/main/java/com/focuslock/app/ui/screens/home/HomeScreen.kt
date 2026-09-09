package com.focuslock.app.ui.screens.home

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.R
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.service.FocusAccessibilityService
import com.focuslock.app.ui.screens.home.components.BlockLogoView
import com.focuslock.app.ui.screens.home.components.DigitalTimerDisplay
import com.focuslock.app.ui.screens.home.components.SlideToStart
import com.focuslock.app.ui.screens.home.components.VerticalRulerPicker
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SecondaryMuted
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.ui.theme.SurfaceVariant

@Composable
fun HomeScreen(
    onNavigateToLock: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val bags by viewModel.bags.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()

    val isAccessibilityOn = FocusAccessibilityService.isServiceRunning

    if (isSessionActive) {
        onNavigateToLock()
    }

    val hours = selectedMinutes / 60
    val minutes = selectedMinutes % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // 1. TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill: [Square dot] 10
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceBright)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(SecondaryGray)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "${viewModel.getStreak().coerceAtLeast(10)}",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CharcoalPrimary
                )
            }

            // Center Wordmark / Logo
            BlockLogoView(
                pixelSize = 4.5.dp,
                color = CharcoalPrimary,
                pulseColor = AccentOrange
            )

            // Right Pill: [Parachute Circle] 0 | [Avatar Circle]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceBright)
                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Parachute Circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CharcoalPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_parachute),
                        contentDescription = "Parachutes",
                        tint = SurfaceBright,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(-25f)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "0",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CharcoalPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Vertical Divider Line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(OutlineSubtle)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Avatar Profile Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CharcoalPrimary)
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_account),
                        contentDescription = "Profile",
                        tint = SurfaceBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Accessibility Permission Notice (if disabled)
        if (!isAccessibilityOn) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceBright),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = AccentOrange
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Permission Required",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Enable Accessibility to activate screen blocking.",
                            fontFamily = GoogleSans,
                            color = SecondaryGray,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("Enable", color = SurfaceBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. MAIN TIMER CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp),
            shape = RoundedCornerShape(38.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBright),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                DigitalTimerDisplay(
                    hours = hours,
                    minutes = minutes,
                    blockSize = 17.5.dp,
                    digitColor = CharcoalPrimary,
                    colonColor = SecondaryGray.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. PRESETS GRID & VERTICAL SELECTOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column: 2x2 Preset Cards
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: "15 Minutes" & "45 Minutes"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCard(
                        value = "15",
                        label = "Minutes",
                        isSelected = selectedMinutes == 15,
                        onClick = { viewModel.setMinutes(15) },
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                    )
                    PresetCard(
                        value = "45",
                        label = "Minutes",
                        isSelected = selectedMinutes == 45,
                        onClick = { viewModel.setMinutes(45) },
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                    )
                }

                // Row 2: "3 Hours" & "12:00 Custom (Locked)"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCard(
                        value = "3",
                        label = "Hours",
                        isSelected = selectedMinutes == 180,
                        onClick = { viewModel.setMinutes(180) },
                        modifier = Modifier
                            .weight(1f)
                            .height(182.dp)
                    )
                    CustomLockedCard(
                        value = "12:00",
                        label = "Custom",
                        modifier = Modifier
                            .weight(1f)
                            .height(182.dp)
                    )
                }
            }

            // Right Column: Vertical Time Ruler Picker & Chevrons
            VerticalRulerPicker(
                selectedMinutes = selectedMinutes,
                onMinutesChanged = { viewModel.setMinutes(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. SLIDE TO START
        SlideToStart(
            onSlideComplete = {
                val currentBag = bags.firstOrNull() ?: BagEntity(name = "Default", isDefault = true)
                viewModel.startFocusSession(currentBag)
                onNavigateToLock()
            },
            text = "SLIDE TO START"
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun PresetCard(
    value: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = CharcoalPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SecondaryGray
            )
        }
    }
}

@Composable
fun CustomLockedCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = "Locked",
                tint = AccentOrange,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = SecondaryMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SecondaryMuted
            )
        }
    }
}
