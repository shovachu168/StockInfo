package com.cathysec.stock.info.ui.stock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cathysec.stock.info.databinding.ItemStockBinding

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
        }
    }

    private object StockDiffCallback : DiffUtil.ItemCallback<StockUiModel>() {
        override fun areItemsTheSame(oldItem: StockUiModel, newItem: StockUiModel) =
            oldItem.code == newItem.code

        override fun areContentsTheSame(oldItem: StockUiModel, newItem: StockUiModel) =
            oldItem == newItem
    }
}
