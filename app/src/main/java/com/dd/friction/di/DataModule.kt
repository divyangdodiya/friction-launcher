package com.dd.friction.di

import com.dd.friction.data.apps.LauncherAppsRepository
import com.dd.friction.data.launcher.AndroidAppLauncher
import com.dd.friction.data.settings.DataStoreSettingsRepository
import com.dd.friction.data.time.SystemTimeSource
import com.dd.friction.data.usage.DataStoreUsageRepository
import com.dd.friction.domain.repository.AppLauncher
import com.dd.friction.domain.repository.AppRepository
import com.dd.friction.domain.repository.SettingsRepository
import com.dd.friction.domain.repository.UsageRepository
import com.dd.friction.domain.time.TimeSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindAppRepository(impl: LauncherAppsRepository): AppRepository

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindUsageRepository(impl: DataStoreUsageRepository): UsageRepository

    @Binds
    abstract fun bindAppLauncher(impl: AndroidAppLauncher): AppLauncher

    @Binds
    abstract fun bindTimeSource(impl: SystemTimeSource): TimeSource
}
