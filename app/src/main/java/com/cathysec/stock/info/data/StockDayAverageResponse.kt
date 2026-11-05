package com.cathysec.stock.info.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * One item of `/exchangeReport/STOCK_DAY_AVG_ALL`: closing price and monthly average price.
 * Values are kept as raw strings from the API; parsing happens when the data is merged.
 */
@JsonClass(generateAdapter = true)
data class StockDayAverageResponse(
    /** Trading date in ROC calendar, e.g. `1151104`. */
    @Json(name = "Date") val date: String? = null,
    @Json(name = "Code") val code: String,
    @Json(name = "Name") val name: String,
    @Json(name = "ClosingPrice") val closingPrice: String? = null,
    @Json(name = "MonthlyAveragePrice") val monthlyAveragePrice: String? = null,
)
