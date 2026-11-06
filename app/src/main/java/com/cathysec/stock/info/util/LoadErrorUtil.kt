package com.cathysec.stock.info.util

import com.cathysec.stock.info.model.LoadError
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

/**
 * Classifies the exception of a failed request into a [LoadError].
 */
object LoadErrorUtil {

    /**
     * Looks at [throwable] and its causes, because libraries often wrap the real exception.
     * [InterruptedIOException] covers both the socket timeout and the OkHttp call timeout.
     */
    fun classify(throwable: Throwable): LoadError {
        val causes = generateSequence(throwable) { it.cause }.take(MAX_CAUSE_DEPTH).toList()
        return when {
            causes.any { it is InterruptedIOException } -> LoadError.TIMEOUT
            causes.any { it is UnknownHostException || it is ConnectException || it is NoRouteToHostException } ->
                LoadError.NO_CONNECTION
            else -> LoadError.GENERIC
        }
    }

    // Guards against a cyclic cause chain.
    private const val MAX_CAUSE_DEPTH = 10
}
