package com.dd.friction.domain.model

/** A launchable activity installed on the device. */
data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
) {
    /** Unique per launchable activity; some packages expose more than one. */
    val key: String get() = "$packageName/$activityName"
}
