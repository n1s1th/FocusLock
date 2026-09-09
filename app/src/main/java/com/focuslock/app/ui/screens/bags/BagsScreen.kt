package com.focuslock.app.ui.screens.bags

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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.R
import com.focuslock.app.data.database.entities.BagEntity
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

@Composable
fun BagsScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: BagsViewModel = viewModel()
) {
    val bags by viewModel.bags.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    var selectedBagIndex by remember { mutableIntStateOf(0) }
    var editingBag by remember { mutableStateOf<BagEntity?>(null) }
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

        // 1. TOP HEADER
        TopHeaderBar(
            streakCount = 10,
            parachuteCount = 0,
            onProfileClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. TOP WHITE CARD (3 Pixel-Art Bags)
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

        // 3. MIDDLE SECTION (Bag Tabs + App Slot Grid)
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
                // Bag Selector Pill Tabs
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

                // App Slots 3x2 Grid
                // Row 1: Active App Slots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppSlotCard(
                        iconRes = null,
                        fallbackText = "WA",
                        bgColor = Color(0xFF25D366),
                        onClick = {
                            editingBag = currentBag
                            showAppPicker = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AppSlotCard(
                        iconRes = null,
                        fallbackText = "FD",
                        bgColor = Color(0xFF2563EB),
                        onClick = {
                            editingBag = currentBag
                            showAppPicker = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AppSlotCard(
                        iconRes = null,
                        fallbackText = "CLK",
                        bgColor = SurfaceBright,
                        onClick = {
                            editingBag = currentBag
                            showAppPicker = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Locked Slots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LockedSlotCard(modifier = Modifier.weight(1f))
                    LockedSlotCard(modifier = Modifier.weight(1f))
                    LockedSlotCard(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. BOTTOM PURCHASE / PARACHUTE SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Parachute Card
            Card(
                modifier = Modifier
                    .width(92.dp)
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Small orange badge at top-right
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
                            .padding(vertical = 16.dp),
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
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "0",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = CharcoalPrimary
                        )
                    }
                }
            }

            // Right 2-column purchase options
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Col 1: +1 & +3 tiles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PurchaseTile(
                        count = "+1",
                        price = "LKR 500.00",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    PurchaseTile(
                        count = "+3",
                        price = "LKR 1,025.00",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                // Col 2: +10 tall tile
                PurchaseTile(
                    count = "+10",
                    price = "LKR 2,675.00",
                    isLarge = true,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(180.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // App Picker Sheet
    if (showAppPicker && editingBag != null) {
        AppPickerSheet(
            initialSelectedPackages = editingBag!!.allowedPackages,
            installedApps = installedApps,
            onDismiss = {
                showAppPicker = false
                editingBag = null
            },
            onSave = { updatedPackages ->
                viewModel.updateBag(editingBag!!.copy(allowedPackages = updatedPackages))
                showAppPicker = false
                editingBag = null
            }
        )
    }
}

@Composable
fun AppSlotCard(
    iconRes: Int?,
    fallbackText: String,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fallbackText,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (bgColor == SurfaceBright) CharcoalPrimary else Color.White
                )
            }
        }
    }
}

@Composable
fun LockedSlotCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = "Locked Slot",
                tint = AccentOrange.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PurchaseTile(
    count: String,
    price: String,
    isLarge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = if (isLarge) 36.sp else 26.sp,
                color = CharcoalPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = price,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = SecondaryGray
            )
        }
    }
}
