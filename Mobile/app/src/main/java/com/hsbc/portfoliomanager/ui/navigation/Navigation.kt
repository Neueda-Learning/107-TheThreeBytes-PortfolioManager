package com.hsbc.portfoliomanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hsbc.portfoliomanager.data.model.AssetType
import com.hsbc.portfoliomanager.ui.screens.*
import com.hsbc.portfoliomanager.ui.theme.*
import com.hsbc.portfoliomanager.ui.viewmodel.*

sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Holdings     : Screen("holdings")
    object Watchlist    : Screen("watchlist")
    object Transactions : Screen("transactions")
    object Analytics    : Screen("analytics")

    // Stock detail — route includes ticker and assetType
    // e.g. "stock_detail/AAPL/STOCK"
    object StockDetail  : Screen("stock_detail/{ticker}/{assetType}") {
        fun createRoute(ticker: String, assetType: AssetType) =
            "stock_detail/$ticker/${assetType.name}"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard,    Icons.Default.Home,           "Home"),
    BottomNavItem(Screen.Holdings,     Icons.Default.Inventory2,     "Holdings"),
    BottomNavItem(Screen.Watchlist,    Icons.Default.Visibility,     "Watchlist"),
    BottomNavItem(Screen.Transactions, Icons.Default.Receipt,        "Orders"),
    BottomNavItem(Screen.Analytics,    Icons.Default.BarChart,       "Analytics")
)

// Bottom nav routes — StockDetail is NOT part of bottom nav
private val bottomNavRoutes = bottomNavItems.map { it.screen.route }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioNavigation() {
    val navController = rememberNavController()
    val dashboardViewModel: DashboardViewModel     = viewModel()
    val portfolioViewModel: PortfolioViewModel     = viewModel()
    val watchlistViewModel: WatchlistViewModel     = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val performanceViewModel: PerformanceViewModel = viewModel()
    val riskTaxViewModel: RiskTaxViewModel         = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom nav on StockDetail screen
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = GrowwBg,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = BottomNavBg,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) GrowwGreen else TextHint
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) GrowwGreen else TextHint
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = GrowwGreen,
                                selectedTextColor   = GrowwGreen,
                                unselectedIconColor = TextHint,
                                unselectedTextColor = TextHint,
                                indicatorColor      = GrowwGreenAlpha
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Dashboard.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToHoldings = {
                        navController.navigate(Screen.Holdings.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToStockDetail = { ticker, assetType, item ->
                        navController.navigate(Screen.StockDetail.createRoute(ticker, assetType))
                    }
                )
            }

            composable(Screen.Holdings.route) {
                HoldingsScreen(
                    viewModel      = portfolioViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToStockDetail = { ticker, assetType, item ->
                        navController.navigate(Screen.StockDetail.createRoute(ticker, assetType))
                    }
                )
            }

            composable(Screen.Watchlist.route) {
                WatchlistScreen(viewModel = watchlistViewModel)
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(viewModel = transactionViewModel)
            }

            composable(Screen.Analytics.route) {
                MoreScreen(viewModel = riskTaxViewModel, performanceViewModel = performanceViewModel)
            }

            // ── Stock Detail Screen ───────────────────────────────────────────
            composable(
                route = Screen.StockDetail.route,
                arguments = listOf(
                    navArgument("ticker")    { type = NavType.StringType },
                    navArgument("assetType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val ticker    = backStackEntry.arguments?.getString("ticker") ?: ""
                val assetTypeStr = backStackEntry.arguments?.getString("assetType") ?: "STOCK"
                val assetType = runCatching { AssetType.valueOf(assetTypeStr) }.getOrDefault(AssetType.STOCK)

                // Find the matching portfolio item from the portfolio ViewModel's list
                val items by portfolioViewModel.uiState.collectAsState()
                val portfolioItem = items.items.find {
                    it.ticker.equals(ticker, ignoreCase = true) && it.assetType == assetType
                }

                val stockDetailViewModel: StockDetailViewModel = viewModel()

                StockDetailScreen(
                    ticker         = ticker,
                    assetType      = assetType,
                    portfolioItem  = portfolioItem,
                    viewModel      = stockDetailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
