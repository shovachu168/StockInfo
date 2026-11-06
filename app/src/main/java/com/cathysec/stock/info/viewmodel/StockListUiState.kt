package com.cathysec.stock.info.viewmodel

import com.cathysec.stock.info.model.LoadError

sealed interface StockListUiState {

    /** First load or retry: no list is shown. */
    data object Loading : StockListUiState

    /** The list is shown. [isRefreshing] is true while a pull-to-refresh is running. */
    data class Success(
        val stocks: List<StockUiModel>,
        val isRefreshing: Boolean = false,
    ) : StockListUiState

    /** Loading failed and there is no list to show. */
    data class Error(val error: LoadError) : StockListUiState
}
