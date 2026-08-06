package com.hsbc.portfoliomanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.data.api.ApiClient
import com.hsbc.portfoliomanager.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.hsbc.portfoliomanager.ui.viewmodel.PortfolioViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingsScreen(
    viewModel: PortfolioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStockDetail: (ticker: String, assetType: AssetType, item: PortfolioItem) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<PortfolioItem?>(null) }
    var itemToDelete by remember { mutableStateOf<PortfolioItem?>(null) }
    var itemToSell by remember { mutableStateOf<PortfolioItem?>(null) }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        if (uiState.successMessage != null || uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrowwBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GrowwSurface)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        "My Holdings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.loadPortfolioItems() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                    }
                }
            }

            // ── Toast messages ────────────────────────────────────────────────
            AnimatedVisibility(visible = uiState.successMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = GrowwGreenAlpha
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GrowwGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(uiState.successMessage ?: "", color = GrowwGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            AnimatedVisibility(visible = uiState.error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = GrowwRedAlpha
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = GrowwRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(uiState.error ?: "", color = GrowwRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            when {
                uiState.isLoading && uiState.items.isEmpty() -> GrowwLoadingScreen()

                uiState.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextHint
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("No holdings yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text(
                                "Tap + to add your first investment",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextHint,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.items) { item ->
                            GrowwHoldingManagementCard(
                                item = item,
                                onEdit   = { itemToEdit = item },
                                onDelete = { itemToDelete = item },
                                onSell   = { itemToSell = item },
                                onViewDetail = { onNavigateToStockDetail(item.ticker, item.assetType, item) }
                            )
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = GrowwGreen,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Holding")
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (showAddDialog) {
        GrowwAddEditPortfolioDialog(
            item = null,
            onDismiss = { showAddDialog = false },
            onSave = { request ->
                viewModel.createPortfolioItem(request)
                showAddDialog = false
            }
        )
    }

    itemToEdit?.let { item ->
        GrowwAddEditPortfolioDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { request ->
                viewModel.updatePortfolioItem(
                    item.id,
                    UpdatePortfolioItemRequest(
                        ticker        = request.ticker,
                        quantity      = request.quantity,
                        assetType     = request.assetType,
                        purchasePrice = request.purchasePrice,
                        purchaseDate  = request.purchaseDate,
                        name          = request.name,
                        sector        = request.sector,
                        issuer        = request.issuer,
                        interestRate  = request.interestRate,
                        maturityDate  = request.maturityDate
                    )
                )
                itemToEdit = null
            }
        )
    }

    itemToDelete?.let { item ->
        GrowwDeleteDialog(
            ticker    = item.ticker,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                viewModel.deletePortfolioItem(item.id)
                itemToDelete = null
            }
        )
    }

    itemToSell?.let { item ->
        GrowwSellDialog(
            item      = item,
            onDismiss = { itemToSell = null },
            onConfirm = { price, quantity ->
                viewModel.sellPortfolioItem(item.id, price, quantity)
                itemToSell = null
            }
        )
    }
}

@Composable
fun GrowwHoldingManagementCard(
    item: PortfolioItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSell: () -> Unit,
    onViewDetail: (() -> Unit)? = null
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val totalValue = item.purchasePrice.multiply(item.quantity)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GrowwSurface,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onViewDetail != null) Modifier.clickable { onViewDetail() }
                else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
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
                        style = MaterialTheme.typography.titleMedium,
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
                        currencyFormat.format(totalValue),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Cost basis",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GrowwSurface3, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GrowwDetailChip(label = "Qty", value = item.quantity.toString())
                GrowwDetailChip(label = "Buy Price", value = currencyFormat.format(item.purchasePrice))
                GrowwDetailChip(label = "Date", value = item.purchaseDate.take(10))
            }

            // Optional BOND/CRYPTO fields
            if (item.sector != null || item.issuer != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item.sector?.let { GrowwDetailChip(label = "Sector", value = it, modifier = Modifier.weight(1f)) }
                    item.issuer?.let { GrowwDetailChip(label = "Issuer", value = it, modifier = Modifier.weight(1f)) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SELL
                OutlinedButton(
                    onClick = onSell,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowwRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        /* just reuse default */
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Sell", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentBlue, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = GrowwRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun GrowwDetailChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextHint)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowwAddEditPortfolioDialog(
    item: PortfolioItem?,
    onDismiss: () -> Unit,
    onSave: (CreatePortfolioItemRequest) -> Unit
) {
    var ticker by remember { mutableStateOf(item?.ticker ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toPlainString() ?: "") }
    var assetType by remember { mutableStateOf(item?.assetType ?: AssetType.STOCK) }
    var price by remember { mutableStateOf(item?.purchasePrice?.toPlainString() ?: "") }
    var totalAmount by remember { mutableStateOf(
        if (item != null) item.purchasePrice.multiply(item.quantity).stripTrailingZeros().toPlainString() else ""
    ) }
    var date by remember { mutableStateOf(item?.purchaseDate?.take(10) ?: LocalDate.now().toString()) }
    var name by remember { mutableStateOf(item?.name ?: "") }
    var sector by remember { mutableStateOf(item?.sector ?: "") }
    var issuer by remember { mutableStateOf(item?.issuer ?: "") }
    var interestRate by remember { mutableStateOf(item?.interestRate?.toPlainString() ?: "") }
    var maturityDate by remember { mutableStateOf(item?.maturityDate?.take(10) ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GrowwSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        if (item == null) "Add Holding" else "Edit Holding",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                item {
                    val stockSuggestions = listOf("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NFLX", "JPM", "BAC", "NVDA", "META")
                    val bondSuggestions = listOf("US10Y", "US05Y", "IND10Y", "IND05Y", "UK10Y", "GER10Y", "JPN10Y", "CAN10Y", "AUS10Y", "FRA10Y")
                    val cryptoSuggestions = listOf("BTC", "ETH", "SOL", "ADA", "DOT", "DOGE", "SHIB", "MATIC", "LTC", "XRP")

                    val suggestions = when (assetType) {
                        AssetType.STOCK -> stockSuggestions
                        AssetType.BOND -> bondSuggestions
                        AssetType.CRYPTO -> cryptoSuggestions
                    }.filter { it.contains(ticker, ignoreCase = true) }

                    var tickerExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = tickerExpanded,
                        onExpandedChange = { tickerExpanded = !tickerExpanded }
                    ) {
                        OutlinedTextField(
                            value = ticker,
                            onValueChange = { ticker = it.uppercase(); tickerExpanded = true },
                            label = { Text("Ticker (e.g. ${suggestions.firstOrNull() ?: "AAPL"})", color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = growwTextFieldColors()
                        )
                        if (suggestions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = tickerExpanded,
                                onDismissRequest = { tickerExpanded = false }
                            ) {
                                suggestions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            ticker = selectionOption
                                            tickerExpanded = false
                                            
                                            // Fetch live price for auto-fill
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val response = ApiClient.portfolioApi.getLivePrice(selectionOption, assetType.name)
                                                    if (response.isSuccessful) {
                                                        val livePrice = response.body()?.currentPrice
                                                        if (livePrice != null) {
                                                            withContext(Dispatchers.Main) {
                                                                price = livePrice.toPlainString()
                                                                val q = quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                                if (q > BigDecimal.ZERO) {
                                                                    totalAmount = livePrice.multiply(q).stripTrailingZeros().toPlainString()
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch(e: Exception) {
                                                    // ignore on error
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    // Asset Type dropdown
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
                }
                item {
                    GrowwTextField(
                        value = price,
                        onValueChange = { newPrice ->
                            price = newPrice
                            val p = newPrice.toBigDecimalOrNull()
                            if (p != null && p.compareTo(BigDecimal.ZERO) > 0) {
                                val q = quantity.toBigDecimalOrNull()
                                if (q != null) {
                                    totalAmount = p.multiply(q).stripTrailingZeros().toPlainString()
                                } else {
                                    val t = totalAmount.toBigDecimalOrNull()
                                    if (t != null) {
                                        quantity = t.divide(p, 4, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                                    }
                                }
                            }
                        },
                        label = "Purchase Price ($)",
                        keyboardType = KeyboardType.Decimal
                    )
                }
                item {
                    GrowwTextField(
                        value = totalAmount,
                        onValueChange = { newTotal ->
                            totalAmount = newTotal
                            val t = newTotal.toBigDecimalOrNull()
                            val p = price.toBigDecimalOrNull()
                            if (t != null && p != null && p.compareTo(BigDecimal.ZERO) > 0) {
                                quantity = t.divide(p, 4, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                            } else if (newTotal.isEmpty()) {
                                quantity = ""
                            }
                        },
                        label = "Total Investment Amount ($)",
                        keyboardType = KeyboardType.Decimal
                    )
                }
                item {
                    GrowwTextField(
                        value = quantity,
                        onValueChange = { newQty ->
                            if (newQty.isEmpty() || newQty.toBigDecimalOrNull() != null || newQty.endsWith(".")) {
                                quantity = newQty
                                val q = newQty.toBigDecimalOrNull()
                                val p = price.toBigDecimalOrNull()
                                if (q != null && p != null) {
                                    totalAmount = p.multiply(q).stripTrailingZeros().toPlainString()
                                } else if (newQty.isEmpty()) {
                                    totalAmount = ""
                                }
                            }
                        },
                        label = "Quantity (Units)",
                        keyboardType = KeyboardType.Decimal
                    )
                }
                item { GrowwTextField(value = date, onValueChange = { date = it }, label = "Purchase Date (YYYY-MM-DD)") }

                // Optional fields
                item { GrowwTextField(value = name, onValueChange = { name = it }, label = "Name (optional)") }
                item { GrowwTextField(value = sector, onValueChange = { sector = it }, label = "Sector (optional)") }
                if (assetType == AssetType.BOND) {
                    item { GrowwTextField(value = issuer, onValueChange = { issuer = it }, label = "Issuer (optional)") }
                    item { GrowwTextField(value = maturityDate, onValueChange = { maturityDate = it }, label = "Maturity Date YYYY-MM-DD (optional)") }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val request = CreatePortfolioItemRequest(
                                        ticker        = ticker.trim(),
                                        quantity      = quantity.toBigDecimal(),
                                        assetType     = assetType,
                                        purchasePrice = BigDecimal(price),
                                        purchaseDate  = date.trim(),
                                        name          = name.trim().ifBlank { null },
                                        sector        = sector.trim().ifBlank { null },
                                        issuer        = issuer.trim().ifBlank { null },
                                        interestRate  = interestRate.trim().toBigDecimalOrNull(),
                                        maturityDate  = maturityDate.trim().ifBlank { null }
                                    )
                                    onSave(request)
                                } catch (_: Exception) { }
                            },
                            enabled = ticker.isNotBlank() && quantity.isNotBlank() &&
                                    price.isNotBlank() && date.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GrowwGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                if (item == null) "Add" else "Update",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrowwDeleteDialog(
    ticker: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GrowwSurface,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = GrowwRed) },
        title = { Text("Delete $ticker?", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = { Text("This will permanently remove the holding. This cannot be undone.", color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GrowwRed),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Delete", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun GrowwSellDialog(
    item: PortfolioItem,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal, BigDecimal) -> Unit
) {
    var sellPrice by remember { mutableStateOf("") }
    var sellQuantity by remember { mutableStateOf(item.quantity.toPlainString()) }
    var isFetchingPrice by remember { mutableStateOf(true) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val scope = rememberCoroutineScope()

    LaunchedEffect(item) {
        try {
            val response = ApiClient.portfolioApi.getLivePrice(item.ticker, item.assetType.name)
            if (response.isSuccessful) {
                val livePrice = response.body()?.currentPrice
                if (livePrice != null) {
                    sellPrice = livePrice.toPlainString()
                }
            }
        } catch(e: Exception) {
            // handle error if needed
        } finally {
            isFetchingPrice = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GrowwSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Sell ${item.ticker}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${item.quantity} units • Bought at ${currencyFormat.format(item.purchasePrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(20.dp))

                if (isFetchingPrice) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GrowwGreen, modifier = Modifier.size(24.dp))
                    }
                } else {
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Current Market Price ($)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = growwTextFieldColors(),
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = sellQuantity,
                    onValueChange = { sellQuantity = it },
                    label = { Text("Quantity to Sell", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = growwTextFieldColors(),
                    singleLine = true
                )

                // Show estimated proceeds
                val price = sellPrice.toBigDecimalOrNull()
                val qty = sellQuantity.toBigDecimalOrNull()
                if (price != null && qty != null && price > BigDecimal.ZERO && qty > BigDecimal.ZERO) {
                    Spacer(Modifier.height(12.dp))
                    val proceeds = price.multiply(qty)
                    val gain = proceeds.subtract(item.purchasePrice.multiply(qty))
                    val isGain = gain >= BigDecimal.ZERO
                    Surface(shape = RoundedCornerShape(10.dp), color = if (isGain) GrowwGreenAlpha else GrowwRedAlpha) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Proceeds", style = MaterialTheme.typography.labelSmall, color = TextHint)
                                Text(currencyFormat.format(proceeds), fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("P&L", style = MaterialTheme.typography.labelSmall, color = TextHint)
                                Text(
                                    currencyFormat.format(gain),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGain) GrowwGreen else GrowwRed
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val p = sellPrice.toBigDecimalOrNull()
                            val q = sellQuantity.toBigDecimalOrNull()
                            if (p != null && q != null) {
                                onConfirm(p, q)
                            }
                        },
                        enabled = sellPrice.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true &&
                                  sellQuantity.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO && it <= item.quantity } == true,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowwRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirm Sell", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun growwTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = GrowwGreen,
    unfocusedBorderColor = GrowwSurface3,
    focusedLabelColor    = GrowwGreen,
    unfocusedLabelColor  = TextSecondary,
    cursorColor          = GrowwGreen,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary
)

@Composable
fun GrowwTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        colors = growwTextFieldColors(),
        singleLine = true
    )
}

// displayName() is defined in AssetType.kt
