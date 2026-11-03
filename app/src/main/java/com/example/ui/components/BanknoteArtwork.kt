package com.example.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BanknoteCatalog
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.util.BanglaFormatters

@Composable
fun BkashOrigamiLogo(
    modifier: Modifier = Modifier.size(40.dp)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFE2136E), RoundedCornerShape(12.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
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

@Composable
fun BangladeshiBanknoteBanner(
    denom: Int,
    count: Int,
    height: Dp = 84.dp,
    compactMode: Boolean = false,
    showZoomBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
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
                        Color.Black.copy(alpha = 0.48f),
                        Color.Black.copy(alpha = 0.15f),
                        Color.Black.copy(alpha = 0.45f)
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
                        text = "বাংলাদেশ ব্যাংক",
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
                        text = "${BanglaFormatters.toBanglaDigits(count)}টি",
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
                            text = "বাংলাদেশ ব্যাংক",
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
                                text = "🔍 জুম",
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
                            text = "${BanglaFormatters.toBanglaDigits(count)}টি নোট",
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
