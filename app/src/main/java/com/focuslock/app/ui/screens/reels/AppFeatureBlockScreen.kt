package com.focuslock.app.ui.screens.reels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.data.database.entities.ReelBlockAppEntity
import com.focuslock.app.ui.theme.CharcoalPrimary
import com.focuslock.app.ui.theme.GoogleSans
import com.focuslock.app.ui.theme.ScreenBackground
import com.focuslock.app.ui.theme.SurfaceBright
import com.focuslock.app.ui.theme.SurfaceVariant
import com.focuslock.app.FocusLockApp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppFeatureBlockScreenLoader(
    packageName: String,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var appEntity by remember { mutableStateOf<ReelBlockAppEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(packageName) {
        val appDatabase = FocusLockApp.instance.database
        var entity = appDatabase.reelBlockAppDao().getReelBlockApp(packageName)
        if (entity == null) {
            val pm = context.packageManager
            val appName = try {
                pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
            } catch (e: Exception) {
                packageName
            }
            entity = ReelBlockAppEntity(packageName = packageName, appName = appName)
        }
        appEntity = entity
        isLoading = false
    }

    if (isLoading || appEntity == null) {
        Box(modifier = Modifier.fillMaxSize().background(ScreenBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        AppFeatureBlockScreen(
            appEntity = appEntity!!,
            onSave = { updatedEntity ->
                coroutineScope.launch {
                    FocusLockApp.instance.database.reelBlockAppDao().insertApp(updatedEntity)
                    withContext(Dispatchers.Main) {
                        onBack()
                    }
                }
            },
            onCancel = onBack
        )
    }
}

@Composable
fun AppFeatureBlockScreen(
    appEntity: ReelBlockAppEntity,
    onSave: (ReelBlockAppEntity) -> Unit,
    onCancel: () -> Unit
) {
    var blockReels by remember { mutableStateOf(appEntity.blockReels) }
    var blockStories by remember { mutableStateOf(appEntity.blockStories) }
    var blockMarketplace by remember { mutableStateOf(appEntity.blockMarketplace) }
    var blockGaming by remember { mutableStateOf(appEntity.blockGaming) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Configure ${appEntity.appName}",
            fontFamily = GoogleSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = CharcoalPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        FeatureToggleRow("Block Reels/Shorts", blockReels) { blockReels = it }
        Spacer(modifier = Modifier.height(12.dp))
        FeatureToggleRow("Block Stories", blockStories) { blockStories = it }
        Spacer(modifier = Modifier.height(12.dp))
        FeatureToggleRow("Block Marketplace", blockMarketplace) { blockMarketplace = it }
        Spacer(modifier = Modifier.height(12.dp))
        FeatureToggleRow("Block Gaming", blockGaming) { blockGaming = it }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)) {
                Text("Cancel", color = CharcoalPrimary)
            }
            Button(
                onClick = {
                    onSave(appEntity.copy(
                        blockReels = blockReels,
                        blockStories = blockStories,
                        blockMarketplace = blockMarketplace,
                        blockGaming = blockGaming
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalPrimary)
            ) {
                Text("Save Settings", color = SurfaceBright)
            }
        }
    }
}

@Composable
fun FeatureToggleRow(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceBright),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontFamily = GoogleSans, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
            Switch(checked = isChecked, onCheckedChange = onCheckedChange)
        }
    }
}
