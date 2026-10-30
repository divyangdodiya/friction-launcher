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
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        appRepository.installedApps,
        settingsRepository.settings,
    ) { apps, settings ->
        SettingsUiState(
            settings = settings,
            distractions = apps
                .filter { it.packageName in settings.distractionPackages }
                .distinctBy { it.packageName },
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
