package com.dd.friction.ui.gate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dd.friction.di.ApplicationScope
import com.dd.friction.domain.model.IntentionRule
import com.dd.friction.domain.repository.AppLauncher
import com.dd.friction.domain.repository.AppRepository
import com.dd.friction.domain.repository.SettingsRepository
import com.dd.friction.domain.usecase.RecordDistractionOpenUseCase
import com.dd.friction.ui.navigation.GateRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GateUiState(
    val appLabel: String = "",
    val secondsRemaining: Int = 0,
    val requireIntention: Boolean = true,
    val intentionSatisfied: Boolean = false,
    val isCounting: Boolean = false,
) {
    val canOpen: Boolean
        get() = isCounting && secondsRemaining == 0 && (!requireIntention || intentionSatisfied)
}

@HiltViewModel
class GateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val appLauncher: AppLauncher,
    private val recordDistractionOpen: RecordDistractionOpenUseCase,
    @ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<GateRoute>()

    var intention by mutableStateOf("")
        private set

    /** Null while the screen is not visible: leaving the gate restarts the wait. */
    private val secondsRemaining = MutableStateFlow<Int?>(null)
    private var countdownJob: Job? = null

    val uiState: StateFlow<GateUiState> = combine(
        appRepository.installedApps,
        settingsRepository.settings,
        secondsRemaining,
        snapshotFlow { intention },
    ) { apps, settings, remaining, currentIntention ->
        val app = apps.firstOrNull {
            it.packageName == route.packageName && it.activityName == route.activityName
        }
        GateUiState(
            appLabel = app?.label ?: route.packageName,
            secondsRemaining = remaining ?: settings.delaySeconds,
            requireIntention = settings.requireIntention,
            intentionSatisfied = IntentionRule.isSatisfied(currentIntention),
            isCounting = remaining != null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = GateUiState(),
    )

    fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val total = settingsRepository.settings.first().delaySeconds
            for (remaining in total downTo 0) {
                secondsRemaining.value = remaining
                if (remaining > 0) delay(MILLIS_PER_SECOND)
            }
        }
    }

    fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        secondsRemaining.value = null
    }

    fun onIntentionChange(value: String) {
        intention = value
    }

    /** Returns true when the app was opened and the gate should close. */
    fun open(): Boolean {
        if (!uiState.value.canOpen) return false
        val launched = appLauncher.launch(route.packageName, route.activityName)
        // Recorded on the app scope so leaving the screen cannot cancel the write.
        if (launched) appScope.launch { recordDistractionOpen() }
        return launched
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MILLIS_PER_SECOND = 1_000L
    }
}
