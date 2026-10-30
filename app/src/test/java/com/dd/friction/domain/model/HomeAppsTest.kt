package com.dd.friction.domain.model

import com.dd.friction.domain.usecase.ResolveHomeAppsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAppsTest {

    private val settings = FrictionSettings(
        distractionPackages = setOf("com.instagram.android"),
        homePackages = listOf("com.dialer", "com.sms"),
    )

    @Test
    fun `adding a normal app is free and appends in order`() {
        val change = SettingChange.AddHomeApp("com.notes", "Notes")
        assertFalse(change.reducesFriction(settings))
        assertEquals(listOf("com.dialer", "com.sms", "com.notes"), settings.withChange(change).homePackages)
    }

    @Test
    fun `adding a distraction to home needs confirmation`() {
        assertTrue(SettingChange.AddHomeApp("com.instagram.android", "Instagram").reducesFriction(settings))
    }

    @Test
    fun `removing a home app is free`() {
        val change = SettingChange.RemoveHomeApp("com.sms")
        assertFalse(change.reducesFriction(settings))
        assertEquals(listOf("com.dialer"), settings.withChange(change).homePackages)
    }

    @Test
    fun `home screen never grows past the limit`() {
        val full = settings.copy(homePackages = (1..FrictionSettings.MAX_HOME_APPS).map { "pkg.$it" })
        assertEquals(full, full.withChange(SettingChange.AddHomeApp("com.extra", "Extra")))
    }

    @Test
    fun `adding an app already on home changes nothing`() {
        assertEquals(settings, settings.withChange(SettingChange.AddHomeApp("com.sms", "Messages")))
    }

    @Test
    fun `resolve keeps user order and skips uninstalled apps`() {
        val dialer = InstalledApp("com.dialer", "Main", "Phone")
        val notes = InstalledApp("com.notes", "Main", "Notes")
        val resolved = ResolveHomeAppsUseCase()(
            apps = listOf(notes, dialer),
            homePackages = listOf("com.dialer", "com.gone", "com.notes"),
        )
        assertEquals(listOf(dialer, notes), resolved)
    }
}
