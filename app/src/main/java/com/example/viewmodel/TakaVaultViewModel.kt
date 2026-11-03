package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BanknoteCatalog
import com.example.data.CategoryEntity
import com.example.data.DebtEntity
import com.example.data.TakaVaultDatabase
import com.example.data.TakaVaultRepository
import com.example.data.TempMoneyEntity
import com.example.data.TransactionEntity
import com.example.data.WalletConfigEntity
import com.example.util.BanglaFormatters
import com.example.util.CyberSoundType
import com.example.util.SoundHapticsHelper
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val key: String, val icon: String, val labelBn: String, val labelEn: String) {
    HOME("home", "🏠", "হোম ও ভল্ট", "Home & Vault"),
    CATEGORIES("categories", "⚡", "খরচ ও খাত", "Spend & Cats"),
    TOOLS("tools", "📒", "বাকি ও টুলস", "Tools & Debt"),
    HISTORY("history", "📊", "ইতিহাস ও চার্ট", "History & Stats");

    val label: String
        get() = BanglaFormatters.tr(labelBn, labelEn)
}

enum class ExpenseStep {
    STEP_1_METHOD,
    STEP_2A_BKASH,
    STEP_2B_TENDER,
    STEP_2B_CHANGE,
    STEP_2B_CONFIRM,
    STEP_2C_DBBL
}

enum class ToastVariant {
    SUCCESS, INFO, WARN
}

@Immutable
data class CyberToast(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val variant: ToastVariant = ToastVariant.SUCCESS
)

@Immutable
data class ExpenseWorkflowState(
    val isOpen: Boolean = false,
    val categoryId: String = "grocery",
    val method: String? = null, // "bkash", "cash", or "dbbl"
    val step: ExpenseStep = ExpenseStep.STEP_1_METHOD,
    val billAmountInput: String = "",
    val noteInput: String = "",
    val bkashError: String? = null,
    val tenderNotes: Map<Int, Int> = emptyMap(),
    val receivedNotes: Map<Int, Int> = emptyMap()
) {
    val billAmount: Int = BanglaFormatters.parseBanglaNumber(billAmountInput)
    val tenderTotal: Int = BanglaFormatters.calculateNotesTotal(tenderNotes)
    val receivedTotal: Int = BanglaFormatters.calculateNotesTotal(receivedNotes)
    val expectedChange: Int = max(0, tenderTotal - billAmount)
    val discrepancy: Int = receivedTotal - expectedChange
}

@Immutable
data class BkashBridgeState(
    val isOpen: Boolean = false,
    val mode: String = "cashout", // "cashout" or "cashin"
    val amountInput: String = "১০০০",
    val feeRatePercent: Double = 1.85,
    val notes: Map<Int, Int> = emptyMap()
) {
    val amount: Int = BanglaFormatters.parseBanglaNumber(amountInput)
    val feeAmount: Int = if (mode == "cashout") Math.round(amount * (feeRatePercent / 100.0)).toInt() else 0
    val notesTotal: Int = BanglaFormatters.calculateNotesTotal(notes)
}

@Immutable
data class DbblBridgeState(
    val isOpen: Boolean = false,
    val mode: String = "atm_withdraw", // "atm_withdraw", "to_bkash", "from_bkash"
    val amountInput: String = "১০০০",
    val notes: Map<Int, Int> = emptyMap()
) {
    val amount: Int = BanglaFormatters.parseBanglaNumber(amountInput)
    val notesTotal: Int = BanglaFormatters.calculateNotesTotal(notes)
}

@Immutable
data class NoteExchangeState(
    val isOpen: Boolean = false,
    val sourceDenom: Int = 1000,
    val targetNotes: Map<Int, Int> = emptyMap()
) {
    val targetTotal: Int = BanglaFormatters.calculateNotesTotal(targetNotes)
}

@Immutable
data class CalculatorState(
    val isOpen: Boolean = false,
    val expression: String = "",
    val lastResult: Int = 0
)

@Immutable
data class HistoryFilterState(
    val searchQuery: String = "",
    val methodFilter: String = "all", // "all", "cash", "bkash", "dbbl", "inflow"
    val categoryFilter: String = "all",
    val dateFilter: String = "all", // "all", "today", "week", "month", "year", "custom_days"
    val customDaysCount: Int = 7
)

@Immutable
data class TakaVaultUiState(
    val walletConfig: WalletConfigEntity = WalletConfigEntity(),
    val notes: Map<Int, Int> = BanglaFormatters.DENOMINATIONS.associateWith { 0 },
    val categories: List<CategoryEntity> = BanknoteCatalog.DEFAULT_CATEGORIES,
    val transactions: List<TransactionEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val tempMoney: List<TempMoneyEntity> = emptyList(),
    val currentTab: AppTab = AppTab.HOME,
    val filterState: HistoryFilterState = HistoryFilterState(),
    val quickCalcBkashInput: String = "১০০০"
) {
    val cashTotal: Int = BanglaFormatters.calculateNotesTotal(notes)
    val totalNoteCount: Int = BanglaFormatters.calculateNotesCount(notes)
    val activeDbblBalance: Int = if (walletConfig.showDbblSection) walletConfig.dbblBalance else 0
    val netWorth: Int = walletConfig.bkashBalance + cashTotal + activeDbblBalance
    val smallNoteCount: Int = (notes[20] ?: 0) + (notes[10] ?: 0) + (notes[5] ?: 0) + (notes[2] ?: 0) + (notes[1] ?: 0)
}

class TakaVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TakaVaultDatabase.getInstance(application)
    val repository = TakaVaultRepository(database.dao())
    private val soundHelper = SoundHapticsHelper(application)

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    private val _filterState = MutableStateFlow(HistoryFilterState())
    private val _quickCalcBkashInput = MutableStateFlow("১০০০")

    private val _toast = MutableStateFlow<CyberToast?>(null)
    val toast: StateFlow<CyberToast?> = _toast.asStateFlow()

    // Biometric session unlock state (when biometricLockEnabled is true, starts locked on app launch)
    private val _isSessionUnlocked = MutableStateFlow(false)
    val isSessionUnlocked: StateFlow<Boolean> = _isSessionUnlocked.asStateFlow()

    // Modal states
    private val _expenseWorkflow = MutableStateFlow(ExpenseWorkflowState())
    val expenseWorkflow: StateFlow<ExpenseWorkflowState> = _expenseWorkflow.asStateFlow()

    private val _showAddBkashModal = MutableStateFlow(false)
    val showAddBkashModal: StateFlow<Boolean> = _showAddBkashModal.asStateFlow()

    private val _showAddDbblModal = MutableStateFlow(false)
    val showAddDbblModal: StateFlow<Boolean> = _showAddDbblModal.asStateFlow()

    private val _addCashNotes = MutableStateFlow<Map<Int, Int>?>(null)
    val addCashNotes: StateFlow<Map<Int, Int>?> = _addCashNotes.asStateFlow()

    private val _showAddCategoryModal = MutableStateFlow(false)
    val showAddCategoryModal: StateFlow<Boolean> = _showAddCategoryModal.asStateFlow()

    private val _showSettingsModal = MutableStateFlow(false)
    val showSettingsModal: StateFlow<Boolean> = _showSettingsModal.asStateFlow()

    private val _activeReceiptTx = MutableStateFlow<TransactionEntity?>(null)
    val activeReceiptTx: StateFlow<TransactionEntity?> = _activeReceiptTx.asStateFlow()

    private val _activePreviewDenom = MutableStateFlow<Int?>(null)
    val activePreviewDenom: StateFlow<Int?> = _activePreviewDenom.asStateFlow()

    private val _bkashBridge = MutableStateFlow(BkashBridgeState())
    val bkashBridge: StateFlow<BkashBridgeState> = _bkashBridge.asStateFlow()

    private val _dbblBridge = MutableStateFlow(DbblBridgeState())
    val dbblBridge: StateFlow<DbblBridgeState> = _dbblBridge.asStateFlow()

    private val _noteExchange = MutableStateFlow(NoteExchangeState())
    val noteExchange: StateFlow<NoteExchangeState> = _noteExchange.asStateFlow()

    private val _calculator = MutableStateFlow(CalculatorState())
    val calculator: StateFlow<CalculatorState> = _calculator.asStateFlow()

    val uiState: StateFlow<TakaVaultUiState> = combine(
        repository.walletConfigFlow.distinctUntilChanged(),
        repository.noteCountsFlow.distinctUntilChanged(),
        repository.categoriesFlow.distinctUntilChanged(),
        repository.transactionsFlow.distinctUntilChanged(),
        repository.debtsFlow.distinctUntilChanged(),
        repository.tempMoneyFlow.distinctUntilChanged(),
        _currentTab,
        _filterState,
        _quickCalcBkashInput
    ) { values ->
        val config = (values[0] as? WalletConfigEntity) ?: WalletConfigEntity()
        BanglaFormatters.setLanguageMode(config.language)

        @Suppress("UNCHECKED_CAST")
        val noteList = values[1] as List<com.example.data.NoteCountEntity>
        @Suppress("UNCHECKED_CAST")
        val cats = (values[2] as List<CategoryEntity>).ifEmpty { BanknoteCatalog.DEFAULT_CATEGORIES }
        @Suppress("UNCHECKED_CAST")
        val txs = values[3] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val debts = values[4] as List<DebtEntity>
        @Suppress("UNCHECKED_CAST")
        val temps = values[5] as List<TempMoneyEntity>
        val tab = values[6] as AppTab
        val filter = values[7] as HistoryFilterState
        val quickFeeInput = values[8] as String

        val notesMap = HashMap<Int, Int>(16)
        for (d in BanglaFormatters.DENOMINATIONS) {
            notesMap[d] = 0
        }
        for (n in noteList) {
            notesMap[n.denom] = n.count
        }

        TakaVaultUiState(
            walletConfig = config,
            notes = notesMap,
            categories = cats,
            transactions = txs,
            debts = debts,
            tempMoney = temps,
            currentTab = tab,
            filterState = filter,
            quickCalcBkashInput = quickFeeInput
        )
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TakaVaultUiState()
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureInitializedWithDemoIfNeeded()
        }
    }

    fun playSound(type: CyberSoundType = CyberSoundType.TAP) {
        soundHelper.play(type, uiState.value.walletConfig.soundEnabled)
    }

    fun showToastMessage(message: String, variant: ToastVariant = ToastVariant.SUCCESS) {
        val item = CyberToast(message = message, variant = variant)
        _toast.value = item
        viewModelScope.launch {
            delay(3000)
            if (_toast.value?.id == item.id) {
                _toast.value = null
            }
        }
    }

    fun switchTab(tab: AppTab) {
        playSound(CyberSoundType.TAP)
        _currentTab.value = tab
        if (uiState.value.walletConfig.layoutMode == "full") {
            viewModelScope.launch {
                repository.updateWalletConfig { it.copy(layoutMode = "tabs") }
            }
        }
    }

    fun toggleLayoutMode() {
        playSound(CyberSoundType.TAP)
        val current = uiState.value.walletConfig.layoutMode
        val next = if (current == "tabs") "full" else "tabs"
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(layoutMode = next) }
            showToastMessage(
                if (next == "full") BanglaFormatters.tr("ফুল ড্যাশবোর্ড ভিউ চালু করা হয়েছে", "Full Dashboard View Enabled")
                else BanglaFormatters.tr("অ্যান্ড্রয়েড ট্যাব ভিউ চালু হয়েছে", "Compact Tab View Enabled"),
                ToastVariant.INFO
            )
        }
    }

    fun toggleThemeMode() {
        playSound(CyberSoundType.TAP)
        val current = uiState.value.walletConfig.themeMode
        val next = if (current == "light") "dark" else "light"
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(themeMode = next) }
            showToastMessage(
                if (next == "light") BanglaFormatters.tr("☀️ লাইট থিম চালু করা হয়েছে", "☀️ Light Theme Enabled")
                else BanglaFormatters.tr("🌙 ডার্ক থিম চালু করা হয়েছে", "🌙 Dark Theme Enabled"),
                ToastVariant.INFO
            )
        }
    }

    fun setThemeMode(mode: String) {
        val normalized = if (mode == "light") "light" else "dark"
        playSound(CyberSoundType.TAP)
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(themeMode = normalized) }
        }
    }

    fun setLanguage(lang: String) {
        val normalized = if (lang == "en") "en" else "bn"
        BanglaFormatters.setLanguageMode(normalized)
        playSound(CyberSoundType.TAP)
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(language = normalized) }
            showToastMessage(
                if (normalized == "en") "Language switched to English"
                else "ভাষা বাংলাতে পরিবর্তন করা হয়েছে",
                ToastVariant.INFO
            )
        }
    }

    fun toggleShowDbblSection() {
        playSound(CyberSoundType.TAP)
        val next = !uiState.value.walletConfig.showDbblSection
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(showDbblSection = next) }
            showToastMessage(
                if (next) BanglaFormatters.tr("🏦 ডিবিবিএল ব্যাংক ও কার্ড সেকশন চালু হয়েছে", "🏦 DBBL Bank & Card section shown")
                else BanglaFormatters.tr("🏦 ডিবিবিএল ব্যাংক ও কার্ড সেকশন লুকানো হয়েছে", "🏦 DBBL Bank & Card section hidden"),
                ToastVariant.INFO
            )
        }
    }

    fun toggleShowTempMoneySection() {
        playSound(CyberSoundType.TAP)
        val next = !uiState.value.walletConfig.showTempMoneySection
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(showTempMoneySection = next) }
        }
    }

    fun toggleSoundEnabled() {
        val next = !uiState.value.walletConfig.soundEnabled
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(soundEnabled = next) }
            if (next) soundHelper.play(CyberSoundType.TAP, true)
            showToastMessage(
                if (next) BanglaFormatters.tr("অডিও ও হ্যাপটিক ভাইব চালু করা হয়েছে", "Sound & Haptics Enabled")
                else BanglaFormatters.tr("অডিও ভাইব বন্ধ করা হয়েছে", "Sound & Haptics Muted"),
                ToastVariant.INFO
            )
        }
    }

    fun toggleBkashBalanceVisibility() {
        playSound(CyberSoundType.TAP)
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(bkashVisible = !it.bkashVisible) }
        }
    }

    fun toggleDbblBalanceVisibility() {
        playSound(CyberSoundType.TAP)
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(dbblVisible = !it.dbblVisible) }
        }
    }

    fun adjustVaultNote(denom: Int, delta: Int) {
        val current = uiState.value.notes[denom] ?: 0
        val next = max(0, current + delta)
        if (next == current) return
        playSound(if (delta > 0) CyberSoundType.TAP else CyberSoundType.STEP_DOWN)
        viewModelScope.launch {
            repository.setNoteCount(denom, next)
        }
    }

    // --- HD Note Preview Modal ---
    fun openNotePreview(denom: Int) {
        playSound(CyberSoundType.TAP)
        _activePreviewDenom.value = denom
    }

    fun closeNotePreview() {
        _activePreviewDenom.value = null
    }

    // --- Smart Expense Workflow ---
    fun openExpenseModal(
        categoryId: String? = null,
        presetMethod: String? = null,
        presetTitle: String = "",
        presetAmount: Int = 0
    ) {
        playSound(CyberSoundType.TAP)
        val defaultCat = categoryId ?: uiState.value.categories.firstOrNull()?.id ?: "grocery"
        val initialStep = when (presetMethod) {
            "bkash" -> ExpenseStep.STEP_2A_BKASH
            "dbbl" -> ExpenseStep.STEP_2C_DBBL
            "cash" -> ExpenseStep.STEP_2B_TENDER
            else -> ExpenseStep.STEP_1_METHOD
        }
        val amtStr = if (presetAmount > 0) BanglaFormatters.toBanglaDigits(presetAmount) else ""
        _expenseWorkflow.value = ExpenseWorkflowState(
            isOpen = true,
            categoryId = defaultCat,
            method = presetMethod,
            step = initialStep,
            billAmountInput = amtStr,
            noteInput = presetTitle,
            bkashError = null,
            tenderNotes = BanglaFormatters.DENOMINATIONS.associateWith { 0 },
            receivedNotes = BanglaFormatters.DENOMINATIONS.associateWith { 0 }
        )
    }

    fun closeExpenseModal() {
        _expenseWorkflow.update { it.copy(isOpen = false) }
    }

    fun updateExpenseCategory(categoryId: String) {
        _expenseWorkflow.update { it.copy(categoryId = categoryId) }
    }

    fun selectExpenseMethod(method: String) {
        playSound(CyberSoundType.TAP)
        val nextStep = when (method) {
            "bkash" -> ExpenseStep.STEP_2A_BKASH
            "dbbl" -> ExpenseStep.STEP_2C_DBBL
            else -> ExpenseStep.STEP_2B_TENDER
        }
        _expenseWorkflow.update {
            it.copy(
                method = method,
                step = nextStep,
                bkashError = null
            )
        }
    }

    fun goToExpenseStep(step: ExpenseStep) {
        playSound(CyberSoundType.TAP)
        _expenseWorkflow.update { it.copy(step = step, bkashError = null) }
    }

    fun updateExpenseBillAmount(input: String) {
        _expenseWorkflow.update { it.copy(billAmountInput = input, bkashError = null) }
    }

    fun setExpenseBillPreset(amount: Int) {
        playSound(CyberSoundType.TAP)
        _expenseWorkflow.update {
            it.copy(billAmountInput = BanglaFormatters.toBanglaDigits(amount), bkashError = null)
        }
    }

    fun updateExpenseNote(input: String) {
        _expenseWorkflow.update { it.copy(noteInput = input) }
    }

    fun confirmBkashExpense() {
        val wf = _expenseWorkflow.value
        val amount = wf.billAmount
        val available = uiState.value.walletConfig.bkashBalance

        if (amount <= 0) {
            playSound(CyberSoundType.WARN)
            _expenseWorkflow.update {
                it.copy(
                    bkashError = BanglaFormatters.tr(
                        "অনুগ্রহ করে সঠিক খরচের পরিমাণ (৳১ বা তার বেশি) লিখুন।",
                        "Please enter a valid expense amount (৳1 or more)."
                    )
                )
            }
            return
        }
        if (amount > available) {
            playSound(CyberSoundType.WARN)
            _expenseWorkflow.update {
                it.copy(
                    bkashError = BanglaFormatters.tr(
                        "বিকাশ ব্যালেন্স অপর্যাপ্ত! আগে বিকাশে টাকা যোগ করুন অথবা খরচের পরিমাণ কমান।",
                        "Insufficient bKash balance! Please add money to bKash first."
                    )
                )
            }
            return
        }

        val cat = uiState.value.categories.find { it.id == wf.categoryId }
        val noteText = wf.noteInput.trim().ifEmpty { "${cat?.name ?: "bKash"} পেমেন্ট" }
        val tx = TransactionEntity(
            id = "TRX-${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            type = "expense",
            method = "bkash",
            categoryId = wf.categoryId,
            amount = amount,
            note = noteText,
            tenderedTotal = amount
        )

        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(bkashBalance = it.bkashBalance - amount) }
            repository.insertTransaction(tx)
            playSound(CyberSoundType.CONFIRM)
            closeExpenseModal()
            showToastMessage(
                BanglaFormatters.tr(
                    "বিকাশ থেকে ${BanglaFormatters.formatBDT(amount)} খরচ সফলভাবে রেকর্ড করা হয়েছে!",
                    "Spent ${BanglaFormatters.formatBDT(amount)} from bKash!"
                )
            )
        }
    }

    fun confirmDbblExpense() {
        val wf = _expenseWorkflow.value
        val amount = wf.billAmount
        val available = uiState.value.walletConfig.dbblBalance

        if (amount <= 0) {
            playSound(CyberSoundType.WARN)
            _expenseWorkflow.update {
                it.copy(
                    bkashError = BanglaFormatters.tr(
                        "অনুগ্রহ করে সঠিক খরচের পরিমাণ (৳১ বা তার বেশি) লিখুন।",
                        "Please enter a valid payment amount (৳1 or more)."
                    )
                )
            }
            return
        }
        if (amount > available) {
            playSound(CyberSoundType.WARN)
            _expenseWorkflow.update {
                it.copy(
                    bkashError = BanglaFormatters.tr(
                        "ডিবিবিএল ব্যাংক ব্যালেন্স অপর্যাপ্ত! আগে ব্যাংকে টাকা জমা করুন।",
                        "Insufficient DBBL Bank balance! Deposit funds first."
                    )
                )
            }
            return
        }

        val cat = uiState.value.categories.find { it.id == wf.categoryId }
        val noteText = wf.noteInput.trim().ifEmpty { "${cat?.name ?: "DBBL"} কার্ড/ব্যাংক পেমেন্ট" }
        val tx = TransactionEntity(
            id = "TRX-${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            type = "expense",
            method = "dbbl",
            categoryId = wf.categoryId,
            amount = amount,
            note = noteText,
            tenderedTotal = amount
        )

        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(dbblBalance = it.dbblBalance - amount) }
            repository.insertTransaction(tx)
            playSound(CyberSoundType.CONFIRM)
            closeExpenseModal()
            showToastMessage(
                BanglaFormatters.tr(
                    "ডিবিবিএল ব্যাংক/কার্ড থেকে ${BanglaFormatters.formatBDT(amount)} পেমেন্ট সফল হয়েছে!",
                    "Paid ${BanglaFormatters.formatBDT(amount)} via DBBL Bank/Card!"
                )
            )
        }
    }

    /**
     * Clicking a note in STEP_2B_TENDER adds +1 of that note AND automatically adds that note's
     * value (+denom) to the Amount of Expenditure (billAmountInput). Clicking minus (−) decrements
     * the note and subtracts denom from the Amount of Expenditure.
     */
    fun adjustTenderNote(denom: Int, delta: Int) {
        val inVault = uiState.value.notes[denom] ?: 0
        val wf = _expenseWorkflow.value
        val current = wf.tenderNotes[denom] ?: 0
        val next = (current + delta).coerceIn(0, inVault)
        val actualDelta = next - current
        if (actualDelta == 0) return

        playSound(if (actualDelta > 0) CyberSoundType.TAP else CyberSoundType.STEP_DOWN)
        _expenseWorkflow.update { cur ->
            val updated = cur.tenderNotes.toMutableMap()
            updated[denom] = next
            val currentBill = cur.billAmount
            val nextBill = max(0, currentBill + actualDelta * denom)
            val nextBillStr = if (nextBill > 0) BanglaFormatters.toBanglaDigits(nextBill) else ""
            cur.copy(
                tenderNotes = updated,
                billAmountInput = nextBillStr
            )
        }
    }

    fun clearTenderNotes() {
        playSound(CyberSoundType.STEP_DOWN)
        _expenseWorkflow.update { wf ->
            val nextBillStr = if (wf.billAmount == wf.tenderTotal) "" else wf.billAmountInput
            wf.copy(
                tenderNotes = BanglaFormatters.DENOMINATIONS.associateWith { 0 },
                billAmountInput = nextBillStr
            )
        }
    }

    fun autoSelectTenderNotes() {
        val wf = _expenseWorkflow.value
        val bill = wf.billAmount
        if (bill <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage(
                BanglaFormatters.tr(
                    "অটো নোট নির্বাচনের আগে খরচের পরিমাণ (বিল) লিখুন!",
                    "Enter the expenditure amount before auto-selecting notes!"
                ),
                ToastVariant.WARN
            )
            return
        }
        val vaultCash = uiState.value.cashTotal
        if (bill > vaultCash) {
            playSound(CyberSoundType.WARN)
            showToastMessage(
                BanglaFormatters.tr(
                    "আপনার নগদ ভল্টে বিলের সমপরিমাণ টাকা নেই!",
                    "Not enough cash in your vault for this bill!"
                ),
                ToastVariant.WARN
            )
            return
        }

        val newTender = BanglaFormatters.DENOMINATIONS.associateWith { 0 }.toMutableMap()
        val vaultNotes = uiState.value.notes

        // Single note candidate >= bill
        val singleCandidate = BanglaFormatters.DENOMINATIONS
            .reversed()
            .firstOrNull { d -> d >= bill && (vaultNotes[d] ?: 0) > 0 }

        if (singleCandidate != null) {
            newTender[singleCandidate] = 1
        } else {
            var remaining = bill
            for (d in BanglaFormatters.DENOMINATIONS) {
                val avail = vaultNotes[d] ?: 0
                if (avail <= 0) continue
                val need = min(avail, ceil(remaining.toDouble() / d).toInt())
                if (need > 0) {
                    newTender[d] = need
                    remaining -= need * d
                    if (remaining <= 0) break
                }
            }
        }
        playSound(CyberSoundType.TAP)
        _expenseWorkflow.update { it.copy(tenderNotes = newTender) }
    }

    /**
     * When the amount of expenditure (`billAmount`) and given money (`tenderTotal`) are the same
     * (`expectedChange == 0`), skip the return change option (`STEP_2B_CHANGE`) completely!
     */
    fun proceedFromTenderToChange() {
        val wf = _expenseWorkflow.value
        val bill = wf.billAmount
        val tenderTotal = wf.tenderTotal
        if (bill <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage(
                BanglaFormatters.tr(
                    "অনুগ্রহ করে খরচের পরিমাণ লিখুন বা নোট ট্যাপ করুন!",
                    "Please enter expenditure amount or tap notes!"
                ),
                ToastVariant.WARN
            )
            return
        }
        if (tenderTotal < bill) {
            playSound(CyberSoundType.WARN)
            showToastMessage(
                BanglaFormatters.tr(
                    "প্রদত্ত নোটের যোগফল (${BanglaFormatters.formatBDT(tenderTotal)}) বিলের (${BanglaFormatters.formatBDT(bill)}) চেয়ে কম হতে পারে না!",
                    "Given notes (${BanglaFormatters.formatBDT(tenderTotal)}) cannot be less than bill (${BanglaFormatters.formatBDT(bill)})!"
                ),
                ToastVariant.WARN
            )
            return
        }

        // If expenditure and given money are the same, skip STEP_2B_CHANGE (return change) completely!
        if (bill == tenderTotal) {
            commitCashExpenseTransaction()
            return
        }

        playSound(CyberSoundType.TAP)
        _expenseWorkflow.update { it.copy(step = ExpenseStep.STEP_2B_CHANGE) }
    }

    fun adjustReceivedNote(denom: Int, delta: Int) {
        val current = _expenseWorkflow.value.receivedNotes[denom] ?: 0
        val next = max(0, current + delta)
        if (next == current) return
        playSound(if (delta > 0) CyberSoundType.TAP else CyberSoundType.STEP_DOWN)
        _expenseWorkflow.update { wf ->
            val updated = wf.receivedNotes.toMutableMap()
            updated[denom] = next
            wf.copy(receivedNotes = updated)
        }
    }

    fun clearReceivedNotes() {
        playSound(CyberSoundType.STEP_DOWN)
        _expenseWorkflow.update { wf ->
            wf.copy(receivedNotes = BanglaFormatters.DENOMINATIONS.associateWith { 0 })
        }
    }

    fun autoFillReceivedChange() {
        val wf = _expenseWorkflow.value
        var rem = wf.expectedChange
        val newReceived = BanglaFormatters.DENOMINATIONS.associateWith { 0 }.toMutableMap()
        for (d in BanglaFormatters.DENOMINATIONS) {
            if (rem >= d) {
                val cnt = rem / d
                newReceived[d] = cnt
                rem -= cnt * d
            }
        }
        playSound(CyberSoundType.TAP)
        _expenseWorkflow.update { it.copy(receivedNotes = newReceived) }
    }

    fun commitCashExpenseTransaction() {
        val wf = _expenseWorkflow.value
        val cat = uiState.value.categories.find { it.id == wf.categoryId }
        val bill = wf.billAmount
        val tenderTotal = wf.tenderTotal
        val receivedTotal = if (tenderTotal == bill) 0 else wf.receivedTotal
        val expectedChange = max(0, tenderTotal - bill)
        val discrepancy = receivedTotal - expectedChange

        val currentNotes = uiState.value.notes.toMutableMap()
        BanglaFormatters.DENOMINATIONS.forEach { d ->
            val given = wf.tenderNotes[d] ?: 0
            val got = if (tenderTotal == bill) 0 else (wf.receivedNotes[d] ?: 0)
            currentNotes[d] = max(0, (currentNotes[d] ?: 0) - given + got)
        }

        val labelNote = wf.noteInput.trim().ifEmpty { "${cat?.name ?: "নগদ"} খরচ" }
        val now = System.currentTimeMillis()
        val newTx = TransactionEntity(
            id = "TRX-$now",
            timestamp = now,
            type = "expense",
            method = "cash",
            categoryId = wf.categoryId,
            amount = bill,
            note = labelNote,
            tenderedNotesEncoded = BanglaFormatters.encodeNotesMap(wf.tenderNotes),
            tenderedTotal = tenderTotal,
            receivedNotesEncoded = if (tenderTotal == bill) "" else BanglaFormatters.encodeNotesMap(wf.receivedNotes),
            receivedTotal = receivedTotal,
            expectedChange = expectedChange,
            discrepancy = discrepancy
        )

        viewModelScope.launch {
            repository.setNoteCountsMap(currentNotes)
            repository.insertTransaction(newTx)
            if (discrepancy != 0) {
                repository.insertDebt(
                    DebtEntity(
                        id = "DEBT-$now",
                        type = if (discrepancy < 0) "receivable" else "payable",
                        person = "$labelNote (ভাঙতি গরমিল)",
                        amount = abs(discrepancy),
                        timestamp = now
                    )
                )
            }
            playSound(CyberSoundType.CONFIRM)
            closeExpenseModal()
            showToastMessage(
                BanglaFormatters.tr(
                    "নগদ খরচ (${BanglaFormatters.formatBDT(bill)}) ও ভাঙতি ভল্ট সফলভাবে আপডেট হয়েছে!",
                    "Cash expense (${BanglaFormatters.formatBDT(bill)}) recorded & vault updated!"
                )
            )
        }
    }

    // --- Add bKash Modal ---
    fun openAddBkashModal() {
        playSound(CyberSoundType.TAP)
        _showAddBkashModal.value = true
    }

    fun closeAddBkashModal() {
        _showAddBkashModal.value = false
    }

    fun confirmAddBkashMoney(amountInput: String, sourceInput: String) {
        val amt = BanglaFormatters.parseBanglaNumber(amountInput)
        if (amt <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage(BanglaFormatters.tr("অনুগ্রহ করে সঠিক টাকার পরিমাণ লিখুন।", "Please enter a valid amount."), ToastVariant.WARN)
            return
        }
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            id = "TRX-$now",
            timestamp = now,
            type = "inflow",
            method = "bkash",
            categoryId = "inflow",
            amount = amt,
            note = sourceInput.trim().ifEmpty { "বিকাশ অ্যাকাউন্টে টাকা জমা" },
            receivedTotal = amt
        )
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(bkashBalance = it.bkashBalance + amt) }
            repository.insertTransaction(tx)
            playSound(CyberSoundType.CONFIRM)
            closeAddBkashModal()
            showToastMessage(
                BanglaFormatters.tr(
                    "বিকাশ ব্যালেন্সে +${BanglaFormatters.formatBDT(amt)} সফলভাবে যোগ হয়েছে!",
                    "Added +${BanglaFormatters.formatBDT(amt)} to bKash!"
                )
            )
        }
    }

    // --- Add DBBL Bank Deposit Modal ---
    fun openAddDbblModal() {
        playSound(CyberSoundType.TAP)
        _showAddDbblModal.value = true
    }

    fun closeAddDbblModal() {
        _showAddDbblModal.value = false
    }

    fun confirmAddDbblMoney(amountInput: String, sourceInput: String) {
        val amt = BanglaFormatters.parseBanglaNumber(amountInput)
        if (amt <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage(BanglaFormatters.tr("অনুগ্রহ করে সঠিক টাকার পরিমাণ লিখুন।", "Please enter a valid amount."), ToastVariant.WARN)
            return
        }
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            id = "TRX-$now",
            timestamp = now,
            type = "inflow",
            method = "dbbl",
            categoryId = "inflow",
            amount = amt,
            note = sourceInput.trim().ifEmpty { "ডিবিবিএল ব্যাংক অ্যাকাউন্টে জমা" },
            receivedTotal = amt
        )
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(dbblBalance = it.dbblBalance + amt) }
            repository.insertTransaction(tx)
            playSound(CyberSoundType.CONFIRM)
            closeAddDbblModal()
            showToastMessage(
                BanglaFormatters.tr(
                    "ডিবিবিএল ব্যাংকে +${BanglaFormatters.formatBDT(amt)} সফলভাবে জমা হয়েছে!",
                    "Deposited +${BanglaFormatters.formatBDT(amt)} to DBBL Bank!"
                )
            )
        }
    }

    // --- DBBL Bank Bridge Modal (ATM Withdraw to Cash Notes / DBBL ⇄ bKash Transfer) ---
    fun openDbblBridgeModal(mode: String = "atm_withdraw") {
        playSound(CyberSoundType.TAP)
        val defaultAmt = 1000
        val notesMap = computeAtmWithdrawNotes(defaultAmt)
        _dbblBridge.value = DbblBridgeState(
            isOpen = true,
            mode = mode,
            amountInput = BanglaFormatters.toBanglaDigits(defaultAmt),
            notes = notesMap
        )
    }

    fun closeDbblBridgeModal() {
        _dbblBridge.update { it.copy(isOpen = false) }
    }

    fun updateDbblBridgeAmount(input: String) {
        _dbblBridge.update { it.copy(amountInput = input) }
    }

    fun autoFillDbblBridgeNotes() {
        playSound(CyberSoundType.TAP)
        val cur = _dbblBridge.value
        _dbblBridge.update { it.copy(notes = computeAtmWithdrawNotes(cur.amount)) }
    }

    private fun computeAtmWithdrawNotes(amount: Int): Map<Int, Int> {
        var rem = amount
        val result = BanglaFormatters.DENOMINATIONS.associateWith { 0 }.toMutableMap()
        // Prefer ATM notes (1000, 500, 200, 100) then smaller denominations
        for (d in BanglaFormatters.DENOMINATIONS) {
            if (rem >= d) {
                val cnt = rem / d
                result[d] = cnt
                rem -= cnt * d
            }
        }
        return result
    }

    fun adjustDbblBridgeNote(denom: Int, delta: Int) {
        val curState = _dbblBridge.value
        val cur = curState.notes[denom] ?: 0
        val next = (cur + delta).coerceIn(0, 99)
        if (next == cur) return
        playSound(if (delta > 0) CyberSoundType.TAP else CyberSoundType.STEP_DOWN)
        val updated = curState.notes.toMutableMap()
        updated[denom] = next
        val nextSum = BanglaFormatters.calculateNotesTotal(updated)
        _dbblBridge.update {
            it.copy(
                notes = updated,
                amountInput = if (nextSum > 0) BanglaFormatters.toBanglaDigits(nextSum) else ""
            )
        }
    }

    fun confirmDbblBridgeTransfer() {
        val state = _dbblBridge.value
        val now = System.currentTimeMillis()
        when (state.mode) {
            "atm_withdraw" -> {
                val notesSum = state.notesTotal
                if (notesSum <= 0) {
                    playSound(CyberSoundType.WARN)
                    showToastMessage(BanglaFormatters.tr("এটিএম উত্তোলনের নোট নির্বাচন করুন!", "Select ATM withdrawal notes!"), ToastVariant.WARN)
                    return
                }
                if (notesSum > uiState.value.walletConfig.dbblBalance) {
                    playSound(CyberSoundType.WARN)
                    showToastMessage(BanglaFormatters.tr("ডিবিবিএল ব্যাংকে পর্যাপ্ত ব্যালেন্স নেই!", "Insufficient DBBL Bank balance!"), ToastVariant.WARN)
                    return
                }
                val updatedVault = uiState.value.notes.toMutableMap()
                BanglaFormatters.DENOMINATIONS.forEach { d ->
                    val add = state.notes[d] ?: 0
                    if (add > 0) updatedVault[d] = (updatedVault[d] ?: 0) + add
                }
                val tx = TransactionEntity(
                    id = "TRX-$now",
                    timestamp = now,
                    type = "inflow",
                    method = "cash",
                    categoryId = "inflow",
                    amount = notesSum,
                    note = "ডিবিবিএল এটিএম বুথ থেকে নগদ উত্তোলন",
                    tenderedTotal = notesSum,
                    receivedNotesEncoded = BanglaFormatters.encodeNotesMap(state.notes),
                    receivedTotal = notesSum
                )
                viewModelScope.launch {
                    repository.updateWalletConfig { it.copy(dbblBalance = it.dbblBalance - notesSum) }
                    repository.setNoteCountsMap(updatedVault)
                    repository.insertTransaction(tx)
                    playSound(CyberSoundType.CONFIRM)
                    closeDbblBridgeModal()
                    showToastMessage(
                        BanglaFormatters.tr(
                            "ডিবিবিএল এটিএম উত্তোলন সফল! ভল্টে +${BanglaFormatters.formatBDT(notesSum)} যোগ হয়েছে।",
                            "ATM Withdrawal complete! +${BanglaFormatters.formatBDT(notesSum)} added to Cash Vault."
                        )
                    )
                }
            }
            "to_bkash" -> {
                val amt = state.amount
                if (amt <= 0 || amt > uiState.value.walletConfig.dbblBalance) {
                    playSound(CyberSoundType.WARN)
                    showToastMessage(BanglaFormatters.tr("সঠিক পরিমাণ লিখুন (ব্যাংক ব্যালেন্সের মধ্যে)!", "Enter a valid amount within DBBL balance!"), ToastVariant.WARN)
                    return
                }
                val tx = TransactionEntity(
                    id = "TRX-$now",
                    timestamp = now,
                    type = "inflow",
                    method = "bkash",
                    categoryId = "inflow",
                    amount = amt,
                    note = "ডিবিবিএল নেক্সাসপে থেকে বিকাশে অ্যাড মানি",
                    tenderedTotal = amt,
                    receivedTotal = amt
                )
                viewModelScope.launch {
                    repository.updateWalletConfig {
                        it.copy(
                            dbblBalance = it.dbblBalance - amt,
                            bkashBalance = it.bkashBalance + amt
                        )
                    }
                    repository.insertTransaction(tx)
                    playSound(CyberSoundType.CONFIRM)
                    closeDbblBridgeModal()
                    showToastMessage(
                        BanglaFormatters.tr(
                            "ডিবিবিএল ব্যাংক থেকে বিকাশে +${BanglaFormatters.formatBDT(amt)} স্থানান্তর সফল!",
                            "Transferred ${BanglaFormatters.formatBDT(amt)} from DBBL to bKash!"
                        )
                    )
                }
            }
            "from_bkash" -> {
                val amt = state.amount
                if (amt <= 0 || amt > uiState.value.walletConfig.bkashBalance) {
                    playSound(CyberSoundType.WARN)
                    showToastMessage(BanglaFormatters.tr("সঠিক পরিমাণ লিখুন (বিকাশ ব্যালেন্সের মধ্যে)!", "Enter a valid amount within bKash balance!"), ToastVariant.WARN)
                    return
                }
                val tx = TransactionEntity(
                    id = "TRX-$now",
                    timestamp = now,
                    type = "inflow",
                    method = "dbbl",
                    categoryId = "inflow",
                    amount = amt,
                    note = "বিকাশ থেকে ডিবিবিএল ব্যাংকে ট্রান্সফার",
                    tenderedTotal = amt,
                    receivedTotal = amt
                )
                viewModelScope.launch {
                    repository.updateWalletConfig {
                        it.copy(
                            bkashBalance = it.bkashBalance - amt,
                            dbblBalance = it.dbblBalance + amt
                        )
                    }
                    repository.insertTransaction(tx)
                    playSound(CyberSoundType.CONFIRM)
                    closeDbblBridgeModal()
                    showToastMessage(
                        BanglaFormatters.tr(
                            "বিকাশ থেকে ডিবিবিএল ব্যাংকে +${BanglaFormatters.formatBDT(amt)} স্থানান্তর সফল!",
                            "Transferred ${BanglaFormatters.formatBDT(amt)} from bKash to DBBL!"
                        )
                    )
                }
            }
        }
    }

    // --- Add Cash Notes Modal ---
    fun openAddCashModal() {
        playSound(CyberSoundType.TAP)
        _addCashNotes.value = BanglaFormatters.DENOMINATIONS.associateWith { 0 }
    }

    fun closeAddCashModal() {
        _addCashNotes.value = null
    }

    fun adjustAddCashTempNote(denom: Int, delta: Int) {
        val currentMap = _addCashNotes.value ?: return
        val cur = currentMap[denom] ?: 0
        val next = max(0, cur + delta)
        if (next == cur) return
        playSound(if (delta > 0) CyberSoundType.TAP else CyberSoundType.STEP_DOWN)
        val updated = currentMap.toMutableMap()
        updated[denom] = next
        _addCashNotes.value = updated
    }

    fun confirmAddCashNotes(sourceNote: String) {
        val tempMap = _addCashNotes.value ?: return
        val totalAdd = BanglaFormatters.calculateNotesTotal(tempMap)
        if (totalAdd <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage("ভল্টে যোগ করার জন্য অন্তত ১টি নোট নির্বাচন করুন।", ToastVariant.WARN)
            return
        }
        val updatedVault = uiState.value.notes.toMutableMap()
        BanglaFormatters.DENOMINATIONS.forEach { d ->
            val addCnt = tempMap[d] ?: 0
            if (addCnt > 0) {
                updatedVault[d] = (updatedVault[d] ?: 0) + addCnt
            }
        }
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            id = "TRX-$now",
            timestamp = now,
            type = "inflow",
            method = "cash",
            categoryId = "inflow",
            amount = totalAdd,
            note = sourceNote.trim().ifEmpty { "ভাঙতি ভল্টে নতুন নোট সংযোজন" },
            receivedNotesEncoded = BanglaFormatters.encodeNotesMap(tempMap),
            receivedTotal = totalAdd
        )
        viewModelScope.launch {
            repository.setNoteCountsMap(updatedVault)
            repository.insertTransaction(tx)
            playSound(CyberSoundType.CONFIRM)
            closeAddCashModal()
            showToastMessage("ভাঙতি ভল্টে +${BanglaFormatters.formatBDT(totalAdd)} মূল্যের নোট যুক্ত করা হয়েছে!")
        }
    }

    // --- Add Custom Category Modal ---
    fun openAddCategoryModal() {
        playSound(CyberSoundType.TAP)
        _showAddCategoryModal.value = true
    }

    fun closeAddCategoryModal() {
        _showAddCategoryModal.value = false
    }

    fun confirmAddCategory(nameInput: String, iconInput: String) {
        val name = nameInput.trim()
        val icon = iconInput.trim().ifEmpty { "✨" }
        if (name.isEmpty()) {
            playSound(CyberSoundType.WARN)
            showToastMessage("অনুগ্রহ করে খাতের একটি নাম লিখুন।", ToastVariant.WARN)
            return
        }
        val palette = listOf("#00F0FF", "#00FFA3", "#FF0055", "#E2136E", "#FACC15", "#A855F7", "#38BDF8")
        val cats = uiState.value.categories
        val colorHex = palette[cats.size % palette.size]
        val newCat = CategoryEntity(
            id = "cat_${System.currentTimeMillis()}",
            name = name,
            icon = icon,
            colorHex = colorHex,
            sortOrder = cats.size
        )
        viewModelScope.launch {
            repository.addCategory(newCat)
            playSound(CyberSoundType.CONFIRM)
            closeAddCategoryModal()
            showToastMessage("নতুন খাত \"$icon $name\" যুক্ত করা হয়েছে!")
        }
    }

    // --- Debt Ledger & Quick bKash Fee Calc ---
    fun updateQuickBkashFeeInput(input: String) {
        _quickCalcBkashInput.value = input
    }

    fun addManualDebtEntry(type: String, personInput: String, amountInput: String) {
        val person = personInput.trim()
        val amount = BanglaFormatters.parseBanglaNumber(amountInput)
        if (person.isEmpty() || amount <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage("নাম এবং সঠিক টাকার পরিমাণ লিখুন!", ToastVariant.WARN)
            return
        }
        val debt = DebtEntity(
            id = "DEBT-${System.currentTimeMillis()}",
            type = type,
            person = person,
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertDebt(debt)
            playSound(CyberSoundType.CONFIRM)
            showToastMessage("বাকি ও পাওনা খাতায় নতুন এন্ট্রি যুক্ত হয়েছে!")
        }
    }

    fun settleDebtEntry(debt: DebtEntity) {
        viewModelScope.launch {
            repository.updateWalletConfig { cfg ->
                val nextBal = if (debt.type == "receivable") {
                    cfg.bkashBalance + debt.amount
                } else {
                    max(0, cfg.bkashBalance - debt.amount)
                }
                cfg.copy(bkashBalance = nextBal)
            }
            repository.deleteDebt(debt.id)
            playSound(CyberSoundType.CONFIRM)
            showToastMessage("${debt.person}-এর ${BanglaFormatters.formatBDT(debt.amount)} নিষ্পত্তি হয়েছে!")
        }
    }

    fun deleteDebtEntry(id: String) {
        playSound(CyberSoundType.STEP_DOWN)
        viewModelScope.launch {
            repository.deleteDebt(id)
        }
    }

    // --- Temporary Money (অস্থায়ী টাকা) ---
    fun addTempMoney(titleInput: String, amountInput: String, whereStored: String) {
        val title = titleInput.trim()
        val amount = BanglaFormatters.parseBanglaNumber(amountInput)
        if (title.isEmpty() || amount <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage("বিবরণ ও সঠিক পরিমাণ লিখুন!", ToastVariant.WARN)
            return
        }
        val item = TempMoneyEntity(
            id = "TMP-${System.currentTimeMillis()}",
            title = title.take(60),
            amount = amount,
            whereStored = whereStored,
            timestamp = System.currentTimeMillis(),
            done = false
        )
        viewModelScope.launch {
            repository.upsertTempMoney(item)
            playSound(CyberSoundType.CONFIRM)
            showToastMessage("অস্থায়ী টাকা ${BanglaFormatters.formatBDT(amount)} যোগ হয়েছে")
        }
    }

    fun toggleTempMoney(item: TempMoneyEntity) {
        playSound(CyberSoundType.TAP)
        viewModelScope.launch {
            repository.upsertTempMoney(item.copy(done = !item.done))
        }
    }

    fun deleteTempMoney(id: String) {
        playSound(CyberSoundType.STEP_DOWN)
        viewModelScope.launch {
            repository.deleteTempMoney(id)
        }
    }

    // --- bKash Bridge Modal (Cash-Out / Cash-In) ---
    fun openBkashBridgeModal(mode: String = "cashout") {
        playSound(CyberSoundType.TAP)
        val defaultAmt = BanglaFormatters.parseBanglaNumber(_quickCalcBkashInput.value).let {
            if (it > 0) it else 1000
        }
        val notesMap = computeAutoBridgeNotes(mode, defaultAmt)
        _bkashBridge.value = BkashBridgeState(
            isOpen = true,
            mode = mode,
            amountInput = BanglaFormatters.toBanglaDigits(defaultAmt),
            feeRatePercent = 1.85,
            notes = notesMap
        )
    }

    fun closeBkashBridgeModal() {
        _bkashBridge.update { it.copy(isOpen = false) }
    }

    fun updateBridgeAmount(input: String) {
        _bkashBridge.update { it.copy(amountInput = input) }
    }

    fun updateBridgeFeeRate(rate: Double) {
        _bkashBridge.update { it.copy(feeRatePercent = rate) }
    }

    fun autoFillBridgeNotes() {
        playSound(CyberSoundType.TAP)
        val cur = _bkashBridge.value
        val notesMap = computeAutoBridgeNotes(cur.mode, cur.amount)
        _bkashBridge.update { it.copy(notes = notesMap) }
    }

    private fun computeAutoBridgeNotes(mode: String, amount: Int): Map<Int, Int> {
        var rem = amount
        val vault = uiState.value.notes
        val result = BanglaFormatters.DENOMINATIONS.associateWith { 0 }.toMutableMap()
        for (d in BanglaFormatters.DENOMINATIONS) {
            val maxAvail = if (mode == "cashin") (vault[d] ?: 0) else 99
            if (rem >= d && maxAvail > 0) {
                val cnt = min(maxAvail, rem / d)
                result[d] = cnt
                rem -= cnt * d
            }
        }
        return result
    }

    fun adjustBridgeNote(denom: Int, delta: Int) {
        val curState = _bkashBridge.value
        val maxAvail = if (curState.mode == "cashin") (uiState.value.notes[denom] ?: 0) else 99
        val cur = curState.notes[denom] ?: 0
        val next = (cur + delta).coerceIn(0, maxAvail)
        if (next == cur) return
        playSound(CyberSoundType.TAP)
        val updated = curState.notes.toMutableMap()
        updated[denom] = next
        _bkashBridge.update { it.copy(notes = updated) }
    }

    fun confirmBkashBridgeTransfer() {
        val state = _bkashBridge.value
        val amt = state.amount
        val notesSum = state.notesTotal
        if (amt <= 0 || notesSum <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage("সঠিক টাকার পরিমাণ ও নোট নির্বাচন করুন!", ToastVariant.WARN)
            return
        }

        val now = System.currentTimeMillis()
        if (state.mode == "cashout") {
            val fee = state.feeAmount
            val totalDeduct = notesSum + fee
            if (totalDeduct > uiState.value.walletConfig.bkashBalance) {
                playSound(CyberSoundType.WARN)
                showToastMessage("বিকাশে পর্যাপ্ত ব্যালেন্স নেই!", ToastVariant.WARN)
                return
            }
            val updatedVault = uiState.value.notes.toMutableMap()
            BanglaFormatters.DENOMINATIONS.forEach { d ->
                val add = state.notes[d] ?: 0
                if (add > 0) updatedVault[d] = (updatedVault[d] ?: 0) + add
            }
            val tx = TransactionEntity(
                id = "TRX-$now",
                timestamp = now,
                type = "inflow",
                method = "cash",
                categoryId = "misc",
                amount = notesSum,
                note = "বিকাশ ক্যাশ-আউট (চার্জ: ${BanglaFormatters.formatBDT(fee)})",
                tenderedTotal = totalDeduct,
                receivedNotesEncoded = BanglaFormatters.encodeNotesMap(state.notes),
                receivedTotal = notesSum
            )
            viewModelScope.launch {
                repository.updateWalletConfig { it.copy(bkashBalance = it.bkashBalance - totalDeduct) }
                repository.setNoteCountsMap(updatedVault)
                repository.insertTransaction(tx)
                playSound(CyberSoundType.CONFIRM)
                closeBkashBridgeModal()
                showToastMessage("বিকাশ ক্যাশ-আউট সফল! ভাঙতি ভল্টে +${BanglaFormatters.formatBDT(notesSum)} যুক্ত হয়েছে।")
            }
        } else {
            val updatedVault = uiState.value.notes.toMutableMap()
            BanglaFormatters.DENOMINATIONS.forEach { d ->
                val sub = state.notes[d] ?: 0
                if (sub > 0) updatedVault[d] = max(0, (updatedVault[d] ?: 0) - sub)
            }
            val tx = TransactionEntity(
                id = "TRX-$now",
                timestamp = now,
                type = "inflow",
                method = "bkash",
                categoryId = "inflow",
                amount = notesSum,
                note = "ভাঙতি ভল্ট থেকে বিকাশে ক্যাশ-ইন",
                tenderedNotesEncoded = BanglaFormatters.encodeNotesMap(state.notes),
                tenderedTotal = notesSum,
                receivedTotal = notesSum
            )
            viewModelScope.launch {
                repository.updateWalletConfig { it.copy(bkashBalance = it.bkashBalance + notesSum) }
                repository.setNoteCountsMap(updatedVault)
                repository.insertTransaction(tx)
                playSound(CyberSoundType.CONFIRM)
                closeBkashBridgeModal()
                showToastMessage("বিকাশ ক্যাশ-ইন সফল! বিকাশে +${BanglaFormatters.formatBDT(notesSum)} যুক্ত হয়েছে।")
            }
        }
    }

    // --- Note Breaker / Exchanger Modal (ভাঙতি করুন) ---
    fun openNoteExchangeModal() {
        playSound(CyberSoundType.TAP)
        val vault = uiState.value.notes
        val availBig = listOf(1000, 500, 200, 100, 50).firstOrNull { (vault[it] ?: 0) > 0 } ?: 1000
        _noteExchange.value = NoteExchangeState(
            isOpen = true,
            sourceDenom = availBig,
            targetNotes = computeAutoBrokenNotes(availBig)
        )
    }

    fun closeNoteExchangeModal() {
        _noteExchange.update { it.copy(isOpen = false) }
    }

    fun selectExchangeSource(denom: Int) {
        playSound(CyberSoundType.TAP)
        _noteExchange.update {
            it.copy(sourceDenom = denom, targetNotes = computeAutoBrokenNotes(denom))
        }
    }

    fun autoBreakSelectedNote() {
        playSound(CyberSoundType.TAP)
        val src = _noteExchange.value.sourceDenom
        _noteExchange.update { it.copy(targetNotes = computeAutoBrokenNotes(src)) }
    }

    private fun computeAutoBrokenNotes(sourceDenom: Int): Map<Int, Int> {
        var rem = sourceDenom
        val map = BanglaFormatters.DENOMINATIONS.associateWith { 0 }.toMutableMap()
        for (d in BanglaFormatters.DENOMINATIONS) {
            if (d < sourceDenom && rem >= d) {
                val cnt = rem / d
                map[d] = cnt
                rem -= cnt * d
            }
        }
        return map
    }

    fun adjustExchangeTarget(denom: Int, delta: Int) {
        val cur = _noteExchange.value.targetNotes[denom] ?: 0
        val next = max(0, cur + delta)
        if (next == cur) return
        playSound(CyberSoundType.TAP)
        val updated = _noteExchange.value.targetNotes.toMutableMap()
        updated[denom] = next
        _noteExchange.update { it.copy(targetNotes = updated) }
    }

    fun confirmNoteExchange() {
        val state = _noteExchange.value
        val src = state.sourceDenom
        val vault = uiState.value.notes
        if ((vault[src] ?: 0) <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage("ভল্টে ৳${BanglaFormatters.toBanglaDigits(src)} নোট নেই!", ToastVariant.WARN)
            return
        }
        if (state.targetTotal != src) {
            playSound(CyberSoundType.WARN)
            showToastMessage("খুচরা ভাঙতি নোটের যোগফল মূল নোটের সমান হতে হবে!", ToastVariant.WARN)
            return
        }
        val updated = vault.toMutableMap()
        updated[src] = max(0, (updated[src] ?: 0) - 1)
        BanglaFormatters.DENOMINATIONS.forEach { d ->
            val add = state.targetNotes[d] ?: 0
            if (add > 0) updated[d] = (updated[d] ?: 0) + add
        }
        viewModelScope.launch {
            repository.setNoteCountsMap(updated)
            playSound(CyberSoundType.CONFIRM)
            closeNoteExchangeModal()
            showToastMessage("৳${BanglaFormatters.toBanglaDigits(src)} নোট সফলভাবে খুচরা ভাঙতিতে রূপান্তর করা হয়েছে!")
        }
    }

    // --- Quick Market Calculator ---
    fun openCalculatorModal() {
        playSound(CyberSoundType.TAP)
        _calculator.value = CalculatorState(isOpen = true, expression = "", lastResult = 0)
    }

    fun closeCalculatorModal() {
        _calculator.update { it.copy(isOpen = false) }
    }

    fun calcInput(token: String) {
        playSound(CyberSoundType.TAP)
        val cur = _calculator.value
        when (token) {
            "C" -> _calculator.value = cur.copy(expression = "", lastResult = 0)
            "DEL" -> {
                val nextExpr = cur.expression.dropLast(1)
                _calculator.value = cur.copy(
                    expression = nextExpr,
                    lastResult = evaluateSimpleMath(nextExpr)
                )
            }
            "=" -> {
                val res = evaluateSimpleMath(cur.expression)
                _calculator.value = cur.copy(expression = if (res != 0) res.toString() else "", lastResult = res)
            }
            else -> {
                val nextExpr = cur.expression + token
                _calculator.value = cur.copy(
                    expression = nextExpr,
                    lastResult = evaluateSimpleMath(nextExpr)
                )
            }
        }
    }

    private fun evaluateSimpleMath(expr: String): Int {
        val clean = expr.trim()
        if (clean.isEmpty()) return 0
        return try {
            val tokens = Regex("(?<=[+\\-*/])|(?=[+\\-*/])").split(clean).map { it.trim() }.filter { it.isNotEmpty() }
            if (tokens.isEmpty()) return 0
            val pass1 = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val t = tokens[i]
                if ((t == "*" || t == "/") && pass1.isNotEmpty() && i + 1 < tokens.size) {
                    val left = pass1.removeAt(pass1.lastIndex).toDoubleOrNull() ?: 0.0
                    val right = tokens[i + 1].toDoubleOrNull() ?: 1.0
                    val r = if (t == "*") left * right else if (right != 0.0) left / right else 0.0
                    pass1.add(r.toString())
                    i += 2
                } else {
                    pass1.add(t)
                    i++
                }
            }
            var acc = pass1.firstOrNull()?.toDoubleOrNull() ?: 0.0
            var j = 1
            while (j + 1 < pass1.size) {
                val op = pass1[j]
                val rhs = pass1[j + 1].toDoubleOrNull() ?: 0.0
                if (op == "+") acc += rhs
                else if (op == "-") acc -= rhs
                j += 2
            }
            Math.round(acc).toInt()
        } catch (_: Exception) {
            0
        }
    }

    fun useCalcResultForExpense() {
        val res = _calculator.value.lastResult.let {
            if (it > 0) it else BanglaFormatters.parseBanglaNumber(_calculator.value.expression)
        }
        closeCalculatorModal()
        openExpenseModal(presetAmount = res)
    }

    // --- Transactions & History Filters ---
    fun updateFilterSearch(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun updateFilterMethod(method: String) {
        _filterState.update { it.copy(methodFilter = method) }
    }

    fun updateFilterCategory(categoryId: String) {
        _filterState.update { it.copy(categoryFilter = categoryId) }
    }

    fun updateFilterDate(dateRange: String) {
        _filterState.update { it.copy(dateFilter = dateRange) }
    }

    fun updateFilterCustomDays(days: Int) {
        val clean = days.coerceIn(1, 3650)
        _filterState.update { it.copy(dateFilter = "custom_days", customDaysCount = clean) }
    }

    fun periodLabelForFilter(f: HistoryFilterState): String {
        return when (f.dateFilter) {
            "today" -> BanglaFormatters.tr("আজকের লেনদেন", "Today")
            "week" -> BanglaFormatters.tr("গত ৭ দিন", "Last 7 Days")
            "month" -> BanglaFormatters.tr("চলতি মাস (এই মাস)", "This Month")
            "year" -> BanglaFormatters.tr("চলতি বছর (এই বছর)", "This Year")
            "custom_days" -> BanglaFormatters.tr(
                "গত ${BanglaFormatters.toBanglaDigits(f.customDaysCount)} দিন",
                "Last ${f.customDaysCount} Days"
            )
            else -> BanglaFormatters.tr("সব সময়ের লেনদেন", "All Time")
        }
    }

    fun filteredTransactions(state: TakaVaultUiState): List<TransactionEntity> {
        val f = state.filterState
        val q = f.searchQuery.trim().lowercase()
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        val sevenDaysAgo = now - 7 * 86_400_000L
        val customDaysAgo = now - f.customDaysCount.toLong() * 86_400_000L
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        val startOfYear = cal.timeInMillis

        return state.transactions.filter { tx ->
            if (f.methodFilter == "cash" && !(tx.type == "expense" && tx.method == "cash")) return@filter false
            if (f.methodFilter == "bkash" && !(tx.type == "expense" && tx.method == "bkash")) return@filter false
            if (f.methodFilter == "dbbl" && !(tx.type == "expense" && tx.method == "dbbl")) return@filter false
            if (f.methodFilter == "inflow" && tx.type != "inflow") return@filter false
            if (f.categoryFilter != "all" && tx.categoryId != f.categoryFilter) return@filter false
            if (f.dateFilter == "today" && tx.timestamp < startOfToday) return@filter false
            if (f.dateFilter == "week" && tx.timestamp < sevenDaysAgo) return@filter false
            if (f.dateFilter == "month" && tx.timestamp < startOfMonth) return@filter false
            if (f.dateFilter == "year" && tx.timestamp < startOfYear) return@filter false
            if (f.dateFilter == "custom_days" && tx.timestamp < customDaysAgo) return@filter false
            if (q.isNotEmpty()) {
                val catName = state.categories.find { it.id == tx.categoryId }?.name?.lowercase() ?: ""
                val noteLower = tx.note.lowercase()
                if (!catName.contains(q) && !noteLower.contains(q)) return@filter false
            }
            true
        }
    }

    fun deleteTransaction(txId: String) {
        playSound(CyberSoundType.STEP_DOWN)
        viewModelScope.launch {
            repository.deleteTransaction(txId)
            showToastMessage(BanglaFormatters.tr("লেনদেনটি ইতিহাস থেকে মুছে ফেলা হয়েছে।", "Transaction removed from history."), ToastVariant.INFO)
        }
    }

    // --- Biometric Authentication Lock & Session ---
    fun unlockVaultSession() {
        _isSessionUnlocked.value = true
        playSound(CyberSoundType.CONFIRM)
        showToastMessage(
            BanglaFormatters.tr(
                "🔓 ফিঙ্গারপ্রিন্ট যাচাই সফল! টাকাভল্ট আনলক হয়েছে।",
                "🔓 Biometric verified! TakaVault unlocked."
            ),
            ToastVariant.SUCCESS
        )
    }

    fun lockVaultSessionNow() {
        _isSessionUnlocked.value = false
        playSound(CyberSoundType.TAP)
    }

    fun toggleBiometricLockEnabled() {
        val next = !uiState.value.walletConfig.biometricLockEnabled
        playSound(CyberSoundType.TAP)
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(biometricLockEnabled = next) }
            if (next) {
                // Keep current session unlocked while user is inside Settings, so it locks on next launch or manual lock
                _isSessionUnlocked.value = true
            }
            showToastMessage(
                if (next) BanglaFormatters.tr("🔐 অ্যাপ লঞ্চে ফিঙ্গারপ্রিন্ট বায়োমেট্রিক লক চালু হয়েছে!", "🔐 Biometric fingerprint lock enabled on app launch!")
                else BanglaFormatters.tr("🔓 বায়োমেট্রিক লক বন্ধ করা হয়েছে।", "🔓 Biometric fingerprint lock disabled."),
                ToastVariant.INFO
            )
        }
    }

    fun saveBiometricPin(pinInput: String) {
        val clean = pinInput.filter { it.isDigit() }.take(6)
        if (clean.length < 4) {
            playSound(CyberSoundType.WARN)
            showToastMessage(
                BanglaFormatters.tr("অন্তত ৪ ডিজিটের ব্যাকআপ পিন লিখুন!", "Enter at least a 4-digit backup PIN!"),
                ToastVariant.WARN
            )
            return
        }
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(biometricPinCode = clean) }
            playSound(CyberSoundType.CONFIRM)
            showToastMessage(
                BanglaFormatters.tr("🔐 ব্যাকআপ সিকিউরিটি পিন সংরক্ষিত হয়েছে!", "🔐 Backup security PIN saved!"),
                ToastVariant.SUCCESS
            )
        }
    }

    // --- Receipt & Settings / Backup ---
    fun openReceiptModal(tx: TransactionEntity) {
        playSound(CyberSoundType.TAP)
        _activeReceiptTx.value = tx
    }

    fun closeReceiptModal() {
        _activeReceiptTx.value = null
    }

    fun openSettingsModal() {
        playSound(CyberSoundType.TAP)
        _showSettingsModal.value = true
    }

    fun closeSettingsModal() {
        _showSettingsModal.value = false
    }

    fun saveMonthlyBudget(budgetInput: String) {
        val valNum = BanglaFormatters.parseBanglaNumber(budgetInput)
        if (valNum <= 0) {
            playSound(CyberSoundType.WARN)
            showToastMessage(BanglaFormatters.tr("অনুগ্রহ করে সঠিক মাসিক বাজেট সীমা লিখুন!", "Please enter a valid monthly budget!"), ToastVariant.WARN)
            return
        }
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(monthlyBudget = valNum) }
            playSound(CyberSoundType.CONFIRM)
            showToastMessage(BanglaFormatters.tr("মাসিক বাজেট লিমিট ${BanglaFormatters.formatBDT(valNum)}-এ আপডেট করা হয়েছে!", "Monthly budget updated to ${BanglaFormatters.formatBDT(valNum)}!"))
        }
    }

    fun saveSavingsGoal(goalInput: String) {
        val valNum = BanglaFormatters.parseBanglaNumber(goalInput)
        if (valNum < 0) return
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(savingsGoal = valNum) }
            playSound(CyberSoundType.CONFIRM)
            showToastMessage(BanglaFormatters.tr("সঞ্চয় লক্ষ্যমাত্রা ${BanglaFormatters.formatBDT(valNum)} সংরক্ষিত হয়েছে!", "Savings goal updated to ${BanglaFormatters.formatBDT(valNum)}!"))
        }
    }

    fun saveLowBhangtiThreshold(threshold: Int) {
        val clean = threshold.coerceIn(2, 30)
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(lowBhangtiThreshold = clean) }
            playSound(CyberSoundType.TAP)
        }
    }

    fun saveDbblAccountNo(accInput: String) {
        val clean = accInput.filter { it.isDigit() }.takeLast(4).ifEmpty { "4829" }
        viewModelScope.launch {
            repository.updateWalletConfig { it.copy(dbblAccountNo = clean) }
            playSound(CyberSoundType.CONFIRM)
            showToastMessage(BanglaFormatters.tr("ডিবিবিএল কার্ড/অ্যাকাউন্ট নম্বর (*$clean) সংরক্ষিত হয়েছে!", "DBBL Account/Card (*$clean) saved!"))
        }
    }

    fun executeHardResetZero() {
        viewModelScope.launch {
            repository.hardResetZero()
            playSound(CyberSoundType.CONFIRM)
            closeSettingsModal()
            showToastMessage(BanglaFormatters.tr("সকল ব্যালেন্স, ভাঙতি নোট ও লেনদেন মুছে শূন্য (০) করা হয়েছে।", "All balances, notes, and history reset to zero."), ToastVariant.INFO)
        }
    }

    fun loadRichDemoData() {
        viewModelScope.launch {
            repository.seedRichDemoData()
            playSound(CyberSoundType.CONFIRM)
            closeSettingsModal()
            showToastMessage(BanglaFormatters.tr("নমুনা ডাটা (বিকাশ, ডিবিবিএল, ভাঙতি ভল্ট ও লেনদেন) সফলভাবে লোড হয়েছে!", "Sample demo data loaded!"))
        }
    }

    fun exportBackupJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJsonString()
            playSound(CyberSoundType.CONFIRM)
            onReady(json)
        }
    }

    fun exportTransactionsCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportTransactionsCsvString()
            playSound(CyberSoundType.CONFIRM)
            onReady(csv)
        }
    }

    fun importBackupJson(jsonStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = repository.importBackupJsonString(jsonStr)
            if (ok) {
                playSound(CyberSoundType.CONFIRM)
                closeSettingsModal()
                showToastMessage(BanglaFormatters.tr("ডাটা ব্যাকআপ সফলভাবে রিস্টোর করা হয়েছে!", "Backup restored successfully!"))
            } else {
                playSound(CyberSoundType.WARN)
                showToastMessage(BanglaFormatters.tr("ত্রুটি: সঠিক টাকাভল্ট JSON ব্যাকআপ ফাইল নির্বাচন করুন!", "Error: Invalid TakaVault JSON backup file!"), ToastVariant.WARN)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundHelper.release()
    }
}
