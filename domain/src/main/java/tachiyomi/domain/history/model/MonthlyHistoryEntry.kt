package tachiyomi.domain.history.model

/**
 * Chapter-level reading history joined with basic manga metadata for monthly recap aggregation.
 */
data class MonthlyHistoryEntry(
    val mangaId: Long,
    val title: String,
    val author: String?,
    val artist: String?,
    val thumbnailUrl: String?,
    val sourceId: Long,
    val isFavorite: Boolean,
    val coverLastModified: Long,
    val readAtMs: Long,
    val readDurationMs: Long,
)
