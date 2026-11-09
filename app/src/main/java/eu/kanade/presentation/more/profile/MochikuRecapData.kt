package eu.kanade.presentation.more.profile

import kotlinx.coroutines.flow.first
import tachiyomi.domain.history.interactor.GetTotalReadDuration
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.model.MangaCover
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class MochikuMangaStat(
    val mangaId: Long,
    val title: String,
    val author: String?,
    val cover: MangaCover?,
    val chaptersRead: Int,
    val durationMs: Long,
) {
    val durationFormatted: String
        get() {
            val hours = durationMs / (1000 * 60 * 60)
            val minutes = (durationMs / (1000 * 60)) % 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "< 1m"
            }
        }

    val hoursFloat: Float
        get() = durationMs / (1000f * 60f * 60f)
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
    val heroManga: MochikuMangaStat?,
    val topAuthor: String?,
    val topAuthorCount: Int,
    val hasReadThisMonth: Boolean,
)

object MochikuRecapDataEngine {

    suspend fun loadCurrentMonthRecap(): MochikuRecapData {
        val historyRepo = Injekt.get<HistoryRepository>()
        val getLibraryManga = Injekt.get<GetLibraryManga>()
        val getTotalReadDuration = Injekt.get<GetTotalReadDuration>()

        val allTimeDurationMs = runCatching { getTotalReadDuration.await() }.getOrDefault(0L)
        val libraryMangaList = runCatching { getLibraryManga.await() }.getOrDefault(emptyList())
        val libraryCount = libraryMangaList.size
        val allTimeChaptersRead = libraryMangaList.sumOf { it.readCount }.toInt()

        val allHistory = runCatching {
            historyRepo.getHistory(
                query = "",
                unfinishedManga = null,
                unfinishedChapter = null,
                nonLibraryEntries = null,
            ).first()
        }.getOrDefault(emptyList())

        val now = LocalDate.now()
        val currentYear = now.year
        val currentMonth = now.monthValue
        val monthName = now.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

        // Filter history entries strictly read in the current calendar month
        val monthHistory = allHistory.filter { item ->
            val date = item.readAt ?: return@filter false
            val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            localDate.year == currentYear && localDate.monthValue == currentMonth
        }

        val monthDurationMs = monthHistory.sumOf { it.readDuration }
        val monthChaptersRead = monthHistory.size

        val libraryMap = libraryMangaList.associateBy { it.id }

        // Group history by mangaId to find top manga this month
        val groupedByManga = monthHistory.groupBy { it.mangaId }
        val topMangaList = groupedByManga.map { (mangaId, histories) ->
            val first = histories.first()
            val libManga = libraryMap[mangaId]
            MochikuMangaStat(
                mangaId = mangaId,
                title = first.title.ifBlank { libManga?.manga?.title ?: "Manga #$mangaId" },
                author = libManga?.manga?.author?.takeIf { it.isNotBlank() }
                    ?: libManga?.manga?.artist?.takeIf { it.isNotBlank() },
                cover = first.coverData,
                chaptersRead = histories.size,
                durationMs = histories.sumOf { it.readDuration },
            )
        }.sortedByDescending { it.chaptersRead }

        val heroManga = topMangaList.firstOrNull()

        // Find top author from this month's read manga
        val authorCounts = topMangaList.mapNotNull { it.author }.groupingBy { it }.eachCount()
        val topAuthorEntry = authorCounts.maxByOrNull { it.value }

        val allTimeHours = allTimeDurationMs / (1000 * 60 * 60)
        val allTimeMinutes = (allTimeDurationMs / (1000 * 60)) % 60
        val allTimeFormatted = if (allTimeHours > 0) "${allTimeHours}h ${allTimeMinutes}m" else "${allTimeMinutes}m"

        val monthHours = monthDurationMs / (1000 * 60 * 60)
        val monthMinutes = (monthDurationMs / (1000 * 60)) % 60
        val monthFormatted = if (monthHours > 0) "${monthHours}h ${monthMinutes}m" else "${monthMinutes}m"

        return MochikuRecapData(
            monthName = monthName,
            year = currentYear,
            allTimeDurationFormatted = allTimeFormatted,
            allTimeDurationMs = allTimeDurationMs,
            monthDurationFormatted = monthFormatted,
            monthDurationMs = monthDurationMs,
            monthChaptersRead = monthChaptersRead,
            allTimeChaptersRead = allTimeChaptersRead,
            libraryCount = libraryCount,
            topMangaList = topMangaList,
            heroManga = heroManga,
            topAuthor = topAuthorEntry?.key,
            topAuthorCount = topAuthorEntry?.value ?: 0,
            hasReadThisMonth = topMangaList.isNotEmpty(),
        )
    }
}
