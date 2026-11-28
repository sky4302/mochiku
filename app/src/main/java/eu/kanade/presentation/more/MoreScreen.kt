package eu.kanade.presentation.more

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.profile.MochikuProfileHeader
import eu.kanade.presentation.more.settings.widget.MochikuCircleIconBadge
import eu.kanade.presentation.more.settings.widget.MochikuPillCard
import eu.kanade.presentation.more.settings.widget.MochikuPillPreferenceRow
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import eu.kanade.tachiyomi.util.system.openInBrowser
import exh.pref.DelegateSourcePreferences
import exh.source.ExhPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.core.common.Constants
import tachiyomi.domain.history.model.StampState
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.TextButton
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    // SY -->
    showNavUpdates: Boolean,
    showNavHistory: Boolean,
    // SY <--
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickBatchAdd: () -> Unit,
    onClickUpdates: () -> Unit,
    onClickHistory: () -> Unit,
    // KMK -->
    onClickLibraryUpdateErrors: () -> Unit,
    passportStamps: ImmutableList<StampState> = persistentListOf(),
    // KMK <--
) {
    val uriHandler = LocalUriHandler.current
    // SY -->
    val exhPreferences = remember { Injekt.get<ExhPreferences>() }
    val delegateSourcePreferences = remember { Injekt.get<DelegateSourcePreferences>() }
    // SY <--

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val pullThresholdPx = remember(density) { with(density) { 76.dp.toPx() } }
    val maxPullPx = remember(density) { with(density) { 120.dp.toPx() } }
    val pullOffsetPx = remember { Animatable(0f) }
    var armedHapticFired by remember { mutableStateOf(false) }
    var pullLaunchNonce by remember { mutableIntStateOf(0) }

    val pullDownConnection = remember(listState, pullThresholdPx, maxPullPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && pullOffsetPx.value > 0f && available.y < 0f) {
                    val consumedY = available.y.coerceAtLeast(-pullOffsetPx.value)
                    val next = (pullOffsetPx.value + consumedY).coerceAtLeast(0f)
                    if (next < pullThresholdPx) armedHapticFired = false
                    scope.launch { pullOffsetPx.snapTo(next) }
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                if (source == NestedScrollSource.UserInput && atTop && available.y > 0f) {
                    val next = (pullOffsetPx.value + available.y * 0.45f).coerceIn(0f, maxPullPx)
                    if (next >= pullThresholdPx && !armedHapticFired) {
                        armedHapticFired = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    scope.launch { pullOffsetPx.snapTo(next) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pullOffsetPx.value > 0f) {
                    val shouldLaunch = pullOffsetPx.value >= pullThresholdPx
                    armedHapticFired = false
                    scope.launch {
                        if (shouldLaunch) {
                            launch {
                                delay(90)
                                pullLaunchNonce++
                            }
                        }
                        pullOffsetPx.animateTo(0f, spring(dampingRatio = 0.86f, stiffness = 260f))
                    }
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    Scaffold { contentPadding ->
        val colorScheme = MaterialTheme.colorScheme
        val layoutDirection = LocalLayoutDirection.current
        Box(modifier = Modifier.fillMaxSize()) {
            ScrollbarLazyColumn(
                modifier = Modifier.nestedScroll(pullDownConnection),
                state = listState,
                // Top padding passes directly into MochikuProfileHeader so the dotted hero extends edge-to-edge
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    MochikuProfileHeader(
                        onClickStats = onClickStats,
                        recentStamps = passportStamps,
                        topPadding = contentPadding.calculateTopPadding(),
                        pullOffsetProvider = { pullOffsetPx.value },
                        pullThresholdPx = pullThresholdPx,
                        pullLaunchNonce = pullLaunchNonce,
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MochikuMoreQuickTogglePill(
                            title = stringResource(MR.strings.label_downloaded_only),
                            icon = Icons.Outlined.CloudOff,
                            checked = downloadedOnly,
                            onCheckedChange = onDownloadedOnlyChange,
                            modifier = Modifier.weight(1f),
                        )
                        MochikuMoreQuickTogglePill(
                            title = stringResource(MR.strings.pref_incognito_mode),
                            icon = rememberAnimatedVectorPainter(
                                AnimatedImageVector.animatedVectorResource(R.drawable.anim_incognito),
                                incognitoMode,
                            ),
                            checked = incognitoMode,
                            onCheckedChange = onIncognitoModeChange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // SY -->
                if (!showNavUpdates) {
                    item {
                        MochikuPillPreferenceRow(
                            title = stringResource(MR.strings.label_recent_updates),
                            icon = Icons.Outlined.NewReleases,
                            onClick = onClickUpdates,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                if (!showNavHistory) {
                    item {
                        MochikuPillPreferenceRow(
                            title = stringResource(MR.strings.label_recent_manga),
                            icon = Icons.Outlined.History,
                            onClick = onClickHistory,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                // SY <--

                item {
                    val downloadQueueState = downloadQueueStateProvider()
                    MochikuPillPreferenceRow(
                        title = stringResource(MR.strings.label_download_queue),
                        subtitle = when (downloadQueueState) {
                            DownloadQueueState.Stopped -> null
                            is DownloadQueueState.Paused -> {
                                val pending = downloadQueueState.pending
                                if (pending == 0) {
                                    stringResource(MR.strings.paused)
                                } else {
                                    "${stringResource(MR.strings.paused)} • ${
                                        pluralStringResource(
                                            MR.plurals.download_queue_summary,
                                            count = pending,
                                            pending,
                                        )
                                    }"
                                }
                            }
                            is DownloadQueueState.Downloading -> {
                                val pending = downloadQueueState.pending
                                pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                            }
                        },
                        icon = Icons.Outlined.GetApp,
                        onClick = onClickDownloadQueue,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                item {
                    MochikuPillPreferenceRow(
                        title = stringResource(MR.strings.categories),
                        icon = Icons.AutoMirrored.Outlined.Label,
                        onClick = onClickCategories,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                item {
                    MochikuPillPreferenceRow(
                        title = stringResource(MR.strings.label_stats),
                        icon = Icons.Outlined.QueryStats,
                        onClick = onClickStats,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                // KMK -->
                item {
                    MochikuPillPreferenceRow(
                        title = stringResource(KMR.strings.option_label_library_update_errors),
                        icon = Icons.Outlined.NewReleases,
                        onClick = onClickLibraryUpdateErrors,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                // KMK <--
                item {
                    MochikuPillPreferenceRow(
                        title = stringResource(MR.strings.label_data_storage),
                        icon = Icons.Outlined.Storage,
                        onClick = onClickDataAndStorage,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                // SY -->
                if (exhPreferences.isHentaiEnabled().get() || delegateSourcePreferences.delegateSources().get()) {
                    item {
                        MochikuPillPreferenceRow(
                            title = stringResource(SYMR.strings.eh_batch_add),
                            icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                            onClick = onClickBatchAdd,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                // SY <--

                item {
                    MochikuPillPreferenceRow(
                        title = stringResource(MR.strings.label_settings),
                        icon = Icons.Outlined.Settings,
                        onClick = onClickSettings,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                item {
                    MochikuPillPreferenceRow(
                        title = stringResource(MR.strings.pref_category_about),
                        icon = Icons.Outlined.Info,
                        onClick = onClickAbout,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                item {
                    MochikuPillPreferenceRow(
                        title = stringResource(MR.strings.label_help),
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        onClick = { uriHandler.openUri(Constants.URL_HELP) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                // KMK -->
                item {
                    Sponsor()
                }
                // KMK <--
            }

            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val statusBarBg = colorScheme.background
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight + 16.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to statusBarBg,
                            0.65f to statusBarBg.copy(alpha = 0.88f),
                            1f to Color.Transparent,
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun MochikuMoreQuickTogglePill(
    title: String,
    icon: Any,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    MochikuPillCard(
        modifier = modifier,
        onClick = { onCheckedChange(!checked) },
        cornerRadius = 20.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MochikuCircleIconBadge(
                icon = icon,
                badgeSize = 32.dp,
                iconSize = 17.dp,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.scale(0.82f),
            )
        }
    }
}

// KMK -->
@Composable
fun Sponsor() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.padding.medium),
        horizontalArrangement = Arrangement.Center,
    ) {
        TextButton(
            onClick = { context.openInBrowser(Constants.SPONSOR) },
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(KMR.strings.sponsor_me),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(KMR.strings.sponsor_me),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SponsorPreview() {
    TachiyomiPreviewTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Sponsor()
        }
    }
}
// KMK <--
