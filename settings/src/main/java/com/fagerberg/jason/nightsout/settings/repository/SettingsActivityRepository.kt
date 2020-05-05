package com.fagerberg.jason.nightsout.settings.repository

import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.common.android.NightsOutSharedPreferences
import com.fagerberg.jason.common.android.getNightsOutSharedPreferences
import com.fagerberg.jason.nightsout.settings.R
import io.reactivex.Observable

class SettingsActivityRepository(private val activity: NightsOutActivity) {

    private lateinit var sharedPreferences: NightsOutSharedPreferences

    fun getSharedPrefs(): Observable<NightsOutSharedPreferences> =
        Observable.just(activity.getNightsOutSharedPreferences()).doOnNext { sharedPreferences = it }

    fun updateShowNotification(isOn: Boolean): Observable<NightsOutSharedPreferences> =
        Observable.just(sharedPreferences.update(showBacNotification = isOn))
            .doOnNext { sharedPreferences = it }

    fun updateTheme(isDarkTheme: Boolean) =
        Observable.just(sharedPreferences.update(activeTheme = if (isDarkTheme) R.style.DarkAppTheme else R.style.AppTheme))
            .doOnNext { sharedPreferences = it }

    fun updateUse24HourTime(isOn: Boolean) =
        Observable.just(sharedPreferences.update(use24HourTime = isOn))
            .doOnNext { sharedPreferences = it }
}
