package eu.kanade.presentation.more.profile

import tachiyomi.domain.history.interactor.GetTotalReadDuration
import tachiyomi.domain.history.model.MonthlyHistoryEntry
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.model.MangaCover
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private const val MAX_RECAP_ENTRIES = 10

internal fun formatDurationMs(durationMs: Long, showSubMinuteLabel: Boolean = false): String {
    val safeMs = durationMs.coerceAtLeast(0L)
    val hours = safeMs / (1000L * 60L * 60L)
    val minutes = (safeMs / (1000L * 60L)) % 60L
    return when {
        hours > 0L -> "${hours}h ${minutes}m"
        minutes > 0L -> "${minutes}m"
        showSubMinuteLabel -> "< 1m"
        else -> "0m"
    }
}

data class MochikuMangaStat(
    val mangaId: Long,
    val title: String,
    val author: String?,
    val cover: MangaCover?,
    val chaptersRead: Int,
    val durationMs: Long,
) {
    val durationFormatted: String
        get() = formatDurationMs(durationMs, showSubMinuteLabel = true)

    val hoursFloat: Float
        get() = durationMs / (1000f * 60f * 60f)
}

data class MochikuAuthorStat(
    val author: String,
    val chaptersRead: Int,
    val durationMs: Long,
    val topMangaTitle: String?,
) {
    val durationFormatted: String
        get() = formatDurationMs(durationMs, showSubMinuteLabel = true)
}

data class MochikuRecapData(
    val monthName: String,
    val year: Int,
    val allTimeDurationFormatted: String,
    val allTimeDurationMs: Long,
    val monthDurationFormatted: String,
    val monthDurationMs: Long,
    val monthChaptersRead: Int,
    val allTimeChaptersRead: Int,
    val libraryCount: Int,
    val topMangaList: List<MochikuMangaStat>,
    val topAuthorList: List<MochikuAuthorStat>,
    val heroManga: MochikuMangaStat?,
    val topAuthor: String?,
    val topAuthorCount: Int,
    val favoriteWeekday: String?,
    val favoriteWeekdayMinutes: Long,
    val secondWeekdayMinutes: Long,
    val percentMoreThanSecondDay: Int?,
    val hasReadThisMonth: Boolean,
)

/**
 * Pure aggregator for monthly recap statistics so domain/presentation math can be verified
 * independently of database or Injekt state.
 */
