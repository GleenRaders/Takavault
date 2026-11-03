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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BanknoteCatalog
import com.example.data.DebtEntity
import com.example.data.TempMoneyEntity
import com.example.data.TransactionEntity
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
import com.example.viewmodel.AppTab
import com.example.viewmodel.TakaVaultUiState
import java.util.Calendar
import kotlin.math.ceil

@Composable
fun HomeTabContent(
    state: TakaVaultUiState,
    onOpenSettings: () -> Unit,
    onToggleBkashVisibility: () -> Unit,
    onOpenAddBkash: () -> Unit,
    onToggleDbblVisibility: () -> Unit,
    onOpenAddDbbl: () -> Unit,
    onOpenDbblBridge: (String) -> Unit,
    onOpenExpenseModal: (String?, String?) -> Unit,
    onOpenBkashBridge: (String) -> Unit,
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 1. Compact Budget Bar
        BudgetProgressCard(state = state, onEditBudgetClick = onOpenSettings)

        // 2. bKash Wallet Card
        BkashBalanceCard(
            state = state,
            onToggleVisibility = onToggleBkashVisibility,
            onAddMoneyClick = onOpenAddBkash,
            onSpendBkashClick = { onOpenExpenseModal(null, "bkash") },
            onCashOutClick = { onOpenBkashBridge("cashout") },
            onCashInClick = { onOpenBkashBridge("cashin") }
        )

        // 3. DBBL Bank & Nexus Card Section (toggled via Settings `showDbblSection`)
        if (state.walletConfig.showDbblSection) {
            DbblBankCard(
                state = state,
                onToggleVisibility = onToggleDbblVisibility,
                onDepositClick = onOpenAddDbbl,
                onCardPayClick = { onOpenExpenseModal(null, "dbbl") },
                onAtmWithdrawClick = { onOpenDbblBridge("atm_withdraw") },
                onBankBkashTransferClick = { onOpenDbblBridge("to_bkash") }
            )
        }

        // 4. Cash Bhangti Vault Card
        CashVaultSummaryCard(
            state = state,
            onAddNotesClick = onOpenAddCash,
            onExchangeNotesClick = onOpenNoteExchange
        )

        // 5. Compact 5×2 Banknote Grid (Tap any note to open popup with + / − buttons)
        HomeBanknotesGridCard(
            state = state,
            onOpenAddCash = onOpenAddCash,
            onOpenNoteExchange = onOpenNoteExchange,
            onOpenNotePreview = onOpenNotePreview
        )

        // 6. Compact Quick Spend Categories Strip
        HomeQuickCategoriesCard(
            state = state,
            onCategoryClick = { catId -> onOpenExpenseModal(catId, null) },
            onAddCategoryClick = onOpenAddCategory,
            onSeeAllClick = { onSwitchTab(AppTab.CATEGORIES) }
        )

        // 7. Temporary Money Tracker (shown when enabled in Settings)
        if (state.walletConfig.showTempMoneySection) {
            TempMoneyTrackerCard(
                items = state.tempMoney,
                onAddTempMoney = onAddTempMoney,
                onToggleTempMoney = onToggleTempMoney,
                onDeleteTempMoney = onDeleteTempMoney
            )
        }

        // 8. Recent Activity & Pending Change Snapshot
        HomeRecentSnapshotCard(
            state = state,
            onSeeAllHistory = { onSwitchTab(AppTab.HISTORY) },
            onSeeAllDebts = { onSwitchTab(AppTab.TOOLS) },
            onOpenReceipt = onOpenReceipt,
            onSettleDebt = onSettleDebt
        )
    }
}

