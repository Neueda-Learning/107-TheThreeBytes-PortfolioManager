package com.hsbc.portfoliomanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsbc.portfoliomanager.data.model.AssetType
import com.hsbc.portfoliomanager.data.model.PortfolioItem
import com.hsbc.portfoliomanager.data.model.displayName
import com.hsbc.portfoliomanager.ui.theme.*
import com.hsbc.portfoliomanager.ui.viewmodel.DashboardViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToHoldings: () -> Unit,
    onNavigateToStockDetail: (ticker: String, assetType: AssetType, item: PortfolioItem) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf<AssetType?>(null) }

    LaunchedEffect(selectedFilter) {
        viewModel.loadDashboard(selectedFilter)
    }

    val context = LocalContext.current
    LaunchedEffect(uiState.error) {
        if (uiState.error != null && uiState.dashboard != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrowwBg)
    ) {
        when {
            uiState.isLoading && uiState.dashboard == null -> GrowwLoadingScreen()
            uiState.error != null && uiState.dashboard == null -> GrowwErrorScreen(
                message = uiState.error ?: "An error occurred",
                onRetry = { viewModel.loadDashboard(selectedFilter) }
            )
            uiState.dashboard != null -> {
                val dashboard = uiState.dashboard!!
                val isProfit = dashboard.unrealizedGainLoss >= BigDecimal.ZERO
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // ── Header ────────────────────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF111111), GrowwBg)
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Portfolio",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                        Text(
                                            "Dashboard",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.loadDashboard(selectedFilter) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(GrowwSurface2)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Portfolio Value
                                Text(
                                    "Current Value",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    currencyFormat.format(dashboard.estimatedTotalValue),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // P&L Badge
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GrowwPnlBadge(
                                        value = dashboard.unrealizedGainLoss,
                                        pct = dashboard.unrealizedGainLossPct,
                                        currencyFormat = currencyFormat,
                                        isProfit = isProfit
                                    )
                                    Text(
                                        "Total Returns",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextHint
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Stats Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GrowwStatChip(
                                        label = "Invested",
                                        value = currencyFormat.format(dashboard.totalCostBasis),
                                        modifier = Modifier.weight(1f)
                                    )
                                    GrowwStatChip(
                                        label = "Positions",
                                        value = dashboard.totalPositions.toString(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    GrowwStatChip(
                                        label = "Holdings",
                                        value = dashboard.totalQuantity.toString(),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // ── Asset Type Filter ─────────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        GrowwSectionHeader("Filter by Type")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GrowwFilterChip(
                                label = "All",
                                selected = selectedFilter == null,
                                onClick = { selectedFilter = null }
                            )
                            AssetType.entries.forEach { type ->
                                GrowwFilterChip(
                                    label = type.displayName(),
                                    selected = selectedFilter == type,
                                    onClick = {
                                        selectedFilter = if (selectedFilter == type) null else type
                                    }
                                )
                            }
                        }
                    }

                    // ── Asset Allocation ──────────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        GrowwSectionHeader("Allocation")
                        GrowwAllocationCard(
                            quantityByAssetType = dashboard.quantityByAssetType,
                            costByAssetType = dashboard.costByAssetType
                        )
                    }

                    // ── Holdings List ─────────────────────────────────────────
                    if (dashboard.holdings.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Holdings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                TextButton(onClick = onNavigateToHoldings) {
                                    Text(
                                        "Manage →",
                                        color = GrowwGreen,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        items(dashboard.holdings) { item ->
                            GrowwHoldingRow(
                                item = item,
                                onNavigateToStockDetail = onNavigateToStockDetail
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrowwLoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = GrowwGreen,
                strokeWidth = 2.dp,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Loading portfolio…", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun GrowwErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.SignalWifiOff,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = TextHint
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GrowwGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GrowwPnlBadge(
    value: BigDecimal,
    pct: BigDecimal,
    currencyFormat: NumberFormat,
    isProfit: Boolean
) {
    val bgColor = if (isProfit) GrowwGreenAlpha else GrowwRedAlpha
    val fgColor = if (isProfit) GrowwGreen else GrowwRed
    val icon = if (isProfit) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fgColor, modifier = Modifier.size(18.dp))
            Text(
                "${currencyFormat.format(value)} (${String.format("%.2f", pct)}%)",
                color = fgColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GrowwStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GrowwSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun GrowwSectionHeader(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 0.5.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowwFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GrowwGreenAlpha,
            selectedLabelColor = GrowwGreen,
            containerColor = GrowwSurface,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = GrowwGreen,
            borderColor = GrowwSurface3
        )
    )
}

@Composable
fun GrowwAllocationCard(
    quantityByAssetType: Map<String, BigDecimal>,
    costByAssetType: Map<String, BigDecimal>
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val totalCost = costByAssetType.values.fold(BigDecimal.ZERO) { acc, v -> acc + v }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = GrowwSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            quantityByAssetType.entries.forEachIndexed { index, entry ->
                val cost = costByAssetType[entry.key] ?: BigDecimal.ZERO
                val pct = if (totalCost > BigDecimal.ZERO)
                    cost.divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal(100))
                else BigDecimal.ZERO
                val color = ChartColors[index % ChartColors.size]

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    "${entry.value.toPlainString()} units",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                currencyFormat.format(cost),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                "${String.format("%.1f", pct)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { pct.toFloat() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = color,
                        trackColor = GrowwSurface3
                    )
                    if (index < quantityByAssetType.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GrowwHoldingRow(
    item: PortfolioItem,
    onNavigateToStockDetail: (ticker: String, assetType: AssetType, item: PortfolioItem) -> Unit = { _, _, _ -> }
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onNavigateToStockDetail(item.ticker, item.assetType, item) },
        shape = RoundedCornerShape(12.dp),
        color = GrowwSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Asset icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (item.assetType) {
                            AssetType.STOCK  -> Color(0x2600C853)
                            AssetType.BOND   -> Color(0x26FF9500)
                            AssetType.CRYPTO -> Color(0x26BF5AF2)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (item.assetType) {
                        AssetType.STOCK  -> Icons.Default.TrendingUp
                        AssetType.BOND   -> Icons.Default.AccountBalance
                        AssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
                    },
                    contentDescription = null,
                    tint = when (item.assetType) {
                        AssetType.STOCK  -> GrowwGreen
                        AssetType.BOND   -> AccentOrange
                        AssetType.CRYPTO -> AccentPurple
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.ticker,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    item.name ?: item.assetType.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    currencyFormat.format(item.purchasePrice.multiply(item.quantity)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "${item.quantity.toPlainString()} units @ ${currencyFormat.format(item.purchasePrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
