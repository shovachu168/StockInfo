package com.cathysec.stock.info.model

import java.math.BigDecimal

/**
 * Valuation figures of a listed stock. A null value means the figure is not available,
 * e.g. a stock without earnings has no P/E ratio.
 */
data class StockValuation(
    val code: String,
    val peRatio: BigDecimal?,
    /** Dividend yield in percent. */
    val dividendYield: BigDecimal?,
    val pbRatio: BigDecimal?,
)
