package com.dd.friction.data.system

import android.annotation.SuppressLint
import android.content.Context
import com.dd.friction.domain.repository.SystemUi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android has no public API for opening the notification shade, so this uses the same
 * StatusBarManager call other launchers rely on. It needs the normal EXPAND_STATUS_BAR
 * permission, which only allows expanding the shade, nothing else.
 */
@Singleton
class AndroidSystemUi @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemUi {

    @SuppressLint("WrongConstant", "PrivateApi")
    override fun expandNotifications(): Boolean = runCatching {
        val statusBarService = context.getSystemService(STATUS_BAR_SERVICE)
        Class.forName(STATUS_BAR_MANAGER_CLASS)
            .getMethod(EXPAND_METHOD)
            .invoke(statusBarService)
    }.isSuccess

    private companion object {
        const val STATUS_BAR_SERVICE = "statusbar"
        const val STATUS_BAR_MANAGER_CLASS = "android.app.StatusBarManager"
        const val EXPAND_METHOD = "expandNotificationsPanel"
    }
}
