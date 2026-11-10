package tachiyomi.domain.history.service

import tachiyomi.core.common.preference.PreferenceStore
import java.time.LocalDate

/**
 * Persists Mochiku Passport stamp unlock timestamps, seen announcement IDs, and observed active
 * reading days (`yyyy-MM-dd`) so rereading or clearing history never revokes earned milestones.
 */
class PassportPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun unlockedStamps() = preferenceStore.getStringSet(
        "pref_passport_unlocked_stamps",
        emptySet(),
    )

    fun seenStampIds() = preferenceStore.getStringSet(
        "pref_passport_seen_stamps",
        emptySet(),
    )

    fun activeReadingDates() = preferenceStore.getStringSet(
        "pref_passport_active_dates",
        emptySet(),
    )

    companion object {
        fun parseUnlockedMap(rawSet: Set<String>): Map<String, Long> {
            return buildMap {
                rawSet.forEach { entry ->
                    val sep = entry.lastIndexOf(':')
                    if (sep <= 0 || sep == entry.lastIndex) return@forEach
                    val id = entry.substring(0, sep)
                    val epochMs = entry.substring(sep + 1).toLongOrNull() ?: return@forEach
                    val existing = this[id]
                    if (existing == null || epochMs < existing) {
                        put(id, epochMs)
                    }
                }
            }
        }

        fun serializeUnlockedMap(unlocked: Map<String, Long>): Set<String> {
            return unlocked.entries.map { (id, epochMs) -> "$id:$epochMs" }.toSet()
        }

        fun parseActiveDates(rawSet: Set<String>): Set<LocalDate> {
            return rawSet.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        }

        fun serializeActiveDates(dates: Set<LocalDate>): Set<String> {
            return dates.map { it.toString() }.toSet()
        }
    }
}
