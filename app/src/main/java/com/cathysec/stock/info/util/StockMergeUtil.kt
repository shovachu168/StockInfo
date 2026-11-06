package com.cathysec.stock.info.util

import com.cathysec.stock.info.data.StockDayAverageResponse
import com.cathysec.stock.info.data.StockDayResponse
import com.cathysec.stock.info.model.Stock

/**
 * Merges the daily trading information with the monthly average prices.
 */
object StockMergeUtil {

    /**
     * Builds one [Stock] for each item of [days], keeping the order of [days].
     * - The closing price comes from [days] so the card shows and compares the same value.
     * - A stock without a matching item in [averages] gets a null monthly average price.
     * - Items that only exist in [averages] are dropped because they have no trading data.
     * - If a code appears more than once in [averages], the last item is used.
     */
    fun merge(days: List<StockDayResponse>, averages: List<StockDayAverageResponse>): List<Stock> {
        val monthlyAverageByCode = averages.associate { it.code to it.monthlyAveragePrice }
        return days.map { day ->
            Stock(
                code = day.code,
                name = day.name,
                openingPrice = StockParseUtil.parsePrice(day.openingPrice),
                closingPrice = StockParseUtil.parsePrice(day.closingPrice),
                highestPrice = StockParseUtil.parsePrice(day.highestPrice),
                lowestPrice = StockParseUtil.parsePrice(day.lowestPrice),
                change = StockParseUtil.parsePrice(day.change),
                monthlyAveragePrice = StockParseUtil.parsePrice(monthlyAverageByCode[day.code]),
                transaction = StockParseUtil.parseCount(day.transaction),
                tradeVolume = StockParseUtil.parseCount(day.tradeVolume),
                tradeValue = StockParseUtil.parseCount(day.tradeValue),
            )
        }
    }
}
