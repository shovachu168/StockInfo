package com.cathysec.stock.info.data

import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.model.ValuationsState
import com.cathysec.stock.info.util.LoadErrorUtil
import com.cathysec.stock.info.util.StockMergeUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [StockRepository] backed by the TWSE OpenAPI.
 *
 * The daily trading information and the monthly average prices are requested in parallel.
 * Both must succeed: if either request fails, the other one is cancelled and the exception
 * is thrown, so the list is never shown with missing monthly averages.
 *
 * The valuation data is separate: its result is kept in [valuations] and never affects the list.
 */
class TwseStockRepository(
    private val api: TwseApiService,
) : StockRepository {

    private val _valuations = MutableStateFlow<ValuationsState>(ValuationsState.Loading)
    override val valuations: StateFlow<ValuationsState> = _valuations.asStateFlow()

    private val isValuationRequestRunning = AtomicBoolean(false)

    override suspend fun getStocks(): List<Stock> = coroutineScope {
        val days = async { api.getStockDayAll() }
        val averages = async { api.getStockDayAverageAll() }
        StockMergeUtil.merge(days.await(), averages.await())
    }

    override suspend fun loadValuations() {
        if (_valuations.value is ValuationsState.Loaded) return
        if (!isValuationRequestRunning.compareAndSet(false, true)) return
        try {
            _valuations.value = ValuationsState.Loading
            _valuations.value = try {
                ValuationsState.Loaded(StockMergeUtil.toValuations(api.getStockValuationAll()))
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                ValuationsState.Failed(LoadErrorUtil.classify(e))
            }
        } finally {
            isValuationRequestRunning.set(false)
        }
    }
}
