package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import com.example.ui.components.AddDonationDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddKirtanDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DevotionalHeader
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TransparencyReportDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DonationsScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.HistoryAndEventsScreen
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.Localization
import com.example.ui.viewmodel.KirtanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: KirtanViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val devotionalPalette by viewModel.devotionalPalette.collectAsState()
            val appLanguage by viewModel.appLanguage.collectAsState()

            MyApplicationTheme(
                themeMode = themeMode,
                palette = devotionalPalette,
                language = appLanguage
            ) {
                KirtanApp(viewModel)
            }
        }
    }
}

enum class NavigationTab(val title: String, val hindiTitle: String) {
    DASHBOARD("Dashboard", "डैशबोर्ड"),
    DONATIONS("Donations", "दान (Daan)"),
    EXPENSES("Expenses", "खर्च (Kharcha)"),
    HISTORY("Logs & Events", "इतिहास (Logs)")
}

@Composable
fun KirtanApp(viewModel: KirtanViewModel = viewModel()) {
    val allKirtans by viewModel.allKirtans.collectAsState()
    val selectedKirtanId by viewModel.selectedKirtanId.collectAsState()
    val donations by viewModel.currentDonations.collectAsState()
    val expenses by viewModel.currentExpenses.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val historyLogs by viewModel.historyLogs.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val devotionalPalette by viewModel.devotionalPalette.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val localBackups by viewModel.localBackups.collectAsState()

    val colors = LocalAppColors.current
    val strings = Localization.get(appLanguage)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Dialog state
    var showAddDonationDialog by remember { mutableStateOf(false) }
    var editingDonation by remember { mutableStateOf<DonationEntity?>(null) }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showAddKirtanDialog by remember { mutableStateOf(false) }
    var editingKirtan by remember { mutableStateOf<KirtanEntity?>(null) }

    var viewingReceiptDonation by remember { mutableStateOf<DonationEntity?>(null) }
    var showTransparencyReportDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var deleteConfirmationTarget by remember { mutableStateOf<Any?>(null) }

    val currentKirtan = allKirtans.find { it.id == selectedKirtanId }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                DevotionalHeader(
                    currentKirtan = currentKirtan,
                    allKirtans = allKirtans,
                    currentThemeMode = themeMode,
                    currentLanguage = appLanguage,
                    onSelectKirtan = { viewModel.selectKirtan(it) },
                    onAddNewKirtan = {
                        editingKirtan = null
                        showAddKirtanDialog = true
                    },
                    onShareReport = { showTransparencyReportDialog = true },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.Divider(color = colors.navBorder, thickness = 0.8.dp)
                NavigationBar(
                    containerColor = colors.navBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_navigation_bar")
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.navActiveText,
                        selectedTextColor = colors.navActiveText,
                        indicatorColor = colors.navIndicator,
                        unselectedIconColor = colors.navInactiveText,
                        unselectedTextColor = colors.navInactiveText
                    )

                    // Tab 1: Dashboard
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.DASHBOARD,
                        onClick = { currentTab = NavigationTab.DASHBOARD },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = {
                            Text(
                                text = strings.tabDashboard,
                                fontSize = 10.5.sp,
                                fontWeight = if (currentTab == NavigationTab.DASHBOARD) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = itemColors,
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    // Tab 2: Donations
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.DONATIONS,
                        onClick = { currentTab = NavigationTab.DONATIONS },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.DONATIONS) Icons.Default.VolunteerActivism else Icons.Outlined.VolunteerActivism,
                                contentDescription = "Donations"
                            )
                        },
                        label = {
                            Text(
                                text = strings.tabDonations,
                                fontSize = 10.5.sp,
                                fontWeight = if (currentTab == NavigationTab.DONATIONS) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = itemColors,
                        modifier = Modifier.testTag("nav_item_donations")
                    )

                    // Tab 3: Expenses
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.EXPENSES,
                        onClick = { currentTab = NavigationTab.EXPENSES },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.EXPENSES) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Expenses"
                            )
                        },
                        label = {
                            Text(
                                text = strings.tabExpenses,
                                fontSize = 10.5.sp,
                                fontWeight = if (currentTab == NavigationTab.EXPENSES) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = itemColors,
                        modifier = Modifier.testTag("nav_item_expenses")
                    )

                    // Tab 4: History & Kirtans
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.HISTORY,
                        onClick = { currentTab = NavigationTab.HISTORY },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.HISTORY) Icons.Default.History else Icons.Outlined.History,
                                contentDescription = "Logs"
                            )
                        },
                        label = {
                            Text(
                                text = strings.tabHistory,
                                fontSize = 10.5.sp,
                                fontWeight = if (currentTab == NavigationTab.HISTORY) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = itemColors,
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    // Tab 5: Settings (Down Right Side)
                    NavigationBarItem(
                        selected = showSettingsDialog,
                        onClick = { showSettingsDialog = true },
                        icon = {
                            Icon(
                                imageVector = if (showSettingsDialog) Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = strings.settings
                            )
                        },
                        label = {
                            Text(
                                text = strings.settings,
                                fontSize = 10.5.sp,
                                fontWeight = if (showSettingsDialog) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = itemColors,
                        modifier = Modifier.testTag("nav_item_settings")
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
            when (currentTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        currentKirtan = currentKirtan,
                        summary = summary,
                        recentDonations = donations,
                        recentExpenses = expenses,
                        onAddDonation = {
                            editingDonation = null
                            showAddDonationDialog = true
                        },
                        onAddExpense = {
                            editingExpense = null
                            showAddExpenseDialog = true
                        },
                        onSelectDonation = { donation ->
                            viewingReceiptDonation = donation
                        },
                        onShareReport = {
                            showTransparencyReportDialog = true
                        },
                        onNavigateToDonations = { currentTab = NavigationTab.DONATIONS },
                        onNavigateToExpenses = { currentTab = NavigationTab.EXPENSES }
                    )
                }

                NavigationTab.DONATIONS -> {
                    DonationsScreen(
                        donations = donations,
                        onAddDonation = {
                            editingDonation = null
                            showAddDonationDialog = true
                        },
                        onViewReceipt = { donation ->
                            viewingReceiptDonation = donation
                        },
                        onEditDonation = { donation ->
                            editingDonation = donation
                            showAddDonationDialog = true
                        },
                        onDeleteDonation = { donation ->
                            deleteConfirmationTarget = donation
                        }
                    )
                }

                NavigationTab.EXPENSES -> {
                    ExpensesScreen(
                        expenses = expenses,
                        onAddExpense = {
                            editingExpense = null
                            showAddExpenseDialog = true
                        },
                        onEditExpense = { expense ->
                            editingExpense = expense
                            showAddExpenseDialog = true
                        },
                        onDeleteExpense = { expense ->
                            deleteConfirmationTarget = expense
                        }
                    )
                }

                NavigationTab.HISTORY -> {
                    HistoryAndEventsScreen(
                        logs = historyLogs,
                        kirtans = allKirtans,
                        selectedKirtanId = selectedKirtanId,
                        onSelectKirtan = { viewModel.selectKirtan(it) },
                        onAddNewKirtan = {
                            editingKirtan = null
                            showAddKirtanDialog = true
                        },
                        onEditKirtan = { kirtan ->
                            editingKirtan = kirtan
                            showAddKirtanDialog = true
                        },
                        onDeleteKirtan = { kirtan ->
                            deleteConfirmationTarget = kirtan
                        },
                        onToggleKirtanStatus = { kirtan ->
                            viewModel.updateKirtan(kirtan.copy(isCompleted = !kirtan.isCompleted))
                        },
                        onViewDonationReceipt = { donation ->
                            viewingReceiptDonation = donation
                        }
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // DIALOGS
    // -------------------------------------------------------------------------

    // Add / Edit Donation Dialog
    if (showAddDonationDialog) {
        AddDonationDialog(
            initialDonation = editingDonation,
            kirtans = allKirtans,
            selectedKirtanId = selectedKirtanId,
            onDismiss = {
                showAddDonationDialog = false
                editingDonation = null
            },
            onSave = { donorName, amount, mobile, mode, ref, notes, receivedBy, kirtanId, isPaymentReceived ->
                if (editingDonation != null) {
                    viewModel.updateDonation(
                        editingDonation!!.copy(
                            donorName = donorName,
                            amount = amount,
                            mobileNumber = mobile,
                            paymentMode = mode,
                            referenceId = ref,
                            notes = notes,
                            receivedBy = receivedBy,
                            kirtanId = kirtanId,
                            isPaymentReceived = isPaymentReceived
                        )
                    )
                } else {
                    viewModel.addDonation(
                        donorName = donorName,
                        amount = amount,
                        mobileNumber = mobile,
                        paymentMode = mode,
                        referenceId = ref,
                        dateMillis = System.currentTimeMillis(),
                        notes = notes,
                        receivedBy = receivedBy,
                        kirtanId = kirtanId,
                        isPaymentReceived = isPaymentReceived
                    )
                }
                showAddDonationDialog = false
                editingDonation = null
            }
        )
    }

    // Add / Edit Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            initialExpense = editingExpense,
            kirtans = allKirtans,
            selectedKirtanId = selectedKirtanId,
            onDismiss = {
                showAddExpenseDialog = false
                editingExpense = null
            },
            onSave = { title, category, amount, mode, paidTo, ref, notes, kirtanId ->
                if (editingExpense != null) {
                    viewModel.updateExpense(
                        editingExpense!!.copy(
                            title = title,
                            category = category,
                            amount = amount,
                            paymentMode = mode,
                            paidTo = paidTo,
                            referenceId = ref,
                            notes = notes,
                            kirtanId = kirtanId
                        )
                    )
                } else {
                    viewModel.addExpense(
                        title = title,
                        category = category,
                        amount = amount,
                        paymentMode = mode,
                        paidTo = paidTo,
                        referenceId = ref,
                        dateMillis = System.currentTimeMillis(),
                        notes = notes,
                        kirtanId = kirtanId
                    )
                }
                showAddExpenseDialog = false
                editingExpense = null
            }
        )
    }

    // Add / Edit Kirtan Dialog
    if (showAddKirtanDialog) {
        AddKirtanDialog(
            initialKirtan = editingKirtan,
            onDismiss = {
                showAddKirtanDialog = false
                editingKirtan = null
            },
            onSave = { name, organizer, location, notes ->
                if (editingKirtan != null) {
                    viewModel.updateKirtan(
                        editingKirtan!!.copy(
                            name = name,
                            organizer = organizer,
                            location = location,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addKirtan(
                        name = name,
                        organizer = organizer,
                        location = location,
                        dateMillis = System.currentTimeMillis(),
                        notes = notes
                    )
                }
                showAddKirtanDialog = false
                editingKirtan = null
            }
        )
    }

    // Donation Receipt Slip Dialog
    viewingReceiptDonation?.let { donation ->
        val kirtanName = allKirtans.find { it.id == donation.kirtanId }?.name ?: "कीर्तन सेवा"
        ReceiptDialog(
            donation = donation,
            kirtanName = kirtanName,
            receiptText = viewModel.generateDonorReceipt(donation),
            onDismiss = { viewingReceiptDonation = null },
            onEdit = {
                editingDonation = donation
                viewingReceiptDonation = null
                showAddDonationDialog = true
            },
            onDelete = {
                deleteConfirmationTarget = donation
                viewingReceiptDonation = null
            }
        )
    }

    // Transparency Report Dialog
    if (showTransparencyReportDialog) {
        TransparencyReportDialog(
            reportText = viewModel.generateTransparencyReport(),
            onDismiss = { showTransparencyReportDialog = false }
        )
    }

    // Settings (Theme, Language, Backup & Reset) Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentThemeMode = themeMode,
            currentPalette = devotionalPalette,
            currentLanguage = appLanguage,
            autoBackupEnabled = autoBackupEnabled,
            lastBackupTime = lastBackupTime,
            localBackups = localBackups,
            onThemeModeSelected = { viewModel.setThemeMode(it) },
            onPaletteSelected = { viewModel.setDevotionalPalette(it) },
            onLanguageSelected = { viewModel.setAppLanguage(it) },
            onToggleAutoBackup = { viewModel.setAutoBackupEnabled(it) },
            onBackupNow = {
                viewModel.createManualBackup { success, msg, _ ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            onRestoreFromUri = { uri ->
                viewModel.restoreFromUri(uri) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            onRestoreFromFile = { file ->
                viewModel.restoreFromLocalFile(file) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            onDeleteBackup = { file ->
                viewModel.deleteLocalBackup(file)
            },
            onDefaultReset = { keepSample ->
                viewModel.defaultReset(keepSample) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Confirm Delete Dialog
    deleteConfirmationTarget?.let { target ->
        when (target) {
            is DonationEntity -> {
                ConfirmDeleteDialog(
                    title = "दान रिकॉर्ड हटाएं?",
                    message = "क्या आप ${target.donorName} जी का ₹${target.amount.toInt()} का दान रिकॉर्ड हटाना चाहते हैं?",
                    onConfirm = {
                        viewModel.deleteDonation(target)
                        deleteConfirmationTarget = null
                    },
                    onDismiss = { deleteConfirmationTarget = null }
                )
            }
            is ExpenseEntity -> {
                ConfirmDeleteDialog(
                    title = "खर्चा रिकॉर्ड हटाएं?",
                    message = "क्या आप '${target.title}' (₹${target.amount.toInt()}) का खर्चा रिकॉर्ड हटाना चाहते हैं?",
                    onConfirm = {
                        viewModel.deleteExpense(target)
                        deleteConfirmationTarget = null
                    },
                    onDismiss = { deleteConfirmationTarget = null }
                )
            }
            is KirtanEntity -> {
                ConfirmDeleteDialog(
                    title = "कीर्तन आयोजन हटाएं?",
                    message = "क्या आप '${target.name}' और इससे जुड़े सभी दान एवं खर्च रिकॉर्ड हटाना चाहते हैं?",
                    onConfirm = {
                        viewModel.deleteKirtan(target)
                        deleteConfirmationTarget = null
                    },
                    onDismiss = { deleteConfirmationTarget = null }
                )
            }
            else -> {}
        }
    }
}