@Composable
fun BudgetProgressCard(
    state: TakaVaultUiState,
    onEditBudgetClick: () -> Unit
) {
    val startOfMonth = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val monthSpent = remember(state.transactions, startOfMonth) {
        state.transactions
            .filter { it.type == "expense" && it.timestamp >= startOfMonth }
            .sumOf { it.amount }
    }

    val budget = state.walletConfig.monthlyBudget.coerceAtLeast(1)
    val pct = ((monthSpent.toDouble() / budget.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    val barColor = when {
        pct >= 90 -> NeonPink
        pct >= 75 -> AmberGold
        else -> NeonMint
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlateCardBg.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
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
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(barColor)
                )
                Text(
                    text = BanglaFormatters.tr("মাসিক বাজেট ট্র্যাকার", "Monthly Budget"),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = BanglaFormatters.tr("(এডিট)", "(Edit)"),
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onEditBudgetClick() }
                )
            }
            Text(
                text = "${BanglaFormatters.formatBDT(monthSpent)} / ${BanglaFormatters.formatBDT(budget)} (${BanglaFormatters.toBanglaDigits(pct)}%)",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = barColor,
            trackColor = Color.Black.copy(alpha = 0.45f)
        )
    }
}

@Composable
fun BkashBalanceCard(
    state: TakaVaultUiState,
    onToggleVisibility: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onSpendBkashClick: () -> Unit,
    onCashOutClick: () -> Unit,
    onCashInClick: () -> Unit
) {
    val visible = state.walletConfig.bkashVisible
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
            .border(1.dp, BkashPink.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(11.dp),
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
                BkashOrigamiLogo(modifier = Modifier.size(34.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.tr("বিকাশ ব্যালেন্স", "bKash Balance"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BkashPink)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "bKash",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = if (visible) BanglaFormatters.formatBDT(state.walletConfig.bkashBalance) else "৳ • • • • •",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clickable { onToggleVisibility() }
                            .testTag("bkash_balance_text")
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(BkashPink)
                        .clickable { onAddMoneyClick() }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("btn_add_bkash"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("+ যোগ", "+ Add"),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(9.dp))
                        .clickable { onSpendBkashClick() }
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("খরচ", "Spend"),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(BkashPink.copy(alpha = 0.18f))
                    .border(1.dp, BkashPink.copy(alpha = 0.4f), RoundedCornerShape(9.dp))
                    .clickable { onCashOutClick() }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("📲 ক্যাশ-আউট (বিকাশ ➔ ভল্ট)", "📲 Cash-Out (bKash ➔ Vault)"),
                    color = Color(0xFFFBCFE8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(NeonMint.copy(alpha = 0.14f))
                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                    .clickable { onCashInClick() }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("📥 ক্যাশ-ইন (ভল্ট ➔ বিকাশ)", "📥 Cash-In (Vault ➔ bKash)"),
                    color = NeonMint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DbblBankCard(
    state: TakaVaultUiState,
    onToggleVisibility: () -> Unit,
    onDepositClick: () -> Unit,
    onCardPayClick: () -> Unit,
    onAtmWithdrawClick: () -> Unit,
    onBankBkashTransferClick: () -> Unit
) {
    val visible = state.walletConfig.dbblVisible
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        DbblBlue.copy(alpha = 0.28f),
                        SlateCardBg
                    )
                )
            )
            .border(1.dp, DbblBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(11.dp)
            .testTag("dbbl_bank_card"),
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
                DbblBankLogo(modifier = Modifier.size(34.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.tr("ডিবিবিএল ব্যাংক ও কার্ড", "DBBL Bank & Card"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DbblBlue)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Nexus *${state.walletConfig.dbblAccountNo}",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = if (visible) BanglaFormatters.formatBDT(state.walletConfig.dbblBalance) else "৳ • • • • •",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clickable { onToggleVisibility() }
                            .testTag("dbbl_balance_text")
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(DbblBlue)
                        .clickable { onDepositClick() }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("btn_add_dbbl"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("+ জমা", "+ Deposit"),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(9.dp))
                        .clickable { onCardPayClick() }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("btn_spend_dbbl"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("💳 পেমেন্ট", "💳 Pay"),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(DbblBlue.copy(alpha = 0.2f))
                    .border(1.dp, DbblBlue.copy(alpha = 0.45f), RoundedCornerShape(9.dp))
                    .clickable { onAtmWithdrawClick() }
                    .padding(vertical = 6.dp)
                    .testTag("btn_dbbl_atm"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("🏧 এটিএম উত্তোলন (ব্যাংক ➔ ভল্ট)", "🏧 ATM Withdraw (Bank ➔ Vault)"),
                    color = Color(0xFFBFDBFE),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(BkashPink.copy(alpha = 0.16f))
                    .border(1.dp, BkashPink.copy(alpha = 0.4f), RoundedCornerShape(9.dp))
                    .clickable { onBankBkashTransferClick() }
                    .padding(vertical = 6.dp)
                    .testTag("btn_dbbl_bkash_transfer"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("📲 নেক্সাসপে (ব্যাংক ⇄ বিকাশ)", "📲 NexusPay (Bank ⇄ bKash)"),
                    color = Color(0xFFFBCFE8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CashVaultSummaryCard(
    state: TakaVaultUiState,
    onAddNotesClick: () -> Unit,
    onExchangeNotesClick: () -> Unit
) {
    val smallNotesCount = state.smallNoteCount
    val threshold = state.walletConfig.lowBhangtiThreshold
    val hasHealthyChange = smallNotesCount >= threshold

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        NeonMint.copy(alpha = 0.18f),
                        SlateCardBg
                    )
                )
            )
            .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(11.dp),
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonMint.copy(alpha = 0.2f))
                        .border(1.dp, NeonMint.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💵", fontSize = 17.sp)
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.tr("নগদ ভাঙতি ভল্ট", "Cash Bhangti Vault"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (hasHealthyChange) NeonMint.copy(alpha = 0.2f)
                                    else AmberGold.copy(alpha = 0.22f)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (hasHealthyChange) {
                                    BanglaFormatters.tr(
                                        "খুচরা: ${BanglaFormatters.toBanglaDigits(smallNotesCount)}টি",
                                        "Small: ${BanglaFormatters.toBanglaDigits(smallNotesCount)}"
                                    )
                                } else {
                                    BanglaFormatters.tr(
                                        "⚠️ খুচরা: ${BanglaFormatters.toBanglaDigits(smallNotesCount)}টি",
                                        "⚠️ Low: ${BanglaFormatters.toBanglaDigits(smallNotesCount)}"
                                    )
                                },
                                color = if (hasHealthyChange) NeonMint else AmberGold,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.formatBDT(state.cashTotal),
                            color = NeonMint,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("cash_vault_total_text")
                        )
                        Text(
                            text = BanglaFormatters.tr(
                                "(${BanglaFormatters.toBanglaDigits(state.totalNoteCount)}টি নোট)",
                                "(${BanglaFormatters.toBanglaDigits(state.totalNoteCount)} notes)"
                            ),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(NeonMint)
                        .clickable { onAddNotesClick() }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("btn_add_cash_notes"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("+ নোট যোগ", "+ Notes"),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(9.dp))
                        .clickable { onExchangeNotesClick() }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("btn_exchange_notes"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("🔄 ভাঙতি", "🔄 Break"),
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HomeBanknotesGridCard(
    state: TakaVaultUiState,
    onOpenAddCash: () -> Unit,
    onOpenNoteExchange: () -> Unit,
    onOpenNotePreview: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardBg.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = BanglaFormatters.tr("💴 বাংলাদেশী ভাঙতি নোট ভল্ট", "💴 Bangladeshi Bhangti Vault"),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = BanglaFormatters.tr(
                        "যেকোনো নোটে ট্যাপ করে পপআপে (+ / −) দিয়ে সংখ্যা পরিবর্তন করুন",
                        "Tap any note to open popup & adjust count (+ / −)"
                    ),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonMint.copy(alpha = 0.16f))
                        .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onOpenAddCash() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("+ বাল্ক যোগ", "+ Bulk Add"),
                        color = NeonMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.16f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onOpenNoteExchange() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("🔄 ভাঙতি", "🔄 Break"),
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Ultra space-efficient 5×2 grid (5 columns × 2 rows) for all 10 denominations
        val rows = listOf(
            listOf(1000, 500, 200, 100, 50),
            listOf(20, 10, 5, 2, 1)
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { rowDenoms ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowDenoms.forEach { denom ->
                        val count = state.notes[denom] ?: 0
                        CompactHomeNoteCard(
                            denom = denom,
                            count = count,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenNotePreview(denom) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactHomeNoteCard(
    denom: Int,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val customBitmap = CustomAssetManager.getCustomBitmap(context, CustomAssetManager.noteKey(denom))
    val style = BanknoteCatalog.styleFor(denom)
    val totalVal = denom * count

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        style.primaryColor.copy(alpha = 0.85f),
                        style.secondaryColor.copy(alpha = 0.45f),
                        ObsidianDark
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (count > 0) style.borderColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(11.dp)
            )
            .clickable { onClick() }
            .testTag("home_note_card_$denom")
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = "৳$denom Note",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color.Black.copy(alpha = 0.48f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "৳${BanglaFormatters.toBanglaDigits(denom)}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (count > 0) Color.Black.copy(alpha = 0.65f)
                        else Color.Black.copy(alpha = 0.35f)
                    )
                    .border(
                        1.dp,
                        if (count > 0) NeonMint.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${BanglaFormatters.toBanglaDigits(count)}${BanglaFormatters.tr("টি", "x")}",
                    color = if (count > 0) NeonMint else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            Text(
                text = if (totalVal > 0) "৳${BanglaFormatters.toBanglaDigits(totalVal)}" else BanglaFormatters.tr("৳০", "৳0"),
                color = if (totalVal > 0) style.accentColor else TextSecondary.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeQuickCategoriesCard(
    state: TakaVaultUiState,
    onCategoryClick: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardBg.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = BanglaFormatters.tr("⚡ দ্রুত খরচের খাত", "⚡ Quick Expense Categories"),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
                        .clickable { onAddCategoryClick() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = BanglaFormatters.tr("+ নতুন খাত", "+ New"),
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .clickable { onSeeAllClick() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = BanglaFormatters.tr("সব খাত ➔", "All ➔"),
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Compact 4-column grid (2 rows of 4)
        val rows = state.categories.take(8).chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { rowCats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowCats.forEach { cat ->
                        val catColor = BanknoteCatalog.parseHexColor(cat.colorHex)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlateSurface)
                                .border(1.dp, catColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable { onCategoryClick(cat.id) }
                                .padding(horizontal = 4.dp, vertical = 7.dp)
                                .testTag("quick_cat_${cat.id}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
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
fun TempMoneyTrackerCard(
    items: List<TempMoneyEntity>,
    onAddTempMoney: (String, String, String) -> Unit,
    onToggleTempMoney: (TempMoneyEntity) -> Unit,
    onDeleteTempMoney: (String) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var whereStored by remember { mutableStateOf("cash") }

    val activeTotal = items.filter { !it.done }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardBg.copy(alpha = 0.92f))
            .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = BanglaFormatters.tr("⏳ অস্থায়ী টাকা (অন্যের / সাময়িক জমা)", "⏳ Temporary Held Money"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = BanglaFormatters.tr("মূল তহবিলের বাইরে আলাদা সাময়িক হিসাব", "Tracked separately from your main net worth"),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AmberGold.copy(alpha = 0.15f))
                    .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = BanglaFormatters.formatBDT(activeTotal),
                    color = AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                placeholder = { Text(BanglaFormatters.tr("কার টাকা / কী জন্য?", "Whose / Purpose?"), fontSize = 11.sp, color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.weight(1.2f),
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
                placeholder = { Text("৳", fontSize = 11.sp, color = TextSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.75f),
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val places = listOf(
                "cash" to BanglaFormatters.tr("💵 নগদে", "💵 Cash"),
                "bkash" to BanglaFormatters.tr("📱 বিকাশে", "📱 bKash"),
                "dbbl" to BanglaFormatters.tr("🏦 ব্যাংকে", "🏦 DBBL"),
                "other" to BanglaFormatters.tr("📦 অন্যান্য", "📦 Other")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                places.forEach { (key, label) ->
                    val selected = whereStored == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) AmberGold.copy(alpha = 0.2f) else SlateSurface)
                            .border(
                                1.dp,
                                if (selected) AmberGold else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { whereStored = key }
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selected) AmberGold else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(AmberGold)
                    .clickable {
                        onAddTempMoney(titleInput, amountInput, whereStored)
                        if (titleInput.isNotBlank() && BanglaFormatters.parseBanglaNumber(amountInput) > 0) {
                            titleInput = ""
                            amountInput = ""
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BanglaFormatters.tr("+ যোগ", "+ Add"),
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (items.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items.forEach { item ->
                    androidx.compose.runtime.key(item.id) {
                        val badgeLabel = when (item.whereStored) {
                            "bkash" -> BanglaFormatters.tr("বিকাশে", "bKash")
                            "dbbl" -> BanglaFormatters.tr("ডিবিবিএল", "DBBL")
                            "other" -> BanglaFormatters.tr("অন্যান্য", "Other")
                            else -> BanglaFormatters.tr("নগদে", "Cash")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (item.done) 0.55f else 1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ObsidianDark)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleTempMoney(item) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (item.done) NeonMint else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (item.done) NeonMint else Color.White.copy(alpha = 0.3f),
                                            RoundedCornerShape(5.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.done) {
                                        Text(text = "✓", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Column {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "$badgeLabel • ${BanglaFormatters.formatBanglaDateTime(item.timestamp)}",
                                        color = TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = BanglaFormatters.formatBDT(item.amount),
                                    color = AmberGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .clickable { onDeleteTempMoney(item.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "✕", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeRecentSnapshotCard(
    state: TakaVaultUiState,
    onSeeAllHistory: () -> Unit,
    onSeeAllDebts: () -> Unit,
    onOpenReceipt: (TransactionEntity) -> Unit,
    onSettleDebt: (DebtEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardBg.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = BanglaFormatters.tr("🕒 সাম্প্রতিক লেনদেন", "🕒 Recent Activity"),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = BanglaFormatters.tr("সব ইতিহাস ➔", "Full History ➔"),
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onSeeAllHistory() }
            )
        }

        if (state.transactions.isEmpty()) {
            Text(
                text = BanglaFormatters.tr("এখনো কোনো লেনদেন রেকর্ড করা হয়নি।", "No transactions recorded yet."),
                color = TextSecondary,
                fontSize = 11.sp
            )
        } else {
            val categoriesById = remember(state.categories) {
                state.categories.associateBy { it.id }
            }
            state.transactions.take(3).forEach { tx ->
                androidx.compose.runtime.key(tx.id) {
                    val cat = categoriesById[tx.categoryId]
                    val isInflow = tx.type == "inflow"
                    val methodBadge = when (tx.method) {
                        "bkash" -> BanglaFormatters.tr("বিকাশ", "bKash")
                        "dbbl" -> BanglaFormatters.tr("ডিবিবিএল", "DBBL")
                        else -> BanglaFormatters.tr("নগদ", "Cash")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianDark)
                            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
                            .clickable { onOpenReceipt(tx) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = cat?.icon ?: if (isInflow) "📥" else "📦", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = tx.note.ifEmpty { cat?.name ?: BanglaFormatters.tr("লেনদেন", "Transaction") },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$methodBadge • ${BanglaFormatters.formatBanglaDateTime(tx.timestamp)}",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = "${if (isInflow) "+" else "−"}${BanglaFormatters.formatBDT(tx.amount)}",
                            color = if (isInflow) NeonMint else NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        if (state.debts.isNotEmpty()) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BanglaFormatters.tr(
                        "📒 অমীমাংসিত ভাঙতি ও বাকি (${BanglaFormatters.toBanglaDigits(state.debts.size)}টি)",
                        "📒 Pending Change & Debts (${BanglaFormatters.toBanglaDigits(state.debts.size)})"
                    ),
                    color = AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = BanglaFormatters.tr("বাকি খাতা ➔", "Debt Ledger ➔"),
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSeeAllDebts() }
                )
            }
            state.debts.take(2).forEach { d ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianDark)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = d.person, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(
                            text = if (d.type == "receivable") BanglaFormatters.tr("আপনি পাবেন", "Receivable")
                            else BanglaFormatters.tr("আপনার দেনা", "Payable"),
                            color = if (d.type == "receivable") NeonMint else NeonPink,
                            fontSize = 9.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = BanglaFormatters.formatBDT(d.amount),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonMint.copy(alpha = 0.18f))
                                .clickable { onSettleDebt(d) }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(text = BanglaFormatters.tr("✓ মিটান", "✓ Settle"), color = NeonMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesTabContent(
    state: TakaVaultUiState,
    onCategoryClick: (String) -> Unit,
    onAddCategory: () -> Unit,
    onQuickPresetExpense: (String, String, Int) -> Unit
) {
    val categorySpendMap = remember(state.transactions) {
        val map = mutableMapOf<String, Pair<Int, Int>>()
        state.transactions.filter { it.type == "expense" }.forEach { tx ->
            val cur = map[tx.categoryId] ?: (0 to 0)
            map[tx.categoryId] = (cur.first + tx.amount) to (cur.second + 1)
        }
        map
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // One-Tap Presets Card
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
                text = BanglaFormatters.tr("⚡ ১-ট্যাপ রেডি প্রিসেট খরচ", "⚡ 1-Tap Quick Expense Presets"),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = BanglaFormatters.tr(
                    "প্রতিদিনের সাধারণ খরচে দ্রুত ট্যাপ করে নোট বা বিকাশ পেমেন্ট সম্পন্ন করুন",
                    "Tap any everyday expense preset to pay via Cash, bKash, or DBBL"
                ),
                color = TextSecondary,
                fontSize = 11.sp
            )

            val presets = listOf(
                Triple("transport", "🛺 অটো ভাড়া", 20),
                Triple("transport", "🛺 রিকশা ভাড়া", 40),
                Triple("food", "☕ চা ও নাস্তা", 50),
                Triple("food", "🍱 দুপুরের খাবার", 150),
                Triple("grocery", "🛒 কাঁচাবাজার", 300),
                Triple("bills", "📱 মোবাইল রিচার্জ", 100)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (catId, label, amt) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .clickable { onQuickPresetExpense(catId, label, amt) }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "$label (${BanglaFormatters.formatBDT(amt)})",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // All Expense Categories Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateCardBg.copy(alpha = 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
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
                        text = BanglaFormatters.tr("📂 সকল খরচের খাতসমূহ", "📂 All Expense Categories"),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = BanglaFormatters.tr("যেকোনো খাতে ট্যাপ করে নতুন খরচ রেকর্ড করুন", "Tap any category to record an expense"),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.16f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { onAddCategory() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = BanglaFormatters.tr("+ নতুন খাত যোগ", "+ Add Category"),
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val rows = state.categories.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCats.forEach { cat ->
                            val stats = categorySpendMap[cat.id] ?: (0 to 0)
                            val catColor = BanknoteCatalog.parseHexColor(cat.colorHex)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SlateSurface)
                                    .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .clickable { onCategoryClick(cat.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(catColor.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = cat.icon, fontSize = 18.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = BanglaFormatters.formatBDT(stats.first),
                                        color = catColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${BanglaFormatters.toBanglaDigits(stats.second)}${BanglaFormatters.tr("টি লেনদেন", " txns")}",
                                        color = TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        if (rowCats.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolsTabContent(
    state: TakaVaultUiState,
    onAddDebt: (String, String, String) -> Unit,
    onSettleDebt: (DebtEntity) -> Unit,
    onDeleteDebt: (String) -> Unit,
    onQuickFeeInputChange: (String) -> Unit,
    onOpenBkashBridge: (String) -> Unit,
    onOpenDbblBridge: (String) -> Unit,
    onOpenNoteExchange: () -> Unit
) {
    var debtType by remember { mutableStateOf("receivable") }
    var personInput by remember { mutableStateOf("") }
    var debtAmtInput by remember { mutableStateOf("") }

    // Bill Splitter states
    var splitBillInput by remember { mutableStateOf("১২০০") }
    var splitPeopleCount by remember { mutableStateOf(4) }
    var splitTipPercent by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Debt & Change Ledger Card
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
                text = BanglaFormatters.tr("📒 বাকি ও পাওনা খাতা (ভাঙতি গরমিল ট্র্যাকার)", "📒 Debt & Change Discrepancy Ledger"),
                color = Color.White,
                fontSize = 15.sp,
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
                        .background(if (debtType == "receivable") NeonMint.copy(alpha = 0.2f) else SlateSurface)
                        .border(
                            1.dp,
                            if (debtType == "receivable") NeonMint else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { debtType = "receivable" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("📥 আমি পাবো (পাওনা)", "📥 Receivable (Owed to me)"),
                        color = if (debtType == "receivable") NeonMint else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (debtType == "payable") NeonPink.copy(alpha = 0.2f) else SlateSurface)
                        .border(
                            1.dp,
                            if (debtType == "payable") NeonPink else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { debtType = "payable" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("📤 আমার দেনা (দেবো)", "📤 Payable (I owe)"),
                        color = if (debtType == "payable") NeonPink else TextSecondary,
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
                    value = personInput,
                    onValueChange = { personInput = it },
                    placeholder = { Text(BanglaFormatters.tr("ব্যক্তি বা দোকানের নাম", "Person or Shop Name"), fontSize = 11.sp, color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = debtAmtInput,
                    onValueChange = { debtAmtInput = it },
                    placeholder = { Text("৳", fontSize = 11.sp, color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.7f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan)
                        .clickable {
                            onAddDebt(debtType, personInput, debtAmtInput)
                            if (personInput.isNotBlank() && BanglaFormatters.parseBanglaNumber(debtAmtInput) > 0) {
                                personInput = ""
                                debtAmtInput = ""
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("+ যোগ", "+ Add"),
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (state.debts.isEmpty()) {
                Text(
                    text = BanglaFormatters.tr("কোনো বকেয়া বা পাওনা এন্ট্রি নেই।", "No active debts or pending change."),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.debts.forEach { d ->
                        androidx.compose.runtime.key(d.id) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ObsidianDark)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = d.person, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${if (d.type == "receivable") BanglaFormatters.tr("পাওনা", "Receivable") else BanglaFormatters.tr("দেনা", "Payable")} • ${BanglaFormatters.formatBanglaDateTime(d.timestamp)}",
                                        color = if (d.type == "receivable") NeonMint else NeonPink,
                                        fontSize = 10.sp
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = BanglaFormatters.formatBDT(d.amount),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NeonMint.copy(alpha = 0.18f))
                                            .border(1.dp, NeonMint.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { onSettleDebt(d) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = BanglaFormatters.tr("✓ নিষ্পত্তি", "✓ Settle"),
                                            color = NeonMint,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .clickable { onDeleteDebt(d.id) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(text = "✕", color = TextSecondary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. NEW: Friends Bill Splitter & Chanda Calculator (চাঁদা ও বিল ভাগ ক্যালকুলেটর)
        val totalSplitBill = BanglaFormatters.parseBanglaNumber(splitBillInput)
        val totalWithTip = Math.round(totalSplitBill * (1.0 + splitTipPercent / 100.0)).toInt()
        val perPersonShare = if (splitPeopleCount > 0) ceil(totalWithTip.toDouble() / splitPeopleCount.toDouble()).toInt() else totalWithTip

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateCardBg.copy(alpha = 0.92f))
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = BanglaFormatters.tr("🤝 চাঁদা ও রেস্তোরাঁ বিল ভাগ ক্যালকুলেটর", "🤝 Bill Splitter & Chanda Calculator"),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = splitBillInput,
                    onValueChange = { splitBillInput = it },
                    label = { Text(BanglaFormatters.tr("মোট বিল (৳)", "Total Bill (৳)"), fontSize = 11.sp) },
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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = BanglaFormatters.tr("মানুষ সংখ্যা", "People"), color = TextSecondary, fontSize = 10.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurface)
                                .clickable { if (splitPeopleCount > 1) splitPeopleCount-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "−", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${BanglaFormatters.toBanglaDigits(splitPeopleCount)}${BanglaFormatters.tr(" জন", "")}",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurface)
                                .clickable { if (splitPeopleCount < 50) splitPeopleCount++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = NeonMint, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0, 5, 10, 15).forEach { tip ->
                    val active = splitTipPercent == tip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) NeonCyan.copy(alpha = 0.2f) else SlateSurface)
                            .border(1.dp, if (active) NeonCyan else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable { splitTipPercent = tip }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tip == 0) BanglaFormatters.tr("টিপস ছাড়া", "No Tip")
                            else "+${BanglaFormatters.toBanglaDigits(tip)}% ${BanglaFormatters.tr("টিপস", "Tip")}",
                            color = if (active) NeonCyan else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianDark)
                    .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = BanglaFormatters.tr("জনপ্রতি চাঁদা / শেয়ার:", "Per Person Share:"), color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = BanglaFormatters.tr("সর্বমোট: ${BanglaFormatters.formatBDT(totalWithTip)}", "Total: ${BanglaFormatters.formatBDT(totalWithTip)}"),
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = BanglaFormatters.formatBDT(perPersonShare),
                    color = NeonMint,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 3. Quick bKash Cash-Out Fee Calculator & Vault Bridge Tools
        val calcAmt = BanglaFormatters.parseBanglaNumber(state.quickCalcBkashInput)
        val agentFee = Math.round(calcAmt * 0.0185).toInt()
        val priyoFee = Math.round(calcAmt * 0.0149).toInt()
        val atmFee = Math.round(calcAmt * 0.0149).toInt()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateCardBg.copy(alpha = 0.92f))
                .border(1.dp, BkashPink.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = BanglaFormatters.tr("🧮 বিকাশ ক্যাশ-আউট চার্জ ক্যালকুলেটর", "🧮 bKash Cash-Out Fee Calculator"),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = state.quickCalcBkashInput,
                onValueChange = onQuickFeeInputChange,
                label = { Text(BanglaFormatters.tr("ক্যাশ-আউটের পরিমাণ (৳)", "Cash-Out Amount (৳)"), fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeeMiniBox(
                    title = BanglaFormatters.tr("সাধারণ এজেন্ট (১.৮৫%)", "Regular Agent (1.85%)"),
                    fee = agentFee,
                    total = calcAmt + agentFee,
                    color = BkashPink,
                    modifier = Modifier.weight(1f)
                )
                FeeMiniBox(
                    title = BanglaFormatters.tr("প্রিয় এজেন্ট (১.৪৯%)", "Priyo Agent (1.49%)"),
                    fee = priyoFee,
                    total = calcAmt + priyoFee,
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                FeeMiniBox(
                    title = BanglaFormatters.tr("এটিএম বুথ (১.৪৯%)", "ATM Booth (1.49%)"),
                    fee = atmFee,
                    total = calcAmt + atmFee,
                    color = NeonMint,
                    modifier = Modifier.weight(1f)
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
                        .background(BkashPink)
                        .clickable { onOpenBkashBridge("cashout") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("📲 ক্যাশ-আউট", "📲 Cash-Out"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (state.walletConfig.showDbblSection) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DbblBlue)
                            .clickable { onOpenDbblBridge("atm_withdraw") }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("🏧 ডিবিবিএল এটিএম", "🏧 DBBL ATM"),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .clickable { onOpenNoteExchange() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = BanglaFormatters.tr("🔄 নোট ভাঙতি", "🔄 Break Note"),
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeeMiniBox(
    title: String,
    fee: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianDark)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "+${BanglaFormatters.formatBDT(fee)}",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = BanglaFormatters.formatBDT(total),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
