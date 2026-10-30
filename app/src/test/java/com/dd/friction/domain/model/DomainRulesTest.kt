package com.dd.friction.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyCountTest {

    private val monday = LocalDate.of(2026, 10, 26)
    private val tuesday = monday.plusDays(1)

    @Test
    fun `count from an earlier day reads as zero`() {
        assertEquals(0, DailyCount(monday, 5).countOn(tuesday))
    }

    @Test
    fun `increment on the same day adds one`() {
        assertEquals(DailyCount(monday, 3), DailyCount(monday, 2).increment(monday))
    }

    @Test
    fun `increment on a new day starts over`() {
        assertEquals(DailyCount(tuesday, 1), DailyCount(monday, 9).increment(tuesday))
    }
}

class IntentionRuleTest {

    @Test
    fun `short or empty reasons are rejected`() {
        assertFalse(IntentionRule.isSatisfied(""))
        assertFalse(IntentionRule.isSatisfied("bored"))
        assertFalse(IntentionRule.isSatisfied("a b c d"))
    }

    @Test
    fun `a real sentence is accepted`() {
        assertTrue(IntentionRule.isSatisfied("check train timings"))
    }
}

class UnlockPhraseTest {

    @Test
    fun `phrase must match fully`() {
        assertFalse(UnlockPhrase.matches("I choose distraction"))
        assertTrue(UnlockPhrase.matches("  i choose   DISTRACTION over focus "))
    }
}

class SettingChangeTest {

    private val settings = FrictionSettings(
        distractionPackages = setOf("com.netflix.mediaclient"),
        delaySeconds = 15,
        requireIntention = true,
        showIcons = false,
    )

    @Test
    fun `lowering friction needs confirmation`() {
        assertTrue(SettingChange.Delay(5).reducesFriction(settings))
        assertTrue(SettingChange.RequireIntention(false).reducesFriction(settings))
        assertTrue(SettingChange.ShowIcons(true).reducesFriction(settings))
        assertTrue(SettingChange.RemoveDistraction("com.netflix.mediaclient", "Netflix").reducesFriction(settings))
    }

    @Test
    fun `raising friction is free`() {
        assertFalse(SettingChange.Delay(30).reducesFriction(settings))
        assertFalse(SettingChange.RequireIntention(true).reducesFriction(settings))
        assertFalse(SettingChange.ShowIcons(false).reducesFriction(settings))
        assertFalse(SettingChange.AddDistraction("com.instagram.android").reducesFriction(settings))
    }

    @Test
    fun `changes apply to settings`() {
        val updated = settings
            .withChange(SettingChange.AddDistraction("com.instagram.android"))
            .withChange(SettingChange.RemoveDistraction("com.netflix.mediaclient", "Netflix"))
            .withChange(SettingChange.Delay(30))
        assertEquals(setOf("com.instagram.android"), updated.distractionPackages)
        assertEquals(30, updated.delaySeconds)
    }
}
