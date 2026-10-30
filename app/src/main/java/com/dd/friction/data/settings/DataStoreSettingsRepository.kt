package com.dd.friction.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.dd.friction.data.apps.LaunchablePackages
import com.dd.friction.di.IoDispatcher
import com.dd.friction.domain.model.FrictionSettings
import com.dd.friction.domain.model.SettingChange
import com.dd.friction.domain.model.withChange
import com.dd.friction.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val launchablePackages: LaunchablePackages,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    override val settings: Flow<FrictionSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toSettings() }
        .flowOn(ioDispatcher)
        .distinctUntilChanged()

    override suspend fun apply(change: SettingChange) {
        dataStore.edit { prefs ->
            // The first home screen edit stores the device defaults, so they become the user's list.
            val updated = prefs.toSettings().withChange(change)
            prefs.write(updated)
        }
    }

    private fun Preferences.toSettings(): FrictionSettings {
        val defaults = FrictionSettings()
        val storedHome = this[Keys.HOME_PACKAGES]?.let(::decodeList)
        return FrictionSettings(
            distractionPackages = this[Keys.DISTRACTIONS] ?: defaults.distractionPackages,
            delaySeconds = this[Keys.DELAY_SECONDS] ?: defaults.delaySeconds,
            requireIntention = this[Keys.REQUIRE_INTENTION] ?: defaults.requireIntention,
            showIcons = this[Keys.SHOW_ICONS] ?: defaults.showIcons,
            // Uninstalled apps drop out so they never hold a slot the user cannot see.
            homePackages = (storedHome ?: launchablePackages.defaultHomePackages())
                .filter(launchablePackages::isLaunchable)
                .distinct()
                .take(FrictionSettings.MAX_HOME_APPS),
        )
    }

    private fun MutablePreferences.write(settings: FrictionSettings) {
        this[Keys.DISTRACTIONS] = settings.distractionPackages
        this[Keys.DELAY_SECONDS] = settings.delaySeconds
        this[Keys.REQUIRE_INTENTION] = settings.requireIntention
        this[Keys.SHOW_ICONS] = settings.showIcons
        this[Keys.HOME_PACKAGES] = settings.homePackages.joinToString(LIST_SEPARATOR)
    }

    // Package names never contain commas, so a joined string keeps the order a Set would lose.
    private fun decodeList(value: String): List<String> =
        value.split(LIST_SEPARATOR).filter { it.isNotBlank() }

    private object Keys {
        val DISTRACTIONS = stringSetPreferencesKey("distraction_packages")
        val DELAY_SECONDS = intPreferencesKey("delay_seconds")
        val REQUIRE_INTENTION = booleanPreferencesKey("require_intention")
        val SHOW_ICONS = booleanPreferencesKey("show_icons")
        val HOME_PACKAGES = stringPreferencesKey("home_packages")
    }

    private companion object {
        const val LIST_SEPARATOR = ","
    }
}
