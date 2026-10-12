package com.focuslock.app.ui.screens.routines

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focuslock.app.data.database.entities.RoutineEntity

@Composable
fun RoutineHabitDialog(routine: RoutineEntity, onDismiss: () -> Unit, onSave: (String, Boolean) -> Unit) {
    var cue by remember { mutableStateOf(routine.cue) }
    var reminderOnly by remember { mutableStateOf(routine.reminderOnly) }
    AlertDialog(containerColor = com.focuslock.app.ui.theme.SurfaceBright, onDismissRequest = onDismiss, title = { Text("My routine cue") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(cue, { cue = it.take(80) }, label = { Text("After…") }, placeholder = { Text("Breakfast, arriving at my desk…") }, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Remind me instead of locking", modifier = Modifier.weight(1f))
                Switch(checked = reminderOnly, onCheckedChange = { reminderOnly = it })
            }
            Text(if (reminderOnly) "Tap the reminder to prepare your session. Notification permission is required." else "This enabled routine starts a focus session automatically.")
            Text("Keep the routine switch on to schedule the selected mode.")
        }
    }, confirmButton = { TextButton(onClick = { onSave(cue, reminderOnly); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
