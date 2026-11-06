package com.cathysec.stock.info.viewmodel

import app.cash.turbine.test
import com.cathysec.stock.info.data.StockRepository
import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.model.PriceTrend
import com.cathysec.stock.info.model.SortOrder
import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.math.BigDecimal

class StockListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val stock = Stock(
        code = "2330",
        name = "TSMC",
        openingPrice = BigDecimal("2320.00"),
        closingPrice = BigDecimal("2260.00"),
        highestPrice = BigDecimal("2320.00"),
        lowestPrice = BigDecimal("2235.00"),
        change = BigDecimal("-35.0000"),
        monthlyAveragePrice = BigDecimal("2295.00"),
        transaction = 508_399L,
        tradeVolume = 53_879_371L,
        tradeValue = 122_293_100_290L,
    )

    @Test
    fun `emits loading then formatted stocks when loading succeeds`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(listOf(stock))))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            val success = awaitItem() as StockListUiState.Success
            assertEquals(
                listOf(
                    StockUiModel(
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
                    ),
                ),
                success.stocks,
            )
        }
    }

    @Test
    fun `emits error when loading fails`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.failure(IOException("network error"))))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(StockListUiState.Error(LoadError.GENERIC), awaitItem())
        }
    }

    @Test
    fun `retry loads again and emits stocks after a previous failure`() = runTest {
        val repository = FakeRepository(Result.failure(IOException("network error")))
        val viewModel = StockListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(StockListUiState.Error(LoadError.GENERIC), awaitItem())

            repository.result = Result.success(listOf(stock))
            viewModel.retry()

            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(1, (awaitItem() as StockListUiState.Success).stocks.size)
        }
        assertEquals(2, repository.callCount)
    }

    @Test
    fun `emits NO_CONNECTION error when the device is offline`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.failure(UnknownHostException("host"))))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(StockListUiState.Error(LoadError.NO_CONNECTION), awaitItem())
        }
    }

    @Test
    fun `emits TIMEOUT error when the request times out`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.failure(SocketTimeoutException("slow"))))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(StockListUiState.Error(LoadError.TIMEOUT), awaitItem())
        }
    }

    // Refresh

    @Test
    fun `refresh keeps the list on screen and replaces it with new data`() = runTest {
        val repository = FakeRepository(Result.success(listOf(stock)))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()

        viewModel.uiState.test {
            val old = awaitItem() as StockListUiState.Success
            repository.result = Result.success(listOf(stock.copy(code = "2317"), stock.copy(code = "2454")))

            viewModel.refresh()

            // The old list is still there while refreshing.
            assertEquals(StockListUiState.Success(old.stocks, isRefreshing = true), awaitItem())
            val refreshed = awaitItem() as StockListUiState.Success
            assertEquals(listOf("2454", "2317"), refreshed.stocks.map { it.code })
            assertEquals(false, refreshed.isRefreshing)
        }
    }

    @Test
    fun `refresh failure keeps the old list and emits a one time error`() = runTest {
        val repository = FakeRepository(Result.success(listOf(stock)))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()
        val old = viewModel.uiState.value as StockListUiState.Success
        repository.result = Result.failure(UnknownHostException("host"))

        viewModel.refreshErrors.test {
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(LoadError.NO_CONNECTION, awaitItem())
            assertEquals(StockListUiState.Success(old.stocks, isRefreshing = false), viewModel.uiState.value)
        }
    }

    @Test
    fun `refresh without a list behaves like retry`() = runTest {
        val repository = FakeRepository(Result.failure(IOException("network error")))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()
        repository.result = Result.success(listOf(stock))

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(1, (viewModel.uiState.value as StockListUiState.Success).stocks.size)
        assertEquals(2, repository.callCount)
    }

    @Test
    fun `refresh and retry are ignored while a load is running`() = runTest {
        val gate = CompletableDeferred<List<Stock>>()
        val repository = FakeRepository(Result.success(listOf(stock))).apply { onGetStocks = { gate.await() } }
        val viewModel = StockListViewModel(repository)
        runCurrent()

        viewModel.refresh()
        viewModel.retry()
        viewModel.refresh()
        runCurrent()

        assertEquals(1, repository.callCount)
        gate.complete(listOf(stock))
        advanceUntilIdle()
        assertEquals(1, repository.callCount)
        assertEquals(1, (viewModel.uiState.value as StockListUiState.Success).stocks.size)
    }

    @Test
    fun `refresh is ignored while another refresh is running`() = runTest {
        val repository = FakeRepository(Result.success(listOf(stock)))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()
        val gate = CompletableDeferred<List<Stock>>()
        repository.onGetStocks = { gate.await() }

        viewModel.refresh()
        viewModel.refresh()
        runCurrent()

        assertEquals(2, repository.callCount)
        gate.complete(listOf(stock))
        advanceUntilIdle()
        assertEquals(false, (viewModel.uiState.value as StockListUiState.Success).isRefreshing)
    }

    @Test
    fun `refresh can return an empty list`() = runTest {
        val repository = FakeRepository(Result.success(listOf(stock)))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()
        repository.result = Result.success(emptyList())

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(StockListUiState.Success(emptyList()), viewModel.uiState.value)
    }

    // Sorting

    private fun codesOf(state: StockListUiState) = (state as StockListUiState.Success).stocks.map { it.code }

    private fun stocksWithCodes(vararg codes: String) = codes.map { stock.copy(code = it) }

    @Test
    fun `list is sorted by code descending by default`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(stocksWithCodes("1101", "2330", "0050"))))
        advanceUntilIdle()

        assertEquals(SortOrder.DESCENDING, viewModel.sortOrder.value)
        assertEquals(listOf("2330", "1101", "0050"), codesOf(viewModel.uiState.value))
    }

    @Test
    fun `selecting ascending re-sorts the list without a new request`() = runTest {
        val repository = FakeRepository(Result.success(stocksWithCodes("1101", "2330", "0050")))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()

        viewModel.onSortOrderSelected(SortOrder.ASCENDING)

        assertEquals(SortOrder.ASCENDING, viewModel.sortOrder.value)
        assertEquals(listOf("0050", "1101", "2330"), codesOf(viewModel.uiState.value))
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `sort order is kept after refresh and retry`() = runTest {
        val repository = FakeRepository(Result.success(stocksWithCodes("1101", "2330")))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()
        viewModel.onSortOrderSelected(SortOrder.ASCENDING)

        repository.result = Result.success(stocksWithCodes("3008", "2454", "2317"))
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(listOf("2317", "2454", "3008"), codesOf(viewModel.uiState.value))

        repository.result = Result.failure(IOException("down"))
        viewModel.refresh()
        advanceUntilIdle()
        // A failed refresh keeps the old list in the chosen order.
        assertEquals(listOf("2317", "2454", "3008"), codesOf(viewModel.uiState.value))
    }

    @Test
    fun `selecting the same order keeps the state unchanged`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(stocksWithCodes("1101", "2330"))))
        advanceUntilIdle()
        val before = viewModel.uiState.value

        viewModel.onSortOrderSelected(SortOrder.DESCENDING)

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun `selecting an order while loading failed does not break the error state`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.failure(IOException("down"))))
        advanceUntilIdle()

        viewModel.onSortOrderSelected(SortOrder.ASCENDING)

        assertEquals(StockListUiState.Error(LoadError.GENERIC), viewModel.uiState.value)
        assertEquals(SortOrder.ASCENDING, viewModel.sortOrder.value)
    }

    @Test
    fun `selecting an order keeps the refreshing flag`() = runTest {
        val repository = FakeRepository(Result.success(stocksWithCodes("1101", "2330")))
        val viewModel = StockListViewModel(repository)
        advanceUntilIdle()
        val gate = CompletableDeferred<List<Stock>>()
        repository.onGetStocks = { gate.await() }
        viewModel.refresh()
        runCurrent()

        viewModel.onSortOrderSelected(SortOrder.ASCENDING)

        val state = viewModel.uiState.value as StockListUiState.Success
        assertEquals(true, state.isRefreshing)
        assertEquals(listOf("1101", "2330"), state.stocks.map { it.code })
        gate.complete(stocksWithCodes("3008", "2317"))
        advanceUntilIdle()
        assertEquals(listOf("2317", "3008"), codesOf(viewModel.uiState.value))
    }

    @Test
    fun `sorting an empty list works`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(emptyList())))
        advanceUntilIdle()

        viewModel.onSortOrderSelected(SortOrder.ASCENDING)

        assertEquals(StockListUiState.Success(emptyList()), viewModel.uiState.value)
    }

    // Sort sheet

    @Test
    fun `sort sheet is hidden at first and opens on click`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(emptyList())))
        assertEquals(false, viewModel.isSortSheetVisible.value)

        viewModel.onSortClicked()

        assertEquals(true, viewModel.isSortSheetVisible.value)
    }

    @Test
    fun `sort sheet closes when dismissed`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(emptyList())))
        viewModel.onSortClicked()

        viewModel.onSortSheetDismissed()

        assertEquals(false, viewModel.isSortSheetVisible.value)
    }

    @Test
    fun `sort sheet closes when an order is selected even if it is the same order`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(emptyList())))
        viewModel.onSortClicked()

        viewModel.onSortOrderSelected(SortOrder.DESCENDING)

        assertEquals(false, viewModel.isSortSheetVisible.value)
    }

    // Out of bound and extreme values

    @Test
    fun `emits empty success when repository returns no stocks`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(emptyList())))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(StockListUiState.Success(emptyList()), awaitItem())
        }
    }

    @Test
    fun `emits a single stock when repository returns one stock`() = runTest {
        val viewModel = StockListViewModel(FakeRepository(Result.success(listOf(stock))))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(1, (awaitItem() as StockListUiState.Success).stocks.size)
        }
    }

    @Test
    fun `converts a large list and sorts it by code descending`() = runTest {
        val stocks = List(LARGE_LIST_SIZE) { index -> stock.copy(code = index.toString()) }
        val viewModel = StockListViewModel(FakeRepository(Result.success(stocks)))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            val result = (awaitItem() as StockListUiState.Success).stocks
            assertEquals(LARGE_LIST_SIZE, result.size)
            assertEquals("999", result.first().code)
            assertEquals("0", result.last().code)
        }
    }

    @Test
    fun `converts a stock with all optional values missing`() = runTest {
        val emptyStock = Stock(
            code = "00625K",
            name = "No trades",
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
        val viewModel = StockListViewModel(FakeRepository(Result.success(listOf(emptyStock))))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            val item = (awaitItem() as StockListUiState.Success).stocks.single()
            assertEquals("-", item.closingPrice)
            assertEquals("-", item.change)
            assertEquals("-", item.tradeValue)
            assertEquals(PriceTrend.FLAT, item.closingPriceTrend)
            assertEquals(PriceTrend.FLAT, item.changeTrend)
        }
    }

    private companion object {
        const val LARGE_LIST_SIZE = 2_000
    }

    private class FakeRepository(var result: Result<List<Stock>>) : StockRepository {
        var callCount = 0
            private set

        /** When set, replaces the result so a test can hold the request open. */
        var onGetStocks: (suspend () -> List<Stock>)? = null

        override suspend fun getStocks(): List<Stock> {
            callCount++
            return onGetStocks?.invoke() ?: result.getOrThrow()
        }
    }
}
