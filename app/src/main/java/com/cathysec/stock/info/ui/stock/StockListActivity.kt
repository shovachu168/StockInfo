package com.cathysec.stock.info.ui.stock

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.cathysec.stock.info.databinding.ActivityStockListBinding

class StockListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockListBinding
    private val stockAdapter = StockAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupStockList()
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
        stockAdapter.submitList(StockSampleData.stocks)
    }
}
