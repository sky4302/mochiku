package eu.kanade.presentation.more.stats.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import kotlinx.coroutines.launch
import tachiyomi.domain.history.model.PassportStamp
import tachiyomi.domain.history.model.StampState
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val ShortStampDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

internal val PassportStamp.iconVector: ImageVector
    get() = when (this) {
        PassportStamp.FIRST_PAGE -> Icons.Outlined.Book
        PassportStamp.BOOKWORM_100 -> Icons.Outlined.AutoStories
        PassportStamp.BOOKWORM_1000 -> Icons.Outlined.CollectionsBookmark
        PassportStamp.STREAK_7 -> Icons.Outlined.LocalFireDepartment
        PassportStamp.STREAK_30 -> Icons.Outlined.Whatshot
        PassportStamp.NIGHT_OWL -> Icons.Outlined.DarkMode
        PassportStamp.MARATHON -> Icons.Outlined.Bolt
        PassportStamp.FINISHER -> Icons.Outlined.WorkspacePremium
    }

internal val PassportStamp.titleRes: StringResource
    get() = when (this) {
        PassportStamp.FIRST_PAGE -> KMR.strings.stamp_first_page_title
        PassportStamp.BOOKWORM_100 -> KMR.strings.stamp_bookworm_100_title
        PassportStamp.BOOKWORM_1000 -> KMR.strings.stamp_bookworm_1000_title
        PassportStamp.STREAK_7 -> KMR.strings.stamp_streak_7_title
        PassportStamp.STREAK_30 -> KMR.strings.stamp_streak_30_title
        PassportStamp.NIGHT_OWL -> KMR.strings.stamp_night_owl_title
        PassportStamp.MARATHON -> KMR.strings.stamp_marathon_title
        PassportStamp.FINISHER -> KMR.strings.stamp_finisher_title
    }

internal val PassportStamp.descriptionRes: StringResource
    get() = when (this) {
        PassportStamp.FIRST_PAGE -> KMR.strings.stamp_first_page_desc
        PassportStamp.BOOKWORM_100 -> KMR.strings.stamp_bookworm_100_desc
        PassportStamp.BOOKWORM_1000 -> KMR.strings.stamp_bookworm_1000_desc
        PassportStamp.STREAK_7 -> KMR.strings.stamp_streak_7_desc
        PassportStamp.STREAK_30 -> KMR.strings.stamp_streak_30_desc
        PassportStamp.NIGHT_OWL -> KMR.strings.stamp_night_owl_desc
        PassportStamp.MARATHON -> KMR.strings.stamp_marathon_desc
        PassportStamp.FINISHER -> KMR.strings.stamp_finisher_desc
    }

internal fun formatStampShortDate(epochMs: Long): String {
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(ShortStampDateFormatter)
}

internal fun formatStampLongDate(epochMs: Long): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}

/**
 * Displays the user's on-device milestone passport stamps in a 3-column ink grid with a detail sheet.
 */
@Composable
fun PassportStampsSection(
    stamps: List<StampState>,
    onShareStampsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (stamps.isEmpty()) return

    val colorScheme = MaterialTheme.colorScheme
    var selectedStamp by remember { mutableStateOf<StampState?>(null) }
    val earnedCount = remember(stamps) { stamps.count { it.isUnlocked } }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(KMR.strings.passport_stamps_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(KMR.strings.passport_stamps_subtitle, earnedCount, stamps.size),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                if (onShareStampsClick != null) {
                    IconButton(onClick = onShareStampsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(KMR.strings.action_share_recap),
                            tint = colorScheme.primary,
                        )
                    }
                }
            }

            // 3-column fixed grid inside LazyColumn
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stamps.chunked(3).forEach { rowStamps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowStamps.forEach { stampState ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                PassportStampBadge(
                                    state = stampState,
                                    size = 96.dp,
                                    onClick = { selectedStamp = stampState },
                                )
                            }
                        }
                        repeat(3 - rowStamps.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    val sheetStamp = selectedStamp
    if (sheetStamp != null) {
        PassportStampDetailSheet(
            state = sheetStamp,
            onDismissRequest = { selectedStamp = null },
        )
    }
}

/**
 * Reusable circular ink stamp badge usable inside grids, the Reader ID mini-row, story cards, and slam overlays.
 */
