package tachiyomi.domain.history.interactor

import eu.kanade.tachiyomi.source.model.SManga
import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.PassportComputationResult
import tachiyomi.domain.history.model.PassportStamp
import tachiyomi.domain.history.model.StampState
import tachiyomi.domain.library.model.LibraryManga
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val FINISHER_MIN_CHAPTERS = 5L

/**
 * Pure domain evaluator for [PassportStamp] milestones.
 *
 * Note: SQLite's `history` table stores a unique `last_read` timestamp per `chapter_id`, so
 * rereading an older chapter moves that chapter's timestamp to the new reading date. Passing
 * [observedActiveDates] from persistent preferences preserves prior streak days while allowing
 * bulk "mark as read" actions (which do not insert history rows) to stay excluded from chapter
 * history milestones.
 */
fun computePassportStats(
    historyEntries: List<History>,
    libraryManga: List<LibraryManga>,
    observedActiveDates: Set<LocalDate> = emptySet(),
    previouslyUnlockedAt: Map<String, Long> = emptyMap(),
    seenStampIds: Set<String> = emptySet(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    nowEpochMs: Long = System.currentTimeMillis(),
): PassportComputationResult {
    val readZonedTimes = historyEntries.mapNotNull { entry ->
        val readAt = entry.readAt?.takeIf { it.time > 0L } ?: return@mapNotNull null
        Instant.ofEpochMilli(readAt.time).atZone(zoneId)
    }

    val chaptersRead = readZonedTimes.size
    val nightOwlChapters = readZonedTimes.count { it.hour in 0..4 }
    val maxChaptersInDay = readZonedTimes
        .groupingBy { it.toLocalDate() }
        .eachCount()
        .values
        .maxOrNull()
        ?: 0

    val allActiveDates = (observedActiveDates + readZonedTimes.map { it.toLocalDate() }).toSortedSet()
    val longestStreak = computeLongestStreak(allActiveDates)

    val finishedSeriesCount = libraryManga.count { manga ->
        manga.manga.status.toInt() == SManga.COMPLETED &&
            manga.totalChapters >= FINISHER_MIN_CHAPTERS &&
            manga.readCount >= manga.totalChapters
    }

    val stamps = PassportStamp.entries.map { stamp ->
        val rawProgress = when (stamp) {
            PassportStamp.FIRST_PAGE,
            PassportStamp.BOOKWORM_100,
            PassportStamp.BOOKWORM_1000,
            -> chaptersRead

            PassportStamp.STREAK_7,
            PassportStamp.STREAK_30,
            -> longestStreak

            PassportStamp.NIGHT_OWL -> nightOwlChapters
            PassportStamp.MARATHON -> maxChaptersInDay
            PassportStamp.FINISHER -> finishedSeriesCount
        }

        val existingUnlock = previouslyUnlockedAt[stamp.id]
        val unlockedAt = existingUnlock ?: if (rawProgress >= stamp.goal) nowEpochMs else null
        val displayProgress = if (unlockedAt != null) {
            stamp.goal
        } else {
            rawProgress.coerceIn(0, stamp.goal)
        }

        StampState(
            stamp = stamp,
            progress = displayProgress,
            goal = stamp.goal,
            unlockedAt = unlockedAt,
            isSeen = stamp.id in seenStampIds,
        )
    }

    return PassportComputationResult(
        stamps = stamps,
        activeDates = allActiveDates,
    )
}

private fun computeLongestStreak(sortedUniqueDates: Set<LocalDate>): Int {
    var best = 0
    var current = 0
    var previous: LocalDate? = null
    for (date in sortedUniqueDates) {
        current = if (previous != null && previous.plusDays(1) == date) {
            current + 1
        } else {
            1
        }
        if (current > best) {
            best = current
        }
        previous = date
    }
    return best
}
