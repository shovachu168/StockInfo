package com.cathysec.stock.info.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cathysec.stock.info.data.StockRepository
import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.util.LoadErrorUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StockListViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StockListUiState>(StockListUiState.Loading)
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    private val _refreshErrors = Channel<LoadError>(Channel.BUFFERED)

    /** One-time events: a pull-to-refresh failed while the old list stays on screen. */
    val refreshErrors: Flow<LoadError> = _refreshErrors.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        retry()
    }

    /**
     * Loads the list from scratch and shows the loading state. Used for the first load and
     * the retry button of the error screen. Ignored while another load is running.
     */
    fun retry() {
        if (isLoading()) return
        _uiState.value = StockListUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = try {
                StockListUiState.Success(fetchStocks())
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                StockListUiState.Error(LoadErrorUtil.classify(e))
            }
        }
    }

    /**
     * Reloads the list while keeping the current one on screen. If it fails, the old list
     * stays and [refreshErrors] emits. Without a list to keep, it behaves like [retry].
     * Ignored while another load is running.
     */
    fun refresh() {
        if (isLoading()) return
        val current = _uiState.value as? StockListUiState.Success
        if (current == null) {
            retry()
            return
        }
        _uiState.value = current.copy(isRefreshing = true)
        loadJob = viewModelScope.launch {
            try {
                _uiState.value = StockListUiState.Success(fetchStocks())
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                _uiState.value = current.copy(isRefreshing = false)
                _refreshErrors.send(LoadErrorUtil.classify(e))
            }
        }
    }

    private fun isLoading() = loadJob?.isActive == true

    private suspend fun fetchStocks() = repository.getStocks().map(Stock::toUiModel)
}
