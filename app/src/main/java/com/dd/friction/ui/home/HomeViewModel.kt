package com.dd.friction.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd.friction.domain.model.EssentialApp
import com.dd.friction.domain.repository.AppLauncher
import com.dd.friction.domain.repository.SystemUi
import com.dd.friction.domain.repository.UsageRepository
import com.dd.friction.domain.time.TimeSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.ZonedDateTime
import javax.inject.Inject

data class HomeUiState(
    val now: ZonedDateTime,
    val opensToday: Int = 0,
    val essentials: List<EssentialApp> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    usageRepository: UsageRepository,
    private val appLauncher: AppLauncher,
    private val systemUi: SystemUi,
    private val timeSource: TimeSource,
) : ViewModel() {

    private val essentials = MutableStateFlow<List<EssentialApp>>(emptyList())

    /** Emits once a minute, aligned to the minute boundary. */
    private val minuteTicker: Flow<ZonedDateTime> = flow {
        while (true) {
            val now = timeSource.now()
            emit(now)
            val elapsedInMinute = now.second * MILLIS_PER_SECOND + now.nano / NANOS_PER_MILLI
            delay((MILLIS_PER_MINUTE - elapsedInMinute).coerceAtLeast(1L))
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        minuteTicker,
        usageRepository.dailyOpens,
        essentials,
    ) { now, dailyOpens, available ->
        HomeUiState(
            now = now,
            opensToday = dailyOpens?.countOn(now.toLocalDate()) ?: 0,
            essentials = available,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(now = timeSource.now()),
    )

    init {
        refreshEssentials()
    }

    /** Default dialer or SMS app may change while the launcher is in the background. */
    fun refreshEssentials() {
        essentials.value = EssentialApp.entries.filter { appLauncher.isAvailable(it) }
    }

    fun onEssentialClick(app: EssentialApp) {
        appLauncher.launch(app)
    }

    fun onSwipeDown() {
        systemUi.expandNotifications()
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MILLIS_PER_SECOND = 1_000L
        const val MILLIS_PER_MINUTE = 60_000L
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
