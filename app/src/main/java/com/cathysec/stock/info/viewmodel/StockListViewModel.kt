package com.cathysec.stock.info.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cathysec.stock.info.data.StockRepository
import com.cathysec.stock.info.model.Stock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockListViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StockListUiState>(StockListUiState.Loading)
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    init {
        loadStocks()
    }

    fun loadStocks() {
        viewModelScope.launch {
            _uiState.value = StockListUiState.Loading
            _uiState.value = try {
                StockListUiState.Success(repository.getStocks().map(Stock::toUiModel))
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                StockListUiState.Error
            }
        }
    }
}
