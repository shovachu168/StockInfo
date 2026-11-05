package com.cathysec.stock.info.di

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
        appModule.verify()
    }

    @Test
    fun `networkModule declares all dependencies`() {
        networkModule.verify()
    }
}
