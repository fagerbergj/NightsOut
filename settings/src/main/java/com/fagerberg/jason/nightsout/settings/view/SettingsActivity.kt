package com.fagerberg.jason.nightsout.settings.view

import android.os.Bundle
import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.nightsout.settings.R

class SettingsActivity : NightsOutActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }
}
