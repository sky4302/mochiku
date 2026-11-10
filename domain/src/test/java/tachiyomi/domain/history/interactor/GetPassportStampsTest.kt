package tachiyomi.domain.history.interactor

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.PassportStamp
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.history.service.PassportPreferences
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.interactor.GetLibraryManga
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class GetPassportStampsTest {

    @Test
    fun `unlocked stamp stays unlocked and marked seen after history is cleared`() = runTest {
        val historyFlow = MutableStateFlow(emptyList<History>())
        val libraryFlow = MutableStateFlow(emptyList<LibraryManga>())
        val historyRepository = mockk<HistoryRepository> {
            every { getAllHistory() } returns historyFlow
        }
        val getLibraryManga = mockk<GetLibraryManga> {
            every { subscribe() } returns libraryFlow
        }
        val preferenceStore = InMemoryPreferenceStore()
        val passportPreferences = PassportPreferences(preferenceStore)
        val utc = ZoneOffset.UTC
        val unlockEpochMs = ZonedDateTime.of(2026, 11, 10, 15, 0, 0, 0, utc).toInstant().toEpochMilli()

        val getPassportStamps = GetPassportStamps(
            historyRepository = historyRepository,
            getLibraryManga = getLibraryManga,
            passportPreferences = passportPreferences,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            zoneId = utc,
            nowProvider = { unlockEpochMs },
        )

        // Initially locked
        val initial = getPassportStamps.subscribe().first()
        initial.first { it.stamp == PassportStamp.FIRST_PAGE }.isUnlocked shouldBe false

        // Read 1 chapter -> FIRST_PAGE unlocks and persists
        historyFlow.value = listOf(
            History(id = 1L, chapterId = 10L, readAt = Date(unlockEpochMs), readDuration = 10_000L),
        )
        val afterRead = getPassportStamps.subscribe().first()
        afterRead.first { it.stamp == PassportStamp.FIRST_PAGE }.apply {
            isUnlocked shouldBe true
            unlockedAt shouldBe unlockEpochMs
            isSeen shouldBe false
        }

        // Clear history completely -> FIRST_PAGE must remain unlocked with original timestamp
        historyFlow.value = emptyList()
        val afterClear = getPassportStamps.subscribe().first()
        afterClear.first { it.stamp == PassportStamp.FIRST_PAGE }.apply {
            isUnlocked shouldBe true
            unlockedAt shouldBe unlockEpochMs
            isSeen shouldBe false
        }

        // Mark stamp seen -> isSeen flips to true while staying unlocked
        getPassportStamps.markStampSeen(PassportStamp.FIRST_PAGE.id)
        val afterSeen = getPassportStamps.subscribe().first()
        afterSeen.first { it.stamp == PassportStamp.FIRST_PAGE }.apply {
            isUnlocked shouldBe true
            unlockedAt shouldBe unlockEpochMs
            isSeen shouldBe true
        }
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val flows = mutableMapOf<String, MutableStateFlow<Any?>>()

        @Suppress("UNCHECKED_CAST")
        private fun <T> createPref(key: String, defaultValue: T): Preference<T> {
            val flow = flows.getOrPut(key) { MutableStateFlow(defaultValue) } as MutableStateFlow<T>
            return object : Preference<T> {
                override fun key(): String = key
                override fun get(): T = flow.value
                override fun set(value: T) {
                    flow.value = value
                }
                override fun isSet(): Boolean = flow.value != defaultValue
                override fun delete() {
                    flow.value = defaultValue
                }
                override fun defaultValue(): T = defaultValue
                override fun changes(): Flow<T> = flow
                override fun stateIn(scope: CoroutineScope): StateFlow<T> =
                    flow.stateIn(scope, SharingStarted.Eagerly, flow.value)
            }
        }

        override fun getString(key: String, defaultValue: String) = createPref(key, defaultValue)
        override fun getLong(key: String, defaultValue: Long) = createPref(key, defaultValue)
        override fun getInt(key: String, defaultValue: Int) = createPref(key, defaultValue)
        override fun getFloat(key: String, defaultValue: Float) = createPref(key, defaultValue)
        override fun getBoolean(key: String, defaultValue: Boolean) = createPref(key, defaultValue)
        override fun getStringSet(key: String, defaultValue: Set<String>) = createPref(key, defaultValue)

        override fun <T> getObjectFromString(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T,
        ) = createPref(key, defaultValue)

        override fun <T> getObjectFromInt(
            key: String,
            defaultValue: T,
            serializer: (T) -> Int,
            deserializer: (Int) -> T,
        ) = createPref(key, defaultValue)

        override fun getAll(): Map<String, *> = flows.mapValues { it.value.value }
    }
}
