package com.focuslock.app.ui.screens.lock

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.focuslock.app.FocusLockApp
import com.focuslock.app.R
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.service.FocusLockService
import com.focuslock.app.ui.screens.home.components.BlockLogoView
import com.focuslock.app.ui.screens.home.components.DigitalTimerDisplay
import com.focuslock.app.ui.screens.home.components.SquareDigit
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.GoogleSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val CardBackgroundDark = Color(0xFF191817)
private val CardMutedText = Color(0xFF8E8D8A)
private val CardAccentHandle = Color(0xFF454341)

@Composable
fun LockScreenContent(
    onExitLock: () -> Unit
) {
    val context = LocalContext.current
    val app = FocusLockApp.instance

    var remainingMillis by remember { mutableLongStateOf(0L) }
    var bagEntity by remember { mutableStateOf<BagEntity?>(null) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showNoParachuteDialog by remember { mutableStateOf(false) }

    val totalParachutes by app.preferences.parachutesStateFlow.collectAsState()

    // Live countdown loop
    LaunchedEffect(Unit) {
        val bagId = app.preferences.getActiveBagId()
        bagEntity = app.database.bagDao().getBagById(bagId)

        while (true) {
            val endMillis = app.preferences.getSessionEndTimeMillis()
            val remaining = endMillis - System.currentTimeMillis()
            if (remaining <= 0) {
                onExitLock()
                break
            }
            remainingMillis = remaining
            delay(250L)
        }
    }

    val totalSeconds = (remainingMillis / 1000).toInt()
    val totalMinutes = totalSeconds / 60
    val displayHours = totalMinutes / 60
    val displayMinutes = totalMinutes % 60
    val displaySeconds = totalSeconds % 60

    // Formatted Expected End Time
    val endTimeMillis = app.preferences.getSessionEndTimeMillis()
    val formattedEndTime = remember(endTimeMillis) {
        if (endTimeMillis > 0) {
            SimpleDateFormat("h:mm a", Locale.US).format(Date(endTimeMillis))
        } else {
            "--:--"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP LOGO
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(10.dp))
                BlockLogoView(
                    pixelSize = 4.8.dp,
                    color = Color.White,
                    pulseColor = AccentOrange
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 2. BIG DIGITAL TIMER CARD (HH:MM / MM:SS)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        DigitalTimerDisplay(
                            hours = displayHours,
                            minutes = displayMinutes,
                            blockSize = 16.dp,
                            digitColor = Color.White,
                            colonColor = CardMutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. MIDDLE 2-COLUMN SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT COLUMN
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Seconds Tile
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(176.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val s1 = displaySeconds / 10
                                    val s2 = displaySeconds % 10
                                    SquareDigit(digit = s1, blockSize = 15.dp, color = Color.White)
                                    SquareDigit(digit = s2, blockSize = 15.dp, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "SEC",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CardMutedText,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        // Ends Time Pill
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "ENDS",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = CardMutedText,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formattedEndTime,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // Parachutes Count Pill
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(CardAccentHandle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_parachute),
                                        contentDescription = "Parachutes",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(13.dp)
                                            .rotate(-25f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "$totalParachutes",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // RIGHT COLUMN
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // +15 min Extension Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(176.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .clickable {
                                    // Add 15 minutes extension to active session
                                    val currentEnd = app.preferences.getSessionEndTimeMillis()
                                    val newEnd = currentEnd + (15 * 60 * 1000L)
                                    val totalMin = app.preferences.getSessionTotalDurationMinutes() + 15
                                    app.preferences.startSession(
                                        endTimeMillis = newEnd,
                                        durationMinutes = totalMin,
                                        bagId = app.preferences.getActiveBagId(),
                                        bagName = app.preferences.getActiveBagName()
                                    )
                                },
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "+15",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 38.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "min",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = CardMutedText
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(CardAccentHandle)
                                        )
                                    }
                                }
                            }
                        }

                        // Ambient Rain / Focus Sound Tile
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(102.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(CardMutedText)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "RAIN",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = CardMutedText,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(CardMutedText)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. BOTTOM ACTION SECTION (Slide to Exit + Allowed Apps Dock)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emergency Exit Slider
                SlideToExitTrack(
                    onTriggerExit = {
                        if (totalParachutes > 0) {
                            showEmergencyDialog = true
                        } else {
                            showNoParachuteDialog = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Allowed Apps Dock & Emergency Dialer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fix 8: Show all 6 allowed app slots
                    val allowedPackages = (bagEntity?.allowedPackages ?: emptyList()).filter { it.isNotBlank() }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(CardBackgroundDark)
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..5) {
                            val pkg = allowedPackages.getOrNull(i)
                            if (!pkg.isNullOrBlank()) {
                                AllowedAppCircle(
                                    packageName = pkg,
                                    onClick = {
                                        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                                        if (intent != null) {
                                            context.startActivity(intent)
                                        }
                                    }
                                )
                            } else {
                                EmptyAllowedSlotCircle()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // White Emergency Phone Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
                                context.startActivity(dialIntent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "Emergency Phone",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Parachute Emergency Confirmation Dialog
        if (showEmergencyDialog) {
            AlertDialog(
                onDismissRequest = { showEmergencyDialog = false },
                containerColor = CardBackgroundDark,
                title = {
                    Text(
                        text = "Use 1 Parachute?",
                        fontFamily = GoogleSans,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "You have $totalParachutes parachute(s) available. Using 1 will end this focus session immediately without penalty.",
                        fontFamily = GoogleSans,
                        color = CardMutedText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEmergencyDialog = false
                            val success = app.preferences.useParachute()
                            if (success) {
                                FocusLockService.stopService(context)
                                onExitLock()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Text(
                            text = "Use Parachute",
                            fontFamily = GoogleSans,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmergencyDialog = false }) {
                        Text(
                            text = "Keep Focusing",
                            fontFamily = GoogleSans,
                            color = Color.White
                        )
                    }
                }
            )
        }

        // No Parachutes Available Dialog
        if (showNoParachuteDialog) {
            AlertDialog(
                onDismissRequest = { showNoParachuteDialog = false },
                containerColor = CardBackgroundDark,
                title = {
                    Text(
                        text = "No Parachutes Available",
                        fontFamily = GoogleSans,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "You currently have 0 parachutes. You receive 1 free parachute every week, or you can request extra ones with a 5-hour cooldown in the Bags tab.",
                        fontFamily = GoogleSans,
                        color = CardMutedText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showNoParachuteDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CardAccentHandle)
                    ) {
                        Text(
                            text = "Got It",
                            fontFamily = GoogleSans,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SlideToExitTrack(
    onTriggerExit: () -> Unit
) {
    // Fix 7: Real horizontal drag gesture — must slide 75% across to trigger exit
    val scope = rememberCoroutineScope()
    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val density = LocalDensity.current

    val containerHeight = 64.dp
    val handleWidth = 62.dp
    val handleHeight = 48.dp
    val trackPadding = 8.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight)
            .clip(RoundedCornerShape(32.dp))
            .background(CardBackgroundDark)
            .padding(trackPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        val maxDragPx = with(density) {
            (maxWidth - handleWidth - trackPadding * 2).toPx()
        }

        // Background label
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SLIDE TO EXIT",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CardMutedText,
                letterSpacing = 2.sp
            )
        }

        // Draggable parachute knob
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .width(handleWidth)
                .height(handleHeight)
                .clip(RoundedCornerShape(24.dp))
                .background(CardAccentHandle)
                .pointerInput(maxDragPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= maxDragPx * 0.75f) {
                                    // Committed: slide fully right then trigger
                                    offsetX.animateTo(maxDragPx, androidx.compose.animation.core.tween(120))
                                    onTriggerExit()
                                }
                                // Snap back to start
                                offsetX.animateTo(0f, androidx.compose.animation.core.tween(280))
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(0f, maxDragPx)
                                offsetX.snapTo(next)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_parachute),
                contentDescription = "Parachute Exit",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(-25f)
            )
        }
    }
}

@Composable
fun AllowedAppCircle(
    packageName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    var appIcon by remember { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
    var appLabel by remember { mutableStateOf("") }

    LaunchedEffect(packageName) {
        try {
            val info = pm.getApplicationInfo(packageName, 0)
            appIcon = pm.getApplicationIcon(info)
            appLabel = pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            appLabel = packageName.take(2).uppercase()
        }
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(CardAccentHandle)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val bitmap = remember(appIcon) {
            try {
                appIcon?.toBitmap(80, 80)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = appLabel,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = appLabel.take(1).uppercase(),
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun EmptyAllowedSlotCircle() {
    Canvas(modifier = Modifier.size(36.dp)) {
        val strokeWidth = 1.2.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw subtle dashed/dotted circle
        val dotCount = 12
        for (i in 0 until dotCount) {
            val angle = (i * 360f / dotCount) * (Math.PI / 180f).toFloat()
            val dx = center.x + radius * kotlin.math.cos(angle)
            val dy = center.y + radius * kotlin.math.sin(angle)
            drawCircle(
                color = CardMutedText.copy(alpha = 0.6f),
                radius = 1.dp.toPx(),
                center = Offset(dx, dy)
            )
        }

        // Draw small + inside
        val plusSize = 3.5.dp.toPx()
        drawLine(
            color = CardMutedText.copy(alpha = 0.6f),
            start = Offset(center.x - plusSize, center.y),
            end = Offset(center.x + plusSize, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = CardMutedText.copy(alpha = 0.6f),
            start = Offset(center.x, center.y - plusSize),
            end = Offset(center.x, center.y + plusSize),
            strokeWidth = 1.dp.toPx()
        )
    }
}

