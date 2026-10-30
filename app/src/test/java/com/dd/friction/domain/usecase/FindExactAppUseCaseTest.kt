package com.dd.friction.domain.usecase

import com.dd.friction.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindExactAppUseCaseTest {

    private val find = FindExactAppUseCase()
    private val netflix = InstalledApp("com.netflix.mediaclient", "Main", "Netflix")
    private val googleMaps = InstalledApp("com.google.android.apps.maps", "Main", "Google Maps")
    private val apps = listOf(netflix, googleMaps)

    @Test
    fun `blank query matches nothing`() {
        assertNull(find(apps, "   "))
    }

    @Test
    fun `partial name matches nothing`() {
        assertNull(find(apps, "Netf"))
        assertNull(find(apps, "Maps"))
    }

    @Test
    fun `full name matches ignoring case and extra spaces`() {
        assertEquals(netflix, find(apps, "  netflix "))
        assertEquals(googleMaps, find(apps, "google   MAPS"))
    }
}
