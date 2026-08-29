package com.quicpos.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quicpos.app.ui.backoffice.BackOfficeScreen
import com.quicpos.app.ui.items.*
import com.quicpos.app.ui.receipts.*
import com.quicpos.app.ui.sales.*
import com.quicpos.app.ui.settings.*

@Composable
fun QuicPOSNavGraph(
    navController: NavHostController,
    salesViewModel: SalesViewModel,
    modifier: Modifier = Modifier,
    onNavigateToDrawerItem: (Screen) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Sales.route,
        modifier = modifier
    ) {
        // Primary Screens
        composable(Screen.Sales.route) {
            SalesScreen(
                onNavigateToTicket = { navController.navigate(Screen.Ticket.route) },
                onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.route) },
                viewModel = salesViewModel
            )
        }

        composable(Screen.Receipts.route) {
            ReceiptsScreen(
                onReceiptClick = { receiptNumber ->
                    navController.navigate(Screen.ReceiptDetail.createRoute(receiptNumber))
                }
            )
        }

        composable(Screen.Items.route) {
            ItemsScreen(
                onItemClick = { itemId ->
                    navController.navigate(Screen.ItemEdit.createRoute(itemId))
                },
                onAddItem = {
                    navController.navigate(Screen.ItemEdit.createRoute(0L))
                },
                onManageCategories = {
                    navController.navigate(Screen.CategoryManage.route)
                },
                onManageModifiers = {
                    navController.navigate(Screen.ModifierManage.route)
                }
            )
        }

        composable(Screen.BackOffice.route) {
            BackOfficeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToPrinters = { navController.navigate(Screen.PrinterSetup.route) },
                onNavigateToTaxes = { navController.navigate(Screen.TaxSetup.route) }
            )
        }

        // Sub Screens
        composable(Screen.Ticket.route) {
            TicketScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCharge = { navController.navigate(Screen.Charge.route) },
                viewModel = salesViewModel
            )
        }

        composable(Screen.Charge.route) {
            ChargeScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaleComplete = {
                    navController.popBackStack(Screen.Sales.route, inclusive = false)
                },
                viewModel = salesViewModel
            )
        }

        composable(
            route = Screen.ItemEdit.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            ItemEditScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onScanBarcode = { navController.navigate(Screen.BarcodeScanner.route) }
            )
        }

        composable(Screen.CategoryManage.route) {
            CategoryManageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ModifierManage.route) {
            ModifierManageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PrinterSetup.route) {
            PrinterSetupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TaxSetup.route) {
            TaxSetupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeScanned = { barcode ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ReceiptDetail.route,
            arguments = listOf(navArgument("receiptNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val receiptNumber = backStackEntry.arguments?.getString("receiptNumber") ?: ""
            ReceiptDetailScreen(
                receiptNumber = receiptNumber,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
