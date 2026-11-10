package tachiyomi.domain.history.interactor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tachiyomi.domain.history.model.StampState
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.history.service.PassportPreferences
import tachiyomi.domain.manga.interactor.GetLibraryManga
import java.time.ZoneId

/**
 * Observes history entries and library completion to emit live [StampState] milestones while
 * persisting sticky unlock timestamps and active reading days in [PassportPreferences].
 */
class GetPassportStamps(
    private val historyRepository: HistoryRepository,
    private val getLibraryManga: GetLibraryManga,
    private val passportPreferences: PassportPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
) {

    fun subscribe(): Flow<List<StampState>> {
        return combine(
            historyRepository.getAllHistory(),
            getLibraryManga.subscribe(),
            passportPreferences.unlockedStamps().changes(),
            passportPreferences.seenStampIds().changes(),
            passportPreferences.activeReadingDates().changes(),
        ) { history, libraryManga, rawUnlocked, seenIds, rawActiveDates ->
            val previousUnlocked = PassportPreferences.parseUnlockedMap(rawUnlocked)
            val observedDates = PassportPreferences.parseActiveDates(rawActiveDates)

            val result = computePassportStats(
                historyEntries = history,
                libraryManga = libraryManga,
                observedActiveDates = observedDates,
                previouslyUnlockedAt = previousUnlocked,
                seenStampIds = seenIds,
                zoneId = zoneId,
                nowEpochMs = nowProvider(),
            )

            val mergedUnlocked = buildMap {
                putAll(previousUnlocked)
                result.stamps.forEach { state ->
                    val unlocked = state.unlockedAt ?: return@forEach
                    putIfAbsent(state.stamp.id, unlocked)
                }
            }
            val serializedUnlocked = PassportPreferences.serializeUnlockedMap(mergedUnlocked)
            if (serializedUnlocked != rawUnlocked) {
                passportPreferences.unlockedStamps().set(serializedUnlocked)
            }

            val serializedDates = PassportPreferences.serializeActiveDates(result.activeDates)
            if (serializedDates != rawActiveDates) {
                passportPreferences.activeReadingDates().set(serializedDates)
            }

            result.stamps
        }
            .distinctUntilChanged()
            .flowOn(dispatcher)
    }

    fun markStampSeen(stampId: String) {
        val pref = passportPreferences.seenStampIds()
        val current = pref.get()
        if (stampId !in current) {
            pref.set(current + stampId)
        }
    }
}
