package tachiyomi.domain.history.interactor

import eu.kanade.tachiyomi.source.model.SManga
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.PassportComputationResult
import tachiyomi.domain.history.model.PassportStamp
import tachiyomi.domain.history.model.StampState
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

class ComputePassportStatsTest {

    private val utc = ZoneOffset.UTC
    private val evalNowMs = ZonedDateTime.of(2026, 11, 10, 12, 0, 0, 0, utc).toInstant().toEpochMilli()

    @Test
    fun `empty history leaves all stamps locked with zero progress`() {
        val result = computePassportStats(
            historyEntries = emptyList(),
            libraryManga = emptyList(),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )

        result.activeDates shouldBe emptySet()
        result.stamps.all { !it.isUnlocked && it.progress == 0 } shouldBe true
    }

    @Test
    fun `streak with gap uses longest consecutive run`() {
        // Streak 1: Nov 1..Nov 4 (4 days); Gap on Nov 5; Streak 2: Nov 6..Nov 8 (3 days)
        val dates = listOf(1, 2, 3, 4, 6, 7, 8).map { day ->
            historyAt(year = 2026, month = 11, day = day, hour = 14)
        }

        val withGap = computePassportStats(
            historyEntries = dates,
            libraryManga = emptyList(),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )

        withGap.find(PassportStamp.STREAK_7).apply {
            isUnlocked shouldBe false
            progress shouldBe 4
        }

        // Fill the gap (Nov 5) -> 8 consecutive days unlocks STREAK_7
        val continuous = computePassportStats(
            historyEntries = dates + historyAt(year = 2026, month = 11, day = 5, hour = 14),
            libraryManga = emptyList(),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )

        continuous.find(PassportStamp.STREAK_7).apply {
            isUnlocked shouldBe true
            progress shouldBe 7
            unlockedAt shouldBe evalNowMs
        }
        continuous.find(PassportStamp.STREAK_30).apply {
            isUnlocked shouldBe false
            progress shouldBe 8
        }
    }

    @Test
    fun `midnight boundary separates calendar days and 04_59 vs 05_00 gates night owl`() {
        val beforeMidnight = historyAt(year = 2026, month = 11, day = 9, hour = 23, minute = 59, second = 59, id = 1L)
        val atMidnight = historyAt(year = 2026, month = 11, day = 10, hour = 0, minute = 0, second = 0, id = 2L)
        val at0459 = historyAt(year = 2026, month = 11, day = 10, hour = 4, minute = 59, second = 59, id = 3L)
        val at0500 = historyAt(year = 2026, month = 11, day = 10, hour = 5, minute = 0, second = 0, id = 4L)

        val result = computePassportStats(
            historyEntries = listOf(beforeMidnight, atMidnight, at0459, at0500),
            libraryManga = emptyList(),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )

        result.activeDates.shouldContainExactly(
            LocalDate.of(2026, 11, 9),
            LocalDate.of(2026, 11, 10),
        )
        // Only atMidnight (00:00:00) and at0459 (04:59:59) qualify for NIGHT_OWL
        result.find(PassportStamp.NIGHT_OWL).progress shouldBe 2

        val twentyNightChapters = (0 until 20).map { idx ->
            historyAt(year = 2026, month = 11, day = 10, hour = 3, minute = idx, id = 100L + idx)
        }
        val unlockedNight = computePassportStats(
            historyEntries = twentyNightChapters,
            libraryManga = emptyList(),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )
        unlockedNight.find(PassportStamp.NIGHT_OWL).isUnlocked shouldBe true
    }

    @Test
    fun `marathon unlocks at 30 chapters in a single day`() {
        val twentyNine = (1..29).map { idx ->
            historyAt(year = 2026, month = 11, day = 10, hour = 10, minute = idx % 60, id = idx.toLong())
        }
        val lockedResult = computePassportStats(twentyNine, emptyList(), zoneId = utc, nowEpochMs = evalNowMs)
        lockedResult.find(PassportStamp.MARATHON).apply {
            isUnlocked shouldBe false
            progress shouldBe 29
        }

        val thirty = twentyNine + historyAt(year = 2026, month = 11, day = 10, hour = 18, id = 30L)
        val unlockedResult = computePassportStats(thirty, emptyList(), zoneId = utc, nowEpochMs = evalNowMs)
        unlockedResult.find(PassportStamp.MARATHON).isUnlocked shouldBe true
    }

    @Test
    fun `finisher requires completed series with at least 5 chapters all read`() {
        val shortCompleted = mockLibraryManga(
            id = 1L,
            status = SManga.COMPLETED.toLong(),
            totalChapters = 4L,
            readCount = 4L,
        )
        val longOngoing = mockLibraryManga(
            id = 2L,
            status = SManga.ONGOING.toLong(),
            totalChapters = 10L,
            readCount = 10L,
        )
        val notFinished = computePassportStats(
            historyEntries = emptyList(),
            libraryManga = listOf(shortCompleted, longOngoing),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )
        notFinished.find(PassportStamp.FINISHER).apply {
            isUnlocked shouldBe false
            progress shouldBe 0
        }

        val eligibleCompleted = mockLibraryManga(
            id = 3L,
            status = SManga.COMPLETED.toLong(),
            totalChapters = 5L,
            readCount = 5L,
        )
        val finished = computePassportStats(
            historyEntries = emptyList(),
            libraryManga = listOf(shortCompleted, longOngoing, eligibleCompleted),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )
        finished.find(PassportStamp.FINISHER).apply {
            isUnlocked shouldBe true
            progress shouldBe 1
            unlockedAt shouldBe evalNowMs
        }
    }

    @Test
    fun `previously unlocked stamp and observed dates survive empty history`() {
        val persistedDate = LocalDate.of(2026, 11, 1)
        val result = computePassportStats(
            historyEntries = emptyList(),
            libraryManga = emptyList(),
            observedActiveDates = setOf(persistedDate),
            previouslyUnlockedAt = mapOf(PassportStamp.FIRST_PAGE.id to 1_234_567L),
            zoneId = utc,
            nowEpochMs = evalNowMs,
        )

        result.activeDates shouldBe setOf(persistedDate)
        result.find(PassportStamp.FIRST_PAGE).apply {
            isUnlocked shouldBe true
            unlockedAt shouldBe 1_234_567L
            progress shouldBe 1
        }
    }

    private fun PassportComputationResult.find(stamp: PassportStamp): StampState {
        return stamps.first { it.stamp == stamp }
    }

    private fun historyAt(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int = 0,
        second: Int = 0,
        id: Long = day.toLong(),
    ): History {
        val instant = ZonedDateTime.of(year, month, day, hour, minute, second, 0, utc).toInstant()
        return History(
            id = id,
            chapterId = id,
            readAt = Date(instant.toEpochMilli()),
            readDuration = 60_000L,
        )
    }

    private fun mockLibraryManga(
        id: Long,
        status: Long,
        totalChapters: Long,
        readCount: Long,
    ): LibraryManga {
        val manga = mockk<Manga> {
            every { this@mockk.id } returns id
            every { this@mockk.status } returns status
        }
        return LibraryManga(
            manga = manga,
            categories = emptyList(),
            totalChapters = totalChapters,
            readCount = readCount,
            bookmarkCount = 0L,
            bookmarkReadCount = 0L,
            chapterFlags = 0L,
            latestUpload = 0L,
            chapterFetchedAt = 0L,
            lastRead = 0L,
        )
    }
}
