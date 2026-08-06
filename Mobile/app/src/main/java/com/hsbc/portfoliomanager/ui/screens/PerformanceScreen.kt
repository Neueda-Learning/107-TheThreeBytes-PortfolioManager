package com.hsbc.portfoliomanager.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.ui.theme.*
import com.hsbc.portfoliomanager.ui.viewmodel.PerformanceViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@Composable
fun PerformanceScreen(viewModel: PerformanceViewModel, showHeader: Boolean = false) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrowwBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GrowwSurface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Unrealized P&L per holding", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(
                        onClick = { viewModel.loadPerformance() },
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(GrowwSurface2)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            when {
                uiState.isLoading -> GrowwLoadingScreen()
                uiState.error != null -> GrowwErrorScreen(message = uiState.error!!, onRetry = { viewModel.loadPerformance() })
                uiState.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextHint)
                            Spacer(Modifier.height(16.dp))
                            Text("No performance data", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text("Add holdings to see P&L analytics", style = MaterialTheme.typography.bodySmall, color = TextHint, textAlign = TextAlign.Center)
                        }
                    }
                }
                else -> {
                    // Summary row
                    val totalGain = uiState.items.fold(BigDecimal.ZERO) { acc, item -> acc + item.unrealizedGain }
                    val totalValue = uiState.items.fold(BigDecimal.ZERO) { acc, item -> acc + item.currentValue }
                    val totalCost = uiState.items.fold(BigDecimal.ZERO) { acc, item -> acc + item.costBasis }
                    val overallPct = if (totalCost > BigDecimal.ZERO)
                        totalGain.divide(totalCost, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal(100))
                    else BigDecimal.ZERO
                    val isGain = totalGain >= BigDecimal.ZERO

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GrowwSurface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PerformanceSummaryItem("Invested", currencyFormat.format(totalCost))
                                VerticalDivider(modifier = Modifier.height(40.dp), color = GrowwSurface3)
                                PerformanceSummaryItem("Current", currencyFormat.format(totalValue))
                                VerticalDivider(modifier = Modifier.height(40.dp), color = GrowwSurface3)
                                PerformanceSummaryItem(
                                    "P&L",
                                    "${if (isGain) "+" else ""}${currencyFormat.format(totalGain)}",
                                    valueColor = if (isGain) GrowwGreen else GrowwRed
                                )
                            }
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items) { item ->
                            PerformanceCard(item = item, currencyFormat = currencyFormat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceSummaryItem(label: String, value: String, valueColor: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun PerformanceCard(item: PerformanceItem, currencyFormat: NumberFormat) {
    val isGain = item.unrealizedGain >= BigDecimal.ZERO
    val gainColor = if (isGain) GrowwGreen else GrowwRed
    val gainBg = if (isGain) GrowwGreenAlpha else GrowwRedAlpha

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GrowwSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    Text(item.ticker, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${item.quantity} units • ${item.holdingDays}d held", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }

                // P&L badge
                Surface(shape = RoundedCornerShape(8.dp), color = gainBg) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.End) {
                        Text(
                            "${if (isGain) "+" else ""}${String.format("%.2f", item.unrealizedGainPct)}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = gainColor
                        )
                        Text(
                            "${if (isGain) "+" else ""}${currencyFormat.format(item.unrealizedGain)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = gainColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = GrowwSurface3, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GrowwDetailChip(label = "Buy Price", value = currencyFormat.format(item.purchasePrice))
                GrowwDetailChip(label = "Current", value = currencyFormat.format(item.currentPrice))
                GrowwDetailChip(label = "Invested", value = currencyFormat.format(item.costBasis))
                GrowwDetailChip(label = "Value", value = currencyFormat.format(item.currentValue))
            }
        }
    }
}
