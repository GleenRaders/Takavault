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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BanknoteCatalog
import com.example.data.CategoryEntity
import com.example.data.DebtEntity
import com.example.data.TempMoneyEntity
import com.example.data.TransactionEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BkashPink
import com.example.ui.theme.BkashPinkDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BanglaFormatters
import com.example.viewmodel.AppTab
import com.example.viewmodel.TakaVaultUiState
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun HomeTabContent(
    state: TakaVaultUiState,
    onOpenSettings: () -> Unit,
    onToggleBkashVisibility: () -> Unit,
    onOpenAddBkash: () -> Unit,
    onOpenExpenseModal: (categoryId: String?, method: String?) -> Unit,
    onOpenBkashBridge: (mode: String) -> Unit,
    onOpenNoteExchange: () -> Unit,
    onOpenAddCash: () -> Unit,
    onOpenNotePreview: (Int) -> Unit,
    onSwitchTab: (AppTab) -> Unit,
    onOpenAddCategory: () -> Unit,
    onOpenReceipt: (TransactionEntity) -> Unit,
    onSettleDebt: (DebtEntity) -> Unit,
    onAddTempMoney: (String, String, String) -> Unit,
    onToggleTempMoney: (TempMoneyEntity) -> Unit,
    onDeleteTempMoney: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BudgetAlertBar(state = state, onEditBudget = onOpenSettings)

        BkashWalletCard(
            state = state,
            onToggleVisibility = onToggleBkashVisibility,
            onAddMoney = onOpenAddBkash,
            onDirectBkashSpend = { onOpenExpenseModal(null, "bkash") },
            onCashOut = { onOpenBkashBridge("cashout") },
            onCashIn = { onOpenBkashBridge("cashin") }
        )

        BhangtiQuickVaultCard(
            state = state,
            onOpenNoteExchange = onOpenNoteExchange,
            onOpenAddCash = onOpenAddCash,
            onOpenNotePreview = onOpenNotePreview
        )

        QuickCategoriesCard(
            state = state,
            onCategoryClick = { catId -> onOpenExpenseModal(catId, null) },
            onAddCategory = onOpenAddCategory,
            onViewAllCategories = { onSwitchTab(AppTab.CATEGORIES) }
        )

        RecentTransactionsAndDebtSummary(
            state = state,
            onViewAllHistory = { onSwitchTab(AppTab.HISTORY) },
            onOpenReceipt = onOpenReceipt,
            onViewDebtLedger = { onSwitchTab(AppTab.TOOLS) },
            onSettleDebt = onSettleDebt
        )

        TemporaryMoneySection(
            state = state,
            onAddTempMoney = onAddTempMoney,
            onToggleTempMoney = onToggleTempMoney,
            onDeleteTempMoney = onDeleteTempMoney
        )
    }
}

@Composable
fun BudgetAlertBar(
    state: TakaVaultUiState,
    onEditBudget: () -> Unit
) {
    val cal = Calendar.getInstance()
    val curMonth = cal.get(Calendar.MONTH)
    val curYear = cal.get(Calendar.YEAR)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysLeft = max(1, daysInMonth - cal.get(Calendar.DAY_OF_MONTH) + 1)

    val monthlySpent = remember(state.transactions, curMonth, curYear) {
        state.transactions.filter { tx ->
            if (tx.type != "expense") return@filter false
            val c = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear
        }.sumOf { it.amount }
    }

    val budget = max(1, state.walletConfig.monthlyBudget)
    val rawPct = Math.round((monthlySpent.toDouble() / budget.toDouble()) * 100).toInt()
    val progress = min(1f, monthlySpent.toFloat() / budget.toFloat())
    val remaining = budget - monthlySpent
    val dailySafe = max(0, Math.round(remaining.toDouble() / daysLeft.toDouble()).toInt())

    val statusColor = when {
        rawPct >= 100 -> NeonPink
        rawPct >= 80 -> AmberGold
        else -> NeonMint
    }
    val statusText = when {
        rawPct >= 100 -> "⚠️ বাজেট অতিক্রম!"
        rawPct >= 80 -> "⚡ সতর্কতা (৮০%+)"
        else -> "✓ নিরাপদ বাজেট"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlateCardBg.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "বাজেট: ${BanglaFormatters.formatBDT(budget)} (${BanglaFormatters.toBanglaDigits(rawPct)}%)",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .clickable { onEditBudget() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("btn_edit_budget")
            ) {
                Text(
                    text = "লিমিট পরিবর্তন",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "খরচ: ${BanglaFormatters.formatBDT(monthlySpent)} • অবশিষ্ট: ${if (remaining >= 0) BanglaFormatters.formatBDT(remaining) else "-${BanglaFormatters.formatBDT(abs(remaining))}"}",
                color = if (remaining >= 0) NeonCyan else NeonPink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "দৈনিক সীমা: ${BanglaFormatters.formatBDT(dailySafe)}/দিন",
                color = NeonMint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50)),
            color = statusColor,
            trackColor = SlateSurface
        )
    }
}

