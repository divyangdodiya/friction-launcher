package com.dd.friction.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.dd.friction.domain.model.FrictionSettings
import com.dd.friction.domain.model.SettingChange
import com.dd.friction.domain.model.withChange
import com.dd.friction.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<FrictionSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toSettings() }
        .distinctUntilChanged()

    override suspend fun apply(change: SettingChange) {
        dataStore.edit { prefs ->
            val updated = prefs.toSettings().withChange(change)
            prefs.write(updated)
        }
    }

    private fun Preferences.toSettings(): FrictionSettings {
        val defaults = FrictionSettings()
        return FrictionSettings(
            distractionPackages = this[Keys.DISTRACTIONS] ?: defaults.distractionPackages,
            delaySeconds = this[Keys.DELAY_SECONDS] ?: defaults.delaySeconds,
            requireIntention = this[Keys.REQUIRE_INTENTION] ?: defaults.requireIntention,
            showIcons = this[Keys.SHOW_ICONS] ?: defaults.showIcons,
        )
    }

    private fun MutablePreferences.write(settings: FrictionSettings) {
        this[Keys.DISTRACTIONS] = settings.distractionPackages
        this[Keys.DELAY_SECONDS] = settings.delaySeconds
        this[Keys.REQUIRE_INTENTION] = settings.requireIntention
        this[Keys.SHOW_ICONS] = settings.showIcons
    }

    private object Keys {
        val DISTRACTIONS = stringSetPreferencesKey("distraction_packages")
        val DELAY_SECONDS = intPreferencesKey("delay_seconds")
        val REQUIRE_INTENTION = booleanPreferencesKey("require_intention")
        val SHOW_ICONS = booleanPreferencesKey("show_icons")
    }
}
