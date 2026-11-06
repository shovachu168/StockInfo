package com.cathysec.stock.info.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class StockParseUtilTest {

    // ---- parsePrice: correct ----

    @Test
    fun `parsePrice parses a normal price with two decimals`() {
        assertEquals(BigDecimal("2320.00"), StockParseUtil.parsePrice("2320.00"))
    }

    @Test
    fun `parsePrice normalizes four decimals of change to two`() {
        assertEquals(BigDecimal("-35.00"), StockParseUtil.parsePrice("-35.0000"))
    }

    @Test
    fun `parsePrice pads an integer price to two decimals`() {
        assertEquals(BigDecimal("100.00"), StockParseUtil.parsePrice("100"))
    }

    @Test
    fun `parsePrice trims surrounding spaces`() {
        assertEquals(BigDecimal("27.25"), StockParseUtil.parsePrice("  27.25 "))
    }

    // ---- parsePrice: error ----

    @Test
    fun `parsePrice returns null for null`() {
        assertNull(StockParseUtil.parsePrice(null))
    }

    @Test
    fun `parsePrice returns null for empty and blank strings`() {
        assertNull(StockParseUtil.parsePrice(""))
        assertNull(StockParseUtil.parsePrice("   "))
    }

    @Test
    fun `parsePrice returns null for missing value markers`() {
        assertNull(StockParseUtil.parsePrice("--"))
        assertNull(StockParseUtil.parsePrice("-"))
    }

    @Test
    fun `parsePrice returns null for malformed strings`() {
        assertNull(StockParseUtil.parsePrice("abc"))
        assertNull(StockParseUtil.parsePrice("1.2.3"))
        assertNull(StockParseUtil.parsePrice("1,234.50"))
    }

    // ---- parsePrice: extreme values ----

    @Test
    fun `parsePrice keeps zero as zero with two decimals`() {
        assertEquals(BigDecimal("0.00"), StockParseUtil.parsePrice("0.0000"))
    }

    @Test
    fun `parsePrice truncates instead of rounding at the 0_005 boundary`() {
        assertEquals(BigDecimal("0.00"), StockParseUtil.parsePrice("0.005"))
        assertEquals(BigDecimal("0.00"), StockParseUtil.parsePrice("0.009"))
        assertEquals(BigDecimal("0.01"), StockParseUtil.parsePrice("0.0149"))
    }

    @Test
    fun `parsePrice truncates negative values toward zero`() {
        assertEquals(BigDecimal("-0.01"), StockParseUtil.parsePrice("-0.019"))
        assertEquals(0, StockParseUtil.parsePrice("-0.005")!!.signum())
    }

    @Test
    fun `parsePrice turns a tiny change into zero so it matches the displayed text`() {
        val parsed = StockParseUtil.parsePrice("0.0001")
        assertEquals(0, parsed!!.signum())
    }

    @Test
    fun `parsePrice handles a very large price and many decimals`() {
        assertEquals(BigDecimal("99999999999999999999.99"), StockParseUtil.parsePrice("99999999999999999999.99"))
        assertEquals(BigDecimal("1.23"), StockParseUtil.parsePrice("1.2345678901234567890"))
    }

    // ---- parsePrice: out of bound ----
    // A price has no upper or lower bound in the parser (BigDecimal), so the only boundary is
    // the input scale, which is covered by the extreme value tests above.

    // ---- parseCount: correct ----

    @Test
    fun `parseCount parses a normal count`() {
        assertEquals(508_399L, StockParseUtil.parseCount("508399"))
        assertEquals(122_293_100_290L, StockParseUtil.parseCount("122293100290"))
    }

    @Test
    fun `parseCount trims surrounding spaces`() {
        assertEquals(16_560L, StockParseUtil.parseCount(" 16560 "))
    }

    // ---- parseCount: error ----

    @Test
    fun `parseCount returns null for null empty blank and markers`() {
        assertNull(StockParseUtil.parseCount(null))
        assertNull(StockParseUtil.parseCount(""))
        assertNull(StockParseUtil.parseCount("  "))
        assertNull(StockParseUtil.parseCount("--"))
    }

    @Test
    fun `parseCount returns null for malformed strings`() {
        assertNull(StockParseUtil.parseCount("abc"))
        assertNull(StockParseUtil.parseCount("1,000"))
        assertNull(StockParseUtil.parseCount("12.5"))
    }

    // ---- parseCount: extreme values ----

    @Test
    fun `parseCount handles zero and negative values`() {
        assertEquals(0L, StockParseUtil.parseCount("0"))
        assertEquals(-1L, StockParseUtil.parseCount("-1"))
    }

    @Test
    fun `parseCount handles Long MAX_VALUE`() {
        assertEquals(Long.MAX_VALUE, StockParseUtil.parseCount(Long.MAX_VALUE.toString()))
    }

    // ---- parseCount: out of bound ----

    @Test
    fun `parseCount returns null when the value exceeds Long MAX_VALUE`() {
        assertNull(StockParseUtil.parseCount("9223372036854775808"))
    }

    @Test
    fun `parseCount returns null when the value is below Long MIN_VALUE`() {
        assertNull(StockParseUtil.parseCount("-9223372036854775809"))
    }
}
