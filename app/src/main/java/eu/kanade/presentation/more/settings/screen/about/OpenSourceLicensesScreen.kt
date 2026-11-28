package eu.kanade.presentation.more.settings.screen.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.MochikuPillCard
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.R
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.plus

class OpenSourceLicensesScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.licenses),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            val libraries by produceLibraries(R.raw.aboutlibraries)
            val libraryList = libraries?.libraries.orEmpty()
            FastScrollLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding + PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = libraryList,
                    key = { it.uniqueId },
                ) { library ->
                    MochikuPillCard(
                        cornerRadius = 18.dp,
                        onClick = {
                            navigator.push(
                                OpenSourceLibraryLicenseScreen(
                                    name = library.name,
                                    website = library.website,
                                    license = library.licenses.firstOrNull()?.htmlReadyLicenseContent.orEmpty(),
                                ),
                            )
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = library.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val subtitle = library.description?.takeIf { it.isNotBlank() }
                                    ?: library.website?.takeIf { it.isNotBlank() }
                                if (!subtitle.isNullOrBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            library.artifactVersion?.takeIf { it.isNotBlank() }?.let { version ->
                                Text(
                                    text = version,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
