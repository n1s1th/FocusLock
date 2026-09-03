package com.focuslock.app.ui.screens.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.data.database.entities.SessionEntity
import com.focuslock.app.ui.theme.AccentAmber
import com.focuslock.app.ui.theme.AccentRose
import com.focuslock.app.ui.theme.BackgroundDark
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SecondaryEmerald
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val lifetimeMinutes by viewModel.totalLifetimeMinutes.collectAsState()
    val todayMinutes by viewModel.todayMinutes.collectAsState()
    val totalCompleted by viewModel.totalCompletedSessions.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Focus Analytics",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Track your productivity and screen-time recovery",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2x2 Metric Cards Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Today Focus",
                value = "${todayMinutes ?: 0}m",
                icon = Icons.Default.HourglassBottom,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Daily Streak",
                value = "${viewModel.getStreak()}d",
                icon = Icons.Default.LocalFireDepartment,
                iconColor = AccentAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val lifetimeHours = (lifetimeMinutes ?: 0) / 60
            MetricCard(
                title = "Total Focus",
                value = if (lifetimeHours > 0) "${lifetimeHours}h ${(lifetimeMinutes ?: 0) % 60}m" else "${lifetimeMinutes ?: 0}m",
                icon = Icons.Default.CheckCircle,
                iconColor = SecondaryEmerald,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Completed",
                value = "$totalCompleted",
                icon = Icons.Default.Shield,
                iconColor = AccentRose,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RECENT SESSIONS",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (recentSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No focus sessions recorded yet.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recentSessions, key = { it.id }) { session ->
                    SessionHistoryItem(session = session)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun SessionHistoryItem(session: SessionEntity) {
    val timeFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(session.startTime))

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = session.bagName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$timeFormatted • ${session.distractionsBlockedCount} distractions blocked",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "${session.actualDurationMinutes} mins",
                color = if (session.isCompleted) SecondaryEmerald else AccentRose,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
