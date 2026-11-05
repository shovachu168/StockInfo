package com.cathysec.stock.info.model

import java.math.BigDecimal

/**
 * Daily trading information of a listed stock.
 *
 * Prices use [BigDecimal] to avoid floating point errors. A null value means the data
 * is not available, e.g. the stock had no trades on that day.
 */
data class Stock(
    val code: String,
    val name: String,
    val openingPrice: BigDecimal?,
    val closingPrice: BigDecimal?,
    val highestPrice: BigDecimal?,
    val lowestPrice: BigDecimal?,
    val change: BigDecimal?,
    val monthlyAveragePrice: BigDecimal?,
    val transaction: Long?,
    val tradeVolume: Long?,
    val tradeValue: Long?,
)
