package com.wit.jasonfagerberg.nightsout.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.database.CREATE_LEGACY_TABLES
import com.wit.jasonfagerberg.nightsout.database.DatabaseAdoption
import com.wit.jasonfagerberg.nightsout.database.NightsOutDatabase
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.database.UuidMigration
import com.wit.jasonfagerberg.nightsout.home.HomeViewModel
import com.wit.jasonfagerberg.nightsout.profile.ProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        // rewrite the two entity tables of pre-Room installs before Room validates them
        DatabaseAdoption.normalizeIfNeeded(androidContext())
        Room.databaseBuilder<NightsOutDatabase>(androidContext(), Constants.DB_NAME)
            .createFromAsset("nights_out_db.db")
            .addMigrations(*UuidMigration.ALL)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(connection: SQLiteConnection) {
                    // asset-copy failure fallback: Room only creates entity tables itself
                    CREATE_LEGACY_TABLES.forEach { connection.execSQL(it) }
                }
            })
            .build()
    }
    single { get<NightsOutDatabase>().drinkDao() }
    single { get<NightsOutDatabase>().logDao() }
    single { NightsOutRepository(get(), get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}
