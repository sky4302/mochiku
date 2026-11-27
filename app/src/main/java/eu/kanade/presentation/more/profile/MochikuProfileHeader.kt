package eu.kanade.presentation.more.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.domain.history.interactor.GetTotalReadDuration
import tachiyomi.domain.history.model.StampState
import tachiyomi.domain.manga.interactor.GetLibraryManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun MochikuProfileHeader(
    onClickStats: () -> Unit,
    modifier: Modifier = Modifier,
    recentStamps: ImmutableList<StampState> = persistentListOf(),
    topPadding: Dp = 0.dp,
    pullOffsetProvider: () -> Float = { 0f },
    pullThresholdPx: Float = 1f,
    pullLaunchNonce: Int = 0,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    var username by remember { mutableStateOf(MochikuProfileStore.getUsername(context)) }
    var photoPath by remember { mutableStateOf(MochikuProfileStore.getPhotoPath(context)) }
    val cardNumber = remember { MochikuProfileStore.getCardNumber(context) }
    val joinedDate = remember { MochikuProfileStore.getJoinedDate(context) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showStoryRecapDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pullLaunchNonce) {
        if (pullLaunchNonce > 0) {
            showStoryRecapDialog = true
        }
    }

    // Read live stats asynchronously from database
    val recapData by produceState<MochikuRecapData?>(initialValue = null) {
        value = MochikuRecapDataEngine.loadCurrentMonthRecap()
    }

    val displayDuration = recapData?.monthDurationFormatted ?: "0h 0m"
    val displayChapters = recapData?.monthChaptersRead ?: 0
    val displayLibraryCount = recapData?.libraryCount ?: 0

    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    val watermarkColor = colorScheme.onBackground.copy(alpha = if (isDarkTheme) 0.12f else 0.08f)
    val dotColor = mochikuDotColor(
        surfaceColor = colorScheme.surfaceContainerLow,
        onBackgroundColor = colorScheme.onBackground,
    )
    val heroShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Dotted-matrix expandable hero card matching Mochy-Music profile header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(heroShape)
                .background(colorScheme.surfaceContainerLow)
                .clipToBounds()
                .mochikuDotMatrix(dotColor = dotColor, spacing = 24.dp, radius = 1.2.dp),
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                RightEdgeBrandWatermark(
                    text = "MOCHIKU",
                    color = watermarkColor,
                    fontSize = 104.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 26.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = topPadding + 12.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 6.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Top Greeting (Matches Mochy-Music: "Hello again, Sky!")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                    ) {
                        Text(
                            text = "Hello again, $username!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = "Mochiku Manga Profile",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                        )
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                    }
                }

                // The Signature Mochy 3D ID Card (tapping opens centered auto-flip + scratch stage)
                MochikuIdCard(
                    username = username,
                    photoPath = photoPath,
                    cardNumber = cardNumber,
                    joinedDate = joinedDate,
                    onEditProfileClick = { showEditProfileDialog = true },
                    onLaunchRecap = { showStoryRecapDialog = true },
                    recentStamps = recentStamps,
                )

                val pullPx = pullOffsetProvider()
                val pullProgress = (pullPx / pullThresholdPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
                val pullExtraDp = with(density) { (pullPx * 0.55f).toDp() }
                if (pullExtraDp > 0.dp) {
                    Spacer(Modifier.height(pullExtraDp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .clickable { showStoryRecapDialog = true }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Open Story Recap",
                        tint = if (pullProgress >= 1f) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                translationY = pullProgress * 6f
                                val s = 1f + pullProgress * 0.2f
                                scaleX = s
                                scaleY = s
                            },
                    )
                }
            }
        }

        // Reading Time Card (Matches Mochy: "2h 9m Total Listening time | View Stats >")
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onClickStats),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QueryStats,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )

                    Column {
                        Text(
                            text = displayDuration,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = "This Month · $displayChapters ch read",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "View Stats",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        HorizontalDivider(
            color = colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }

    // Set Up Profile Dialog
    if (showEditProfileDialog) {
        MochikuEditProfileDialog(
            initialUsername = username,
            initialPhotoPath = photoPath,
            cardNumber = cardNumber,
            joinedDate = joinedDate,
            onDismissRequest = { showEditProfileDialog = false },
            onProfileSaved = { newName, newPhoto ->
                username = newName
                photoPath = newPhoto
            },
        )
    }

    // Fullscreen Story Recap Dialog (starts with centered tap-to-flip + scratch stage)
    if (showStoryRecapDialog) {
        MochikuStoryRecapDialog(
            username = username,
            photoPath = photoPath,
            cardNumber = cardNumber,
            joinedDate = joinedDate,
            readDuration = displayDuration,
            totalChaptersRead = displayChapters,
            libraryCount = displayLibraryCount,
            onDismissRequest = { showStoryRecapDialog = false },
            recentStamps = recentStamps,
            startWithCardVerify = true,
        )
    }
}

/** Vertical right-edge MOCHIKU watermark hugging the edge independently of screen DPI. */
@Composable
internal fun RightEdgeBrandWatermark(
    text: String,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    letterSpacing: TextUnit = 6.sp,
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = letterSpacing,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .layout { measurable, _ ->
                val placeable = measurable.measure(Constraints())
                layout(placeable.height, placeable.width) {
                    placeable.place(
                        x = (placeable.height - placeable.width) / 2,
                        y = (placeable.width - placeable.height) / 2,
                    )
                }
            }
            .graphicsLayer { rotationZ = 90f },
    )
}

/** Luminance-aware dot color so the background matrix remains crisp in OLED dark and light themes. */
internal fun mochikuDotColor(
    surfaceColor: Color,
    onBackgroundColor: Color,
): Color {
    val alpha = if (surfaceColor.luminance() < 0.5f) 0.14f else 0.10f
    return onBackgroundColor.copy(alpha = alpha)
}

/** Cached dot-matrix backdrop modifier shared across profile hero, story recap, and share cards. */
internal fun Modifier.mochikuDotMatrix(
    dotColor: Color,
    spacing: Dp,
    radius: Dp,
    centerStartOffset: Boolean = true,
): Modifier = drawWithCache {
    val spacingPx = spacing.toPx()
    val radiusPx = radius.toPx()
    val startOffset = if (centerStartOffset) spacingPx / 2f else 0f
    onDrawBehind {
        var x = startOffset
        while (x < size.width) {
            var y = startOffset
            while (y < size.height) {
                drawCircle(dotColor, radius = radiusPx, center = Offset(x, y))
                y += spacingPx
            }
            x += spacingPx
        }
    }
}
