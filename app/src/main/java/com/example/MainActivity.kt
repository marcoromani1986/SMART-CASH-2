package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dialogs.*
import com.example.ui.i18n.I18n
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.SmartCashViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SmartCashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.dataState.collectAsState()
            val lang = state.settings.language
            val isLocked by viewModel.isLocked.collectAsState()
            val currentRoute by viewModel.currentRoute.collectAsState()
            val selectedCbId by viewModel.selectedCashboxId.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }
            val snackMsg by viewModel.snackbarMessage.collectAsState()
            val snackAction by viewModel.snackbarAction.collectAsState()

            LaunchedEffect(snackMsg) {
                snackMsg?.let { msg ->
                    val result = snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = snackAction,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.triggerSnackbarAction()
                    } else {
                        viewModel.clearToast()
                    }
                }
            }

            val layoutDirection = if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                SmartCashTheme(themePreference = state.settings.theme) {
                    if (isLocked) {
                        LockScreen(viewModel = viewModel)
                    } else if (!state.settings.onboarded && state.cashboxes.isEmpty()) {
                        OnboardingScreen(viewModel = viewModel)
                    } else {
                        MainAppShell(
                            viewModel = viewModel,
                            currentRoute = currentRoute,
                            selectedCashboxId = selectedCbId,
                            snackbarHostState = snackbarHostState
                        )
                    }

                    // Dialogs & Sheets
                    val showActionChooser by viewModel.showActionChooser.collectAsState()
                    if (showActionChooser) {
                        ActionChooserSheet(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showActionChooser.value = false }
                        )
                    }

                    val showTxDialog by viewModel.showTransactionDialog.collectAsState()
                    val editingTx by viewModel.editingTransaction.collectAsState()
                    val presetType by viewModel.presetTransactionType.collectAsState()
                    if (showTxDialog) {
                        AddEditTransactionSheet(
                            transaction = editingTx,
                            presetType = presetType,
                            viewModel = viewModel,
                            onDismiss = {
                                viewModel.showTransactionDialog.value = false
                                viewModel.editingTransaction.value = null
                            }
                        )
                    }

                    val showTransfer by viewModel.showTransferDialog.collectAsState()
                    if (showTransfer) {
                        TransferSheet(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showTransferDialog.value = false }
                        )
                    }

                    val showCbDialog by viewModel.showCashboxDialog.collectAsState()
                    val editingCb by viewModel.editingCashbox.collectAsState()
                    if (showCbDialog) {
                        AddEditCashboxSheet(
                            cashbox = editingCb,
                            viewModel = viewModel,
                            onDismiss = {
                                viewModel.showCashboxDialog.value = false
                                viewModel.editingCashbox.value = null
                            }
                        )
                    }

                    val showBudgets by viewModel.showBudgetsDialog.collectAsState()
                    if (showBudgets) {
                        BudgetsManagerSheet(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showBudgetsDialog.value = false }
                        )
                    }

                    val showCatDialog by viewModel.showCategoryDialog.collectAsState()
                    val editingCat by viewModel.editingCategory.collectAsState()
                    if (showCatDialog) {
                        AddEditCategorySheet(
                            category = editingCat,
                            viewModel = viewModel,
                            onDismiss = {
                                viewModel.showCategoryDialog.value = false
                                viewModel.editingCategory.value = null
                            }
                        )
                    }

                    val showRecDialog by viewModel.showRecurringDialog.collectAsState()
                    val editingRec by viewModel.editingRecurring.collectAsState()
                    if (showRecDialog) {
                        AddEditRecurringSheet(
                            rule = editingRec,
                            viewModel = viewModel,
                            onDismiss = {
                                viewModel.showRecurringDialog.value = false
                                viewModel.editingRecurring.value = null
                            }
                        )
                    }

                    val showPinSetup by viewModel.showPinSetupDialog.collectAsState()
                    if (showPinSetup) {
                        PinSetupDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showPinSetupDialog.value = false }
                        )
                    }

                    val showBackup by viewModel.showBackupDialog.collectAsState()
                    if (showBackup) {
                        BackupDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showBackupDialog.value = false }
                        )
                    }

                    val showCsv by viewModel.showCsvExportDialog.collectAsState()
                    if (showCsv) {
                        CsvExportDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showCsvExportDialog.value = false }
                        )
                    }

                    val showRestore by viewModel.showRestoreDialog.collectAsState()
                    if (showRestore) {
                        RestoreDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showRestoreDialog.value = false }
                        )
                    }

                    val showClearAll by viewModel.showClearAllDialog.collectAsState()
                    if (showClearAll) {
                        ClearAllConfirmDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.showClearAllDialog.value = false }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    viewModel: SmartCashViewModel,
    currentRoute: String,
    selectedCashboxId: String?,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    // Back handling for nested screens
    BackHandler(enabled = currentRoute != "dashboard") {
        when (currentRoute) {
            "cashbox_detail" -> viewModel.navigateTo("cashboxes")
            "categories", "recurring" -> viewModel.navigateTo("settings")
            else -> viewModel.navigateTo("dashboard")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ColorInk),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SC",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = ColorBrassBright,
                                    fontFamily = SpaceMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = I18n.t("app_name", lang),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo("ledger") },
                        modifier = Modifier.testTag("top_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search transactions")
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo("settings") },
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { viewModel.navigateTo("dashboard") },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(I18n.t("nav_home", lang)) },
                        modifier = Modifier.testTag("nav_home")
                    )

                    // Cashboxes
                    NavigationBarItem(
                        selected = currentRoute == "cashboxes" || currentRoute == "cashbox_detail",
                        onClick = { viewModel.navigateTo("cashboxes") },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        label = { Text(I18n.t("nav_boxes", lang)) },
                        modifier = Modifier.testTag("nav_cashboxes")
                    )

                    // Middle empty spacer for floating FAB
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.showActionChooser.value = true },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .offset(y = (-8).dp)
                                    .clip(CircleShape)
                                    .background(ColorInk)
                                    .shadow(6.dp, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = ColorBrassBright,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        },
                        label = { Text("") },
                        modifier = Modifier.testTag("nav_fab")
                    )

                    // Ledger
                    NavigationBarItem(
                        selected = currentRoute == "ledger",
                        onClick = { viewModel.navigateTo("ledger") },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text(I18n.t("nav_ledger", lang)) },
                        modifier = Modifier.testTag("nav_ledger")
                    )

                    // Reports
                    NavigationBarItem(
                        selected = currentRoute == "reports",
                        onClick = { viewModel.navigateTo("reports") },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text(I18n.t("nav_reports", lang)) },
                        modifier = Modifier.testTag("nav_reports")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "dashboard" -> DashboardScreen(viewModel = viewModel)
                "cashboxes" -> CashboxesScreen(viewModel = viewModel)
                "cashbox_detail" -> {
                    if (selectedCashboxId != null) {
                        CashboxDetailScreen(cashboxId = selectedCashboxId, viewModel = viewModel)
                    } else {
                        CashboxesScreen(viewModel = viewModel)
                    }
                }
                "ledger" -> LedgerScreen(viewModel = viewModel)
                "reports" -> ReportsScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
                "categories" -> CategoriesScreen(viewModel = viewModel)
                "recurring" -> RecurringScreen(viewModel = viewModel)
                else -> DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
