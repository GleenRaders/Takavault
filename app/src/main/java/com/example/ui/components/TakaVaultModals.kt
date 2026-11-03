package com.example.ui.components

import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.viewmodel.BkashBridgeState
import com.example.viewmodel.CalculatorState
import com.example.viewmodel.NoteExchangeState
import com.example.viewmodel.TakaVaultUiState
import kotlin.math.abs

// ============================================================================
// MODAL 2: ADD BKASH MONEY
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
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
                .background(Color(0xFF240B18))
                .border(1.dp, BkashPink.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    BkashOrigamiLogo(modifier = Modifier.size(34.dp))
                    Text(
                        text = "বিকাশ ব্যালেন্সে টাকা যোগ করুন",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("জমা টাকার পরিমাণ (৳) *", fontSize = 12.sp) },
                placeholder = { Text("যেমন: ২০০০", fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("input_add_bkash_amount"),
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
                listOf(500, 1000, 2000, 5000).forEach { p ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, BkashPink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { amountInput = BanglaFormatters.toBanglaDigits(p) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "+${BanglaFormatters.formatBDT(p)}",
                            color = Color(0xFFFCE7F3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = sourceInput,
                onValueChange = { sourceInput = it },
                label = { Text("উৎস বা বিবরণ (ঐচ্ছিক)", fontSize = 12.sp) },
                placeholder = { Text("যেমন: ক্যাশ ইন / বেতন / সেন্ড মানি প্রাপ্তি", fontSize = 12.sp) },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "বাতিল", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = { onConfirm(amountInput, sourceInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BkashPink,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_confirm_add_bkash")
                ) {
                    Text(text = "ব্যালেন্স যুক্ত করুন ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// MODAL 3: ADD CASH TO VAULT
// ============================================================================
@Composable
fun AddCashVaultDialog(
    tempNotes: Map<Int, Int>?,
    onDismiss: () -> Unit,
    onAdjustNote: (Int, Int) -> Unit,
    onConfirm: (String) -> Unit
) {
    if (tempNotes == null) return
    var noteInput by remember { mutableStateOf("") }
    val totalAdd = BanglaFormatters.calculateNotesTotal(tempNotes)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0A181C))
                .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ভাঙতি ভল্ট ডিপোজিট", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "ভল্টে নতুন ভাঙতি/নগদ নোট যুক্ত করুন", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pairs = BanglaFormatters.DENOMINATIONS.chunked(2)
                pairs.forEach { rowDenoms ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowDenoms.forEach { denom ->
                            val cnt = tempNotes[denom] ?: 0
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(
                                        1.dp,
                                        if (cnt > 0) NeonMint else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BangladeshiBanknoteBanner(
                                    denom = denom,
                                    count = cnt,
                                    height = 38.dp,
                                    compactMode = true
                                )
                                Text(
                                    text = "+${BanglaFormatters.toBanglaDigits(cnt)}টি = ${BanglaFormatters.formatBDT(cnt * denom)}",
                                    color = if (cnt > 0) NeonMint else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .clickable(enabled = cnt > 0) { onAdjustNote(denom, -1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "−", color = if (cnt > 0) NeonPink else TextMuted, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .clickable { onAdjustNote(denom, 1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "＋", color = NeonMint, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("টাকার উৎস (যেমন: এটিএম উত্তোলন)", fontSize = 11.sp, color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMint,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "মোট জমা:", color = TextSecondary, fontSize = 10.sp)
                    Text(
                        text = "+ ${BanglaFormatters.formatBDT(totalAdd)}",
                        color = NeonMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "বাতিল", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = { onConfirm(noteInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonMint,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "ভল্টে জমা করুন ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ============================================================================
// MODAL 4: ADD CUSTOM CATEGORY
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    if (!isOpen) return
    var nameInput by remember { mutableStateOf("") }
    var iconInput by remember { mutableStateOf("💊") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C1524))
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
                    text = "নতুন খরচের খাত যুক্ত করুন",
                    color = Color.White,
                    fontSize = 16.sp,
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
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("খাতের নাম (বাংলায়) *", fontSize = 12.sp) },
                placeholder = { Text("যেমন: ওষুধ ও চিকিৎসা", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = NeonCyan.copy(alpha = 0.4f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Text(text = "আইকন / ইমোজি নির্বাচন করুন:", color = TextSecondary, fontSize = 12.sp)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BanknoteCatalog.EMOJI_CHOICES.forEach { emoji ->
                    val selected = iconInput == emoji
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) NeonCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                            .border(
                                1.dp,
                                if (selected) NeonCyan else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { iconInput = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "বাতিল", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = { onConfirm(nameInput, iconInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "খাত সংরক্ষণ করুন ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ============================================================================
// MODAL 5: SETTINGS, BUDGET, BACKUP & RESET
// ============================================================================
@Composable
fun SettingsAndBackupDialog(
    isOpen: Boolean,
    currentBudget: Int,
    onDismiss: () -> Unit,
    onSaveBudget: (String) -> Unit,
    onExportJsonClick: () -> Unit,
    onImportJsonClick: () -> Unit,
    onHardResetZero: () -> Unit,
    onReloadDemoData: () -> Unit
) {
    if (!isOpen) return
    var budgetInput by remember(currentBudget) {
        mutableStateOf(BanglaFormatters.toBanglaDigits(currentBudget))
    }
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SlateCardBg)
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "সিস্টেম কনফিগারেশন", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "উন্নত সেটিংস, বাজেট ও ডাটা ব্যবস্থাপনা",
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
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            // Budget Config
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurface)
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 মাসিক বাজেট লিমিট ও সতর্কতা সেটআপ",
                    color = Color.White,
                    fontSize = 13.sp,
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
                        placeholder = { Text("যেমন: ২৫০০০", fontSize = 12.sp) },
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
                    Button(
                        onClick = { onSaveBudget(budgetInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "আপডেট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Backup & Restore
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurface)
                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💾 ডাটা ব্যাকআপ ও রিস্টোর (JSON ফাইল)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonMint.copy(alpha = 0.2f))
                            .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .clickable { onExportJsonClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⬇️ ব্যাকআপ নিন (JSON)",
                            color = NeonMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .clickable { onImportJsonClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⬆️ রিস্টোর করুন",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Hard Reset & Demo Data
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonPink.copy(alpha = 0.1f))
                    .border(1.dp, NeonPink.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚠️ ডাটা রিসেট ও ডেমো ডাটা নিয়ন্ত্রণ",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (showResetConfirm) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, NeonPink, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "আপনি কি নিশ্চিতভাবে সকল ব্যালেন্স, ভাঙতি নোট ও লেনদেনের ইতিহাস মুছে শূন্য (০) করতে চান?",
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    showResetConfirm = false
                                    onHardResetZero()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonPink,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "হ্যাঁ, সব মুছে শূন্য করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable { showResetConfirm = false }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = "না, বাতিল", color = Color.White, fontSize = 11.sp)
                            }
                        }
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
                            .background(NeonPink.copy(alpha = 0.25f))
                            .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { showResetConfirm = true }
                            .padding(vertical = 10.dp)
                            .testTag("btn_hard_reset"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗑️ সব ডাটা রিসেট (০)",
                            color = Color(0xFFFCE7F3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .clickable { onReloadDemoData() }
                            .padding(vertical = 10.dp)
                            .testTag("btn_reload_demo"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔄 নমুনা ডাটা লোড",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODAL 6: DIGITAL RECEIPT GENERATOR
// ============================================================================
@Composable
fun ReceiptDialog(
    tx: TransactionEntity?,
    state: TakaVaultUiState,
    onDismiss: () -> Unit
) {
    if (tx == null) return
    val context = LocalContext.current
    val cat = state.categories.find { it.id == tx.categoryId }
    val methodLabel = if (tx.method == "bkash") "বিকাশ (bKash)" else "নগদ ক্যাশ (Cash)"
    val typeLabel = if (tx.type == "inflow") "তহবিল জমা (Inflow)" else "খরচ / পেমেন্ট"

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C1524))
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
                    text = "🧾 ডিজিটাল লেনদেন রসিদ",
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
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D1322))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "টাকাভল্ট ৪.০ সাইবার-ফিনটেক",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "স্মার্ট বিকাশ ও ভাঙতি নোট লেনদেন রসিদ",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "রসিদ নং: ${BanglaFormatters.toBanglaDigits(tx.id)}",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                ReceiptRow("তারিখ ও সময়:", BanglaFormatters.formatBanglaDateTime(tx.timestamp))
                ReceiptRow("লেনদেনের ধরন:", typeLabel)
                ReceiptRow("খাত:", "${cat?.icon ?: "🧾"} ${cat?.name ?: "লেনদেন"}")
                ReceiptRow("পেমেন্ট মাধ্যম:", methodLabel, valueColor = NeonCyan)
                ReceiptRow("বিবরণ:", tx.note.ifBlank { "—" })

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                ReceiptRow(
                    "মোট পরিমাণ:",
                    BanglaFormatters.formatBDT(tx.amount),
                    valueColor = NeonMint
                )

                if (tx.method == "cash" && tx.type == "expense") {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ReceiptRow("প্রদত্ত মোট টাকা:", BanglaFormatters.formatBDT(tx.tenderedTotal), valueColor = NeonCyan)
                    Text(
                        text = "দেওয়া নোট: ${BanglaFormatters.summarizeNotesInline(tx.tenderedNotes)}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    ReceiptRow("ফেরত পাওয়া ভাঙতি:", BanglaFormatters.formatBDT(tx.receivedTotal), valueColor = NeonMint)
                    Text(
                        text = "ফেরত নোট: ${BanglaFormatters.summarizeNotesInline(tx.receivedNotes)}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    if (tx.discrepancy != 0) {
                        Text(
                            text = "অসামঞ্জস্য/বকেয়া: ${BanglaFormatters.formatBDT(abs(tx.discrepancy))}",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Text(
                    text = "ধন্যবাদ — আপনার প্রতিটি ভাঙতি ও টাকার নোটের হিসাব সুরক্ষিত",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        val shareText = buildString {
                            appendLine("🧾 টাকাভল্ট ৪.০ — ডিজিটাল রসিদ")
                            appendLine("রসিদ নং: ${BanglaFormatters.toBanglaDigits(tx.id)}")
                            appendLine("সময়: ${BanglaFormatters.formatBanglaDateTime(tx.timestamp)}")
                            appendLine("খাত: ${cat?.name ?: "লেনদেন"}")
                            appendLine("মাধ্যম: $methodLabel")
                            appendLine("বিবরণ: ${tx.note}")
                            appendLine("মোট পরিমাণ: ${BanglaFormatters.formatBDT(tx.amount)}")
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "রসিদ শেয়ার করুন"))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "📤 রসিদ শেয়ার করুন", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ============================================================================
// MODAL 7: HD BANKNOTE PHOTO INSPECTOR & COUNT ADJUSTER (ভাঙতি নোট পপআপ)
// ============================================================================
@Composable
fun NotePreviewDialog(
    denom: Int?,
    count: Int,
    onDismiss: () -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    if (denom == null) return
    val style = BanknoteCatalog.styleFor(denom)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C1524))
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
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
                        text = "বাংলাদেশ ব্যাংক ভাঙতি নোট ইনভেন্টরি",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${style.label} (${style.tag})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .testTag("btn_close_note_popup"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            BangladeshiBanknoteBanner(
                denom = denom,
                count = count,
                height = 136.dp,
                compactMode = false,
                showZoomBadge = false
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(text = style.metaDescription, color = TextSecondary, fontSize = 11.sp)
            }

            // Prominent Plus / Minus Note Counter inside the Popup
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurface)
                    .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ভল্টে নোটের সংখ্যা পরিবর্তন করুন:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "মোট = ${BanglaFormatters.formatBDT(count * denom)}",
                        color = NeonMint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (count > 0) NeonPink.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                if (count > 0) NeonPink.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = count > 0) { onMinus() }
                            .testTag("popup_note_minus_$denom"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "− কমান",
                            color = if (count > 0) NeonPink else TextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${BanglaFormatters.toBanglaDigits(count)}টি",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonMint.copy(alpha = 0.22f))
                            .border(1.dp, NeonMint.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .clickable { onPlus() }
                            .testTag("popup_note_plus_$denom"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "＋ বাড়ান",
                            color = NeonMint,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODAL 8: BKASH <-> CASH VAULT BRIDGE (ক্যাশ-আউট ও ক্যাশ-ইন ইঞ্জিন)
// ============================================================================
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
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF240B18))
                .border(1.dp, BkashPink.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.mode == "cashout") "🏧 বিকাশ ক্যাশ-আউট (বিকাশ → ভাঙতি ভল্ট)"
                        else "📲 বিকাশ ক্যাশ-ইন (ভাঙতি ভল্ট → বিকাশ)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (state.mode == "cashout") "বিকাশ থেকে টাকা তুলে সরাসরি ভাঙতি ভল্টের নোটে যুক্ত করুন"
                        else "হাতের নগদ নোট এজেন্টকে দিয়ে বিকাশ ব্যালেন্সে টাকা ভরুন",
                        color = Color(0xFFFBCFE8),
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isOut = state.mode == "cashout"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOut) BkashPink else Color.Black.copy(alpha = 0.4f))
                        .clickable { onSwitchMode("cashout") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏧 ক্যাশ-আউট",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isOut) NeonCyan else Color.Black.copy(alpha = 0.4f))
                        .clickable { onSwitchMode("cashin") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📲 ক্যাশ-ইন",
                        color = if (!isOut) ObsidianBg else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedTextField(
                value = state.amountInput,
                onValueChange = onAmountChange,
                label = { Text("টাকার পরিমাণ (৳) *", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = BkashPink.copy(alpha = 0.6f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (state.mode == "cashout") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rates = listOf(1.85 to "রেগুলার (১.৮৫%)", 1.49 to "প্রিয়/ATM (১.৪৯%)", 0.0 to "চার্জ ছাড়া (০%)")
                    rates.forEach { (rate, label) ->
                        val active = state.feeRatePercent == rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) BkashPink.copy(alpha = 0.35f) else SlateSurface)
                                .border(
                                    1.dp,
                                    if (active) BkashPink else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onFeeRateChange(rate) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.mode == "cashout") "এজেন্ট আপনাকে কোন নোটগুলো দিয়েছেন?"
                        else "আপনি এজেন্টকে ভল্ট থেকে কোন নোটগুলো দিয়েছেন?",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonMint.copy(alpha = 0.2f))
                            .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .clickable { onAutoFillNotes() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "⚡ অটো নির্বাচন", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                BanglaFormatters.DENOMINATIONS.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { d ->
                            val cnt = state.notes[d] ?: 0
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SlateSurface)
                                    .border(
                                        1.dp,
                                        if (cnt > 0) NeonMint else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "৳${BanglaFormatters.toBanglaDigits(d)}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${BanglaFormatters.toBanglaDigits(cnt)}টি",
                                        color = NeonMint,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .clickable { onAdjustNote(d, -1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "−", color = NeonPink, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .clickable { onAdjustNote(d, 1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "＋", color = NeonMint, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (state.mode == "cashout") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "বিকাশ থেকে কাটবে (চার্জ ${BanglaFormatters.formatBDT(state.feeAmount)} সহ):",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = BanglaFormatters.formatBDT(state.notesTotal + state.feeAmount),
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ভাঙতি ভল্টে যোগ হবে:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "+ ${BanglaFormatters.formatBDT(state.notesTotal)}",
                            color = NeonMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ভাঙতি ভল্ট থেকে কমবে:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "- ${BanglaFormatters.formatBDT(state.notesTotal)}",
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "বিকাশ ব্যালেন্সে যোগ হবে:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "+ ${BanglaFormatters.formatBDT(state.notesTotal)}",
                            color = NeonMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "বাতিল", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = onConfirmTransfer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonMint,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "ট্রান্সফার নিশ্চিত করুন ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ============================================================================
// MODAL 9: NOTE BREAKER / EXCHANGER (বড় নোট ভেঙে খুচরা ভাঙতি করুন)
// ============================================================================
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
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C1524))
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "স্মার্ট ভাঙতি এক্সচেঞ্জার", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "🔄 বড় নোট ভেঙে খুচরা ভাঙতি করুন",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
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
                    Text(text = "✕", color = Color.White, fontSize = 13.sp)
                }
            }

            Text(
                text = "১. ভল্ট থেকে কোন বড় নোটটি ভাঙাতে চান?",
                color = NeonPink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1000, 500, 200, 100, 50).forEach { d ->
                    val cnt = vaultNotes[d] ?: 0
                    val active = state.sourceDenom == d
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) NeonPink.copy(alpha = 0.25f) else SlateSurface)
                            .border(
                                1.dp,
                                if (active) NeonPink else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = cnt > 0) { onSelectSource(d) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "৳${BanglaFormatters.toBanglaDigits(d)}",
                            color = if (cnt > 0) Color.White else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "আছে: ${BanglaFormatters.toBanglaDigits(cnt)}",
                            color = TextSecondary,
                            fontSize = 9.sp
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
                    text = "২. পরিবর্তে কোন কোন খুচরা ভাঙতি নোট পেয়েছেন?",
                    color = NeonMint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonMint.copy(alpha = 0.2f))
                        .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .clickable { onAutoBreak() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "⚡ অটো সাজেশন", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BanglaFormatters.DENOMINATIONS.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { d ->
                            val cnt = state.targetNotes[d] ?: 0
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SlateSurface)
                                    .border(
                                        1.dp,
                                        if (cnt > 0) NeonMint else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "৳${BanglaFormatters.toBanglaDigits(d)}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "+${BanglaFormatters.toBanglaDigits(cnt)}টি",
                                        color = NeonMint,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .clickable { onAdjustTarget(d, -1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "−", color = NeonPink, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .clickable { onAdjustTarget(d, 1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "＋", color = NeonMint, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val matches = state.targetTotal == state.sourceDenom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (matches) NeonMint.copy(alpha = 0.15f) else NeonPink.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        if (matches) NeonMint.copy(alpha = 0.45f) else NeonPink.copy(alpha = 0.45f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (matches) {
                        "✓ ১টি ৳${BanglaFormatters.toBanglaDigits(state.sourceDenom)} নোট = খুচরা ${BanglaFormatters.formatBDT(state.targetTotal)} (হিসাব সম্পূর্ণ মিলেছে)"
                    } else {
                        "সতর্কতা: ভাঙানো নোট (৳${BanglaFormatters.toBanglaDigits(state.sourceDenom)}) এবং খুচরা নোটের যোগফল (${BanglaFormatters.formatBDT(state.targetTotal)}) সমান হতে হবে"
                    },
                    color = if (matches) NeonMint else NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "বাতিল", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = onConfirmExchange,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "ভাঙতি সম্পন্ন করুন ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ============================================================================
// MODAL 10: BUILT-IN BENGALI QUICK CALCULATOR
// ============================================================================
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
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C1524))
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
                    text = "🧮 বাজারের কুইক ক্যালকুলেটর",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = BanglaFormatters.toBanglaDigits(state.expression.ifEmpty { "০" }),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = BanglaFormatters.formatBDT(state.lastResult),
                    color = NeonMint,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            val buttonRows = listOf(
                listOf("C" to "মুছুন", "DEL" to "⌫", "/" to "÷", "*" to "×"),
                listOf("7" to "৭", "8" to "৮", "9" to "৯", "-" to "−"),
                listOf("4" to "৪", "5" to "৫", "6" to "৬", "+" to "＋"),
                listOf("1" to "১", "2" to "২", "3" to "৩", "=" to "="),
                listOf("0" to "০", "." to ".")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                buttonRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (token, label) ->
                            val bg = when (token) {
                                "C" -> NeonPink.copy(alpha = 0.25f)
                                "=" -> NeonMint
                                "/", "*", "-", "+" -> NeonCyan.copy(alpha = 0.18f)
                                else -> SlateSurface
                            }
                            val fg = when (token) {
                                "C" -> NeonPink
                                "=" -> ObsidianBg
                                "/", "*", "-", "+" -> NeonCyan
                                else -> Color.White
                            }
                            Box(
                                modifier = Modifier
                                    .weight(if (token == "0") 2f else 1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                    .clickable { onInputToken(token) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = fg,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onUseResultForExpense,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BkashPink,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = "⚡ এই ফল দিয়ে সরাসরি খরচ এন্ট্রি করুন →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
