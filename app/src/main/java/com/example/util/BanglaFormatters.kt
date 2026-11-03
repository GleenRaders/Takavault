package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

object BanglaFormatters {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    val DENOMINATIONS = listOf(1000, 500, 200, 100, 50, 20, 10, 5, 2, 1)

    @Volatile
    var currentLanguage: String = "bn" // "bn" or "en"

    fun tr(bn: String, en: String): String = if (currentLanguage == "en") en else bn

    fun setLanguageMode(lang: String) {
        val normalized = if (lang == "en") "en" else "bn"
        if (currentLanguage != normalized) {
            currentLanguage = normalized
            bdtFormatCache.clear()
            dateFormatCache.clear()
        }
    }

    // Pre-computed O(1) lookup table for numbers 0..2000 (covers all note counts & denominations)
    private val smallNumberCache = Array(2001) { n ->
        val s = n.toString()
        val chars = CharArray(s.length) { i -> bnDigits[s[i] - '0'] }
        String(chars)
    }

    private val bdtFormatCache = ConcurrentHashMap<Long, String>(256)
    private val dateFormatCache = ConcurrentHashMap<Long, String>(128)
    private val banglaLocale = Locale("bn", "BD")

    private val bnDateFormatter = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("d MMM, hh:mm a", banglaLocale)
        }
    }

    private val enDateFormatter = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("d MMM, hh:mm a", Locale.US)
        }
    }

    fun toBanglaDigits(input: Int): String {
        if (currentLanguage == "en") return input.toString()
        if (input in 0..2000) return smallNumberCache[input]
        return toBanglaDigits(input.toString())
    }

    fun toBanglaDigits(input: Any?): String {
        if (input == null) return if (currentLanguage == "en") "0" else "০"
        if (currentLanguage == "en") {
            val s = input.toString()
            val out = CharArray(s.length)
            for (i in s.indices) {
                val ch = s[i]
                val bnIdx = bnDigits.indexOf(ch)
                out[i] = if (bnIdx >= 0) ('0' + bnIdx) else ch
            }
            return String(out)
        }
        if (input is Int && input in 0..2000) return smallNumberCache[input]
        val str = input.toString()
        val len = str.length
        val out = CharArray(len)
        for (i in 0 until len) {
            val ch = str[i]
            out[i] = if (ch in '0'..'9') bnDigits[ch - '0'] else ch
        }
        return String(out)
    }

    fun formatIndianCommas(number: Long): String {
        val str = abs(number).toString()
        val len = str.length
        if (len <= 3) return str
        val sb = StringBuilder(len + (len - 2) / 2)
        val firstGroupLen = if ((len - 3) % 2 == 1) 1 else 2
        sb.append(str, 0, firstGroupLen)
        var idx = firstGroupLen
        while (idx < len - 3) {
            sb.append(',')
            sb.append(str, idx, idx + 2)
            idx += 2
        }
        sb.append(',')
        sb.append(str, len - 3, len)
        return sb.toString()
    }

    fun formatBDT(amount: Long): String {
        val cacheKey = if (currentLanguage == "en") amount xor (1L shl 62) else amount
        bdtFormatCache[cacheKey]?.let { return it }
        val isNeg = amount < 0
        val formatted = formatIndianCommas(abs(amount))
        val prefix = if (isNeg) "-৳" else "৳"
        val result = prefix + if (currentLanguage == "en") formatted else toBanglaDigits(formatted)
        if (bdtFormatCache.size < 1024) {
            bdtFormatCache[cacheKey] = result
        }
        return result
    }

    fun formatBDT(amount: Int): String = formatBDT(amount.toLong())

    fun parseBanglaNumber(input: String?): Int {
        if (input.isNullOrBlank()) return 0
        val sb = StringBuilder(input.length)
        for (i in 0 until input.length) {
            val ch = input[i]
            val bnIdx = bnDigits.indexOf(ch)
            when {
                bnIdx >= 0 -> sb.append(bnIdx)
                ch in '0'..'9' -> sb.append(ch)
                ch == '.' -> sb.append('.')
            }
        }
        val clean = sb.toString()
        if (clean.isEmpty()) return 0
        return clean.toDoubleOrNull()?.let { Math.round(it).toInt() } ?: 0
    }

    fun formatBanglaDateTime(timestamp: Long): String {
        val minuteKey = (timestamp / 60_000L) xor (if (currentLanguage == "en") (1L shl 61) else 0L)
        dateFormatCache[minuteKey]?.let { return it }
        val formatted = try {
            if (currentLanguage == "en") {
                val sdf = enDateFormatter.get() ?: SimpleDateFormat("d MMM, hh:mm a", Locale.US)
                sdf.format(Date(timestamp))
            } else {
                val sdf = bnDateFormatter.get() ?: SimpleDateFormat("d MMM, hh:mm a", banglaLocale)
                toBanglaDigits(sdf.format(Date(timestamp)))
            }
        } catch (_: Exception) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            toBanglaDigits(sdf.format(Date(timestamp)))
        }
        if (dateFormatCache.size < 512) {
            dateFormatCache[minuteKey] = formatted
        }
        return formatted
    }

    fun summarizeNotesInline(notesMap: Map<Int, Int>): String {
        val sb = StringBuilder()
        for (denom in DENOMINATIONS) {
            val count = notesMap[denom] ?: 0
            if (count > 0) {
                if (sb.isNotEmpty()) sb.append(", ")
                sb.append('৳').append(toBanglaDigits(denom)).append('×').append(toBanglaDigits(count))
            }
        }
        return if (sb.isNotEmpty()) sb.toString() else if (currentLanguage == "en") "৳0" else "৳০"
    }

    fun encodeNotesMap(notesMap: Map<Int, Int>): String {
        val sb = StringBuilder()
        for (denom in DENOMINATIONS) {
            val count = notesMap[denom] ?: 0
            if (count > 0) {
                if (sb.isNotEmpty()) sb.append(';')
                sb.append(denom).append(':').append(count)
            }
        }
        return sb.toString()
    }

    fun decodeNotesMap(encoded: String?): Map<Int, Int> {
        if (encoded.isNullOrBlank()) return emptyMap()
        val result = mutableMapOf<Int, Int>()
        encoded.split(";").forEach { pair ->
            val colonIdx = pair.indexOf(':')
            if (colonIdx > 0 && colonIdx < pair.length - 1) {
                val d = pair.substring(0, colonIdx).toIntOrNull()
                val c = pair.substring(colonIdx + 1).toIntOrNull()
                if (d != null && c != null && c > 0) {
                    result[d] = c
                }
            }
        }
        return result
    }

    fun calculateNotesTotal(notesMap: Map<Int, Int>): Int {
        var sum = 0
        for (d in DENOMINATIONS) {
            sum += d * (notesMap[d] ?: 0)
        }
        return sum
    }

    fun calculateNotesCount(notesMap: Map<Int, Int>): Int {
        var count = 0
        for (d in DENOMINATIONS) {
            count += (notesMap[d] ?: 0)
        }
        return count
    }
}
