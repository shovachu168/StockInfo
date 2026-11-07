package com.cathysec.stock.info.util

import com.cathysec.stock.info.model.LoadError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoadErrorUtilTest {

    // ---- correct ----

    @Test
    fun `classify returns NO_CONNECTION for unknown host`() {
        assertEquals(LoadError.NO_CONNECTION, LoadErrorUtil.classify(UnknownHostException("openapi.twse.com.tw")))
    }

    @Test
    fun `classify returns NO_CONNECTION for connect and route failures`() {
        assertEquals(LoadError.NO_CONNECTION, LoadErrorUtil.classify(ConnectException("refused")))
        assertEquals(LoadError.NO_CONNECTION, LoadErrorUtil.classify(NoRouteToHostException("no route")))
    }

    @Test
    fun `classify returns TIMEOUT for socket timeout`() {
        assertEquals(LoadError.TIMEOUT, LoadErrorUtil.classify(SocketTimeoutException("read timed out")))
    }

    @Test
    fun `classify returns TIMEOUT for the OkHttp call timeout`() {
        assertEquals(LoadError.TIMEOUT, LoadErrorUtil.classify(InterruptedIOException("timeout")))
    }

    // ---- error ----

    @Test
    fun `classify returns GENERIC for other IO and runtime exceptions`() {
        assertEquals(LoadError.GENERIC, LoadErrorUtil.classify(IOException("unexpected end of stream")))
        assertEquals(LoadError.GENERIC, LoadErrorUtil.classify(IllegalStateException("bad data")))
    }

    @Test
    fun `classify returns GENERIC for an exception without message`() {
        assertEquals(LoadError.GENERIC, LoadErrorUtil.classify(RuntimeException()))
    }

    // ---- extreme values ----

    @Test
    fun `classify looks into the cause chain`() {
        val wrapped = RuntimeException("wrapper", IOException("io", UnknownHostException("host")))

        assertEquals(LoadError.NO_CONNECTION, LoadErrorUtil.classify(wrapped))
    }

    @Test
    fun `classify prefers TIMEOUT when both kinds appear in the cause chain`() {
        val mixed = UnknownHostException("host").apply { initCause(SocketTimeoutException("timeout")) }

        assertEquals(LoadError.TIMEOUT, LoadErrorUtil.classify(mixed))
    }

    @Test
    fun `classify does not loop forever on a cyclic cause chain`() {
        val first = RuntimeException("first")
        val second = RuntimeException("second", first)
        first.initCause(second)

        assertEquals(LoadError.GENERIC, LoadErrorUtil.classify(first))
    }

    // ---- messageRes ----

    @Test
    fun `messageRes returns a different message for every error kind`() {
        val ids = LoadError.entries.map { LoadErrorUtil.messageRes(it) }

        assertTrue(ids.all { it != 0 })
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `messageRes uses the dedicated message for each kind`() {
        assertNotEquals(
            LoadErrorUtil.messageRes(LoadError.NO_CONNECTION),
            LoadErrorUtil.messageRes(LoadError.GENERIC),
        )
    }

    @Test
    fun `refreshFailedMessageRes returns a different message for every error kind`() {
        val ids = LoadError.entries.map { LoadErrorUtil.refreshFailedMessageRes(it) }

        assertTrue(ids.all { it != 0 })
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `refreshFailedMessageRes differs from the full screen error message`() {
        LoadError.entries.forEach {
            assertNotEquals(LoadErrorUtil.messageRes(it), LoadErrorUtil.refreshFailedMessageRes(it))
        }
    }

    // ---- out of bound ----
    // The input is a single throwable, so there is no collection or numeric bound to test.
    // A very deep cause chain is covered by the cyclic chain test above.
}
