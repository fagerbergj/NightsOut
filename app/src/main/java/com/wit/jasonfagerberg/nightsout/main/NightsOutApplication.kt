package com.wit.jasonfagerberg.nightsout.main

import android.app.Application
import android.app.Activity
import com.jakewharton.threetenabp.AndroidThreeTen

class NightsOutApplication : Application() {
    var mCurrentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        // required by material-calendarview 2.x
        AndroidThreeTen.init(this)
    }
}
