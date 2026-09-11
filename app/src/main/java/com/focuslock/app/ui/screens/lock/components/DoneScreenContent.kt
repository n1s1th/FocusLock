package com.focuslock.app.ui.screens.lock.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.R
import com.focuslock.app.ui.screens.home.components.BlockLogoView
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.GoogleSans
import java.util.Calendar

private val CardBackgroundDark = Color(0xFF191817)
private val CardMutedText = Color(0xFF8E8D8A)
private val IndicatorDark = Color(0xFF2C2A28)
private val IndicatorActive = Color(0xFF454341)

@Composable
fun DoneScreenContent(
    sessionMinutes: Int,
    todayMinutes: Int,
    streak: Int,
    onDoneClick: () -> Unit
) {
    val context = LocalContext.current

    // Determine current day of week (Monday=0 ... Sunday=6)
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
    val dayIndexMondayBased = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
    val weekLetters = listOf("M", "T", "W", "T", "F", "S", "S")

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        val totalH = maxHeight
        val doneCardHeight = (totalH * 0.245f).coerceIn(175.dp, 245.dp)
        val doneBlockSize = (doneCardHeight * 0.098f).coerceIn(16.dp, 23.dp)

        val statsRowHeight = (totalH * 0.24f).coerceIn(165.dp, 230.dp)
        val streakCardHeight = (totalH * 0.155f).coerceIn(100.dp, 145.dp)
        val checkButtonHeight = (totalH * 0.075f).coerceIn(58.dp, 66.dp)
        val shareButtonSize = (totalH * 0.065f).coerceIn(48.dp, 54.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP LOGO (With comfortable top margin)
            BlockLogoView(
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                pixelSize = 4.2.dp,
                color = Color.White,
                pulseColor = AccentOrange
            )

            // 2. DONE PIXEL ART CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(doneCardHeight),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DonePixelArtView(
                        blockSize = doneBlockSize,
                        color = Color.White
                    )
                }
            }

            // 3. STATS ROW (MIN · SESSION | MIN · TODAY)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statsRowHeight),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Session Minutes Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$sessionMinutes",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "MIN · SESSION",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = CardMutedText,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                // Today Total Minutes Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$todayMinutes",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "MIN · TODAY",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = CardMutedText,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }

            // 4. DAY STREAK & WEEKDAYS CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(streakCardHeight),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Streak
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$streak",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            color = AccentOrange
                        )
                        Text(
                            text = "DAY STREAK",
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = CardMutedText,
                            letterSpacing = 1.sp
                        )
                    }

                    // Right: Weekdays row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weekLetters.forEachIndexed { index, letter ->
                            val isToday = index == dayIndexMondayBased
                            val isPastOrToday = index <= dayIndexMondayBased

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Square indicator above day
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isPastOrToday) IndicatorActive else IndicatorDark)
                                )
                                // Day letter
                                Text(
                                    text = letter,
                                    fontFamily = GoogleSans,
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (isToday) Color.White else CardMutedText
                                )
                            }
                        }
                    }
                }
            }

            // 5. Large White Pill Checkmark Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(checkButtonHeight)
                    .clip(RoundedCornerShape(checkButtonHeight / 2))
                    .background(Color.White)
                    .clickable { onDoneClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Done",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 6. White Circular Share Button
            Box(
                modifier = Modifier
                    .size(shareButtonSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        shareSessionStats(context, sessionMinutes, streak)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun shareSessionStats(context: Context, minutes: Int, streak: Int) {
    val shareText = "I just crushed a $minutes-minute focus session on BlockIT! Current streak: $streak days. 🎯 #BlockIT #Focus"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "BlockIT Focus Session Completed")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Session"))
}
