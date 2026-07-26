package com.wit.jasonfagerberg.nightsout.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.wit.jasonfagerberg.nightsout.settings.SettingsRepository
import com.wit.jasonfagerberg.nightsout.settings.settingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Added to the startKoin module list when this PR and the Koin scaffolding PR
// (NightsOutApplication + di/AppModule.kt) both land — one-line integration for the merger.
val settingsModule = module {
    single<DataStore<Preferences>> { androidContext().settingsDataStore }
    single { SettingsRepository(get()) }
}
