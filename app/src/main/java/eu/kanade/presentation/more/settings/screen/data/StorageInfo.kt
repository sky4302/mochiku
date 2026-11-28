package eu.kanade.presentation.more.settings.screen.data

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.MochikuPillCard
import eu.kanade.tachiyomi.util.storage.DiskUtil
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.header
import tachiyomi.presentation.core.util.secondaryItemAlpha
import java.io.File
import kotlin.math.roundToInt

@Composable
fun StorageInfo(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val storages = remember { DiskUtil.getExternalStorages(context) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
    ) {
        storages.forEach {
            StorageInfo(it)
        }
    }
}

@Composable
private fun StorageInfo(
    file: File,
) {
    val context = LocalContext.current

    val available = remember(file) { DiskUtil.getAvailableStorageSpace(file) }
    val availableText = remember(available) { Formatter.formatFileSize(context, available) }
    val total = remember(file) { DiskUtil.getTotalStorageSpace(file) }
    val totalText = remember(total) { Formatter.formatFileSize(context, total) }
    val usedFraction = if (total > 0L) (1f - (available / total.toFloat())).coerceIn(0f, 1f) else 0f
    val usedPercent = (usedFraction * 100f).roundToInt()

    MochikuPillCard(cornerRadius = 18.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = file.absolutePath,
                    style = MaterialTheme.typography.header,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$usedPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }

            LinearProgressIndicator(
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxWidth()
                    .height(10.dp),
                progress = { usedFraction },
            )

            Text(
                text = stringResource(MR.strings.available_disk_space_info, availableText, totalText),
                modifier = Modifier.secondaryItemAlpha(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
