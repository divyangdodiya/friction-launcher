package com.dd.friction.data.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.provider.Telephony
import android.telecom.TelecomManager
import androidx.core.content.getSystemService
import com.dd.friction.domain.model.EssentialApp
import com.dd.friction.domain.repository.AppLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAppLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppLauncher {

    private val packageManager = context.packageManager
    private val launcherApps: LauncherApps = checkNotNull(context.getSystemService<LauncherApps>())

    override fun launch(packageName: String, activityName: String): Boolean = runCatching {
        launcherApps.startMainActivity(
            ComponentName(packageName, activityName),
            Process.myUserHandle(),
            null,
            null,
        )
    }.isSuccess

    override fun launch(essential: EssentialApp): Boolean {
        val intent = intentFor(essential) ?: return false
        return runCatching {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.isSuccess
    }

    override fun isAvailable(essential: EssentialApp): Boolean = intentFor(essential) != null

    private fun intentFor(essential: EssentialApp): Intent? = when (essential) {
        EssentialApp.Phone -> dialerIntent()
        EssentialApp.Messages -> messagesIntent()
        EssentialApp.WhatsApp -> WHATSAPP_PACKAGES.firstNotNullOfOrNull { launchIntent(it) }
    }

    /** The user's chosen dialer app, falling back to the system keypad. */
    private fun dialerIntent(): Intent? {
        val defaultDialer = context.getSystemService<TelecomManager>()?.defaultDialerPackage
        return defaultDialer?.let { launchIntent(it) }
            ?: Intent(Intent.ACTION_DIAL).takeIf { it.resolveActivity(packageManager) != null }
    }

    /** The user's default SMS app, falling back to any messaging app. */
    private fun messagesIntent(): Intent? {
        val defaultSms: String? = Telephony.Sms.getDefaultSmsPackage(context)
        return defaultSms?.let { launchIntent(it) }
            ?: Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
                .takeIf { it.resolveActivity(packageManager) != null }
    }

    private fun launchIntent(packageName: String): Intent? =
        packageManager.getLaunchIntentForPackage(packageName)

    private companion object {
        val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")
    }
}
