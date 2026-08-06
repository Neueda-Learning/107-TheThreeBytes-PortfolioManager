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
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.ui.theme.*
import com.hsbc.portfoliomanager.ui.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: TransactionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

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
                        Text("Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${uiState.transactions.size} transactions", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(
                        onClick = { viewModel.loadTransactions() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GrowwSurface2)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            AnimatedVisibility(visible = uiState.successMessage != null) {
                Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(10.dp), color = GrowwGreenAlpha) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GrowwGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(uiState.successMessage ?: "", color = GrowwGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            when {
                uiState.isLoading -> GrowwLoadingScreen()
                uiState.transactions.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextHint)
                            Spacer(Modifier.height(16.dp))
                            Text("No transactions yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text("Buy or sell holdings to see transaction history", style = MaterialTheme.typography.bodySmall, color = TextHint, textAlign = TextAlign.Center)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.transactions) { txn ->
                            TransactionCard(txn = txn, currencyFormat = currencyFormat, onDelete = { viewModel.deleteTransaction(txn.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(txn: TransactionResponse, currencyFormat: NumberFormat, onDelete: () -> Unit) {
    val isBuy = txn.transactionType == TransactionType.BUY
    val typeColor = if (isBuy) GrowwGreen else GrowwRed
    val typeBg = if (isBuy) GrowwGreenAlpha else GrowwRedAlpha

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GrowwSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(typeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(txn.ticker, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Surface(shape = RoundedCornerShape(4.dp), color = typeBg) {
                        Text(
                            txn.transactionType.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = typeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    "${txn.quantity} units @ ${currencyFormat.format(txn.pricePerUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(txn.transactionDate.take(10), style = MaterialTheme.typography.labelSmall, color = TextHint)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    currencyFormat.format(txn.totalValue),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextHint, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
