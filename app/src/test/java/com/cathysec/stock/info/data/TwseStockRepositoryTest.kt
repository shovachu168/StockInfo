package com.cathysec.stock.info.data

import com.cathysec.stock.info.data.response.StockDayAverageResponse
import com.cathysec.stock.info.data.response.StockDayResponse
import com.cathysec.stock.info.data.response.StockValuationResponse
import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.model.ValuationsState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class TwseStockRepositoryTest {

    private val api = mockk<TwseApiService>()
    private val repository = TwseStockRepository(api)

    private val day = StockDayResponse(
        code = "2330",
        name = "TSMC",
        closingPrice = "2260.00",
        change = "-35.0000"
    )
    private val average =
        StockDayAverageResponse(code = "2330", name = "TSMC", monthlyAveragePrice = "2295.00")

    // ---- correct ----

    @Test
    fun `getStocks merges both responses`() = runTest {
        coEvery { api.getStockDayAll() } returns listOf(day)
        coEvery { api.getStockDayAverageAll() } returns listOf(average)

        val stock = repository.getStocks().single()

        assertEquals("2330", stock.code)
        assertEquals(BigDecimal("2260.00"), stock.closingPrice)
        assertEquals(BigDecimal("2295.00"), stock.monthlyAveragePrice)
    }

    @Test
    fun `getStocks requests both apis in parallel`() = runTest {
        val dayGate = CompletableDeferred<List<StockDayResponse>>()
        val averageGate = CompletableDeferred<List<StockDayAverageResponse>>()
        var averageRequested = false
        coEvery { api.getStockDayAll() } coAnswers { dayGate.await() }
        coEvery { api.getStockDayAverageAll() } coAnswers {
            averageRequested = true
            averageGate.await()
        }

        val result = async { repository.getStocks() }
        runCurrent()

        // The second request starts while the first one is still waiting.
        assertTrue(averageRequested)
        assertFalse(result.isCompleted)

        dayGate.complete(listOf(day))
        averageGate.complete(listOf(average))
        advanceUntilIdle()
        assertEquals(1, result.await().size)
    }

    // ---- error ----

    @Test
    fun `getStocks throws when the daily request fails`() = runTest {
        coEvery { api.getStockDayAll() } throws IOException("network down")
        coEvery { api.getStockDayAverageAll() } returns listOf(average)

        assertThrows(IOException::class.java) { runBlockingGetStocks() }
    }

    @Test
    fun `getStocks throws when the average request fails`() = runTest {
        coEvery { api.getStockDayAll() } returns listOf(day)
        coEvery { api.getStockDayAverageAll() } throws IOException("timeout")

        assertThrows(IOException::class.java) { runBlockingGetStocks() }
    }

    @Test
    fun `getStocks cancels the other request when one fails`() = runTest {
        var dayCancelled = false
        coEvery { api.getStockDayAll() } coAnswers {
            try {
                awaitCancellation()
            } finally {
                dayCancelled = true
            }
        }
        coEvery { api.getStockDayAverageAll() } throws IOException("boom")

        runCatching { repository.getStocks() }

        assertTrue(dayCancelled)
    }

    // ---- extreme values ----
    // Number parsing and rounding boundaries are covered by StockParseUtilTest and StockMergeUtilTest.

    // ---- out of bound ----

    @Test
    fun `getStocks returns an empty list when both responses are empty`() = runTest {
        coEvery { api.getStockDayAll() } returns emptyList()
        coEvery { api.getStockDayAverageAll() } returns emptyList()

        assertTrue(repository.getStocks().isEmpty())
    }

    @Test
    fun `getStocks keeps stocks with null monthly average when the average list is empty`() = runTest {
        coEvery { api.getStockDayAll() } returns listOf(day)
        coEvery { api.getStockDayAverageAll() } returns emptyList()

        val stock = repository.getStocks().single()

        assertEquals(null, stock.monthlyAveragePrice)
    }

    private fun runBlockingGetStocks() = kotlinx.coroutines.runBlocking { repository.getStocks() }

    // ---- valuations ----

    private val valuation = StockValuationResponse(
        code = "2330", name = "TSMC", peRatio = "26.20", dividendYield = "1.50", pbRatio = "6.10",
    )

    @Test
    fun `valuations starts as loading`() {
        assertEquals(ValuationsState.Loading, repository.valuations.value)
    }

    @Test
    fun `loadValuations publishes the parsed valuations by code`() = runTest {
        coEvery { api.getStockValuationAll() } returns listOf(valuation)

        repository.loadValuations()

        val loaded = repository.valuations.value as ValuationsState.Loaded
        assertEquals(BigDecimal("26.20"), loaded.valuations.getValue("2330").peRatio)
    }

    @Test
    fun `loadValuations reports the error kind instead of throwing`() = runTest {
        coEvery { api.getStockValuationAll() } throws SocketTimeoutException("slow")
        repository.loadValuations()
        assertEquals(ValuationsState.Failed(LoadError.TIMEOUT), repository.valuations.value)

        coEvery { api.getStockValuationAll() } throws UnknownHostException("offline")
        repository.loadValuations()
        assertEquals(ValuationsState.Failed(LoadError.NO_CONNECTION), repository.valuations.value)

        coEvery { api.getStockValuationAll() } throws IllegalStateException("bad data")
        repository.loadValuations()
        assertEquals(ValuationsState.Failed(LoadError.GENERIC), repository.valuations.value)
    }

    @Test
    fun `loadValuations retries after a failure`() = runTest {
        coEvery { api.getStockValuationAll() } throws IOException("down")
        repository.loadValuations()

        coEvery { api.getStockValuationAll() } returns listOf(valuation)
        repository.loadValuations()

        assertTrue(repository.valuations.value is ValuationsState.Loaded)
    }

    @Test
    fun `loadValuations does not request again once loaded`() = runTest {
        coEvery { api.getStockValuationAll() } returns listOf(valuation)

        repository.loadValuations()
        repository.loadValuations()

        coVerify(exactly = 1) { api.getStockValuationAll() }
    }

    @Test
    fun `loadValuations ignores a call while a request is running`() = runTest {
        val gate = CompletableDeferred<List<StockValuationResponse>>()
        coEvery { api.getStockValuationAll() } coAnswers { gate.await() }

        val first = async { repository.loadValuations() }
        runCurrent()
        repository.loadValuations()

        assertEquals(ValuationsState.Loading, repository.valuations.value)
        coVerify(exactly = 1) { api.getStockValuationAll() }
        gate.complete(listOf(valuation))
        first.await()
        assertTrue(repository.valuations.value is ValuationsState.Loaded)
    }

    @Test
    fun `loadValuations rethrows cancellation and can run again`() = runTest {
        coEvery { api.getStockValuationAll() } coAnswers { awaitCancellation() }
        val job = async { repository.loadValuations() }
        runCurrent()
        job.cancel()
        runCurrent()

        coEvery { api.getStockValuationAll() } returns listOf(valuation)
        repository.loadValuations()

        assertTrue(repository.valuations.value is ValuationsState.Loaded)
    }

    @Test
    fun `loadValuations failure does not affect getStocks`() = runTest {
        coEvery { api.getStockValuationAll() } throws IOException("down")
        coEvery { api.getStockDayAll() } returns listOf(day)
        coEvery { api.getStockDayAverageAll() } returns listOf(average)

        repository.loadValuations()

        assertEquals(1, repository.getStocks().size)
    }

    // Extreme values: parsing boundaries are covered by StockMergeUtilTest.

    @Test
    fun `loadValuations publishes an empty map for an empty response`() = runTest {
        coEvery { api.getStockValuationAll() } returns emptyList()

        repository.loadValuations()

        assertEquals(ValuationsState.Loaded(emptyMap()), repository.valuations.value)
    }

    @Test
    fun `loadValuations reports a http error as generic`() = runTest {
        val response = retrofit2.Response.error<List<StockValuationResponse>>(
            500, okhttp3.ResponseBody.create(null, ""),
        )
        coEvery { api.getStockValuationAll() } throws HttpException(response)

        repository.loadValuations()

        assertEquals(ValuationsState.Failed(LoadError.GENERIC), repository.valuations.value)
    }
}
