package tachiyomi.domain.history.interactor

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.domain.chapter.interactor.GetChapter
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.history.service.HistoryPreferences
import tachiyomi.domain.manga.model.MangaCover
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class GetContinueReadingTest {

    private lateinit var historyRepository: HistoryRepository
    private lateinit var getNextChapters: GetNextChapters
    private lateinit var getChapter: GetChapter
    private lateinit var historyPreferences: HistoryPreferences
    private lateinit var getContinueReading: GetContinueReading

    @BeforeEach
    fun beforeEach() {
        historyRepository = mockk()
        getNextChapters = mockk()
        getChapter = mockk()
        val rereadFlow = MutableStateFlow("")
        val fakeStringPref = object : Preference<String> {
            override fun key(): String = "pref_continue_reading_reread_state"
            override fun get(): String = rereadFlow.value
            override fun set(value: String) {
                rereadFlow.value = value
            }
            override fun isSet(): Boolean = rereadFlow.value.isNotEmpty()
            override fun delete() {
                rereadFlow.value = ""
            }
            override fun defaultValue(): String = ""
            override fun changes(): Flow<String> = rereadFlow
            override fun stateIn(scope: CoroutineScope): StateFlow<String> =
                rereadFlow.stateIn(scope, SharingStarted.Eagerly, rereadFlow.value)
        }
        val preferenceStore = mockk<PreferenceStore> {
            every { getString(any(), any()) } returns fakeStringPref
        }
        historyPreferences = HistoryPreferences(preferenceStore)
        getContinueReading = GetContinueReading(
            historyRepository = historyRepository,
            getNextChapters = getNextChapters,
            getChapter = getChapter,
            historyPreferences = historyPreferences,
            dispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun `empty history emits null`() = runTest {
        every { historyRepository.getHistory("", null, null, null) } returns flowOf(emptyList())

        val result = getContinueReading.subscribe().first()

        result shouldBe null
    }

    @Test
    fun `unfinished latest chapter emits current chapter`() = runTest {
        val cover = mockk<MangaCover>()
        val latest = mockHistoryEntry(
            mangaId = 10L,
            chapterId = 101L,
            title = "Chainsaw Man",
            readAtMs = 5_000L,
            cover = cover,
        )
        val currentChapter = Chapter.create().copy(
            id = 101L,
            mangaId = 10L,
            chapterNumber = 12.0,
            read = false,
            lastPageRead = 14L,
        )
        val nextChapter = Chapter.create().copy(
            id = 102L,
            mangaId = 10L,
            chapterNumber = 13.0,
            read = false,
        )
        every { historyRepository.getHistory("", null, null, null) } returns flowOf(listOf(latest))
        coEvery { getNextChapters.await(10L, 101L, onlyUnread = false) } returns listOf(currentChapter, nextChapter)

        val result = getContinueReading.subscribe().first()

        result?.mangaId shouldBe 10L
        result?.mangaTitle shouldBe "Chainsaw Man"
        result?.coverData shouldBe cover
        result?.chapter shouldBe currentChapter
        result?.lastReadAt shouldBe 5_000L
    }

    @Test
    fun `finished latest chapter advances to first unread chapter`() = runTest {
        val latest = mockHistoryEntry(
            mangaId = 20L,
            chapterId = 201L,
            title = "Dandadan",
            readAtMs = 9_000L,
        )
        val alreadyReadNext = Chapter.create().copy(
            id = 202L,
            mangaId = 20L,
            chapterNumber = 2.0,
            read = true,
        )
        val targetUnread = Chapter.create().copy(
            id = 203L,
            mangaId = 20L,
            chapterNumber = 3.0,
            read = false,
            lastPageRead = 0L,
        )
        every { historyRepository.getHistory("", null, null, null) } returns flowOf(listOf(latest))
        coEvery { getNextChapters.await(20L, 201L, onlyUnread = false) } returns listOf(alreadyReadNext, targetUnread)

        val result = getContinueReading.subscribe().first()

        result?.mangaId shouldBe 20L
        result?.chapter shouldBe targetUnread
        result?.lastReadAt shouldBe 9_000L
    }

    @Test
    fun `finished latest chapter with no unread left emits null`() = runTest {
        val latest = mockHistoryEntry(
            mangaId = 30L,
            chapterId = 305L,
            title = "Monster",
            readAtMs = 12_000L,
        )
        val trailingRead = Chapter.create().copy(
            id = 306L,
            mangaId = 30L,
            chapterNumber = 162.0,
            read = true,
        )
        every { historyRepository.getHistory("", null, null, null) } returns flowOf(listOf(latest))
        coEvery { getNextChapters.await(30L, 305L, onlyUnread = false) } returns listOf(trailingRead)

        val result = getContinueReading.subscribe().first()

        result shouldBe null
    }

    @Test
    fun `active mid-chapter reread emits current read chapter with resumePage`() = runTest {
        val latest = mockHistoryEntry(
            mangaId = 40L,
            chapterId = 405L,
            title = "Solo Leveling",
            readAtMs = 15_000L,
        )
        val currentReadChapter = Chapter.create().copy(
            id = 405L,
            mangaId = 40L,
            chapterNumber = 5.0,
            read = true,
            lastPageRead = 11L,
        )
        val nextReadChapter = Chapter.create().copy(
            id = 406L,
            mangaId = 40L,
            chapterNumber = 6.0,
            read = true,
            lastPageRead = 24L,
        )
        every { historyRepository.getHistory("", null, null, null) } returns flowOf(listOf(latest))
        coEvery { getNextChapters.await(40L, 405L, onlyUnread = false) } returns listOf(nextReadChapter)
        coEvery { getChapter.await(405L) } returns currentReadChapter
        historyPreferences.setRereadState(mangaId = 40L, chapterId = 405L, isCompleted = false)

        val result = getContinueReading.subscribe().first()

        result?.mangaId shouldBe 40L
        result?.chapter shouldBe currentReadChapter
        result?.resumePage shouldBe 11
    }

    @Test
    fun `completed reread chapter advances to next chapter starting at page 0`() = runTest {
        val latest = mockHistoryEntry(
            mangaId = 40L,
            chapterId = 405L,
            title = "Solo Leveling",
            readAtMs = 16_000L,
        )
        val nextReadChapter = Chapter.create().copy(
            id = 406L,
            mangaId = 40L,
            chapterNumber = 6.0,
            read = true,
            lastPageRead = 24L,
        )
        every { historyRepository.getHistory("", null, null, null) } returns flowOf(listOf(latest))
        coEvery { getNextChapters.await(40L, 405L, onlyUnread = false) } returns listOf(nextReadChapter)
        historyPreferences.setRereadState(mangaId = 40L, chapterId = 405L, isCompleted = true)

        val result = getContinueReading.subscribe().first()

        result?.mangaId shouldBe 40L
        result?.chapter?.id shouldBe 406L
        result?.chapter?.lastPageRead shouldBe 0L
        result?.resumePage shouldBe 0
    }

    @Test
    fun `completed reread on final chapter emits null`() = runTest {
        val latest = mockHistoryEntry(
            mangaId = 40L,
            chapterId = 500L,
            title = "Solo Leveling",
            readAtMs = 17_000L,
        )
        every { historyRepository.getHistory("", null, null, null) } returns flowOf(listOf(latest))
        coEvery { getNextChapters.await(40L, 500L, onlyUnread = false) } returns emptyList()
        historyPreferences.setRereadState(mangaId = 40L, chapterId = 500L, isCompleted = true)

        val result = getContinueReading.subscribe().first()

        result shouldBe null
    }

    private fun mockHistoryEntry(
        mangaId: Long,
        chapterId: Long,
        title: String,
        readAtMs: Long,
        cover: MangaCover = mockk(),
    ): HistoryWithRelations {
        return mockk {
            every { this@mockk.mangaId } returns mangaId
            every { this@mockk.chapterId } returns chapterId
            every { this@mockk.title } returns title
            every { this@mockk.readAt } returns Date(readAtMs)
            every { this@mockk.coverData } returns cover
        }
    }
}
