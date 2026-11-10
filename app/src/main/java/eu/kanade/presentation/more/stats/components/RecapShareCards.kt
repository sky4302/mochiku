package eu.kanade.presentation.more.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.more.profile.ID_CARD_ASPECT
import eu.kanade.presentation.more.profile.MochikuIdCardBackContent
import eu.kanade.presentation.more.profile.MochikuIdCardFrontContent

data class RecapShareMangaRow(
    val title: String,
    val durationFormatted: String,
    val chaptersRead: Int,
)

data class RecapShareData(
    val monthLabel: String,
    val yearMonthCode: String,
    val username: String,
    val photoPath: String?,
    val cardNumber: String,
    val joinedDate: String,
    val chaptersRead: Int,
    val durationFormatted: String,
    val libraryCount: Int,
    val topMangaList: List<RecapShareMangaRow>,
)

private val StoryCardColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121214),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E22),
    onSurfaceVariant = Color(0xFFAEAEB8),
    surfaceContainerLowest = Color(0xFF121215),
    surfaceContainer = Color(0xFF18181C),
    surfaceContainerHigh = Color(0xFF202026),
    surfaceContainerHighest = Color(0xFF282830),
    outlineVariant = Color(0xFF383842),
)

private fun RecapShareData.sanitized(hideDetails: Boolean): RecapShareData {
    if (!hideDetails) return this
    return copy(
        username = "Reader",
        photoPath = null,
        cardNumber = "••••••••••",
        topMangaList = topMangaList.mapIndexed { index, row ->
            row.copy(title = "Series ${index + 1}")
        },
    )
}

@Composable
fun ReceiptShareCard(
    data: RecapShareData,
    hideDetails: Boolean,
    modifier: Modifier = Modifier,
) {
    val effective = data.sanitized(hideDetails)
    StoryCardShell(
        monthLabel = effective.monthLabel,
        modifier = modifier,
    ) {
        MochikuThermalReceiptPaper(
            username = effective.username,
            cardNumberSuffix = if (hideDetails) "••••" else effective.cardNumber.takeLast(4),
            topMangaList = effective.topMangaList,
            chaptersRead = effective.chaptersRead,
            durationFormatted = effective.durationFormatted,
            modifier = Modifier.fillMaxWidth(0.92f),
        )
    }
}

@Composable
fun ReaderIdShareCard(
    data: RecapShareData,
    hideDetails: Boolean,
    modifier: Modifier = Modifier,
    onImageReadyChanged: (Boolean) -> Unit = {},
) {
    val effective = data.sanitized(hideDetails)
    val avatarPath = effective.photoPath
    var avatarLoaded by remember(avatarPath) { mutableStateOf(avatarPath == null) }

    LaunchedEffect(avatarPath, avatarLoaded) {
        onImageReadyChanged(avatarLoaded)
    }

    StoryCardShell(
        monthLabel = effective.monthLabel,
        modifier = modifier,
    ) {
        MaterialTheme(colorScheme = StoryCardColorScheme) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ID_CARD_ASPECT)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(22.dp),
                        ),
                ) {
                    MochikuIdCardFrontContent(
                        username = effective.username,
                        photoPath = avatarPath,
                        cardNumber = effective.cardNumber,
                        joinedDate = effective.joinedDate,
                        onEditProfileClick = null,
                        hintText = "MOCHIKU PASSPORT",
                        disableHardwareBitmap = true,
                        onAvatarSettled = { avatarLoaded = true },
                    )
                }

                StorySummaryStrip(
                    chaptersRead = effective.chaptersRead,
                    durationFormatted = effective.durationFormatted,
                    libraryCount = effective.libraryCount,
                )
            }
        }
    }
}

@Composable
fun BarcodeShareCard(
    data: RecapShareData,
    hideDetails: Boolean,
    modifier: Modifier = Modifier,
) {
    val effective = data.sanitized(hideDetails)
    val barcodePayload = if (hideDetails) {
        "MKU-${effective.yearMonthCode}-MASKED"
    } else {
        "MFY${effective.yearMonthCode} ${effective.cardNumber}"
    }
    val barcodeLabel = if (hideDetails) {
        "MKU-${effective.yearMonthCode}-••••"
    } else {
        "MFY ${effective.yearMonthCode}  ${effective.cardNumber.takeLast(8).chunked(4).joinToString(" ")}"
    }

    StoryCardShell(
        monthLabel = effective.monthLabel,
        modifier = modifier,
    ) {
        MaterialTheme(colorScheme = StoryCardColorScheme) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ID_CARD_ASPECT)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(22.dp),
                        ),
                ) {
                    MochikuIdCardBackContent(
                        cardNumber = effective.cardNumber,
                        username = effective.username,
                        isScanning = false,
                        isVerified = false,
                        barcodePayload = barcodePayload,
                        barcodeLabel = barcodeLabel,
                        statusText = "${effective.chaptersRead} CHAPTERS VERIFIED",
                    )
                }

                StorySummaryStrip(
                    chaptersRead = effective.chaptersRead,
                    durationFormatted = effective.durationFormatted,
                    libraryCount = effective.libraryCount,
                )
            }
        }
    }
}

