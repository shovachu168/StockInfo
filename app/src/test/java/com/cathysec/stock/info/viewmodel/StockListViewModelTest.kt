package com.cathysec.stock.info.viewmodel

import app.cash.turbine.test
import com.cathysec.stock.info.data.StockRepository
import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
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
            assertEquals(StockListUiState.Error, awaitItem())
        }
    }

    @Test
    fun `loadStocks retries and emits stocks after a previous failure`() = runTest {
        val repository = FakeRepository(Result.failure(IOException("network error")))
        val viewModel = StockListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(StockListUiState.Error, awaitItem())

            repository.result = Result.success(listOf(stock))
            viewModel.loadStocks()

            assertEquals(StockListUiState.Loading, awaitItem())
            assertEquals(1, (awaitItem() as StockListUiState.Success).stocks.size)
        }
        assertEquals(2, repository.callCount)
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
    fun `converts a large list and keeps the original order`() = runTest {
        val stocks = List(LARGE_LIST_SIZE) { index -> stock.copy(code = index.toString()) }
        val viewModel = StockListViewModel(FakeRepository(Result.success(stocks)))

        viewModel.uiState.test {
            assertEquals(StockListUiState.Loading, awaitItem())
            val result = (awaitItem() as StockListUiState.Success).stocks
            assertEquals(LARGE_LIST_SIZE, result.size)
            assertEquals("0", result.first().code)
            assertEquals((LARGE_LIST_SIZE - 1).toString(), result.last().code)
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
        }
    }

    private companion object {
        const val LARGE_LIST_SIZE = 2_000
    }

    private class FakeRepository(var result: Result<List<Stock>>) : StockRepository {
        var callCount = 0
            private set

        override suspend fun getStocks(): List<Stock> {
            callCount++
            return result.getOrThrow()
        }
    }
}
