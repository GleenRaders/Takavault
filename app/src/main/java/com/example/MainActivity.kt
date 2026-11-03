package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TransactionEntity
import com.example.ui.components.AddBkashDialog
import com.example.ui.components.AddCashVaultDialog
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.AddDbblDialog
import com.example.ui.components.AppBrandLogo
import com.example.ui.components.BkashBridgeDialog
import com.example.ui.components.CategoriesTabContent
import com.example.ui.components.DbblBridgeDialog
import com.example.ui.components.ExpenseWorkflowDialog
import com.example.ui.components.HistoryAndAnalyticsTabContent
import com.example.ui.components.HomeTabContent
import com.example.ui.components.NoteExchangeDialog
import com.example.ui.components.NotePreviewDialog
import com.example.ui.components.QuickCalculatorDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SettingsAndBackupDialog
import com.example.ui.components.ToolsTabContent
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TakaVaultTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BanglaFormatters
import com.example.util.BiometricAuthHelper
import com.example.util.CustomAssetManager
import com.example.util.PdfReportGenerator
import com.example.viewmodel.AppTab
import com.example.viewmodel.CyberToast
import com.example.viewmodel.HistoryFilterState
import com.example.viewmodel.TakaVaultUiState
import com.example.viewmodel.TakaVaultViewModel
import com.example.viewmodel.ToastVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: TakaVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            TakaVaultTheme(isLightTheme = state.walletConfig.themeMode == "light") {
                TakaVaultAppScreen(viewModel = viewModel, state = state)
            }
        }
    }
}

private data class PendingPdfRequest(
    val transactions: List<TransactionEntity>,
    val periodLabel: String
)

