package com.quicpos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quicpos.app.ui.components.QuicPOSTopBar
import com.quicpos.app.ui.navigation.*
import com.quicpos.app.ui.sales.SalesIntent
import com.quicpos.app.ui.sales.SalesViewModel
import com.quicpos.app.ui.settings.SettingsViewModel
import com.quicpos.app.ui.theme.AppTheme
import com.quicpos.app.ui.theme.QuicPOSTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    QuicPOSTheme(appTheme = AppTheme.fromString(settings.theme)) {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Sales.route

        // Determine if we should show the drawer and top bar
        val isPrimaryScreen = Screen.primaryScreens.any { it.route == currentRoute }
        val currentTitle = Screen.primaryScreens.find { it.route == currentRoute }?.title
            ?: when {
                currentRoute.startsWith("item_edit") -> "Edit Item"
                currentRoute.startsWith("receipt_detail") -> "Receipt"
                else -> ""
            }

        // Get ticket count from SalesViewModel
        val salesViewModel: SalesViewModel = hiltViewModel()
        val salesState by salesViewModel.uiState.collectAsStateWithLifecycle()

        var showCustomerDialog by remember { mutableStateOf(false) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    currentRoute = currentRoute,
                    businessName = settings.businessName,
                    posRegisterName = settings.posRegisterName,
                    onNavigate = { screen ->
                        scope.launch { drawerState.close() }
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Sales.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    onLockScreen = {
                        scope.launch { drawerState.close() }
                    },
                    onBackOffice = {
                        scope.launch { drawerState.close() }
                        if (currentRoute != Screen.BackOffice.route) {
                            navController.navigate(Screen.BackOffice.route) {
                                popUpTo(Screen.Sales.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            },
            gesturesEnabled = isPrimaryScreen
        ) {
            Scaffold(
                topBar = {
                    if (isPrimaryScreen) {
                        QuicPOSTopBar(
                            title = currentTitle,
                            ticketItemCount = salesState.ticketItemCount,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onTicketClick = {
                                navController.navigate(Screen.Ticket.route)
                            },
                            onAddCustomer = {
                                showCustomerDialog = true
                            }
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                QuicPOSNavGraph(
                    navController = navController,
                    salesViewModel = salesViewModel,
                    modifier = Modifier.padding(innerPadding)
                )

                if (showCustomerDialog) {
                    CustomerDialog(
                        initialName = salesState.customerName ?: "",
                        onDismiss = { showCustomerDialog = false },
                        onSave = { name ->
                            salesViewModel.onIntent(SalesIntent.SetCustomerName(name.ifBlank { null }))
                            showCustomerDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Customer") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Customer Name / Table #") },
                placeholder = { Text("e.g. John Doe or Table 5") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(name) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
