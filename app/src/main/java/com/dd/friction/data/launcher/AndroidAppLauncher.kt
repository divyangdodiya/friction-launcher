package com.dd.friction.data.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import androidx.core.content.getSystemService
import com.dd.friction.domain.repository.AppLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAppLauncher @Inject constructor(
    @ApplicationContext context: Context,
) : AppLauncher {

    private val launcherApps: LauncherApps = checkNotNull(context.getSystemService<LauncherApps>())

    override fun launch(packageName: String, activityName: String): Boolean = runCatching {
        launcherApps.startMainActivity(
            ComponentName(packageName, activityName),
            Process.myUserHandle(),
            null,
            null,
        )
    }.isSuccess
}
