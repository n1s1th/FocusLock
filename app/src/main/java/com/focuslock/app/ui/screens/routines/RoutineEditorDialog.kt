package com.focuslock.app.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.SurfaceDark
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorDialog(
    bags: List<BagEntity>,
    onDismiss: () -> Unit,
    onSave: (name: String, bagId: Long, hour: Int, minute: Int, duration: Int, daysMask: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedBag by remember { mutableStateOf(bags.firstOrNull()) }
    var isBagDropdownExpanded by remember { mutableStateOf(false) }

    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(60) }
    var activeDaysMask by remember { mutableIntStateOf(127) } // All days

    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("New Scheduled Routine", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    placeholder = { Text("e.g., Morning Deep Work") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bag Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = isBagDropdownExpanded,
                    onExpandedChange = { isBagDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBag?.name ?: "Select Bag",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Allowed App Bag") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBagDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isBagDropdownExpanded,
                        onDismissRequest = { isBagDropdownExpanded = false },
                        modifier = Modifier.background(SurfaceCard)
                    ) {
                        bags.forEach { bag ->
                            DropdownMenuItem(
                                text = { Text(bag.name, color = TextPrimary) },
                                onClick = {
                                    selectedBag = bag
                                    isBagDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Days Selector Chips
                Text("Repeat on days:", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEachIndexed { index, label ->
                        val dayBit = 1 shl index
                        val isDaySelected = (activeDaysMask and dayBit) != 0
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDaySelected) PrimaryIndigo else SurfaceCard)
                                .clickable {
                                    activeDaysMask = if (isDaySelected) activeDaysMask and dayBit.inv() else activeDaysMask or dayBit
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isDaySelected) Color.White else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time and Duration summary
                Text(
                    text = "Starts at: %02d:%02d • Duration: %d mins".format(startHour, startMinute, durationMinutes),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedBag != null) {
                        onSave(name, selectedBag!!.id, startHour, startMinute, durationMinutes, activeDaysMask)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Save Routine", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
