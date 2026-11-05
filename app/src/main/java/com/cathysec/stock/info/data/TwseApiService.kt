package com.cathysec.stock.info.data

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

/**
 * Taiwan Stock Exchange OpenAPI (https://openapi.twse.com.tw/).
 * Each call throws:
 * - [retrofit2.HttpException] for non-2xx responses
 * - [java.io.IOException] for network errors and malformed JSON
 * - [com.squareup.moshi.JsonDataException] when the JSON does not match the expected structure,
 *   e.g. a required field such as `Code` is missing
 */
interface TwseApiService {

    /** Daily trading information of all listed stocks. */
    @GET("exchangeReport/STOCK_DAY_ALL")
    suspend fun getStockDayAll(): List<StockDayResponse>

    /** Closing price and monthly average price of all listed stocks. */
    @GET("exchangeReport/STOCK_DAY_AVG_ALL")
    suspend fun getStockDayAverageAll(): List<StockDayAverageResponse>

    /** P/E ratio, dividend yield and P/B ratio of all listed stocks. */
    @GET("exchangeReport/BWIBBU_ALL")
    suspend fun getStockValuationAll(): List<StockValuationResponse>

    companion object {

        const val BASE_URL = "https://openapi.twse.com.tw/v1/"

        fun create(client: OkHttpClient, baseUrl: String = BASE_URL): TwseApiService =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
                .build()
                .create(TwseApiService::class.java)
    }
}
