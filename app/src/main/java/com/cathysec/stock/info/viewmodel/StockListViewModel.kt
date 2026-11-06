package com.cathysec.stock.info.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cathysec.stock.info.data.StockRepository
import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.model.SortOrder
import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.util.LoadErrorUtil
import com.cathysec.stock.info.util.StockSortUtil
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

    private val _sortOrder = MutableStateFlow(StockSortUtil.DEFAULT_ORDER)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Kept in the ViewModel so the sort sheet survives a configuration change.
    private val _isSortSheetVisible = MutableStateFlow(false)
    val isSortSheetVisible: StateFlow<Boolean> = _isSortSheetVisible.asStateFlow()

    // The latest loaded stocks in API order, so changing the sort order does not need a new request.
    private var loadedStocks: List<Stock> = emptyList()

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

    fun onSortClicked() {
        _isSortSheetVisible.value = true
    }

    fun onSortSheetDismissed() {
        _isSortSheetVisible.value = false
    }

    /** Applies [order] to the current list and closes the sort sheet. */
    fun onSortOrderSelected(order: SortOrder) {
        _isSortSheetVisible.value = false
        if (order == _sortOrder.value) return
        _sortOrder.value = order
        val current = _uiState.value as? StockListUiState.Success ?: return
        _uiState.value = current.copy(stocks = sortedUiModels())
    }

    private fun isLoading() = loadJob?.isActive == true

    private suspend fun fetchStocks(): List<StockUiModel> {
        loadedStocks = repository.getStocks()
        return sortedUiModels()
    }

    private fun sortedUiModels() = StockSortUtil.sortByCode(loadedStocks, _sortOrder.value).map(Stock::toUiModel)
}
