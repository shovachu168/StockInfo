package com.cathysec.stock.info.util

import com.cathysec.stock.info.model.SortOrder
import com.cathysec.stock.info.model.Stock

/**
 * Sorts stocks by stock code.
 */
object StockSortUtil {

    /** The order used when the user has not chosen one. */
    val DEFAULT_ORDER = SortOrder.DESCENDING

    /**
     * Returns a new list sorted by [Stock.code].
     *
     * Codes are compared as text, never as numbers, because they can have leading zeros or
     * letters (`0050`, `00625K`). Digits come before letters. The sort is stable: stocks with
     * the same code keep their original relative order in both directions.
     */
    fun sortByCode(stocks: List<Stock>, order: SortOrder): List<Stock> = when (order) {
        SortOrder.ASCENDING -> stocks.sortedBy { it.code }
        SortOrder.DESCENDING -> stocks.sortedByDescending { it.code }
    }
}
