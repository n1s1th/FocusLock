package com.focuslock.app.ui.screens.routines

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.data.database.entities.RoutineEntity
import com.focuslock.app.ui.theme.AccentRose
import com.focuslock.app.ui.theme.BackgroundDark
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SecondaryEmerald
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = viewModel()
) {
    val routines by viewModel.routines.collectAsState()
    val bags by viewModel.bags.collectAsState()
    var showEditor by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Automated Routines",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Schedule recurring focus locks without limits",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No routines scheduled.\nTap + to create your first auto-session.",
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(routines, key = { it.id }) { routine ->
                        RoutineItemCard(
                            routine = routine,
                            onToggle = { viewModel.toggleRoutine(routine) },
                            onDelete = { viewModel.deleteRoutine(routine) }
                        )
                    }
                }
            }
        }

        // Floating Add Routine Button
        FloatingActionButton(
            onClick = { showEditor = true },
            containerColor = PrimaryIndigo,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine")
        }

        if (showEditor) {
            RoutineEditorDialog(
                bags = bags,
                onDismiss = { showEditor = false },
                onSave = { name, bagId, hour, min, dur, mask ->
                    viewModel.createRoutine(name, bagId, hour, min, dur, mask)
                    showEditor = false
                }
            )
        }
    }
}

@Composable
fun RoutineItemCard(
    routine: RoutineEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = String.format(Locale.US, "%02d:%02d", routine.startHour, routine.startMinute)

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SecondaryEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SecondaryEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = routine.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Starts at $timeFormatted • ${routine.durationMinutes} mins",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AccentRose)
                }
            }
        }
    }
}
