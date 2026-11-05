package com.cathysec.stock.info.util

import com.cathysec.stock.info.model.PriceTrend
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class PriceTrendUtilTest {

    // closingPriceTrend

    @Test
    fun `closingPriceTrend is UP when closing price is above monthly average`() {
        assertEquals(
            PriceTrend.UP,
            PriceTrendUtil.closingPriceTrend(BigDecimal("161.50"), BigDecimal("160.00")),
        )
    }

    @Test
    fun `closingPriceTrend is DOWN when closing price is below monthly average`() {
        assertEquals(
            PriceTrend.DOWN,
            PriceTrendUtil.closingPriceTrend(BigDecimal("2260.00"), BigDecimal("2295.00")),
        )
    }

    @Test
    fun `closingPriceTrend is FLAT when closing price equals monthly average`() {
        assertEquals(
            PriceTrend.FLAT,
            PriceTrendUtil.closingPriceTrend(BigDecimal("19.30"), BigDecimal("19.30")),
        )
    }

    @Test
    fun `closingPriceTrend ignores decimal scale when comparing`() {
        assertEquals(
            PriceTrend.FLAT,
            PriceTrendUtil.closingPriceTrend(BigDecimal("19.3"), BigDecimal("19.30")),
        )
    }

    @Test
    fun `closingPriceTrend is FLAT when closing price is missing`() {
        assertEquals(PriceTrend.FLAT, PriceTrendUtil.closingPriceTrend(null, BigDecimal("19.30")))
    }

    @Test
    fun `closingPriceTrend is FLAT when monthly average is missing`() {
        assertEquals(PriceTrend.FLAT, PriceTrendUtil.closingPriceTrend(BigDecimal("19.30"), null))
    }

    // changeTrend

    @Test
    fun `changeTrend is UP for positive change`() {
        assertEquals(PriceTrend.UP, PriceTrendUtil.changeTrend(BigDecimal("40.0000")))
    }

    @Test
    fun `changeTrend is DOWN for negative change`() {
        assertEquals(PriceTrend.DOWN, PriceTrendUtil.changeTrend(BigDecimal("-35.0000")))
    }

    @Test
    fun `changeTrend is FLAT for zero change`() {
        assertEquals(PriceTrend.FLAT, PriceTrendUtil.changeTrend(BigDecimal("0.0000")))
    }

    @Test
    fun `changeTrend is FLAT when change is missing`() {
        assertEquals(PriceTrend.FLAT, PriceTrendUtil.changeTrend(null))
    }

    // Extreme values

    @Test
    fun `closingPriceTrend compares five digit prices`() {
        assertEquals(
            PriceTrend.UP,
            PriceTrendUtil.closingPriceTrend(BigDecimal("99999.99"), BigDecimal("98765.43")),
        )
        assertEquals(
            PriceTrend.DOWN,
            PriceTrendUtil.closingPriceTrend(BigDecimal("10000.00"), BigDecimal("10000.01")),
        )
    }

    @Test
    fun `changeTrend handles four digit changes`() {
        assertEquals(PriceTrend.DOWN, PriceTrendUtil.changeTrend(BigDecimal("-640.0000")))
        assertEquals(PriceTrend.UP, PriceTrendUtil.changeTrend(BigDecimal("9999.99")))
    }

    @Test
    fun `closingPriceTrend detects a difference of 0_0001`() {
        assertEquals(
            PriceTrend.UP,
            PriceTrendUtil.closingPriceTrend(BigDecimal("100.0001"), BigDecimal("100.0000")),
        )
        assertEquals(
            PriceTrend.DOWN,
            PriceTrendUtil.closingPriceTrend(BigDecimal("99.9999"), BigDecimal("100.0000")),
        )
    }

    @Test
    fun `closingPriceTrend compares very large prices exactly`() {
        assertEquals(
            PriceTrend.UP,
            PriceTrendUtil.closingPriceTrend(
                BigDecimal("100000000000000000001"),
                BigDecimal("100000000000000000000"),
            ),
        )
    }

    @Test
    fun `closingPriceTrend is FLAT when both prices are zero`() {
        assertEquals(PriceTrend.FLAT, PriceTrendUtil.closingPriceTrend(BigDecimal.ZERO, BigDecimal("0.00")))
    }

    @Test
    fun `changeTrend handles very large changes`() {
        assertEquals(PriceTrend.UP, PriceTrendUtil.changeTrend(BigDecimal("999999999999999999.99")))
        assertEquals(PriceTrend.DOWN, PriceTrendUtil.changeTrend(BigDecimal("-999999999999999999.99")))
    }

    // Out of bound: negative prices are invalid in practice but must not break the comparison

    @Test
    fun `closingPriceTrend compares negative prices numerically`() {
        assertEquals(
            PriceTrend.UP,
            PriceTrendUtil.closingPriceTrend(BigDecimal("-1.00"), BigDecimal("-2.00")),
        )
    }
}
