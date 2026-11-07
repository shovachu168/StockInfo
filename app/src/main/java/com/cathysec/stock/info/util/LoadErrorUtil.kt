package com.cathysec.stock.info.util

import androidx.annotation.StringRes
import com.cathysec.stock.info.R
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

    /**
     * Returns the string resource of the message for [error], for screens where loading failed
     * and nothing else can be shown. Only the resource id is used, no Android class is involved.
     */
    @StringRes
    fun messageRes(error: LoadError): Int = when (error) {
        LoadError.NO_CONNECTION -> R.string.error_no_connection
        LoadError.TIMEOUT -> R.string.error_timeout
        LoadError.GENERIC -> R.string.error_generic
    }

    /**
     * Returns the string resource of the short message for [error], shown when a pull-to-refresh
     * fails and the previous list stays on screen.
     */
    @StringRes
    fun refreshFailedMessageRes(error: LoadError): Int = when (error) {
        LoadError.NO_CONNECTION -> R.string.refresh_failed_no_connection
        LoadError.TIMEOUT -> R.string.refresh_failed_timeout
        LoadError.GENERIC -> R.string.refresh_failed_generic
    }

    // Guards against a cyclic cause chain.
    private const val MAX_CAUSE_DEPTH = 10
}
