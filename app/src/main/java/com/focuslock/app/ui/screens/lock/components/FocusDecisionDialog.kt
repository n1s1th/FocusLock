package com.focuslock.app.ui.screens.lock.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focuslock.app.ui.theme.GoogleSans
import kotlinx.coroutines.delay

@Composable
fun FocusDecisionDialog(task: String, plan: String, remainingMinutes: Int,
                        onContinue: () -> Unit, onReset: () -> Unit, onExit: () -> Unit) {
    var seconds by remember { mutableIntStateOf(5) }
    LaunchedEffect(Unit) { while (seconds > 0) { delay(1000); seconds-- } }
    AlertDialog(onDismissRequest = onContinue, containerColor = Color(0xFF191817),
        title = { Text("Take a moment", color = Color.White, fontFamily = GoogleSans) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$remainingMinutes minutes remain. ${task.ifBlank { "Return to your next small step." }}", color = Color.White)
                if (plan.isNotBlank()) Text("If the urge appears: $plan", color = Color.LightGray)
                Text(if (seconds > 0) "One slow breath · $seconds seconds (optional)" else "Ready when you are.", color = Color.LightGray)
                TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Keep focusing", color = Color.White) }
                TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Take a 60-second reset", color = Color.White) }
            }
        }, confirmButton = { TextButton(onClick = onExit) { Text("End now · use 1 parachute", color = Color.White) } },
        dismissButton = { TextButton(onClick = onContinue) { Text("Back", color = Color.White) } })
}
