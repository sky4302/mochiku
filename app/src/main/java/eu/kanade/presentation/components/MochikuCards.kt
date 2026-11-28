package eu.kanade.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GroupCardPosition {
    Single,
    Top,
    Middle,
    Bottom,
}

/** Resolves perimeter card corners for an item inside a preference group, accounting for disabled rows. */
fun resolveGroupCardPosition(
    index: Int,
    enabledFlags: List<Boolean>,
): GroupCardPosition {
    val firstEnabled = enabledFlags.indexOfFirst { it }
    val lastEnabled = enabledFlags.indexOfLast { it }
    return when {
        firstEnabled == -1 || firstEnabled == lastEnabled -> GroupCardPosition.Single
        index <= firstEnabled -> GroupCardPosition.Top
        index >= lastEnabled -> GroupCardPosition.Bottom
        else -> GroupCardPosition.Middle
    }
}

/** Resolves perimeter card corners for any contiguous section item given whether neighbors share the same card group. */
fun resolveContiguousGroupCardPosition(
    hasPreviousInGroup: Boolean,
    hasNextInGroup: Boolean,
): GroupCardPosition = when {
    !hasPreviousInGroup && !hasNextInGroup -> GroupCardPosition.Single
    !hasPreviousInGroup -> GroupCardPosition.Top
    !hasNextInGroup -> GroupCardPosition.Bottom
    else -> GroupCardPosition.Middle
}

/**
 * Computes per-index [GroupCardPosition] for a list containing section headers and optional collapsed/hidden items.
 * Headers and invisible items map to `null`; consecutive visible non-header items share a continuous card perimeter.
 */
fun <T> resolveSectionCardPositions(
    items: List<T>,
    isHeader: (T) -> Boolean,
    isVisible: (T) -> Boolean = { true },
): List<GroupCardPosition?> {
    val result = MutableList<GroupCardPosition?>(items.size) { null }
    val activeIndices = ArrayList<Int>()
    fun flushGroup() {
        for (k in activeIndices.indices) {
            result[activeIndices[k]] = resolveContiguousGroupCardPosition(
                hasPreviousInGroup = k > 0,
                hasNextInGroup = k < activeIndices.lastIndex,
            )
        }
        activeIndices.clear()
    }
    for (i in items.indices) {
        val item = items[i]
        when {
            isHeader(item) -> flushGroup()
            isVisible(item) -> activeIndices.add(i)
        }
    }
    flushGroup()
    return result
}

@Composable
fun mochikuFilterChipColors(): SelectableChipColors {
    val colorScheme = MaterialTheme.colorScheme
    return FilterChipDefaults.filterChipColors(
        containerColor = colorScheme.surfaceContainerLow,
        labelColor = colorScheme.onSurfaceVariant,
        iconColor = colorScheme.onSurfaceVariant,
        selectedContainerColor = colorScheme.primary,
        selectedLabelColor = colorScheme.onPrimary,
        selectedLeadingIconColor = colorScheme.onPrimary,
        selectedTrailingIconColor = colorScheme.onPrimary,
    )
}

@Composable
fun mochikuCardBorderStroke(): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

@Composable
fun MochikuCircleIconBadge(
    icon: Any?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    badgeSize: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    if (icon == null) return
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        when (icon) {
            is ImageVector -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
            is Painter -> Icon(
                painter = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
fun MochikuPillCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 22.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = mochikuCardBorderStroke()
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            border = border,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            border = border,
            content = content,
        )
    }
}

@Composable
fun Modifier.mochikuGroupedCardContainer(
    position: GroupCardPosition,
    cornerRadius: Dp = 20.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
): Modifier {
    val shape = when (position) {
        GroupCardPosition.Single -> RoundedCornerShape(cornerRadius)
        GroupCardPosition.Top -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        GroupCardPosition.Middle -> RectangleShape
        GroupCardPosition.Bottom -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
    }

    return this
        .clip(shape)
        .background(containerColor)
        .drawWithCache {
            val r = cornerRadius.toPx().coerceAtMost(minOf(size.width, size.height) / 2f)
            val strokePx = 1.dp.toPx()
            val half = strokePx / 2f
            val left = half
            val top = half
            val right = size.width - half
            val bottom = size.height - half

            val perimeterPath = when (position) {
                GroupCardPosition.Single, GroupCardPosition.Middle -> null
                GroupCardPosition.Top -> Path().apply {
                    moveTo(left, size.height)
                    lineTo(left, top + r)
                    arcTo(Rect(left, top, left + 2f * r, top + 2f * r), 180f, 90f, false)
                    lineTo(right - r, top)
                    arcTo(Rect(right - 2f * r, top, right, top + 2f * r), 270f, 90f, false)
                    lineTo(right, size.height)
                }
                GroupCardPosition.Bottom -> Path().apply {
                    moveTo(left, 0f)
                    lineTo(left, bottom - r)
                    arcTo(Rect(left, bottom - 2f * r, left + 2f * r, bottom), 180f, -90f, false)
                    lineTo(right - r, bottom)
                    arcTo(Rect(right - 2f * r, bottom - 2f * r, right, bottom), 90f, -90f, false)
                    lineTo(right, 0f)
                }
            }

            onDrawWithContent {
                drawContent()
                when (position) {
                    GroupCardPosition.Single -> {
                        drawRoundRect(
                            color = borderColor,
                            topLeft = Offset(left, top),
                            size = Size(size.width - strokePx, size.height - strokePx),
                            cornerRadius = CornerRadius(r, r),
                            style = Stroke(width = strokePx),
                        )
                    }
                    GroupCardPosition.Middle -> {
                        drawLine(borderColor, Offset(left, 0f), Offset(left, size.height), strokeWidth = strokePx)
                        drawLine(borderColor, Offset(right, 0f), Offset(right, size.height), strokeWidth = strokePx)
                    }
                    GroupCardPosition.Top, GroupCardPosition.Bottom -> {
                        perimeterPath?.let { drawPath(it, color = borderColor, style = Stroke(width = strokePx)) }
                    }
                }
            }
        }
}

/** Shared capsule segmented tab strip used in sheet dialogs and top-level tabbed views. */
@Composable
fun MochikuCapsuleTabRow(
    tabTitles: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badgeCounts: List<Int?>? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colorScheme.surfaceContainerLow,
        border = mochikuCardBorderStroke(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabTitles.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                val bg by animateColorAsState(
                    targetValue = if (selected) colorScheme.primary else Color.Transparent,
                    label = "capsuleTabBg",
                )
                val fg by animateColorAsState(
                    targetValue = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                    label = "capsuleTabFg",
                )
                val badge = badgeCounts?.getOrNull(index)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(bg)
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = fg,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (badge != null) {
                        Text(
                            text = badge.toString(),
                            color = if (selected) colorScheme.primary else colorScheme.onSurface,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selected) colorScheme.onPrimary else colorScheme.surfaceContainerHigh,
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MochikuSectionHeaderText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
