package tachiyomi.data.history

import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.history.model.MonthlyHistoryEntry
import tachiyomi.domain.manga.model.MangaCover
import java.util.Date

object HistoryMapper {
    fun mapHistory(
        id: Long,
        chapterId: Long,
        readAt: Date?,
        readDuration: Long,
    ): History = History(
        id = id,
        chapterId = chapterId,
        readAt = readAt,
        readDuration = readDuration,
    )

    fun mapMonthlyHistoryEntry(
        mangaId: Long,
        title: String,
        author: String?,
        artist: String?,
        thumbnailUrl: String?,
        sourceId: Long,
        isFavorite: Boolean,
        coverLastModified: Long,
        readAt: Date?,
        readDuration: Long,
    ): MonthlyHistoryEntry = MonthlyHistoryEntry(
        mangaId = mangaId,
        title = title,
        author = author,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        sourceId = sourceId,
        isFavorite = isFavorite,
        coverLastModified = coverLastModified,
        readAtMs = readAt?.time ?: 0L,
        readDurationMs = readDuration,
    )

    fun mapHistoryWithRelations(
        historyId: Long,
        mangaId: Long,
        chapterId: Long,
        title: String,
        thumbnailUrl: String?,
        sourceId: Long,
        isFavorite: Boolean,
        coverLastModified: Long,
        chapterNumber: Double,
        // KMK -->
        read: Boolean,
        lastPageRead: Long,
        totalCount: Double,
        readCount: Double,
        // KMK <--
        readAt: Date?,
        readDuration: Long,
    ): HistoryWithRelations = HistoryWithRelations(
        id = historyId,
        chapterId = chapterId,
        mangaId = mangaId,
        // SY -->
        ogTitle = title,
        // SY <--
        chapterNumber = chapterNumber,
        // KMK -->
        read = read,
        lastPageRead = lastPageRead,
        totalCountCalculated = totalCount.toLong(),
        readCountCalculated = readCount.toLong(),
        // KMK <--
        readAt = readAt,
        readDuration = readDuration,
        coverData = MangaCover(
            mangaId = mangaId,
            sourceId = sourceId,
            isMangaFavorite = isFavorite,
            ogUrl = thumbnailUrl,
            lastModified = coverLastModified,
        ),
    )
}
