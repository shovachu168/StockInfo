package com.cathysec.stock.info.viewmodel

sealed interface StockListUiState {

    data object Loading : StockListUiState

    data class Success(val stocks: List<StockUiModel>) : StockListUiState

    data object Error : StockListUiState
}
