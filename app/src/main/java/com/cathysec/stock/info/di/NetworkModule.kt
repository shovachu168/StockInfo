package com.cathysec.stock.info.di

import com.cathysec.stock.info.BuildConfig
import com.cathysec.stock.info.data.TwseApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

// STOCK_DAY_AVG_ALL returns about 3 MB, so the read timeout is longer than the default 10 seconds.
private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val READ_TIMEOUT_SECONDS = 30L

/**
 * Koin module for networking: OkHttp client and the TWSE API service.
 */
val networkModule = module {
    single { createOkHttpClient() }
    single { TwseApiService.create(client = get()) }
}

private fun createOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                // BASIC only: logging full bodies of several MB would flood Logcat.
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()
