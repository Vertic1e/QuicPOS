package com.quicpos.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quicpos.app.ui.theme.Green500
import com.quicpos.app.ui.theme.Green800

@Composable
fun AppDrawer(
    currentRoute: String,
    businessName: String,
    posRegisterName: String,
    onNavigate: (Screen) -> Unit,
    onLockScreen: () -> Unit,
    onBackOffice: () -> Unit = {},
    onSupport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // Profile Header
        DrawerHeader(
            businessName = businessName,
            posRegisterName = posRegisterName,
            onLockScreen = onLockScreen
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Primary Navigation
        Screen.primaryScreens.forEach { screen ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = if (currentRoute == screen.route) {
                            screen.selectedIcon ?: screen.icon!!
                        } else {
                            screen.icon!!
                        },
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Green800.copy(alpha = 0.2f),
                    selectedIconColor = Green500,
                    selectedTextColor = Green500,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Links
        SecondaryDrawerItem(
            icon = Icons.Outlined.Dashboard,
            label = "Back Office",
            onClick = onBackOffice
        )
        SecondaryDrawerItem(
            icon = Icons.Outlined.Apps,
            label = "Apps",
            onClick = { }
        )
        SecondaryDrawerItem(
            icon = Icons.Outlined.HelpOutline,
            label = "Support",
            onClick = onSupport
        )

        Spacer(modifier = Modifier.weight(1f))

        // Version Display
        Text(
            text = "QuicPOS v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(horizontal = 28.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun DrawerHeader(
    businessName: String,
    posRegisterName: String,
    onLockScreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Business Logo Placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Green800),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = businessName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = businessName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = posRegisterName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Lock Button
            IconButton(onClick = onLockScreen) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Lock Screen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SecondaryDrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
