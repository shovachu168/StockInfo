package com.cathysec.stock.info.view

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cathysec.stock.info.R
import com.cathysec.stock.info.databinding.ActivityStockListBinding
import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.viewmodel.StockListUiState
import com.cathysec.stock.info.viewmodel.StockListViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StockListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockListBinding
    private val viewModel: StockListViewModel by viewModel()
    private val stockAdapter = StockAdapter()

    // The enter animation only plays when the list appears, not on every refresh.
    private var isListShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupStockList()
        setupStateViews()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        val list = binding.rvStocks
        val basePaddingLeft = list.paddingLeft
        val basePaddingRight = list.paddingRight
        val basePaddingBottom = list.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(list) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = basePaddingLeft + systemBars.left,
                right = basePaddingRight + systemBars.right,
                bottom = basePaddingBottom + systemBars.bottom,
            )
            insets
        }
    }

    private fun setupStockList() {
        binding.rvStocks.adapter = stockAdapter
        binding.rvStocks.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down)
    }

    private fun setupStateViews() {
        binding.swipeRefresh.setColorSchemeColors(MaterialColors.getColor(binding.root, androidx.appcompat.R.attr.colorPrimary))
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.btnRetry.setOnClickListener { viewModel.retry() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.refreshErrors.collect { error ->
                        Snackbar.make(binding.root, error.refreshFailedMessage(), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun render(state: StockListUiState) {
        binding.progress.visibility = visibleIf(state is StockListUiState.Loading)
        binding.swipeRefresh.visibility = visibleIf(state is StockListUiState.Success)
        binding.errorView.visibility = visibleIf(state is StockListUiState.Error)

        when (state) {
            is StockListUiState.Success -> {
                binding.swipeRefresh.isRefreshing = state.isRefreshing
                val playEnterAnimation = !isListShown
                isListShown = true
                stockAdapter.submitList(state.stocks) {
                    if (playEnterAnimation) {
                        binding.rvStocks.scrollToPosition(0)
                        binding.rvStocks.scheduleLayoutAnimation()
                    }
                }
            }
            is StockListUiState.Error -> {
                isListShown = false
                binding.tvErrorMessage.setText(state.error.errorMessage())
            }
            StockListUiState.Loading -> isListShown = false
        }
    }

    private fun visibleIf(condition: Boolean) = if (condition) View.VISIBLE else View.GONE

    @StringRes
    private fun LoadError.errorMessage() = when (this) {
        LoadError.NO_CONNECTION -> R.string.error_no_connection
        LoadError.TIMEOUT -> R.string.error_timeout
        LoadError.GENERIC -> R.string.error_generic
    }

    @StringRes
    private fun LoadError.refreshFailedMessage() = when (this) {
        LoadError.NO_CONNECTION -> R.string.refresh_failed_no_connection
        LoadError.TIMEOUT -> R.string.refresh_failed_timeout
        LoadError.GENERIC -> R.string.refresh_failed_generic
    }
}
