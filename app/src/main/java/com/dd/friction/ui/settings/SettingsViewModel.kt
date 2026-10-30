package com.dd.friction.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd.friction.domain.model.FrictionSettings
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.model.SettingChange
import com.dd.friction.domain.model.UnlockPhrase
import com.dd.friction.domain.repository.AppRepository
import com.dd.friction.domain.repository.SettingsRepository
import com.dd.friction.domain.usecase.ResolveHomeAppsUseCase
import com.dd.friction.ui.model.AppItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: FrictionSettings = FrictionSettings(),
    /** Installed apps that are marked as distractions, alphabetical. */
    val distractions: List<InstalledApp> = emptyList(),
    /** Apps on the home screen, in display order. */
    val homeApps: List<AppItem> = emptyList(),
    /** Apps that can still be added to the home screen, alphabetical. */
    val homeCandidates: List<AppItem> = emptyList(),
) {
    val canAddHomeApp: Boolean get() = homeApps.size < FrictionSettings.MAX_HOME_APPS
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val resolveHomeApps: ResolveHomeAppsUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        appRepository.installedApps,
        settingsRepository.settings,
    ) { apps, settings ->
        val distractions = settings.distractionPackages
        fun InstalledApp.toItem() = AppItem(this, packageName in distractions)
        val onePerPackage = apps.distinctBy { it.packageName }

        SettingsUiState(
            settings = settings,
            distractions = onePerPackage.filter { it.packageName in distractions },
            homeApps = resolveHomeApps(apps, settings.homePackages).map { it.toItem() },
            homeCandidates = onePerPackage
                .filter { it.packageName !in settings.homePackages }
                .map { it.toItem() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SettingsUiState(),
    )

    /** A change waiting for the unlock phrase, or null. */
    var pendingChange by mutableStateOf<SettingChange?>(null)
        private set

    var confirmationText by mutableStateOf("")
        private set

    var isPickingHomeApp by mutableStateOf(false)
        private set

    val isConfirmationValid: Boolean
        get() = UnlockPhrase.matches(confirmationText)

    fun request(change: SettingChange) {
        viewModelScope.launch {
            val current = settingsRepository.settings.first()
            if (change.reducesFriction(current)) {
                confirmationText = ""
                pendingChange = change
            } else {
                settingsRepository.apply(change)
            }
        }
    }

    fun openHomeAppPicker() {
        if (uiState.value.canAddHomeApp) isPickingHomeApp = true
    }

    fun closeHomeAppPicker() {
        isPickingHomeApp = false
    }

    fun addHomeApp(app: InstalledApp) {
        isPickingHomeApp = false
        request(SettingChange.AddHomeApp(app.packageName, app.label))
    }

    fun removeHomeApp(app: InstalledApp) {
        request(SettingChange.RemoveHomeApp(app.packageName))
    }

    fun onConfirmationChange(value: String) {
        confirmationText = value
    }

    fun confirm() {
        val change = pendingChange ?: return
        if (!isConfirmationValid) return
        dismiss()
        viewModelScope.launch { settingsRepository.apply(change) }
    }

    fun dismiss() {
        pendingChange = null
        confirmationText = ""
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
