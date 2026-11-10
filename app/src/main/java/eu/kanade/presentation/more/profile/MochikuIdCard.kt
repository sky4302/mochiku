package eu.kanade.presentation.more.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import eu.kanade.presentation.more.stats.components.PassportStampBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.domain.history.model.StampState

// KMK -->
internal const val ID_CARD_ASPECT = 1.89f
// KMK <--

@Composable
fun MochikuIdCard(
    username: String,
    photoPath: String?,
    cardNumber: String,
    joinedDate: String,
    onEditProfileClick: () -> Unit,
    onLaunchRecap: () -> Unit,
    modifier: Modifier = Modifier,
    // KMK -->
    recentStamps: List<StampState> = emptyList(),
    // KMK <--
) {
    var isFlipped by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 3D rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )

    // Scanner laser animation on back
    var isScanning by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }
    val laserProgress = remember { Animatable(0f) }

    fun triggerScanAndRecap() {
        if (isScanning) return
        isScanning = true
        isVerified = false
        scope.launch {
            laserProgress.snapTo(0f)
            laserProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            )
            isVerified = true
            delay(500)
            isScanning = false
            onLaunchRecap()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ID_CARD_ASPECT)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density.density
            }
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (!isFlipped) {
                    isFlipped = true
                } else if (!isScanning) {
                    // Tap back to trigger laser scan or flip back
                    if (!isVerified) {
                        triggerScanAndRecap()
                    } else {
                        isFlipped = false
                        isVerified = false
                    }
                }
            },
    ) {
        if (rotation <= 90f) {
            // Front Side
            // KMK -->
            MochikuIdCardFrontContent(
                username = username,
                photoPath = photoPath,
                cardNumber = cardNumber,
                joinedDate = joinedDate,
                onEditProfileClick = onEditProfileClick,
                recentStamps = recentStamps,
            )
            // KMK <--
        } else {
            // Back Side (mirrored so text reads correctly)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
            ) {
                // KMK -->
                MochikuIdCardBackContent(
                    cardNumber = cardNumber,
                    username = username,
                    isScanning = isScanning,
                    isVerified = isVerified,
                    scanProgress = laserProgress.value,
                )
                // KMK <--
            }
        }
    }
}

