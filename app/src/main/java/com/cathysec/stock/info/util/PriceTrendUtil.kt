package com.cathysec.stock.info.util

import com.cathysec.stock.info.model.PriceTrend
import java.math.BigDecimal

/**
 * Decides the price trend used to color stock values.
 * Missing values always result in [PriceTrend.FLAT].
 */
object PriceTrendUtil {

    /**
     * Trend of the closing price compared with the monthly average price:
     * above is [PriceTrend.UP], below is [PriceTrend.DOWN], equal is [PriceTrend.FLAT].
     */
    fun closingPriceTrend(closingPrice: BigDecimal?, monthlyAveragePrice: BigDecimal?): PriceTrend {
        if (closingPrice == null || monthlyAveragePrice == null) return PriceTrend.FLAT
        // compareTo ignores scale, so 2.0 and 2.00 are treated as equal (equals would not).
        return closingPrice.compareTo(monthlyAveragePrice).toTrend()
    }

    /**
     * Trend of the price change: positive is [PriceTrend.UP], negative is [PriceTrend.DOWN],
     * zero is [PriceTrend.FLAT].
     */
    fun changeTrend(change: BigDecimal?): PriceTrend = change?.signum()?.toTrend() ?: PriceTrend.FLAT

    private fun Int.toTrend(): PriceTrend = when {
        this > 0 -> PriceTrend.UP
        this < 0 -> PriceTrend.DOWN
        else -> PriceTrend.FLAT
    }
}
