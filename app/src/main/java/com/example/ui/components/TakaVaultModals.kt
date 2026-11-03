package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BanknoteCatalog
import com.example.data.TransactionEntity
import com.example.data.WalletConfigEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BkashPink
import com.example.ui.theme.DbblBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextSecondary
import com.example.util.BanglaFormatters
import com.example.util.CustomAssetManager
import com.example.viewmodel.BkashBridgeState
import com.example.viewmodel.CalculatorState
import com.example.viewmodel.DbblBridgeState
import com.example.viewmodel.NoteExchangeState
import com.example.viewmodel.TakaVaultUiState
import kotlin.math.abs

@Composable
fun AddBkashDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    if (!isOpen) return

    var amountInput by remember { mutableStateOf("") }
    var sourceInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SlateCardBg)
                .border(1.dp, BkashPink.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BkashOrigamiLogo(modifier = Modifier.size(32.dp))
                    Text(
                        text = BanglaFormatters.tr("বিকাশে টাকা যোগ করুন", "Add Money to bKash"),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text(BanglaFormatters.tr("টাকার পরিমাণ (৳)", "Amount (৳)"), fontSize = 12.sp) },
                placeholder = { Text(BanglaFormatters.tr("যেমন: ১০০০", "e.g. 1000"), fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_bkash_amount_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BkashPink,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            val presets = listOf(500, 1000, 2000, 5000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { amt ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BkashPink.copy(alpha = 0.16f))
                            .border(1.dp, BkashPink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { amountInput = BanglaFormatters.toBanglaDigits(amt) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+৳${BanglaFormatters.toBanglaDigits(amt)}",
                            color = Color(0xFFFBCFE8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = sourceInput,
                onValueChange = { sourceInput = it },
                label = { Text(BanglaFormatters.tr("উৎস বা বিবরণ (ঐচ্ছিক)", "Source / Note (Optional)"), fontSize = 12.sp) },
                placeholder = { Text(BanglaFormatters.tr("যেমন: ক্যাশ-ইন, সেন্ড মানি প্রাপ্তি", "e.g. Cash-In, Send Money"), fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BkashPink,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BkashPink)
                    .clickable { onConfirm(amountInput, sourceInput) }
                    .padding(vertical = 13.dp)
                    .testTag("confirm_add_bkash_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("✓ বিকাশ ব্যালেন্সে যোগ করুন", "✓ Add to bKash Balance"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddDbblDialog(
    isOpen: Boolean,
    accountNo: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    if (!isOpen) return

    var amountInput by remember { mutableStateOf("") }
    var sourceInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SlateCardBg)
                .border(1.dp, DbblBlue.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DbblBankLogo(modifier = Modifier.size(32.dp))
                    Column {
                        Text(
                            text = BanglaFormatters.tr("ডিবিবিএল ব্যাংকে টাকা জমা", "Deposit to DBBL Bank"),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nexus Card / A/C *$accountNo",
                            color = Color(0xFF93C5FD),
                            fontSize = 10.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text(BanglaFormatters.tr("জমার পরিমাণ (৳)", "Deposit Amount (৳)"), fontSize = 12.sp) },
                placeholder = { Text(BanglaFormatters.tr("যেমন: ৫০০০", "e.g. 5000"), fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_dbbl_amount_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DbblBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            val presets = listOf(1000, 2000, 5000, 10000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { amt ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DbblBlue.copy(alpha = 0.16f))
                            .border(1.dp, DbblBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { amountInput = BanglaFormatters.toBanglaDigits(amt) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+৳${BanglaFormatters.toBanglaDigits(amt)}",
                            color = Color(0xFF93C5FD),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = sourceInput,
                onValueChange = { sourceInput = it },
                label = { Text(BanglaFormatters.tr("উৎস বা বিবরণ (ঐচ্ছিক)", "Source / Note (Optional)"), fontSize = 12.sp) },
                placeholder = { Text(BanglaFormatters.tr("যেমন: বেতন, ব্যাংক ডিপোজিট, এনপিএসবি", "e.g. Salary, Bank Deposit"), fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DbblBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DbblBlue)
                    .clickable { onConfirm(amountInput, sourceInput) }
                    .padding(vertical = 13.dp)
                    .testTag("confirm_add_dbbl_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("✓ ডিবিবিএল ব্যাংকে জমা করুন", "✓ Deposit to DBBL Bank"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DbblBridgeDialog(
    state: DbblBridgeState,
    dbblBalance: Int,
    bkashBalance: Int,
    onDismiss: () -> Unit,
    onSwitchMode: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onAutoFillNotes: () -> Unit,
    onAdjustNote: (Int, Int) -> Unit,
    onConfirmTransfer: () -> Unit
) {
    if (!state.isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.dp, DbblBlue.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    DbblBankLogo(modifier = Modifier.size(34.dp))
                    Column {
                        Text(
                            text = BanglaFormatters.tr("ডিবিবিএল এটিএম ও নেক্সাসপে ব্রিজ", "DBBL ATM & NexusPay Bridge"),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = BanglaFormatters.tr(
                                "ব্যাংক ব্যালেন্স: ${BanglaFormatters.formatBDT(dbblBalance)} • বিকাশ: ${BanglaFormatters.formatBDT(bkashBalance)}",
                                "DBBL: ${BanglaFormatters.formatBDT(dbblBalance)} • bKash: ${BanglaFormatters.formatBDT(bkashBalance)}"
                            ),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            // Mode Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val modes = listOf(
                    "atm_withdraw" to BanglaFormatters.tr("🏧 এটিএম উত্তোলন", "🏧 ATM Withdraw"),
                    "to_bkash" to BanglaFormatters.tr("🏦 ব্যাংক ➔ বিকাশ", "🏦 Bank ➔ bKash"),
                    "from_bkash" to BanglaFormatters.tr("📱 বিকাশ ➔ ব্যাংক", "📱 bKash ➔ Bank")
                )
                modes.forEach { (key, label) ->
                    val active = state.mode == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) DbblBlue.copy(alpha = 0.25f) else SlateSurface)
                            .border(
                                1.dp,
                                if (active) DbblBlue else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSwitchMode(key) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color(0xFF93C5FD) else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.amountInput,
                    onValueChange = onAmountChange,
                    label = { Text(BanglaFormatters.tr("টাকার পরিমাণ (৳)", "Amount (৳)"), fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DbblBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (state.mode == "atm_withdraw") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.18f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onAutoFillNotes() }
                            .padding(horizontal = 10.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("✨ অটো নোট", "✨ Auto Notes"),
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (state.mode == "atm_withdraw") {
                Text(
                    text = BanglaFormatters.tr(
                        "এটিএম থেকে পাওয়া নোটে ক্লিক করুন (+১ যোগ হবে, কমাতে − চাপুন):",
                        "Tap ATM notes received (+1 per tap, tap − to remove):"
                    ),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                val rows = listOf(
                    listOf(1000, 500),
                    listOf(200, 100),
                    listOf(50, 20),
                    listOf(10, 5),
                    listOf(2, 1)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { rowDenoms ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowDenoms.forEach { denom ->
                                val cnt = state.notes[denom] ?: 0
                                PaymentClickableNoteCard(
                                    denom = denom,
                                    selectedCount = cnt,
                                    subLabel = BanglaFormatters.tr(
                                        "এটিএম: ${BanglaFormatters.toBanglaDigits(cnt)}টি",
                                        "ATM: ${BanglaFormatters.toBanglaDigits(cnt)}"
                                    ),
                                    canAdd = true,
                                    accentHighlight = NeonMint,
                                    testTagPrefix = "atm_note",
                                    modifier = Modifier.weight(1f),
                                    onAddOne = { onAdjustNote(denom, 1) },
                                    onMinusOne = { onAdjustNote(denom, -1) }
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DbblBlue)
                    .clickable { onConfirmTransfer() }
                    .padding(vertical = 13.dp)
                    .testTag("confirm_dbbl_bridge_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (state.mode) {
                        "atm_withdraw" -> BanglaFormatters.tr(
                            "✓ এটিএম উত্তোলন ও ভল্টে যোগ করুন (${BanglaFormatters.formatBDT(state.notesTotal)})",
                            "✓ Withdraw ATM Notes to Vault (${BanglaFormatters.formatBDT(state.notesTotal)})"
                        )
                        "to_bkash" -> BanglaFormatters.tr(
                            "✓ ডিবিবিএল থেকে বিকাশে স্থানান্তর (${BanglaFormatters.formatBDT(state.amount)})",
                            "✓ Transfer DBBL ➔ bKash (${BanglaFormatters.formatBDT(state.amount)})"
                        )
                        else -> BanglaFormatters.tr(
                            "✓ বিকাশ থেকে ডিবিবিএল ব্যাংকে স্থানান্তর (${BanglaFormatters.formatBDT(state.amount)})",
                            "✓ Transfer bKash ➔ DBBL (${BanglaFormatters.formatBDT(state.amount)})"
                        )
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddCashVaultDialog(
    tempNotes: Map<Int, Int>?,
    onDismiss: () -> Unit,
    onAdjustNote: (Int, Int) -> Unit,
    onConfirm: (String) -> Unit
) {
    if (tempNotes == null) return

    var sourceInput by remember { mutableStateOf("") }
    val totalAdd = BanglaFormatters.calculateNotesTotal(tempNotes)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = BanglaFormatters.tr("💵 ভাঙতি ভল্টে নতুন নোট যোগ করুন", "💵 Add Notes to Bhangti Vault"),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = BanglaFormatters.tr("নোটে ক্লিক করলে +১ যোগ হবে, কমাতে − চাপুন", "Tap note to add +1, tap − to remove"),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            OutlinedTextField(
                value = sourceInput,
                onValueChange = { sourceInput = it },
                label = { Text(BanglaFormatters.tr("উৎস বা বিবরণ (ঐচ্ছিক)", "Source / Memo (Optional)"), fontSize = 11.sp) },
                placeholder = { Text(BanglaFormatters.tr("যেমন: এটিএম বুথ, বেতন, দোকান থেকে প্রাপ্তি", "e.g. ATM, Salary, Shop"), fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonMint,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            val rows = listOf(
                listOf(1000, 500),
                listOf(200, 100),
                listOf(50, 20),
                listOf(10, 5),
                listOf(2, 1)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { rowDenoms ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowDenoms.forEach { denom ->
                            val cnt = tempNotes[denom] ?: 0
                            PaymentClickableNoteCard(
                                denom = denom,
                                selectedCount = cnt,
                                subLabel = "+${BanglaFormatters.formatBDT(denom * cnt)}",
                                canAdd = true,
                                accentHighlight = NeonMint,
                                testTagPrefix = "add_cash_note",
                                modifier = Modifier.weight(1f),
                                onAddOne = { onAdjustNote(denom, 1) },
                                onMinusOne = { onAdjustNote(denom, -1) }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonMint)
                    .clickable { onConfirm(sourceInput) }
                    .padding(vertical = 13.dp)
                    .testTag("confirm_add_cash_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr(
                        "✓ ভল্টে +${BanglaFormatters.formatBDT(totalAdd)} যোগ করুন",
                        "✓ Add +${BanglaFormatters.formatBDT(totalAdd)} to Vault"
                    ),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    if (!isOpen) return

    var nameInput by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("💊") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SlateCardBg)
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BanglaFormatters.tr("✨ নতুন খরচের খাত তৈরি করুন", "✨ Create Expense Category"),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(BanglaFormatters.tr("খাতের নাম", "Category Name"), fontSize = 12.sp) },
                placeholder = { Text(BanglaFormatters.tr("যেমন: ওষুধ ও চিকিৎসা, শিক্ষা", "e.g. Medicine, Education"), fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_category_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = BanglaFormatters.tr("আইকন নির্বাচন করুন:", "Choose Icon:"),
                color = TextSecondary,
                fontSize = 11.sp
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BanknoteCatalog.EMOJI_CHOICES.forEach { emoji ->
                    val active = selectedIcon == emoji
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) NeonCyan.copy(alpha = 0.2f) else SlateSurface)
                            .border(
                                1.dp,
                                if (active) NeonCyan else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedIcon = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan)
                    .clickable { onConfirm(nameInput, selectedIcon) }
                    .padding(vertical = 13.dp)
                    .testTag("confirm_add_category_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("✓ খাত যুক্ত করুন", "✓ Add Category"),
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Rich, multi-section Settings & Backup Studio Dialog:
 * 1. Light & Dark Theme Toggle + Bangla / English Language Toggle + Layout & Sound Toggles
 * 2. Show/Hide DBBL Bank & Card Toggle + DBBL Account Digits + Show/Hide Temp Money + Low-Bhangti Threshold
 * 3. Custom Image Importer Studio for Logos (App, bKash, DBBL) and all 10 Banknotes (1000..1)
 * 4. Monthly Budget & Savings Goal Configuration
 * 5. Real File Backup Generator (.json file & .csv spreadsheet file via CreateDocument) + Restore
 */
@Composable
fun SettingsAndBackupDialog(
    isOpen: Boolean,
    walletConfig: WalletConfigEntity,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit,
    onSetLanguage: (String) -> Unit,
    onToggleLayoutMode: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleBiometricLock: () -> Unit,
    onSaveBiometricPin: (String) -> Unit,
    onLockAppNow: () -> Unit,
    onToggleShowDbbl: () -> Unit,
    onSaveDbblAccountNo: (String) -> Unit,
    onToggleShowTempMoney: () -> Unit,
    onSaveLowBhangtiThreshold: (Int) -> Unit,
    onSaveBudget: (String) -> Unit,
    onSaveSavingsGoal: (String) -> Unit,
    onPickCustomImageForKey: (String) -> Unit,
    onResetCustomImageKey: (String) -> Unit,
    onResetAllCustomImages: () -> Unit,
    onSaveJsonFileClick: () -> Unit,
    onSaveCsvFileClick: () -> Unit,
    onExportPdfWithPeriod: (String, Int) -> Unit,
    onImportJsonClick: () -> Unit,
    onHardResetZero: () -> Unit,
    onReloadDemoData: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var budgetInput by remember(walletConfig.monthlyBudget) {
        mutableStateOf(BanglaFormatters.toBanglaDigits(walletConfig.monthlyBudget))
    }
    var savingsInput by remember(walletConfig.savingsGoal) {
        mutableStateOf(BanglaFormatters.toBanglaDigits(walletConfig.savingsGoal))
    }
    var dbblAccInput by remember(walletConfig.dbblAccountNo) {
        mutableStateOf(walletConfig.dbblAccountNo)
    }
    var pinInput by remember(walletConfig.biometricPinCode) {
        mutableStateOf(walletConfig.biometricPinCode)
    }
    var pdfPeriodChoice by remember { mutableStateOf("month") }
    var pdfCustomDaysInput by remember { mutableStateOf("15") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = BanglaFormatters.tr("⚙️ কন্ট্রোল সেন্টার, সিকিউরিটি ও ফাইল ব্যাকআপ", "⚙️ Settings, Biometric Security & File Backup"),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = BanglaFormatters.tr(
                            "ফিঙ্গারপ্রিন্ট লক, থিম, ভাষা, ব্যাংক অপশন, নোটের ছবি ও PDF/JSON ব্যাকআপ",
                            "Fingerprint lock, theme, language, DBBL visibility, note images & PDF/JSON backup"
                        ),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .testTag("close_settings_modal"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // SECTION 0: Biometric Fingerprint Authentication on App Launch
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = BanglaFormatters.tr(
                                "🔐 বায়োমেট্রিক ফিঙ্গারপ্রিন্ট লক (Biometric Unlock)",
                                "🔐 Biometric Fingerprint Lock on Launch"
                            ),
                            color = NeonMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = BanglaFormatters.tr(
                                "অ্যাপ চালু করার সময় ফিঙ্গারপ্রিন্ট আনলক স্ক্রিন দিয়ে আপনার আর্থিক তথ্য সুরক্ষিত রাখুন",
                                "Require fingerprint authentication on the app launch screen to secure your financial data"
                            ),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = walletConfig.biometricLockEnabled,
                        onCheckedChange = { onToggleBiometricLock() },
                        modifier = Modifier.testTag("toggle_biometric_lock_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonMint,
                            checkedTrackColor = NeonMint.copy(alpha = 0.38f)
                        )
                    )
                }

                if (walletConfig.biometricLockEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it.take(8) },
                            label = {
                                Text(
                                    BanglaFormatters.tr("ব্যাকআপ সিকিউরিটি পিন (৪ সংখ্যার)", "Backup Security PIN (4+ digits)"),
                                    fontSize = 10.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("biometric_pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonMint,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonMint)
                                .clickable { onSaveBiometricPin(pinInput) }
                                .padding(horizontal = 12.dp, vertical = 13.dp)
                                .testTag("save_biometric_pin_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = BanglaFormatters.tr("পিন সেভ", "Save PIN"),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.18f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .clickable {
                                onDismiss()
                                onLockAppNow()
                            }
                            .padding(vertical = 9.dp)
                            .testTag("btn_lock_app_now"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr(
                                "🔒 এখনই লক স্ক্রিন টেস্ট করুন (Lock App Now)",
                                "🔒 Lock App Now & Test Fingerprint Unlock"
                            ),
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // SECTION 1: Theme (Light / Dark) & Language (Bangla / English)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = BanglaFormatters.tr("🎨 ১. থিম, ভাষা ও ডিসপ্লে সেটিংস", "🎨 1. Theme, Language & Display"),
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Light vs Dark Theme Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isDark = walletConfig.themeMode != "light"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) NeonCyan.copy(alpha = 0.22f) else SlateSurface)
                            .border(1.dp, if (isDark) NeonCyan else Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable { if (!isDark) onToggleTheme() }
                            .padding(vertical = 9.dp)
                            .testTag("theme_dark_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("🌙 ডার্ক থিম (Dark)", "🌙 Dark Theme"),
                            color = if (isDark) NeonCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isDark) AmberGold.copy(alpha = 0.22f) else SlateSurface)
                            .border(1.dp, if (!isDark) AmberGold else Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable { if (isDark) onToggleTheme() }
                            .padding(vertical = 9.dp)
                            .testTag("theme_light_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("☀️ লাইট থিম (Light)", "☀️ Light Theme"),
                            color = if (!isDark) AmberGold else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bangla vs English Language Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isBn = walletConfig.language != "en"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBn) NeonMint.copy(alpha = 0.22f) else SlateSurface)
                            .border(1.dp, if (isBn) NeonMint else Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable { onSetLanguage("bn") }
                            .padding(vertical = 9.dp)
                            .testTag("lang_bn_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🇧🇩 বাংলা (Bangla)",
                            color = if (isBn) NeonMint else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBn) NeonMint.copy(alpha = 0.22f) else SlateSurface)
                            .border(1.dp, if (!isBn) NeonMint else Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable { onSetLanguage("en") }
                            .padding(vertical = 9.dp)
                            .testTag("lang_en_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🇬🇧 English (ইংরেজি)",
                            color = if (!isBn) NeonMint else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sound & Full Dashboard Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = BanglaFormatters.tr("🔊 সাউন্ড ও হ্যাপটিক ভাইব্রেশন", "🔊 Sound & Haptic Feedback"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = walletConfig.soundEnabled,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonMint, checkedTrackColor = NeonMint.copy(alpha = 0.35f))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = BanglaFormatters.tr("🖥️ ফুল পেজ ড্যাশবোর্ড মোড (সব ট্যাব একসাথে)", "🖥️ Full Dashboard Mode (All sections)"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = walletConfig.layoutMode == "full",
                        onCheckedChange = { onToggleLayoutMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.35f))
                    )
                }
            }

            // SECTION 2: Bank & Card Visibility Toggle + Module Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, DbblBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = BanglaFormatters.tr("🏦 ২. ব্যাংক ও কার্ড অপশন এবং মডিউল নিয়ন্ত্রণ", "🏦 2. Bank & Card Option & Modules"),
                    color = Color(0xFF93C5FD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = BanglaFormatters.tr("🏦 ডিবিবিএল ব্যাংক ও কার্ড অপশন দেখান", "🏦 Show DBBL Bank & Card Option"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = BanglaFormatters.tr(
                                "হোম স্ক্রিন ও পেমেন্ট অপশনে ব্যাংক/কার্ড চালু বা বন্ধ করুন",
                                "Show or hide Bank & Card on Home screen and Payment modal"
                            ),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = walletConfig.showDbblSection,
                        onCheckedChange = { onToggleShowDbbl() },
                        modifier = Modifier.testTag("toggle_show_dbbl_switch"),
                        colors = SwitchDefaults.colors(checkedThumbColor = DbblBlue, checkedTrackColor = DbblBlue.copy(alpha = 0.4f))
                    )
                }

                if (walletConfig.showDbblSection) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dbblAccInput,
                            onValueChange = { dbblAccInput = it.take(4) },
                            label = { Text(BanglaFormatters.tr("কার্ড/অ্যাকাউন্টের শেষ ৪ ডিজিট", "Card/Account Last 4 Digits"), fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DbblBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DbblBlue)
                                .clickable { onSaveDbblAccountNo(dbblAccInput) }
                                .padding(horizontal = 12.dp, vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = BanglaFormatters.tr("সেভ", "Save"),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = BanglaFormatters.tr("⏳ অস্থায়ী টাকা ট্র্যাকার হোমে দেখান", "⏳ Show Temporary Money Section"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = walletConfig.showTempMoneySection,
                        onCheckedChange = { onToggleShowTempMoney() },
                        colors = SwitchDefaults.colors(checkedThumbColor = AmberGold, checkedTrackColor = AmberGold.copy(alpha = 0.35f))
                    )
                }

                // Low-Bhangti Alert Threshold
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = BanglaFormatters.tr(
                            "⚠️ খুচরা ভাঙতি সতর্কতা সীমা (বর্তমান: ${BanglaFormatters.toBanglaDigits(walletConfig.lowBhangtiThreshold)}টি ছোট নোট)",
                            "⚠️ Low Small-Change Alert Threshold (${walletConfig.lowBhangtiThreshold} notes)"
                        ),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 8, 12, 15).forEach { th ->
                            val active = walletConfig.lowBhangtiThreshold == th
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NeonMint.copy(alpha = 0.2f) else SlateSurface)
                                    .border(1.dp, if (active) NeonMint else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { onSaveLowBhangtiThreshold(th) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${BanglaFormatters.toBanglaDigits(th)}${BanglaFormatters.tr("টি", " notes")}",
                                    color = if (active) NeonMint else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: Custom Image Importer Studio for Logos & All 10 Banknotes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = BanglaFormatters.tr(
                                "🖼️ ৩. লোগো ও ব্যাংকনোটের কাস্টম ছবি ইমপোর্ট স্টুডিও",
                                "🖼️ 3. Custom Logo & Banknote Image Importer"
                            ),
                            color = NeonMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = BanglaFormatters.tr(
                                "ইমপোর্ট করা নোটের ছবি হোম ও পেমেন্ট/খরচ সেকশনেও দেখা যাবে",
                                "Imported banknote images appear on both Home and Expenditure sections"
                            ),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPink.copy(alpha = 0.16f))
                            .border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onResetAllCustomImages() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.tr("সব রিসেট", "Reset All"),
                            color = NeonPink,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Logos Row
                Text(
                    text = BanglaFormatters.tr("লোগো কাস্টমাইজেশন (অ্যাপ, বিকাশ, ডিবিবিএল):", "Custom Logos (App, bKash, DBBL):"),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val logoItems = listOf(
                        CustomAssetManager.KEY_LOGO_APP to BanglaFormatters.tr("অ্যাপ লোগো", "App Logo"),
                        CustomAssetManager.KEY_LOGO_BKASH to BanglaFormatters.tr("বিকাশ লোগো", "bKash Logo"),
                        CustomAssetManager.KEY_LOGO_DBBL to BanglaFormatters.tr("ডিবিবিএল লোগো", "DBBL Logo")
                    )
                    logoItems.forEach { (key, label) ->
                        val hasCustom = CustomAssetManager.hasCustomAsset(context, key)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlateSurface)
                                .border(
                                    1.dp,
                                    if (hasCustom) NeonMint else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonCyan.copy(alpha = 0.2f))
                                        .clickable { onPickCustomImageForKey(key) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                        .testTag("pick_img_$key")
                                ) {
                                    Text(
                                        text = BanglaFormatters.tr("📂 ছবি", "📂 Pick"),
                                        color = NeonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (hasCustom) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonPink.copy(alpha = 0.2f))
                                            .clickable { onResetCustomImageKey(key) }
                                            .padding(horizontal = 5.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "✕", color = NeonPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Banknotes Grid (1000 down to 1)
                Text(
                    text = BanglaFormatters.tr("ব্যাংকনোটের ছবি ইমপোর্ট (৳১০০০ থেকে ৳১):", "Banknote Images (৳1000 to ৳1):"),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val noteRows = listOf(
                    listOf(1000, 500, 200, 100, 50),
                    listOf(20, 10, 5, 2, 1)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    noteRows.forEach { rDenoms ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rDenoms.forEach { d ->
                                val key = CustomAssetManager.noteKey(d)
                                val bmp = CustomAssetManager.getCustomBitmap(context, key)
                                val style = BanknoteCatalog.styleFor(d)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(style.primaryColor.copy(alpha = 0.65f))
                                        .border(
                                            1.dp,
                                            if (bmp != null) NeonMint else style.borderColor,
                                            RoundedCornerShape(9.dp)
                                        )
                                        .clickable { onPickCustomImageForKey(key) }
                                        .padding(5.dp)
                                        .testTag("pick_note_img_$d"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = "৳$d",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(22.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                    Text(
                                        text = "৳${BanglaFormatters.toBanglaDigits(d)}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = if (bmp != null) BanglaFormatters.tr("✓ কাস্টম", "✓ Set")
                                            else BanglaFormatters.tr("+ ছবি", "+ Img"),
                                            color = if (bmp != null) NeonMint else style.accentColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (bmp != null) {
                                            Text(
                                                text = "✕",
                                                color = NeonPink,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.clickable { onResetCustomImageKey(key) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: Monthly Budget & Savings Goal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = BanglaFormatters.tr("🎯 ৪. মাসিক বাজেট ও সঞ্চয় লক্ষ্যমাত্রা", "🎯 4. Monthly Budget & Savings Target"),
                    color = AmberGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text(BanglaFormatters.tr("মাসিক বাজেট (৳)", "Monthly Budget (৳)"), fontSize = 10.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan)
                            .clickable { onSaveBudget(budgetInput) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("বাজেট সেভ", "Save"),
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = savingsInput,
                        onValueChange = { savingsInput = it },
                        label = { Text(BanglaFormatters.tr("সঞ্চয় লক্ষ্যমাত্রা (৳)", "Savings Goal (৳)"), fontSize = 10.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonMint)
                            .clickable { onSaveSavingsGoal(savingsInput) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("সঞ্চয় সেভ", "Save"),
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // SECTION 5: File-Based Data Backup (.pdf, .json & .csv files) & Reset
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = BanglaFormatters.tr(
                        "💾 ৫. ফাইল ব্যাকআপ (.PDF রিপোর্ট, .JSON ও .CSV ফাইল)",
                        "💾 5. PDF History Export & File Backup (.json & .csv)"
                    ),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = BanglaFormatters.tr(
                        "কত দিনের, এই মাসের অথবা এই বছরের লেনদেন ইতিহাসের PDF ফাইল তৈরি করুন:",
                        "Choose period (How many days, This Month, This Year, or All Time) and export History to PDF:"
                    ),
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                // Period selector for PDF Export in Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val periodOptions = listOf(
                        "month" to BanglaFormatters.tr("এই মাস", "This Month"),
                        "year" to BanglaFormatters.tr("এই বছর", "This Year"),
                        "custom_days" to BanglaFormatters.tr("কত দিন", "Custom Days"),
                        "all" to BanglaFormatters.tr("সব সময়", "All Time")
                    )
                    periodOptions.forEach { (key, label) ->
                        val active = pdfPeriodChoice == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) NeonMint.copy(alpha = 0.22f) else SlateSurface)
                                .border(1.dp, if (active) NeonMint else Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .clickable { pdfPeriodChoice = key }
                                .padding(vertical = 6.dp)
                                .testTag("settings_pdf_period_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (active) NeonMint else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (pdfPeriodChoice == "custom_days") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pdfCustomDaysInput,
                            onValueChange = { pdfCustomDaysInput = it.take(4) },
                            label = {
                                Text(
                                    BanglaFormatters.tr("কত দিনের ইতিহাস? (যেমন: 7, 15, 45)", "How many days? (e.g. 7, 15, 45)"),
                                    fontSize = 10.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_pdf_custom_days_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Export PDF Button in Settings
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NeonMint.copy(alpha = 0.28f), NeonCyan.copy(alpha = 0.25f))
                            )
                        )
                        .border(1.dp, NeonMint, RoundedCornerShape(10.dp))
                        .clickable {
                            val parsedDays = BanglaFormatters.parseBanglaNumber(pdfCustomDaysInput).let {
                                if (it > 0) it else 7
                            }
                            onExportPdfWithPeriod(pdfPeriodChoice, parsedDays)
                        }
                        .padding(vertical = 11.dp)
                        .testTag("btn_settings_export_pdf"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr(
                            "📄 লেনদেন ইতিহাসের .PDF রিপোর্ট ফাইল সেভ করুন",
                            "📄 Export Transaction History to .PDF File"
                        ),
                        color = NeonMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonMint.copy(alpha = 0.2f))
                            .border(1.dp, NeonMint, RoundedCornerShape(10.dp))
                            .clickable { onSaveJsonFileClick() }
                            .padding(vertical = 11.dp)
                            .testTag("btn_save_backup_json_file"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("💾 .JSON ব্যাকআপ ফাইল সেভ", "💾 Save .JSON Backup File"),
                            color = NeonMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                            .clickable { onSaveCsvFileClick() }
                            .padding(vertical = 11.dp)
                            .testTag("btn_save_csv_file"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("📊 .CSV রিপোর্ট ফাইল সেভ", "📊 Save .CSV Report File"),
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateSurface)
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .clickable { onImportJsonClick() }
                        .padding(vertical = 10.dp)
                        .testTag("btn_restore_json_file"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("📂 JSON ব্যাকআপ ফাইল থেকে রিস্টোর করুন", "📂 Restore from .JSON Backup File"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberGold.copy(alpha = 0.16f))
                            .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onReloadDemoData() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("🔄 নমুনা ডাটা রিলোড", "🔄 Load Demo Data"),
                            color = AmberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonPink.copy(alpha = 0.18f))
                            .border(1.dp, NeonPink.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .clickable { onHardResetZero() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("🗑️ সব শূন্য (০) করুন", "🗑️ Reset All to Zero"),
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    tx: TransactionEntity?,
    state: TakaVaultUiState,
    onDismiss: () -> Unit
) {
    if (tx == null) return

    val cat = state.categories.find { it.id == tx.categoryId }
    val isInflow = tx.type == "inflow"
    val methodText = when (tx.method) {
        "bkash" -> BanglaFormatters.tr("বিকাশ ডিজিটাল পেমেন্ট", "bKash Digital")
        "dbbl" -> BanglaFormatters.tr("ডিবিবিএল ব্যাংক / নেক্সাস কার্ড", "DBBL Bank / Card")
        else -> BanglaFormatters.tr("নগদ ভাঙতি ভল্ট", "Cash Vault")
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SlateCardBg)
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BanglaFormatters.tr("🧾 ডিজিটাল ভাউচার রসিদ", "🧾 Digital Transaction Receipt"),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReceiptRow(BanglaFormatters.tr("ভাউচার আইডি:", "Voucher ID:"), tx.id)
                ReceiptRow(BanglaFormatters.tr("তারিখ ও সময়:", "Date & Time:"), BanglaFormatters.formatBanglaDateTime(tx.timestamp))
                ReceiptRow(BanglaFormatters.tr("খাত:", "Category:"), "${cat?.icon ?: "📦"} ${cat?.name ?: BanglaFormatters.tr("তহবিল জমা", "Inflow")}")
                ReceiptRow(BanglaFormatters.tr("মাধ্যম:", "Method:"), methodText)
                ReceiptRow(
                    BanglaFormatters.tr("পরিমাণ:", "Amount:"),
                    "${if (isInflow) "+" else "−"}${BanglaFormatters.formatBDT(tx.amount)}",
                    if (isInflow) NeonMint else NeonPink
                )
                if (tx.note.isNotBlank()) {
                    ReceiptRow(BanglaFormatters.tr("বিবরণ:", "Memo:"), tx.note)
                }
                if (tx.method == "cash") {
                    if (tx.tenderedNotes.isNotEmpty()) {
                        ReceiptRow(
                            BanglaFormatters.tr("দেওয়া নোট:", "Given Notes:"),
                            BanglaFormatters.summarizeNotesInline(tx.tenderedNotes),
                            NeonCyan
                        )
                    }
                    if (tx.receivedNotes.isNotEmpty()) {
                        ReceiptRow(
                            BanglaFormatters.tr("ফেরত ভাঙতি:", "Change Notes:"),
                            BanglaFormatters.summarizeNotesInline(tx.receivedNotes),
                            NeonMint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, color: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NotePreviewDialog(
    denom: Int?,
    count: Int,
    onDismiss: () -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onPickCustomNoteImage: ((Int) -> Unit)? = null
) {
    if (denom == null) return

    val style = BanknoteCatalog.styleFor(denom)
    val totalValue = denom * count

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.5.dp, style.borderColor, RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = style.label,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = style.monumentTitle,
                        color = style.accentColor,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .testTag("close_note_preview_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            BangladeshiBanknoteBanner(
                denom = denom,
                count = count,
                height = 124.dp,
                compactMode = false
            )

            Text(
                text = style.metaDescription,
                color = TextSecondary,
                fontSize = 11.sp
            )

            if (onPickCustomNoteImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.14f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { onPickCustomNoteImage(denom) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr(
                            "🖼️ গ্যালারি থেকে এই নোটের কাস্টম ছবি সেট করুন",
                            "🖼️ Import Custom Image for ৳$denom Note"
                        ),
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Prominent Plus / Minus Controls inside the Popup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = BanglaFormatters.tr("ভল্টে মোট মূল্যমান:", "Total Vault Value:"),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = BanglaFormatters.formatBDT(totalValue),
                        color = NeonMint,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurface)
                            .border(1.dp, NeonPink.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .clickable { onMinus() }
                            .testTag("popup_note_minus_$denom"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "−", color = NeonPink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(
                        text = "${BanglaFormatters.toBanglaDigits(count)}${BanglaFormatters.tr("টি", "")}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonMint.copy(alpha = 0.2f))
                            .border(1.dp, NeonMint, RoundedCornerShape(12.dp))
                            .clickable { onPlus() }
                            .testTag("popup_note_plus_$denom"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = NeonMint, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun BkashBridgeDialog(
    state: BkashBridgeState,
    onDismiss: () -> Unit,
    onSwitchMode: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onFeeRateChange: (Double) -> Unit,
    onAutoFillNotes: () -> Unit,
    onAdjustNote: (Int, Int) -> Unit,
    onConfirmTransfer: () -> Unit
) {
    if (!state.isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.dp, BkashPink.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BkashOrigamiLogo(modifier = Modifier.size(32.dp))
                    Text(
                        text = BanglaFormatters.tr("বিকাশ ⇄ ভাঙতি ভল্ট ব্রিজ", "bKash ⇄ Cash Vault Bridge"),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (state.mode == "cashout") BkashPink.copy(alpha = 0.25f) else SlateSurface)
                        .border(
                            1.dp,
                            if (state.mode == "cashout") BkashPink else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSwitchMode("cashout") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("📲 ক্যাশ-আউট (বিকাশ ➔ ভল্ট)", "📲 Cash-Out (bKash ➔ Vault)"),
                        color = if (state.mode == "cashout") Color(0xFFFBCFE8) else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (state.mode == "cashin") NeonMint.copy(alpha = 0.22f) else SlateSurface)
                        .border(
                            1.dp,
                            if (state.mode == "cashin") NeonMint else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSwitchMode("cashin") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("📥 ক্যাশ-ইন (ভল্ট ➔ বিকাশ)", "📥 Cash-In (Vault ➔ bKash)"),
                        color = if (state.mode == "cashin") NeonMint else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.amountInput,
                    onValueChange = onAmountChange,
                    label = { Text(BanglaFormatters.tr("পরিমাণ (৳)", "Amount (৳)"), fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BkashPink,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.18f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { onAutoFillNotes() }
                        .padding(horizontal = 10.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("✨ অটো নোট", "✨ Auto Notes"),
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state.mode == "cashout") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rates = listOf(1.85 to "১.৮৫% এজেন্ট", 1.49 to "১.৪৯% প্রিয়/ATM", 0.0 to "০% চার্জ ছাড়া")
                    rates.forEach { (r, label) ->
                        val active = abs(state.feeRatePercent - r) < 0.01
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) BkashPink.copy(alpha = 0.22f) else SlateSurface)
                                .border(1.dp, if (active) BkashPink else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { onFeeRateChange(r) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color(0xFFFBCFE8) else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            val rows = listOf(
                listOf(1000, 500),
                listOf(200, 100),
                listOf(50, 20),
                listOf(10, 5),
                listOf(2, 1)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { rowDenoms ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowDenoms.forEach { denom ->
                            val cnt = state.notes[denom] ?: 0
                            PaymentClickableNoteCard(
                                denom = denom,
                                selectedCount = cnt,
                                subLabel = BanglaFormatters.formatBDT(denom * cnt),
                                canAdd = true,
                                accentHighlight = NeonMint,
                                testTagPrefix = "bridge_note",
                                modifier = Modifier.weight(1f),
                                onAddOne = { onAdjustNote(denom, 1) },
                                onMinusOne = { onAdjustNote(denom, -1) }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (state.mode == "cashout") BkashPink else NeonMint)
                    .clickable { onConfirmTransfer() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.mode == "cashout") {
                        BanglaFormatters.tr(
                            "✓ ক্যাশ-আউট সম্পন্ন করুন (${BanglaFormatters.formatBDT(state.notesTotal)})",
                            "✓ Complete Cash-Out (${BanglaFormatters.formatBDT(state.notesTotal)})"
                        )
                    } else {
                        BanglaFormatters.tr(
                            "✓ ক্যাশ-ইন সম্পন্ন করুন (${BanglaFormatters.formatBDT(state.notesTotal)})",
                            "✓ Complete Cash-In (${BanglaFormatters.formatBDT(state.notesTotal)})"
                        )
                    },
                    color = if (state.mode == "cashout") Color.White else Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun NoteExchangeDialog(
    state: NoteExchangeState,
    vaultNotes: Map<Int, Int>,
    onDismiss: () -> Unit,
    onSelectSource: (Int) -> Unit,
    onAutoBreak: () -> Unit,
    onAdjustTarget: (Int, Int) -> Unit,
    onConfirmExchange: () -> Unit
) {
    if (!state.isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BanglaFormatters.tr("🔄 বড় নোট ভাঙতি রূপান্তরকারী", "🔄 Large Note Bhangti Exchanger"),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            Text(
                text = BanglaFormatters.tr("যে বড় নোটটি ভাঙাতে চান নির্বাচন করুন:", "Select the note to break:"),
                color = TextSecondary,
                fontSize = 11.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1000, 500, 200, 100, 50).forEach { d ->
                    val inVault = vaultNotes[d] ?: 0
                    val active = state.sourceDenom == d
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) NeonCyan.copy(alpha = 0.22f) else SlateSurface)
                            .border(
                                1.dp,
                                if (active) NeonCyan else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectSource(d) }
                            .padding(vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "৳${BanglaFormatters.toBanglaDigits(d)}",
                            color = if (active) NeonCyan else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${BanglaFormatters.toBanglaDigits(inVault)}${BanglaFormatters.tr("টি", "x")}",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            val smallerDenoms = BanglaFormatters.DENOMINATIONS.filter { it < state.sourceDenom }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                smallerDenoms.chunked(2).forEach { rowDenoms ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowDenoms.forEach { d ->
                            val cnt = state.targetNotes[d] ?: 0
                            PaymentClickableNoteCard(
                                denom = d,
                                selectedCount = cnt,
                                subLabel = "+${BanglaFormatters.formatBDT(d * cnt)}",
                                canAdd = true,
                                accentHighlight = NeonMint,
                                testTagPrefix = "exchange_note",
                                modifier = Modifier.weight(1f),
                                onAddOne = { onAdjustTarget(d, 1) },
                                onMinusOne = { onAdjustTarget(d, -1) }
                            )
                        }
                        if (rowDenoms.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianDark)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = BanglaFormatters.tr("মূল নোট: ৳${BanglaFormatters.toBanglaDigits(state.sourceDenom)}", "Source: ৳${state.sourceDenom}"),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = BanglaFormatters.tr("ভাঙতি যোগফল: ${BanglaFormatters.formatBDT(state.targetTotal)}", "Broken Sum: ${BanglaFormatters.formatBDT(state.targetTotal)}"),
                    color = if (state.targetTotal == state.sourceDenom) NeonMint else NeonPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan)
                    .clickable { onConfirmExchange() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("✓ ভাঙতি রূপান্তর সম্পন্ন করুন", "✓ Confirm Note Break"),
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun QuickCalculatorDialog(
    state: CalculatorState,
    onDismiss: () -> Unit,
    onInputToken: (String) -> Unit,
    onUseResultForExpense: () -> Unit
) {
    if (!state.isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BanglaFormatters.tr("🧮 বাজারের দ্রুত হিসাব ক্যালকুলেটর", "🧮 Quick Market Calculator"),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = state.expression.ifEmpty { "0" },
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = BanglaFormatters.formatBDT(state.lastResult),
                    color = NeonMint,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            val rows = listOf(
                listOf("C", "DEL", "/", "*"),
                listOf("7", "8", "9", "-"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "="),
                listOf("0", "00", "50", "100")
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { rowTokens ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowTokens.forEach { token ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (token) {
                                            "=" -> NeonMint
                                            "C", "DEL" -> NeonPink.copy(alpha = 0.2f)
                                            "/", "*", "-", "+" -> NeonCyan.copy(alpha = 0.2f)
                                            else -> SlateSurface
                                        }
                                    )
                                    .clickable { onInputToken(token) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = token,
                                    color = if (token == "=") Color.Black else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan)
                    .clickable { onUseResultForExpense() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("⚡ এই ফল দিয়ে নতুন খরচ পেমেন্ট করুন", "⚡ Use Result for New Expense"),
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
