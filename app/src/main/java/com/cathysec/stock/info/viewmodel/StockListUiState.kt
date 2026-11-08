package com.cathysec.stock.info.viewmodel

import com.cathysec.stock.info.model.LoadError

sealed interface StockListUiState {

    /**
     * True only when a list is shown and no pull-to-refresh is running. Sorting is unavailable while
     * loading or on error (there is no list) and during a refresh (the list is about to be replaced).
     */
    val canSort: Boolean
        get() = this is Success && !isRefreshing

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