@Composable
fun PassportStampBadge(
    state: StampState,
    size: Dp = 96.dp,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val title = stringResource(state.stamp.titleRes)

    if (state.isUnlocked) {
        val inkColor = colorScheme.onSurface.copy(alpha = 0.85f)
        val innerRingColor = colorScheme.primary.copy(alpha = 0.55f)
        val fillColor = colorScheme.primaryContainer.copy(alpha = 0.24f)

        Box(
            modifier = modifier
                .size(size)
                .graphicsLayer { rotationZ = state.stamp.angleDegrees }
                .clip(CircleShape)
                .background(fillColor)
                .border(
                    width = if (compact) 1.5.dp else 2.dp,
                    color = inkColor,
                    shape = CircleShape,
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(if (compact) 4.dp else 6.dp)
                .border(
                    width = 1.dp,
                    color = innerRingColor,
                    shape = CircleShape,
                )
                .padding(if (compact) 4.dp else 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = state.stamp.iconVector,
                    contentDescription = title,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(if (compact) 16.dp else 22.dp),
                )
                if (!compact) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        lineHeight = 11.sp,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.unlockedAt?.let { unlockedMs ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = formatStampShortDate(unlockedMs),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    } else {
        val dashedBorderColor = colorScheme.outlineVariant
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .drawBehind {
                    val strokePx = 1.dp.toPx()
                    drawCircle(
                        color = dashedBorderColor,
                        radius = (this.size.minDimension - strokePx) / 2f,
                        style = Stroke(
                            width = strokePx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                        ),
                    )
                }
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "?",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${state.progress}/${state.goal}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PassportStampDetailSheet(
    state: StampState,
    onDismissRequest: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val title = stringResource(state.stamp.titleRes)
    val description = stringResource(state.stamp.descriptionRes)

    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PassportStampBadge(
                state = state,
                size = 112.dp,
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            val unlockedMs = state.unlockedAt
            if (unlockedMs != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer,
                ) {
                    Text(
                        text = stringResource(KMR.strings.passport_stamps_unlocked_on, formatStampLongDate(unlockedMs)),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            } else {
                val fraction = (state.progress.toFloat() / state.goal.coerceAtLeast(1)).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    )
                    Text(
                        text = stringResource(KMR.strings.passport_stamps_progress, state.progress, state.goal),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Full-screen celebratory stamp slam overlay displayed once per newly unlocked unseen milestone.
 */
@Composable
fun PassportStampSlamOverlay(
    state: StampState,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val title = stringResource(state.stamp.titleRes)
    val description = stringResource(state.stamp.descriptionRes)

    val scale = remember(state.stamp.id) { Animatable(2.2f) }
    val rotation = remember(state.stamp.id) { Animatable(-20f) }
    val alpha = remember(state.stamp.id) { Animatable(0f) }

    LaunchedEffect(state.stamp.id) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
            )
        }
        launch {
            rotation.animateTo(
                targetValue = state.stamp.angleDegrees,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
            )
        }
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180),
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onDismiss,
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        rotationZ = rotation.value - state.stamp.angleDegrees
                        this.alpha = alpha.value
                    },
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        PassportStampBadge(
                            state = state,
                            size = 152.dp,
                        )
                    }
                }

                Text(
                    text = stringResource(KMR.strings.passport_stamps_new_stamp, title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD0D0D8),
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(KMR.strings.passport_stamps_tap_to_dismiss),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF9C9CA8),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PassportStampsSectionPreview() {
    val sampleStamps = listOf(
        StampState(PassportStamp.FIRST_PAGE, 1, 1, unlockedAt = 1_794_300_000_000L, isSeen = true),
        StampState(PassportStamp.BOOKWORM_100, 100, 100, unlockedAt = 1_794_310_000_000L, isSeen = true),
        StampState(PassportStamp.BOOKWORM_1000, 248, 1000, unlockedAt = null),
        StampState(PassportStamp.STREAK_7, 7, 7, unlockedAt = 1_794_320_000_000L, isSeen = true),
        StampState(PassportStamp.STREAK_30, 11, 30, unlockedAt = null),
        StampState(PassportStamp.NIGHT_OWL, 8, 20, unlockedAt = null),
        StampState(PassportStamp.MARATHON, 19, 30, unlockedAt = null),
        StampState(PassportStamp.FINISHER, 0, 1, unlockedAt = null),
    )
    TachiyomiPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                PassportStampsSection(
                    stamps = sampleStamps,
                    onShareStampsClick = {},
                )
            }
        }
    }
}
