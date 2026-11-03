package com.example.data

import com.example.util.BanglaFormatters
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class TakaVaultRepository(private val dao: TakaVaultDao) {

    val walletConfigFlow: Flow<WalletConfigEntity?> = dao.observeWalletConfig()
    val noteCountsFlow: Flow<List<NoteCountEntity>> = dao.observeNoteCounts()
    val categoriesFlow: Flow<List<CategoryEntity>> = dao.observeCategories()
    val transactionsFlow: Flow<List<TransactionEntity>> = dao.observeTransactions()
    val debtsFlow: Flow<List<DebtEntity>> = dao.observeDebts()
    val tempMoneyFlow: Flow<List<TempMoneyEntity>> = dao.observeTempMoney()

    suspend fun ensureInitializedWithDemoIfNeeded() {
        val existingConfig = dao.getWalletConfig()
        if (existingConfig == null) {
            seedRichDemoData()
        }
    }

    suspend fun seedRichDemoData() {
        val now = System.currentTimeMillis()
        val currentConfig = dao.getWalletConfig()
        dao.upsertWalletConfig(
            WalletConfigEntity(
                id = 1,
                bkashBalance = 8450,
                bkashVisible = true,
                monthlyBudget = 25000,
                soundEnabled = currentConfig?.soundEnabled ?: true,
                layoutMode = currentConfig?.layoutMode ?: "tabs"
            )
        )

        val demoNotes = listOf(
            NoteCountEntity(1000, 3),
            NoteCountEntity(500, 4),
            NoteCountEntity(200, 3),
            NoteCountEntity(100, 5),
            NoteCountEntity(50, 4),
            NoteCountEntity(20, 6),
            NoteCountEntity(10, 8),
            NoteCountEntity(5, 6),
            NoteCountEntity(2, 5),
            NoteCountEntity(1, 10)
        )
        dao.upsertNoteCounts(demoNotes)

        dao.clearCategories()
        dao.upsertCategories(BanknoteCatalog.DEFAULT_CATEGORIES)

        dao.clearDebts()
        dao.insertDebts(
            listOf(
                DebtEntity(
                    id = "DEBT-1",
                    type = "receivable",
                    person = "রবীন্দ্র সরণি অটো চালক (ভাঙতি বকেয়া)",
                    amount = 5,
                    timestamp = now - 172_800_000L
                ),
                DebtEntity(
                    id = "DEBT-2",
                    type = "receivable",
                    person = "করিম স্টোর (মুদি দোকান ভাঙতি পাওনা)",
                    amount = 20,
                    timestamp = now - 90_000_000L
                )
            )
        )

        dao.clearTransactions()
        dao.insertTransactions(
            listOf(
                TransactionEntity(
                    id = "TRX-${now - 18_000_000L}",
                    timestamp = now - 18_000_000L,
                    type = "expense",
                    method = "cash",
                    categoryId = "grocery",
                    amount = 320,
                    note = "সপ্তাহের কাঁচাবাজার ও সবজি",
                    tenderedNotesEncoded = BanglaFormatters.encodeNotesMap(mapOf(500 to 1)),
                    tenderedTotal = 500,
                    receivedNotesEncoded = BanglaFormatters.encodeNotesMap(mapOf(100 to 1, 50 to 1, 20 to 1, 10 to 1)),
                    receivedTotal = 180,
                    expectedChange = 180,
                    discrepancy = 0
                ),
                TransactionEntity(
                    id = "TRX-${now - 86_400_000L}",
                    timestamp = now - 86_400_000L,
                    type = "expense",
                    method = "bkash",
                    categoryId = "bills",
                    amount = 1450,
                    note = "ডেসকো প্রিপেইড বিদ্যুৎ বিল ও ওয়াসা",
                    tenderedNotesEncoded = "",
                    tenderedTotal = 1450,
                    receivedNotesEncoded = "",
                    receivedTotal = 0,
                    expectedChange = 0,
                    discrepancy = 0
                ),
                TransactionEntity(
                    id = "TRX-${now - 172_800_000L}",
                    timestamp = now - 172_800_000L,
                    type = "expense",
                    method = "cash",
                    categoryId = "transport",
                    amount = 65,
                    note = "রবীন্দ্র সরণি থেকে অটো ভাড়া",
                    tenderedNotesEncoded = BanglaFormatters.encodeNotesMap(mapOf(100 to 1)),
                    tenderedTotal = 100,
                    receivedNotesEncoded = BanglaFormatters.encodeNotesMap(mapOf(20 to 1, 10 to 1)),
                    receivedTotal = 30,
                    expectedChange = 35,
                    discrepancy = -5
                ),
                TransactionEntity(
                    id = "TRX-${now - 240_000_000L}",
                    timestamp = now - 240_000_000L,
                    type = "expense",
                    method = "bkash",
                    categoryId = "food",
                    amount = 680,
                    note = "কাচ্চি ভাই রেস্টুরেন্ট ডিনার পেমেন্ট",
                    tenderedNotesEncoded = "",
                    tenderedTotal = 680,
                    receivedNotesEncoded = "",
                    receivedTotal = 0,
                    expectedChange = 0,
                    discrepancy = 0
                )
            )
        )

        dao.clearTempMoney()
    }

    suspend fun hardResetZero() {
        val current = dao.getWalletConfig() ?: WalletConfigEntity()
        dao.upsertWalletConfig(current.copy(bkashBalance = 0))
        dao.upsertNoteCounts(BanglaFormatters.DENOMINATIONS.map { NoteCountEntity(it, 0) })
        dao.clearTransactions()
        dao.clearDebts()
        dao.clearTempMoney()
    }

    suspend fun updateWalletConfig(transform: (WalletConfigEntity) -> WalletConfigEntity) {
        val current = dao.getWalletConfig() ?: WalletConfigEntity()
        dao.upsertWalletConfig(transform(current))
    }

    suspend fun setNoteCount(denom: Int, count: Int) {
        dao.upsertNoteCount(NoteCountEntity(denom, count.coerceAtLeast(0)))
    }

    suspend fun setNoteCountsMap(map: Map<Int, Int>) {
        val entities = BanglaFormatters.DENOMINATIONS.map { d ->
            NoteCountEntity(d, (map[d] ?: 0).coerceAtLeast(0))
        }
        dao.upsertNoteCounts(entities)
    }

    suspend fun addCategory(category: CategoryEntity) {
        dao.upsertCategory(category)
    }

    suspend fun insertTransaction(tx: TransactionEntity) {
        dao.insertTransaction(tx)
    }

    suspend fun deleteTransaction(id: String) {
        dao.deleteTransactionById(id)
    }

    suspend fun insertDebt(debt: DebtEntity) {
        dao.insertDebt(debt)
    }

    suspend fun deleteDebt(id: String) {
        dao.deleteDebtById(id)
    }

    suspend fun upsertTempMoney(item: TempMoneyEntity) {
        dao.upsertTempMoney(item)
    }

    suspend fun deleteTempMoney(id: String) {
        dao.deleteTempMoneyById(id)
    }

    suspend fun exportBackupJsonString(): String {
        val config = dao.getWalletConfig() ?: WalletConfigEntity()
        val notes = dao.getNoteCounts().associate { it.denom to it.count }
        val cats = dao.getCategories()
        val txs = dao.getTransactions()
        val debts = dao.getDebts()
        val temps = dao.getTempMoney()

        val root = JSONObject()
        root.put("bkashBalance", config.bkashBalance)
        root.put("bkashVisible", config.bkashVisible)

        val settingsObj = JSONObject()
        settingsObj.put("monthlyBudget", config.monthlyBudget)
        settingsObj.put("soundEnabled", config.soundEnabled)
        settingsObj.put("layoutMode", config.layoutMode)
        root.put("settings", settingsObj)

        val notesObj = JSONObject()
        BanglaFormatters.DENOMINATIONS.forEach { d ->
            notesObj.put(d.toString(), notes[d] ?: 0)
        }
        root.put("notes", notesObj)

        val catsArr = JSONArray()
        cats.forEach { c ->
            val o = JSONObject()
            o.put("id", c.id)
            o.put("name", c.name)
            o.put("icon", c.icon)
            o.put("color", c.colorHex)
            catsArr.put(o)
        }
        root.put("categories", catsArr)

        val txArr = JSONArray()
        txs.forEach { t ->
            val o = JSONObject()
            o.put("id", t.id)
            o.put("timestamp", t.timestamp)
            o.put("type", t.type)
            o.put("method", t.method)
            o.put("categoryId", t.categoryId)
            o.put("amount", t.amount)
            o.put("note", t.note)
            o.put("tenderedTotal", t.tenderedTotal)
            o.put("receivedTotal", t.receivedTotal)
            o.put("expectedChange", t.expectedChange)
            o.put("discrepancy", t.discrepancy)

            val tNotes = JSONObject()
            t.tenderedNotes.forEach { (k, v) -> tNotes.put(k.toString(), v) }
            o.put("tenderedNotes", tNotes)

            val rNotes = JSONObject()
            t.receivedNotes.forEach { (k, v) -> rNotes.put(k.toString(), v) }
            o.put("receivedNotes", rNotes)
            txArr.put(o)
        }
        root.put("transactions", txArr)

        val debtArr = JSONArray()
        debts.forEach { d ->
            val o = JSONObject()
            o.put("id", d.id)
            o.put("type", d.type)
            o.put("person", d.person)
            o.put("amount", d.amount)
            o.put("timestamp", d.timestamp)
            debtArr.put(o)
        }
        root.put("debts", debtArr)

        val tempArr = JSONArray()
        temps.forEach { tm ->
            val o = JSONObject()
            o.put("id", tm.id)
            o.put("title", tm.title)
            o.put("amount", tm.amount)
            o.put("where", tm.whereStored)
            o.put("ts", tm.timestamp)
            o.put("done", tm.done)
            tempArr.put(o)
        }
        root.put("tempMoney", tempArr)

        return root.toString(2)
    }

    suspend fun importBackupJsonString(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (!root.has("bkashBalance") || !root.has("notes")) return false

            val bkashBalance = root.optInt("bkashBalance", 0)
            val bkashVisible = root.optBoolean("bkashVisible", true)
            val settingsObj = root.optJSONObject("settings")
            val monthlyBudget = settingsObj?.optInt("monthlyBudget", 25000) ?: 25000
            val soundEnabled = settingsObj?.optBoolean("soundEnabled", true) ?: true
            val layoutMode = settingsObj?.optString("layoutMode", "tabs") ?: "tabs"

            dao.upsertWalletConfig(
                WalletConfigEntity(
                    id = 1,
                    bkashBalance = bkashBalance,
                    bkashVisible = bkashVisible,
                    monthlyBudget = monthlyBudget,
                    soundEnabled = soundEnabled,
                    layoutMode = layoutMode
                )
            )

            val notesObj = root.getJSONObject("notes")
            val noteEntities = BanglaFormatters.DENOMINATIONS.map { d ->
                NoteCountEntity(d, notesObj.optInt(d.toString(), 0).coerceAtLeast(0))
            }
            dao.upsertNoteCounts(noteEntities)

            val catsArr = root.optJSONArray("categories")
            if (catsArr != null && catsArr.length() > 0) {
                val cats = mutableListOf<CategoryEntity>()
                for (i in 0 until catsArr.length()) {
                    val o = catsArr.getJSONObject(i)
                    cats.add(
                        CategoryEntity(
                            id = o.optString("id", "cat_$i"),
                            name = o.optString("name", "খাত"),
                            icon = o.optString("icon", "✨"),
                            colorHex = o.optString("color", "#00F0FF"),
                            sortOrder = i
                        )
                    )
                }
                dao.clearCategories()
                dao.upsertCategories(cats)
            }

            val txArr = root.optJSONArray("transactions")
            dao.clearTransactions()
            if (txArr != null) {
                val txs = mutableListOf<TransactionEntity>()
                for (i in 0 until txArr.length()) {
                    val o = txArr.getJSONObject(i)
                    val tNotesObj = o.optJSONObject("tenderedNotes")
                    val tMap = mutableMapOf<Int, Int>()
                    if (tNotesObj != null) {
                        BanglaFormatters.DENOMINATIONS.forEach { d ->
                            val c = tNotesObj.optInt(d.toString(), 0)
                            if (c > 0) tMap[d] = c
                        }
                    }
                    val rNotesObj = o.optJSONObject("receivedNotes")
                    val rMap = mutableMapOf<Int, Int>()
                    if (rNotesObj != null) {
                        BanglaFormatters.DENOMINATIONS.forEach { d ->
                            val c = rNotesObj.optInt(d.toString(), 0)
                            if (c > 0) rMap[d] = c
                        }
                    }
                    txs.add(
                        TransactionEntity(
                            id = o.optString("id", "TRX-$i"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                            type = o.optString("type", "expense"),
                            method = o.optString("method", "cash"),
                            categoryId = o.optString("categoryId", "misc"),
                            amount = o.optInt("amount", 0),
                            note = o.optString("note", ""),
                            tenderedNotesEncoded = BanglaFormatters.encodeNotesMap(tMap),
                            tenderedTotal = o.optInt("tenderedTotal", 0),
                            receivedNotesEncoded = BanglaFormatters.encodeNotesMap(rMap),
                            receivedTotal = o.optInt("receivedTotal", 0),
                            expectedChange = o.optInt("expectedChange", 0),
                            discrepancy = o.optInt("discrepancy", 0)
                        )
                    )
                }
                dao.insertTransactions(txs)
            }

            val debtArr = root.optJSONArray("debts")
            dao.clearDebts()
            if (debtArr != null) {
                val debts = mutableListOf<DebtEntity>()
                for (i in 0 until debtArr.length()) {
                    val o = debtArr.getJSONObject(i)
                    debts.add(
                        DebtEntity(
                            id = o.optString("id", "DEBT-$i"),
                            type = o.optString("type", "receivable"),
                            person = o.optString("person", ""),
                            amount = o.optInt("amount", 0),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                dao.insertDebts(debts)
            }

            val tempArr = root.optJSONArray("tempMoney")
            dao.clearTempMoney()
            if (tempArr != null) {
                val temps = mutableListOf<TempMoneyEntity>()
                for (i in 0 until tempArr.length()) {
                    val o = tempArr.getJSONObject(i)
                    temps.add(
                        TempMoneyEntity(
                            id = o.optString("id", "TMP-$i"),
                            title = o.optString("title", ""),
                            amount = o.optInt("amount", 0),
                            whereStored = o.optString("where", "cash"),
                            timestamp = o.optLong("ts", System.currentTimeMillis()),
                            done = o.optBoolean("done", false)
                        )
                    )
                }
                dao.upsertTempMoneyList(temps)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
