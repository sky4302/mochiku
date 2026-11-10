package eu.kanade.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.util.formatChapterNumber
import eu.kanade.tachiyomi.ui.home.ContinueReadingBarState
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.history.model.ContinueReadingItem
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.domain.manga.model.MangaCover as DomainMangaCover

/**
 * Floating capsule bar positioned above the bottom navigation pill to jump directly back into the
 * most recently read series or swipe away for the current reading session.
 */
@Composable
fun ContinueReadingBar(
    state: ContinueReadingBarState,
    visible: Boolean,
    onBarClick: (ContinueReadingItem) -> Unit,
    onResumeClick: (ContinueReadingItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lastItem by remember { mutableStateOf<ContinueReadingItem?>(null) }
    if (state is ContinueReadingBarState.Visible) {
        lastItem = state.item
    }

    val shouldShow = visible && state is ContinueReadingBarState.Visible && lastItem != null

    AnimatedVisibility(
        visible = shouldShow,
        enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        modifier = modifier,
    ) {
        val item = lastItem ?: return@AnimatedVisibility
        key(item.mangaId, item.chapter.id, item.lastReadAt) {
            ContinueReadingCapsule(
                item = item,
                onBarClick = { onBarClick(item) },
                onResumeClick = { onResumeClick(item) },
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun ContinueReadingCapsule(
    item: ContinueReadingItem,
    onBarClick: () -> Unit,
    onResumeClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            if (target == SwipeToDismissBoxValue.StartToEnd || target == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDismiss()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.25f },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp),
    ) {
        ContinueReadingCapsuleSurface(
            item = item,
            onBarClick = onBarClick,
            onResumeClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onResumeClick()
            },
        )
    }
}

@Composable
private fun ContinueReadingCapsuleSurface(
    item: ContinueReadingItem,
    onBarClick: () -> Unit,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val pageNumber = (item.chapter.lastPageRead + 1).coerceAtLeast(1L)
    val subtitle = if (item.chapter.isRecognizedNumber) {
        stringResource(
            KMR.strings.continue_reading_chapter_page,
            formatChapterNumber(item.chapter.chapterNumber),
            pageNumber,
        )
    } else {
        stringResource(
            KMR.strings.continue_reading_chapter_name_page,
            item.chapter.name,
            pageNumber,
        )
    }

    Surface(
        onClick = onBarClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(50),
                spotColor = colorScheme.scrim.copy(alpha = 0.22f),
                ambientColor = colorScheme.scrim.copy(alpha = 0.14f),
            ),
        shape = RoundedCornerShape(50),
        color = colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MangaCover.Square(
                data = item.coverData,
                shape = CircleShape,
                modifier = Modifier.size(36.dp),
                size = MangaCover.Size.Normal,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
            ) {
                Text(
                    text = item.mangaTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                onClick = onResumeClick,
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(KMR.strings.action_resume_reading),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ContinueReadingBarPreview() {
    TachiyomiPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(24.dp)) {
                ContinueReadingCapsuleSurface(
                    item = ContinueReadingItem(
                        mangaId = 1L,
                        mangaTitle = "Chainsaw Man",
                        coverData = DomainMangaCover(
                            mangaId = 1L,
                            sourceId = 1L,
                            isMangaFavorite = true,
                            ogUrl = null,
                            lastModified = 0L,
                        ),
                        chapter = Chapter.create().copy(
                            id = 10L,
                            mangaId = 1L,
                            chapterNumber = 142.0,
                            lastPageRead = 14L,
                        ),
                        lastReadAt = 1_000L,
                    ),
                    onBarClick = {},
                    onResumeClick = {},
                )
            }
        }
    }
}