@Composable
fun BkashWalletCard(
    state: TakaVaultUiState,
    onToggleVisibility: () -> Unit,
    onAddMoney: () -> Unit,
    onDirectBkashSpend: () -> Unit,
    onCashOut: () -> Unit,
    onCashIn: () -> Unit
) {
    val bkashMonthlySpent = remember(state.transactions) {
        state.transactions.filter { it.method == "bkash" && it.type == "expense" }.sumOf { it.amount }
    }
    val lastBkashTx = remember(state.transactions) {
        state.transactions.firstOrNull { it.method == "bkash" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BkashPink.copy(alpha = 0.25f),
                        SlateCardBg.copy(alpha = 0.94f),
                        BkashPinkDark.copy(alpha = 0.30f)
                    )
                )
            )
            .border(1.dp, BkashPink.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                BkashOrigamiLogo(modifier = Modifier.size(36.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "বিকাশ ওয়ালেট",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BkashPink)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "bKash",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "ট্যাপ করে ব্যালেন্স ও দ্রুত লেনদেন",
                        color = Color(0xFFFBCFE8).copy(alpha = 0.85f),
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, BkashPink.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .clickable { onToggleVisibility() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("btn_toggle_bkash_balance")
            ) {
                Text(
                    text = if (state.walletConfig.bkashVisible) "👁️ লুকান" else "🔒 দেখুন",
                    color = Color(0xFFFBCFE8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Tap-for-balance box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.dp, BkashPink.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .clickable { onToggleVisibility() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("bkash_balance_tap_box"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.walletConfig.bkashVisible) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = BanglaFormatters.formatBDT(state.walletConfig.bkashBalance),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "উপলব্ধ",
                        color = Color(0xFFF9A8D4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BkashPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "৳",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "ব্যালেন্স দেখতে ট্যাপ করুন",
                        color = Color(0xFFFCE7F3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "এ মাসে খরচ: ${BanglaFormatters.formatBDT(bkashMonthlySpent)}",
                    color = NeonPink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lastBkashTx?.let { "${BanglaFormatters.formatBDT(it.amount)} (${it.note})" }
                        ?: "কোনো লেনদেন নেই",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 4-Action Super Dock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BkashDockButton(
                modifier = Modifier.weight(1f).testTag("btn_add_bkash"),
                icon = "＋",
                label = "টাকা যোগ",
                bgColor = BkashPink,
                borderColor = BkashPink,
                textColor = Color.White,
                onClick = onAddMoney
            )
            BkashDockButton(
                modifier = Modifier.weight(1f).testTag("btn_spend_bkash"),
                icon = "💸",
                label = "পেমেন্ট",
                bgColor = Color.Black.copy(alpha = 0.45f),
                borderColor = BkashPink.copy(alpha = 0.45f),
                textColor = Color(0xFFFCE7F3),
                onClick = onDirectBkashSpend
            )
            BkashDockButton(
                modifier = Modifier.weight(1f).testTag("btn_cashout_bkash"),
                icon = "🏧",
                label = "ক্যাশ-আউট",
                bgColor = Color.Black.copy(alpha = 0.45f),
                borderColor = NeonMint.copy(alpha = 0.4f),
                textColor = NeonMint,
                onClick = onCashOut
            )
            BkashDockButton(
                modifier = Modifier.weight(1f).testTag("btn_cashin_bkash"),
                icon = "📲",
                label = "ক্যাশ-ইন",
                bgColor = Color.Black.copy(alpha = 0.45f),
                borderColor = NeonCyan.copy(alpha = 0.4f),
                textColor = NeonCyan,
                onClick = onCashIn
            )
        }
    }
}

@Composable
private fun BkashDockButton(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BhangtiQuickVaultCard(
    state: TakaVaultUiState,
    onOpenNoteExchange: () -> Unit,
    onOpenAddCash: () -> Unit,
    onOpenNotePreview: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A181C).copy(alpha = 0.94f))
            .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Header + Action Buttons
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonMint.copy(alpha = 0.15f))
                        .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💵", fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = "ভাঙতি ও নগদ ভল্ট",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "মোট ${BanglaFormatters.toBanglaDigits(state.totalNoteCount)}টি নোট • নোটে ট্যাপ করে ＋/− করুন",
                        color = NeonMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "মোট ভাঙতি", color = TextSecondary, fontSize = 9.sp)
                Text(
                    text = BanglaFormatters.formatBDT(state.cashTotal),
                    color = NeonMint,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable { onOpenNoteExchange() }
                    .testTag("btn_open_note_breaker"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔄 ভাঙতি করুন",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonMint, Color(0xFF34D399))
                        )
                    )
                    .clickable { onOpenAddCash() }
                    .testTag("btn_open_add_cash"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "＋ নগদ টাকা যোগ",
                    color = ObsidianBg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Space-efficient 5-column x 2-row grid of all 10 denominations (tap note -> popup with + / -)
        val rowsOfFive = remember { BanglaFormatters.DENOMINATIONS.chunked(5) }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rowsOfFive.forEach { rowDenoms ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowDenoms.forEach { denom ->
                        val count = state.notes[denom] ?: 0
                        CompactNoteCell(
                            modifier = Modifier.weight(1f),
                            denom = denom,
                            count = count,
                            onClick = { onOpenNotePreview(denom) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactNoteCell(
    modifier: Modifier = Modifier,
    denom: Int,
    count: Int,
    onClick: () -> Unit
) {
    val style = BanknoteCatalog.styleFor(denom)
    val hasNote = count > 0

    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = if (hasNote) {
                        listOf(style.primaryColor, style.secondaryColor)
                    } else {
                        listOf(
                            style.primaryColor.copy(alpha = 0.38f),
                            style.secondaryColor.copy(alpha = 0.38f)
                        )
                    }
                )
            )
            .border(
                width = if (hasNote) 1.2.dp else 1.dp,
                color = if (hasNote) style.borderColor else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 5.dp)
            .testTag("home_note_card_$denom"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "৳${BanglaFormatters.toBanglaDigits(denom)}",
            color = if (hasNote) Color.White else Color.White.copy(alpha = 0.65f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            maxLines = 1,
            softWrap = false
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = if (hasNote) 0.68f else 0.45f))
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${BanglaFormatters.toBanglaDigits(count)}টি",
                color = if (hasNote) NeonMint else TextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun QuickCategoriesCard(
    state: TakaVaultUiState,
    onCategoryClick: (String) -> Unit,
    onAddCategory: () -> Unit,
    onViewAllCategories: () -> Unit
) {
    val catTotals = remember(state.transactions) {
        val map = mutableMapOf<String, Int>()
        state.transactions.forEach { tx ->
            if (tx.type == "expense") {
                map[tx.categoryId] = (map[tx.categoryId] ?: 0) + tx.amount
            }
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardBg.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ দ্রুত খরচের খাত",
                color = NeonPink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .clickable { onAddCategory() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "＋ নতুন খাত",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .clickable { onViewAllCategories() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "সব খাত →",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Space-efficient 4-column x 2-row grid for 8 quick categories
        val rows = remember(state.categories) { state.categories.take(8).chunked(4) }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { rowCats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowCats.forEach { cat ->
                        val spent = catTotals[cat.id] ?: 0
                        val catColor = BanknoteCatalog.parseHexColor(cat.colorHex)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlateSurface)
                                .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable { onCategoryClick(cat.id) }
                                .padding(horizontal = 6.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = cat.icon, fontSize = 16.sp)
                            Text(
                                text = cat.name,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = BanglaFormatters.formatBDT(spent),
                                color = if (spent > 0) NeonMint else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    repeat(4 - rowCats.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsAndDebtSummary(
    state: TakaVaultUiState,
    onViewAllHistory: () -> Unit,
    onOpenReceipt: (TransactionEntity) -> Unit,
    onViewDebtLedger: () -> Unit,
    onSettleDebt: (DebtEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Recent 3 Transactions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCardBg.copy(alpha = 0.9f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️ সাম্প্রতিক লেনদেন (সর্বশেষ ৩টি)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "সম্পূর্ণ ইতিহাস →",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onViewAllHistory() }
                )
            }

            val recent3 = state.transactions.take(3)
            if (recent3.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "কোনো লেনদেন নেই", color = TextSecondary, fontSize = 11.sp)
                }
            } else {
                recent3.forEach { tx ->
                    val isInflow = tx.type == "inflow"
                    val cat = state.categories.find { it.id == tx.categoryId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = cat?.icon ?: if (isInflow) "📥" else "📦", fontSize = 15.sp)
                            Column {
                                Text(
                                    text = cat?.name ?: if (isInflow) "তহবিল জমা" else "বিবিধ",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (tx.note.isNotBlank()) {
                                    Text(
                                        text = tx.note,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${if (isInflow) "+" else "−"}${BanglaFormatters.formatBDT(tx.amount)}",
                                color = if (isInflow) NeonMint else NeonPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onOpenReceipt(tx) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = "🧾", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Debt Summary Mini Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCardBg.copy(alpha = 0.9f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📒 বাকি ও পাওনা খাতা (সংক্ষেপ)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "খাতা খুলুন →",
                    color = NeonMint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onViewDebtLedger() }
                )
            }

            if (state.debts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ কারো কাছে কোনো ভাঙতি বা বাকি পাওনা নেই",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            } else {
                state.debts.take(2).forEach { d ->
                    val isRec = d.type == "receivable"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = d.person,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isRec) "আমি পাবো (পাওনা)" else "দিতে হবে (দেনা)",
                                color = if (isRec) NeonMint else NeonPink,
                                fontSize = 9.sp
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.formatBDT(d.amount),
                                color = if (isRec) NeonMint else NeonPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonMint.copy(alpha = 0.2f))
                                    .clickable { onSettleDebt(d) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "মিটিয়ে দিন",
                                    color = NeonMint,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemporaryMoneySection(
    state: TakaVaultUiState,
    onAddTempMoney: (String, String, String) -> Unit,
    onToggleTempMoney: (TempMoneyEntity) -> Unit,
    onDeleteTempMoney: (String) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var whereStored by remember { mutableStateOf("cash") }

    val activeItems = remember(state.tempMoney) { state.tempMoney.filter { !it.done } }
    val doneItems = remember(state.tempMoney) { state.tempMoney.filter { it.done }.take(5) }
    val totalTemp = remember(activeItems) { activeItems.sumOf { it.amount } }
    val inWalletTemp = remember(activeItems) {
        activeItems.filter { it.whereStored != "other" }.sumOf { it.amount }
    }
    val ownActualMoney = state.netWorth - inWalletTemp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardBg.copy(alpha = 0.92f))
            .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
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
                    text = "⏳ অস্থায়ী টাকা",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "অন্যের আমানত বা ফেরতযোগ্য টাকা — ভাঙতি/বিকাশ ব্যালেন্স বদলায় না",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmberGold.copy(alpha = 0.1f))
                    .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(text = "মোট অস্থায়ী (চলমান)", color = Color(0xFFFDE68A), fontSize = 9.sp)
                Text(
                    text = BanglaFormatters.formatBDT(totalTemp),
                    color = AmberGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonMint.copy(alpha = 0.1f))
                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(text = "নিজের আসল টাকা", color = TextSecondary, fontSize = 9.sp)
                Text(
                    text = BanglaFormatters.formatBDT(ownActualMoney),
                    color = NeonMint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Input form
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = { Text("কার/কিসের টাকা?", fontSize = 11.sp, color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1.4f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    placeholder = { Text("পরিমাণ ৳", fontSize = 11.sp, color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val whereOptions = listOf("cash" to "💵 নগদ", "bkash" to "📲 বিকাশ", "other" to "🏦 অন্যত্র")
                whereOptions.forEach { (key, label) ->
                    val selected = whereStored == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) AmberGold.copy(alpha = 0.2f) else SlateSurface)
                            .border(
                                1.dp,
                                if (selected) AmberGold else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { whereStored = key },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) AmberGold else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        onAddTempMoney(titleInput, amountInput, whereStored)
                        if (titleInput.isNotBlank() && BanglaFormatters.parseBanglaNumber(amountInput) > 0) {
                            titleInput = ""
                            amountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(text = "＋ যোগ", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
            }
        }

        val displayList = activeItems + doneItems
        if (displayList.isEmpty()) {
            Text(
                text = "কোনো অস্থায়ী টাকা নেই",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        } else {
            displayList.forEach { item ->
                val whLabel = when (item.whereStored) {
                    "cash" -> "💵 নগদ"
                    "bkash" -> "📲 বিকাশ"
                    else -> "🏦 অন্যত্র"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateSurface.copy(alpha = if (item.done) 0.5f else 0.9f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = if (item.done) TextSecondary else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = "$whLabel • ${BanglaFormatters.formatBanglaDateTime(item.timestamp)}",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = BanglaFormatters.formatBDT(item.amount),
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .clickable { onToggleTempMoney(item) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (item.done) "↩ চালু" else "✓ ফেরত/শেষ",
                            color = NeonMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { onDeleteTempMoney(item.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🗑", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 2: ⚡ খরচ ও খাত (EXPENSE CATEGORIES & RECURRING BILLS)
// ============================================================================
@Composable
fun CategoriesTabContent(
    state: TakaVaultUiState,
    onCategoryClick: (String) -> Unit,
    onAddCategory: () -> Unit,
    onQuickPresetExpense: (String, String, Int) -> Unit
) {
    val catTotals = remember(state.transactions) {
        val map = mutableMapOf<String, Int>()
        state.transactions.forEach { tx ->
            if (tx.type == "expense") {
                map[tx.categoryId] = (map[tx.categoryId] ?: 0) + tx.amount
            }
        }
        map
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateCardBg.copy(alpha = 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "◈ খরচের খাতসমূহ (ক্যাটাগরি নির্বাচন করুন)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "যে খাতে খরচ করেছেন তার ওপর ক্লিক করলেই স্মার্ট পেমেন্ট ও ভাঙতি নোটের হিসাব চালু হবে",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onAddCategory() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "＋ নতুন খাত",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val rows = remember(state.categories) { state.categories.chunked(2) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { cat ->
                            val spent = catTotals[cat.id] ?: 0
                            val catColor = BanknoteCatalog.parseHexColor(cat.colorHex)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SlateSurface)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .clickable { onCategoryClick(cat.id) }
                                    .padding(12.dp),
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
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(catColor.copy(alpha = 0.18f))
                                            .border(1.dp, catColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = cat.icon, fontSize = 18.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "খরচ যোগ ＋",
                                            color = NeonCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = cat.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "মোট ব্যয়: ${BanglaFormatters.formatBDT(spent)}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Quick Monthly Recurring Bills Preset Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0C1524).copy(alpha = 0.92f))
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🔔 নিয়মিত মাসিক বিল কুইক-পে (১-ট্যাপ পেমেন্ট)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "প্রতি মাসের নির্দিষ্ট বিলগুলো বারবার টাইপ না করে এক ক্লিকে পরিশোধ করুন",
                color = TextSecondary,
                fontSize = 11.sp
            )

            val presets = listOf(
                Triple("rent", "🏠 মাসিক বাড়ি ভাড়া", 12000),
                Triple("bills", "⚡ বিদ্যুৎ ও গ্যাস বিল", 1500),
                Triple("bills", "🌐 ওয়াইফাই ইন্টারনেট", 800),
                Triple("transport", "🛺 প্রতিদিনের অটো ভাড়া", 50)
            )
            presets.chunked(2).forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPresets.forEach { (catId, title, amt) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val cleanNote = title.substringAfter(" ").trim()
                                    onQuickPresetExpense(catId, cleanNote, amt)
                                }
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${BanglaFormatters.formatBDT(amt)} • দ্রুত এন্ট্রি →",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 4: 📒 বাকি ও টুলস (DEBT LEDGER, BKASH FEE CALC & SMART CHANGE ADVISOR)
// ============================================================================
@Composable
fun ToolsTabContent(
    state: TakaVaultUiState,
    onAddDebt: (String, String, String) -> Unit,
    onSettleDebt: (DebtEntity) -> Unit,
    onDeleteDebt: (String) -> Unit,
    onQuickFeeInputChange: (String) -> Unit,
    onOpenBkashBridge: (String) -> Unit,
    onOpenNoteExchange: () -> Unit
) {
    var debtType by remember { mutableStateOf("receivable") }
    var debtPerson by remember { mutableStateOf("") }
    var debtAmount by remember { mutableStateOf("") }

    val totalRec = remember(state.debts) {
        state.debts.filter { it.type == "receivable" }.sumOf { it.amount }
    }
    val totalPay = remember(state.debts) {
        state.debts.filter { it.type == "payable" }.sumOf { it.amount }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Debt & Due Tracker Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateCardBg.copy(alpha = 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "📒 দোকানদারের ভাঙতি ও ব্যক্তিগত বাকি/পাওনা খাতা",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "দোকানদারের কাছে ভাঙতি পাওনা থাকলে বা কারো সাথে দেনা-পাওনা থাকলে এখানে ট্র্যাক ও নিষ্পত্তি করুন",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonMint.copy(alpha = 0.15f))
                        .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "পাওনা: ${BanglaFormatters.formatBDT(totalRec)}",
                        color = NeonMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonPink.copy(alpha = 0.15f))
                        .border(1.dp, NeonPink.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "দেনা: ${BanglaFormatters.formatBDT(totalPay)}",
                        color = NeonPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Add New Debt Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val recSelected = debtType == "receivable"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (recSelected) NeonMint.copy(alpha = 0.2f) else SlateSurface)
                            .border(
                                1.dp,
                                if (recSelected) NeonMint else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { debtType = "receivable" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🟢 আমি পাবো (পাওনা)",
                            color = if (recSelected) NeonMint else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!recSelected) NeonPink.copy(alpha = 0.2f) else SlateSurface)
                            .border(
                                1.dp,
                                if (!recSelected) NeonPink else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { debtType = "payable" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔴 আমাকে দিতে হবে (দেনা)",
                            color = if (!recSelected) NeonPink else TextSecondary,
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
                        value = debtPerson,
                        onValueChange = { debtPerson = it },
                        placeholder = { Text("দোকানদার বা ব্যক্তির নাম...", fontSize = 12.sp, color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = debtAmount,
                        onValueChange = { debtAmount = it },
                        placeholder = { Text("টাকা (৳)", fontSize = 12.sp, color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.9f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Button(
                    onClick = {
                        onAddDebt(debtType, debtPerson, debtAmount)
                        if (debtPerson.isNotBlank() && BanglaFormatters.parseBanglaNumber(debtAmount) > 0) {
                            debtPerson = ""
                            debtAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(text = "যুক্ত করুন ＋", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }

            if (state.debts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "কোনো বকেয়া বা দেনা-পাওনার হিসাব নেই", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                state.debts.forEach { d ->
                    val isRec = d.type == "receivable"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurface)
                            .border(
                                1.dp,
                                if (isRec) NeonMint.copy(alpha = 0.35f) else NeonPink.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isRec) NeonMint.copy(alpha = 0.2f) else NeonPink.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isRec) "পাওনা (আমি পাবো)" else "দেনা (দিতে হবে)",
                                    color = if (isRec) NeonMint else NeonPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = d.person,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = BanglaFormatters.formatBanglaDateTime(d.timestamp),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = BanglaFormatters.formatBDT(d.amount),
                                color = if (isRec) NeonMint else NeonPink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonMint)
                                        .clickable { onSettleDebt(d) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "✓ নিষ্পত্তি",
                                        color = ObsidianBg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable { onDeleteDebt(d.id) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "✕", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // bKash Cash-Out Charge Calculator Card
        val calcAmt = BanglaFormatters.parseBanglaNumber(state.quickCalcBkashInput)
        val regFee = Math.round(calcAmt * 0.0185).toInt()
        val priyoFee = Math.round(calcAmt * 0.0149).toInt()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            BkashPink.copy(alpha = 0.22f),
                            SlateCardBg.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(1.dp, BkashPink.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧮 বিকাশ ক্যাশ-আউট চার্জ ক্যালকুলেটর",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BkashPink)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "লাইভ চার্জ",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedTextField(
                value = state.quickCalcBkashInput,
                onValueChange = onQuickFeeInputChange,
                placeholder = { Text("টাকার পরিমাণ লিখুন (যেমন: ১০০০)", color = TextSecondary, fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BkashPink,
                    unfocusedBorderColor = BkashPink.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, BkashPink.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(text = "রেগুলার এজেন্ট (১.৮৫%)", color = Color(0xFFFBCFE8), fontSize = 11.sp)
                    Text(
                        text = "চার্জ: ${BanglaFormatters.formatBDT(regFee)}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "মোট কাটবে: ${BanglaFormatters.formatBDT(calcAmt + regFee)}",
                        color = Color(0xFFF9A8D4),
                        fontSize = 11.sp
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(text = "প্রিয় এজেন্ট / ATM (১.৪৯%)", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                    Text(
                        text = "চার্জ: ${BanglaFormatters.formatBDT(priyoFee)}",
                        color = NeonMint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "মোট কাটবে: ${BanglaFormatters.formatBDT(calcAmt + priyoFee)}",
                        color = Color(0xFF6EE7B7),
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = { onOpenBkashBridge("cashout") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BkashPink,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = "🏧 এখনই বিকাশ থেকে ভাঙতি ভল্টে ক্যাশ-আউট করুন →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Smart Bhangti Advisor Card
        val smallCount = state.smallNoteCount
        val healthy = smallCount >= 8
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0C1524).copy(alpha = 0.92f))
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 স্মার্ট ভাঙতি উপদেষ্টা (রিকশা ও বাজার টিপস)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (healthy) NeonMint.copy(alpha = 0.2f) else AmberGold.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (healthy) "✓ ভাঙতি পর্যাপ্ত" else "⚠️ ভাঙতি সংকট",
                        color = if (healthy) NeonMint else AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = if (healthy) {
                    "আপনার ভল্টে বর্তমানে ${BanglaFormatters.toBanglaDigits(smallCount)}টি খুচরা ভাঙতি নোট (৳২০—৳১) আছে যা রিকশা ভাড়া বা দোকানের ভাঙতি দেওয়ার জন্য যথেষ্ট।"
                } else {
                    "আপনার কাছে খুচরা ভাঙতি নোট মাত্র ${BanglaFormatters.toBanglaDigits(smallCount)}টি আছে। রিকশা বা অটো ভাড়ায় ঝামেলা এড়াতে একটি ৳৫০০ বা ৳১০০ নোট ভাঙতি করে নিন।"
                },
                color = TextSecondary,
                fontSize = 12.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { onOpenNoteExchange() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔄 বড় নোট ভেঙে খুচরা ভাঙতি তৈরি করুন",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
