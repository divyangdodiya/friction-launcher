package com.dd.friction.domain.usecase

import com.dd.friction.domain.model.InstalledApp
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

/** Matches only the complete app name. Partial input never narrows the list. */
class FindExactAppUseCase @Inject constructor() {
    operator fun invoke(apps: List<InstalledApp>, query: String): InstalledApp? {
        val needle = query.normalizeForMatch()
        if (needle.isEmpty()) return null
        return apps.firstOrNull { it.label.normalizeForMatch() == needle }
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
