package eu.kanade.tachiyomi.ui.home

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.ui.UiPreferences
import exh.log.xLogE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import tachiyomi.domain.history.interactor.GetContinueReading
import tachiyomi.domain.history.model.ContinueReadingItem
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Immutable
sealed interface ContinueReadingBarState {
    data object Hidden : ContinueReadingBarState
    data class Visible(val item: ContinueReadingItem) : ContinueReadingBarState
}

/**
 * Tracks the latest reading target for [HomeScreen]'s floating continue-reading bar and handles
 * session-scoped swipe dismissal that automatically re-arms on any newer reading timestamp.
 */
class ContinueReadingBarScreenModel(
    private val getContinueReading: GetContinueReading = Injekt.get(),
    private val uiPreferences: UiPreferences = Injekt.get(),
) : StateScreenModel<ContinueReadingBarState>(ContinueReadingBarState.Hidden) {

    private val dismissedAt = MutableStateFlow<Long?>(null)

    init {
        screenModelScope.launch {
            combine(
                uiPreferences.showContinueReadingBar().changes(),
                getContinueReading.subscribe(),
                dismissedAt,
            ) { enabled, item, dismissedReadAt ->
                when {
                    !enabled || item == null -> ContinueReadingBarState.Hidden
                    dismissedReadAt != null && item.lastReadAt <= dismissedReadAt -> ContinueReadingBarState.Hidden
                    else -> ContinueReadingBarState.Visible(item)
                }
            }
                .catch { error ->
                    xLogE("Failed to observe continue-reading bar state", error)
                    emit(ContinueReadingBarState.Hidden)
                }
                .distinctUntilChanged()
                .collect { nextState ->
                    mutableState.value = nextState
                }
        }
    }

    fun dismiss() {
        val visibleState = state.value as? ContinueReadingBarState.Visible ?: return
        dismissedAt.value = visibleState.item.lastReadAt
    }
}
