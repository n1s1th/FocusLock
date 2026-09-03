package com.focuslock.app.ui.screens.bags

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.ui.theme.AccentRose
import com.focuslock.app.ui.theme.BackgroundDark
import com.focuslock.app.ui.theme.PrimaryIndigo
import com.focuslock.app.ui.theme.SecondaryEmerald
import com.focuslock.app.ui.theme.SurfaceCard
import com.focuslock.app.ui.theme.SurfaceDark
import com.focuslock.app.ui.theme.TextMuted
import com.focuslock.app.ui.theme.TextPrimary
import com.focuslock.app.ui.theme.TextSecondary

@Composable
fun BagsScreen(
    viewModel: BagsViewModel = viewModel()
) {
    val bags by viewModel.bags.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    var editingBag by remember { mutableStateOf<BagEntity?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newBagName by remember { mutableStateOf("") }

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
                text = "Allowed App Bags",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Group apps permitted during your focus periods",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bags, key = { it.id }) { bag ->
                    BagItemCard(
                        bag = bag,
                        onEdit = {
                            editingBag = bag
                            showAppPicker = true
                        },
                        onDelete = { viewModel.deleteBag(bag) }
                    )
                }
            }
        }

        // Floating Add Bag Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = PrimaryIndigo,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bag")
        }

        // App Picker Sheet
        if (showAppPicker && editingBag != null) {
            AppPickerSheet(
                initialSelectedPackages = editingBag!!.allowedPackages,
                installedApps = installedApps,
                onDismiss = {
                    showAppPicker = false
                    editingBag = null
                },
                onSave = { updatedPackages ->
                    viewModel.updateBag(editingBag!!.copy(allowedPackages = updatedPackages))
                    showAppPicker = false
                    editingBag = null
                }
            )
        }

        // Create Bag Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = SurfaceDark,
                title = { Text("Create New Bag", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Give your app collection a name:", color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newBagName,
                            onValueChange = { newBagName = it },
                            placeholder = { Text("e.g., Coding, Writing, Gym", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newBagName.isNotBlank()) {
                                viewModel.createBag(newBagName, emptyList())
                                newBagName = ""
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun BagItemCard(
    bag: BagEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                        .background(PrimaryIndigo.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Workspaces,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = bag.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${bag.allowedPackages.size} apps whitelisted",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary)
                }
                if (!bag.isDefault) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AccentRose)
                    }
                }
            }
        }
    }
}
