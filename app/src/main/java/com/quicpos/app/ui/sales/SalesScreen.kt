package com.quicpos.app.ui.sales

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicpos.app.ui.components.*

@Composable
fun SalesScreen(
    onNavigateToTicket: () -> Unit,
    onNavigateToCharge: () -> Unit,
    onNavigateToScanner: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshSettings()
    }

    val gridCells = when (uiState.gridColumns) {
        2 -> GridCells.Fixed(2)
        3 -> GridCells.Fixed(3)
        4 -> GridCells.Fixed(4)
        else -> {
            val minSize = when (uiState.itemSize) {
                "SMALL" -> 90.dp
                "LARGE" -> 145.dp
                else -> 110.dp
            }
            GridCells.Adaptive(minSize = minSize)
        }
    }

    val gridSpacing = when (uiState.gridColumns) {
        2 -> 14.dp
        4 -> 8.dp
        else -> 10.dp
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        QuicPOSSearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.onIntent(SalesIntent.SearchItems(it)) },
            placeholder = "Search items...",
            onScanBarcode = onNavigateToScanner,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Category Filter Chips
        CategoryFilterChips(
            categories = uiState.categories.map { cat ->
                CategoryFilter(
                    id = cat.id,
                    name = cat.name,
                    colorHex = cat.colorHex
                )
            },
            selectedCategoryId = uiState.selectedCategoryId,
            onCategorySelected = { viewModel.onIntent(SalesIntent.SelectCategory(it)) }
        )

        // Item Catalog
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) "No items found" else "No items yet.\nAdd items from the Items menu.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            if (uiState.layoutMode == "GRID") {
                LazyVerticalGrid(
                    columns = gridCells,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing)
                ) {
                    items(
                        items = uiState.items,
                        key = { it.id }
                    ) { item ->
                        ItemCard(
                            item = item,
                            currencyCode = uiState.currencyCode,
                            isDualCurrencyEnabled = uiState.isDualCurrencyEnabled,
                            secondaryCurrencyCode = uiState.secondaryCurrencyCode,
                            exchangeRate = uiState.exchangeRate,
                            fontSizeScale = uiState.fontSizeScale,
                            itemSize = uiState.itemSize,
                            gridColumns = uiState.gridColumns,
                            onClick = { viewModel.onIntent(SalesIntent.AddItemToTicket(item)) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.items,
                        key = { it.id }
                    ) { item ->
                        ItemListRow(
                            item = item,
                            currencyCode = uiState.currencyCode,
                            isDualCurrencyEnabled = uiState.isDualCurrencyEnabled,
                            secondaryCurrencyCode = uiState.secondaryCurrencyCode,
                            exchangeRate = uiState.exchangeRate,
                            fontSizeScale = uiState.fontSizeScale,
                            onClick = { viewModel.onIntent(SalesIntent.AddItemToTicket(item)) }
                        )
                    }
                }
            }
        }

        // Charge Button (sticky bottom)
        AnimatedVisibility(
            visible = !uiState.isTicketEmpty,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                ChargeButton(
                    totalAmount = uiState.grandTotal,
                    currencyCode = uiState.currencyCode,
                    itemCount = uiState.ticketItemCount,
                    onClick = onNavigateToCharge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
