package eu.kanade.presentation.more.stats.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import eu.kanade.presentation.more.profile.MochikuEditProfileDialog
import eu.kanade.presentation.more.profile.MochikuIdCard
import eu.kanade.presentation.more.profile.MochikuProfileStore
import eu.kanade.presentation.more.profile.MochikuStoryRecapDialog

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

    val minChaptersRequired = 10
    val unlockProgress = (totalChaptersRead.toFloat() / minChaptersRequired).coerceIn(0f, 1f)
    val isUnlocked = totalChaptersRead >= minChaptersRequired

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Section Header & Range Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Mochiku Manga Passport & Recap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "Adapted from Mochy-Music · 100% On-Device",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E8E),
                    fontFamily = FontFamily.Monospace,
                )
            }

            // Month / Year toggle pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF141416))
                    .border(1.dp, Color(0xFF26262A), RoundedCornerShape(50))
                    .padding(2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (rangeMode == "Month") Color.White else Color.Transparent)
                        .clickable { rangeMode = "Month" }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Month",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rangeMode == "Month") Color.Black else Color(0xFF8E8E8E),
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (rangeMode == "Year") Color.White else Color.Transparent)
                        .clickable { rangeMode = "Year" }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Year",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rangeMode == "Year") Color.Black else Color(0xFF8E8E8E),
                    )
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101014)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222228)),
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
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isUnlocked) {
                                "Requirements met! Tap the card above to scan barcode, or launch recap below."
                            } else {
                                "Read at least $minChaptersRequired chapters this month to unlock full story ($totalChaptersRead / $minChaptersRequired completed)."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E8E),
                        )
                    }
                }

                // Unlock Progress Bar
                LinearProgressIndicator(
                    progress = { unlockProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (isUnlocked) Color(0xFF52FF7A) else Color.White,
                    trackColor = Color(0xFF222228),
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { showStoryDialog = true },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Story Rewind", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showReceiptDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33333C)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33333C)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Profile", fontSize = 12.sp)
                    }
                }
            }
        }

        // Reading Activity Histogram (24-Hour Binge Distribution)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101014)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222228)),
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
                        color = Color.White,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "Peak: 22:00 - 01:00",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8E8E8E),
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
                                .background(if (isPeak) Color(0xFF52FF7A) else Color(0xFF2C2C34)),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("00:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF666666))
                    Text("06:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF666666))
                    Text("12:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF666666))
                    Text("18:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF666666))
                    Text("23:00", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF666666))
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
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
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
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "MOCHIKU MANGA MART",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.Black,
                    )
                    Text(
                        text = "✦ 餅ク · FRESH STORIES DAILY ✦",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF555555),
                    )
                    Text(
                        text = "--------------------------------------",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF888888),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("READER: $username", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                        Text("TXN: #${cardNumber.takeLast(4)}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                    }
                    Text(
                        text = "--------------------------------------",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF888888),
                    )

                    val sampleItems = listOf(
                        "1x Chainsaw Man - 1.2h",
                        "2x Oshi no Ko - 0.9h",
                        "3x Frieren: Beyond Journey's End - 0.8h",
                        "4x Dandadan - 0.7h",
                        "5x Jujutsu Kaisen - 0.6h",
                    )
                    sampleItems.forEach { item ->
                        Text(
                            text = item,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = Color(0xFF222222),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Text(
                        text = "--------------------------------------",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF888888),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL CHAPTERS:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                        Text("$totalChaptersRead", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("READ DURATION:", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                        Text(readDuration, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Text(
                        text = "--------------------------------------",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF888888),
                    )
                    Text(
                        text = "END OF LINE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.Black,
                    )
                    Text(
                        text = "||||||| | ||||| |||| | ||||||||",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showReceiptDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Close Receipt", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Fullscreen Story Dialog
    if (showStoryDialog) {
        MochikuStoryRecapDialog(
            username = username,
            photoPath = photoPath,
            cardNumber = cardNumber,
            joinedDate = joinedDate,
            readDuration = readDuration,
            totalChaptersRead = totalChaptersRead,
            libraryCount = libraryCount,
            onDismissRequest = { showStoryDialog = false },
        )
    }
}
