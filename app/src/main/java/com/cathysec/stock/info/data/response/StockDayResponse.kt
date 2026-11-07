package com.cathysec.stock.info.data.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * One item of `/exchangeReport/STOCK_DAY_ALL`: daily trading information of a listed stock.
 * Values are kept as raw strings from the API; parsing happens when the data is merged.
 */
@JsonClass(generateAdapter = true)
data class StockDayResponse(
    /** Trading date in ROC calendar, e.g. `1151104`. */
    @Json(name = "Date") val date: String? = null,
    @Json(name = "Code") val code: String,
    @Json(name = "Name") val name: String,
    @Json(name = "TradeVolume") val tradeVolume: String? = null,
    @Json(name = "TradeValue") val tradeValue: String? = null,
    @Json(name = "OpeningPrice") val openingPrice: String? = null,
    @Json(name = "HighestPrice") val highestPrice: String? = null,
    @Json(name = "LowestPrice") val lowestPrice: String? = null,
    @Json(name = "ClosingPrice") val closingPrice: String? = null,
    @Json(name = "Change") val change: String? = null,
    @Json(name = "Transaction") val transaction: String? = null,
)
