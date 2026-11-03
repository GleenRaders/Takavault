package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BanknoteCatalog
import com.example.ui.theme.BkashPink
import com.example.ui.theme.DbblBlue
import com.example.ui.theme.DbblRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.util.BanglaFormatters
import com.example.util.CustomAssetManager

@Composable
fun AppBrandLogo(
    modifier: Modifier = Modifier.size(36.dp)
) {
    val context = LocalContext.current
    val customBitmap = CustomAssetManager.getCustomBitmap(context, CustomAssetManager.KEY_LOGO_APP)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.25f),
                        NeonMint.copy(alpha = 0.2f),
                        BkashPink.copy(alpha = 0.25f)
                    )
                )
            )
            .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = "App Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Spacer(
                modifier = Modifier
                    .size(18.dp)
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val stroke = Stroke(width = 2.dp.toPx())
                        val path = Path().apply {
                            moveTo(w * 0.5f, h * 0.08f)
                            lineTo(w * 0.08f, h * 0.29f)
                            lineTo(w * 0.5f, h * 0.5f)
                            lineTo(w * 0.92f, h * 0.29f)
                            close()
                        }
                        onDrawBehind {
                            drawPath(path, NeonCyan, style = stroke)
                        }
                    }
            )
        }
    }
}

@Composable
fun BkashOrigamiLogo(
    modifier: Modifier = Modifier.size(40.dp)
) {
    val context = LocalContext.current
    val customBitmap = CustomAssetManager.getCustomBitmap(context, CustomAssetManager.KEY_LOGO_BKASH)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFE2136E), RoundedCornerShape(12.dp))
            .padding(if (customBitmap != null) 0.dp else 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = "bKash Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height

                        val p1 = Path().apply {
                            moveTo(w * 0.12f, h * 0.18f)
                            lineTo(w * 0.54f, h * 0.42f)
                            lineTo(w * 0.36f, h * 0.64f)
                            close()
                        }
                        val p2 = Path().apply {
                            moveTo(w * 0.54f, h * 0.42f)
                            lineTo(w * 0.88f, h * 0.24f)
                            lineTo(w * 0.62f, h * 0.62f)
                            close()
                        }
                        val p3 = Path().apply {
                            moveTo(w * 0.36f, h * 0.64f)
                            lineTo(w * 0.62f, h * 0.62f)
                            lineTo(w * 0.46f, h * 0.88f)
                            close()
                        }
                        val p4 = Path().apply {
                            moveTo(w * 0.62f, h * 0.62f)
                            lineTo(w * 0.84f, h * 0.74f)
                            lineTo(w * 0.58f, h * 0.76f)
                            close()
                        }
                        val p5 = Path().apply {
                            moveTo(w * 0.88f, h * 0.24f)
                            lineTo(w * 0.96f, h * 0.30f)
                            lineTo(w * 0.82f, h * 0.36f)
                            close()
                        }

                        onDrawBehind {
                            drawPath(p1, Color(0xFFE2136E))
                            drawPath(p2, Color(0xFFC60E5C))
                            drawPath(p3, Color(0xFFE2136E))
                            drawPath(p4, Color(0xFFA5094B))
                            drawPath(p5, Color(0xFFE2136E))
                        }
                    }
            )
        }
    }
}

@Composable
fun DbblBankLogo(
    modifier: Modifier = Modifier.size(40.dp)
) {
    val context = LocalContext.current
    val customBitmap = CustomAssetManager.getCustomBitmap(context, CustomAssetManager.KEY_LOGO_DBBL)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, DbblBlue, RoundedCornerShape(12.dp))
            .padding(if (customBitmap != null) 0.dp else 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = "DBBL Bank Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height

                        val leftArch = Path().apply {
                            moveTo(w * 0.14f, h * 0.78f)
                            lineTo(w * 0.36f, h * 0.20f)
                            lineTo(w * 0.54f, h * 0.20f)
                            lineTo(w * 0.32f, h * 0.78f)
                            close()
                        }
                        val rightArch = Path().apply {
                            moveTo(w * 0.48f, h * 0.78f)
                            lineTo(w * 0.68f, h * 0.20f)
                            lineTo(w * 0.86f, h * 0.20f)
                            lineTo(w * 0.66f, h * 0.78f)
                            close()
                        }

                        onDrawBehind {
                            drawPath(leftArch, DbblBlue)
                            drawPath(rightArch, DbblBlue)
                            drawCircle(
                                color = DbblRed,
                                radius = w * 0.18f,
                                center = Offset(w * 0.50f, h * 0.50f)
                            )
                        }
                    }
            )
        }
    }
}

