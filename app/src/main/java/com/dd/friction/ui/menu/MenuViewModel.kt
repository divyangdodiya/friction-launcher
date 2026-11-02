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
import com.dd.friction.domain.model.SearchMode
import com.dd.friction.domain.model.SettingChange
import com.dd.friction.domain.repository.AppLauncher
import com.dd.friction.domain.repository.AppRepository
import com.dd.friction.domain.repository.SettingsRepository
import com.dd.friction.domain.usecase.SearchAppsUseCase
import com.dd.friction.domain.usecase.ShuffleAppsUseCase
import com.dd.friction.ui.model.AppItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A search hit. [showPackage] is set when another hit has the same name, so the two can be told apart. */
data class SearchResult(val item: AppItem, val showPackage: Boolean)

data class MenuUiState(
    val apps: List<AppItem> = emptyList(),
    val isSearching: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val searchMode: SearchMode = SearchMode.DEFAULT,
    val showIcons: Boolean = false,
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val shuffleApps: ShuffleAppsUseCase,
    private val searchApps: SearchAppsUseCase,
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
        fun InstalledApp.toItem() = AppItem(this, packageName in distractions)

        val hits = searchApps(apps, currentQuery, settings.searchMode, distractions)
        val labelCounts = hits.groupingBy { it.label.lowercase() }.eachCount()

        MenuUiState(
            apps = shuffleApps(apps, shuffleSeed).map { it.toItem() },
            isSearching = currentQuery.isNotBlank(),
            results = hits.map { app ->
                SearchResult(app.toItem(), showPackage = (labelCounts[app.label.lowercase()] ?: 0) > 1)
            },
            searchMode = settings.searchMode,
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
