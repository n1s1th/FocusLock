package com.focuslock.app.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.data.database.entities.SessionEntity
import com.focuslock.app.data.model.FocusProgress
import com.focuslock.app.data.preferences.AppPreferences
import com.focuslock.app.ui.theme.*
import java.time.LocalDate

@Composable
fun WeeklyFocusCard(sessions: List<SessionEntity>, preferences: AppPreferences, onSettings: (() -> Unit)? = null) {
    val revision by preferences.habitRevision.collectAsState()
    val start = FocusProgress.weekStart(LocalDate.now())
    val week = sessions.filter { runCatching { LocalDate.parse(it.dateString) in start..start.plusDays(6) }.getOrDefault(false) }
    val completed = week.count { it.isCompleted }
    val goal = preferences.getWeeklyGoal()
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceBright), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$completed / $goal sessions this week", modifier = Modifier.weight(1f), fontFamily = GoogleSans, color = CharcoalPrimary)
                if (onSettings != null) TextButton(onClick = onSettings, contentPadding = PaddingValues(0.dp)) { Text("My goal") }
            }
            LinearProgressIndicator(progress = { (completed.toFloat() / goal).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = AccentOrange)
            Text("${week.filter { it.isCompleted }.sumOf { it.actualDurationMinutes }} completed min · ${week.filterNot { it.isCompleted }.sumOf { it.actualDurationMinutes }} interrupted min",
                fontFamily = GoogleSans, color = SecondaryGray)
            if (completed >= goal) Text("Weekly goal reached. Your next session is your choice.", color = CharcoalPrimary)
            val achievements = preferences.getAchievements()
            if (achievements.isNotEmpty()) Text(achievements.sorted().joinToString(" · "), color = CharcoalPrimary, fontFamily = GoogleSans)
        }
    }
}

@Composable
fun HabitGoalDialog(preferences: AppPreferences, onDismiss: () -> Unit) {
    var goal by remember { mutableIntStateOf(preferences.getWeeklyGoal()) }
    var days by remember { mutableIntStateOf(preferences.getFocusDaysMask()) }
    AlertDialog(containerColor = com.focuslock.app.ui.theme.SurfaceBright, onDismissRequest = onDismiss, title = { Text("My focus goal") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("$goal completed sessions each week")
            Slider(value = goal.toFloat(), onValueChange = { goal = it.toInt() }, valueRange = 1f..14f, steps = 12)
            Text("Planned focus days. Rest days do not break your streak.")
            Row {
                listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, label ->
                    val bit = 1 shl ((index + 1) % 7)
                    TextButton(onClick = { days = days xor bit }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                        Text(label, color = if (days and bit != 0) AccentOrange else SecondaryGray)
                    }
                }
            }
            Text("A completed session earns streak credit. Previous achievements stay yours.")
        }
    }, confirmButton = { TextButton(enabled = days != 0, onClick = { preferences.saveHabitSettings(goal, days); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun SessionPlanDialog(minutes: Int, bags: List<BagEntity>, selectedBagId: Long, preferences: AppPreferences,
                      onDismiss: () -> Unit, canStart: Boolean = true, onStart: (BagEntity, String, String) -> Unit) {
    var bagId by remember { mutableLongStateOf(selectedBagId) }
    var task by remember(bagId) { mutableStateOf(preferences.getSavedTask(bagId)) }
    var plan by remember(bagId) { mutableStateOf(preferences.getSavedPlan(bagId)) }
    val bag = bags.find { it.id == bagId } ?: bags.firstOrNull()
    AlertDialog(containerColor = com.focuslock.app.ui.theme.SurfaceBright, onDismissRequest = onDismiss, title = { Text("One clear task · $minutes min") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(task, { task = it.take(120) }, label = { Text("What will you work on? (optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(plan, { plan = it.take(160) }, label = { Text("If I want to scroll… (optional)") }, placeholder = { Text("Write it down, then return to my task") }, modifier = Modifier.fillMaxWidth())
            Text("Choose your allowed app bag")
            bags.forEach { item ->
                TextButton(onClick = { bagId = item.id }) { Text((if (bag?.id == item.id) "✓ " else "") + item.name) }
            }
            Text("${bag?.allowedPackages?.count { it.isNotBlank() } ?: 0} apps allowed. Check your bag has the tools you need before starting.")
            Text("Calls remain available. You can adjust allowed apps on the Bags screen.")
            if (!canStart) Text("Enable Accessibility on the Home screen before starting.")
        }
    }, confirmButton = { TextButton(enabled = bag != null && canStart, onClick = { bag?.let { onStart(it, task, plan) } }) { Text("Start focus") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Back") } })
}

@Composable
fun ExitReasonDialog(session: SessionEntity, onReason: (String) -> Unit) {
    AlertDialog(containerColor = com.focuslock.app.ui.theme.SurfaceBright, onDismissRequest = { onReason("") }, title = { Text("Session ended") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${session.actualDurationMinutes} minutes recorded as interrupted. Your completed work stays safe.")
            Text("What got in the way? Optional.")
            listOf("urgent" to "An urgent interruption", "blocked_app" to "I needed a blocked app",
                "too_long" to "The session felt too long", "distraction" to "I wanted to scroll",
                "app_problem" to "An app problem").forEach { (reason, label) ->
                TextButton(onClick = { onReason(reason) }) { Text(label) }
            }
        }
    }, confirmButton = { TextButton(onClick = { onReason("") }) { Text("Skip for now") } })
}
