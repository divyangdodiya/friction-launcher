package com.dd.friction.domain.model

/** Every edit the user can make to [FrictionSettings]. */
sealed interface SettingChange {
    data class Delay(val seconds: Int) : SettingChange
    data class RequireIntention(val enabled: Boolean) : SettingChange
    data class ShowIcons(val enabled: Boolean) : SettingChange
    data class AddDistraction(val packageName: String) : SettingChange
    data class RemoveDistraction(val packageName: String, val label: String) : SettingChange

    /**
     * Adding friction is free; removing it requires typing [UnlockPhrase]. This keeps a weak
     * moment from quietly undoing the setup.
     */
    fun reducesFriction(current: FrictionSettings): Boolean = when (this) {
        is Delay -> seconds < current.delaySeconds
        is RequireIntention -> !enabled && current.requireIntention
        is ShowIcons -> enabled && !current.showIcons
        is AddDistraction -> false
        is RemoveDistraction -> packageName in current.distractionPackages
    }
}

fun FrictionSettings.withChange(change: SettingChange): FrictionSettings = when (change) {
    is SettingChange.Delay -> copy(delaySeconds = change.seconds)
    is SettingChange.RequireIntention -> copy(requireIntention = change.enabled)
    is SettingChange.ShowIcons -> copy(showIcons = change.enabled)
    is SettingChange.AddDistraction -> copy(distractionPackages = distractionPackages + change.packageName)
    is SettingChange.RemoveDistraction -> copy(distractionPackages = distractionPackages - change.packageName)
}
