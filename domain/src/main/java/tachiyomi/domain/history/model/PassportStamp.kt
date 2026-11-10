package tachiyomi.domain.history.model

import java.time.LocalDate

/**
 * Milestones earned from on-device reading history and library completion.
 */
enum class PassportStamp(
    val id: String,
    val goal: Int,
) {
    FIRST_PAGE(id = "first_page", goal = 1),
    BOOKWORM_100(id = "bookworm_100", goal = 100),
    BOOKWORM_1000(id = "bookworm_1000", goal = 1000),
    STREAK_7(id = "streak_7", goal = 7),
    STREAK_30(id = "streak_30", goal = 30),
    NIGHT_OWL(id = "night_owl", goal = 20),
    MARATHON(id = "marathon", goal = 30),
    FINISHER(id = "finisher", goal = 1),
    ;

    /**
     * Deterministic ink-stamp tilt in [-12f, +12f] degrees derived strictly from [id].
     */
    val angleDegrees: Float
        get() = (((id.hashCode() and 0x7fffffff) % 241) - 120) / 10f
}

/**
 * Current evaluation state of a [PassportStamp] including sticky unlock and seen state.
 */
data class StampState(
    val stamp: PassportStamp,
    val progress: Int,
    val goal: Int,
    val unlockedAt: Long?,
    val isSeen: Boolean = false,
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}

/**
 * Result of evaluating all passport milestone rules and updated observed active dates.
 */
data class PassportComputationResult(
    val stamps: List<StampState>,
    val activeDates: Set<LocalDate>,
)
