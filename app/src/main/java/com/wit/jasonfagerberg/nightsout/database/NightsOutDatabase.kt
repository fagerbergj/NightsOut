package com.wit.jasonfagerberg.nightsout.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wit.jasonfagerberg.nightsout.constants.Constants

@Database(
    entities = [DrinkEntity::class, LogEntity::class],
    version = Constants.DB_VERSION,
    exportSchema = false
)
abstract class NightsOutDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun logDao(): LogDao
}