// KMK -->
@Composable
internal fun MochikuIdCardFrontContent(
    username: String,
    photoPath: String?,
    cardNumber: String,
    joinedDate: String,
    onEditProfileClick: (() -> Unit)? = null,
    hintText: String? = "TAP CARD TO FLIP >",
    ribbonText: String = "$username's CARD",
    disableHardwareBitmap: Boolean = false,
    onAvatarSettled: (() -> Unit)? = null,
    recentStamps: List<StampState> = emptyList(),
) {
    val context = LocalContext.current
    val editableModifier = if (onEditProfileClick != null) {
        Modifier.clickable(onClick = onEditProfileClick)
    } else {
        Modifier
    }

    val latestMiniStamps = remember(recentStamps) {
        recentStamps
            .filter { it.isUnlocked }
            .sortedByDescending { it.unlockedAt ?: 0L }
            .take(3)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 18.dp, top = 16.dp, end = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top User ID row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = editableModifier,
                ) {
                    Text(
                        text = "PROFILE USR#$cardNumber",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                    )
                    if (onEditProfileClick != null) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }

                // Middle Avatar + Name + Join date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Round Avatar
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                            .then(editableModifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (photoPath != null) {
                            val imageModel = remember(photoPath, disableHardwareBitmap) {
                                ImageRequest.Builder(context)
                                    .data(photoPath)
                                    .apply { if (disableHardwareBitmap) allowHardware(false) }
                                    .build()
                            }
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                onSuccess = { onAvatarSettled?.invoke() },
                                onError = { onAvatarSettled?.invoke() },
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile Picture",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(34.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "ID CARD OF",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = editableModifier,
                        ) {
                            Text(
                                text = username,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (onEditProfileClick != null) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Name",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }

                        Text(
                            text = "joined at $joinedDate",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Bottom hint
                if (hintText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = hintText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                }
            }

            if (latestMiniStamps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy((-12).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    latestMiniStamps.forEach { stamp ->
                        PassportStampBadge(
                            state = stamp,
                            size = 46.dp,
                            compact = true,
                        )
                    }
                }
            }
        }

        // Vertical Ribbon Strip on Right
        MarqueeRibbon(text = ribbonText)
    }
}

@Composable
internal fun MochikuIdCardBackContent(
    cardNumber: String,
    username: String,
    isScanning: Boolean = false,
    isVerified: Boolean = false,
    scanProgress: Float = 0f,
    barcodePayload: String = "MFY2026-11 $cardNumber",
    barcodeLabel: String = "MFY 2026 - 11  ${cardNumber.takeLast(8).chunked(4).joinToString(" ")}",
    statusText: String? = if (isVerified) {
        "OPENING RECAP..."
    } else if (isScanning) {
        "SCANNING BARCODE..."
    } else {
        "TAP TO SCAN & UNLOCK RECAP"
    },
    ribbonText: String = "$username's CARD",
) {
    val barcodeBits = remember(barcodePayload) {
        Code128Encoder.encode(barcodePayload)
    }
    val barcodeBarColor = MaterialTheme.colorScheme.onSurface

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Barcode Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(82.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawBarcodePattern(
                            bits = barcodeBits,
                            isVerified = isVerified,
                            isScanning = isScanning,
                            scanProgress = scanProgress,
                            barColor = barcodeBarColor,
                        )
                    }

                    // Green "VERIFIED" Stamp Badge
                    if (isVerified) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0A2412),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF52FF7A)),
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -6f
                                    scaleX = 1.05f
                                    scaleY = 1.05f
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF52FF7A),
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "VERIFIED",
                                    color = Color(0xFF52FF7A),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = barcodeLabel,
                    color = if (isVerified) Color(0xFF52FF7A) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )

                if (statusText != null) {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = statusText,
                        color = if (isVerified || isScanning) Color(0xFF52FF7A) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        // Vertical Ribbon Strip on Right
        MarqueeRibbon(text = ribbonText)
    }
}
// KMK <--

