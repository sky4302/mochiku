package eu.kanade.presentation.more.settings.widget

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
}
