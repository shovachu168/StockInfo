package com.cathysec.stock.info.data

import com.cathysec.stock.info.data.response.StockDayAverageResponse
import com.cathysec.stock.info.data.response.StockDayResponse
import com.cathysec.stock.info.data.response.StockValuationResponse
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

class TwseApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TwseApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = TwseApiService.create(client = OkHttpClient(), baseUrl = server.url("/v1/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // Correct: request paths

    @Test
    fun `getStockDayAll requests STOCK_DAY_ALL`() = runTest {
        enqueueJson("[]")

        api.getStockDayAll()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v1/exchangeReport/STOCK_DAY_ALL", request.path)
    }

    @Test
    fun `getStockDayAverageAll requests STOCK_DAY_AVG_ALL`() = runTest {
        enqueueJson("[]")

        api.getStockDayAverageAll()

        assertEquals("/v1/exchangeReport/STOCK_DAY_AVG_ALL", server.takeRequest().path)
    }

    @Test
    fun `getStockValuationAll requests BWIBBU_ALL`() = runTest {
        enqueueJson("[]")

        api.getStockValuationAll()

        assertEquals("/v1/exchangeReport/BWIBBU_ALL", server.takeRequest().path)
    }

    // Correct: parsing real responses

    @Test
    fun `getStockDayAll parses every field`() = runTest {
        enqueueJson(readFixture("stock_day_all.json"))

        val tsmc = api.getStockDayAll().single { it.code == "2330" }

        assertEquals(
            StockDayResponse(
                date = "1151104",
                code = "2330",
                name = "台積電",
                tradeVolume = "53879371",
                tradeValue = "122293100290",
                openingPrice = "2320.00",
                highestPrice = "2320.00",
                lowestPrice = "2235.00",
                closingPrice = "2260.00",
                change = "-35.0000",
                transaction = "508399",
            ),
            tsmc,
        )
    }

    @Test
    fun `getStockDayAverageAll parses every field`() = runTest {
        enqueueJson(readFixture("stock_day_avg_all.json"))

        val tsmc = api.getStockDayAverageAll().single { it.code == "2330" }

        assertEquals(
            StockDayAverageResponse(
                date = "1151104",
                code = "2330",
                name = "台積電",
                closingPrice = "2260.00",
                monthlyAveragePrice = "2295.00",
            ),
            tsmc,
        )
    }

    @Test
    fun `getStockValuationAll parses every field`() = runTest {
        enqueueJson(readFixture("bwibbu_all.json"))

        val tsmc = api.getStockValuationAll().single { it.code == "2330" }

        assertEquals(
            StockValuationResponse(
                date = "1151104",
                code = "2330",
                name = "台積電",
                peRatio = "26.20",
                dividendYield = "0.97",
                pbRatio = "9.11",
            ),
            tsmc,
        )
    }

    @Test
    fun `responses keep the original order`() = runTest {
        enqueueJson(readFixture("stock_day_all.json"))

        assertEquals(listOf("00625K", "1101", "2330"), api.getStockDayAll().map { it.code })
    }

    // Error

    @Test
    fun `server error throws HttpException with the status code`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val error = assertFailsWith<HttpException> { api.getStockDayAll() }

        assertEquals(500, error.code())
    }

    @Test
    fun `not found throws HttpException`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        assertEquals(404, assertFailsWith<HttpException> { api.getStockValuationAll() }.code())
    }

    @Test
    fun `malformed JSON throws IOException`() = runTest {
        enqueueJson("""[{"Code": "2330", "Name": """)

        assertFailsWith<IOException> { api.getStockDayAll() }
    }

    @Test
    fun `missing required Code throws JsonDataException`() = runTest {
        enqueueJson("""[{"Name": "TSMC"}]""")

        assertFailsWith<JsonDataException> { api.getStockDayAll() }
    }

    @Test
    fun `null required Name throws JsonDataException`() = runTest {
        enqueueJson("""[{"Code": "2330", "Name": null}]""")

        assertFailsWith<JsonDataException> { api.getStockValuationAll() }
    }

    @Test
    fun `object instead of array throws JsonDataException`() = runTest {
        enqueueJson("""{"Code": "2330", "Name": "TSMC"}""")

        assertFailsWith<JsonDataException> { api.getStockDayAverageAll() }
    }

    @Test
    fun `dropped connection throws IOException`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertFailsWith<IOException> { api.getStockDayAll() }
    }

    // Extreme values

    @Test
    fun `empty strings are kept for stocks without trades`() = runTest {
        enqueueJson(readFixture("stock_day_all.json"))

        val noTrades = api.getStockDayAll().single { it.code == "00625K" }

        assertEquals("富邦上証+R", noTrades.name)
        assertEquals("", noTrades.openingPrice)
        assertEquals("", noTrades.closingPrice)
        assertEquals("", noTrades.tradeValue)
        assertEquals("0.0000", noTrades.change)
    }

    @Test
    fun `empty PE ratio is kept as an empty string`() = runTest {
        enqueueJson(readFixture("bwibbu_all.json"))

        val taiwanCement = api.getStockValuationAll().single { it.code == "1101" }

        assertEquals("", taiwanCement.peRatio)
        assertEquals("2.94", taiwanCement.dividendYield)
    }

    @Test
    fun `very long numeric strings are kept exactly`() = runTest {
        enqueueJson("""[{"Code": "9999", "Name": "Max", "TradeValue": "9223372036854775807", "ClosingPrice": "99999.99"}]""")

        val stock = api.getStockDayAll().single()

        assertEquals("9223372036854775807", stock.tradeValue)
        assertEquals("99999.99", stock.closingPrice)
    }

    @Test
    fun `unknown fields are ignored`() = runTest {
        enqueueJson("""[{"Code": "2330", "Name": "TSMC", "NewField": "value", "PEratio": "26.20"}]""")

        assertEquals("26.20", api.getStockValuationAll().single().peRatio)
    }

    @Test
    fun `parses a large response`() = runTest {
        val items = (0 until LARGE_RESPONSE_SIZE).joinToString(",") { index ->
            """{"Code": "$index", "Name": "Stock $index", "ClosingPrice": "100.00", "MonthlyAveragePrice": "99.50"}"""
        }
        enqueueJson("[$items]")

        val result = api.getStockDayAverageAll()

        assertEquals(LARGE_RESPONSE_SIZE, result.size)
        assertEquals((LARGE_RESPONSE_SIZE - 1).toString(), result.last().code)
    }

    // Out of bound

    @Test
    fun `empty array returns an empty list`() = runTest {
        enqueueJson("[]")

        assertTrue(api.getStockDayAll().isEmpty())
    }

    @Test
    fun `missing optional fields become null`() = runTest {
        enqueueJson("""[{"Code": "2330", "Name": "TSMC"}]""")

        val stock = api.getStockDayAll().single()

        assertNull(stock.date)
        assertNull(stock.closingPrice)
        assertNull(stock.change)
        assertNull(stock.tradeValue)
    }

    @Test
    fun `explicit null optional fields become null`() = runTest {
        enqueueJson("""[{"Code": "2330", "Name": "TSMC", "MonthlyAveragePrice": null}]""")

        assertNull(api.getStockDayAverageAll().single().monthlyAveragePrice)
    }

    @Test
    fun `empty body throws IOException`() = runTest {
        enqueueJson("")

        assertFailsWith<IOException> { api.getStockDayAll() }
    }

    private fun enqueueJson(body: String) {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(body))
    }

    private fun readFixture(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource("twse/$name")) { "Missing fixture: $name" }.readText()

    private suspend inline fun <reified T : Throwable> assertFailsWith(block: suspend () -> Unit): T {
        try {
            block()
        } catch (e: Throwable) {
            if (e is T) return e
            throw AssertionError("Expected ${T::class.java.simpleName} but was ${e::class.java.simpleName}", e)
        }
        fail("Expected ${T::class.java.simpleName} but nothing was thrown")
        throw AssertionError()
    }

    private companion object {
        const val LARGE_RESPONSE_SIZE = 2_000
    }
}
