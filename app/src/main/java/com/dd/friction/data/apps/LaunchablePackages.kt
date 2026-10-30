package com.dd.friction.data.apps

import android.content.Context
import android.provider.Telephony
import android.telecom.TelecomManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Device-specific package lookups used to seed and clean the home screen list. */
@Singleton
class LaunchablePackages @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val packageManager = context.packageManager

    fun isLaunchable(packageName: String): Boolean =
        packageManager.getLaunchIntentForPackage(packageName) != null

    /** The user's dialer, SMS app and WhatsApp: the starting home screen before any edits. */
    fun defaultHomePackages(): List<String> = listOfNotNull(
        context.getSystemService<TelecomManager>()?.defaultDialerPackage,
        Telephony.Sms.getDefaultSmsPackage(context),
        WHATSAPP_PACKAGES.firstOrNull(::isLaunchable),
    )

    private companion object {
        val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")
    }
}
