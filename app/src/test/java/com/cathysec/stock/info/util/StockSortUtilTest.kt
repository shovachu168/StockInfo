package com.cathysec.stock.info.util

import com.cathysec.stock.info.model.SortOrder
import com.cathysec.stock.info.model.Stock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class StockSortUtilTest {

    private fun stock(code: String, name: String = "name-$code") = Stock(
        code = code,
        name = name,
        openingPrice = null,
        closingPrice = null,
        highestPrice = null,
        lowestPrice = null,
        change = null,
        monthlyAveragePrice = null,
        transaction = null,
        tradeVolume = null,
        tradeValue = null,
    )

    private fun codes(stocks: List<Stock>) = stocks.map { it.code }

    // ---- correct ----

    @Test
    fun `default order is descending`() {
        assertEquals(SortOrder.DESCENDING, StockSortUtil.DEFAULT_ORDER)
    }

    @Test
    fun `sortByCode sorts ascending`() {
        val result = StockSortUtil.sortByCode(listOf(stock("2330"), stock("1101"), stock("2317")), SortOrder.ASCENDING)

        assertEquals(listOf("1101", "2317", "2330"), codes(result))
    }

    @Test
    fun `sortByCode sorts descending`() {
        val result = StockSortUtil.sortByCode(listOf(stock("2330"), stock("1101"), stock("2317")), SortOrder.DESCENDING)

        assertEquals(listOf("2330", "2317", "1101"), codes(result))
    }

    @Test
    fun `sortByCode does not change the input list`() {
        val input = listOf(stock("2330"), stock("1101"))
        StockSortUtil.sortByCode(input, SortOrder.ASCENDING)

        assertEquals(listOf("2330", "1101"), codes(input))
    }

    // ---- error ----
    // The input is a typed list of stocks, so there is no invalid input to test.
    // Unusual codes (letters, leading zeros) are covered by the extreme value tests.

    // ---- extreme values ----

    @Test
    fun `sortByCode compares codes as text so leading zeros are kept`() {
        val result = StockSortUtil.sortByCode(listOf(stock("1101"), stock("0050"), stock("00625K")), SortOrder.ASCENDING)

        assertEquals(listOf("0050", "00625K", "1101"), codes(result))
    }

    @Test
    fun `sortByCode puts digits before letters`() {
        val result = StockSortUtil.sortByCode(listOf(stock("00400A"), stock("00400"), stock("0050")), SortOrder.ASCENDING)

        assertEquals(listOf("00400", "00400A", "0050"), codes(result))
    }

    @Test
    fun `sortByCode orders codes that differ only by the last letter`() {
        val input = listOf(stock("00691B"), stock("00691"), stock("00691A"))

        assertEquals(listOf("00691", "00691A", "00691B"), codes(StockSortUtil.sortByCode(input, SortOrder.ASCENDING)))
        assertEquals(listOf("00691B", "00691A", "00691"), codes(StockSortUtil.sortByCode(input, SortOrder.DESCENDING)))
    }

    @Test
    fun `sortByCode does not compare codes as numbers`() {
        // As numbers 9 < 10000, but as text "10000" comes before "9".
        val result = StockSortUtil.sortByCode(listOf(stock("10000"), stock("9")), SortOrder.ASCENDING)

        assertEquals(listOf("10000", "9"), codes(result))
    }

    @Test
    fun `sortByCode keeps the original order of equal codes in both directions`() {
        val input = listOf(stock("2330", "first"), stock("1101"), stock("2330", "second"))

        val ascending = StockSortUtil.sortByCode(input, SortOrder.ASCENDING)
        val descending = StockSortUtil.sortByCode(input, SortOrder.DESCENDING)

        assertEquals(listOf("first", "second"), ascending.filter { it.code == "2330" }.map { it.name })
        assertEquals(listOf("first", "second"), descending.filter { it.code == "2330" }.map { it.name })
    }

    @Test
    fun `sortByCode handles a large list`() {
        val input = List(LARGE_LIST_SIZE) { stock(it.toString().padStart(CODE_LENGTH, '0')) }.shuffled()

        val result = StockSortUtil.sortByCode(input, SortOrder.ASCENDING)

        assertEquals(LARGE_LIST_SIZE, result.size)
        assertEquals("0000".padStart(CODE_LENGTH, '0'), result.first().code)
        assertEquals((LARGE_LIST_SIZE - 1).toString().padStart(CODE_LENGTH, '0'), result.last().code)
    }

    // ---- out of bound ----

    @Test
    fun `sortByCode returns an empty list for an empty list`() {
        assertTrue(StockSortUtil.sortByCode(emptyList(), SortOrder.ASCENDING).isEmpty())
        assertTrue(StockSortUtil.sortByCode(emptyList(), SortOrder.DESCENDING).isEmpty())
    }

    @Test
    fun `sortByCode returns the same content for a single item`() {
        val input = listOf(stock("2330"))

        assertEquals(input, StockSortUtil.sortByCode(input, SortOrder.ASCENDING))
        assertEquals(input, StockSortUtil.sortByCode(input, SortOrder.DESCENDING))
        assertNotSame(input, StockSortUtil.sortByCode(input, SortOrder.ASCENDING))
    }

    private companion object {
        const val LARGE_LIST_SIZE = 5_000
        const val CODE_LENGTH = 5
    }
}
