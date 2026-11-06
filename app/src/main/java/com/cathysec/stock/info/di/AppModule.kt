package com.cathysec.stock.info.di

import com.cathysec.stock.info.data.StockRepository
import com.cathysec.stock.info.data.TwseStockRepository
import com.cathysec.stock.info.viewmodel.StockListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module of the app.
 */
val appModule = module {
    singleOf(::TwseStockRepository) bind StockRepository::class
    viewModelOf(::StockListViewModel)
}