@Composable
private fun MarqueeRibbon(text: String) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "• $text • $text • ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.graphicsLayer {
                rotationZ = 90f
            },
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun DrawScope.drawBarcodePattern(
    bits: BooleanArray,
    isVerified: Boolean,
    isScanning: Boolean,
    scanProgress: Float,
    barColor: Color,
) {
    if (bits.isEmpty()) return
    val barWidth = size.width / bits.size
    val activeColor = if (isVerified) Color(0xFF52FF7A) else barColor

    // Draw base barcode
    for (i in bits.indices) {
        if (bits[i]) {
            drawRect(
                color = activeColor,
                topLeft = Offset(i * barWidth, 0f),
                size = Size(barWidth, size.height),
            )
        }
    }

    // Laser Sweep and Brackets
    if (isScanning && !isVerified) {
        val sweepY = size.height * scanProgress
        val laserColor = Color(0xFF52FF7A)

        // Corner scan brackets
        val bracketArm = 14.dp.toPx()
        val bracketStroke = 2.5.dp.toPx()
        val pad = 4.dp.toPx()

        // Top-left
        drawLine(laserColor, Offset(-pad, -pad), Offset(-pad + bracketArm, -pad), bracketStroke)
        drawLine(laserColor, Offset(-pad, -pad), Offset(-pad, -pad + bracketArm), bracketStroke)
        // Top-right
        drawLine(laserColor, Offset(size.width + pad, -pad), Offset(size.width + pad - bracketArm, -pad), bracketStroke)
        drawLine(laserColor, Offset(size.width + pad, -pad), Offset(size.width + pad, -pad + bracketArm), bracketStroke)
        // Bottom-left
        drawLine(laserColor, Offset(-pad, size.height + pad), Offset(-pad + bracketArm, size.height + pad), bracketStroke)
        drawLine(laserColor, Offset(-pad, size.height + pad), Offset(-pad, size.height + pad - bracketArm), bracketStroke)
        // Bottom-right
        drawLine(laserColor, Offset(size.width + pad, size.height + pad), Offset(size.width + pad - bracketArm, size.height + pad), bracketStroke)
        drawLine(laserColor, Offset(size.width + pad, size.height + pad), Offset(size.width + pad, size.height + pad - bracketArm), bracketStroke)

        // Laser beam gradient
        val trailHeight = 18.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(laserColor.copy(alpha = 0f), laserColor.copy(alpha = 0.4f)),
                startY = sweepY - trailHeight,
                endY = sweepY,
            ),
            topLeft = Offset(0f, sweepY - trailHeight),
            size = Size(size.width, trailHeight),
        )

        // Bright laser center line
        drawLine(
            color = laserColor,
            start = Offset(-6.dp.toPx(), sweepY),
            end = Offset(size.width + 6.dp.toPx(), sweepY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(0f, sweepY),
            end = Offset(size.width, sweepY),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

/** Code 128 Pattern Encoder (Matches Mochy-Music exact barcode specification) */
private object Code128Encoder {
    private const val START_B = 104
    private const val STOP = 0b1100011101011

    private val PATTERNS = intArrayOf(
        0b11011001100, 0b11001101100, 0b11001100110, 0b10010011000, 0b10010001100, 0b10001001100, 0b10011001000, 0b10011000100,
        0b10001100100, 0b11001001000, 0b11001000100, 0b11000100100, 0b10110011100, 0b10011011100, 0b10011001110, 0b10111001100,
        0b10011101100, 0b10011100110, 0b11001110010, 0b11001011100, 0b11001001110, 0b11011100100, 0b11001110100, 0b11101101110,
        0b11101001100, 0b11100101100, 0b11100100110, 0b11101100100, 0b11100110100, 0b11100110010, 0b11011011000, 0b11011000110,
        0b11000110110, 0b10100011000, 0b10001011000, 0b10001000110, 0b10110001000, 0b10001101000, 0b10001100010, 0b11010001000,
        0b11000101000, 0b11000100010, 0b10110111000, 0b10110001110, 0b10001101110, 0b10111011000, 0b10111000110, 0b10001110110,
        0b11101110110, 0b11010001110, 0b11000101110, 0b11011101000, 0b11011100010, 0b11011101110, 0b11101011000, 0b11101000110,
        0b11100010110, 0b11101101000, 0b11101100010, 0b11100011010, 0b11101111010, 0b11001000010, 0b11110001010, 0b10100110000,
        0b10100001100, 0b10010110000, 0b10010000110, 0b10000101100, 0b10000100110, 0b10110010000, 0b10110000100, 0b10011010000,
        0b10011000010, 0b10000110100, 0b10000110010, 0b11000010010, 0b11001010000, 0b11110111010, 0b11000010100, 0b10001111010,
    )

    fun encode(text: String): BooleanArray {
        val values = ArrayList<Int>(text.length + 3)
        values += START_B
        var checksum = START_B
        text.filter { it.code in 32..126 }.forEachIndexed { i, ch ->
            val v = ch.code - 32
            values += v
            checksum += (i + 1) * v
        }
        values += checksum % 103

        val bits = ArrayList<Boolean>(values.size * 11 + 13)
        values.forEach { v ->
            val pattern = if (v in PATTERNS.indices) PATTERNS[v] else PATTERNS[0]
            for (b in 10 downTo 0) {
                bits += ((pattern shr b) and 1) == 1
            }
        }
        for (b in 12 downTo 0) {
            bits += ((STOP shr b) and 1) == 1
        }
        return bits.toBooleanArray()
    }
}
