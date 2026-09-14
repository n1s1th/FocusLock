package com.focuslock.app.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.ui.theme.SurfaceVariant

@Composable
fun MainModeSwitch(
    isReelMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceBright)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (!isReelMode) CharcoalPrimary else SurfaceBright)
                .clickable { onModeChange(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Phone Block",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (!isReelMode) SurfaceBright else CharcoalPrimary,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isReelMode) CharcoalPrimary else SurfaceBright)
                .clickable { onModeChange(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reel Block",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isReelMode) SurfaceBright else CharcoalPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
