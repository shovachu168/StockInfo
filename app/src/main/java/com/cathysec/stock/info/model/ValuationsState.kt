package com.cathysec.stock.info.model

/**
 * Loading state of the valuation figures of all stocks.
 */
sealed interface ValuationsState {

    /** The request is running. This is also the initial state. */
    data object Loading : ValuationsState

    /** Valuations by stock code. */
    data class Loaded(val valuations: Map<String, StockValuation>) : ValuationsState

    data class Failed(val error: LoadError) : ValuationsState
}
