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
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

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
    var rangeMode by remember { mutableStateOf("Month") }
    var showPassportDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showStoryDialog by remember { mutableStateOf(false) }

    val minChaptersRequired = 10
    val unlockProgress = (totalChaptersRead.toFloat() / minChaptersRequired).coerceIn(0f, 1f)
    val isUnlocked = totalChaptersRead >= minChaptersRequired

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Section Header & Range Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Mochiku Recap Gimmick",
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

        // Signature Mochy Barcode Unlock Card
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
                            text = if (rangeMode == "Month") "November 2026 Rewind" else "Year 2026 Rewind",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isUnlocked) {
                                "Requirements met! Tap Launch to view your full story recap."
                            } else {
                                "Read at least $minChaptersRequired chapters this month to unlock full story ($totalChaptersRead / $minChaptersRequired completed)."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E8E),
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Minimal Barcode Graphic
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.padding(2.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "|||| | ||||| ||",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black,
                            )
                            Text(
                                text = "MOCHIKU",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp,
                                color = Color.Black,
                            )
                        }
                    }
                }

                // Unlock Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { unlockProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Color.White,
                        trackColor = Color(0xFF222228),
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { showStoryDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                        ),
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
                        onClick = { showPassportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333338)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("3D Passport", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showReceiptDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333338)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }

        // 24-Hour Timeline Distribution Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0E12)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E1E24)),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Reading Time by Hour of Day",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Peak: 01:00 AM 🌙",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8E8E8E),
                    )
                }

                // 24 Hour Histogram Simulation (Mochy-Music Style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val sampleHours = listOf(
                        10, 8, 4, 2, 0, 0, 2, 8, 14, 18, 12, 10,
                        15, 20, 25, 30, 22, 18, 25, 35, 45, 60, 80, 55
                    )
                    sampleHours.forEachIndexed { hr, value ->
                        val isPeak = hr == 22 || hr == 23 || hr == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((value * 0.45).dp)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(if (isPeak) Color.White else Color(0xFF2C2C34)),
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

    // 1. 3D Otaku Passport Dialog
    if (showPassportDialog) {
        Dialog(onDismissRequest = { showPassportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0C0C0E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "OTAKU PASSPORT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                        Text(
                            text = "TIER: S-RANK",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF8E8E8E),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E24))
                            .border(2.dp, Color(0xFF3E3E4C), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🍡", fontSize = 32.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Arif Fahmi",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White,
                        )
                        Text(
                            text = "Night Owl Binger 🌙",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFCCCCCC),
                        )
                        Text(
                            text = "ID: #MKU-2026-8492",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFF777777),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF141418))
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CHAPTERS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF888888))
                            Text("$totalChaptersRead", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DURATION", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF888888))
                            Text(readDuration, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TITLES", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF888888))
                            Text("$libraryCount", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Button(
                        onClick = { showPassportDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 2. Vintage Thermal Paper Receipt Dialog
    if (showReceiptDialog) {
        Dialog(onDismissRequest = { showReceiptDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFAFAF9),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "MOCHIKU MANGA MART",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.Black,
                    )
                    Text(
                        text = "✦ 餅ク · FRESH STORIES DAILY ✦",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF555555),
                    )
                    Text(
                        text = "--------------------------------",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF888888),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("READER: Arif Fahmi", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                        Text("TXN: #8492", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                    }
                    Text(
                        text = "--------------------------------",
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
                        text = "--------------------------------",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF888888),
                    )
                    Text(
                        text = "||||||| | ||||| |||| | ||||||||",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                    Text(
                        text = "THANK YOU FOR DEVOURING STORIES!",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color(0xFF666666),
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

    // 3. Cinematic Story Recap Dialog
    if (showStoryDialog) {
        Dialog(onDismissRequest = { showStoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF08080A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24242C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MOCHIKU REWIND",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF8E8E8E),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your Reading Summary",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = readDuration,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                        )
                        Text(
                            text = "$totalChaptersRead Chapters Cleared",
                            fontSize = 13.sp,
                            color = Color(0xFFCCCCCC),
                        )
                        Text(
                            text = "You are in the top 3% of readers this period 👑",
                            fontSize = 11.sp,
                            color = Color(0xFF8E8E8E),
                        )
                    }

                    Button(
                        onClick = { showStoryDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
