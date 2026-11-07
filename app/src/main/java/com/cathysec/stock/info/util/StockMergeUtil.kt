package com.cathysec.stock.info.util

import com.cathysec.stock.info.data.response.StockDayAverageResponse
import com.cathysec.stock.info.data.response.StockDayResponse
import com.cathysec.stock.info.data.response.StockValuationResponse
import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.model.StockValuation

/**
 * Merges the daily trading information with the monthly average prices and converts valuation data.
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

    /**
     * Converts the valuation items to a map by stock code. Values that are missing or cannot be parsed
     * become null. If a code appears more than once, the last item is used.
     */
    fun toValuations(responses: List<StockValuationResponse>): Map<String, StockValuation> =
        responses.associate { item ->
            item.code to StockValuation(
                code = item.code,
                peRatio = StockParseUtil.parsePrice(item.peRatio),
                dividendYield = StockParseUtil.parsePrice(item.dividendYield),
                pbRatio = StockParseUtil.parsePrice(item.pbRatio),
            )
        }
}
