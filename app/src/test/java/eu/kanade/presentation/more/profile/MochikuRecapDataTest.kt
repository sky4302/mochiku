package eu.kanade.presentation.more.profile

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.history.model.MonthlyHistoryEntry
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

class MochikuRecapDataTest {

    private val utc = ZoneOffset.UTC

    private fun epochMs(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        return ZonedDateTime.of(year, month, day, hour, 0, 0, 0, utc).toInstant().toEpochMilli()
    }

    private fun entry(
        mangaId: Long,
        title: String,
        author: String?,
        readAtMs: Long,
        durationMinutes: Long,
    ): MonthlyHistoryEntry {
        return MonthlyHistoryEntry(
            mangaId = mangaId,
            title = title,
            author = author,
            artist = null,
            thumbnailUrl = null,
            sourceId = 1L,
            isFavorite = false,
            coverLastModified = 0L,
            readAtMs = readAtMs,
            readDurationMs = durationMinutes * 60_000L,
        )
    }

    @Test
    fun `aggregates multiple chapters per manga, authors, and favorite weekday comparison`() {
        // 2026-11-14 is Saturday, 2026-11-13 is Friday
        val satMs = epochMs(2026, 11, 14, 21)
        val friMs = epochMs(2026, 11, 13, 20)

        val entries = listOf(
            entry(1L, "Kagurabachi", "Takeru Hokazono", satMs, 30L),
            entry(1L, "Kagurabachi", "Takeru Hokazono", satMs + 1000L, 30L),
            entry(1L, "Kagurabachi", "Takeru Hokazono", satMs + 2000L, 30L),
            entry(1L, "Kagurabachi", "Takeru Hokazono", satMs + 3000L, 30L),
            entry(2L, "Sakamoto Days", "Yuto Suzuki", friMs, 40L),
            entry(2L, "Sakamoto Days", "Yuto Suzuki", friMs + 1000L, 40L),
        )

        val recap = computeMonthlyRecap(
            monthEntries = entries,
            libraryMangaList = emptyList(),
            allTimeDurationMs = 300L * 60_000L,
            now = LocalDate.of(2026, 11, 20),
            zoneId = utc,
            coverProvider = { null },
        )

        recap.hasReadThisMonth shouldBe true
        recap.monthName shouldBe "November"
        recap.monthChaptersRead shouldBe 6
        recap.monthDurationFormatted shouldBe "3h 20m"
        recap.allTimeDurationFormatted shouldBe "5h 0m"

        recap.heroManga?.title shouldBe "Kagurabachi"
        recap.heroManga?.chaptersRead shouldBe 4
        recap.heroManga?.durationFormatted shouldBe "2h 0m"

        recap.topAuthor shouldBe "Takeru Hokazono"
        recap.topAuthorCount shouldBe 4
        recap.topAuthorList shouldHaveSize 2

        // Saturday = 120 min vs Friday = 80 min -> 50% more than 2nd
        recap.favoriteWeekday shouldBe "Saturday"
        recap.favoriteWeekdayMinutes shouldBe 120L
        recap.secondWeekdayMinutes shouldBe 80L
        recap.percentMoreThanSecondDay shouldBe 50
    }

    @Test
    fun `caps top manga and authors at 10 and preserves long CJK titles`() {
        val satMs = epochMs(2026, 11, 14, 15)
        val longJapaneseTitle = "とても長い日本語の漫画タイトルで省略記号が正しく機能するか確認するシリーズ"

        val entries = (1L..12L).flatMap { id ->
            val count = (13 - id).toInt()
            val title = if (id == 1L) longJapaneseTitle else "Series $id"
            List(count) {
                entry(id, title, "Author $id", satMs, 10L)
            }
        }

        val recap = computeMonthlyRecap(
            monthEntries = entries,
            libraryMangaList = emptyList(),
            allTimeDurationMs = 0L,
            now = LocalDate.of(2026, 11, 20),
            zoneId = utc,
            coverProvider = { null },
        )

        recap.topMangaList shouldHaveSize 10
        recap.topAuthorList shouldHaveSize 10
        recap.heroManga?.title shouldBe longJapaneseTitle
        recap.heroManga?.chaptersRead shouldBe 12
        // Single active weekday -> percentMoreThanSecondDay is null
        recap.favoriteWeekday shouldBe "Saturday"
        recap.percentMoreThanSecondDay shouldBe null
    }

    @Test
    fun `empty monthly entries produce clean empty defaults`() {
        val recap = computeMonthlyRecap(
            monthEntries = emptyList(),
            libraryMangaList = emptyList(),
            allTimeDurationMs = 45L * 60_000L,
            now = LocalDate.of(2026, 11, 20),
            zoneId = utc,
            coverProvider = { null },
        )

        recap.hasReadThisMonth shouldBe false
        recap.monthChaptersRead shouldBe 0
        recap.monthDurationFormatted shouldBe "0m"
        recap.allTimeDurationFormatted shouldBe "45m"
        recap.topMangaList shouldHaveSize 0
        recap.topAuthorList shouldHaveSize 0
        recap.heroManga shouldBe null
        recap.topAuthor shouldBe null
        recap.favoriteWeekday shouldBe null
        recap.percentMoreThanSecondDay shouldBe null
        buildCoverGridTiles(recap) shouldHaveSize 0
        buildRouletteSlots(recap) shouldHaveSize 0
    }

    @Test
    fun `cover grid fills all 12 tiles and keeps hero exclusive when multiple series exist`() {
        val satMs = epochMs(2026, 11, 14, 15)
        val entries = (1L..10L).flatMap { id ->
            val count = (11 - id).toInt()
            List(count) { entry(id, "Series $id", "Author $id", satMs, 10L) }
        }
        val recap = computeMonthlyRecap(
            monthEntries = entries,
            libraryMangaList = emptyList(),
            allTimeDurationMs = 0L,
            now = LocalDate.of(2026, 11, 20),
            zoneId = utc,
            coverProvider = { null },
        )

        val gridTiles = buildCoverGridTiles(recap)
        gridTiles shouldHaveSize 12
        gridTiles[4].mangaId shouldBe 1L
        gridTiles.filterIndexed { index, _ -> index != 4 }.none { it.mangaId == 1L } shouldBe true

        val rouletteSlots = buildRouletteSlots(recap)
        rouletteSlots shouldHaveSize 12
        rouletteSlots[0].mangaId shouldBe 1L
        rouletteSlots.drop(1).none { it.mangaId == 1L } shouldBe true
    }

    @Test
    fun `cover grid and roulette fall back cleanly when only one series was read`() {
        val satMs = epochMs(2026, 11, 14, 15)
        val entries = listOf(entry(9L, "Solo Series", "Solo Author", satMs, 25L))
        val recap = computeMonthlyRecap(
            monthEntries = entries,
            libraryMangaList = emptyList(),
            allTimeDurationMs = 0L,
            now = LocalDate.of(2026, 11, 20),
            zoneId = utc,
            coverProvider = { null },
        )

        val gridTiles = buildCoverGridTiles(recap)
        gridTiles shouldHaveSize 12
        gridTiles.all { it.mangaId == 9L } shouldBe true

        val rouletteSlots = buildRouletteSlots(recap)
        rouletteSlots shouldHaveSize 12
        rouletteSlots.all { it.mangaId == 9L } shouldBe true
    }
}
