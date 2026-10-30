package com.dd.friction.domain.model

data class FrictionSettings(
    val distractionPackages: Set<String> = DEFAULT_DISTRACTIONS,
    val delaySeconds: Int = DEFAULT_DELAY_SECONDS,
    val requireIntention: Boolean = true,
    val showIcons: Boolean = false,
    /**
     * Packages shown on the home screen, in display order. Until the user edits the list,
     * the data layer fills it with the device's dialer, SMS app and WhatsApp.
     */
    val homePackages: List<String> = emptyList(),
) {
    companion object {
        const val DEFAULT_DELAY_SECONDS = 15
        val DELAY_OPTIONS = listOf(5, 10, 15, 30, 60)

        /** Keeps the clock, reminders and app list on one screen without crowding. */
        const val MAX_HOME_APPS = 5

        /** Common time sinks, pre-marked on first run. Apps that are not installed are ignored. */
        val DEFAULT_DISTRACTIONS = setOf(
            // Browsers
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.brave.browser",
            "com.opera.browser",
            "com.microsoft.emmx",
            "com.sec.android.app.sbrowser",
            // Video and streaming
            "com.google.android.youtube",
            "com.netflix.mediaclient",
            "com.amazon.avod.thirdpartyclient",
            "in.startv.hotstar",
            "com.jio.media.ondemand",
            // Social
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.snapchat.android",
            "com.pinterest",
            "com.linkedin.android",
            "com.zhiliaoapp.musically",
            "in.mohalla.sharechat",
        )
    }
}