@Composable
fun BangladeshiBanknoteBanner(
    denom: Int,
    count: Int,
    height: Dp = 84.dp,
    compactMode: Boolean = false,
    showZoomBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val customBitmap = CustomAssetManager.getCustomBitmap(context, CustomAssetManager.noteKey(denom))
    val style = BanknoteCatalog.styleFor(denom)
    val shape = RoundedCornerShape(if (compactMode) 10.dp else 12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(1.dp, style.borderColor, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val pad = if (compactMode) 3.dp.toPx() else 5.dp.toPx()
                val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                val borderStroke = Stroke(width = 1.dp.toPx())
                val threadDash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                val threadStroke = 2.dp.toPx()

                val bgBrush = Brush.linearGradient(
                    colors = listOf(style.primaryColor, style.secondaryColor, style.primaryColor),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )
                val scrimBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.52f),
                        Color.Black.copy(alpha = 0.18f),
                        Color.Black.copy(alpha = 0.48f)
                    )
                )

                onDrawBehind {
                    drawRect(brush = bgBrush)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.28f),
                        topLeft = Offset(pad, pad),
                        size = Size(w - pad * 2, h - pad * 2),
                        cornerRadius = corner,
                        style = borderStroke
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f),
                        radius = h * 0.32f,
                        center = Offset(w * 0.78f, h * 0.52f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = h * 0.32f,
                        center = Offset(w * 0.78f, h * 0.52f),
                        style = borderStroke
                    )
                    drawLine(
                        color = Color(0xFFFDE68A).copy(alpha = 0.65f),
                        start = Offset(w * 0.62f, pad),
                        end = Offset(w * 0.62f, h - pad),
                        strokeWidth = threadStroke,
                        pathEffect = threadDash
                    )
                    drawRect(brush = scrimBrush)
                }
            }
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = "৳$denom Banknote",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.36f))
            )
        }

        if (compactMode) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "৳${BanglaFormatters.toBanglaDigits(denom)}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = BanglaFormatters.tr("বাংলাদেশ ব্যাংক", "Bangladesh Bank"),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${BanglaFormatters.toBanglaDigits(count)}${BanglaFormatters.tr("টি", "x")}",
                        color = NeonMint,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.tr("বাংলাদেশ ব্যাংক", "Bangladesh Bank"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    if (showZoomBadge) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.68f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.tr("🔍 জুম", "🔍 Zoom"),
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "৳${BanglaFormatters.toBanglaDigits(denom)}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = style.monumentTitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.78f))
                            .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${BanglaFormatters.toBanglaDigits(count)}${BanglaFormatters.tr("টি নোট", " Notes")}",
                            color = NeonMint,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact, color-coded, banknote-image-backed clickable note card for the Expenditure / Payment
 * workflow:
 * - Clicking the note card itself adds +1 of that note (and auto-adds its value to expenditure).
 * - Displays custom imported banknote image (or authentic color-coded banknote visual).
 * - Contains ONLY a minus (−) button to decrement; no plus (+) button.
 */
@Composable
fun PaymentClickableNoteCard(
    denom: Int,
    selectedCount: Int,
    subLabel: String,
    canAdd: Boolean = true,
    accentHighlight: Color = NeonCyan,
    testTagPrefix: String = "payment_note",
    modifier: Modifier = Modifier,
    onAddOne: () -> Unit,
    onMinusOne: () -> Unit
) {
    val context = LocalContext.current
    val customBitmap = CustomAssetManager.getCustomBitmap(context, CustomAssetManager.noteKey(denom))
    val style = BanknoteCatalog.styleFor(denom)
    val shape = RoundedCornerShape(10.dp)
    val isSelected = selectedCount > 0

    Box(
        modifier = modifier
            .height(54.dp)
            .alpha(if (!canAdd && !isSelected) 0.42f else 1f)
            .clip(shape)
            .border(
                width = if (isSelected) 1.6.dp else 1.dp,
                color = if (isSelected) accentHighlight else style.borderColor,
                shape = shape
            )
            .clickable(enabled = canAdd) { onAddOne() }
            .testTag("${testTagPrefix}_card_$denom")
            .drawWithCache {
                val w = size.width
                val h = size.height
                val bgBrush = Brush.linearGradient(
                    colors = listOf(style.primaryColor, style.secondaryColor, style.primaryColor),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )
                val scrim = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.56f),
                        Color.Black.copy(alpha = 0.24f),
                        Color.Black.copy(alpha = 0.52f)
                    )
                )
                onDrawBehind {
                    drawRect(brush = bgBrush)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f),
                        radius = h * 0.34f,
                        center = Offset(w * 0.65f, h * 0.5f)
                    )
                    drawLine(
                        color = Color(0xFFFDE68A).copy(alpha = 0.5f),
                        start = Offset(w * 0.52f, 3.dp.toPx()),
                        end = Offset(w * 0.52f, h - 3.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                    )
                    drawRect(brush = scrim)
                }
            }
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = "৳$denom Note",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.46f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Denom + Vault/Sub label (tapping anywhere on the card adds +1)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "৳${BanglaFormatters.toBanglaDigits(denom)}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentHighlight)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "×${BanglaFormatters.toBanglaDigits(selectedCount)}",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Text(
                    text = subLabel,
                    color = style.accentColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: ONLY a Minus (−) button (no plus button!)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (isSelected) NeonPink.copy(alpha = 0.85f)
                        else Color.Black.copy(alpha = 0.55f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.55f)
                        else Color.White.copy(alpha = 0.18f),
                        RoundedCornerShape(7.dp)
                    )
                    .clickable(enabled = isSelected) { onMinusOne() }
                    .testTag("${testTagPrefix}_minus_$denom"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
