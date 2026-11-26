package eu.kanade.presentation.more.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import eu.kanade.tachiyomi.R
import kotlinx.coroutines.launch

private const val GRID_COLS = 3
private const val GRID_ROWS = 4
private const val HERO_TILE_INDEX = 4 // Center tile (row 1, col 1)
private const val MAX_UNIQUE_GRID_COVERS = 8

/**
 * -28deg panning masonry cover grid that fades surrounding tiles and un-rotates the #1 cover
 * into the centered spotlight.
 */
@Composable
internal fun RecapCoverGridSlide(data: MochikuRecapData) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val hero = data.heroManga

    val panProgress = remember(hero?.mangaId) { Animatable(0f) }
    val surroundAlpha = remember(hero?.mangaId) { Animatable(1f) }
    val gridRotation = remember(hero?.mangaId) { Animatable(-28f) }
    val heroScale = remember(hero?.mangaId) { Animatable(1f) }
    var spotlightReady by remember(hero?.mangaId) { mutableStateOf(hero == null) }

    LaunchedEffect(hero?.mangaId) {
        if (hero == null) return@LaunchedEffect
        spotlightReady = false
        panProgress.snapTo(0f)
        surroundAlpha.snapTo(1f)
        gridRotation.snapTo(-28f)
        heroScale.snapTo(1f)

        // Phase 0: Slow diagonal pan across tilted covers
        panProgress.animateTo(1f, tween(durationMillis = 1100, easing = FastOutSlowInEasing))
        // Phase 1: Fade surroundings, un-rotate to 0deg, and zoom hero cover
        launch { surroundAlpha.animateTo(0f, tween(durationMillis = 550, easing = FastOutSlowInEasing)) }
        launch { gridRotation.animateTo(0f, tween(durationMillis = 700, easing = FastOutSlowInEasing)) }
        heroScale.animateTo(1.85f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        spotlightReady = true
    }

    val distinctCovers = remember(data.topMangaList) {
        data.topMangaList.take(MAX_UNIQUE_GRID_COVERS)
    }

    if (hero == null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorScheme.surfaceContainer)
                    .border(1.5.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.komikku),
                        contentDescription = "Mochiku Logo",
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Mochiku Manga",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                    )
                }
            }
            Text(
                text = "Every panel, every story, devoured.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        // Intro caption before spotlight settles
        if (!spotlightReady) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 12.dp)
                    .graphicsLayer { alpha = surroundAlpha.value },
            ) {
                Text(
                    text = "You might recognize this!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground,
                )
                Text(
                    text = "hmm, which series...",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onBackground.copy(alpha = 0.65f),
                )
            }
        }

        // Tilted panning cover grid centered on HERO_TILE_INDEX
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    rotationZ = gridRotation.value
                    val panFade = surroundAlpha.value
                    translationX = (-26f + 52f * panProgress.value) * panFade
                    translationY = (20f - 40f * panProgress.value) * panFade
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (row in 0 until GRID_ROWS) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (col in 0 until GRID_COLS) {
                            val tileIdx = row * GRID_COLS + col
                            val isHeroTile = tileIdx == HERO_TILE_INDEX
                            val isPlaceholder = !isHeroTile && (distinctCovers.isEmpty() || tileIdx % 3 == 2)
                            val tileStat = if (isHeroTile) {
                                hero
                            } else if (!isPlaceholder && distinctCovers.isNotEmpty()) {
                                distinctCovers[tileIdx % distinctCovers.size]
                            } else {
                                null
                            }

                            Box(
                                modifier = Modifier
                                    .size(98.dp)
                                    .graphicsLayer {
                                        alpha = if (isHeroTile) 1f else surroundAlpha.value
                                        if (isHeroTile) {
                                            scaleX = heroScale.value
                                            scaleY = heroScale.value
                                        }
                                    }
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isHeroTile) {
                                            colorScheme.surfaceContainer
                                        } else {
                                            colorScheme.surfaceContainer.copy(alpha = 0.50f)
                                        },
                                    )
                                    .border(
                                        width = if (isHeroTile) 1.5.dp else 1.dp,
                                        color = if (isHeroTile && spotlightReady) {
                                            colorScheme.primary.copy(alpha = 0.7f)
                                        } else {
                                            colorScheme.outlineVariant.copy(alpha = 0.45f)
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (tileStat?.cover != null) {
                                    val req = remember(tileStat.cover) {
                                        ImageRequest.Builder(context).data(tileStat.cover).size(160).build()
                                    }
                                    AsyncImage(
                                        model = req,
                                        contentDescription = tileStat.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else if (isHeroTile) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoStories,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(34.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (spotlightReady) {
            Text(
                text = "Turn the page..",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = hero.title,
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                RollingCounterText(
                    targetValue = hero.chaptersRead,
                    suffix = " Chapters!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.primary,
                )
                Text(
                    text = "Your favorite manga this month · ${hero.durationFormatted}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
