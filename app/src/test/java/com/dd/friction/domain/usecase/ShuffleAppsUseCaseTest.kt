package com.dd.friction.domain.usecase

import com.dd.friction.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ShuffleAppsUseCaseTest {

    private val shuffle = ShuffleAppsUseCase()
    private val apps = (1..30).map { InstalledApp("pkg.$it", "Main", "App $it") }

    @Test
    fun `same seed keeps the same order`() {
        assertEquals(shuffle(apps, 42L), shuffle(apps, 42L))
    }

    @Test
    fun `different seeds give different orders`() {
        assertNotEquals(shuffle(apps, 1L), shuffle(apps, 2L))
    }

    @Test
    fun `shuffling keeps every app exactly once`() {
        val shuffled = shuffle(apps, 7L)
        assertEquals(apps.size, shuffled.size)
        assertEquals(apps.toSet(), shuffled.toSet())
    }
}