@Composable
fun TakaVaultAppScreen(
    viewModel: TakaVaultViewModel,
    state: TakaVaultUiState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toast by viewModel.toast.collectAsStateWithLifecycle()
    val isSessionUnlocked by viewModel.isSessionUnlocked.collectAsStateWithLifecycle()
    val expenseWorkflow by viewModel.expenseWorkflow.collectAsStateWithLifecycle()
    val showAddBkash by viewModel.showAddBkashModal.collectAsStateWithLifecycle()
    val showAddDbbl by viewModel.showAddDbblModal.collectAsStateWithLifecycle()
    val addCashNotes by viewModel.addCashNotes.collectAsStateWithLifecycle()
    val showAddCategory by viewModel.showAddCategoryModal.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettingsModal.collectAsStateWithLifecycle()
    val activeReceiptTx by viewModel.activeReceiptTx.collectAsStateWithLifecycle()
    val activePreviewDenom by viewModel.activePreviewDenom.collectAsStateWithLifecycle()
    val bkashBridge by viewModel.bkashBridge.collectAsStateWithLifecycle()
    val dbblBridge by viewModel.dbblBridge.collectAsStateWithLifecycle()
    val noteExchange by viewModel.noteExchange.collectAsStateWithLifecycle()
    val calculator by viewModel.calculator.collectAsStateWithLifecycle()

    // Observe custom asset version so UI recomposes whenever a custom image is imported or reset
    val assetVersion = CustomAssetManager.versionCounter.intValue

    // Pending file payloads for CreateDocument file backup & PDF export
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }
    var pendingCsvContent by remember { mutableStateOf<String?>(null) }
    var pendingPdfRequest by remember { mutableStateOf<PendingPdfRequest?>(null) }
    var pendingCustomAssetKey by remember { mutableStateOf<String?>(null) }

    // Back handler when on a secondary tab returns to Home tab
    BackHandler(enabled = state.currentTab != AppTab.HOME) {
        viewModel.switchTab(AppTab.HOME)
    }

    // 1. Real .JSON Backup File Creator (CreateDocument writes an actual file to device storage)
    val createJsonDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val jsonPayload = pendingBackupJson
        if (uri != null && !jsonPayload.isNullOrBlank()) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonPayload.toByteArray(Charsets.UTF_8))
                    out.flush()
                }
                viewModel.showToastMessage(
                    BanglaFormatters.tr(
                        "✓ ব্যাকআপ .JSON ফাইল সফলভাবে আপনার ডিভাইসে সেভ হয়েছে!",
                        "✓ Backup .JSON file saved to your device!"
                    ),
                    ToastVariant.SUCCESS
                )
            } catch (_: Exception) {
                viewModel.showToastMessage(
                    BanglaFormatters.tr("ফাইল সংরক্ষণ করতে সমস্যা হয়েছে!", "Failed to write backup file!"),
                    ToastVariant.WARN
                )
            }
        }
        pendingBackupJson = null
    }

    // 2. Real .CSV Spreadsheet Report File Creator
    val createCsvDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val csvPayload = pendingCsvContent
        if (uri != null && !csvPayload.isNullOrBlank()) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csvPayload.toByteArray(Charsets.UTF_8))
                    out.flush()
                }
                viewModel.showToastMessage(
                    BanglaFormatters.tr(
                        "✓ লেনদেন রিপোর্ট (.CSV ফাইল) সফলভাবে সেভ হয়েছে!",
                        "✓ Transactions .CSV report saved to device!"
                    ),
                    ToastVariant.SUCCESS
                )
            } catch (_: Exception) {
                viewModel.showToastMessage(
                    BanglaFormatters.tr("CSV ফাইল সেভ করতে সমস্যা হয়েছে!", "Failed to save CSV file!"),
                    ToastVariant.WARN
                )
            }
        }
        pendingCsvContent = null
    }

    // 3. Real .PDF Transaction History Report File Creator
    val createPdfDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val req = pendingPdfRequest
        if (uri != null && req != null) {
            scope.launch {
                val success = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            PdfReportGenerator.writeHistoryPdf(
                                outputStream = out,
                                walletConfig = state.walletConfig,
                                cashTotal = state.cashTotal,
                                categories = state.categories,
                                transactions = req.transactions,
                                periodLabel = req.periodLabel
                            )
                        } ?: false
                    } catch (_: Exception) {
                        false
                    }
                }
                if (success) {
                    viewModel.showToastMessage(
                        BanglaFormatters.tr(
                            "📄 লেনদেন ইতিহাসের .PDF রিপোর্ট সফলভাবে সেভ হয়েছে! (${req.periodLabel})",
                            "📄 PDF History Report saved to device! (${req.periodLabel})"
                        ),
                        ToastVariant.SUCCESS
                    )
                } else {
                    viewModel.showToastMessage(
                        BanglaFormatters.tr("PDF ফাইল তৈরি করতে সমস্যা হয়েছে!", "Failed to write PDF file!"),
                        ToastVariant.WARN
                    )
                }
            }
        }
        pendingPdfRequest = null
    }

    // 4. JSON Backup File Restore Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonStr = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (jsonStr.isNotBlank()) {
                    viewModel.importBackupJson(jsonStr)
                }
            } catch (_: Exception) {
                viewModel.showToastMessage(
                    BanglaFormatters.tr("ফাইল পড়তে সমস্যা হয়েছে!", "Failed to read backup file!"),
                    ToastVariant.WARN
                )
            }
        }
    }

    // 5. Custom Image Importer Launcher for Logos & Banknotes
    val pickCustomImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val targetKey = pendingCustomAssetKey
        if (uri != null && !targetKey.isNullOrBlank()) {
            val ok = CustomAssetManager.importCustomImage(context, targetKey, uri)
            if (ok) {
                viewModel.showToastMessage(
                    BanglaFormatters.tr(
                        "🖼️ কাস্টম ছবি সফলভাবে ইমপোর্ট ও সংরক্ষিত হয়েছে!",
                        "🖼️ Custom image imported and applied!"
                    ),
                    ToastVariant.SUCCESS
                )
            } else {
                viewModel.showToastMessage(
                    BanglaFormatters.tr("ছবি ইমপোর্ট করতে সমস্যা হয়েছে!", "Could not load selected image!"),
                    ToastVariant.WARN
                )
            }
        }
        pendingCustomAssetKey = null
    }

    val isFullMode = state.walletConfig.layoutMode == "full"
    val scrollState = rememberScrollState()

    LaunchedEffect(state.currentTab) {
        if (!isFullMode && scrollState.value > 0) {
            scrollState.scrollTo(0)
        }
    }

    // Reference assetVersion so top-level tree recomposes on custom image updates
    if (assetVersion >= 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
        ) {
            // Biometric Lock Screen Gate on App Launch when enabled
            if (state.walletConfig.biometricLockEnabled && !isSessionUnlocked) {
                BiometricLaunchLockScreen(
                    savedPin = state.walletConfig.biometricPinCode,
                    onUnlocked = {
                        viewModel.unlockVaultSession()
                        viewModel.showToastMessage(
                            BanglaFormatters.tr(
                                "🔓 বায়োমেট্রিক যাচাই সফল! টাকাভল্ট আনলক হয়েছে",
                                "🔓 Biometric verification successful! Vault unlocked"
                            ),
                            ToastVariant.SUCCESS
                        )
                    },
                    onAuthError = { msg ->
                        viewModel.showToastMessage(msg, ToastVariant.WARN)
                    }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ObsidianBg,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TakaVaultTopBar(
                            state = state,
                            onQuickSpendClick = { viewModel.openExpenseModal() },
                            onOpenCalculator = { viewModel.openCalculatorModal() },
                            onToggleTheme = { viewModel.toggleThemeMode() },
                            onToggleLayoutMode = { viewModel.toggleLayoutMode() },
                            onToggleSound = { viewModel.toggleSoundEnabled() },
                            onLockNow = { viewModel.lockVaultSessionNow() },
                            onOpenSettings = { viewModel.openSettingsModal() }
                        )
                    },
                    bottomBar = {
                        TakaVaultBottomNavigationBar(
                            currentTab = state.currentTab,
                            hasActiveDebts = state.debts.isNotEmpty(),
                            onSelectTab = { viewModel.switchTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 720.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isFullMode || state.currentTab == AppTab.HOME) {
                                HomeTabContent(
                                    state = state,
                                    onOpenSettings = { viewModel.openSettingsModal() },
                                    onToggleBkashVisibility = { viewModel.toggleBkashBalanceVisibility() },
                                    onOpenAddBkash = { viewModel.openAddBkashModal() },
                                    onToggleDbblVisibility = { viewModel.toggleDbblBalanceVisibility() },
                                    onOpenAddDbbl = { viewModel.openAddDbblModal() },
                                    onOpenDbblBridge = { mode -> viewModel.openDbblBridgeModal(mode) },
                                    onOpenExpenseModal = { catId, method -> viewModel.openExpenseModal(catId, method) },
                                    onOpenBkashBridge = { mode -> viewModel.openBkashBridgeModal(mode) },
                                    onOpenNoteExchange = { viewModel.openNoteExchangeModal() },
                                    onOpenAddCash = { viewModel.openAddCashModal() },
                                    onOpenNotePreview = { denom -> viewModel.openNotePreview(denom) },
                                    onSwitchTab = { tab -> viewModel.switchTab(tab) },
                                    onOpenAddCategory = { viewModel.openAddCategoryModal() },
                                    onOpenReceipt = { tx -> viewModel.openReceiptModal(tx) },
                                    onSettleDebt = { debt -> viewModel.settleDebtEntry(debt) },
                                    onAddTempMoney = { t, a, w -> viewModel.addTempMoney(t, a, w) },
                                    onToggleTempMoney = { item -> viewModel.toggleTempMoney(item) },
                                    onDeleteTempMoney = { id -> viewModel.deleteTempMoney(id) }
                                )
                            }

                            if (isFullMode || state.currentTab == AppTab.CATEGORIES) {
                                CategoriesTabContent(
                                    state = state,
                                    onCategoryClick = { catId -> viewModel.openExpenseModal(catId) },
                                    onAddCategory = { viewModel.openAddCategoryModal() },
                                    onQuickPresetExpense = { catId, title, amt ->
                                        viewModel.openExpenseModal(
                                            categoryId = catId,
                                            presetTitle = title,
                                            presetAmount = amt
                                        )
                                    }
                                )
                            }

                            if (isFullMode || state.currentTab == AppTab.TOOLS) {
                                ToolsTabContent(
                                    state = state,
                                    onAddDebt = { type, person, amt -> viewModel.addManualDebtEntry(type, person, amt) },
                                    onSettleDebt = { debt -> viewModel.settleDebtEntry(debt) },
                                    onDeleteDebt = { id -> viewModel.deleteDebtEntry(id) },
                                    onQuickFeeInputChange = { viewModel.updateQuickBkashFeeInput(it) },
                                    onOpenBkashBridge = { mode -> viewModel.openBkashBridgeModal(mode) },
                                    onOpenDbblBridge = { mode -> viewModel.openDbblBridgeModal(mode) },
                                    onOpenNoteExchange = { viewModel.openNoteExchangeModal() }
                                )
                            }

                            if (isFullMode || state.currentTab == AppTab.HISTORY) {
                                val filteredTransactions = remember(
                                    state.transactions,
                                    state.filterState,
                                    state.categories
                                ) {
                                    viewModel.filteredTransactions(state)
                                }
                                HistoryAndAnalyticsTabContent(
                                    state = state,
                                    filteredTransactions = filteredTransactions,
                                    onSearchQueryChange = { viewModel.updateFilterSearch(it) },
                                    onMethodFilterChange = { viewModel.updateFilterMethod(it) },
                                    onCategoryFilterChange = { viewModel.updateFilterCategory(it) },
                                    onDateFilterChange = { viewModel.updateFilterDate(it) },
                                    onCustomDaysChange = { days -> viewModel.updateFilterCustomDays(days) },
                                    onExportPdfClick = {
                                        val label = viewModel.periodLabelForFilter(state.filterState)
                                        pendingPdfRequest = PendingPdfRequest(
                                            transactions = filteredTransactions,
                                            periodLabel = label
                                        )
                                        val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                        createPdfDocumentLauncher.launch("takavault_history_$dateTag.pdf")
                                    },
                                    onOpenReceipt = { tx -> viewModel.openReceiptModal(tx) },
                                    onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // All Interactive Dialogs / Modals
                ExpenseWorkflowDialog(
                    state = state,
                    workflow = expenseWorkflow,
                    onDismiss = { viewModel.closeExpenseModal() },
                    onCategoryChange = { viewModel.updateExpenseCategory(it) },
                    onSelectMethod = { viewModel.selectExpenseMethod(it) },
                    onGoToStep = { viewModel.goToExpenseStep(it) },
                    onBillAmountChange = { viewModel.updateExpenseBillAmount(it) },
                    onBillPresetClick = { viewModel.setExpenseBillPreset(it) },
                    onNoteChange = { viewModel.updateExpenseNote(it) },
                    onConfirmBkashSpend = { viewModel.confirmBkashExpense() },
                    onConfirmDbblSpend = { viewModel.confirmDbblExpense() },
                    onAdjustTenderNote = { d, delta -> viewModel.adjustTenderNote(d, delta) },
                    onAutoSelectTender = { viewModel.autoSelectTenderNotes() },
                    onClearTender = { viewModel.clearTenderNotes() },
                    onProceedToChangeStep = { viewModel.proceedFromTenderToChange() },
                    onAdjustReceivedNote = { d, delta -> viewModel.adjustReceivedNote(d, delta) },
                    onAutoFillChange = { viewModel.autoFillReceivedChange() },
                    onClearReceived = { viewModel.clearReceivedNotes() },
                    onCommitCashExpense = { viewModel.commitCashExpenseTransaction() }
                )

                AddBkashDialog(
                    isOpen = showAddBkash,
                    onDismiss = { viewModel.closeAddBkashModal() },
                    onConfirm = { amt, src -> viewModel.confirmAddBkashMoney(amt, src) }
                )

                AddDbblDialog(
                    isOpen = showAddDbbl,
                    accountNo = state.walletConfig.dbblAccountNo,
                    onDismiss = { viewModel.closeAddDbblModal() },
                    onConfirm = { amt, src -> viewModel.confirmAddDbblMoney(amt, src) }
                )

                DbblBridgeDialog(
                    state = dbblBridge,
                    dbblBalance = state.walletConfig.dbblBalance,
                    bkashBalance = state.walletConfig.bkashBalance,
                    onDismiss = { viewModel.closeDbblBridgeModal() },
                    onSwitchMode = { viewModel.openDbblBridgeModal(it) },
                    onAmountChange = { viewModel.updateDbblBridgeAmount(it) },
                    onAutoFillNotes = { viewModel.autoFillDbblBridgeNotes() },
                    onAdjustNote = { d, delta -> viewModel.adjustDbblBridgeNote(d, delta) },
                    onConfirmTransfer = { viewModel.confirmDbblBridgeTransfer() }
                )

                AddCashVaultDialog(
                    tempNotes = addCashNotes,
                    onDismiss = { viewModel.closeAddCashModal() },
                    onAdjustNote = { d, delta -> viewModel.adjustAddCashTempNote(d, delta) },
                    onConfirm = { src -> viewModel.confirmAddCashNotes(src) }
                )

                AddCategoryDialog(
                    isOpen = showAddCategory,
                    onDismiss = { viewModel.closeAddCategoryModal() },
                    onConfirm = { name, icon -> viewModel.confirmAddCategory(name, icon) }
                )

                SettingsAndBackupDialog(
                    isOpen = showSettings,
                    walletConfig = state.walletConfig,
                    onDismiss = { viewModel.closeSettingsModal() },
                    onToggleTheme = { viewModel.toggleThemeMode() },
                    onSetLanguage = { viewModel.setLanguage(it) },
                    onToggleLayoutMode = { viewModel.toggleLayoutMode() },
                    onToggleSound = { viewModel.toggleSoundEnabled() },
                    onToggleBiometricLock = { viewModel.toggleBiometricLockEnabled() },
                    onSaveBiometricPin = { viewModel.saveBiometricPin(it) },
                    onLockAppNow = { viewModel.lockVaultSessionNow() },
                    onToggleShowDbbl = { viewModel.toggleShowDbblSection() },
                    onSaveDbblAccountNo = { viewModel.saveDbblAccountNo(it) },
                    onToggleShowTempMoney = { viewModel.toggleShowTempMoneySection() },
                    onSaveLowBhangtiThreshold = { viewModel.saveLowBhangtiThreshold(it) },
                    onSaveBudget = { viewModel.saveMonthlyBudget(it) },
                    onSaveSavingsGoal = { viewModel.saveSavingsGoal(it) },
                    onPickCustomImageForKey = { key ->
                        pendingCustomAssetKey = key
                        pickCustomImageLauncher.launch("image/*")
                    },
                    onResetCustomImageKey = { key ->
                        CustomAssetManager.clearCustomAsset(context, key)
                        viewModel.showToastMessage(
                            BanglaFormatters.tr("ডিফল্ট ছবিতে ফিরিয়ে নেওয়া হয়েছে", "Reset to default artwork"),
                            ToastVariant.INFO
                        )
                    },
                    onResetAllCustomImages = {
                        CustomAssetManager.clearAllCustomAssets(context)
                        viewModel.showToastMessage(
                            BanglaFormatters.tr("সকল কাস্টম লোগো ও নোটের ছবি রিসেট করা হয়েছে", "All custom images reset"),
                            ToastVariant.INFO
                        )
                    },
                    onSaveJsonFileClick = {
                        viewModel.exportBackupJson { jsonStr ->
                            pendingBackupJson = jsonStr
                            val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            createJsonDocumentLauncher.launch("takavault_backup_$dateTag.json")
                        }
                    },
                    onSaveCsvFileClick = {
                        viewModel.exportTransactionsCsv { csvStr ->
                            pendingCsvContent = csvStr
                            val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            createCsvDocumentLauncher.launch("takavault_report_$dateTag.csv")
                        }
                    },
                    onExportPdfWithPeriod = { periodKey, customDays ->
                        val tempFilter = HistoryFilterState(
                            dateFilter = periodKey,
                            customDaysCount = customDays
                        )
                        val periodTxs = viewModel.filteredTransactions(
                            state.copy(filterState = tempFilter)
                        )
                        val label = viewModel.periodLabelForFilter(tempFilter)
                        pendingPdfRequest = PendingPdfRequest(
                            transactions = periodTxs,
                            periodLabel = label
                        )
                        val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                        createPdfDocumentLauncher.launch("takavault_history_${periodKey}_$dateTag.pdf")
                    },
                    onImportJsonClick = {
                        importLauncher.launch("*/*")
                    },
                    onHardResetZero = { viewModel.executeHardResetZero() },
                    onReloadDemoData = { viewModel.loadRichDemoData() }
                )

                ReceiptDialog(
                    tx = activeReceiptTx,
                    state = state,
                    onDismiss = { viewModel.closeReceiptModal() }
                )

                NotePreviewDialog(
                    denom = activePreviewDenom,
                    count = activePreviewDenom?.let { state.notes[it] ?: 0 } ?: 0,
                    onDismiss = { viewModel.closeNotePreview() },
                    onMinus = { activePreviewDenom?.let { viewModel.adjustVaultNote(it, -1) } },
                    onPlus = { activePreviewDenom?.let { viewModel.adjustVaultNote(it, 1) } },
                    onPickCustomNoteImage = { d ->
                        pendingCustomAssetKey = CustomAssetManager.noteKey(d)
                        pickCustomImageLauncher.launch("image/*")
                    }
                )

                BkashBridgeDialog(
                    state = bkashBridge,
                    onDismiss = { viewModel.closeBkashBridgeModal() },
                    onSwitchMode = { viewModel.openBkashBridgeModal(it) },
                    onAmountChange = { viewModel.updateBridgeAmount(it) },
                    onFeeRateChange = { viewModel.updateBridgeFeeRate(it) },
                    onAutoFillNotes = { viewModel.autoFillBridgeNotes() },
                    onAdjustNote = { d, delta -> viewModel.adjustBridgeNote(d, delta) },
                    onConfirmTransfer = { viewModel.confirmBkashBridgeTransfer() }
                )

                NoteExchangeDialog(
                    state = noteExchange,
                    vaultNotes = state.notes,
                    onDismiss = { viewModel.closeNoteExchangeModal() },
                    onSelectSource = { viewModel.selectExchangeSource(it) },
                    onAutoBreak = { viewModel.autoBreakSelectedNote() },
                    onAdjustTarget = { d, delta -> viewModel.adjustExchangeTarget(d, delta) },
                    onConfirmExchange = { viewModel.confirmNoteExchange() }
                )

                QuickCalculatorDialog(
                    state = calculator,
                    onDismiss = { viewModel.closeCalculatorModal() },
                    onInputToken = { viewModel.calcInput(it) },
                    onUseResultForExpense = { viewModel.useCalcResultForExpense() }
                )
            }

            // Non-overlapping Toast Notification Banner (visible on both Lock screen and Main screen)
            AnimatedVisibility(
                visible = toast != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                toast?.let { ToastBanner(it) }
            }
        }
    }
}

/**
 * Biometric Authentication (Fingerprint Unlock) Screen shown on App Launch
 * when `walletConfig.biometricLockEnabled` is enabled.
 */
@Composable
private fun BiometricLaunchLockScreen(
    savedPin: String,
    onUnlocked: () -> Unit,
    onAuthError: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var enteredPin by remember { mutableStateOf("") }
    var showPinInput by remember { mutableStateOf(false) }
    val hardwareEnrolled = remember(context) { BiometricAuthHelper.isBiometricHardwareAvailable(context) }

    // Automatically launch native fingerprint prompt on screen appearance if hardware is enrolled
    LaunchedEffect(Unit) {
        if (hardwareEnrolled && activity != null) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                title = BanglaFormatters.tr("টাকাভল্ট ৪.০ আনলক করুন", "Unlock TakaVault 4.0"),
                subtitle = BanglaFormatters.tr(
                    "আপনার আর্থিক ডাটা সুরক্ষিত রাখতে ফিঙ্গারপ্রিন্ট ছোঁয়ান",
                    "Touch the fingerprint sensor to access your financial vault"
                ),
                cancelText = BanglaFormatters.tr("পিন ব্যবহার করুন", "Use Security PIN"),
                onSuccess = onUnlocked,
                onFallbackOrError = { msg ->
                    showPinInput = true
                    onAuthError(msg)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF060B14), Color(0xFF0D1829), Color(0xFF070C16))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
            .testTag("biometric_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SlateCardBg)
                .border(1.5.dp, NeonMint.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppBrandLogo(modifier = Modifier.size(56.dp))

            Text(
                text = BanglaFormatters.tr("🔐 টাকাভল্ট সিকিউরিটি শিল্ড", "🔐 TakaVault Biometric Shield"),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = BanglaFormatters.tr(
                    "আপনার বিকাশ, ডিবিবিএল ব্যাংক এবং নগদ ভাঙতি ভল্টের তথ্য সুরক্ষিত রয়েছে। আনলক করতে নিচের ফিঙ্গারপ্রিন্ট সেন্সরে ট্যাপ করুন।",
                    "Your bKash, DBBL Bank, and Cash Vault data is protected. Tap the fingerprint sensor below to authenticate."
                ),
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            // Interactive Fingerprint Sensor Pad
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonMint.copy(alpha = 0.28f),
                                NeonCyan.copy(alpha = 0.08f),
                                ObsidianDark
                            )
                        )
                    )
                    .border(2.dp, NeonMint, CircleShape)
                    .clickable {
                        if (activity != null) {
                            BiometricAuthHelper.authenticate(
                                activity = activity,
                                title = BanglaFormatters.tr("টাকাভল্ট ৪.০ আনলক করুন", "Unlock TakaVault 4.0"),
                                subtitle = BanglaFormatters.tr(
                                    "আপনার আর্থিক ডাটা সুরক্ষিত রাখতে ফিঙ্গারপ্রিন্ট ছোঁয়ান",
                                    "Touch the fingerprint sensor to access your financial vault"
                                ),
                                cancelText = BanglaFormatters.tr("পিন ব্যবহার করুন", "Use Security PIN"),
                                onSuccess = onUnlocked,
                                onFallbackOrError = {
                                    if (!hardwareEnrolled) {
                                        onUnlocked()
                                    } else {
                                        showPinInput = true
                                    }
                                }
                            )
                        } else {
                            onUnlocked()
                        }
                    }
                    .testTag("btn_fingerprint_unlock"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🫆", fontSize = 40.sp)
                    Text(
                        text = BanglaFormatters.tr("ফিঙ্গারপ্রিন্ট", "Fingerprint"),
                        color = NeonMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonMint, NeonCyan)
                        )
                    )
                    .clickable {
                        if (activity != null) {
                            BiometricAuthHelper.authenticate(
                                activity = activity,
                                title = BanglaFormatters.tr("টাকাভল্ট ৪.০ আনলক করুন", "Unlock TakaVault 4.0"),
                                subtitle = BanglaFormatters.tr(
                                    "আপনার আর্থিক ডাটা সুরক্ষিত রাখতে ফিঙ্গারপ্রিন্ট ছোঁয়ান",
                                    "Touch the fingerprint sensor to access your financial vault"
                                ),
                                cancelText = BanglaFormatters.tr("পিন ব্যবহার করুন", "Use Security PIN"),
                                onSuccess = onUnlocked,
                                onFallbackOrError = {
                                    if (!hardwareEnrolled) {
                                        onUnlocked()
                                    } else {
                                        showPinInput = true
                                    }
                                }
                            )
                        } else {
                            onUnlocked()
                        }
                    }
                    .padding(vertical = 12.dp)
                    .testTag("btn_trigger_biometric_prompt"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr(
                        "🔓 ফিঙ্গারপ্রিন্ট দিয়ে আনলক করুন",
                        "🔓 Authenticate with Fingerprint"
                    ),
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Backup PIN Unlock Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = BanglaFormatters.tr("অথবা ব্যাকআপ সিকিউরিটি পিন দিন:", "Or Unlock with Backup PIN:"),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!showPinInput) {
                        Text(
                            text = BanglaFormatters.tr("পিন লিখুন ▾", "Enter PIN ▾"),
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showPinInput = true }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it.take(8) },
                        label = {
                            Text(
                                BanglaFormatters.tr("সিকিউরিটি পিন", "Security PIN"),
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("biometric_pin_unlock_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurface)
                            .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                            .clickable {
                                val normalizedInput = enteredPin.trim()
                                if (normalizedInput == savedPin || (normalizedInput.isNotEmpty() && BanglaFormatters.parseBanglaNumber(normalizedInput).toString() == savedPin)) {
                                    onUnlocked()
                                } else {
                                    onAuthError(
                                        BanglaFormatters.tr(
                                            "ভুল পিন! সঠিক সিকিউরিটি পিন দিন অথবা ফিঙ্গারপ্রিন্ট ব্যবহার করুন",
                                            "Incorrect PIN! Please enter your backup PIN or use Fingerprint"
                                        )
                                    )
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("btn_pin_unlock"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("আনলক", "Unlock"),
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TakaVaultTopBar(
    state: TakaVaultUiState,
    onQuickSpendClick: () -> Unit,
    onOpenCalculator: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLayoutMode: () -> Unit,
    onToggleSound: () -> Unit,
    onLockNow: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianBg.copy(alpha = 0.95f))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                AppBrandLogo(modifier = Modifier.size(36.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.tr("টাকাভল্ট", "TakaVault"),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonMint.copy(alpha = 0.15f))
                                .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.tr("৪.০", "4.0"),
                                color = NeonMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = BanglaFormatters.tr("বিকাশ, ডিবিবিএল ও ভাঙতি ভল্ট", "bKash, DBBL & Bhangti Vault"),
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right utility icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (state.walletConfig.biometricLockEnabled) {
                    TopBarIconButton(
                        icon = "🔒",
                        testTag = "btn_quick_lock_top",
                        onClick = onLockNow
                    )
                }
                TopBarIconButton(
                    icon = "🧮",
                    testTag = "btn_open_quick_calc",
                    onClick = onOpenCalculator
                )
                TopBarIconButton(
                    icon = if (state.walletConfig.themeMode == "light") "🌙" else "☀️",
                    testTag = "btn_toggle_theme",
                    onClick = onToggleTheme
                )
                TopBarIconButton(
                    icon = if (state.walletConfig.layoutMode == "full") "🖥️" else "📱",
                    testTag = "btn_toggle_layout_mode",
                    onClick = onToggleLayoutMode
                )
                TopBarIconButton(
                    icon = if (state.walletConfig.soundEnabled) "🔊" else "🔇",
                    testTag = "btn_sound_toggle",
                    onClick = onToggleSound
                )
                TopBarIconButton(
                    icon = "⚙️",
                    testTag = "btn_open_settings",
                    onClick = onOpenSettings
                )
            }
        }

        // Second Sub-Bar: Net Worth Pill + Primary "⚡ নতুন খরচ" CTA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C1524))
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonMint)
                )
                Column {
                    Text(
                        text = if (state.walletConfig.showDbblSection) {
                            BanglaFormatters.tr("মোট তহবিল (বিকাশ + ডিবিবিএল + ভাঙতি)", "Total Net Worth (bKash + DBBL + Cash)")
                        } else {
                            BanglaFormatters.tr("মোট তহবিল (বিকাশ + ভাঙতি)", "Total Net Worth (bKash + Cash)")
                        },
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                    Text(
                        text = BanglaFormatters.formatBDT(state.netWorth),
                        color = NeonMint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonPink, BkashPink)
                        )
                    )
                    .clickable { onQuickSpendClick() }
                    .padding(horizontal = 16.dp)
                    .testTag("btn_quick_spend_header"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("⚡ নতুন খরচ", "⚡ Spend"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
    }
}

@Composable
private fun TopBarIconButton(
    icon: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SlateCardBg)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 14.sp)
    }
}

@Composable
private fun TakaVaultBottomNavigationBar(
    currentTab: AppTab,
    hasActiveDebts: Boolean,
    onSelectTab: (AppTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF080C15).copy(alpha = 0.96f))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppTab.entries.forEach { tab ->
                val selected = currentTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) NeonCyan.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (selected) NeonCyan.copy(alpha = 0.45f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectTab(tab) }
                        .testTag("nav_tab_${tab.key}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Text(text = tab.icon, fontSize = 16.sp)
                            if (tab == AppTab.TOOLS && hasActiveDebts) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(NeonPink)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                        Text(
                            text = tab.label,
                            color = if (selected) NeonCyan else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastBanner(toast: CyberToast) {
    val borderColor = when (toast.variant) {
        ToastVariant.SUCCESS -> NeonMint
        ToastVariant.INFO -> NeonCyan
        ToastVariant.WARN -> NeonPink
    }
    val bgColor = when (toast.variant) {
        ToastVariant.SUCCESS -> Color(0xFF0B191E)
        ToastVariant.INFO -> Color(0xFF0B1524)
        ToastVariant.WARN -> Color(0xFF240B18)
    }
    val icon = when (toast.variant) {
        ToastVariant.SUCCESS -> "✓"
        ToastVariant.INFO -> "◈"
        ToastVariant.WARN -> "⚠️"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor.copy(alpha = 0.96f))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, color = borderColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = toast.message, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
