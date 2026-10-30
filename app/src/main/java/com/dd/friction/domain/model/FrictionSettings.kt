package com.dd.friction.domain.model

data class FrictionSettings(
    val distractionPackages: Set<String> = DEFAULT_DISTRACTIONS,
    val delaySeconds: Int = DEFAULT_DELAY_SECONDS,
    val requireIntention: Boolean = true,
    val showIcons: Boolean = false,
) {
    companion object {
        const val DEFAULT_DELAY_SECONDS = 15
        val DELAY_OPTIONS = listOf(5, 10, 15, 30, 60)

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
