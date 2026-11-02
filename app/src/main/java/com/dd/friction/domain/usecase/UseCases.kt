package com.dd.friction.domain.usecase

import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.model.SearchMode
import com.dd.friction.domain.model.normalizeForMatch
import com.dd.friction.domain.repository.UsageRepository
import com.dd.friction.domain.time.TimeSource
import javax.inject.Inject
import kotlin.random.Random

/** Deterministic for a given seed, so one menu visit keeps a stable order while typing. */
class ShuffleAppsUseCase @Inject constructor() {
    operator fun invoke(apps: List<InstalledApp>, seed: Long): List<InstalledApp> =
        apps.shuffled(Random(seed))
}

/**
 * Finds every app the query should reveal under [mode]. Apps sharing a name all match, so
 * look-alikes such as two "Authenticator" apps are each reachable. Exact name matches come first.
 */
class SearchAppsUseCase @Inject constructor() {
    operator fun invoke(
        apps: List<InstalledApp>,
        query: String,
        mode: SearchMode,
        distractionPackages: Set<String>,
    ): List<InstalledApp> {
        val needle = query.normalizeForMatch()
        if (needle.isEmpty()) return emptyList()
        val allowPartial = needle.length >= MIN_PARTIAL_LENGTH

        return apps
            .mapNotNull { app ->
                val label = app.label.normalizeForMatch()
                val partialAllowedForApp = when (mode) {
                    SearchMode.Exact -> false
                    SearchMode.Smart -> app.packageName !in distractionPackages
                    SearchMode.Free -> true
                }
                val rank = when {
                    label == needle -> RANK_EXACT
                    !allowPartial || !partialAllowedForApp -> null
                    label.startsWith(needle) -> RANK_PREFIX
                    label.contains(needle) -> RANK_CONTAINS
                    else -> null
                }
                rank?.let { it to app }
            }
            .sortedWith(compareBy({ it.first }, { it.second.label.lowercase() }))
            .map { it.second }
            .take(MAX_RESULTS)
    }

    companion object {
        /** A single letter would match half the phone; partial search starts at two. */
        const val MIN_PARTIAL_LENGTH = 2
        const val MAX_RESULTS = 6
        private const val RANK_EXACT = 0
        private const val RANK_PREFIX = 1
        private const val RANK_CONTAINS = 2
    }
}

/** The home screen apps in the user's order, skipping any that are no longer installed. */
class ResolveHomeAppsUseCase @Inject constructor() {
    operator fun invoke(apps: List<InstalledApp>, homePackages: List<String>): List<InstalledApp> {
        val byPackage = apps.groupBy { it.packageName }
        return homePackages.mapNotNull { byPackage[it]?.firstOrNull() }
    }
}

class RecordDistractionOpenUseCase @Inject constructor(
    private val usageRepository: UsageRepository,
    private val timeSource: TimeSource,
) {
    suspend operator fun invoke() {
        usageRepository.recordOpen(timeSource.now().toLocalDate())
    }
}
