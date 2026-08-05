package com.wit.jasonfagerberg.nightsout.test

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

// Replaces the production Application under test so Room and DataStore never start.
// Koin is started empty: the screen tests pass their state and callbacks directly.
class NightsOutTestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NightsOutTestApp)
            modules(module { })
        }
    }
}
