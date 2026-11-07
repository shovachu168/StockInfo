package com.cathysec.stock.info.data

import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.model.ValuationsState
import kotlinx.coroutines.flow.StateFlow

interface StockRepository {

    /**
     * Returns the latest daily trading information of all listed stocks.
     * Throws an exception when the data cannot be loaded.
     */
    suspend fun getStocks(): List<Stock>

    /** Valuation figures (P/E, yield, P/B) of all stocks. Loaded independently of [getStocks]. */
    val valuations: StateFlow<ValuationsState>

    /**
     * Loads [valuations] and reports the result through it instead of throwing.
     * Does nothing when the data is already loaded or a request is running; calling it
     * again after a failure retries.
     */
    suspend fun loadValuations()
}
