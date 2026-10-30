package com.dd.friction.domain.repository

import com.dd.friction.domain.model.DailyCount
import com.dd.friction.domain.model.FrictionSettings
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.model.SettingChange
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {
    /** Launchable apps sorted by label; emits again whenever apps are installed or removed. */
    val installedApps: Flow<List<InstalledApp>>
}

interface SettingsRepository {
    val settings: Flow<FrictionSettings>
    suspend fun apply(change: SettingChange)
}

interface UsageRepository {
    /** The last recorded day and its count, or null if nothing was ever recorded. */
    val dailyOpens: Flow<DailyCount?>
    suspend fun recordOpen(day: LocalDate)
}

interface SystemUi {
    /** Pulls down the notification shade. Returns false if the device does not allow it. */
    fun expandNotifications(): Boolean
}

interface AppLauncher {
    fun launch(packageName: String, activityName: String): Boolean
}
