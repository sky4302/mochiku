package eu.kanade.presentation.more.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val ReceiptPaperColor = Color(0xFFF5F2EB)
private val ReceiptInkPrimary = Color(0xFF141414)
private val ReceiptInkSecondary = Color(0xFF4A4A4A)
private val ReceiptDividerColor = Color(0xFF888888)
private val ReceiptTimestampFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

/**
 * Vertically scrollable dual-section thermal paper receipt ("Let's see your bill") with serrated
 * top and bottom edges and an explicit continue button so scrolling never accidentally advances.
 */
@Composable
internal fun RecapThermalReceiptSlide(
    username: String,
    cardNumber: String,
    data: MochikuRecapData,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val receiptSlideY = remember { Animatable(-80f) }
    val receiptAlpha = remember { Animatable(0f) }
    val printedAt = remember { LocalDateTime.now().format(ReceiptTimestampFormatter) }

    LaunchedEffect(Unit) {
        receiptSlideY.animateTo(0f, tween(durationMillis = 600, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        receiptAlpha.animateTo(1f, tween(durationMillis = 380))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "Let's see your bill",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground,
            )
        }

        // Scrollable serrated thermal paper container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .weight(1f, fill = false)
                .heightIn(max = 470.dp)
                .graphicsLayer {
                    translationY = receiptSlideY.value
                    alpha = receiptAlpha.value
                }
                .drawBehind {
                    val teethCount = 16
                    val teethHeight = 10.dp.toPx()
                    val toothWidth = size.width / teethCount
                    val path = Path().apply {
                        moveTo(0f, teethHeight)
                        for (i in 0 until teethCount) {
                            lineTo((i + 0.5f) * toothWidth, 0f)
                            lineTo((i + 1) * toothWidth, teethHeight)
                        }
                        lineTo(size.width, size.height - teethHeight)
                        for (i in teethCount downTo 1) {
                            lineTo((i - 0.5f) * toothWidth, size.height)
                            lineTo((i - 1) * toothWidth, size.height - teethHeight)
                        }
                        close()
                    }
                    drawPath(path, color = ReceiptPaperColor)
                    drawPath(
                        path,
                        color = Color(0x2B000000),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
                .padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "MOCHIKU",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp,
                    color = ReceiptInkPrimary,
                )
                Text(
                    text = "${data.monthName} recap · $username (#${cardNumber.takeLast(4)})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ReceiptInkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "printed at $printedAt",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = ReceiptInkSecondary,
                )

                ReceiptSeparator()

                if (data.topAuthorList.isNotEmpty()) {
                    ReceiptSectionHeader("Top Authors")
                    data.topAuthorList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3DFD6))
                                    .border(0.5.dp, Color(0x44000000), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = item.author.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ReceiptInkPrimary,
                                )
                            }
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = "${item.chaptersRead}x ${item.author}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ReceiptInkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "~ ${item.durationFormatted}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ReceiptInkSecondary,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }

                ReceiptSectionHeader("Top Manga")
                if (data.topMangaList.isNotEmpty()) {
                    data.topMangaList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE3DFD6))
                                    .border(0.5.dp, Color(0x44000000), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (item.cover != null) {
                                    val req = remember(item.cover) {
                                        ImageRequest.Builder(context).data(item.cover).size(64).build()
                                    }
                                    AsyncImage(
                                        model = req,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Book,
                                        contentDescription = null,
                                        tint = ReceiptInkSecondary,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = "${item.chaptersRead}x ${item.title}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ReceiptInkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "~ ${item.durationFormatted}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ReceiptInkSecondary,
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No reading history recorded this month.",
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ReceiptInkSecondary,
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }

                ReceiptSeparator()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Total : ${data.monthDurationFormatted} playtime",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReceiptInkPrimary,
                    )
                    Text(
                        text = "${data.monthChaptersRead} chapters · ${data.libraryCount} titles in library",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = ReceiptInkSecondary,
                    )
                }

                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, ReceiptDividerColor), RoundedCornerShape(4.dp))
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "END OF LINE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ReceiptInkPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.surfaceContainer,
                contentColor = colorScheme.onSurface,
            ),
            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.55f)),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
        ) {
            Text(
                text = "TAP TO CONTINUE",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun ReceiptSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = ReceiptInkPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
    )
}

@Composable
private fun ReceiptSeparator() {
    Text(
        text = "--------------------------------------",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = ReceiptDividerColor,
        maxLines = 1,
    )
}
