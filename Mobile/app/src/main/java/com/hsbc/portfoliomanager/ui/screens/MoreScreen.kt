package com.hsbc.portfoliomanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.ui.theme.*
import com.hsbc.portfoliomanager.ui.viewmodel.RiskTaxViewModel
import com.hsbc.portfoliomanager.ui.viewmodel.PerformanceViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(viewModel: RiskTaxViewModel, performanceViewModel: PerformanceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    // Sub-tab state: 0=Returns, 1=Risk, 2=Tax, 3=Dividends
    var selectedTab by remember { mutableIntStateOf(0) }

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
                    Text("Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(
                        onClick = { viewModel.loadAll() },
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(GrowwSurface2)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Sub-tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = GrowwSurface,
                contentColor = GrowwGreen,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GrowwGreen,
                        height = 2.dp
                    )
                }
            ) {
                listOf("Returns", "Risk", "Tax", "Dividends").forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                label,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == idx) GrowwGreen else TextSecondary
                            )
                        }
                    )
                }
            }

            when {
                uiState.isLoading -> GrowwLoadingScreen()
                else -> when (selectedTab) {
                    0 -> PerformanceScreen(viewModel = performanceViewModel)
                    1 -> RiskTab(riskAnalysis = uiState.riskAnalysis, currencyFormat = currencyFormat)
                    2 -> TaxTab(taxItems = uiState.taxItems, currencyFormat = currencyFormat)
                    3 -> DividendsTab(
                        dividends = uiState.dividends,
                        totalDividends = uiState.totalDividends,
                        currencyFormat = currencyFormat,
                        onDelete = { viewModel.deleteDividend(it) }
                    )
                }
            }
        }
    }
}

// ── Risk Tab ────────────────────────────────────────────────────────────────

@Composable
fun RiskTab(riskAnalysis: RiskAnalysisResponse?, currencyFormat: NumberFormat) {
    if (riskAnalysis == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No risk data available", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overall risk level card
        item {
            val riskColor = when (riskAnalysis.overallRiskLevel.uppercase()) {
                "LOW"    -> GrowwGreen
                "MEDIUM" -> AccentOrange
                else     -> GrowwRed
            }
            Surface(shape = RoundedCornerShape(16.dp), color = GrowwSurface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = riskColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Overall Risk Level", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(riskAnalysis.overallRiskLevel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = riskColor)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Diversification", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            "${String.format("%.0f", riskAnalysis.diversificationScore)}/100",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Concentration
        item {
            Text("Concentration by Asset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        }
        items(riskAnalysis.concentrationByAssetType.entries.toList()) { entry ->
            Surface(shape = RoundedCornerShape(12.dp), color = GrowwSurface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.key, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${String.format("%.1f", entry.value)}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GrowwGreen)
                }
            }
        }

        // Holdings breakdown
        item {
            Spacer(Modifier.height(4.dp))
            Text("Holdings Risk Detail", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        }
        items(riskAnalysis.holdingRiskDetails) { detail ->
            Surface(shape = RoundedCornerShape(12.dp), color = GrowwSurface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(detail.ticker, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${detail.holdingDays}d • ${detail.holdingCategory}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Text(
                        "${String.format("%.1f", detail.portfolioConcentrationPct)}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// ── Tax Tab ─────────────────────────────────────────────────────────────────

@Composable
fun TaxTab(taxItems: List<TaxItem>, currencyFormat: NumberFormat) {
    if (taxItems.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tax data available", color = TextSecondary)
        }
        return
    }

    val totalLiability = taxItems.fold(BigDecimal.ZERO) { acc, t -> acc + t.estimatedTaxLiability }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = GrowwSurface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(GrowwRedAlpha), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GrowwRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Total Est. Tax Liability", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(currencyFormat.format(totalLiability), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GrowwRed)
                    }
                }
            }
        }
        items(taxItems) { tax ->
            Surface(shape = RoundedCornerShape(12.dp), color = GrowwSurface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(tax.ticker, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(4.dp), color = if (tax.taxCategory == "SHORT_TERM") GrowwRedAlpha else GrowwGreenAlpha) {
                            Text(
                                tax.taxCategory.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tax.taxCategory == "SHORT_TERM") GrowwRed else GrowwGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        GrowwDetailChip(label = "Tax Rate", value = "${String.format("%.0f", tax.taxRate)}%")
                        GrowwDetailChip(label = "Est. Gain", value = currencyFormat.format(tax.estimatedGain))
                        GrowwDetailChip(label = "Tax Due", value = currencyFormat.format(tax.estimatedTaxLiability))
                    }
                }
            }
        }
    }
}

// ── Dividends Tab ────────────────────────────────────────────────────────────

@Composable
fun DividendsTab(
    dividends: List<DividendResponse>,
    totalDividends: BigDecimal,
    currencyFormat: NumberFormat,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = GrowwSurface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(GrowwGreenAlpha), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = GrowwGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Total Dividends Received", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(currencyFormat.format(totalDividends), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GrowwGreen)
                    }
                }
            }
        }

        if (dividends.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No dividends recorded", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(dividends) { div ->
                Surface(shape = RoundedCornerShape(12.dp), color = GrowwSurface, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(div.ticker, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${div.sharesHeld} shares @ ${currencyFormat.format(div.dividendPerShare)}/share", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(div.dividendDate.take(10), style = MaterialTheme.typography.labelSmall, color = TextHint)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(currencyFormat.format(div.totalDividend), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GrowwGreen)
                            IconButton(onClick = { onDelete(div.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextHint, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
