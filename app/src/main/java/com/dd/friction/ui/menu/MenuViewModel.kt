package com.dd.friction.ui.menu

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd.friction.data.apps.AppIconLoader
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.model.SettingChange
import com.dd.friction.domain.repository.AppLauncher
import com.dd.friction.domain.repository.AppRepository
import com.dd.friction.domain.repository.SettingsRepository
import com.dd.friction.domain.usecase.FindExactAppUseCase
import com.dd.friction.domain.usecase.ShuffleAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class MenuItem(val app: InstalledApp, val isDistraction: Boolean)

data class MenuUiState(
    val apps: List<MenuItem> = emptyList(),
    val exactMatch: MenuItem? = null,
    val showIcons: Boolean = false,
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val shuffleApps: ShuffleAppsUseCase,
    private val findExactApp: FindExactAppUseCase,
    private val appLauncher: AppLauncher,
    private val iconLoader: AppIconLoader,
) : ViewModel() {

    /** A new seed for every visit: the grid never settles into muscle memory. */
    private val shuffleSeed = Random.nextLong()

    var query by mutableStateOf("")
        private set

    val uiState: StateFlow<MenuUiState> = combine(
        appRepository.installedApps,
        settingsRepository.settings,
        snapshotFlow { query },
    ) { apps, settings, currentQuery ->
        val distractions = settings.distractionPackages
        fun InstalledApp.toItem() = MenuItem(this, packageName in distractions)

        MenuUiState(
            apps = shuffleApps(apps, shuffleSeed).map { it.toItem() },
            exactMatch = findExactApp(apps, currentQuery)?.toItem(),
            showIcons = settings.showIcons,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MenuUiState(),
    )

    fun onQueryChange(value: String) {
        query = value
    }

    fun launch(app: InstalledApp): Boolean = appLauncher.launch(app.packageName, app.activityName)

    fun markAsDistraction(app: InstalledApp) {
        viewModelScope.launch {
            settingsRepository.apply(SettingChange.AddDistraction(app.packageName))
        }
    }

    suspend fun loadIcon(app: InstalledApp, sizePx: Int): Bitmap? = iconLoader.load(app, sizePx)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
