package eu.kanade.presentation.more.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import eu.kanade.presentation.more.profile.MochikuEditProfileDialog
import eu.kanade.presentation.more.profile.MochikuIdCard
import eu.kanade.presentation.more.profile.MochikuProfileStore
import eu.kanade.presentation.more.profile.MochikuRecapData
import eu.kanade.presentation.more.profile.MochikuRecapDataEngine
import eu.kanade.presentation.more.profile.MochikuStoryRecapDialog
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Mochiku Manga Recap & Otaku Passport Gimmick Component (adapted from Mochy-Music).
 * Zero gradients: pure OLED blacks, frosted dark acrylic cards, white/gray typography.
 */
@Composable
fun MochikuRecapSection(
    readDuration: String,
    totalChaptersRead: Int,
    libraryCount: Int,
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf(MochikuProfileStore.getUsername(context)) }
    var photoPath by remember { mutableStateOf(MochikuProfileStore.getPhotoPath(context)) }
    val cardNumber = remember { MochikuProfileStore.getCardNumber(context) }
    val joinedDate = remember { MochikuProfileStore.getJoinedDate(context) }

    var rangeMode by remember { mutableStateOf("Month") }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showStoryDialog by remember { mutableStateOf(false) }
    // KMK -->
    var shareInitialPage by remember { mutableStateOf<Int?>(null) }
    // KMK <--

    // Load real reading history for current month
    val recapData by produceState<MochikuRecapData?>(initialValue = null) {
        value = MochikuRecapDataEngine.loadCurrentMonthRecap()
    }
    val effectiveChapters = recapData?.monthChaptersRead ?: totalChaptersRead
    val effectiveDuration = recapData?.monthDurationFormatted ?: readDuration
    val effectiveLibrary = recapData?.libraryCount ?: libraryCount

    // KMK -->
    val shareData = remember(
        recapData,
        username,
        photoPath,
        cardNumber,
        joinedDate,
        effectiveChapters,
        effectiveDuration,
        effectiveLibrary,
    ) {
        val now = LocalDate.now()
        val monthName = recapData?.monthName ?: now.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val year = recapData?.year ?: now.year
        RecapShareData(
            monthLabel = "$monthName $year",
            yearMonthCode = String.format(Locale.US, "%d-%02d", year, now.monthValue),
            username = username,
            photoPath = photoPath,
            cardNumber = cardNumber,
            joinedDate = joinedDate,
            chaptersRead = effectiveChapters,
            durationFormatted = effectiveDuration,
            libraryCount = effectiveLibrary,
            topMangaList = recapData?.topMangaList?.map {
                RecapShareMangaRow(
                    title = it.title,
                    durationFormatted = it.durationFormatted,
                    chaptersRead = it.chaptersRead,
                )
            }.orEmpty(),
        )
    }
    // KMK <--

    val minChaptersRequired = 10
    val unlockProgress = (effectiveChapters.toFloat() / minChaptersRequired).coerceIn(0f, 1f)
    val isUnlocked = effectiveChapters >= minChaptersRequired

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Section Header & Range Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            ) {
                Text(
                    text = "Mochiku Manga Passport & Recap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Adapted from Mochy-Music · 100% On-Device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }

            // Month / Year toggle pill
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(2.dp),
            ) {
                listOf("Month", "Year").forEach { mode ->
                    val selected = rangeMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { rangeMode = mode }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = mode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // The Signature Mochy 3D ID Card with Tap-to-Flip and Laser Scanning Barcode
        MochikuIdCard(
            username = username,
            photoPath = photoPath,
            cardNumber = cardNumber,
            joinedDate = joinedDate,
            onEditProfileClick = { showEditProfileDialog = true },
            onLaunchRecap = { showStoryDialog = true },
        )

        // Barcode Unlock Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (rangeMode == "Month") "Monthly Rewind Verification" else "Annual Rewind Verification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isUnlocked) {
                                "Requirements met! Tap the card above to scan barcode, or launch recap below."
                            } else {
                                "Read at least $minChaptersRequired chapters this month to unlock full story ($effectiveChapters / $minChaptersRequired completed)."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // KMK -->
                    IconButton(onClick = { shareInitialPage = 1 }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(KMR.strings.action_share_recap),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // KMK <--
                }

                // Unlock Progress Bar
                LinearProgressIndicator(
                    progress = { unlockProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { showStoryDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Story Rewind",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RecapSecondaryButton(
                            label = "Receipt",
                            icon = Icons.Outlined.Receipt,
                            onClick = { showReceiptDialog = true },
                            modifier = Modifier.weight(1f),
                        )
                        RecapSecondaryButton(
                            label = "Edit Profile",
                            icon = Icons.Outlined.Edit,
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Reading Activity Histogram (24-Hour Binge Distribution)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "24-Hour Reading Tempo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "Peak: 22:00 - 01:00",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // 24 Hour Bar chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val sampleHours = listOf(
                        10, 8, 4, 2, 0, 0, 2, 8, 14, 18, 12, 10,
                        15, 20, 25, 30, 22, 18, 25, 35, 45, 60, 80, 55,
                    )
                    sampleHours.forEachIndexed { hr, value ->
                        val isPeak = hr == 22 || hr == 23 || hr == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((value * 0.45).dp)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(if (isPeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("00:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("06:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("12:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("18:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("23:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
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

    // Vintage Thermal Paper Receipt Dialog with serrated edge
    if (showReceiptDialog) {
        Dialog(onDismissRequest = { showReceiptDialog = false }) {
            // KMK -->
            MochikuThermalReceiptPaper(
                username = username,
                cardNumberSuffix = cardNumber.takeLast(4),
                topMangaList = shareData.topMangaList,
                chaptersRead = effectiveChapters,
                durationFormatted = effectiveDuration,
                modifier = Modifier.fillMaxWidth(0.92f),
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { showReceiptDialog = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(MR.strings.action_close),
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                    Button(
                        onClick = {
                            showReceiptDialog = false
                            shareInitialPage = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(KMR.strings.action_share_recap),
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
            // KMK <--
        }
    }

    // KMK -->
    if (shareInitialPage != null) {
        RecapSharePreviewDialog(
            data = shareData,
            initialPage = shareInitialPage!!,
            onDismissRequest = { shareInitialPage = null },
        )
    }
    // KMK <--

    // Fullscreen Story Dialog
    if (showStoryDialog) {
        MochikuStoryRecapDialog(
            username = username,
            photoPath = photoPath,
            cardNumber = cardNumber,
            joinedDate = joinedDate,
            readDuration = effectiveDuration,
            totalChaptersRead = effectiveChapters,
            libraryCount = effectiveLibrary,
            onDismissRequest = { showStoryDialog = false },
        )
    }
}

/** Single-line outlined action button used below the primary Story Rewind CTA. */
@Composable
private fun RecapSecondaryButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
        )
    }
}
