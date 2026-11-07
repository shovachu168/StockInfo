package com.cathysec.stock.info.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats stock values for display.
 *
 * - Prices: thousands separators and two decimal places, e.g. `2,320.00`
 * - Change: same as prices with an explicit `+` for positive values, e.g. `+40.00`
 * - Ratios (P/E, P/B): same as prices, e.g. `26.20`
 * - Yield: a ratio with a percent sign, e.g. `3.12%`
 * - Counts: thousands separators without decimals, e.g. `508,399`
 * - Missing values are shown as [PLACEHOLDER]
 *
 * Values are truncated toward zero (not rounded), the same as StockParseUtil. Symbols are fixed to [Locale.US] so the output
 * does not depend on the device locale.
 */
object StockFormatUtil {

    const val PLACEHOLDER = "-"

    private const val PRICE_SCALE = 2
    private const val PRICE_PATTERN = "#,##0.00"
    private const val COUNT_PATTERN = "#,##0"
    private const val POSITIVE_SIGN = "+"
    private const val PERCENT_SIGN = "%"

    fun formatPrice(price: BigDecimal?): String =
        price?.let { priceFormat().format(it.roundToPriceScale()) } ?: PLACEHOLDER

    fun formatRatio(ratio: BigDecimal?): String = formatPrice(ratio)

    fun formatYield(percent: BigDecimal?): String =
        percent?.let { formatPrice(it) + PERCENT_SIGN } ?: PLACEHOLDER

    fun formatChange(change: BigDecimal?): String {
        change ?: return PLACEHOLDER
        // Truncate first so tiny values such as -0.001 are shown as 0.00 instead of -0.00.
        val rounded = change.roundToPriceScale()
        val formatted = priceFormat().format(rounded)
        return if (rounded.signum() > 0) POSITIVE_SIGN + formatted else formatted
    }

    fun formatCount(count: Long?): String =
        count?.let { countFormat().format(it) } ?: PLACEHOLDER

    private fun BigDecimal.roundToPriceScale(): BigDecimal = setScale(PRICE_SCALE, RoundingMode.DOWN)

    // DecimalFormat is not thread-safe and expensive to create (thousands of items are formatted
    // when the list loads), so each thread keeps and reuses its own instance.
    private val priceFormat = ThreadLocal.withInitial { DecimalFormat(PRICE_PATTERN, DecimalFormatSymbols(Locale.US)) }

    private val countFormat = ThreadLocal.withInitial { DecimalFormat(COUNT_PATTERN, DecimalFormatSymbols(Locale.US)) }

    private fun priceFormat(): DecimalFormat = priceFormat.get()!!

    private fun countFormat(): DecimalFormat = countFormat.get()!!
}
