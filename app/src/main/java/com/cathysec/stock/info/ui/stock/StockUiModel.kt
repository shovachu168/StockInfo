package com.cathysec.stock.info.ui.stock

/**
 * Display data for a stock card. All values are already formatted for display.
 */
data class StockUiModel(
    val code: String,
    val name: String,
    val openingPrice: String,
    val closingPrice: String,
    val highestPrice: String,
    val lowestPrice: String,
    val change: String,
    val monthlyAveragePrice: String,
    val transaction: String,
    val tradeVolume: String,
    val tradeValue: String,
)
