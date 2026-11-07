package com.cathysec.stock.info.viewmodel

import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.model.StockValuation
import com.cathysec.stock.info.util.StockFormatUtil

/**
 * What the valuation dialog shows for the selected stock.
 */
sealed interface ValuationDialogUiState {

    val code: String
    val name: String

    /** The valuation data is still being fetched. */
    data class Loading(override val code: String, override val name: String) : ValuationDialogUiState

    /** All values are already formatted for display. */
    data class Loaded(
        override val code: String,
        override val name: String,
        val peRatio: String,
        val dividendYield: String,
        val pbRatio: String,
    ) : ValuationDialogUiState

    /** The data was loaded but contains no item for this stock. */
    data class NoData(override val code: String, override val name: String) : ValuationDialogUiState

    data class Failed(
        override val code: String,
        override val name: String,
        val error: LoadError,
    ) : ValuationDialogUiState
}

fun StockValuation.toDialogState(name: String) = ValuationDialogUiState.Loaded(
    code = code,
    name = name,
    peRatio = StockFormatUtil.formatRatio(peRatio),
    dividendYield = StockFormatUtil.formatYield(dividendYield),
    pbRatio = StockFormatUtil.formatRatio(pbRatio),
)
