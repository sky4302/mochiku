package eu.kanade.presentation.more.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf(MochikuProfileStore.getUsername(context)) }
    var photoPath by remember { mutableStateOf(MochikuProfileStore.getPhotoPath(context)) }
    val cardNumber = remember { MochikuProfileStore.getCardNumber(context) }
    val joinedDate = remember { MochikuProfileStore.getJoinedDate(context) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showStoryRecapDialog by remember { mutableStateOf(false) }

    // Read live stats asynchronously from database
    val recapData by produceState<MochikuRecapData?>(initialValue = null) {
        value = MochikuRecapDataEngine.loadCurrentMonthRecap()
    }

    val displayDuration = recapData?.monthDurationFormatted ?: "0h 0m"
    val displayChapters = recapData?.monthChaptersRead ?: 0
    val displayLibraryCount = recapData?.libraryCount ?: 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Mochiku Manga Profile",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }

            OutlinedButton(
                onClick = { showEditProfileDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
            }
        }

        // The Signature Mochy 3D ID Card
        MochikuIdCard(
            username = username,
            photoPath = photoPath,
            cardNumber = cardNumber,
            joinedDate = joinedDate,
            onEditProfileClick = { showEditProfileDialog = true },
            onLaunchRecap = { showStoryRecapDialog = true },
            recentStamps = recentStamps,
        )

        // Reading Time Card (Matches Mochy: "2h 9m Total Listening time | View Stats >")
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )

                    Column {
                        Text(
                            text = displayDuration,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "This Month · $displayChapters ch read",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp),
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

    // Fullscreen Story Recap Dialog
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
        )
    }
}
