package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.viewmodel.ExpenseStep
import com.example.viewmodel.ExpenseWorkflowState
import com.example.viewmodel.TakaVaultUiState
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseWorkflowDialog(
    state: TakaVaultUiState,
    workflow: ExpenseWorkflowState,
    onDismiss: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onSelectMethod: (String) -> Unit,
    onGoToStep: (ExpenseStep) -> Unit,
    onBillAmountChange: (String) -> Unit,
    onBillPresetClick: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirmBkashSpend: () -> Unit,
    onConfirmDbblSpend: () -> Unit,
    onAdjustTenderNote: (Int, Int) -> Unit,
    onAutoSelectTender: () -> Unit,
    onClearTender: () -> Unit,
    onProceedToChangeStep: () -> Unit,
    onAdjustReceivedNote: (Int, Int) -> Unit,
    onAutoFillChange: () -> Unit,
    onClearReceived: () -> Unit,
    onCommitCashExpense: () -> Unit
) {
    if (!workflow.isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(SlateCardBg)
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonPink.copy(alpha = 0.2f))
                            .border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💸", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = BanglaFormatters.tr("নতুন খরচ ও পেমেন্ট প্রসেসর", "Smart Expense & Payment"),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = BanglaFormatters.tr(
                                "নোট ক্লিক করলেই খরচের পরিমাণে অটো যোগ হবে",
                                "Tap any note to auto-add to expenditure"
                            ),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .testTag("close_expense_modal"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 14.sp)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Category Selection Strip
            Text(
                text = BanglaFormatters.tr("খরচের খাত নির্বাচন করুন", "Select Expense Category"),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.categories.forEach { cat ->
                    val selected = workflow.categoryId == cat.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) NeonCyan.copy(alpha = 0.2f)
                                else SlateSurface
                            )
                            .border(
                                1.dp,
                                if (selected) NeonCyan else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onCategoryChange(cat.id) }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${cat.icon} ${cat.name}",
                            color = if (selected) NeonCyan else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            when (workflow.step) {
                ExpenseStep.STEP_1_METHOD -> {
                    Text(
                        text = BanglaFormatters.tr("ধাপ ১: পেমেন্ট মাধ্যম নির্বাচন করুন", "Step 1: Choose Payment Method"),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // bKash Option
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            BkashPink.copy(alpha = 0.28f),
                                            Color(0xFF1F122B)
                                        )
                                    )
                                )
                                .border(1.dp, BkashPink.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                .clickable { onSelectMethod("bkash") }
                                .padding(10.dp)
                                .testTag("method_bkash_btn"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            BkashOrigamiLogo(modifier = Modifier.size(34.dp))
                            Text(
                                text = BanglaFormatters.tr("বিকাশ ওয়ালেট", "bKash Wallet"),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = BanglaFormatters.formatBDT(state.walletConfig.bkashBalance),
                                color = Color(0xFFFBCFE8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Cash Vault Option
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            NeonMint.copy(alpha = 0.2f),
                                            Color(0xFF0E2229)
                                        )
                                    )
                                )
                                .border(1.dp, NeonMint.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable { onSelectMethod("cash") }
                                .padding(10.dp)
                                .testTag("method_cash_btn"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonMint.copy(alpha = 0.2f))
                                    .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "💵", fontSize = 18.sp)
                            }
                            Text(
                                text = BanglaFormatters.tr("নগদ ভাঙতি ভল্ট", "Cash Vault"),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = BanglaFormatters.formatBDT(state.cashTotal),
                                color = NeonMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // DBBL Bank & Card Option (shown when enabled in Settings)
                        if (state.walletConfig.showDbblSection) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                DbblBlue.copy(alpha = 0.28f),
                                                Color(0xFF0F1B33)
                                            )
                                        )
                                    )
                                    .border(1.dp, DbblBlue.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                    .clickable { onSelectMethod("dbbl") }
                                    .padding(10.dp)
                                    .testTag("method_dbbl_btn"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                DbblBankLogo(modifier = Modifier.size(34.dp))
                                Text(
                                    text = BanglaFormatters.tr("ডিবিবিএল কার্ড", "DBBL Card"),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = BanglaFormatters.formatBDT(state.walletConfig.dbblBalance),
                                    color = Color(0xFF93C5FD),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                ExpenseStep.STEP_2A_BKASH -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BanglaFormatters.tr("← পেমেন্ট মাধ্যম পরিবর্তন", "← Change Payment Method"),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onGoToStep(ExpenseStep.STEP_1_METHOD) }
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BkashPink.copy(alpha = 0.2f))
                                .border(1.dp, BkashPink.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.tr("বিকাশ ডিজিটাল পেমেন্ট", "bKash Digital Payment"),
                                color = Color(0xFFFBCFE8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = workflow.billAmountInput,
                        onValueChange = onBillAmountChange,
                        label = { Text(BanglaFormatters.tr("খরচের পরিমাণ (৳)", "Expense Amount (৳)"), fontSize = 12.sp) },
                        placeholder = { Text(BanglaFormatters.tr("যেমন: ২৫০", "e.g. 250"), fontSize = 13.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bkash_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BkashPink,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val presets = listOf(50, 100, 200, 500, 1000)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BkashPink.copy(alpha = 0.15f))
                                    .border(1.dp, BkashPink.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable { onBillPresetClick(amt) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "৳${BanglaFormatters.toBanglaDigits(amt)}",
                                    color = Color(0xFFFBCFE8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = workflow.noteInput,
                        onValueChange = onNoteChange,
                        label = { Text(BanglaFormatters.tr("বিবরণ / মেমো (ঐচ্ছিক)", "Memo / Note (Optional)"), fontSize = 12.sp) },
                        placeholder = { Text(BanglaFormatters.tr("যেমন: বিদ্যুৎ বিল, সুপারশপ", "e.g. Electricity bill"), fontSize = 12.sp) },
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

                    val rem = state.walletConfig.bkashBalance - workflow.billAmount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BkashPink.copy(alpha = 0.12f))
                            .border(1.dp, BkashPink.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = BanglaFormatters.tr("খরচের পর অবশিষ্ট বিকাশ ব্যালেন্স:", "Remaining bKash Balance:"),
                            color = Color(0xFFFBCFE8),
                            fontSize = 12.sp
                        )
                        Text(
                            text = BanglaFormatters.formatBDT(rem),
                            color = if (rem < 0) NeonPink else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    workflow.bkashError?.let { err ->
                        Text(
                            text = err,
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BkashPink)
                            .clickable { onConfirmBkashSpend() }
                            .padding(vertical = 14.dp)
                            .testTag("confirm_bkash_spend_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("✓ বিকাশ পেমেন্ট নিশ্চিত করুন", "✓ Confirm bKash Payment"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                ExpenseStep.STEP_2C_DBBL -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BanglaFormatters.tr("← পেমেন্ট মাধ্যম পরিবর্তন", "← Change Payment Method"),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onGoToStep(ExpenseStep.STEP_1_METHOD) }
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DbblBlue.copy(alpha = 0.2f))
                                .border(1.dp, DbblBlue.copy(alpha = 0.45f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.tr(
                                    "ডিবিবিএল নেক্সাস কার্ড (*${state.walletConfig.dbblAccountNo})",
                                    "DBBL Nexus Card (*${state.walletConfig.dbblAccountNo})"
                                ),
                                color = Color(0xFF93C5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = workflow.billAmountInput,
                        onValueChange = onBillAmountChange,
                        label = { Text(BanglaFormatters.tr("খরচের পরিমাণ (৳)", "Payment Amount (৳)"), fontSize = 12.sp) },
                        placeholder = { Text(BanglaFormatters.tr("যেমন: ১২০০", "e.g. 1200"), fontSize = 13.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dbbl_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DbblBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val presets = listOf(100, 500, 1000, 2000, 5000)
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
                                    .clickable { onBillPresetClick(amt) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "৳${BanglaFormatters.toBanglaDigits(amt)}",
                                    color = Color(0xFF93C5FD),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = workflow.noteInput,
                        onValueChange = onNoteChange,
                        label = { Text(BanglaFormatters.tr("বিবরণ / মার্চেন্ট নাম (ঐচ্ছিক)", "Merchant / Memo (Optional)"), fontSize = 12.sp) },
                        placeholder = { Text(BanglaFormatters.tr("যেমন: স্বপ্ন সুপারশপ POS, আড়ং", "e.g. Shwapno POS, Aarong"), fontSize = 12.sp) },
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

                    val rem = state.walletConfig.dbblBalance - workflow.billAmount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DbblBlue.copy(alpha = 0.14f))
                            .border(1.dp, DbblBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = BanglaFormatters.tr("পেমেন্টের পর অবশিষ্ট ডিবিবিএল ব্যালেন্স:", "Remaining DBBL Balance:"),
                            color = Color(0xFF93C5FD),
                            fontSize = 12.sp
                        )
                        Text(
                            text = BanglaFormatters.formatBDT(rem),
                            color = if (rem < 0) NeonPink else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    workflow.bkashError?.let { err ->
                        Text(
                            text = err,
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DbblBlue)
                            .clickable { onConfirmDbblSpend() }
                            .padding(vertical = 14.dp)
                            .testTag("confirm_dbbl_spend_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("✓ ডিবিবিএল পেমেন্ট সম্পন্ন করুন", "✓ Confirm DBBL Card Payment"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                ExpenseStep.STEP_2B_TENDER -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BanglaFormatters.tr("← মাধ্যম পরিবর্তন", "← Change Method"),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onGoToStep(ExpenseStep.STEP_1_METHOD) }
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonMint.copy(alpha = 0.18f))
                                .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.tr("নগদ নোট পেমেন্ট (ট্যাপ করলেই +১)", "Tap Note to Add +1"),
                                color = NeonMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = workflow.billAmountInput,
                            onValueChange = onBillAmountChange,
                            label = { Text(BanglaFormatters.tr("খরচের পরিমাণ (৳)", "Expenditure (৳)"), fontSize = 11.sp) },
                            placeholder = { Text(BanglaFormatters.tr("নোট চাপুন বা লিখুন", "Tap notes or type"), fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cash_bill_amount_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonMint,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = workflow.noteInput,
                            onValueChange = onNoteChange,
                            label = { Text(BanglaFormatters.tr("বিবরণ (ঐচ্ছিক)", "Memo"), fontSize = 11.sp) },
                            placeholder = { Text(BanglaFormatters.tr("রিকশা / বাজার", "Market / Auto"), fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Header + Smart Auto-Select / Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = BanglaFormatters.tr(
                                    "রঙিন নোটে ক্লিক করলে ১টি নোট ও খরচের টাকা যোগ হবে",
                                    "Click color-coded note to add +1 & add to expenditure"
                                ),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = BanglaFormatters.tr(
                                    "কমাতে ডানপাশের (−) বাটন চাপুন • ভাঙতি লাগলে খরচের ঘর এডিট করুন",
                                    "Tap (−) to remove • Edit Expenditure field if you need change"
                                ),
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable { onAutoSelectTender() }
                                    .padding(horizontal = 7.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = BanglaFormatters.tr("✨ অটো", "✨ Auto"),
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onClearTender() }
                                    .padding(horizontal = 7.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = BanglaFormatters.tr("রিসেট", "Reset"),
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Compact 2×5 Grid of Color-Coded Clickable Notes with Banknote Images & Minus-Only Button
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
                                    val inVault = state.notes[denom] ?: 0
                                    val selectedCnt = workflow.tenderNotes[denom] ?: 0
                                    val canAddMore = selectedCnt < inVault
                                    PaymentClickableNoteCard(
                                        denom = denom,
                                        selectedCount = selectedCnt,
                                        subLabel = BanglaFormatters.tr(
                                            "ভল্ট: ${BanglaFormatters.toBanglaDigits(inVault)}টি",
                                            "Vault: ${BanglaFormatters.toBanglaDigits(inVault)}"
                                        ),
                                        canAdd = canAddMore,
                                        accentHighlight = NeonCyan,
                                        testTagPrefix = "tender_note",
                                        modifier = Modifier.weight(1f),
                                        onAddOne = { onAdjustTenderNote(denom, 1) },
                                        onMinusOne = { onAdjustTenderNote(denom, -1) }
                                    )
                                }
                            }
                        }
                    }

                    // Summary Bar
                    val isExactMatch = workflow.billAmount > 0 && workflow.tenderTotal == workflow.billAmount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianDark)
                            .border(
                                1.dp,
                                if (isExactMatch) NeonMint.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = BanglaFormatters.tr("খরচ / বিল:", "Expenditure:"),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = BanglaFormatters.formatBDT(workflow.billAmount),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = BanglaFormatters.tr("দেওয়া নোট:", "Given Notes:"),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = BanglaFormatters.formatBDT(workflow.tenderTotal),
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isExactMatch) BanglaFormatters.tr("ভাঙতি ফেরত:", "Change:")
                                else BanglaFormatters.tr("পাওনা ভাঙতি:", "Expected Change:"),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (isExactMatch) BanglaFormatters.tr("প্রয়োজন নেই ✓", "Exact ✓")
                                else BanglaFormatters.formatBDT(workflow.expectedChange),
                                color = NeonMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isExactMatch) {
                                    Brush.horizontalGradient(listOf(NeonMint, NeonCyan))
                                } else {
                                    Brush.horizontalGradient(listOf(NeonCyan, NeonMint))
                                }
                            )
                            .clickable { onProceedToChangeStep() }
                            .padding(vertical = 13.dp)
                            .testTag("proceed_to_change_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isExactMatch) {
                                BanglaFormatters.tr(
                                    "✓ পেমেন্ট সম্পন্ন করুন (ভাঙতি ফেরত নেই)",
                                    "✓ Complete Payment Directly (No Change)"
                                )
                            } else {
                                BanglaFormatters.tr(
                                    "পরবর্তী ধাপ: ফেরত পাওয়া ভাঙতি নোট (${BanglaFormatters.formatBDT(workflow.expectedChange)}) ➔",
                                    "Next: Select Return Change (${BanglaFormatters.formatBDT(workflow.expectedChange)}) ➔"
                                )
                            },
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                ExpenseStep.STEP_2B_CHANGE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BanglaFormatters.tr("← প্রদত্ত নোট পরিবর্তন", "← Edit Given Notes"),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onGoToStep(ExpenseStep.STEP_2B_TENDER) }
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonCyan.copy(alpha = 0.18f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.tr("ফেরত পাওয়া ভাঙতি নোট", "Return Change Notes"),
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3 Summary Mini-Boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ObsidianDark)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = BanglaFormatters.tr("মোট বিল", "Bill"), color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = BanglaFormatters.formatBDT(workflow.billAmount),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ObsidianDark)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = BanglaFormatters.tr("দেওয়া নোট", "Given"), color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = BanglaFormatters.formatBDT(workflow.tenderTotal),
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonMint.copy(alpha = 0.12f))
                                .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = BanglaFormatters.tr("পাওনা ভাঙতি", "Expected"), color = Color(0xFFA7F3D0), fontSize = 10.sp)
                            Text(
                                text = BanglaFormatters.formatBDT(workflow.expectedChange),
                                color = NeonMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BanglaFormatters.tr(
                                "ফেরত পাওয়া নোটে ক্লিক করুন (+১ যোগ হবে)",
                                "Tap received change notes (+1 per tap)"
                            ),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonMint.copy(alpha = 0.15f))
                                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable { onAutoFillChange() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = BanglaFormatters.tr("✨ অটো ভাঙতি", "✨ Auto Fill"),
                                    color = NeonMint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onClearReceived() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = BanglaFormatters.tr("শূন্য করুন", "Clear"),
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Compact 2×5 Grid of Color-Coded Clickable Change Notes with Minus-Only Button
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
                                    val selectedCnt = workflow.receivedNotes[denom] ?: 0
                                    PaymentClickableNoteCard(
                                        denom = denom,
                                        selectedCount = selectedCnt,
                                        subLabel = BanglaFormatters.tr(
                                            "ফেরত: ${BanglaFormatters.toBanglaDigits(selectedCnt)}টি",
                                            "Got: ${BanglaFormatters.toBanglaDigits(selectedCnt)}"
                                        ),
                                        canAdd = true,
                                        accentHighlight = NeonMint,
                                        testTagPrefix = "received_note",
                                        modifier = Modifier.weight(1f),
                                        onAddOne = { onAdjustReceivedNote(denom, 1) },
                                        onMinusOne = { onAdjustReceivedNote(denom, -1) }
                                    )
                                }
                            }
                        }
                    }

                    // Discrepancy Banner
                    val diff = workflow.discrepancy
                    val bannerColor = when {
                        diff == 0 -> NeonMint
                        diff < 0 -> NeonPink
                        else -> AmberGold
                    }
                    val bannerText = when {
                        diff == 0 -> BanglaFormatters.tr(
                            "✓ ভাঙতির হিসাব সম্পূর্ণ মিলে গেছে! (${BanglaFormatters.formatBDT(workflow.receivedTotal)})",
                            "✓ Change matches perfectly! (${BanglaFormatters.formatBDT(workflow.receivedTotal)})"
                        )
                        diff < 0 -> BanglaFormatters.tr(
                            "⚠️ দোকানদারের কাছে আপনার পাওনা (বাকি): ${BanglaFormatters.formatBDT(abs(diff))}",
                            "⚠️ Shopkeeper owes you: ${BanglaFormatters.formatBDT(abs(diff))}"
                        )
                        else -> BanglaFormatters.tr(
                            "⚡ অতিরিক্ত ফেরত (আপনার দেনা): ${BanglaFormatters.formatBDT(diff)}",
                            "⚡ Extra change received: ${BanglaFormatters.formatBDT(diff)}"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(bannerColor.copy(alpha = 0.14f))
                            .border(1.dp, bannerColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = bannerText,
                            color = bannerColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateSurface)
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { onGoToStep(ExpenseStep.STEP_2B_CONFIRM) }
                                .padding(vertical = 13.dp)
                                .testTag("proceed_to_confirm_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = BanglaFormatters.tr("সারসংক্ষেপ দেখুন", "Review Summary"),
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(NeonMint, NeonCyan)
                                    )
                                )
                                .clickable { onCommitCashExpense() }
                                .padding(vertical = 13.dp)
                                .testTag("commit_cash_expense_direct_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = BanglaFormatters.tr("✓ পেমেন্ট সম্পন্ন করুন", "✓ Complete Payment"),
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                ExpenseStep.STEP_2B_CONFIRM -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BanglaFormatters.tr("← প্রদত্ত নোট সংশোধন", "← Edit Notes"),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                if (workflow.expectedChange == 0) {
                                    onGoToStep(ExpenseStep.STEP_2B_TENDER)
                                } else {
                                    onGoToStep(ExpenseStep.STEP_2B_CHANGE)
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonMint.copy(alpha = 0.18f))
                                .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.tr("চূড়ান্ত নিশ্চিতকরণ", "Final Confirmation"),
                                color = NeonMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val cat = state.categories.find { it.id == workflow.categoryId }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianDark)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryRow(BanglaFormatters.tr("খরচের খাত:", "Category:"), "${cat?.icon ?: "📦"} ${cat?.name ?: ""}", Color.White)
                        SummaryRow(BanglaFormatters.tr("প্রকৃত বিল:", "Bill Amount:"), BanglaFormatters.formatBDT(workflow.billAmount), NeonPink)
                        SummaryRow(
                            BanglaFormatters.tr("ভল্ট থেকে দেওয়া নোট (-):", "Given Notes (-):"),
                            "${BanglaFormatters.formatBDT(workflow.tenderTotal)} (${BanglaFormatters.summarizeNotesInline(workflow.tenderNotes)})",
                            NeonCyan
                        )
                        if (workflow.expectedChange > 0) {
                            SummaryRow(
                                BanglaFormatters.tr("ভল্টে ফেরত পাওয়া ভাঙতি (+):", "Received Change (+):"),
                                "${BanglaFormatters.formatBDT(workflow.receivedTotal)} (${BanglaFormatters.summarizeNotesInline(workflow.receivedNotes)})",
                                NeonMint
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonMint)
                            .clickable { onCommitCashExpense() }
                            .padding(vertical = 14.dp)
                            .testTag("commit_cash_expense_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("✓ পেমেন্ট সম্পন্ন ও ভল্ট আপডেট করুন", "✓ Complete Payment & Update Vault"),
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.42f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}
