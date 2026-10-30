package com.dd.friction.data.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import androidx.core.content.getSystemService
import com.dd.friction.di.ApplicationScope
import com.dd.friction.di.IoDispatcher
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads launchable apps through [LauncherApps], the platform API meant for home screens,
 * and reloads whenever a package is added, removed or updated.
 */
@Singleton
class LauncherAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    @ApplicationScope appScope: CoroutineScope,
) : AppRepository {

    private val launcherApps: LauncherApps = checkNotNull(context.getSystemService<LauncherApps>())

    // Shared eagerly: the launcher process lives as long as the phone is on, so the
    // menu opens instantly from a warm list instead of querying on every visit.
    override val installedApps: Flow<List<InstalledApp>> = packageChanges()
        .onStart { emit(Unit) }
        .conflate()
        .map { loadApps() }
        .flowOn(ioDispatcher)
        .shareIn(appScope, SharingStarted.Eagerly, replay = 1)

    private fun loadApps(): List<InstalledApp> =
        launcherApps.getActivityList(null, Process.myUserHandle())
            .asSequence()
            .filterNot { it.componentName.packageName == context.packageName }
            .map { info ->
                InstalledApp(
                    packageName = info.componentName.packageName,
                    activityName = info.componentName.className,
                    label = info.label.toString(),
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()

    private fun packageChanges(): Flow<Unit> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                trySend(Unit)
            }
        }
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(callback) }
    }
}
