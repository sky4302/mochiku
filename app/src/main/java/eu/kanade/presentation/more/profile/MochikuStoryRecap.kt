package eu.kanade.presentation.more.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay

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
    // Query actual monthly reading data from Komikku's history database
    val recapData by produceState<MochikuRecapData?>(initialValue = null) {
        value = MochikuRecapDataEngine.loadCurrentMonthRecap()
    }

    var currentSlide by remember { mutableIntStateOf(0) }
    var previousSlide by remember { mutableIntStateOf(0) }
    val totalSlides = 8

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val backgroundColor = MaterialTheme.colorScheme.background
        val dotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        val onBackgroundColor = MaterialTheme.colorScheme.onBackground

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .drawBehind {
                    // Mochy-style dot matrix grid
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
            val data = recapData
            if (data == null) {
                // Sleek loading spinner while reading SQLite history
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Text(
                            text = "Analyzing reading history...",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        )
                    }
                }
            } else {
                // Tap Navigation Overlay (Left 30% = Prev, Right 70% = Next)
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (currentSlide > 0) {
                                    previousSlide = currentSlide
                                    currentSlide--
                                }
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
                                    previousSlide = currentSlide
                                    currentSlide++
                                } else {
                                    onDismissRequest()
                                }
                            },
                    )
                }

                // Top Segmented Progress Bar & Title
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
                                            i < currentSlide -> MaterialTheme.colorScheme.primary
                                            i == currentSlide -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
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
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.size(24.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🍡", fontSize = 13.sp)
                                }
                            }
                            Text(
                                text = "Mochiku Recap · ${data.monthName} ${data.year}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            )
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = onBackgroundColor,
                            )
                        }
                    }
                }

                // Slide Content with PPTX Deck Transitions
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 70.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = currentSlide,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(tween(350, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(350, easing = FastOutSlowInEasing)) { -it / 2 } + fadeOut(tween(250)))
                            } else {
                                (slideInHorizontally(tween(350, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(350, easing = FastOutSlowInEasing)) { it / 2 } + fadeOut(tween(250)))
                            }
                        },
                        label = "pptxSlideTransition",
                    ) { targetSlide ->
                        when (targetSlide) {
                            0 -> SlideJourney(username, photoPath, joinedDate, data.allTimeDurationFormatted)
                            1 -> SlideMonthProgress(data.monthName, data.monthChaptersRead)
                            2 -> SlideAuthorMangaCarousel(photoPath, data)
                            3 -> SlideFavoriteMangaHighlight(data)
                            4 -> SlideCoverArtwork(data)
                            5 -> SlideMilestoneChapters(data)
                            6 -> SlideThermalReceipt(username, cardNumber, data)
                            7 -> SlideConclusion(username, cardNumber, joinedDate, data.monthName, photoPath, onDismissRequest)
                        }
                    }
                }
            }
        }
    }
}

/** Reusable PowerPoint-style entrance build-in component */
@Composable
private fun PptxBuildIn(
    index: Int,
    stepDelayMs: Int = 140,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * stepDelayMs).toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "pptxAlpha",
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 26.dp,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "pptxOffset",
    )
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY.toPx()
        },
    ) {
        content()
    }
}

/** Slide 1: Journey Start */
@Composable
private fun SlideJourney(
    username: String,
    photoPath: String?,
    joinedDate: String,
    allTimeDurationFormatted: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PptxBuildIn(index = 0) {
            StoryAvatar(photoPath = photoPath, size = 110)
        }

        Spacer(Modifier.height(4.dp))

        PptxBuildIn(index = 1) {
            Text(
                text = "$username you started your journey in Mochiku on",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }

        PptxBuildIn(index = 2) {
            Text(
                text = joinedDate,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }

        PptxBuildIn(index = 3) {
            Text(
                text = "and you have been reading manga for a total time of",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }

        PptxBuildIn(index = 4) {
            Text(
                text = allTimeDurationFormatted,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(10.dp))

        PptxBuildIn(index = 5) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "so long, partners!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "TAP TO CONTINUE >",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF666666),
                )
            }
        }
    }
}

