package com.cathysec.stock.info.data

import io.mockk.coEvery
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
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class TwseStockRepositoryTest {

    private val api = mockk<TwseApiService>()
    private val repository = TwseStockRepository(api)

    private val day = StockDayResponse(code = "2330", name = "TSMC", closingPrice = "2260.00", change = "-35.0000")
    private val average = StockDayAverageResponse(code = "2330", name = "TSMC", monthlyAveragePrice = "2295.00")

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
}
