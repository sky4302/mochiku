package tachiyomi.domain.history.interactor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tachiyomi.domain.chapter.interactor.GetChapter
import tachiyomi.domain.history.model.ContinueReadingItem
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.history.service.HistoryPreferences

/**
 * Observes the most recent reading history entry and resolves the target chapter to continue
 * reading (either an unread chapter ahead, an active mid-chapter re-read, or the next chapter in an
 * active re-read session).
 */
class GetContinueReading(
    private val historyRepository: HistoryRepository,
    private val getNextChapters: GetNextChapters,
    private val getChapter: GetChapter,
    private val historyPreferences: HistoryPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun subscribe(): Flow<ContinueReadingItem?> {
        return combine(
            historyRepository.getHistory(
                query = "",
                unfinishedManga = null,
                unfinishedChapter = null,
                nonLibraryEntries = null,
            ),
            historyPreferences.rereadContinueState().changes(),
        ) { entries, rawRereadState ->
            val latest = entries.firstOrNull() ?: return@combine null
            val subsequentOrCurrent = getNextChapters.await(
                mangaId = latest.mangaId,
                fromChapterId = latest.chapterId,
                onlyUnread = false,
            )

            val firstUnread = subsequentOrCurrent.firstOrNull { !it.read }
            val (targetChapter, resumePage) = if (firstUnread != null) {
                firstUnread to null
            } else {
                val rereadState = HistoryPreferences.parseRereadState(rawRereadState)
                    ?.takeIf { it.mangaId == latest.mangaId && it.chapterId == latest.chapterId }
                    ?: return@combine null

                if (!rereadState.isCompleted) {
                    val currentChapter = getChapter.await(latest.chapterId) ?: return@combine null
                    currentChapter to currentChapter.lastPageRead.toInt().coerceAtLeast(0)
                } else {
                    val nextChapter = subsequentOrCurrent.firstOrNull() ?: return@combine null
                    nextChapter.copy(lastPageRead = 0L) to 0
                }
            }

            ContinueReadingItem(
                mangaId = latest.mangaId,
                mangaTitle = latest.title,
                coverData = latest.coverData,
                chapter = targetChapter,
                lastReadAt = latest.readAt?.time ?: 0L,
                resumePage = resumePage,
            )
        }
            .distinctUntilChanged()
            .flowOn(dispatcher)
    }
}
