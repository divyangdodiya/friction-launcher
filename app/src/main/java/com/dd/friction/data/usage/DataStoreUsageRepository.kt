package com.dd.friction.data.usage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dd.friction.domain.model.DailyCount
import com.dd.friction.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreUsageRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UsageRepository {

    override val dailyOpens: Flow<DailyCount?> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toDailyCount() }
        .distinctUntilChanged()

    override suspend fun recordOpen(day: LocalDate) {
        dataStore.edit { prefs ->
            val next = prefs.toDailyCount()?.increment(day) ?: DailyCount(day, 1)
            prefs[Keys.DATE] = next.date.toString()
            prefs[Keys.COUNT] = next.count
        }
    }

    private fun Preferences.toDailyCount(): DailyCount? {
        val date = this[Keys.DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return null
        return DailyCount(date, this[Keys.COUNT] ?: 0)
    }

    private object Keys {
        val DATE = stringPreferencesKey("opens_date")
        val COUNT = intPreferencesKey("opens_count")
    }
}
