package tachiyomi.domain.history.interactor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import tachiyomi.domain.history.model.ContinueReadingItem
import tachiyomi.domain.history.repository.HistoryRepository

/**
 * Observes the most recent reading history entry and resolves the target chapter to continue
 * reading (either the current unfinished chapter or the next unread chapter after it).
 */
class GetContinueReading(
    private val historyRepository: HistoryRepository,
    private val getNextChapters: GetNextChapters,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun subscribe(): Flow<ContinueReadingItem?> {
        return historyRepository.getHistory(
            query = "",
            unfinishedManga = null,
            unfinishedChapter = null,
            nonLibraryEntries = null,
        )
            .mapLatest { entries ->
                val latest = entries.firstOrNull() ?: return@mapLatest null
                val targetChapter = getNextChapters.await(
                    mangaId = latest.mangaId,
                    fromChapterId = latest.chapterId,
                    onlyUnread = false,
                ).firstOrNull { !it.read } ?: return@mapLatest null

                ContinueReadingItem(
                    mangaId = latest.mangaId,
                    mangaTitle = latest.title,
                    coverData = latest.coverData,
                    chapter = targetChapter,
                    lastReadAt = latest.readAt?.time ?: 0L,
                )
            }
            .distinctUntilChanged()
            .flowOn(dispatcher)
    }
}
