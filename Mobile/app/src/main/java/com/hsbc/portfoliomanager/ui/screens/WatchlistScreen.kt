package com.hsbc.portfoliomanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.window.Dialog
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.ui.theme.*
import com.hsbc.portfoliomanager.ui.viewmodel.WatchlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(viewModel: WatchlistViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        if (uiState.successMessage != null || uiState.error != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessages()
        }
    }

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
                        Text("Watchlist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${uiState.items.size} assets tracked", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(
                        onClick = { viewModel.loadWatchlist() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GrowwSurface2)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Toast messages
            AnimatedVisibility(visible = uiState.successMessage != null) {
                Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(10.dp), color = GrowwGreenAlpha) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GrowwGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(uiState.successMessage ?: "", color = GrowwGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            AnimatedVisibility(visible = uiState.error != null) {
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(10.dp), color = GrowwRedAlpha) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = GrowwRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(uiState.error ?: "", color = GrowwRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            when {
                uiState.isLoading -> GrowwLoadingScreen()
                uiState.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextHint)
                            Spacer(Modifier.height(16.dp))
                            Text("No assets on watchlist", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text("Tap + to start tracking assets", style = MaterialTheme.typography.bodySmall, color = TextHint, textAlign = TextAlign.Center)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items) { item ->
                            WatchlistItemCard(item = item, onRemove = { viewModel.removeFromWatchlist(item.id) })
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = GrowwGreen,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add to Watchlist")
        }
    }

    if (showAddDialog) {
        AddWatchlistDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { ticker, assetType ->
                viewModel.addToWatchlist(ticker, assetType)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WatchlistItemCard(item: WatchlistItem, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GrowwSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
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
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.ticker, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${item.assetType.displayName()} • Added ${item.addedDate.take(10)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextHint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWatchlistDialog(onDismiss: () -> Unit, onAdd: (String, AssetType) -> Unit) {
    var ticker by remember { mutableStateOf("") }
    var assetType by remember { mutableStateOf(AssetType.STOCK) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GrowwSurface,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Add to Watchlist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)

                GrowwTextField(value = ticker, onValueChange = { ticker = it.uppercase() }, label = "Ticker (e.g. AAPL, BTC)")

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = assetType.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asset Type", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = growwTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(GrowwSurface2)
                    ) {
                        AssetType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName(), color = TextPrimary) },
                                onClick = { assetType = type; expanded = false }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (ticker.isNotBlank()) onAdd(ticker, assetType) },
                        enabled = ticker.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GrowwGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
