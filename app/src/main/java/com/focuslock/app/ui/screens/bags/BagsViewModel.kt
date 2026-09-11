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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BagsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    private val preferences = app.preferences

    val bags: StateFlow<List<BagEntity>> = app.database.bagDao().getAllBagsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    // Parachute States
    private val _totalParachutes = MutableStateFlow(preferences.getTotalParachutes())
    val totalParachutes: StateFlow<Int> = _totalParachutes.asStateFlow()

    private val _isWeeklyAvailable = MutableStateFlow(preferences.isWeeklyParachuteAvailable())
    val isWeeklyAvailable: StateFlow<Boolean> = _isWeeklyAvailable.asStateFlow()

    private val _weeklyRemainingMillis = MutableStateFlow(preferences.getWeeklyParachuteRemainingMillis())
    val weeklyRemainingMillis: StateFlow<Long> = _weeklyRemainingMillis.asStateFlow()

    private val _isRequestActive = MutableStateFlow(preferences.isParachuteRequestActive())
    val isRequestActive: StateFlow<Boolean> = _isRequestActive.asStateFlow()

    private val _isRequestReady = MutableStateFlow(preferences.isParachuteRequestReady())
    val isRequestReady: StateFlow<Boolean> = _isRequestReady.asStateFlow()

    private val _requestRemainingMillis = MutableStateFlow(preferences.getParachuteRequestRemainingMillis())
    val requestRemainingMillis: StateFlow<Long> = _requestRemainingMillis.asStateFlow()

    val currentStreak: Int
        get() = preferences.getCurrentStreak()

    fun getSelectedBagIndex(): Int = preferences.getSelectedBagIndex()

    fun setSelectedBag(bag: BagEntity, index: Int) {
        preferences.setSelectedBagId(bag.id)
        preferences.setSelectedBagIndex(index)
        val nonBlank = bag.allowedPackages.filter { it.isNotBlank() }
        preferences.setActiveAllowedPackages(nonBlank)
        com.focuslock.app.service.FocusAccessibilityService.updateAllowedPackages(nonBlank.toSet())
    }

    init {
        loadInstalledApps()
        ensureDefaultBags()
        startParachuteTimerLoop()
    }

    private fun ensureDefaultBags() {
        viewModelScope.launch(Dispatchers.IO) {
            val bagDao = app.database.bagDao()
            val existing = bagDao.getAllBags()
            if (existing.isEmpty()) {
                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 1",
                        allowedPackages = emptyList(),
                        isDefault = true
                    )
                )
                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 2",
                        allowedPackages = emptyList(),
                        isDefault = false
                    )
                )
                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 3",
                        allowedPackages = emptyList(),
                        isDefault = false
                    )
                )
            } else if (existing.size == 1) {
                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 2",
                        allowedPackages = emptyList(),
                        isDefault = false
                    )
                )
                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 3",
                        allowedPackages = emptyList(),
                        isDefault = false
                    )
                )
            } else if (existing.size == 2) {
                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 3",
                        allowedPackages = emptyList(),
                        isDefault = false
                    )
                )
            }
        }
    }

    private fun startParachuteTimerLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                updateParachuteStates()
                delay(1000L)
            }
        }
    }

    fun updateParachuteStates() {
        _totalParachutes.value = preferences.getTotalParachutes()
        _isWeeklyAvailable.value = preferences.isWeeklyParachuteAvailable()
        _weeklyRemainingMillis.value = preferences.getWeeklyParachuteRemainingMillis()
        _isRequestActive.value = preferences.isParachuteRequestActive()
        _isRequestReady.value = preferences.isParachuteRequestReady()
        _requestRemainingMillis.value = preferences.getParachuteRequestRemainingMillis()
    }

    fun requestParachute() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.requestParachute()
            updateParachuteStates()
        }
    }

    fun claimParachute() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.claimRequestedParachute()
            updateParachuteStates()
        }
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

    fun updateBagAllowedPackages(bag: BagEntity, newPackages: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val filtered = newPackages.take(6)
            app.database.bagDao().updateBag(bag.copy(allowedPackages = filtered))
            if (bag.id == preferences.getSelectedBagId() || bag.id == preferences.getActiveBagId()) {
                val nonBlank = filtered.filter { it.isNotBlank() }
                preferences.setActiveAllowedPackages(nonBlank)
                com.focuslock.app.service.FocusAccessibilityService.updateAllowedPackages(nonBlank.toSet())
            }
        }
    }

    fun setAppInSlot(bag: BagEntity, slotIndex: Int, packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = bag.allowedPackages.toMutableList()
            while (currentList.size <= slotIndex) {
                currentList.add("")
            }
            currentList[slotIndex] = packageName
            val filtered = currentList.take(6)
            app.database.bagDao().updateBag(bag.copy(allowedPackages = filtered))
            if (bag.id == preferences.getSelectedBagId() || bag.id == preferences.getActiveBagId()) {
                val nonBlank = filtered.filter { it.isNotBlank() }
                preferences.setActiveAllowedPackages(nonBlank)
                com.focuslock.app.service.FocusAccessibilityService.updateAllowedPackages(nonBlank.toSet())
            }
        }
    }

    fun removeAppFromSlot(bag: BagEntity, slotIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = bag.allowedPackages.toMutableList()
            if (slotIndex in currentList.indices) {
                currentList[slotIndex] = ""
                val filtered = currentList.take(6)
                app.database.bagDao().updateBag(bag.copy(allowedPackages = filtered))
                if (bag.id == preferences.getSelectedBagId() || bag.id == preferences.getActiveBagId()) {
                    val nonBlank = filtered.filter { it.isNotBlank() }
                    preferences.setActiveAllowedPackages(nonBlank)
                    com.focuslock.app.service.FocusAccessibilityService.updateAllowedPackages(nonBlank.toSet())
                }
            }
        }
    }

    fun createBag(name: String, allowedPackages: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.bagDao().insertBag(
                BagEntity(
                    name = name,
                    allowedPackages = allowedPackages.take(6),
                    isDefault = false
                )
            )
        }
    }

    fun updateBag(bag: BagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.bagDao().updateBag(bag.copy(allowedPackages = bag.allowedPackages.take(6)))
        }
    }

    fun deleteBag(bag: BagEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.bagDao().deleteBag(bag)
        }
    }
}
