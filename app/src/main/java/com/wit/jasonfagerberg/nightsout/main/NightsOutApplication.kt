package com.wit.jasonfagerberg.nightsout.main

import android.app.Application
import android.app.Activity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.wit.jasonfagerberg.nightsout.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NightsOutApplication : Application() {
    var mCurrentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NightsOutApplication)
            modules(appModule)
        }
        // required by material-calendarview 2.x
        AndroidThreeTen.init(this)
    }
}
