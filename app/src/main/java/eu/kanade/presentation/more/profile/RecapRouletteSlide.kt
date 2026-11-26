package eu.kanade.presentation.more.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ROULETTE_SLOT_COUNT = 12

/**
 * Decelerating circular cover roulette that locks onto the user's #1 series with ripple rings.
 */
@Composable
internal fun RecapRouletteSlide(data: MochikuRecapData) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val hero = data.heroManga

    val spinProgress = remember(hero?.mangaId) { Animatable(0f) }
    val ripple1 = remember(hero?.mangaId) { Animatable(0f) }
    val ripple2 = remember(hero?.mangaId) { Animatable(0f) }
    var isLocked by remember(hero?.mangaId) { mutableStateOf(hero == null) }

    LaunchedEffect(hero?.mangaId) {
        if (hero == null) return@LaunchedEffect
        isLocked = false
        spinProgress.snapTo(0f)
        ripple1.snapTo(0f)
        ripple2.snapTo(0f)
        delay(220)
        spinProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1450, easing = FastOutSlowInEasing),
        )
        isLocked = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        launch {
            ripple1.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
        }
        launch {
            delay(180)
            ripple2.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
        }
    }

    val otherAlpha by animateFloatAsState(
        targetValue = if (isLocked) 0f else 0.72f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "rouletteOtherAlpha",
    )
    val badgeScale by animateFloatAsState(
        targetValue = if (isLocked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "rouletteBadgeScale",
    )

    val roulettePool = remember(data.topMangaList) {
        if (data.topMangaList.isEmpty()) {
            emptyList()
        } else {
            List(ROULETTE_SLOT_COUNT) { idx ->
                // Index 0 lands centered at spinProgress = 1f
                if (idx == 0) data.topMangaList.first() else data.topMangaList[idx % data.topMangaList.size]
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1.1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PptxBuildIn(index = 0) {
                    Text(
                        text = "Again\nand\nagain",
                        fontSize = 40.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onBackground,
                    )
                }

                PptxBuildIn(index = 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (data.topAuthor != null) "this author & series.." else "this series..",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground.copy(alpha = 0.72f),
                        )
                        if (data.topAuthor != null) {
                            Text(
                                text = data.topAuthor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Vertical roulette column on right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(276.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.Center,
            ) {
                if (roulettePool.isEmpty() || hero == null) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceContainerHighest)
                            .border(1.5.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CollectionsBookmark,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                } else {
                    val slotStepPx = with(density) { 96.dp.toPx() }
                    val travelSlots = (ROULETTE_SLOT_COUNT - 2).toFloat()

                    // Ripple rings behind locked center cover
                    if (isLocked) {
                        listOf(ripple1.value, ripple2.value).forEach { r ->
                            if (r in 0.01f..0.99f) {
                                Box(
                                    modifier = Modifier
                                        .size(116.dp)
                                        .graphicsLayer {
                                            val s = 1f + r * 0.85f
                                            scaleX = s
                                            scaleY = s
                                            alpha = (1f - r) * 0.45f
                                        }
                                        .border(1.5.dp, colorScheme.onBackground, CircleShape),
                                )
                            }
                        }
                    }

                    roulettePool.forEachIndexed { index, item ->
                        val isWinner = index == 0
                        val itemOffsetYPx = (index - (1f - spinProgress.value) * travelSlots) * slotStepPx
                        val itemAlpha = if (isWinner) 1f else otherAlpha
                        val itemScale = when {
                            isWinner && isLocked -> 1.18f
                            isWinner -> 1.0f
                            else -> 0.82f
                        }

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = itemOffsetYPx
                                    scaleX = itemScale
                                    scaleY = itemScale
                                    alpha = itemAlpha
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surfaceContainerHighest)
                                    .border(
                                        width = if (isWinner && isLocked) 2.5.dp else 1.5.dp,
                                        color = if (isWinner && isLocked) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (item.cover != null) {
                                    val req = remember(item.cover) {
                                        ImageRequest.Builder(context).data(item.cover).size(220).build()
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
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(30.dp),
                                    )
                                }
                            }

                            if (isWinner && badgeScale > 0.01f) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .graphicsLayer {
                                            scaleX = badgeScale
                                            scaleY = badgeScale
                                            rotationZ = 8f
                                            translationX = 10f
                                            translationY = -8f
                                        },
                                ) {
                                    Text(
                                        text = "${hero.chaptersRead} ch",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (hero != null && isLocked) {
            PptxBuildIn(index = 0, stepDelayMs = 80) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MOST REVISITED THIS MONTH",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                    Text(
                        text = hero.title,
                        fontSize = 22.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${hero.chaptersRead}x Chapters · ${hero.durationFormatted}",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
            }
        } else if (hero == null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Read a series this month to crown your top pick.",
                fontSize = 14.sp,
                color = colorScheme.onBackground.copy(alpha = 0.65f),
            )
        }
    }
}
