package com.wit.jasonfagerberg.nightsout.test

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

class NightsOutTestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NightsOutTestApp)
            modules(testAppModule)
        }
    }
}