@Composable
private fun StoryCardShell(
    monthLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = monthLabel.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = Color(0xFF8E8E8E),
        )

        Spacer(Modifier.weight(1f))
        content()
        Spacer(Modifier.weight(1f))

        Text(
            text = "餅ク Mochiku",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF8E8E8E),
        )
    }
}

@Composable
private fun StorySummaryStrip(
    chaptersRead: Int,
    durationFormatted: String,
    libraryCount: Int,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF121214),
        border = BorderStroke(1.dp, Color(0xFF222226)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StoryStatMetric(value = durationFormatted, label = "TIME")
            StoryStatMetric(value = "$chaptersRead", label = "CHAPTERS")
            StoryStatMetric(value = "$libraryCount", label = "TITLES")
        }
    }
}

@Composable
private fun StoryStatMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
        )
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = Color(0xFF8E8E8E),
        )
    }
}

/** Shared vintage serrated thermal receipt card reused by both the Stats dialog and 9:16 Share Card. */
@Composable
internal fun MochikuThermalReceiptPaper(
    username: String,
    cardNumberSuffix: String,
    topMangaList: List<RecapShareMangaRow>,
    chaptersRead: Int,
    durationFormatted: String,
    modifier: Modifier = Modifier,
    footerContent: @Composable ColumnScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .drawBehind {
                val teethCount = 14
                val teethHeight = 12.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height - teethHeight)
                    val toothWidth = size.width / teethCount
                    for (i in teethCount downTo 1) {
                        val midX = (i - 0.5f) * toothWidth
                        val startX = (i - 1) * toothWidth
                        lineTo(midX, size.height)
                        lineTo(startX, size.height - teethHeight)
                    }
                    close()
                }
                drawPath(path, color = Color(0xFFF6F6F4))
                drawPath(path, color = Color(0x2B000000), style = Stroke(width = 1.dp.toPx()))
            }
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "MOCHIKU MANGA MART",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color.Black,
            )
            Text(
                text = "餅ク · FRESH STORIES DAILY",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF555555),
            )
            Text(
                text = "--------------------------------------",
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF888888),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("READER: $username", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                Text("TXN: #$cardNumberSuffix", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
            }
            Text(
                text = "--------------------------------------",
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF888888),
            )

            if (topMangaList.isNotEmpty()) {
                topMangaList.take(8).forEachIndexed { i, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${i + 1}x ${item.title}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = Color(0xFF222222),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "- ${item.durationFormatted}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF444444),
                        )
                    }
                }
            } else {
                Text(
                    text = "No reading history recorded this month.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF666666),
                )
            }

            Text(
                text = "--------------------------------------",
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF888888),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL CHAPTERS:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                Text("$chaptersRead", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("READ DURATION:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                Text(durationFormatted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Text(
                text = "--------------------------------------",
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF888888),
            )
            Text(
                text = "END OF LINE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Color.Black,
            )
            Text(
                text = "||||||| | ||||| |||| | ||||||||",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )

            footerContent()
        }
    }
}

private val SampleShareData = RecapShareData(
    monthLabel = "November 2026",
    yearMonthCode = "2026-11",
    username = "Fify",
    photoPath = null,
    cardNumber = "61280033952",
    joinedDate = "10 Nov 2026",
    chaptersRead = 42,
    durationFormatted = "6h 18m",
    libraryCount = 128,
    topMangaList = listOf(
        RecapShareMangaRow("One Piece", "2h 10m", 15),
        RecapShareMangaRow("Sakamoto Days", "1h 45m", 12),
        RecapShareMangaRow("Kagurabachi", "1h 12m", 9),
    ),
)

@Preview(widthDp = 360)
@Composable
private fun ReceiptShareCardPreview() {
    ReceiptShareCard(data = SampleShareData, hideDetails = false)
}

@Preview(widthDp = 360)
@Composable
private fun ReaderIdShareCardPreview() {
    ReaderIdShareCard(data = SampleShareData, hideDetails = false)
}

@Preview(widthDp = 360)
@Composable
private fun BarcodeShareCardPreview() {
    BarcodeShareCard(data = SampleShareData, hideDetails = true)
}
