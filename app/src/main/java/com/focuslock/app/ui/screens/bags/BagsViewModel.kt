package com.focuslock.app.ui.screens.bags

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.FocusLockApp
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.data.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BagsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp

    val bags: StateFlow<List<BagEntity>> = app.database.bagDao().getAllBagsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = app.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

            val appList = resolveInfos.mapNotNull { info ->
                val pkgName = info.activityInfo.packageName
                if (pkgName == app.packageName) null
                else {
                    InstalledApp(
                        appName = info.loadLabel(pm).toString(),
                        packageName = pkgName,
                        icon = info.loadIcon(pm),
                        isSystemApp = false
                    )
                }
            }.distinctBy { it.packageName }.sortedBy { it.appName }

            _installedApps.value = appList
        }
    }

    fun createBag(name: String, allowedPackages: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.bagDao().insertBag(
                BagEntity(
                    name = name,
                    allowedPackages = allowedPackages,
                    isDefault = false
                )
            )
        }
    }

    fun updateBag(bag: BagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.bagDao().updateBag(bag)
        }
    }

    fun deleteBag(bag: BagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.bagDao().deleteBag(bag)
        }
    }
}
