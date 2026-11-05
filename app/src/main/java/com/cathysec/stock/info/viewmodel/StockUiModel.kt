package com.cathysec.stock.info.viewmodel

import com.cathysec.stock.info.model.PriceTrend
import com.cathysec.stock.info.model.Stock
import com.cathysec.stock.info.util.PriceTrendUtil
import com.cathysec.stock.info.util.StockFormatUtil

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
    /** Closing price compared with the monthly average price. */
    val closingPriceTrend: PriceTrend,
    /** Sign of the price change. */
    val changeTrend: PriceTrend,
)

fun Stock.toUiModel() = StockUiModel(
    code = code,
    name = name,
    openingPrice = StockFormatUtil.formatPrice(openingPrice),
    closingPrice = StockFormatUtil.formatPrice(closingPrice),
    highestPrice = StockFormatUtil.formatPrice(highestPrice),
    lowestPrice = StockFormatUtil.formatPrice(lowestPrice),
    change = StockFormatUtil.formatChange(change),
    monthlyAveragePrice = StockFormatUtil.formatPrice(monthlyAveragePrice),
    transaction = StockFormatUtil.formatCount(transaction),
    tradeVolume = StockFormatUtil.formatCount(tradeVolume),
    tradeValue = StockFormatUtil.formatCount(tradeValue),
    closingPriceTrend = PriceTrendUtil.closingPriceTrend(closingPrice, monthlyAveragePrice),
    changeTrend = PriceTrendUtil.changeTrend(change),
)
