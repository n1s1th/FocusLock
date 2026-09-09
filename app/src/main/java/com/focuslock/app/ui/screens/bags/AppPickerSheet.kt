package com.focuslock.app.ui.screens.bags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.focuslock.app.data.model.InstalledApp
import com.focuslock.app.ui.theme.AccentOrange
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SecondaryGray
import com.focuslock.app.ui.theme.SurfaceDark
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary
import com.focuslock.app.ui.theme.GoogleSans

private const val MAX_BAG_SLOTS = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(
    initialSelectedPackages: List<String>,
    installedApps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedPackages = remember { mutableStateListOf<String>().apply { addAll(initialSelectedPackages) } }
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    val isFull = selectedPackages.size >= MAX_BAG_SLOTS

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            // Header + count indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Select Allowed Apps",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSans
                    )
                    Text(
                        text = "Choose up to $MAX_BAG_SLOTS apps for this bag",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontFamily = GoogleSans
                    )
                }
                // Fix 9: Slot count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFull) AccentOrange else CharcoalPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${selectedPackages.size} / $MAX_BAG_SLOTS",
                        color = if (isFull) Color.White else CharcoalPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSans
                    )
                }
            }

            // Fix 9: Show a warning row when at max capacity
            if (isFull) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentOrange.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠ Bag is full! Remove an app to add another.",
                        color = AccentOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GoogleSans
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search installed apps...", color = TextMuted, fontFamily = GoogleSans) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = Color(0xFF2E334D),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // App List
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isChecked = selectedPackages.contains(app.packageName)
                    // Fix 9: Disable adding more apps when at max (but allow unchecking)
                    val canToggle = isChecked || !isFull

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canToggle) {
                                if (isChecked) selectedPackages.remove(app.packageName)
                                else if (!isFull) selectedPackages.add(app.packageName)
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (!checked) selectedPackages.remove(app.packageName)
                                else if (!isFull) selectedPackages.add(app.packageName)
                            },
                            enabled = canToggle,
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryIndigo,
                                disabledUncheckedColor = SecondaryGray.copy(alpha = 0.35f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = app.appName,
                                color = if (canToggle) TextPrimary else TextMuted,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                fontFamily = GoogleSans
                            )
                            Text(
                                text = app.packageName,
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = GoogleSans
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Action Button
            Button(
                onClick = { onSave(selectedPackages.toList()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Save Apps (${selectedPackages.size}/$MAX_BAG_SLOTS)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
