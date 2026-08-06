package com.hsbc.portfoliomanager.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsbc.portfoliomanager.data.model.AssetType
import com.hsbc.portfoliomanager.data.model.DailyCandle
import com.hsbc.portfoliomanager.data.model.PortfolioItem
import com.hsbc.portfoliomanager.ui.theme.*
import com.hsbc.portfoliomanager.ui.viewmodel.StockDetailViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    ticker: String,
    assetType: AssetType,
    portfolioItem: PortfolioItem?,
    viewModel: StockDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFmt = NumberFormat.getCurrencyInstance(Locale.US)

    // Load on first entry
    LaunchedEffect(ticker) {
        viewModel.load(ticker, assetType, portfolioItem)
    }

    val context = LocalContext.current
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrowwBg)
    ) {
        when {
            uiState.isLoading -> GrowwLoadingScreen()

            uiState.error != null && uiState.currentPrice == null -> GrowwErrorScreen(
                message = uiState.error ?: "Failed to load stock data",
                onRetry = { viewModel.refresh(ticker, assetType, portfolioItem) }
            )

            else -> {
                val isProfit = (uiState.change ?: BigDecimal.ZERO) >= BigDecimal.ZERO
                val lineColor = if (isProfit) GrowwGreen else GrowwRed
                val changeSign = if (isProfit) "+" else ""

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Top Bar ───────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GrowwBg)
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                ticker,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (uiState.companyName != null) {
                                Text(
                                    uiState.companyName!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        // Data source badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (uiState.dataSource == "FINNHUB") GrowwGreenAlpha else GrowwSurface3
                        ) {
                            Text(
                                if (uiState.dataSource == "FINNHUB") "LIVE" else "DEMO",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.dataSource == "FINNHUB") GrowwGreen else TextSecondary
                            )
                        }
                        IconButton(onClick = { viewModel.refresh(ticker, assetType, portfolioItem) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextHint)
                        }
                    }

                    // ── Price Header ─────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            if (uiState.currentPrice != null)
                                currencyFmt.format(uiState.currentPrice)
                            else "—",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 36.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Change pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isProfit) GrowwGreenAlpha else GrowwRedAlpha
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = lineColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "$changeSign${uiState.change?.setScale(2, RoundingMode.HALF_UP) ?: "—"}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = lineColor
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "(${changeSign}${uiState.changePercent?.setScale(2, RoundingMode.HALF_UP) ?: "—"}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = lineColor
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHint
                            )
                        }
                    }

                    // ── 30-Day Chart ──────────────────────────────────────────
                    if (uiState.candles.isNotEmpty()) {
                        val chartIsProfit = uiState.candles.last().close >= uiState.candles.first().close
                        val chartLineColor = if (chartIsProfit) GrowwGreen else GrowwRed
                        
                        StockLineChart(
                            candles   = uiState.candles,
                            lineColor = chartLineColor,
                            modifier  = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )

                        // X-axis labels: first and last date
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                uiState.candles.firstOrNull()?.date?.takeLast(5) ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHint
                            )
                            Text(
                                uiState.candles.lastOrNull()?.date?.takeLast(5) ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHint
                            )
                        }
                    } else {
                        // No candle data placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chart data unavailable", color = TextHint, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Stats Grid ────────────────────────────────────────────
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = GrowwSurface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "KEY STATS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHint,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(12.dp))

                            StatsRow(
                                left  = Pair("Open", currencyFmt.format(uiState.todayOpen ?: uiState.currentPrice ?: BigDecimal.ZERO)),
                                right = Pair("Prev Close", currencyFmt.format(uiState.previousClose ?: uiState.currentPrice ?: BigDecimal.ZERO))
                            )
                            HorizontalDivider(color = GrowwSurface3, modifier = Modifier.padding(vertical = 10.dp))
                            StatsRow(
                                left  = Pair("Today High", currencyFmt.format(uiState.todayHigh ?: uiState.currentPrice ?: BigDecimal.ZERO)),
                                right = Pair("Today Low", currencyFmt.format(uiState.todayLow ?: uiState.currentPrice ?: BigDecimal.ZERO))
                            )
                            HorizontalDivider(color = GrowwSurface3, modifier = Modifier.padding(vertical = 10.dp))
                            StatsRow(
                                left  = Pair("30D High", currencyFmt.format(uiState.thirtyDayHigh ?: BigDecimal.ZERO)),
                                right = Pair("30D Low", currencyFmt.format(uiState.thirtyDayLow ?: BigDecimal.ZERO))
                            )
                            if (uiState.dataSource == "FINNHUB") {
                                HorizontalDivider(color = GrowwSurface3, modifier = Modifier.padding(vertical = 10.dp))
                                StatsRow(
                                    left  = Pair("Data Source", "Finnhub (Live)"),
                                    right = Pair("Days", "${uiState.candles.size} trading days")
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Your Position Card ────────────────────────────────────
                    if (portfolioItem != null) {
                        val pnl    = uiState.unrealisedPnl
                        val pnlPct = uiState.unrealisedPnlPct
                        val pnlPositive = (pnl ?: BigDecimal.ZERO) >= BigDecimal.ZERO

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = GrowwSurface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "YOUR POSITION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextHint,
                                        letterSpacing = 1.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (assetType) {
                                            AssetType.STOCK  -> Color(0x2600C853)
                                            AssetType.BOND   -> Color(0x26FF9500)
                                            AssetType.CRYPTO -> Color(0x26BF5AF2)
                                        }
                                    ) {
                                        Text(
                                            assetType.name,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (assetType) {
                                                AssetType.STOCK  -> GrowwGreen
                                                AssetType.BOND   -> AccentOrange
                                                AssetType.CRYPTO -> AccentPurple
                                            }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(14.dp))

                                StatsRow(
                                    left  = Pair("Quantity", "${portfolioItem.quantity} units"),
                                    right = Pair("Avg Price", currencyFmt.format(portfolioItem.purchasePrice))
                                )
                                HorizontalDivider(color = GrowwSurface3, modifier = Modifier.padding(vertical = 10.dp))
                                StatsRow(
                                    left  = Pair("Invested", currencyFmt.format(
                                        portfolioItem.purchasePrice.multiply(portfolioItem.quantity)
                                    )),
                                    right = Pair("Current Value", currencyFmt.format(
                                        (uiState.currentPrice ?: portfolioItem.purchasePrice)
                                            .multiply(portfolioItem.quantity)
                                    ))
                                )
                                HorizontalDivider(color = GrowwSurface3, modifier = Modifier.padding(vertical = 10.dp))

                                // Unrealised P&L
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Unrealised P&L",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            if (pnl != null) "${if (pnlPositive) "+" else ""}${currencyFmt.format(pnl)}"
                                            else "—",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pnlPositive) GrowwGreen else GrowwRed
                                        )
                                        if (pnlPct != null) {
                                            Text(
                                                "${if (pnlPositive) "+" else ""}${pnlPct.setScale(2, RoundingMode.HALF_UP)}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (pnlPositive) GrowwGreen else GrowwRed
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = GrowwSurface3, modifier = Modifier.padding(vertical = 10.dp))
                                StatsRow(
                                    left  = Pair("Bought On", portfolioItem.purchaseDate),
                                    right = Pair("", "")
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── About Section ─────────────────────────────────────────
                    if (uiState.sector != null || uiState.companyName != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = GrowwSurface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "ABOUT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextHint,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(10.dp))
                                if (uiState.companyName != null) {
                                    Text(
                                        uiState.companyName!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                                if (uiState.sector != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GrowwSurface3
                                    ) {
                                        Text(
                                            uiState.sector!!,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Line Chart (Compose Canvas — no library) ──────────────────────────────────

@Composable
fun StockLineChart(
    candles: List<DailyCandle>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val closePrices = candles.map { it.close.toFloat() }
    val minPrice = closePrices.minOrNull() ?: 0f
    val maxPrice = closePrices.maxOrNull() ?: 1f
    val priceRange = maxPrice - minPrice

    // Animated progress for draw-in effect
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "chartProgress"
    )

    var touchX by remember { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset -> touchX = offset.x },
            onDrag = { change, _ -> touchX = change.position.x },
            onDragEnd = { touchX = null },
            onDragCancel = { touchX = null }
        )
        // Also allow tap to show momentarily
        detectTapGestures(
            onPress = { offset ->
                touchX = offset.x
                tryAwaitRelease()
                touchX = null
            }
        )
    }) {
        if (priceRange == 0f) return@Canvas

        val width  = size.width
        val height = size.height
        val padTop = 16f
        val padBot = 16f
        val usableH = height - padTop - padBot

        val points = closePrices.mapIndexed { i, price ->
            val x = if (closePrices.size == 1) width / 2f
            else i.toFloat() / (closePrices.size - 1).toFloat() * width
            val y = padTop + usableH - ((price - minPrice) / priceRange) * usableH
            Offset(x, y)
        }

        // Limit visible points by animated progress
        val visibleCount = (points.size * animatedProgress).toInt().coerceAtLeast(2)
        val visible = points.take(visibleCount)

        // Build path
        val linePath = Path().apply {
            visible.forEachIndexed { i, pt ->
                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
        }

        // Gradient fill below line
        val fillPath = Path().apply {
            addPath(linePath)
            val lastPt = visible.last()
            lineTo(lastPt.x, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY   = height
            )
        )

        // Line
        drawPath(
            path   = linePath,
            color  = lineColor,
            style  = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5f,
                cap   = StrokeCap.Round,
                join  = StrokeJoin.Round
            )
        )

        // Dot at the last visible point
        if (visible.isNotEmpty() && touchX == null) {
            val last = visible.last()
            drawCircle(color = lineColor, radius = 5f, center = last)
            drawCircle(color = lineColor.copy(alpha = 0.25f), radius = 10f, center = last)
        }

        // Draw interactive tooltip if touched
        if (touchX != null && visible.isNotEmpty()) {
            val closestIndex = visible.indices.minByOrNull { i -> kotlin.math.abs(visible[i].x - touchX!!) } ?: -1
            if (closestIndex != -1) {
                val pt = visible[closestIndex]
                val candle = candles[closestIndex]

                // Vertical dashed line
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(pt.x, 0f),
                    end = Offset(pt.x, height),
                    strokeWidth = 3f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // Point dot
                drawCircle(color = lineColor, radius = 8f, center = pt)
                drawCircle(color = Color.White, radius = 4f, center = pt)

                // Tooltip text
                val dateStr = candle.date.takeLast(5)
                val priceStr = "₹${candle.close.setScale(2, java.math.RoundingMode.HALF_UP)}"
                val text = "$dateStr | $priceStr"
                
                val textLayoutResult = textMeasurer.measure(
                    text = text,
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color.White, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold
                    )
                )
                
                // Tooltip background
                val tooltipWidth = textLayoutResult.size.width + 24f
                val tooltipHeight = textLayoutResult.size.height + 16f
                val tooltipX = (pt.x - tooltipWidth / 2f).coerceIn(0f, width - tooltipWidth)
                val tooltipY = (pt.y - tooltipHeight - 16f).coerceAtLeast(0f)
                
                drawRoundRect(
                    color = Color(0xFF2D2D2D),
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                
                drawText(
                    textMeasurer = textMeasurer,
                    text = text,
                    topLeft = Offset(tooltipX + 12f, tooltipY + 8f),
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color.White, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// ── Helper Composable ─────────────────────────────────────────────────────────

@Composable
private fun StatsRow(left: Pair<String, String>, right: Pair<String, String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left stat
        Column {
            Text(left.first, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                left.second,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        // Right stat
        if (right.first.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.End) {
                Text(right.first, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    right.second,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}
