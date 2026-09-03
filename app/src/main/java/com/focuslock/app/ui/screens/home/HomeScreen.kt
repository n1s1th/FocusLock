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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.service.FocusAccessibilityService
import com.focuslock.app.ui.screens.home.components.FocusDial
import com.focuslock.app.ui.screens.home.components.SlideToStart
import com.focuslock.app.ui.theme.AccentAmber
import com.focuslock.app.ui.theme.BackgroundDark
import com.focuslock.app.ui.theme.BorderSubtle
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.SurfaceDark
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onNavigateToLock: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val bags by viewModel.bags.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()

    var selectedBagIndex by remember { mutableIntStateOf(0) }
    val isAccessibilityOn = FocusAccessibilityService.isServiceRunning

    if (isSessionActive) {
        onNavigateToLock()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Header with Streak Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BlockIT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Stop Phone Addiction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            // Streak Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = AccentAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${viewModel.getStreak()}d streak",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Accessibility Permission Notice (if disabled)
        if (!isAccessibilityOn) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1C13)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = AccentAmber
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accessibility Engine Required",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Enable BlockIT service in settings to block app switching.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("Enable", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Center Rotating Focus Dial
        FocusDial(
            selectedMinutes = selectedMinutes,
            onMinutesChanged = { viewModel.setMinutes(it) },
            maxMinutes = 180
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Preset Chips (15m, 25m, 45m, 60m, 90m, 120m)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(15, 25, 45, 60, 90).forEach { preset ->
                val isSelected = selectedMinutes == preset
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) PrimaryIndigo else SurfaceCard)
                        .clickable { viewModel.setMinutes(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${preset}m",
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Allowed Bag Selector
        Text(
            text = "ALLOWED APPS BAG",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(bags) { index, bag ->
                val isSelected = index == selectedBagIndex
                BagChip(
                    bag = bag,
                    isSelected = isSelected,
                    onClick = { selectedBagIndex = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Slide to Lock Action
        SlideToStart(
            onSlideComplete = {
                val currentBag = bags.getOrNull(selectedBagIndex) ?: BagEntity(name = "Default", isDefault = true)
                viewModel.startFocusSession(currentBag)
                onNavigateToLock()
            },
            text = "SLIDE TO LOCK PHONE"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BagChip(
    bag: BagEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.2f) else SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryIndigo else BorderSubtle
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = bag.name,
                color = if (isSelected) Color.White else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${bag.allowedPackages.size} apps allowed",
                color = if (isSelected) PrimaryIndigo else TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
