package eu.kanade.presentation.more.settings.screen.about

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.LogoHeader
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.LocalBackPress
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.updater.AppUpdateChecker
import eu.kanade.tachiyomi.ui.more.ComingUpdatesScreen
import eu.kanade.tachiyomi.ui.more.NewUpdateScreen
import eu.kanade.tachiyomi.ui.more.WhatsNewScreen
import eu.kanade.tachiyomi.util.CrashLogUtil
import eu.kanade.tachiyomi.util.lang.toDateTimestampString
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.isDebugBuildType
import eu.kanade.tachiyomi.util.system.isPreviewBuildType
import eu.kanade.tachiyomi.util.system.isReleaseBuildType
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.updaterEnabled
import kotlinx.coroutines.launch
import logcat.LogPriority
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.release.interactor.GetApplicationRelease
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.components.LinkIcon
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.icons.CustomIcons
import tachiyomi.presentation.core.icons.Discord
import tachiyomi.presentation.core.icons.Github
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class AboutScreen : Screen() {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val handleBack = LocalBackPress.current
        val navigator = LocalNavigator.currentOrThrow
        var isCheckingUpdates by remember { mutableStateOf(false) }

        // KMK -->
        var isCheckingWhatsNew by remember { mutableStateOf(false) }
        var isCheckingWhatsComing by remember { mutableStateOf(false) }
        // KMK <--

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.pref_category_about),
                    navigateUp = if (handleBack != null) handleBack::invoke else null,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            ScrollbarLazyColumn(
                contentPadding = contentPadding,
            ) {
                item {
                    LogoHeader()
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Honest Attribution · Fork of Komikku",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Mochiku is an open-source fork of Komikku (built upon Tachiyomi & Mihon). All core manga reading engines, extension loaders, parsers, and tracker integrations were built by the Komikku and Tachiyomi contributors.\n\nMochiku provides a custom Mochy-Music OLED dark UI, floating capsule navigation, and Manga Story Recap.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }

                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.version),
                        subtitle = getVersionName(withBuildDate = true),
                        onPreferenceClick = {
                            val deviceInfo = CrashLogUtil(context).getDebugInfo()
                            context.copyToClipboard("Debug information", deviceInfo)
                        },
                    )
                }

                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.check_for_updates),
                        widget = {
                            AnimatedVisibility(visible = isCheckingUpdates) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        },
                        onPreferenceClick = {
                            if (!isCheckingUpdates) {
                                scope.launch {
                                    isCheckingUpdates = true

                                    checkVersion(
                                        context = context,
                                        onAvailableUpdate = { result ->
                                            val updateScreen = NewUpdateScreen(
                                                versionName = result.release.version,
                                                changelogInfo = result.release.info,
                                                releaseLink = result.release.releaseLink,
                                                downloadLink = result.release.downloadLink,
                                            )
                                            navigator.push(updateScreen)
                                        },
                                        onFinish = {
                                            isCheckingUpdates = false
                                        },
                                    )
                                }
                            }
                        },
                    )
                }

                // KMK -->
                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.whats_new),
                        widget = {
                            AnimatedVisibility(visible = isCheckingWhatsNew) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        },
                        onPreferenceClick = {
                            if (!isCheckingWhatsNew) {
                                scope.launch {
                                    isCheckingWhatsNew = true

                                    getReleaseNotes(
                                        context = context,
                                        onAvailableUpdate = { result ->
                                            val whatsNewScreen = WhatsNewScreen(
                                                currentVersion = BuildConfig.VERSION_NAME,
                                                versionName = result.release.version,
                                                changelogInfo = result.release.info,
                                                releaseLink = result.release.releaseLink,
                                            )
                                            navigator.push(whatsNewScreen)
                                        },
                                        onFinish = {
                                            isCheckingWhatsNew = false
                                        },
                                    )
                                }
                            }
                        },
                    )
                }

                if (isReleaseBuildType || isDebugBuildType) {
                    item {
                        TextPreferenceWidget(
                            title = stringResource(KMR.strings.whats_coming),
                            widget = {
                                AnimatedVisibility(visible = isCheckingWhatsComing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp,
                                    )
                                }
                            },
                            onPreferenceClick = {
                                if (!isCheckingWhatsComing) {
                                    scope.launch {
                                        isCheckingWhatsComing = true

                                        checkVersion(
                                            context = context,
                                            onAvailableUpdate = { result ->
                                                val updateScreen = ComingUpdatesScreen(
                                                    versionName = result.release.version,
                                                    changelogInfo = result.release.info,
                                                    releaseLink = result.release.releaseLink,
                                                    downloadLink = result.release.downloadLink,
                                                )
                                                navigator.push(updateScreen)
                                            },
                                            onFinish = {
                                                isCheckingWhatsComing = false
                                            },
                                            peekIntoPreview = true,
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
                // KMK <--

                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.help_translate),
                        onPreferenceClick = {
                            uriHandler.openUri(
                                "https://github.com/sky4302/mochiku",
                            )
                        },
                    )
                }

                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.licenses),
                        onPreferenceClick = { navigator.push(OpenSourceLicensesScreen()) },
                    )
                }

                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.privacy_policy),
                        onPreferenceClick = { uriHandler.openUri("https://github.com/sky4302/mochiku") },
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    ) {
                        LinkIcon(
                            label = "Mochiku",
                            icon = CustomIcons.Github,
                            url = "https://github.com/sky4302/mochiku",
                        )
                        LinkIcon(
                            label = "Komikku (Upstream)",
                            icon = CustomIcons.Github,
                            url = "https://github.com/komikku-app/komikku",
                        )
                    }
                }
            }
        }
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private suspend fun checkVersion(
        context: Context,
        onAvailableUpdate: (GetApplicationRelease.Result.NewUpdate) -> Unit,
        onFinish: () -> Unit,
        // KMK -->
        peekIntoPreview: Boolean = false,
        // KMK <--
    ) {
        val updateChecker = AppUpdateChecker(
            // KMK -->
            peekIntoPreview = peekIntoPreview,
            // KMK <--
        )
        withUIContext {
            try {
                when (val result = withIOContext { updateChecker.checkForUpdate(context, forceCheck = true) }) {
                    is GetApplicationRelease.Result.NewUpdate -> {
                        onAvailableUpdate(result)
                    }
                    is GetApplicationRelease.Result.NoNewUpdate -> {
                        context.toast(MR.strings.update_check_no_new_updates)
                    }
                    is GetApplicationRelease.Result.OsTooOld -> {
                        context.toast(MR.strings.update_check_eol)
                    }
                }
            } catch (e: Exception) {
                context.toast(e.message)
                logcat(LogPriority.ERROR, e)
            } finally {
                onFinish()
            }
        }
    }

    companion object {
        // KMK -->
        suspend fun getReleaseNotes(
            context: Context,
            onAvailableUpdate: (GetApplicationRelease.Result.NewUpdate) -> Unit,
            onFinish: () -> Unit,
        ) {
            val updateChecker = AppUpdateChecker()
            withUIContext {
                try {
                    when (val result = withIOContext { updateChecker.getReleaseNotes() }) {
                        is GetApplicationRelease.Result.NewUpdate -> {
                            onAvailableUpdate(result)
                        }

                        else -> {}
                    }
                } catch (e: Exception) {
                    context.toast(e.message)
                    logcat(LogPriority.ERROR, e)
                } finally {
                    onFinish()
                }
            }
        }
        // KMK <--

        fun getVersionName(withBuildDate: Boolean): String {
            val base = "v${BuildConfig.VERSION_NAME}"
            return if (withBuildDate) {
                "$base • Build ${BuildConfig.VERSION_CODE} (${getFormattedBuildTime()})"
            } else {
                base
            }
        }

        internal fun getFormattedBuildTime(): String {
            return try {
                LocalDateTime.ofInstant(
                    Instant.parse(BuildConfig.BUILD_TIME),
                    ZoneId.systemDefault(),
                )
                    .toDateTimestampString(
                        UiPreferences.dateFormat(
                            Injekt.get<UiPreferences>().dateFormat().get(),
                        ),
                    )
            } catch (_: Exception) {
                BuildConfig.BUILD_TIME
            }
        }
    }
}
