package eu.kanade.presentation.more.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import eu.kanade.presentation.more.stats.components.PassportStampBadge
import eu.kanade.tachiyomi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.domain.history.model.StampState
import java.time.LocalDate
import java.util.Locale

private const val TOTAL_RECAP_SLIDES = 8

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
    recentStamps: List<StampState> = emptyList(),
    startWithCardVerify: Boolean = false,
) {
    // Query monthly reading history from Komikku's SQLite database
    val recapData by produceState<MochikuRecapData?>(initialValue = null) {
        value = MochikuRecapDataEngine.loadCurrentMonthRecap()
    }

    var isCardVerified by remember(startWithCardVerify) { mutableStateOf(!startWithCardVerify) }
    var currentSlide by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    fun advanceSlide() {
        if (currentSlide < TOTAL_RECAP_SLIDES - 1) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentSlide++
        } else {
            onDismissRequest()
        }
    }

    fun rewindSlide() {
        if (currentSlide > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentSlide--
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val colorScheme = MaterialTheme.colorScheme
        val backgroundColor = colorScheme.background
        val dotColor = colorScheme.outlineVariant.copy(alpha = 0.35f)
        val onBackgroundColor = colorScheme.onBackground

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clipToBounds(),
        ) {
            val data = recapData

            // Low-opacity scrolling watermark marquee behind the cached dot matrix
            val watermarkLabel = if (!isCardVerified || data == null) {
                "MOCHIKU"
            } else {
                when (currentSlide) {
                    1 -> data.monthName.uppercase(Locale.ENGLISH)
                    2 -> (data.favoriteWeekday ?: data.monthName).uppercase(Locale.ENGLISH)
                    else -> "MOCHIKU"
                }
            }
            RecapWatermarkMarquee(
                text = watermarkLabel,
                color = onBackgroundColor.copy(alpha = 0.045f),
            )

            // Cached Mochy-style dot-matrix texture
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val spacing = 28.dp.toPx()
                        val radius = 1.2.dp.toPx()
                        onDrawBehind {
                            var x = 0f
                            while (x < size.width) {
                                var y = 0f
                                while (y < size.height) {
                                    drawCircle(dotColor, radius = radius, center = Offset(x, y))
                                    y += spacing
                                }
                                x += spacing
                            }
                        }
                    },
            )

            if (!isCardVerified) {
                // Stage 0/1: Centered Front Card -> Auto-Flip 180deg -> Scratch Foil -> Laser Verify
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainer,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.komikku),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape),
                            )
                        }
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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    RecapCardVerifyStage(
                        username = username,
                        photoPath = photoPath,
                        cardNumber = cardNumber,
                        joinedDate = joinedDate,
                        recentStamps = recentStamps,
                        onVerified = { isCardVerified = true },
                    )
                }
            } else if (data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        CircularProgressIndicator(color = colorScheme.primary, modifier = Modifier.size(36.dp))
                        Text(
                            text = "Analyzing reading history...",
                            color = onBackgroundColor.copy(alpha = 0.7f),
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
                            ) { rewindSlide() },
                    )
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { advanceSlide() },
                    )
                }

                // Top Chrome: Hairline Monochrome Progress + Docked Avatar + Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(top = 12.dp, start = 16.dp, end = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        for (i in 0 until TOTAL_RECAP_SLIDES) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        if (i <= currentSlide) {
                                            onBackgroundColor.copy(alpha = 0.85f)
                                        } else {
                                            colorScheme.outlineVariant.copy(alpha = 0.32f)
                                        },
                                    ),
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            AnimatedVisibility(
                                visible = currentSlide > 0,
                                enter = fadeIn(tween(260)) + scaleIn(initialScale = 0.75f, animationSpec = tween(260)),
                                exit = fadeOut(tween(180)),
                            ) {
                                StoryAvatar(photoPath = photoPath, size = 30)
                            }

                            Surface(
                                shape = CircleShape,
                                color = colorScheme.surfaceContainer,
                                modifier = Modifier.size(24.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(R.drawable.komikku),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape),
                                    )
                                }
                            }
                            Text(
                                text = "Mochiku Recap · ${data.monthName} ${data.year}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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

                // Scene Content with Video-Reel Crossfade & Scale Cuts
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 24.dp, vertical = 72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = currentSlide,
                        transitionSpec = {
                            (
                                fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                                    scaleIn(
                                        initialScale = 0.96f,
                                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                                    )
                                )
                                .togetherWith(fadeOut(tween(220, easing = FastOutSlowInEasing)))
                        },
                        label = "recapSceneTransition",
                    ) { targetSlide ->
                        when (targetSlide) {
                            0 -> SlideJourney(username, photoPath, joinedDate, data.allTimeDurationMs)
                            1 -> SlideMonthProgress(data.monthName, data.monthChaptersRead)
                            2 -> SlideFavoriteWeekday(data)
                            3 -> RecapRouletteSlide(data)
                            4 -> RecapCoverGridSlide(data)
                            5 -> SlideMilestoneChapters(data)
                            6 -> RecapThermalReceiptSlide(
                                username = username,
                                cardNumber = cardNumber,
                                data = data,
                                onContinue = ::advanceSlide,
                            )
                            7 -> SlideConclusion(
                                username = username,
                                cardNumber = cardNumber,
                                joinedDate = joinedDate,
                                monthName = data.monthName,
                                photoPath = photoPath,
                                recentStamps = recentStamps,
                                onFinish = onDismissRequest,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Stage 0/1: centers the front ID card, auto-flips 180deg in 3D, presents the
 * monochrome scratch foil + "scratch!" cue, and runs laser verification into Slide 1.
 */
@Composable
private fun RecapCardVerifyStage(
    username: String,
    photoPath: String?,
    cardNumber: String,
    joinedDate: String,
    recentStamps: List<StampState>,
    onVerified: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val entryScale = remember { Animatable(0.86f) }
    val cardRotation = remember { Animatable(0f) }
    val laserProgress = remember { Animatable(0f) }

    var autoFlipDone by remember { mutableStateOf(false) }
    var isScratchedDone by remember { mutableStateOf(false) }
    var showSkipCue by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        entryScale.animateTo(1f, tween(durationMillis = 240, easing = FastOutSlowInEasing))
        delay(220)
        cardRotation.animateTo(180f, tween(durationMillis = 600, easing = FastOutSlowInEasing))
        autoFlipDone = true
        delay(1500)
        showSkipCue = true
    }

    fun triggerVerifySweep() {
        if (isScanning || isVerified) return
        isScratchedDone = true
        isScanning = true
        scope.launch {
            laserProgress.snapTo(0f)
            laserProgress.animateTo(1f, tween(durationMillis = 950, easing = FastOutSlowInEasing))
            isVerified = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(480)
            isScanning = false
            onVerified()
        }
    }

    val foilAlpha by animateFloatAsState(
        targetValue = if (isScratchedDone) 0f else 1f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "verifyFoilAlpha",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ID_CARD_ASPECT)
                .graphicsLayer {
                    scaleX = entryScale.value
                    scaleY = entryScale.value
                    rotationY = cardRotation.value
                    cameraDistance = 14f * density.density
                }
                .clip(RoundedCornerShape(22.dp))
                .background(colorScheme.surfaceContainerHigh)
                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.55f), RoundedCornerShape(22.dp)),
        ) {
            if (cardRotation.value <= 90f) {
                MochikuIdCardFrontContent(
                    username = username,
                    photoPath = photoPath,
                    cardNumber = cardNumber,
                    joinedDate = joinedDate,
                    onEditProfileClick = null,
                    hintText = null,
                    recentStamps = recentStamps,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                ) {
                    MochikuIdCardBackContent(
                        cardNumber = cardNumber,
                        username = username,
                        isScanning = isScanning,
                        isVerified = isVerified,
                        scanProgress = laserProgress.value,
                        statusText = when {
                            isVerified -> "VERIFIED"
                            isScanning -> "SCANNING BARCODE..."
                            else -> null
                        },
                        barcodeOverlay = {
                            if (foilAlpha > 0f) {
                                ScratchOffFoilOverlay(
                                    sessionKey = 0,
                                    alpha = foilAlpha,
                                    enabled = autoFlipDone && !isScratchedDone && !isScanning && !isVerified,
                                    onScratchComplete = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        triggerVerifySweep()
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(52.dp),
        ) {
            AnimatedVisibility(
                visible = autoFlipDone && !isScratchedDone && !isScanning && !isVerified,
                enter = fadeIn(tween(240)),
                exit = fadeOut(tween(180)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "scratch!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onBackground,
                    )
                    if (showSkipCue) {
                        Text(
                            text = "TAP TO SKIP",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground.copy(alpha = 0.55f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = ::triggerVerifySweep)
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

/** GPU-accelerated scrolling watermark marquee rendered at ultra-low monochrome alpha. */
@Composable
private fun RecapWatermarkMarquee(
    text: String,
    color: Color,
) {
    val transition = rememberInfiniteTransition(label = "watermarkTransition")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "watermarkDrift",
    )
    val repeated = remember(text) { List(6) { text }.joinToString("   ") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (row in 0 until 4) {
            val dir = if (row % 2 == 0) -1f else 1f
            Text(
                text = repeated,
                fontSize = 68.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                color = color,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.graphicsLayer {
                    translationX = dir * (drift * 360f - 180f)
                },
            )
        }
    }
}

/** Reusable staggered entrance build-in component. */
@Composable
internal fun PptxBuildIn(
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

/** Animated integer odometer counter. */
@Composable
internal fun RollingCounterText(
    targetValue: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    fontSize: TextUnit = 32.sp,
    fontWeight: FontWeight = FontWeight.Black,
    fontFamily: FontFamily = FontFamily.Monospace,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Center,
) {
    val counter = remember(targetValue) { Animatable(0f) }
    LaunchedEffect(targetValue) {
        counter.snapTo(0f)
        delay(180)
        counter.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
        )
    }
    Text(
        text = "$prefix${counter.value.toInt()}$suffix",
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        color = color,
        textAlign = textAlign,
        modifier = modifier,
    )
}

/** Slide 1: Journey Start */
@Composable
private fun SlideJourney(
    username: String,
    photoPath: String?,
    joinedDate: String,
    allTimeDurationMs: Long,
) {
    val durationAnim = remember(allTimeDurationMs) { Animatable(0f) }
    LaunchedEffect(allTimeDurationMs) {
        durationAnim.snapTo(0f)
        delay(380)
        durationAnim.animateTo(
            targetValue = allTimeDurationMs.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        )
    }

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
                lineHeight = 38.sp,
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
                text = formatDurationMs(durationAnim.value.toLong()),
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
                    text = "TAP !",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/** Slide 2: Month Pace (Real calendar month progress with animated 2-decimal count-up) */
@Composable
private fun SlideMonthProgress(monthName: String, monthChaptersRead: Int) {
    val today = remember { LocalDate.now() }
    val dayOfMonth = today.dayOfMonth
    val daysInMonth = today.lengthOfMonth().coerceAtLeast(1)
    val targetProgress = (dayOfMonth.toFloat() / daysInMonth.toFloat()).coerceIn(0f, 1f)
    val targetPercentage = targetProgress * 100f

    val animatedPercent = remember { Animatable(0f) }
    val animatedBar = remember { Animatable(0f) }

    LaunchedEffect(targetPercentage) {
        delay(250)
        animatedPercent.animateTo(
            targetValue = targetPercentage,
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
                text = String.format(Locale.US, "%.2f%%", animatedPercent.value),
                fontSize = 62.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false,
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
                    "Day $dayOfMonth of $daysInMonth · $monthChaptersRead chapters read this month"
                } else {
                    "Day $dayOfMonth of $daysInMonth · 0 chapters read so far"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Slide 3: Favorite Reading Weekday */
@Composable
private fun SlideFavoriteWeekday(data: MochikuRecapData) {
    val colorScheme = MaterialTheme.colorScheme
    val weekday = data.favoriteWeekday

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (weekday == null) {
            PptxBuildIn(index = 0) {
                Text(
                    text = "and at this time,",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onBackground.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                )
            }
            PptxBuildIn(index = 1) {
                Text(
                    text = "NO PEAK DAY YET",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
            PptxBuildIn(index = 2) {
                Text(
                    text = "Read chapters across the week to crown your favorite reading day.",
                    fontSize = 15.sp,
                    color = colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            PptxBuildIn(index = 0) {
                Text(
                    text = "and at this time,",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onBackground.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                )
            }

            PptxBuildIn(index = 1) {
                Text(
                    text = weekday.uppercase(Locale.ENGLISH),
                    fontSize = 44.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            PptxBuildIn(index = 2) {
                Text(
                    text = "is your favourite day to read manga",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(6.dp))

            PptxBuildIn(index = 3) {
                val pct = data.percentMoreThanSecondDay
                if (data.favoriteWeekdayMinutes > 0L) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RollingCounterText(
                            targetValue = data.favoriteWeekdayMinutes.toInt(),
                            suffix = if (pct != null && pct > 0) " minutes, $pct% more than your 2nd!" else " minutes read!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground,
                        )
                    }
                } else {
                    Text(
                        text = "your top reading day this ${data.monthName}",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.onBackground.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Slide 6: Chapter Milestone */
@Composable
private fun SlideMilestoneChapters(data: MochikuRecapData) {
    val chapters = data.monthChaptersRead

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PptxBuildIn(index = 0) {
            RollingCounterText(
                targetValue = chapters,
                suffix = " Chapters!",
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.onBackground,
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
                    text = if (chapters >= 20) {
                        "✦ S-RANK OTAKU RATING ✦"
                    } else if (chapters >= 10) {
                        "✦ A-RANK READER STATUS ✦"
                    } else {
                        "✦ ADVENTURER STATUS ✦"
                    },
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
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
    recentStamps: List<StampState> = emptyList(),
    onFinish: () -> Unit,
) {
    val latestMiniStamps = remember(recentStamps) {
        recentStamps
            .filter { it.isUnlocked }
            .sortedByDescending { it.unlockedAt ?: 0L }
            .take(3)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PptxBuildIn(index = 0) {
            Text(
                text = "There you go",
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Mini preview of the ID card (minimalist card + latest earned mini-stamps)
        PptxBuildIn(index = 1) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(130.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StoryAvatar(photoPath = photoPath, size = 60)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = if (latestMiniStamps.isNotEmpty()) 88.dp else 0.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text("PROFILE USR#$cardNumber", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(username, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("joined at $joinedDate", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }

                    if (latestMiniStamps.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy((-10).dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            latestMiniStamps.forEach { stamp ->
                                PassportStampBadge(
                                    state = stamp,
                                    size = 38.dp,
                                    compact = true,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        PptxBuildIn(index = 2) {
            Text(
                text = "That's your $monthName recap!",
                fontSize = 28.sp,
                lineHeight = 34.sp,
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
internal fun StoryAvatar(photoPath: String?, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape),
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
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size((size * 0.55).dp),
            )
        }
    }
}
