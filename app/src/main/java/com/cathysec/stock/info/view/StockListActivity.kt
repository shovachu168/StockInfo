package com.cathysec.stock.info.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cathysec.stock.info.databinding.ActivityStockListBinding
import com.cathysec.stock.info.viewmodel.StockListUiState
import com.cathysec.stock.info.viewmodel.StockListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StockListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockListBinding
    private val viewModel: StockListViewModel by viewModel()
    private val stockAdapter = StockAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupStockList()
        observeUiState()
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
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: StockListUiState) {
        when (state) {
            is StockListUiState.Success -> stockAdapter.submitList(state.stocks)
            // Loading and error views will be added when the API is integrated.
            StockListUiState.Loading, StockListUiState.Error -> Unit
        }
    }
}
