package com.example

import com.example.data.WalletConfigEntity
import com.example.util.BanglaFormatters
import com.example.viewmodel.AppTab
import com.example.viewmodel.ExpenseWorkflowState
import com.example.viewmodel.TakaVaultUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `verify navigation tabs, bilingual formatters, DBBL toggle, and exact expenditure`() {
        BanglaFormatters.setLanguageMode("bn")
        assertEquals(4, AppTab.entries.size)
        assertEquals("হোম ও ভল্ট", AppTab.HOME.label)

        assertEquals("৳১,৪৫০", BanglaFormatters.formatBDT(1450))
        assertEquals("৳১,২৩,৪৫৬", BanglaFormatters.formatBDT(123456))
        assertEquals(320, BanglaFormatters.parseBanglaNumber("৩২০"))
        assertEquals(1000, BanglaFormatters.parseBanglaNumber("1000"))

        // Verify English language toggle
        BanglaFormatters.setLanguageMode("en")
        assertEquals("Home & Vault", AppTab.HOME.label)
        assertEquals("৳1,450", BanglaFormatters.formatBDT(1450))
        BanglaFormatters.setLanguageMode("bn")

        val notes = mapOf(500 to 1, 100 to 2, 20 to 3)
        assertEquals(760, BanglaFormatters.calculateNotesTotal(notes))
        assertEquals(6, BanglaFormatters.calculateNotesCount(notes))
        val encoded = BanglaFormatters.encodeNotesMap(notes)
        val decoded = BanglaFormatters.decodeNotesMap(encoded)
        assertEquals(notes, decoded)

        // Verify DBBL balance inclusion when showDbblSection is true vs false
        val stateWithDbbl = TakaVaultUiState(
            walletConfig = WalletConfigEntity(bkashBalance = 1000, dbblBalance = 5000, showDbblSection = true),
            notes = mapOf(500 to 2)
        )
        assertEquals(7000, stateWithDbbl.netWorth)

        val stateHiddenDbbl = stateWithDbbl.copy(
            walletConfig = stateWithDbbl.walletConfig.copy(showDbblSection = false)
        )
        assertEquals(2000, stateHiddenDbbl.netWorth)

        // Verify exact expenditure match (expectedChange == 0 skips return change)
        val exactWorkflow = ExpenseWorkflowState(
            billAmountInput = "700",
            tenderNotes = mapOf(500 to 1, 200 to 1)
        )
        assertEquals(700, exactWorkflow.billAmount)
        assertEquals(700, exactWorkflow.tenderTotal)
        assertEquals(0, exactWorkflow.expectedChange)

        // Verify biometric lock configuration and custom days filter state
        val bioConfig = WalletConfigEntity(
            biometricLockEnabled = true,
            biometricPinCode = "2580"
        )
        assertEquals(true, bioConfig.biometricLockEnabled)
        assertEquals("2580", bioConfig.biometricPinCode)

        val stateCustomDays = stateWithDbbl.copy(
            filterState = stateWithDbbl.filterState.copy(
                dateFilter = "custom_days",
                customDaysCount = 15
            )
        )
        assertEquals("custom_days", stateCustomDays.filterState.dateFilter)
        assertEquals(15, stateCustomDays.filterState.customDaysCount)
    }
}

