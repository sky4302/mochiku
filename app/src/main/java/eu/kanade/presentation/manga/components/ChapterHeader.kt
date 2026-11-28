package eu.kanade.presentation.manga.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.mochikuCardBorderStroke
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.SECONDARY_ALPHA
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ChapterHeader(
    enabled: Boolean,
    chapterCount: Int?,
    missingChapterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        // KMK <--
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        // KMK -->
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // KMK <--
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
        ) {
            Text(
                text = (
                    if (chapterCount == null) {
                        stringResource(MR.strings.chapters)
                    } else {
                        pluralStringResource(MR.plurals.manga_num_chapters, count = chapterCount, chapterCount)
                    }
                    ).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MissingChaptersWarning(missingChapterCount)
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = mochikuCardBorderStroke(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(MR.strings.action_filter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun MissingChaptersWarning(count: Int) {
    if (count == 0) {
        return
    }

    Text(
        text = pluralStringResource(MR.plurals.missing_chapters, count = count, count),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error.copy(alpha = SECONDARY_ALPHA),
    )
}
