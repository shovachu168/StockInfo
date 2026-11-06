package com.cathysec.stock.info.util

import com.cathysec.stock.info.data.StockDayAverageResponse
import com.cathysec.stock.info.data.StockDayResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class StockMergeUtilTest {

    private fun day(
        code: String,
        name: String = "name-$code",
        closing: String? = "100.00",
        change: String? = "1.0000",
    ) = StockDayResponse(
        code = code,
        name = name,
        tradeVolume = "1000",
        tradeValue = "100000",
        openingPrice = "99.00",
        highestPrice = "101.00",
        lowestPrice = "98.00",
        closingPrice = closing,
        change = change,
        transaction = "10",
    )

    private fun average(code: String, closing: String? = "100.00", monthly: String? = "95.50") =
        StockDayAverageResponse(code = code, name = "name-$code", closingPrice = closing, monthlyAveragePrice = monthly)

    // ---- correct ----

    @Test
    fun `merge combines daily data and monthly average by code`() {
        val stocks = StockMergeUtil.merge(
            days = listOf(day("2330", name = "TSMC", closing = "2260.00", change = "-35.0000")),
            averages = listOf(average("2330", monthly = "2295.00")),
        )

        assertEquals(1, stocks.size)
        val stock = stocks.single()
        assertEquals("2330", stock.code)
        assertEquals("TSMC", stock.name)
        assertEquals(BigDecimal("99.00"), stock.openingPrice)
        assertEquals(BigDecimal("2260.00"), stock.closingPrice)
        assertEquals(BigDecimal("101.00"), stock.highestPrice)
        assertEquals(BigDecimal("98.00"), stock.lowestPrice)
        assertEquals(BigDecimal("-35.00"), stock.change)
        assertEquals(BigDecimal("2295.00"), stock.monthlyAveragePrice)
        assertEquals(10L, stock.transaction)
        assertEquals(1000L, stock.tradeVolume)
        assertEquals(100_000L, stock.tradeValue)
    }

    @Test
    fun `merge keeps the order of the daily list`() {
        val stocks = StockMergeUtil.merge(
            days = listOf(day("2330"), day("0050"), day("1101")),
            averages = listOf(average("1101"), average("2330"), average("0050")),
        )

        assertEquals(listOf("2330", "0050", "1101"), stocks.map { it.code })
    }

    @Test
    fun `merge uses the closing price of the daily list`() {
        val stocks = StockMergeUtil.merge(
            days = listOf(day("2330", closing = "2260.00")),
            averages = listOf(average("2330", closing = "9999.00")),
        )

        assertEquals(BigDecimal("2260.00"), stocks.single().closingPrice)
    }

    // ---- error ----

    @Test
    fun `merge turns unparsable values into null without failing`() {
        val stocks = StockMergeUtil.merge(
            days = listOf(day("2330", closing = "abc", change = "--")),
            averages = listOf(average("2330", monthly = "")),
        )

        val stock = stocks.single()
        assertNull(stock.closingPrice)
        assertNull(stock.change)
        assertNull(stock.monthlyAveragePrice)
    }

    @Test
    fun `merge keeps a stock without trades with null prices`() {
        val noTrade = StockDayResponse(code = "00625K", name = "ETF", change = "0.0000", closingPrice = "")
        val stock = StockMergeUtil.merge(listOf(noTrade), emptyList()).single()

        assertNull(stock.openingPrice)
        assertNull(stock.closingPrice)
        assertNull(stock.tradeVolume)
        assertEquals(BigDecimal("0.00"), stock.change)
    }

    // ---- extreme values ----

    @Test
    fun `merge normalizes a tiny change to zero`() {
        val stock = StockMergeUtil.merge(listOf(day("2330", change = "0.0001")), emptyList()).single()

        assertEquals(0, stock.change!!.signum())
    }

    @Test
    fun `merge handles a very large trade value`() {
        val big = day("2330").copy(tradeValue = Long.MAX_VALUE.toString())
        val stock = StockMergeUtil.merge(listOf(big), emptyList()).single()

        assertEquals(Long.MAX_VALUE, stock.tradeValue)
    }

    // ---- out of bound ----

    @Test
    fun `merge returns an empty list when the daily list is empty`() {
        val stocks = StockMergeUtil.merge(emptyList(), listOf(average("2330")))

        assertTrue(stocks.isEmpty())
    }

    @Test
    fun `merge gives null monthly average when no matching code exists`() {
        val stocks = StockMergeUtil.merge(listOf(day("2330")), listOf(average("9999")))

        assertEquals(1, stocks.size)
        assertNull(stocks.single().monthlyAveragePrice)
    }

    @Test
    fun `merge works with an empty average list`() {
        val stocks = StockMergeUtil.merge(listOf(day("2330")), emptyList())

        assertEquals(1, stocks.size)
        assertNull(stocks.single().monthlyAveragePrice)
    }

    @Test
    fun `merge drops items that only exist in the average list`() {
        val stocks = StockMergeUtil.merge(listOf(day("2330")), listOf(average("2330"), average("1101")))

        assertEquals(listOf("2330"), stocks.map { it.code })
    }

    @Test
    fun `merge uses the last item when the average list has a duplicated code`() {
        val stocks = StockMergeUtil.merge(
            days = listOf(day("2330")),
            averages = listOf(average("2330", monthly = "1.00"), average("2330", monthly = "2.00")),
        )

        assertEquals(BigDecimal("2.00"), stocks.single().monthlyAveragePrice)
    }

    @Test
    fun `merge works with a single item`() {
        val stocks = StockMergeUtil.merge(listOf(day("2330")), listOf(average("2330")))

        assertEquals(1, stocks.size)
    }
}
