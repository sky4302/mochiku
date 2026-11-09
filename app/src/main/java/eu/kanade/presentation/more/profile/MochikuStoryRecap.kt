package eu.kanade.presentation.more.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MochikuStoryRecapDialog(
    username: String,
    photoPath: String?,
    cardNumber: String,
    joinedDate: String,
    readDuration: String,
    totalChaptersRead: Int,
    libraryCount: Int,
    onDismissRequest: () -> Unit,
) {
    var currentSlide by remember { mutableIntStateOf(0) }
    val totalSlides = 8
    val monthName = remember {
        LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    // Top Progress Bar segments
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .drawBehind {
                    // Subtle background dot matrix like Mochy
                    val dotColor = Color(0xFF1E1E22)
                    val spacing = 28.dp.toPx()
                    val radius = 1.2.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        var y = 0f
                        while (y < size.height) {
                            drawCircle(dotColor, radius = radius, center = Offset(x, y))
                            y += spacing
                        }
                        x += spacing
                    }
                },
        ) {
            // Tap navigation overlay (Left = back, Right = forward)
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (currentSlide > 0) currentSlide--
                        },
                )
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (currentSlide < totalSlides - 1) {
                                currentSlide++
                            } else {
                                onDismissRequest()
                            }
                        },
                )
            }

            // Top Header: Segmented Story Bar & Close Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, start = 16.dp, end = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (i in 0 until totalSlides) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        i < currentSlide -> Color.White
                                        i == currentSlide -> Color.White
                                        else -> Color(0xFF333338)
                                    },
                                ),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(26.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🍡", fontSize = 14.sp)
                            }
                        }
                        Text(
                            text = "Mochiku Recap · $monthName",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCCCCC),
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                        )
                    }
                }
            }

            // Slide Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 70.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (currentSlide) {
                    0 -> SlideJourney(username, photoPath, joinedDate, readDuration)
                    1 -> SlideMonthProgress(monthName, totalChaptersRead)
                    2 -> SlideAuthorMangaCarousel(photoPath)
                    3 -> SlideFavoriteMangaHighlight(totalChaptersRead)
                    4 -> SlideCoverArtwork(totalChaptersRead)
                    5 -> SlideMilestoneChapters(totalChaptersRead)
                    6 -> SlideThermalReceipt(username, cardNumber, readDuration, totalChaptersRead, libraryCount)
                    7 -> SlideConclusion(username, cardNumber, joinedDate, monthName, onDismissRequest)
                }
            }
        }
    }
}

/** Slide 1: Journey Start */
@Composable
private fun SlideJourney(username: String, photoPath: String?, joinedDate: String, readDuration: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StoryAvatar(photoPath = photoPath, size = 110)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "$username you started your journey in Mochiku on",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFCCCCCC),
            textAlign = TextAlign.Center,
        )

        Text(
            text = joinedDate,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "and you have been devouring manga for a total time of",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFCCCCCC),
            textAlign = TextAlign.Center,
        )

        Text(
            text = readDuration,
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "so long, partners!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF999999),
        )

        Text(
            text = "TAP TO CONTINUE >",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF666666),
        )
    }
}

/** Slide 2: Month Pace (Progress Bar) */
@Composable
private fun SlideMonthProgress(monthName: String, totalChaptersRead: Int) {
    val progress = (totalChaptersRead / 50f).coerceIn(0.15f, 1f)
    val percentage = (progress * 100).toInt()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "$monthName is",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE0E0E0),
        )

        Text(
            text = "$percentage%",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )

        Text(
            text = "out of 100%",
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF8E8E8E),
        )

        // Rounded progress bar with glowing green accent
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF222228)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF52FF7A)),
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "$totalChaptersRead chapters read this month 📖",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

/** Slide 3: Again and again this author / manga */
@Composable
private fun SlideAuthorMangaCarousel(photoPath: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StoryAvatar(photoPath = photoPath, size = 64)

            Text(
                text = "Again\nand\nagain",
                fontSize = 42.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )

            Text(
                text = "this author..",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFAAAAAA),
            )
        }

        // Rolling circular covers
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            for (icon in listOf("⚔️", "🌸", "🌙", "🔥")) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E24))
                        .border(2.dp, Color(0xFF33333E), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(icon, fontSize = 34.sp)
                }
            }
        }
    }
}

