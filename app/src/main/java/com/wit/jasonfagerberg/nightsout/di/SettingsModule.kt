package com.wit.jasonfagerberg.nightsout.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.wit.jasonfagerberg.nightsout.settings.SettingsRepository
import com.wit.jasonfagerberg.nightsout.settings.settingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsModule = module {
    single<DataStore<Preferences>> { androidContext().settingsDataStore }
    single { SettingsRepository(get()) }
}
