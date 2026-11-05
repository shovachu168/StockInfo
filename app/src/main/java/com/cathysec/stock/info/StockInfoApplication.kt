package com.cathysec.stock.info

import android.app.Application
import com.cathysec.stock.info.di.appModule
import com.cathysec.stock.info.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class StockInfoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@StockInfoApplication)
            modules(appModule, networkModule)
        }
    }
}
