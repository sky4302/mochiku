package tachiyomi.domain.history.model

import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.MangaCover

/**
 * Snapshot of the most recent reading session and the target chapter to resume or start next.
 */
data class ContinueReadingItem(
    val mangaId: Long,
    val mangaTitle: String,
    val coverData: MangaCover,
    val chapter: Chapter,
    val lastReadAt: Long,
)