/** Slide 4: Favorite Manga Highlight */
@Composable
private fun SlideFavoriteMangaHighlight(totalChaptersRead: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Recognize this manga?",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "that's your favorite this month!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9E9E9E),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1B22))
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("📚", fontSize = 64.sp)
            }

            // Tilted floating badge like Mochy
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        rotationZ = 12f
                        translationX = 14f
                    },
            ) {
                Text(
                    text = "${maxOf(totalChaptersRead / 2, 4)} chapters",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Top Reader Choice",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "${maxOf(totalChaptersRead, 10)}x Chapters Read",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF52FF7A),
        )
    }
}

/** Slide 5: Read through the panels */
@Composable
private fun SlideCoverArtwork(totalChaptersRead: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Turn the page..",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF16161C))
                .border(2.dp, Color(0xFF32323E), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🍙", fontSize = 72.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Mochiku Manga",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }

        Text(
            text = "Every panel, every story, devoured.",
            fontSize = 14.sp,
            color = Color(0xFF8E8E8E),
        )
    }
}

/** Slide 6: Chapter Milestone */
@Composable
private fun SlideMilestoneChapters(totalChaptersRead: Int) {
    val count = maxOf(totalChaptersRead, 12)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "$count Chapters!",
            fontSize = 46.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Your favourite series this month!!!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE0E0E0),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF141418))
                .border(1.dp, Color(0xFF2C2C36), RoundedCornerShape(50))
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "✦ S-RANK OTAKU RATING ✦",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF52FF7A),
            )
        }
    }
}

/** Slide 7: Vintage Thermal Paper Receipt ("Let's see your bill") */
@Composable
private fun SlideThermalReceipt(
    username: String,
    cardNumber: String,
    readDuration: String,
    totalChaptersRead: Int,
    libraryCount: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Let's see your bill",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Vintage thermal receipt card with serrated / jagged bottom edge
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                }
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Top Manga",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                )

                Spacer(Modifier.height(4.dp))

                val sampleTitles = listOf(
                    "1x Chainsaw Man - 1.2h",
                    "2x Oshi no Ko - 0.9h",
                    "3x Frieren: Beyond Journey's End - 0.8h",
                    "4x Dandadan - 0.7h",
                    "5x Jujutsu Kaisen - 0.6h",
                    "6x Spy x Family - 0.5h",
                    "7x Blue Lock - 0.4h",
                    "8x Solo Leveling - 0.3h",
                )

                sampleTitles.take(minOf(sampleTitles.size, maxOf(libraryCount, 4))).forEach { item ->
                    Text(
                        text = item,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF222222),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "--------------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF888888),
                )

                Text(
                    text = "Total : $readDuration playtime",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Text(
                    text = "$totalChaptersRead chapters · $libraryCount titles in library",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF555555),
                )

                Text(
                    text = "--------------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF888888),
                )

                Text(
                    text = "END OF LINE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "--------------------------------------",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF888888),
                )
            }
        }
    }
}

/** Slide 8: Conclusion ("There you go, That's your November recap!") */
@Composable
private fun SlideConclusion(
    username: String,
    cardNumber: String,
    joinedDate: String,
    monthName: String,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "There you go",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        // Mini preview of the ID card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF242428),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383842)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(130.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF383840)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🍡", fontSize = 28.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("PROFILE USR#$cardNumber", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF9E9E9E))
                    Text(username, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("joined at $joinedDate", fontSize = 9.sp, color = Color(0xFFCCCCCC))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "That's your $monthName recap!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .clickable(onClick = onFinish)
                .padding(horizontal = 28.dp, vertical = 12.dp),
        ) {
            Text(
                text = "TAP TO FINISH",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = Color.Black,
            )
        }
    }
}

@Composable
private fun StoryAvatar(photoPath: String?, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFF222228))
            .border(2.dp, Color(0xFF444450), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = photoPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text("🍡", fontSize = (size * 0.45).sp)
        }
    }
}
