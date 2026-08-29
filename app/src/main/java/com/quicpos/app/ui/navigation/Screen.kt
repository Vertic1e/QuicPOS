package com.quicpos.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null
) {
    // Primary Navigation
    data object Sales : Screen(
        route = "sales",
        title = "Sales",
        icon = Icons.Outlined.PointOfSale,
        selectedIcon = Icons.Filled.PointOfSale
    )

    data object Receipts : Screen(
        route = "receipts",
        title = "Receipts",
        icon = Icons.Outlined.Receipt,
        selectedIcon = Icons.Filled.Receipt
    )

    data object Items : Screen(
        route = "items",
        title = "Items",
        icon = Icons.Outlined.Inventory2,
        selectedIcon = Icons.Filled.Inventory2
    )

    data object BackOffice : Screen(
        route = "back_office",
        title = "Back Office",
        icon = Icons.Outlined.Analytics,
        selectedIcon = Icons.Filled.Analytics
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )

    // Sub-screens
    data object Ticket : Screen(
        route = "ticket",
        title = "Ticket"
    )

    data object Charge : Screen(
        route = "charge",
        title = "Charge"
    )

    data object ItemEdit : Screen(
        route = "item_edit/{itemId}",
        title = "Edit Item"
    ) {
        fun createRoute(itemId: Long = 0L) = "item_edit/$itemId"
    }

    data object CategoryManage : Screen(
        route = "category_manage",
        title = "Categories"
    )

    data object ModifierManage : Screen(
        route = "modifier_manage",
        title = "Modifiers"
    )

    data object PrinterSetup : Screen(
        route = "printer_setup",
        title = "Printers"
    )

    data object TaxSetup : Screen(
        route = "tax_setup",
        title = "Taxes"
    )

    data object BarcodeScanner : Screen(
        route = "barcode_scanner",
        title = "Scan Barcode"
    )

    data object ReceiptDetail : Screen(
        route = "receipt_detail/{receiptNumber}",
        title = "Receipt Detail"
    ) {
        fun createRoute(receiptNumber: String) = "receipt_detail/$receiptNumber"
    }

    companion object {
        val primaryScreens: List<Screen>
            get() = listOfNotNull(Sales, Receipts, Items, BackOffice, Settings)
    }
}
