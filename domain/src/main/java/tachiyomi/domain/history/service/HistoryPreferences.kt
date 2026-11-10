package tachiyomi.domain.history.service

import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.TriState
import tachiyomi.core.common.preference.getEnum
import tachiyomi.domain.updates.service.USE_PANORAMA_COVER_PREF

class HistoryPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun filterUnfinishedManga() = preferenceStore.getEnum(
        "pref_filter_history_unfinished_manga",
        TriState.DISABLED,
    )

    fun filterUnfinishedChapter() = preferenceStore.getEnum(
        "pref_filter_history_unfinished_chapter",
        TriState.DISABLED,
    )

    fun filterNonLibraryManga() = preferenceStore.getEnum(
        "pref_filter_history_non_library_manga",
        TriState.DISABLED,
    )

    fun usePanoramaCover() = preferenceStore.getBoolean(
        USE_PANORAMA_COVER_PREF,
        false,
    )

    // KMK -->
    fun rereadContinueState() = preferenceStore.getString(
        "pref_continue_reading_reread_state",
        "",
    )

    fun setRereadState(mangaId: Long, chapterId: Long, isCompleted: Boolean) {
        rereadContinueState().set("$mangaId:$chapterId:$isCompleted")
    }

    fun clearRereadState() {
        rereadContinueState().set("")
    }

    data class ContinueRereadState(
        val mangaId: Long,
        val chapterId: Long,
        val isCompleted: Boolean,
    )

    companion object {
        fun parseRereadState(raw: String): ContinueRereadState? {
            if (raw.isBlank()) return null
            val parts = raw.split(':')
            if (parts.size != 3) return null
            val mangaId = parts[0].toLongOrNull() ?: return null
            val chapterId = parts[1].toLongOrNull() ?: return null
            val isCompleted = parts[2].toBooleanStrictOrNull() ?: return null
            return ContinueRereadState(
                mangaId = mangaId,
                chapterId = chapterId,
                isCompleted = isCompleted,
            )
        }
    }
    // KMK <--
}
