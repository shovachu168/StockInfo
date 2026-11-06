package com.cathysec.stock.info.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Parses the raw strings returned by the TWSE API.
 *
 * The API uses an empty string (or `--`) for values that do not exist, e.g. a stock
 * with no trades on that day. All of them are parsed to null instead of throwing,
 * so one bad item never breaks the whole list.
 */
object StockParseUtil {

    private const val PRICE_SCALE = 2
    private val MISSING_MARKERS = setOf("--", "-", "N/A")

    /**
     * Parses a price and normalizes it to two decimal places (truncated toward zero, not rounded), so that the
     * price trend and the displayed text always agree. For example `-0.0001` becomes `0.00`
     * instead of a red-looking "0.00".
     */
    fun parsePrice(raw: String?): BigDecimal? =
        parseDecimal(raw)?.setScale(PRICE_SCALE, RoundingMode.DOWN)

    /** Parses an integer count such as trade volume. Decimals or values over [Long.MAX_VALUE] are invalid. */
    fun parseCount(raw: String?): Long? = cleaned(raw)?.toLongOrNull()

    private fun parseDecimal(raw: String?): BigDecimal? = cleaned(raw)?.toBigDecimalOrNull()

    private fun cleaned(raw: String?): String? {
        val text = raw?.trim().orEmpty()
        return if (text.isEmpty() || text in MISSING_MARKERS) null else text
    }
}
