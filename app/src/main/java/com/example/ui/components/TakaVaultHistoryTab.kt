package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BanknoteCatalog
import com.example.data.TransactionEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BkashPink
import com.example.ui.theme.DbblBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextSecondary
import com.example.util.BanglaFormatters
import com.example.viewmodel.TakaVaultUiState
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryAndAnalyticsTabContent(
    state: TakaVaultUiState,
    filteredTransactions: List<TransactionEntity>,
    onSearchQueryChange: (String) -> Unit,
    onMethodFilterChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onDateFilterChange: (String) -> Unit,
    onCustomDaysChange: (Int) -> Unit,
    onExportPdfClick: () -> Unit,
    onOpenReceipt: (TransactionEntity) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    val expenses = remember(state.transactions) {
        state.transactions.filter { it.type == "expense" }
    }
    val totalExpense = remember(expenses) { expenses.sumOf { it.amount } }
    val bkashExpense = remember(expenses) { expenses.filter { it.method == "bkash" }.sumOf { it.amount } }
    val dbblExpense = remember(expenses) { expenses.filter { it.method == "dbbl" }.sumOf { it.amount } }
    val cashExpense = remember(expenses) { expenses.filter { it.method == "cash" }.sumOf { it.amount } }

    val bkashPct = if (totalExpense > 0) Math.round((bkashExpense.toDouble() / totalExpense.toDouble()) * 100).toInt() else 0
    val dbblPct = if (totalExpense > 0) Math.round((dbblExpense.toDouble() / totalExpense.toDouble()) * 100).toInt() else 0
    val cashPct = if (totalExpense > 0) (100 - bkashPct - dbblPct).coerceAtLeast(0) else 0

    val categoryStats = remember(expenses, state.categories) {
        val map = mutableMapOf<String, Int>()
        expenses.forEach { tx -> map[tx.categoryId] = (map[tx.categoryId] ?: 0) + tx.amount }
        state.categories.mapNotNull { cat ->
            val spent = map[cat.id] ?: 0
            if (spent > 0) Pair(cat, spent) else null
        }.sortedByDescending { it.second }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Analytics Dashboard Card
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
                        text = BanglaFormatters.tr("📊 পরিসংখ্যান ড্যাশবোর্ড", "📊 Analytics Dashboard"),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = BanglaFormatters.tr("খাতভিত্তিক খরচ এবং বিকাশ, ব্যাংক ও নগদ অনুপাত", "Category breakdown & payment method split"),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NeonPink.copy(alpha = 0.15f))
                        .border(1.dp, NeonPink.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${BanglaFormatters.tr("মোট খরচ:", "Total:")} ${BanglaFormatters.formatBDT(totalExpense)}",
                        color = NeonPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Payment Method Split Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BkashPink.copy(alpha = 0.12f))
                        .border(1.dp, BkashPink.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = BanglaFormatters.tr("বিকাশ", "bKash"), color = Color(0xFFFBCFE8), fontSize = 10.sp)
                        Text(
                            text = "${BanglaFormatters.toBanglaDigits(bkashPct)}%",
                            color = Color(0xFFFBCFE8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = BanglaFormatters.formatBDT(bkashExpense),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { bkashPct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = BkashPink,
                        trackColor = Color.Black.copy(alpha = 0.4f)
                    )
                }

                if (state.walletConfig.showDbblSection) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DbblBlue.copy(alpha = 0.14f))
                            .border(1.dp, DbblBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(9.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = BanglaFormatters.tr("ডিবিবিএল", "DBBL"), color = Color(0xFF93C5FD), fontSize = 10.sp)
                            Text(
                                text = "${BanglaFormatters.toBanglaDigits(dbblPct)}%",
                                color = Color(0xFF93C5FD),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = BanglaFormatters.formatBDT(dbblExpense),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { dbblPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(50)),
                            color = DbblBlue,
                            trackColor = Color.Black.copy(alpha = 0.4f)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonMint.copy(alpha = 0.12f))
                        .border(1.dp, NeonMint.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = BanglaFormatters.tr("নগদ ক্যাশ", "Cash"), color = Color(0xFFA7F3D0), fontSize = 10.sp)
                        Text(
                            text = "${BanglaFormatters.toBanglaDigits(cashPct)}%",
                            color = Color(0xFFA7F3D0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = BanglaFormatters.formatBDT(cashExpense),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { cashPct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = NeonMint,
                        trackColor = Color.Black.copy(alpha = 0.4f)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // Donut Chart + Category Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(104.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        if (categoryStats.isEmpty() || totalExpense <= 0) {
                            drawArc(
                                color = Color.White.copy(alpha = 0.08f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                        } else {
                            var startAngle = -90f
                            categoryStats.forEach { (cat, spent) ->
                                val sweep = (spent.toFloat() / totalExpense.toFloat()) * 360f
                                drawArc(
                                    color = BanknoteCatalog.parseHexColor(cat.colorHex),
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweep
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = BanglaFormatters.tr("সর্বমোট ব্যয়", "Total Spend"), color = TextSecondary, fontSize = 9.sp)
                        Text(
                            text = BanglaFormatters.formatBDT(totalExpense),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (categoryStats.isEmpty()) {
                        Text(
                            text = BanglaFormatters.tr("এখনো কোনো খরচের রেকর্ড নেই", "No expenses recorded yet"),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        categoryStats.forEach { (cat, spent) ->
                            val pct = Math.round((spent.toDouble() / totalExpense.toDouble()) * 100).toInt()
                            val catColor = BanknoteCatalog.parseHexColor(cat.colorHex)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${cat.icon} ${cat.name}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${BanglaFormatters.formatBDT(spent)} (${BanglaFormatters.toBanglaDigits(pct)}%)",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = catColor,
                                    trackColor = Color.Black.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Note Denomination Value Distribution Card
        val cashTotal = state.cashTotal
        val highNotes = (state.notes[1000] ?: 0) * 1000 + (state.notes[500] ?: 0) * 500
        val midNotes = (state.notes[200] ?: 0) * 200 + (state.notes[100] ?: 0) * 100 + (state.notes[50] ?: 0) * 50
        val smallNotes = (state.notes[20] ?: 0) * 20 + (state.notes[10] ?: 0) * 10 +
            (state.notes[5] ?: 0) * 5 + (state.notes[2] ?: 0) * 2 + (state.notes[1] ?: 0) * 1
        val smallCount = state.smallNoteCount
        val threshold = state.walletConfig.lowBhangtiThreshold

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SlateCardBg.copy(alpha = 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BanglaFormatters.tr("💴 ভল্টে থাকা ভাঙতি ও নোটের মূল্যমান অনুপাত", "💴 Vault Denomination Distribution"),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (smallCount >= threshold) NeonMint.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (smallCount >= threshold) NeonMint.copy(alpha = 0.35f) else AmberGold.copy(alpha = 0.35f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (smallCount >= threshold) BanglaFormatters.tr("✓ পর্যাপ্ত খুচরা (${BanglaFormatters.toBanglaDigits(smallCount)}টি)", "✓ Healthy Change ($smallCount)")
                        else BanglaFormatters.tr("⚠️ খুচরা কম (${BanglaFormatters.toBanglaDigits(smallCount)}টি)", "⚠️ Low Change ($smallCount)"),
                        color = if (smallCount >= threshold) NeonMint else AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val tiers = listOf(
                Triple(BanglaFormatters.tr("বড় নোট (৳১০০০, ৳৫০০)", "Large Notes (৳1000, ৳500)"), highNotes, Color(0xFFA855F7)),
                Triple(BanglaFormatters.tr("মাঝারি নোট (৳২০০, ৳১০০, ৳৫০)", "Mid Notes (৳200, ৳100, ৳50)"), midNotes, NeonCyan),
                Triple(BanglaFormatters.tr("খুচরা ভাঙতি (৳২০ থেকে ৳১)", "Small Change (৳20 to ৳1)"), smallNotes, NeonMint)
            )
            tiers.forEach { (label, value, color) ->
                val pct = if (cashTotal > 0) Math.round((value.toDouble() / cashTotal.toDouble()) * 100).toInt() else 0
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${BanglaFormatters.formatBDT(value)} (${BanglaFormatters.toBanglaDigits(pct)}%)",
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
                        color = color,
                        trackColor = Color.Black.copy(alpha = 0.45f)
                    )
                }
            }
        }

        // Transaction History Feed & Filters
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
                        text = BanglaFormatters.tr("📜 লেনদেনের ইতিহাস ও নোট ট্র্যাকিং লগ", "📜 Transaction History & Note Log"),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = BanglaFormatters.tr("সময়সীমা নির্বাচন করে ইতিহাস দেখুন ও PDF এক্সপোর্ট করুন", "Filter by days, month, or year & export to PDF"),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonMint.copy(alpha = 0.2f))
                            .border(1.dp, NeonMint, RoundedCornerShape(10.dp))
                            .clickable { onExportPdfClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("btn_export_history_pdf"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaFormatters.tr("📄 PDF এক্সপোর্ট", "📄 Export PDF"),
                            color = NeonMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${BanglaFormatters.toBanglaDigits(filteredTransactions.size)}${BanglaFormatters.tr("টি", "")}",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search & Filter Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.filterState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(BanglaFormatters.tr("🔍 খাত বা বিবরণ খুঁজুন...", "🔍 Search category or memo..."), fontSize = 12.sp, color = TextSecondary) },
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

                val methods = buildList {
                    add("all" to BanglaFormatters.tr("সব মাধ্যম", "All"))
                    add("cash" to BanglaFormatters.tr("নগদ খরচ", "Cash"))
                    add("bkash" to BanglaFormatters.tr("বিকাশ খরচ", "bKash"))
                    if (state.walletConfig.showDbblSection) {
                        add("dbbl" to BanglaFormatters.tr("ডিবিবিএল খরচ", "DBBL"))
                    }
                    add("inflow" to BanglaFormatters.tr("টাকা জমা", "Inflow"))
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.forEach { (key, label) ->
                        val active = state.filterState.methodFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) NeonCyan.copy(alpha = 0.2f) else SlateSurface)
                                .border(
                                    1.dp,
                                    if (active) NeonCyan else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onMethodFilterChange(key) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (active) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Period Filter: All Time, Today, This Month, This Year, Custom Days
                Text(
                    text = BanglaFormatters.tr(
                        "📅 সময়সীমা নির্বাচন করুন (এই মাস / এই বছর / কত দিন):",
                        "📅 Select Period (This Month / This Year / How Many Days):"
                    ),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val dates = listOf(
                    "all" to BanglaFormatters.tr("সব সময়", "All Time"),
                    "today" to BanglaFormatters.tr("আজকের", "Today"),
                    "month" to BanglaFormatters.tr("এই মাস (চলতি মাস)", "This Month"),
                    "year" to BanglaFormatters.tr("এই বছর (চলতি বছর)", "This Year"),
                    "custom_days" to BanglaFormatters.tr(
                        "গত ${BanglaFormatters.toBanglaDigits(state.filterState.customDaysCount)} দিন (কাস্টম)",
                        "Last ${state.filterState.customDaysCount} Days (Custom)"
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dates.forEach { (key, label) ->
                        val active = state.filterState.dateFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) NeonMint.copy(alpha = 0.2f) else SlateSurface)
                                .border(
                                    1.dp,
                                    if (active) NeonMint else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onDateFilterChange(key) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("history_date_filter_$key")
                        ) {
                            Text(
                                text = label,
                                color = if (active) NeonMint else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Custom Days Selector Row (How many days: quick chips + custom input)
                var customDaysInput by remember(state.filterState.customDaysCount) {
                    mutableStateOf(BanglaFormatters.toBanglaDigits(state.filterState.customDaysCount))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateSurface.copy(alpha = 0.75f))
                        .border(
                            1.dp,
                            if (state.filterState.dateFilter == "custom_days") NeonMint.copy(alpha = 0.5f)
                            else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = BanglaFormatters.tr("🗓️ কত দিনের ইতিহাস দেখতে বা PDF করতে চান?", "🗓️ Set how many days to view or export:"),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = BanglaFormatters.tr(
                                "সক্রিয়: ${BanglaFormatters.toBanglaDigits(state.filterState.customDaysCount)} দিন",
                                "Active: ${state.filterState.customDaysCount}d"
                            ),
                            color = NeonMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf(3, 7, 15, 30, 60, 90).forEach { d ->
                            val isSelectedDay = state.filterState.dateFilter == "custom_days" &&
                                state.filterState.customDaysCount == d
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (isSelectedDay) NeonMint.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.35f))
                                    .border(
                                        1.dp,
                                        if (isSelectedDay) NeonMint else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(7.dp)
                                    )
                                    .clickable {
                                        customDaysInput = BanglaFormatters.toBanglaDigits(d)
                                        onCustomDaysChange(d)
                                    }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${BanglaFormatters.toBanglaDigits(d)}${BanglaFormatters.tr("দিন", "d")}",
                                    color = if (isSelectedDay) NeonMint else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
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
                            value = customDaysInput,
                            onValueChange = {
                                customDaysInput = it
                                val parsed = BanglaFormatters.parseBanglaNumber(it)
                                if (parsed > 0) {
                                    onCustomDaysChange(parsed)
                                }
                            },
                            placeholder = { Text(BanglaFormatters.tr("যেকোনো দিন সংখ্যা লিখুন (যেমন: ৪৫)", "Enter custom days (e.g. 45)"), fontSize = 11.sp, color = TextSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_custom_days"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonMint,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonMint)
                                .clickable { onExportPdfClick() }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .testTag("btn_export_filtered_pdf"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = BanglaFormatters.tr("📄 এই সময়ের PDF", "📄 Save PDF"),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "📭", fontSize = 26.sp)
                    Text(
                        text = BanglaFormatters.tr("এই ফিল্টারে কোনো লেনদেন পাওয়া যায়নি", "No transactions match this filter"),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val categoriesById = remember(state.categories) {
                    state.categories.associateBy { it.id }
                }
                filteredTransactions.forEach { tx ->
                    androidx.compose.runtime.key(tx.id) {
                        val isInflow = tx.type == "inflow"
                        val cat = categoriesById[tx.categoryId]
                        val methodBadgeColor = when (tx.method) {
                            "bkash" -> BkashPink
                            "dbbl" -> DbblBlue
                            else -> NeonMint
                        }
                        val methodLabel = when (tx.method) {
                            "bkash" -> BanglaFormatters.tr("বিকাশ", "bKash")
                            "dbbl" -> BanglaFormatters.tr("ডিবিবিএল", "DBBL")
                            else -> BanglaFormatters.tr("নগদ", "Cash")
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat?.icon ?: if (isInflow) "📥" else "📦",
                                            fontSize = 16.sp
                                        )
                                    }
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = cat?.name ?: if (isInflow) BanglaFormatters.tr("তহবিল জমা", "Inflow") else BanglaFormatters.tr("বিবিধ", "Misc"),
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(methodBadgeColor.copy(alpha = 0.22f))
                                                    .border(
                                                        1.dp,
                                                        methodBadgeColor.copy(alpha = 0.45f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = methodLabel,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (tx.note.isNotBlank()) {
                                            Text(
                                                text = tx.note,
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text(
                                            text = BanglaFormatters.formatBanglaDateTime(tx.timestamp),
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${if (isInflow) "+" else "−"} ${BanglaFormatters.formatBDT(tx.amount)}",
                                        color = if (isInflow) NeonMint else NeonPink,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonCyan.copy(alpha = 0.15f))
                                                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                                .clickable { onOpenReceipt(tx) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = BanglaFormatters.tr("🧾 রসিদ", "🧾 Receipt"),
                                                color = NeonCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(alpha = 0.06f))
                                                .clickable { onDeleteTransaction(tx.id) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "✕", color = TextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            if (tx.method == "cash" && tx.type == "expense") {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "📤 ${BanglaFormatters.tr("দেওয়া:", "Given:")} ${BanglaFormatters.summarizeNotesInline(tx.tenderedNotes)}",
                                            color = NeonCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "📥 ${BanglaFormatters.tr("ফেরত:", "Change:")} ${BanglaFormatters.summarizeNotesInline(tx.receivedNotes)}",
                                            color = NeonMint,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (tx.discrepancy != 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (tx.discrepancy < 0) NeonPink.copy(alpha = 0.2f)
                                                    else AmberGold.copy(alpha = 0.2f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (tx.discrepancy < 0) "${BanglaFormatters.tr("পাওনা বাকি:", "Owed:")} ${BanglaFormatters.formatBDT(abs(tx.discrepancy))}"
                                                else "${BanglaFormatters.tr("অতিরিক্ত প্রাপ্তি:", "Extra:")} ${BanglaFormatters.formatBDT(tx.discrepancy)}",
                                                color = if (tx.discrepancy < 0) NeonPink else AmberGold,
                                                fontSize = 10.sp,
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
        }
    }
}
