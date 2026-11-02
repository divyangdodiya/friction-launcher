package com.dd.friction.domain.usecase

import com.dd.friction.domain.model.FrictionSettings
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.model.SearchMode
import com.dd.friction.domain.model.SettingChange
import com.dd.friction.domain.model.withChange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchAppsUseCaseTest {

    private val search = SearchAppsUseCase()

    private val googleAuth = InstalledApp("com.google.android.apps.authenticator2", "Main", "Authenticator")
    private val microsoftAuth = InstalledApp("com.azure.authenticator", "Main", "Authenticator")
    private val instagram = InstalledApp("com.instagram.android", "Main", "Instagram")
    private val install = InstalledApp("com.example.installer", "Main", "Install Helper")
    private val calendar = InstalledApp("com.example.calendar", "Main", "Calendar")
    private val apps = listOf(googleAuth, microsoftAuth, instagram, install, calendar)
    private val distractions = setOf(instagram.packageName)

    private fun run(query: String, mode: SearchMode) = search(apps, query, mode, distractions)

    @Test
    fun `apps sharing a name are all found`() {
        for (mode in SearchMode.entries) {
            assertEquals(setOf(googleAuth, microsoftAuth), run("authenticator", mode).toSet())
        }
    }

    @Test
    fun `blank query finds nothing`() {
        assertTrue(run("  ", SearchMode.Free).isEmpty())
    }

    @Test
    fun `exact mode ignores partial names`() {
        assertTrue(run("auth", SearchMode.Exact).isEmpty())
        assertEquals(listOf(calendar), run("  CALENDAR ", SearchMode.Exact))
    }

    @Test
    fun `smart mode finds normal apps by partial name but hides distractions`() {
        assertEquals(setOf(googleAuth, microsoftAuth), run("auth", SearchMode.Smart).toSet())
        assertEquals(listOf(install), run("ins", SearchMode.Smart))
    }

    @Test
    fun `smart mode still reveals a distraction by its full name`() {
        assertEquals(listOf(instagram), run("instagram", SearchMode.Smart))
    }

    @Test
    fun `free mode finds distractions by partial name`() {
        assertEquals(setOf(install, instagram), run("ins", SearchMode.Free).toSet())
    }

    @Test
    fun `single letter only matches a full name`() {
        assertTrue(run("c", SearchMode.Free).isEmpty())
    }

    @Test
    fun `exact matches rank before partial ones and results are capped`() {
        val many = (1..10).map { InstalledApp("pkg.$it", "Main", "Notes $it") } +
            InstalledApp("pkg.notes", "Main", "Notes")
        val results = search(many, "notes", SearchMode.Free, emptySet())
        assertEquals("Notes", results.first().label)
        assertEquals(SearchAppsUseCase.MAX_RESULTS, results.size)
    }

    @Test
    fun `loosening search needs confirmation, tightening is free`() {
        val smart = FrictionSettings(searchMode = SearchMode.Smart)
        assertTrue(SettingChange.Search(SearchMode.Free).reducesFriction(smart))
        assertFalse(SettingChange.Search(SearchMode.Exact).reducesFriction(smart))
        assertEquals(SearchMode.Exact, smart.withChange(SettingChange.Search(SearchMode.Exact)).searchMode)
    }
}