/** Slide 2: Month Pace (Progress Bar with animated count-up) */
@Composable
private fun SlideMonthProgress(monthName: String, monthChaptersRead: Int) {
    val targetProgress = if (monthChaptersRead > 0) (monthChaptersRead / 50f).coerceIn(0.15f, 1f) else 0.05f
    val targetPercentage = (targetProgress * 100).toInt()

    // PPTX Counter animation
    val animatedPercent = remember { Animatable(0f) }
    val animatedBar = remember { Animatable(0f) }

    LaunchedEffect(targetPercentage) {
        delay(250)
        animatedPercent.animateTo(
            targetValue = targetPercentage.toFloat(),
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(targetProgress) {
        delay(250)
        animatedBar.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "$monthName is",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            )
        }

        PptxBuildIn(index = 1) {
            Text(
                text = "${animatedPercent.value.toInt()}%",
                fontSize = 76.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        PptxBuildIn(index = 2) {
            Text(
                text = "out of 100%",
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }

        PptxBuildIn(index = 3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedBar.value.coerceIn(0.01f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }

        PptxBuildIn(index = 4) {
            Text(
                text = if (monthChaptersRead > 0) {
                    "$monthChaptersRead chapters read this month 📖"
                } else {
                    "0 chapters read so far in $monthName"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

/** Slide 3: Again and again this author / manga */
@Composable
private fun SlideAuthorMangaCarousel(photoPath: String?, data: MochikuRecapData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PptxBuildIn(index = 0) {
                StoryAvatar(photoPath = photoPath, size = 56)
            }

            PptxBuildIn(index = 1) {
                Text(
                    text = "Again\nand\nagain",
                    fontSize = 42.sp,
                    lineHeight = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            }

            PptxBuildIn(index = 2) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (data.topAuthor != null) "this author.." else "this series..",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    )
                    if (data.topAuthor != null) {
                        Text(
                            text = data.topAuthor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // Rolling circular covers from user's actual reading history
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val list = if (data.topMangaList.isNotEmpty()) {
                data.topMangaList.take(4)
            } else {
                emptyList()
            }

            if (list.isNotEmpty()) {
                list.forEachIndexed { idx, item ->
                    PptxBuildIn(index = 2 + idx, stepDelayMs = 120) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1E24))
                                .border(2.dp, Color(0xFF444450), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.cover != null) {
                                AsyncImage(
                                    model = item.cover,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Text("📖", fontSize = 30.sp)
                            }
                        }
                    }
                }
            } else {
                PptxBuildIn(index = 3) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E24))
                            .border(2.dp, Color(0xFF444450), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("📚", fontSize = 32.sp)
                    }
                }
            }
        }
    }
}

/** Slide 4: Favorite Manga Highlight */
@Composable
private fun SlideFavoriteMangaHighlight(data: MochikuRecapData) {
    val hero = data.heroManga

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "Recognize this manga?",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }

        PptxBuildIn(index = 1) {
            Text(
                text = if (hero != null) "that's your favorite this month!" else "read a chapter to crown your favorite!",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(6.dp))

        PptxBuildIn(index = 2) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (hero?.cover != null) {
                        AsyncImage(
                            model = hero.cover,
                            contentDescription = hero.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text("📚", fontSize = 56.sp)
                    }
                }

                if (hero != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .graphicsLayer {
                                rotationZ = 12f
                                translationX = 12f
                            },
                    ) {
                        Text(
                            text = "${hero.chaptersRead} chapters",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        PptxBuildIn(index = 3) {
            Text(
                text = hero?.title ?: "No Manga Read Yet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (hero != null) {
            PptxBuildIn(index = 4) {
                Text(
                    text = "${hero.chaptersRead}x Chapters · ${hero.durationFormatted}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF52FF7A),
                )
            }
        }
    }
}

/** Slide 5: Read through the panels (Showcase) */
@Composable
private fun SlideCoverArtwork(data: MochikuRecapData) {
    val hero = data.heroManga

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "Turn the page..",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
        }

        PptxBuildIn(index = 1) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF16161C))
                    .border(2.dp, Color(0xFF32323E), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (hero?.cover != null) {
                    AsyncImage(
                        model = hero.cover,
                        contentDescription = hero.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍙", fontSize = 64.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Mochiku Manga",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        PptxBuildIn(index = 2) {
            Text(
                text = hero?.title ?: "Every panel, every story, devoured.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Slide 6: Chapter Milestone */
@Composable
private fun SlideMilestoneChapters(data: MochikuRecapData) {
    val chapters = data.monthChaptersRead

    // Number count-up
    val countAnim = remember { Animatable(0f) }
    LaunchedEffect(chapters) {
        delay(200)
        countAnim.animateTo(
            targetValue = chapters.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "${countAnim.value.toInt()} Chapters!",
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }

        PptxBuildIn(index = 1) {
            Text(
                text = if (chapters > 0) "Your reading feat this month!!!" else "Ready for your next chapter!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(14.dp))

        PptxBuildIn(index = 2) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                Text(
                    text = if (chapters >= 20) "✦ S-RANK OTAKU RATING ✦" else if (chapters >= 10) "✦ A-RANK READER STATUS ✦" else "✦ ADVENTURER STATUS ✦",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Slide 7: Vintage Thermal Paper Receipt ("Let's see your bill") with printing slide-down animation */
@Composable
private fun SlideThermalReceipt(
    username: String,
    cardNumber: String,
    data: MochikuRecapData,
) {
    // Thermal receipt printing slide-down animation
    val receiptSlideY = remember { Animatable(-100f) }
    val receiptAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        receiptSlideY.animateTo(0f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        receiptAlpha.animateTo(1f, tween(durationMillis = 400))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "Let's see your bill",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        // Vintage thermal receipt card with serrated / jagged bottom edge
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .graphicsLayer {
                    translationY = receiptSlideY.value
                    alpha = receiptAlpha.value
                }
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

                if (data.topMangaList.isNotEmpty()) {
                    data.topMangaList.take(8).forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${i + 1}x ${item.title}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF222222),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "- ${item.durationFormatted}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF444444),
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No reading history recorded this month.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF666666),
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
                    text = "Total : ${data.monthDurationFormatted} playtime",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Text(
                    text = "${data.monthChaptersRead} chapters · ${data.libraryCount} titles in library",
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

/** Slide 8: Conclusion */
@Composable
private fun SlideConclusion(
    username: String,
    cardNumber: String,
    joinedDate: String,
    monthName: String,
    photoPath: String?,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "There you go",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Mini preview of the ID card
        PptxBuildIn(index = 1) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    StoryAvatar(photoPath = photoPath, size = 60)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("PROFILE USR#$cardNumber", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(username, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("joined at $joinedDate", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        PptxBuildIn(index = 2) {
            Text(
                text = "That's your $monthName recap!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(10.dp))

        PptxBuildIn(index = 3) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onFinish)
                    .padding(horizontal = 28.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "TAP TO FINISH",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
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
