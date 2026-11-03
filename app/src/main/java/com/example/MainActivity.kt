package com.example

import android.content.Intent
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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddBkashDialog
import com.example.ui.components.AddCashVaultDialog
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.BkashBridgeDialog
import com.example.ui.components.CategoriesTabContent
import com.example.ui.components.ExpenseWorkflowDialog
import com.example.ui.components.HistoryAndAnalyticsTabContent
import com.example.ui.components.HomeTabContent
import com.example.ui.components.NoteExchangeDialog
import com.example.ui.components.NotePreviewDialog
import com.example.ui.components.QuickCalculatorDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SettingsAndBackupDialog
import com.example.ui.components.ToolsTabContent
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TakaVaultTheme
import com.example.ui.theme.TextSecondary
import com.example.util.BanglaFormatters
import com.example.viewmodel.AppTab
import com.example.viewmodel.CyberToast
import com.example.viewmodel.TakaVaultUiState
import com.example.viewmodel.TakaVaultViewModel
import com.example.viewmodel.ToastVariant

class MainActivity : ComponentActivity() {

    private val viewModel: TakaVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TakaVaultTheme {
                TakaVaultAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TakaVaultAppScreen(viewModel: TakaVaultViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()
    val expenseWorkflow by viewModel.expenseWorkflow.collectAsStateWithLifecycle()
    val showAddBkash by viewModel.showAddBkashModal.collectAsStateWithLifecycle()
    val addCashNotes by viewModel.addCashNotes.collectAsStateWithLifecycle()
    val showAddCategory by viewModel.showAddCategoryModal.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettingsModal.collectAsStateWithLifecycle()
    val activeReceiptTx by viewModel.activeReceiptTx.collectAsStateWithLifecycle()
    val activePreviewDenom by viewModel.activePreviewDenom.collectAsStateWithLifecycle()
    val bkashBridge by viewModel.bkashBridge.collectAsStateWithLifecycle()
    val noteExchange by viewModel.noteExchange.collectAsStateWithLifecycle()
    val calculator by viewModel.calculator.collectAsStateWithLifecycle()

    // Back handler when on a secondary tab returns to Home tab
    BackHandler(enabled = state.currentTab != AppTab.HOME) {
        viewModel.switchTab(AppTab.HOME)
    }

    // JSON Import Launcher
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
                viewModel.showToastMessage("ফাইল পড়তে সমস্যা হয়েছে!", ToastVariant.WARN)
            }
        }
    }

    val isFullMode = state.walletConfig.layoutMode == "full"
    val scrollState = rememberScrollState()

    LaunchedEffect(state.currentTab) {
        if (!isFullMode && scrollState.value > 0) {
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TakaVaultTopBar(
                    state = state,
                    onQuickSpendClick = { viewModel.openExpenseModal() },
                    onOpenCalculator = { viewModel.openCalculatorModal() },
                    onToggleLayoutMode = { viewModel.toggleLayoutMode() },
                    onToggleSound = { viewModel.toggleSoundEnabled() },
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
                            onOpenReceipt = { tx -> viewModel.openReceiptModal(tx) },
                            onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Non-overlapping Toast Notification Banner
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
            currentBudget = state.walletConfig.monthlyBudget,
            onDismiss = { viewModel.closeSettingsModal() },
            onSaveBudget = { viewModel.saveMonthlyBudget(it) },
            onExportJsonClick = {
                viewModel.exportBackupJson { jsonStr ->
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_TEXT, jsonStr)
                        putExtra(Intent.EXTRA_TITLE, "takavault-backup.json")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "ডাটা ব্যাকআপ সংরক্ষণ বা শেয়ার করুন"))
                }
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
            onPlus = { activePreviewDenom?.let { viewModel.adjustVaultNote(it, 1) } }
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
}

@Composable
private fun TakaVaultTopBar(
    state: TakaVaultUiState,
    onQuickSpendClick: () -> Unit,
    onOpenCalculator: () -> Unit,
    onToggleLayoutMode: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianBg.copy(alpha = 0.95f))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Two clean, clutter-free rows on mobile so nothing ever overlaps or truncates
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.25f),
                                    NeonMint.copy(alpha = 0.2f),
                                    BkashPink.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(18.dp)
                            .drawWithCache {
                                val w = size.width
                                val h = size.height
                                val stroke = Stroke(width = 2.dp.toPx())
                                val path = Path().apply {
                                    moveTo(w * 0.5f, h * 0.08f)
                                    lineTo(w * 0.08f, h * 0.29f)
                                    lineTo(w * 0.5f, h * 0.5f)
                                    lineTo(w * 0.92f, h * 0.29f)
                                    close()
                                }
                                onDrawBehind {
                                    drawPath(path, NeonCyan, style = stroke)
                                }
                            }
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "টাকাভল্ট",
                            color = Color.White,
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
                                text = "৪.০",
                                color = NeonMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "স্মার্ট বিকাশ ও ভাঙতি নোট ভল্ট",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right utility icons (42dp touch targets, neatly spaced)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TopBarIconButton(
                    icon = "🧮",
                    testTag = "btn_open_quick_calc",
                    onClick = onOpenCalculator
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
                    Text(text = "মোট তহবিল (বিকাশ + ভাঙতি)", color = TextSecondary, fontSize = 10.sp)
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
                    text = "⚡ নতুন খরচ",
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
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SlateCardBg)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 15.sp)
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
                            color = if (selected) NeonCyan else TextSecondary,
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
