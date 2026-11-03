package com.example

import com.example.util.BanglaFormatters
import com.example.viewmodel.AppTab
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `verify navigation tabs and Bangla formatters`() {
        assertEquals(4, AppTab.entries.size)
        assertEquals("হোম ও ভল্ট", AppTab.HOME.label)

        assertEquals("৳১,৪৫০", BanglaFormatters.formatBDT(1450))
        assertEquals("৳১,২৩,৪৫৬", BanglaFormatters.formatBDT(123456))
        assertEquals(320, BanglaFormatters.parseBanglaNumber("৩২০"))
        assertEquals(1000, BanglaFormatters.parseBanglaNumber("1000"))

        val notes = mapOf(500 to 1, 100 to 2, 20 to 3)
        assertEquals(760, BanglaFormatters.calculateNotesTotal(notes))
        assertEquals(6, BanglaFormatters.calculateNotesCount(notes))
        val encoded = BanglaFormatters.encodeNotesMap(notes)
        val decoded = BanglaFormatters.decodeNotesMap(encoded)
        assertEquals(notes, decoded)
    }
}
