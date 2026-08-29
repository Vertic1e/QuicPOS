package com.quicpos.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.quicpos.app.domain.model.Item
import com.quicpos.app.ui.theme.*

@Composable
fun ItemCard(
    item: Item,
    currencyCode: String = "KHR",
    isDualCurrencyEnabled: Boolean = false,
    secondaryCurrencyCode: String = "USD",
    exchangeRate: Double = 4000.0,
    fontSizeScale: Float = 1.0f,
    itemSize: String = "MEDIUM",
    gridColumns: Int = 3,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = item.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) }
        catch (e: Exception) { Green600 }
    } ?: Green600

    val hasImage = !item.imageUrl.isNullOrBlank()

    // Compute secondary price if dual currency is enabled
    val secondaryPriceText = if (isDualCurrencyEnabled && exchangeRate > 0) {
        val secondaryAmount = if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
            item.price / exchangeRate
        } else if (currencyCode == "USD" && secondaryCurrencyCode == "KHR") {
            item.price * exchangeRate
        } else {
            item.price / exchangeRate
        }
        val formatted = if (secondaryCurrencyCode == "KHR") {
            String.format("%,.0f", secondaryAmount)
        } else {
            String.format("%,.2f", secondaryAmount)
        }
        " ($secondaryCurrencyCode $formatted)"
    } else ""

    // Base font calculation dynamically scaling with columns and user font scale
    val baseTitleSize = when {
        gridColumns == 2 -> 22.0f
        gridColumns == 3 -> 17.0f
        gridColumns == 4 -> 13.5f
        itemSize == "LARGE" -> 20.0f
        itemSize == "SMALL" -> 12.0f
        else -> 15.5f
    }

    val basePriceSize = when {
        gridColumns == 2 -> 17.0f
        gridColumns == 3 -> 13.5f
        gridColumns == 4 -> 11.0f
        itemSize == "LARGE" -> 15.0f
        itemSize == "SMALL" -> 10.5f
        else -> 12.5f
    }

    val titleFontSize = (baseTitleSize * fontSizeScale).sp
    val priceFontSize = (basePriceSize * fontSizeScale).sp

    val cornerRadius = when {
        gridColumns == 2 || itemSize == "LARGE" -> 18.dp
        gridColumns == 4 || itemSize == "SMALL" -> 10.dp
        else -> 14.dp
    }

    val contentPadding = when {
        gridColumns == 2 -> 16.dp
        gridColumns == 4 -> 6.dp
        else -> 10.dp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = if (hasImage) MaterialTheme.colorScheme.surfaceContainerHigh else bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasImage) {
                // Background Photo
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.82f)
                                )
                            )
                        )
                )

                // Item Name at Center/Bottom
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.name,
                        fontSize = titleFontSize,
                        lineHeight = (titleFontSize.value * 1.18f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Price tag
                    Text(
                        text = "$currencyCode ${item.displayPrice}$secondaryPriceText",
                        fontSize = priceFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarningAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Item Name - Center (Fills the box nicely)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = contentPadding, vertical = contentPadding * 0.8f)
                        .padding(bottom = (basePriceSize * fontSizeScale * 1.5f).dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.name,
                        fontSize = titleFontSize,
                        lineHeight = (titleFontSize.value * 1.18f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price Overlay - Bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.38f),
                            RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                        )
                        .padding(horizontal = 6.dp, vertical = (4 + (gridColumns == 2).let { if (it) 4 else 0 }).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$currencyCode ${item.displayPrice}$secondaryPriceText",
                        fontSize = priceFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Stock Indicator - Top Right
            if (item.trackStock) {
                val stockColor = when {
                    item.isOutOfStock -> OutOfStockColor
                    item.isLowStock -> LowStockColor
                    else -> InStockColor
                }
                val dotSize = when {
                    gridColumns == 2 -> 14.dp
                    gridColumns == 4 -> 8.dp
                    else -> 10.dp
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(dotSize)
                        .clip(RoundedCornerShape(50))
                        .background(stockColor)
                )
            }
        }
    }
}

@Composable
fun ItemListRow(
    item: Item,
    currencyCode: String = "KHR",
    isDualCurrencyEnabled: Boolean = false,
    secondaryCurrencyCode: String = "USD",
    exchangeRate: Double = 4000.0,
    fontSizeScale: Float = 1.0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = item.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) }
        catch (e: Exception) { Green600 }
    } ?: Green600

    val hasImage = !item.imageUrl.isNullOrBlank()

    val secondaryPriceText = if (isDualCurrencyEnabled && exchangeRate > 0) {
        val secondaryAmount = if (currencyCode == "KHR" && secondaryCurrencyCode == "USD") {
            item.price / exchangeRate
        } else if (currencyCode == "USD" && secondaryCurrencyCode == "KHR") {
            item.price * exchangeRate
        } else {
            item.price / exchangeRate
        }
        val formatted = if (secondaryCurrencyCode == "KHR") {
            String.format("%,.0f", secondaryAmount)
        } else {
            String.format("%,.2f", secondaryAmount)
        }
        " ($secondaryCurrencyCode $formatted)"
    } else ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image or Initial Letter
            if (hasImage) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name.take(1).uppercase(),
                        fontSize = (18 * fontSizeScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Name & Stock
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = (16 * fontSizeScale).sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.trackStock) {
                    Text(
                        text = "Stock: ${item.stockQuantity.toLong()}",
                        fontSize = (12 * fontSizeScale).sp,
                        color = when {
                            item.isOutOfStock -> OutOfStockColor
                            item.isLowStock -> LowStockColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Price
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currencyCode ${item.displayPrice}",
                    fontSize = (16 * fontSizeScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Green500
                )
                if (secondaryPriceText.isNotBlank()) {
                    Text(
                        text = secondaryPriceText.trim(),
                        fontSize = (12 * fontSizeScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
