package com.cathysec.stock.info.di

import com.cathysec.stock.info.data.TwseApiService
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

/**
 * Verifies that every definition in the Koin module has its dependencies declared,
 * so a missing dependency fails here instead of crashing at runtime.
 */
@OptIn(KoinExperimentalAPI::class)
class AppModuleTest {

    @Test
    fun `appModule declares all dependencies`() {
        // TwseApiService is provided by networkModule.
        appModule.verify(extraTypes = listOf(TwseApiService::class))
    }

    @Test
    fun `networkModule declares all dependencies`() {
        networkModule.verify()
    }
}
