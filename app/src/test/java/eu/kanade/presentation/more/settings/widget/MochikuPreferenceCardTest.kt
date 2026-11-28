package eu.kanade.presentation.more.settings.widget

import eu.kanade.presentation.components.resolveContiguousGroupCardPosition
import eu.kanade.presentation.components.resolveSectionCardPositions
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MochikuPreferenceCardTest {

    @Test
    fun `single enabled item in group resolves to Single`() {
        resolveGroupCardPosition(0, listOf(true)) shouldBe GroupCardPosition.Single
        resolveGroupCardPosition(1, listOf(false, true, false)) shouldBe GroupCardPosition.Single
        resolveGroupCardPosition(0, emptyList()) shouldBe GroupCardPosition.Single
    }

    @Test
    fun `multi-item group resolves Top, Middle, and Bottom`() {
        val flags = listOf(true, true, true, true)
        resolveGroupCardPosition(0, flags) shouldBe GroupCardPosition.Top
        resolveGroupCardPosition(1, flags) shouldBe GroupCardPosition.Middle
        resolveGroupCardPosition(2, flags) shouldBe GroupCardPosition.Middle
        resolveGroupCardPosition(3, flags) shouldBe GroupCardPosition.Bottom
    }

    @Test
    fun `disabled head or tail shifts visual Top and Bottom corners cleanly`() {
        val flags = listOf(false, true, true, false, true, false)
        resolveGroupCardPosition(1, flags) shouldBe GroupCardPosition.Top
        resolveGroupCardPosition(2, flags) shouldBe GroupCardPosition.Middle
        resolveGroupCardPosition(4, flags) shouldBe GroupCardPosition.Bottom
    }

    @Test
    fun `resolveContiguousGroupCardPosition handles neighbor combinations`() {
        resolveContiguousGroupCardPosition(hasPreviousInGroup = false, hasNextInGroup = false) shouldBe GroupCardPosition.Single
        resolveContiguousGroupCardPosition(hasPreviousInGroup = false, hasNextInGroup = true) shouldBe GroupCardPosition.Top
        resolveContiguousGroupCardPosition(hasPreviousInGroup = true, hasNextInGroup = true) shouldBe GroupCardPosition.Middle
        resolveContiguousGroupCardPosition(hasPreviousInGroup = true, hasNextInGroup = false) shouldBe GroupCardPosition.Bottom
    }

    @Test
    fun `resolveSectionCardPositions resets across headers and skips collapsed rows`() {
        data class RowItem(val isHeader: Boolean, val visible: Boolean = true)

        val rows = listOf(
            RowItem(isHeader = true),
            RowItem(isHeader = false, visible = true),
            RowItem(isHeader = false, visible = false),
            RowItem(isHeader = false, visible = true),
            RowItem(isHeader = true),
            RowItem(isHeader = false, visible = true),
            RowItem(isHeader = false, visible = false),
        )

        resolveSectionCardPositions(
            items = rows,
            isHeader = { it.isHeader },
            isVisible = { it.visible },
        ) shouldBe listOf(
            null,
            GroupCardPosition.Top,
            null,
            GroupCardPosition.Bottom,
            null,
            GroupCardPosition.Single,
            null,
        )
    }
}
