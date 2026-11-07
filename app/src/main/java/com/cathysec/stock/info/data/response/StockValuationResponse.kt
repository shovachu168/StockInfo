package com.cathysec.stock.info.data.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * One item of `/exchangeReport/BWIBBU_ALL`: P/E ratio, dividend yield and P/B ratio.
 * Values are kept as raw strings from the API; parsing happens when the data is merged.
 */
@JsonClass(generateAdapter = true)
data class StockValuationResponse(
    /** Trading date in ROC calendar, e.g. `1151104`. */
    @Json(name = "Date") val date: String? = null,
    @Json(name = "Code") val code: String,
    @Json(name = "Name") val name: String,
    @Json(name = "PEratio") val peRatio: String? = null,
    /** Dividend yield in percent. */
    @Json(name = "DividendYield") val dividendYield: String? = null,
    @Json(name = "PBratio") val pbRatio: String? = null,
)
