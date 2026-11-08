package com.cathysec.stock.info.viewmodel

import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.model.PriceTrend
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockListUiStateTest {

    private val stock = StockUiModel(
        code = "2330",
        name = "TSMC",
        openingPrice = "2,320.00",
        closingPrice = "2,260.00",
        highestPrice = "2,320.00",
        lowestPrice = "2,235.00",
        change = "-35.00",
        monthlyAveragePrice = "2,295.00",
        transaction = "508,399",
        tradeVolume = "53,879,371",
        tradeValue = "122,293,100,290",
        closingPriceTrend = PriceTrend.DOWN,
        changeTrend = PriceTrend.DOWN,
    )

    // Correct

    @Test
    fun `canSort is true for a loaded list that is not refreshing`() {
        assertTrue(StockListUiState.Success(listOf(stock, stock.copy(code = "2317"))).canSort)
    }

    // Error

    @Test
    fun `canSort is false while loading`() {
        assertFalse(StockListUiState.Loading.canSort)
    }

    @Test
    fun `canSort is false for every load error`() {
        // Guards against the enum being empty, which would make the loop below pass trivially.
        assertTrue(LoadError.entries.isNotEmpty())
        LoadError.entries.forEach { error ->
            assertFalse("canSort for $error", StockListUiState.Error(error).canSort)
        }
    }

    @Test
    fun `canSort is false for a loaded list that is refreshing`() {
        assertFalse(StockListUiState.Success(listOf(stock), isRefreshing = true).canSort)
    }

    // Extreme values

    @Test
    fun `canSort is true for an empty list that is not refreshing`() {
        assertTrue(StockListUiState.Success(emptyList()).canSort)
    }

    @Test
    fun `canSort is false for an empty list that is refreshing`() {
        assertFalse(StockListUiState.Success(emptyList(), isRefreshing = true).canSort)
    }

    // Out of bound: not applicable. canSort is a property with no input, so there is no argument,
    // index or type limit to exceed. It does not read the list contents, so the list size cannot
    // go out of bound either; the empty list cases above cover the smallest list.

    // CancellationException: not applicable. canSort is a plain getter with no coroutine or
    // exception handling.
}
