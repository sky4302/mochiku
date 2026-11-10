package eu.kanade.presentation.more.stats.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.kanade.tachiyomi.util.storage.getUriCompat
import eu.kanade.tachiyomi.util.system.toShareIntent
import eu.kanade.tachiyomi.util.system.toast
import exh.log.xLogE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

private const val PAGE_RECEIPT = 0
private const val PAGE_READER_ID = 1
private const val PAGE_BARCODE = 2
private const val PAGE_COUNT = 3

@Composable
fun RecapSharePreviewDialog(
    data: RecapShareData,
    initialPage: Int = PAGE_RECEIPT,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, PAGE_COUNT - 1),
        pageCount = { PAGE_COUNT },
    )

    var hideDetails by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isReaderIdImageReady by remember(data.photoPath) { mutableStateOf(data.photoPath == null) }

    val receiptLayer = rememberGraphicsLayer()
    val readerIdLayer = rememberGraphicsLayer()
    val barcodeLayer = rememberGraphicsLayer()
    val layers = remember(receiptLayer, readerIdLayer, barcodeLayer) {
        listOf(receiptLayer, readerIdLayer, barcodeLayer)
    }

    val isCurrentPageReady = when (pagerState.currentPage) {
        PAGE_READER_ID -> hideDetails || isReaderIdImageReady
        else -> true
    }

    val currentPageLabel = when (pagerState.currentPage) {
        PAGE_RECEIPT -> stringResource(KMR.strings.recap_share_card_receipt)
        PAGE_READER_ID -> stringResource(KMR.strings.recap_share_card_id)
        else -> stringResource(KMR.strings.recap_share_card_barcode)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background,
            contentColor = colors.onBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(MR.strings.action_close),
                            )
                        }
                        Text(
                            text = stringResource(KMR.strings.recap_share_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = currentPageLabel,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 9:16 Story Card Pager
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 2,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) { page ->
                    val layer = layers[page]
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        val fittedWidth = min(maxWidth, maxHeight * (9f / 16f))
                        Box(
                            modifier = Modifier
                                .width(fittedWidth)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    1.dp,
                                    colors.outlineVariant.copy(alpha = 0.45f),
                                    RoundedCornerShape(18.dp),
                                ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawWithContent {
                                        layer.record {
                                            this@drawWithContent.drawContent()
                                        }
                                        drawLayer(layer)
                                    },
                            ) {
                                when (page) {
                                    PAGE_RECEIPT -> ReceiptShareCard(
                                        data = data,
                                        hideDetails = hideDetails,
                                    )
                                    PAGE_READER_ID -> ReaderIdShareCard(
                                        data = data,
                                        hideDetails = hideDetails,
                                        onImageReadyChanged = { isReaderIdImageReady = it },
                                    )
                                    else -> BarcodeShareCard(
                                        data = data,
                                        hideDetails = hideDetails,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(PAGE_COUNT) { idx ->
                        val active = idx == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (active) 9.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) colors.primary else colors.outlineVariant,
                                ),
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Privacy Switch Row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(KMR.strings.recap_share_hide_details),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                        )
                        Switch(
                            checked = hideDetails,
                            onCheckedChange = { hideDetails = it },
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = stringResource(MR.strings.action_close),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }

                    Button(
                        onClick = {
                            if (isExporting || !isCurrentPageReady) return@Button
                            isExporting = true
                            scope.launch {
                                try {
                                    exportAndShareLayer(context, layers[pagerState.currentPage])
                                } catch (e: Exception) {
                                    xLogE("Failed to share recap card", e)
                                    context.toast(KMR.strings.recap_share_failed)
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        enabled = isCurrentPageReady && !isExporting,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.onPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (!isCurrentPageReady) {
                                stringResource(KMR.strings.recap_share_loading_images)
                            } else {
                                stringResource(KMR.strings.action_share_recap)
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}

private suspend fun exportAndShareLayer(context: Context, layer: GraphicsLayer) {
    val bitmap = layer.toImageBitmap().asAndroidBitmap()
    val uri = withContext(Dispatchers.IO) {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
        shareDir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoff) {
                file.delete()
            }
        }
        val outFile = File(shareDir, "recap-${System.currentTimeMillis()}.png")
        FileOutputStream(outFile).use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                "Bitmap.compress returned false"
            }
        }
        outFile.getUriCompat(context)
    }
    context.startActivity(uri.toShareIntent(context, type = "image/png"))
}
