package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BanglaFormatters
import com.example.viewmodel.AppTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context and verify navigation tabs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("টাকাভল্ট ৪.০", appName)
        assertEquals(4, AppTab.entries.size)
        assertEquals("হোম", AppTab.HOME.label)
    }

    @Test
    fun `bangla number formatting and parsing works accurately`() {
        assertEquals("৳১,৪৫০", BanglaFormatters.formatBDT(1450))
        assertEquals(320, BanglaFormatters.parseBanglaNumber("৩২০"))
        assertEquals(1000, BanglaFormatters.parseBanglaNumber("1000"))

        val notes = mapOf(500 to 1, 100 to 2, 20 to 3)
        assertEquals(760, BanglaFormatters.calculateNotesTotal(notes))
        val encoded = BanglaFormatters.encodeNotesMap(notes)
        val decoded = BanglaFormatters.decodeNotesMap(encoded)
        assertEquals(notes, decoded)
    }
}
