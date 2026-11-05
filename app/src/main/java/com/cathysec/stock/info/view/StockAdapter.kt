package com.cathysec.stock.info.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cathysec.stock.info.R
import com.cathysec.stock.info.databinding.ItemStockBinding
import com.cathysec.stock.info.model.PriceTrend
import com.cathysec.stock.info.viewmodel.StockUiModel
import com.google.android.material.color.MaterialColors

class StockAdapter : ListAdapter<StockUiModel, StockAdapter.StockViewHolder>(StockDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StockViewHolder(
        private val binding: ItemStockBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StockUiModel) = with(binding) {
            tvCode.text = item.code
            tvName.text = item.name
            fieldOpeningPrice.value = item.openingPrice
            fieldClosingPrice.value = item.closingPrice
            fieldHighestPrice.value = item.highestPrice
            fieldLowestPrice.value = item.lowestPrice
            fieldChange.value = item.change
            fieldMonthlyAveragePrice.value = item.monthlyAveragePrice
            tvTransaction.text = item.transaction
            tvTradeVolume.text = item.tradeVolume
            tvTradeValue.text = item.tradeValue

            // Always set the color so recycled views do not keep the previous item's color.
            fieldClosingPrice.setValueColor(trendColor(item.closingPriceTrend))
            fieldChange.setValueColor(trendColor(item.changeTrend))
        }

        private fun trendColor(trend: PriceTrend): Int {
            val view = binding.root
            return when (trend) {
                PriceTrend.UP -> view.context.getColor(R.color.stock_up)
                PriceTrend.DOWN -> view.context.getColor(R.color.stock_down)
                PriceTrend.FLAT -> MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurface)
            }
        }
    }

    private object StockDiffCallback : DiffUtil.ItemCallback<StockUiModel>() {
        override fun areItemsTheSame(oldItem: StockUiModel, newItem: StockUiModel) =
            oldItem.code == newItem.code

        override fun areContentsTheSame(oldItem: StockUiModel, newItem: StockUiModel) =
            oldItem == newItem
    }
}
