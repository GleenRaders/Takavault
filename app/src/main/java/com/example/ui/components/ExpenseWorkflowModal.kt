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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
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

    val selectedCat = state.categories.find { it.id == workflow.categoryId } ?: state.categories.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 720.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C1524))
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonPink.copy(alpha = 0.2f))
                            .border(1.dp, NeonPink.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedCat?.icon ?: "🛒", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "স্মার্ট খরচ ও ভাঙতি ট্র্যাকার",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedCat?.name ?: "খরচ"} — খরচ রেকর্ড করুন",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .testTag("btn_close_expense_modal"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Step Progress Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (workflow.step == ExpenseStep.STEP_1_METHOD || workflow.step == ExpenseStep.STEP_2A_BKASH) {
                    val is2A = workflow.step == ExpenseStep.STEP_2A_BKASH
                    StepPill(label = "১. পেমেন্ট মাধ্যম", active = !is2A, activeColor = NeonCyan)
                    Text(text = "→", color = TextSecondary, fontSize = 11.sp)
                    StepPill(label = "২. বিকাশ নিশ্চিতকরণ", active = is2A, activeColor = BkashPink, darkText = false)
                } else {
                    StepPill(
                        label = "১. বিল ও নোট",
                        active = workflow.step == ExpenseStep.STEP_2B_TENDER,
                        activeColor = NeonMint
                    )
                    Text(text = "→", color = TextSecondary, fontSize = 11.sp)
                    StepPill(
                        label = "২. ফেরত ভাঙতি",
                        active = workflow.step == ExpenseStep.STEP_2B_CHANGE,
                        activeColor = NeonMint
                    )
                    Text(text = "→", color = TextSecondary, fontSize = 11.sp)
                    StepPill(
                        label = "৩. নিশ্চিতকরণ",
                        active = workflow.step == ExpenseStep.STEP_2B_CONFIRM,
                        activeColor = NeonMint
                    )
                }
            }

            // Scrollable Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selector Chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "নির্বাচিত খরচের খাত:", color = TextSecondary, fontSize = 11.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.categories.forEach { cat ->
                            val isSelected = cat.id == workflow.categoryId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SlateSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onCategoryChange(cat.id) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${cat.icon} ${cat.name}",
                                    color = if (isSelected) NeonCyan else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                when (workflow.step) {
                    ExpenseStep.STEP_1_METHOD -> {
                        Text(
                            text = "টাকা কীভাবে পরিশোধ করেছেন?",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "পেমেন্টের মাধ্যম নির্বাচন করলে সংশ্লিষ্ট ব্যালেন্স ও ভাঙতি নোটের হিসাব খুলে যাবে",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        // bKash Option Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            BkashPink.copy(alpha = 0.25f),
                                            SlateCardBg
                                        )
                                    )
                                )
                                .border(1.dp, BkashPink.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable { onSelectMethod("bkash") }
                                .padding(14.dp)
                                .testTag("btn_select_method_bkash"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BkashOrigamiLogo(modifier = Modifier.size(40.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(BkashPink.copy(alpha = 0.3f))
                                        .border(1.dp, BkashPink.copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "ডিজিটাল ওয়ালেট",
                                        color = Color(0xFFFCE7F3),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "বিকাশ (bKash)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "সরাসরি বিকাশ ব্যালেন্স থেকে টাকা কেটে নেওয়া হবে",
                                color = Color(0xFFFBCFE8),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "বর্তমান ব্যালেন্স: ${BanglaFormatters.formatBDT(state.walletConfig.bkashBalance)}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Cash Option Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0A181C))
                                .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .clickable { onSelectMethod("cash") }
                                .padding(14.dp)
                                .testTag("btn_select_method_cash"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NeonMint.copy(alpha = 0.2f))
                                        .border(1.dp, NeonMint.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "💵", fontSize = 20.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(NeonMint.copy(alpha = 0.2f))
                                        .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "ভাঙতি নোট ইঞ্জিন",
                                        color = NeonMint,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "নগদ (Cash)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "দেওয়া নোট ও দোকানদারের ফেরত দেওয়া ভাঙতির পূর্ণাঙ্গ হিসাব",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "হাতে নগদ আছে: ${BanglaFormatters.formatBDT(state.cashTotal)}",
                                color = NeonMint,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    ExpenseStep.STEP_2A_BKASH -> {
                        val currentBal = state.walletConfig.bkashBalance
                        val spendAmt = workflow.billAmount
                        val afterBal = currentBal - spendAmt

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF240B18))
                                .border(1.dp, BkashPink.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "বিকাশ পেমেন্টের তথ্য দিন",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "উপলব্ধ: ${BanglaFormatters.formatBDT(currentBal)}",
                                    color = Color(0xFFFBCFE8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedTextField(
                                value = workflow.billAmountInput,
                                onValueChange = onBillAmountChange,
                                label = { Text("খরচের পরিমাণ (টাকা) *", fontSize = 12.sp) },
                                placeholder = { Text("যেমন: ৪৫০", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("input_bkash_spend_amount"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = BkashPink.copy(alpha = 0.6f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(50, 100, 200, 500, 1000).forEach { p ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.45f))
                                            .border(1.dp, BkashPink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { onBillPresetClick(p) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = BanglaFormatters.formatBDT(p),
                                            color = Color(0xFFFCE7F3),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = workflow.noteInput,
                                onValueChange = onNoteChange,
                                label = { Text("ট্রানজেকশন আইডি বা সংক্ষিপ্ত নোট (ঐচ্ছিক)", fontSize = 12.sp) },
                                placeholder = { Text("যেমন: স্বপ্ন সুপারশপ পেমেন্ট", fontSize = 12.sp) },
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

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "বর্তমান বিকাশ ব্যালেন্স:", color = TextSecondary, fontSize = 12.sp)
                                    Text(
                                        text = BanglaFormatters.formatBDT(currentBal),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "বাদ যাবে (খরচ):", color = NeonPink, fontSize = 12.sp)
                                    Text(
                                        text = "- ${BanglaFormatters.formatBDT(spendAmt)}",
                                        color = NeonPink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "পেমেন্ট পরবর্তী ব্যালেন্স:",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = BanglaFormatters.formatBDT(afterBal),
                                        color = if (afterBal < 0) NeonPink else NeonMint,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            if (workflow.bkashError != null || afterBal < 0) {
                                val msg = workflow.bkashError
                                    ?: "সতর্কতা: আপনার বিকাশ অ্যাকাউন্টে পর্যাপ্ত ব্যালেন্স নেই! (${BanglaFormatters.formatBDT(abs(afterBal))} কম আছে)"
                                Text(
                                    text = msg,
                                    color = NeonPink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onGoToStep(ExpenseStep.STEP_1_METHOD) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(text = "← পেছনে যান", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = onConfirmBkashSpend,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BkashPink,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_confirm_bkash_spend")
                            ) {
                                Text(text = "নিশ্চিত করুন ও পেমেন্ট সম্পন্ন করুন ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    ExpenseStep.STEP_2B_TENDER -> {
                        val bill = workflow.billAmount
                        val tenderTotal = workflow.tenderTotal

                        // Step 1: Total Bill Input
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SlateSurface)
                                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "ধাপ ১: মোট বিলের পরিমাণ", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "মোট কত টাকা খরচ বা বিল হয়েছে?", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "ভল্টে: ${BanglaFormatters.formatBDT(state.cashTotal)}",
                                    color = NeonMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedTextField(
                                value = workflow.billAmountInput,
                                onValueChange = onBillAmountChange,
                                label = { Text("খরচের পরিমাণ (৳) *", fontSize = 12.sp) },
                                placeholder = { Text("যেমন: ৩২০", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("input_cash_bill_amount"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = NeonCyan.copy(alpha = 0.45f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = workflow.noteInput,
                                onValueChange = onNoteChange,
                                label = { Text("বিবরণ বা দোকানের নাম (ঐচ্ছিক)", fontSize = 12.sp) },
                                placeholder = { Text("যেমন: কাঁচাবাজার বা রিকশা ভাড়া", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(20, 50, 120, 320, 650).forEach { p ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .clickable { onBillPresetClick(p) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = BanglaFormatters.formatBDT(p),
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Step 2: Tendered Notes Selection
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SlateSurface)
                                .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "ধাপ ২: প্রদত্ত নোট নির্বাচন", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "দোকানদারকে কোন কোন নোট দিয়েছেন?", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NeonCyan.copy(alpha = 0.18f))
                                            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                            .clickable { onAutoSelectTender() }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .testTag("btn_auto_select_tender")
                                    ) {
                                        Text(text = "✨ অটো নির্বাচন", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .clickable { onClearTender() }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = "মুছুন", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }

                            val pairs = BanglaFormatters.DENOMINATIONS.chunked(2)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pairs.forEach { rowDenoms ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowDenoms.forEach { denom ->
                                            val inVault = state.notes[denom] ?: 0
                                            val picked = workflow.tenderNotes[denom] ?: 0
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.Black.copy(alpha = if (inVault > 0) 0.55f else 0.25f))
                                                    .border(
                                                        1.dp,
                                                        if (picked > 0) NeonCyan else Color.White.copy(alpha = 0.1f),
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "৳${BanglaFormatters.toBanglaDigits(denom)}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "ভল্টে: ${BanglaFormatters.toBanglaDigits(inVault)}",
                                                        color = TextSecondary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Text(
                                                    text = "দেওয়া: ${BanglaFormatters.toBanglaDigits(picked)}টি (${BanglaFormatters.formatBDT(picked * denom)})",
                                                    color = if (picked > 0) NeonCyan else TextSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(32.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.White.copy(alpha = 0.06f))
                                                            .clickable(enabled = picked > 0) { onAdjustTenderNote(denom, -1) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "−",
                                                            color = if (picked > 0) NeonPink else TextMuted,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(32.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.White.copy(alpha = 0.06f))
                                                            .clickable(enabled = picked < inVault) { onAdjustTenderNote(denom, 1) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "＋",
                                                            color = if (picked < inVault) NeonCyan else TextMuted,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Tender Status Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "আপনার দেওয়া মোট টাকা: ${BanglaFormatters.formatBDT(tenderTotal)}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val msg = when {
                                        bill <= 0 -> "অনুগ্রহ করে প্রথমে মোট বিলের পরিমাণ লিখুন"
                                        tenderTotal < bill -> "আরও ${BanglaFormatters.formatBDT(bill - tenderTotal)} মূল্যের নোট নির্বাচন করুন"
                                        workflow.expectedChange == 0 -> "✓ সঠিক সমপরিমাণ নোট নির্বাচন করা হয়েছে"
                                        else -> "✓ দোকানদার আপনাকে ${BanglaFormatters.formatBDT(workflow.expectedChange)} ফেরত দেবেন"
                                    }
                                    val msgColor = when {
                                        bill <= 0 -> AmberGold
                                        tenderTotal < bill -> NeonPink
                                        else -> NeonMint
                                    }
                                    Text(text = msg, color = msgColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "ফেরত পাওয়ার কথা", color = TextSecondary, fontSize = 10.sp)
                                    Text(
                                        text = BanglaFormatters.formatBDT(workflow.expectedChange),
                                        color = NeonMint,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onGoToStep(ExpenseStep.STEP_1_METHOD) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(text = "← মাধ্যম পরিবর্তন", color = Color.White, fontSize = 12.sp)
                            }
                            Button(
                                onClick = onProceedToChangeStep,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = ObsidianBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_proceed_to_change")
                            ) {
                                Text(text = "পরবর্তী ধাপ: ফেরত ও ভাঙতি হিসাব →", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    ExpenseStep.STEP_2B_CHANGE -> {
                        val expected = workflow.expectedChange
                        val received = workflow.receivedTotal
                        val diff = workflow.discrepancy

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SlateSurface)
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (expected == 0) "দোকানদারের কোনো টাকা ফেরত দেওয়ার কথা নেই (৳০)"
                                else "দোকানদারের ফেরত দেওয়ার কথা: ${BanglaFormatters.formatBDT(expected)}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "প্রদত্ত নোট: ${BanglaFormatters.formatBDT(workflow.tenderTotal)} − বিল: ${BanglaFormatters.formatBDT(workflow.billAmount)} = প্রাপ্য ফেরত: ${BanglaFormatters.formatBDT(expected)}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonMint.copy(alpha = 0.2f))
                                        .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                        .clickable { onAutoFillChange() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("btn_auto_fill_change")
                                ) {
                                    Text(text = "⚡ সঠিক ভাঙতি অটো-পূরণ", color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable { onClearReceived() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "রিসেট", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SlateSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "দোকানদার আপনাকে কোন কোন ভাঙতি নোট ফেরত দিয়েছেন?",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            val pairs = BanglaFormatters.DENOMINATIONS.chunked(2)
                            pairs.forEach { rowDenoms ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowDenoms.forEach { denom ->
                                        val picked = workflow.receivedNotes[denom] ?: 0
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.Black.copy(alpha = 0.55f))
                                                .border(
                                                    1.dp,
                                                    if (picked > 0) NeonMint else Color.White.copy(alpha = 0.1f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .padding(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "৳${BanglaFormatters.toBanglaDigits(denom)}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "${BanglaFormatters.toBanglaDigits(picked)}টি",
                                                    color = NeonMint,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "ফেরত: ${BanglaFormatters.formatBDT(picked * denom)}",
                                                color = if (picked > 0) NeonMint else TextSecondary,
                                                fontSize = 10.sp
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(32.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.06f))
                                                        .clickable(enabled = picked > 0) { onAdjustReceivedNote(denom, -1) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "−",
                                                        color = if (picked > 0) NeonPink else TextMuted,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(32.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.06f))
                                                        .clickable { onAdjustReceivedNote(denom, 1) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "＋",
                                                        color = NeonMint,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Discrepancy Alert Box
                            val alertColor = when {
                                diff == 0 -> NeonMint
                                diff < 0 -> NeonPink
                                else -> AmberGold
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(alertColor.copy(alpha = 0.15f))
                                    .border(1.dp, alertColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                when {
                                    diff == 0 -> {
                                        Text(
                                            text = "✓ ফেরত ভাঙতি হিসাব শতভাগ মিলেছে!",
                                            color = NeonMint,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "প্রাপ্য ফেরত (${BanglaFormatters.formatBDT(expected)}) এবং প্রাপ্ত নোট (${BanglaFormatters.formatBDT(received)}) সমান।",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                    diff < 0 -> {
                                        Text(
                                            text = "⚠️ দোকানদার এখনো ${BanglaFormatters.formatBDT(abs(diff))} কম দিয়েছেন / বকেয়া!",
                                            color = NeonPink,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "নিশ্চিত করলে এটি স্বয়ংক্রিয়ভাবে 'বাকি ও পাওনা খাতায়' যুক্ত হবে।",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "⚠️ আপনি ${BanglaFormatters.formatBDT(diff)} বেশি পেয়েছেন! ফেরত দিতে হবে",
                                            color = AmberGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "নিশ্চিত করলে এটি আপনার দেনা হিসেবে 'বাকি খাতায়' যুক্ত হবে।",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onGoToStep(ExpenseStep.STEP_2B_TENDER) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(text = "← প্রদত্ত নোট পরিবর্তন", color = Color.White, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onGoToStep(ExpenseStep.STEP_2B_CONFIRM) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonMint,
                                    contentColor = ObsidianBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_go_to_final_confirm")
                            ) {
                                Text(text = "চূড়ান্ত পর্যালোচনা →", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    ExpenseStep.STEP_2B_CONFIRM -> {
                        val bill = workflow.billAmount
                        val tenderTotal = workflow.tenderTotal
                        val receivedTotal = workflow.receivedTotal
                        val diff = workflow.discrepancy
                        val netCashOutflow = tenderTotal - receivedTotal
                        val newVaultBal = state.cashTotal - netCashOutflow

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SlateSurface)
                                .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "ধাপ ৪: চূড়ান্ত নিশ্চিতকরণ — লেনদেনের সারসংক্ষেপ",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SummaryMiniStat(
                                    modifier = Modifier.weight(1f),
                                    label = "মোট খরচ",
                                    value = BanglaFormatters.formatBDT(bill),
                                    color = NeonPink
                                )
                                SummaryMiniStat(
                                    modifier = Modifier.weight(1f),
                                    label = "দেওয়া টাকা",
                                    value = BanglaFormatters.formatBDT(tenderTotal),
                                    color = NeonCyan
                                )
                                SummaryMiniStat(
                                    modifier = Modifier.weight(1f),
                                    label = "ফেরত ভাঙতি",
                                    value = BanglaFormatters.formatBDT(receivedTotal),
                                    color = NeonMint
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonPink.copy(alpha = 0.12f))
                                    .border(1.dp, NeonPink.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "📤 ভল্ট থেকে দেওয়া নোট:", color = Color(0xFFFBCFE8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = BanglaFormatters.summarizeNotesInline(workflow.tenderNotes),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonMint.copy(alpha = 0.12f))
                                    .border(1.dp, NeonMint.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "📥 ভল্টে ফেরত আসা ভাঙতি নোট:", color = Color(0xFFA7F3D0), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = BanglaFormatters.summarizeNotesInline(workflow.receivedNotes),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = when {
                                    diff == 0 -> "✓ ভাঙতি হিসাব সম্পূর্ণ সঠিক — কোনো বকেয়া বা অতিরিক্ত নেই।"
                                    diff < 0 -> "⚠️ দোকানদার এখনো ${BanglaFormatters.formatBDT(abs(diff))} কম দিয়েছেন — এটি বাকি খাতায় যোগ হবে।"
                                    else -> "⚠️ আপনি দোকানদারের কাছ থেকে ${BanglaFormatters.formatBDT(diff)} বেশি পেয়েছেন — এটি দেনা খাতায় যোগ হবে।"
                                },
                                color = when {
                                    diff == 0 -> NeonMint
                                    diff < 0 -> NeonPink
                                    else -> AmberGold
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "ভাঙতি ভল্টে প্রভাব:", color = TextSecondary, fontSize = 10.sp)
                                    Text(
                                        text = "- ${BanglaFormatters.formatBDT(netCashOutflow)}",
                                        color = NeonPink,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "নতুন ভল্ট ব্যালেন্স:", color = TextSecondary, fontSize = 10.sp)
                                    Text(
                                        text = BanglaFormatters.formatBDT(newVaultBal),
                                        color = NeonMint,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onGoToStep(ExpenseStep.STEP_2B_CHANGE) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(text = "← ভাঙতি সংশোধন", color = Color.White, fontSize = 12.sp)
                            }
                            Button(
                                onClick = onCommitCashExpense,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonMint,
                                    contentColor = ObsidianBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_final_confirm_cash_tx")
                            ) {
                                Text(text = "নিশ্চিত করুন ও ভল্ট আপডেট করুন ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepPill(
    label: String,
    active: Boolean,
    activeColor: Color,
    darkText: Boolean = true
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) activeColor else Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = if (active) (if (darkText) ObsidianBg else Color.White) else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SummaryMiniStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}
