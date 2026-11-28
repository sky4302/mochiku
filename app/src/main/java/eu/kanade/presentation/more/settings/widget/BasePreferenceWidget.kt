package eu.kanade.presentation.more.settings.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.more.settings.LocalPreferenceHighlighted
import eu.kanade.presentation.more.settings.LocalPreferenceMinHeight
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import eu.kanade.presentation.components.GroupCardPosition as SharedGroupCardPosition
import eu.kanade.presentation.components.MochikuCircleIconBadge as SharedMochikuCircleIconBadge
import eu.kanade.presentation.components.MochikuPillCard as SharedMochikuPillCard
import eu.kanade.presentation.components.mochikuGroupedCardContainer as sharedMochikuGroupedCardContainer
import eu.kanade.presentation.components.resolveGroupCardPosition as resolveSharedGroupCardPosition

@Composable
internal fun BasePreferenceWidget(
    modifier: Modifier = Modifier,
    title: String? = null,
    subcomponent: @Composable (ColumnScope.() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    widget: @Composable (() -> Unit)? = null,
) {
    val highlighted = LocalPreferenceHighlighted.current
    val minHeight = LocalPreferenceMinHeight.current
    Row(
        modifier = modifier
            .highlightBackground(highlighted)
            .sizeIn(minHeight = minHeight)
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.padding(start = PrefsHorizontalPadding, end = 8.dp),
                content = { icon() },
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = PrefsVerticalPadding),
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    modifier = Modifier.padding(horizontal = PrefsHorizontalPadding),
                    text = title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = TitleFontSize,
                )
            }
            subcomponent?.invoke(this)
        }
        if (widget != null) {
            Box(
                modifier = Modifier.padding(end = PrefsHorizontalPadding),
                content = { widget() },
            )
        }
    }
}

@Composable
internal fun Modifier.highlightBackground(highlighted: Boolean): Modifier {
    var highlightFlag by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (highlighted) {
            highlightFlag = true
            delay(3.seconds)
            highlightFlag = false
        }
    }
    val highlight by animateColorAsState(
        targetValue = if (highlightFlag) {
            MaterialTheme.colorScheme.surfaceTint.copy(alpha = .12f)
        } else {
            Color.Transparent
        },
        animationSpec = if (highlightFlag) {
            repeatable(
                iterations = 5,
                animation = tween(durationMillis = 200),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(
                    offsetMillis = 600,
                    offsetType = StartOffsetType.Delay,
                ),
            )
        } else {
            tween(200)
        },
        label = "highlight",
    )
    return this.background(color = highlight)
}

internal val TrailingWidgetBuffer = 16.dp
internal val PrefsHorizontalPadding = 16.dp
internal val PrefsVerticalPadding = 16.dp
internal val TitleFontSize = 16.sp

internal typealias GroupCardPosition = SharedGroupCardPosition

/** Resolves perimeter card corners for an item inside a preference group, accounting for disabled rows. */
internal fun resolveGroupCardPosition(
    index: Int,
    enabledFlags: List<Boolean>,
): GroupCardPosition = resolveSharedGroupCardPosition(index = index, enabledFlags = enabledFlags)

@Composable
internal fun MochikuCircleIconBadge(
    icon: Any?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    badgeSize: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) = SharedMochikuCircleIconBadge(
    icon = icon,
    modifier = modifier,
    tint = tint,
    badgeSize = badgeSize,
    iconSize = iconSize,
)

@Composable
internal fun MochikuPillCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 22.dp,
    content: @Composable () -> Unit,
) = SharedMochikuPillCard(
    modifier = modifier,
    onClick = onClick,
    cornerRadius = cornerRadius,
    content = content,
)

@Composable
internal fun MochikuPillPreferenceRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: CharSequence? = null,
    icon: Any? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    widget: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    MochikuPillCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 60.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon != null) {
                MochikuCircleIconBadge(
                    icon = icon,
                    tint = iconTint,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    if (subtitle is AnnotatedString) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Text(
                            text = subtitle.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (widget != null) {
                widget()
            }
        }
    }
}

@Composable
internal fun Modifier.mochikuGroupedCardContainer(
    position: GroupCardPosition,
    cornerRadius: Dp = 20.dp,
): Modifier = sharedMochikuGroupedCardContainer(
    position = position,
    cornerRadius = cornerRadius,
)
