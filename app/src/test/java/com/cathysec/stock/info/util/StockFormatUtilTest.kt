package com.cathysec.stock.info.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class StockFormatUtilTest {

    // formatPrice

    @Test
    fun `formatPrice adds thousands separators and two decimals`() {
        assertEquals("2,320.00", StockFormatUtil.formatPrice(BigDecimal("2320")))
    }

    @Test
    fun `formatPrice keeps two decimals for small prices`() {
        assertEquals("19.15", StockFormatUtil.formatPrice(BigDecimal("19.15")))
    }

    @Test
    fun `formatPrice rounds half up to two decimals`() {
        assertEquals("3,786.67", StockFormatUtil.formatPrice(BigDecimal("3786.665")))
        assertEquals("3,786.66", StockFormatUtil.formatPrice(BigDecimal("3786.664")))
    }

    @Test
    fun `formatPrice returns placeholder for null`() {
        assertEquals(StockFormatUtil.PLACEHOLDER, StockFormatUtil.formatPrice(null))
    }

    // formatChange

    @Test
    fun `formatChange adds plus sign for positive change`() {
        assertEquals("+40.00", StockFormatUtil.formatChange(BigDecimal("40.0000")))
    }

    @Test
    fun `formatChange keeps minus sign for negative change`() {
        assertEquals("-35.00", StockFormatUtil.formatChange(BigDecimal("-35.0000")))
    }

    @Test
    fun `formatChange shows zero without sign`() {
        assertEquals("0.00", StockFormatUtil.formatChange(BigDecimal("0.0000")))
    }

    @Test
    fun `formatChange shows values that round to zero without sign`() {
        assertEquals("0.00", StockFormatUtil.formatChange(BigDecimal("0.001")))
        assertEquals("0.00", StockFormatUtil.formatChange(BigDecimal("-0.001")))
    }

    @Test
    fun `formatChange adds thousands separators`() {
        assertEquals("+1,234.50", StockFormatUtil.formatChange(BigDecimal("1234.5")))
    }

    @Test
    fun `formatChange returns placeholder for null`() {
        assertEquals(StockFormatUtil.PLACEHOLDER, StockFormatUtil.formatChange(null))
    }

    // formatCount

    @Test
    fun `formatCount adds thousands separators`() {
        assertEquals("122,293,100,290", StockFormatUtil.formatCount(122_293_100_290L))
    }

    @Test
    fun `formatCount keeps small numbers unchanged`() {
        assertEquals("0", StockFormatUtil.formatCount(0L))
        assertEquals("999", StockFormatUtil.formatCount(999L))
    }

    @Test
    fun `formatCount returns placeholder for null`() {
        assertEquals(StockFormatUtil.PLACEHOLDER, StockFormatUtil.formatCount(null))
    }

    // Extreme values

    @Test
    fun `formatPrice formats five digit prices`() {
        assertEquals("12,345.67", StockFormatUtil.formatPrice(BigDecimal("12345.67")))
    }

    @Test
    fun `formatPrice formats the five digit upper bound`() {
        assertEquals("99,999.99", StockFormatUtil.formatPrice(BigDecimal("99999.99")))
    }

    @Test
    fun `formatPrice rounds up past the five digit upper bound`() {
        assertEquals("100,000.00", StockFormatUtil.formatPrice(BigDecimal("99999.995")))
    }

    @Test
    fun `formatChange formats four digit changes`() {
        assertEquals("+1,250.00", StockFormatUtil.formatChange(BigDecimal("1250.0000")))
        assertEquals("-9,999.99", StockFormatUtil.formatChange(BigDecimal("-9999.99")))
    }

    @Test
    fun `formatPrice formats zero`() {
        assertEquals("0.00", StockFormatUtil.formatPrice(BigDecimal.ZERO))
    }

    @Test
    fun `formatPrice rounds the half up boundary 0_005 to 0_01`() {
        assertEquals("0.01", StockFormatUtil.formatPrice(BigDecimal("0.005")))
        assertEquals("0.00", StockFormatUtil.formatPrice(BigDecimal("0.004999")))
    }

    @Test
    fun `formatPrice truncates many decimal places to two`() {
        assertEquals("0.12", StockFormatUtil.formatPrice(BigDecimal("0.123456789")))
    }

    @Test
    fun `formatPrice keeps full precision for very large prices`() {
        assertEquals(
            "123,456,789,012,345,678.99",
            StockFormatUtil.formatPrice(BigDecimal("123456789012345678.99")),
        )
    }

    @Test
    fun `formatPrice accepts scientific notation input`() {
        assertEquals("1,000.00", StockFormatUtil.formatPrice(BigDecimal("1E+3")))
    }

    @Test
    fun `formatChange rounds the half up boundary away from zero`() {
        assertEquals("+0.01", StockFormatUtil.formatChange(BigDecimal("0.005")))
        assertEquals("-0.01", StockFormatUtil.formatChange(BigDecimal("-0.005")))
    }

    @Test
    fun `formatCount handles Long MAX_VALUE`() {
        assertEquals("9,223,372,036,854,775,807", StockFormatUtil.formatCount(Long.MAX_VALUE))
    }

    // Out of bound: values outside the valid business range must still be formatted safely

    @Test
    fun `formatPrice keeps minus sign for negative prices`() {
        assertEquals("-12.50", StockFormatUtil.formatPrice(BigDecimal("-12.5")))
    }

    @Test
    fun `formatCount keeps minus sign for negative counts`() {
        assertEquals("-1,234", StockFormatUtil.formatCount(-1_234L))
    }

    @Test
    fun `formatCount handles Long MIN_VALUE`() {
        assertEquals("-9,223,372,036,854,775,808", StockFormatUtil.formatCount(Long.MIN_VALUE))
    }
}
