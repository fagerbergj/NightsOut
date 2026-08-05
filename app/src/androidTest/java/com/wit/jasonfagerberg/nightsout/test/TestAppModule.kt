package com.wit.jasonfagerberg.nightsout.test

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module

/**
 * Minimal test DI module. Currently unused since SettingsScreenTest and other tests
 * construct ViewModels directly without going through Koin — but kept as scaffolding for
 * future tests that need repository mocks (HomeScreenBacTest, AddDrinkScreenTest).
 */
val testAppModule = module {
    // viewModel { HomeViewModel(get<NightsOutRepository>()) }
    // single<DataStore<Preferences>> { mockDataStore }
}