internal fun computeMonthlyRecap(
    monthEntries: List<MonthlyHistoryEntry>,
    libraryMangaList: List<LibraryManga>,
    allTimeDurationMs: Long,
    now: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    coverProvider: (MonthlyHistoryEntry) -> MangaCover? = { entry ->
        MangaCover(
            mangaId = entry.mangaId,
            sourceId = entry.sourceId,
            isMangaFavorite = entry.isFavorite,
            ogUrl = entry.thumbnailUrl,
            lastModified = entry.coverLastModified,
        )
    },
): MochikuRecapData {
    val libraryCount = libraryMangaList.size
    val allTimeChaptersRead = libraryMangaList.sumOf { it.readCount }.toInt()
    val monthName = now.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

    val monthDurationMs = monthEntries.sumOf { it.readDurationMs.coerceAtLeast(0L) }
    val monthChaptersRead = monthEntries.size

    val libraryMap = libraryMangaList.associateBy { it.id }

    val mangaById = monthEntries.groupBy { it.mangaId }.map { (mangaId, histories) ->
        val first = histories.first()
        val libManga = libraryMap[mangaId]
        MochikuMangaStat(
            mangaId = mangaId,
            title = libManga?.manga?.title?.takeIf { it.isNotBlank() }
                ?: first.title.ifBlank { "Manga #$mangaId" },
            author = libManga?.manga?.author?.takeIf { it.isNotBlank() }
                ?: libManga?.manga?.artist?.takeIf { it.isNotBlank() }
                ?: first.author?.takeIf { it.isNotBlank() }
                ?: first.artist?.takeIf { it.isNotBlank() },
            cover = coverProvider(first),
            chaptersRead = histories.size,
            durationMs = histories.sumOf { it.readDurationMs.coerceAtLeast(0L) },
        )
    }.sortedWith(
        compareByDescending<MochikuMangaStat> { it.chaptersRead }
            .thenByDescending { it.durationMs },
    )

    val topMangaList = mangaById.take(MAX_RECAP_ENTRIES)
    val heroManga = topMangaList.firstOrNull()

    val topAuthorList = mangaById
        .filter { !it.author.isNullOrBlank() }
        .groupBy { it.author!! }
        .map { (authorName, seriesList) ->
            val topSeries = seriesList.maxByOrNull { it.chaptersRead }
            MochikuAuthorStat(
                author = authorName,
                chaptersRead = seriesList.sumOf { it.chaptersRead },
                durationMs = seriesList.sumOf { it.durationMs },
                topMangaTitle = topSeries?.title,
            )
        }
        .sortedWith(
            compareByDescending<MochikuAuthorStat> { it.chaptersRead }
                .thenByDescending { it.durationMs },
        )
        .take(MAX_RECAP_ENTRIES)

    val topAuthorEntry = topAuthorList.firstOrNull()

    val weekdayBuckets = monthEntries
        .filter { it.readAtMs > 0L }
        .groupBy { entry ->
            Instant.ofEpochMilli(entry.readAtMs).atZone(zoneId).dayOfWeek
        }
        .map { (dayOfWeek, entries) ->
            Triple(
                dayOfWeek,
                entries.sumOf { it.readDurationMs.coerceAtLeast(0L) },
                entries.size,
            )
        }
        .sortedWith(
            compareByDescending<Triple<java.time.DayOfWeek, Long, Int>> { it.second }
                .thenByDescending { it.third },
        )

    val topDay = weekdayBuckets.firstOrNull()
    val secondDay = weekdayBuckets.getOrNull(1)
    val topDayDurationMs = topDay?.second ?: 0L
    val secondDayDurationMs = secondDay?.second ?: 0L
    val percentMoreThanSecondDay = if (topDay != null && secondDay != null && secondDayDurationMs > 0L) {
        (((topDayDurationMs - secondDayDurationMs).toDouble() / secondDayDurationMs.toDouble()) * 100.0)
            .roundToInt()
            .coerceAtLeast(0)
    } else {
        null
    }

    return MochikuRecapData(
        monthName = monthName,
        year = now.year,
        allTimeDurationFormatted = formatDurationMs(allTimeDurationMs),
        allTimeDurationMs = allTimeDurationMs,
        monthDurationFormatted = formatDurationMs(monthDurationMs),
        monthDurationMs = monthDurationMs,
        monthChaptersRead = monthChaptersRead,
        allTimeChaptersRead = allTimeChaptersRead,
        libraryCount = libraryCount,
        topMangaList = topMangaList,
        topAuthorList = topAuthorList,
        heroManga = heroManga,
        topAuthor = topAuthorEntry?.author,
        topAuthorCount = topAuthorEntry?.chaptersRead ?: 0,
        favoriteWeekday = topDay?.first?.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
        favoriteWeekdayMinutes = topDayDurationMs / 60_000L,
        secondWeekdayMinutes = secondDayDurationMs / 60_000L,
        percentMoreThanSecondDay = percentMoreThanSecondDay,
        hasReadThisMonth = topMangaList.isNotEmpty(),
    )
}

object MochikuRecapDataEngine {

    suspend fun loadCurrentMonthRecap(): MochikuRecapData {
        val historyRepo = Injekt.get<HistoryRepository>()
        val getLibraryManga = Injekt.get<GetLibraryManga>()
        val getTotalReadDuration = Injekt.get<GetTotalReadDuration>()

        val zoneId = ZoneId.systemDefault()
        val now = LocalDate.now(zoneId)
        val monthStartMs = now.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val nextMonthStartMs = now.withDayOfMonth(1).plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val allTimeDurationMs = runCatching { getTotalReadDuration.await() }.getOrDefault(0L)
        val libraryMangaList = runCatching { getLibraryManga.await() }.getOrDefault(emptyList())
        val monthEntries = runCatching {
            historyRepo.getHistoryInRange(monthStartMs, nextMonthStartMs)
        }.getOrDefault(emptyList())

        return computeMonthlyRecap(
            monthEntries = monthEntries,
            libraryMangaList = libraryMangaList,
            allTimeDurationMs = allTimeDurationMs,
            now = now,
            zoneId = zoneId,
        )
    }
}
