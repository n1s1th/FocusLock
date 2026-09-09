package com.focuslock.app.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.R
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.OutlineSubtle
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SurfaceBright

@Composable
fun TopHeaderBar(
    streakCount: Int = 10,
    parachuteCount: Int = 0,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                text = "$streakCount",
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
                text = "$parachuteCount",
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
                    .clickable { onProfileClick() },
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
}
