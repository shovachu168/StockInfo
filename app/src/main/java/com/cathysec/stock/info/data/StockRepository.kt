package com.cathysec.stock.info.data

import com.cathysec.stock.info.model.Stock

interface StockRepository {

    /**
     * Returns the latest daily trading information of all listed stocks.
     * Throws an exception when the data cannot be loaded.
     */
    suspend fun getStocks(): List<Stock>
}
