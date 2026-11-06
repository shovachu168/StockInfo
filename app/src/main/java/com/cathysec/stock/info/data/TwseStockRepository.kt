package com.cathysec.stock.info.data

import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.util.StockMergeUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * [StockRepository] backed by the TWSE OpenAPI.
 *
 * The daily trading information and the monthly average prices are requested in parallel.
 * Both must succeed: if either request fails, the other one is cancelled and the exception
 * is thrown, so the list is never shown with missing monthly averages.
 */
class TwseStockRepository(
    private val api: TwseApiService,
) : StockRepository {

    override suspend fun getStocks(): List<Stock> = coroutineScope {
        val days = async { api.getStockDayAll() }
        val averages = async { api.getStockDayAverageAll() }
        StockMergeUtil.merge(days.await(), averages.await())
    }
}
