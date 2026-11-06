package com.cathysec.stock.info.model

/**
 * Why loading the stock list failed, used to choose the message shown to the user.
 */
enum class LoadError {
    /** The device cannot reach the server, e.g. offline or DNS failure. */
    NO_CONNECTION,

    /** The server did not answer in time, usually a slow network. */
    TIMEOUT,

    /** Any other failure, e.g. an HTTP error or malformed data. */
    GENERIC,
}
