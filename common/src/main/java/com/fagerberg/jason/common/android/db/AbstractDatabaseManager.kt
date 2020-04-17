package com.fagerberg.jason.common.android.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.fagerberg.jason.common.constants.DB_NAME
import com.fagerberg.jason.common.constants.DB_VERSION
import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.common.models.LogHeader
import com.fagerberg.jason.common.models.VolumeMeasurement
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

abstract class AbstractDatabaseManager(
    val context: Context,
    factory: SQLiteDatabase.CursorFactory
) : SQLiteOpenHelper(context, DB_NAME, factory, DB_VERSION) {

    val logTag = this::class.java.name

    private val path = "data/data/com.wit.jasonfagerberg.nightsout/$DB_NAME"
    lateinit var db: SQLiteDatabase

    // temp array for maintaining db after upgrade
    private val mAllDrinks = mutableListOf<Drink>()
    private val mIgnoredDrinks = mutableListOf<UUID>()
    private val mAllLoggedDrinks = mutableListOf<Pair<Int, UUID>>()

    fun openDatabase() {
        if (!dbExists()) {
            createDatabase()
        }

        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)

        if (db.version != DB_VERSION) {
            onUpgrade(db, db.version, DB_VERSION)
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        }
    }

    private fun dbExists() = File(path).exists()

    private fun createDatabase() {
        try {
            copyDatabase()
        } catch (e: IOException) {
            Log.e(logTag, "Failed to copy database")
            throw e
        }
    }

    private fun copyDatabase() {
        val inputStream = context.assets.open(DB_NAME)
        val outputStream = FileOutputStream(path)

        val buffer = ByteArray(1024)
        while (inputStream.read(buffer) > 0) outputStream.write(buffer)

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.version = newVersion
        // dont need to do any data saving since there is no data
        if (oldVersion == 0) return

        pullAllDrinks()
        val mDrinksList = pullCurrentSessionDrinks()
        val mFavoritesList = pullFavoriteDrinks()
        val mLogHeaders = pullLogHeaders()
        dropAllTables()
        rebuildTables()

//        pushAllDrinks()
//        pushDrinks(mDrinksList, mFavoritesList)
//        pushLogHeaders(mLogHeaders)
//        for (id in mIgnoredDrinks) updateDrinkSuggestionStatus(id, true)
        mDrinksList.clear()
        mFavoritesList.clear()
        mLogHeaders.clear()
    }

    private fun pullAllDrinks() {
        var cursor = db.query("drinks", null, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val dontSuggest = cursor.getInt(cursor.getColumnIndex("dontSuggest"))

            if (dontSuggest == 1) mIgnoredDrinks.add(id)
            mAllDrinks.add(
                Drink(
                    id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id"))),
                    name = drinkName,
                    abv = cursor.getDouble(cursor.getColumnIndex("abv")),
                    amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                    measurement = VolumeMeasurement.valueOf(cursor.getString(cursor.getColumnIndex("measurement"))),
                    favorited = isFavoritedInDB(drinkName),
                    recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1,
                    modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
                )
            )
        }
        cursor.close()

        cursor = db.query("log_drink", null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val logDate = cursor.getInt(cursor.getColumnIndex("log_date"))
            val drinkId = UUID.fromString(cursor.getString(cursor.getColumnIndex("drink_id")))
            mAllLoggedDrinks.add(Pair(logDate, drinkId))
        }
        cursor.close()
    }

    fun isFavoritedInDB(name: String): Boolean {
        val cursor = db.query("favorites", null, "drink_name = ?", arrayOf(name), null, null, null)
        val ret = cursor.count == 1
        cursor.close()
        return ret
    }

    fun pullCurrentSessionDrinks(): MutableList<Drink> {
        val drinks = mutableListOf<Drink>()
        val table = "drinks, current_session_drinks"
        val where = "drinks.id=current_session_drinks.drink_id"
        val order = "current_session_drinks.position ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)
        while (cursor.moveToNext()) {
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            drinks.add(
                Drink(
                    id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id"))),
                    name = drinkName,
                    abv = cursor.getDouble(cursor.getColumnIndex("abv")),
                    amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                    measurement = VolumeMeasurement.valueOf(cursor.getString(cursor.getColumnIndex("measurement"))),
                    favorited = isFavoritedInDB(drinkName),
                    recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1,
                    modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
                )
            )
        }
        cursor.close()
        return drinks
    }

    fun pullLogHeaders(): MutableList<LogHeader> {
        val headers = mutableListOf<LogHeader>()

        val cursor = db.query("log", null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            headers.add(
                LogHeader(
                    date = cursor.getInt(cursor.getColumnIndex("date")),
                    bac = cursor.getDouble(cursor.getColumnIndex("bac")),
                    duration = cursor.getDouble(cursor.getColumnIndex("duration"))
                )
            )
        }
        cursor.close()
        return headers
    }

    fun pullFavoriteDrinks(): MutableList<Drink> {
        val favorites = mutableListOf<Drink>()
        val table = "drinks, favorites"
        val where = "drinks.id=favorites.origin_id"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()) {
            favorites.add(0,
                Drink(
                    id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id"))),
                    name = cursor.getString(cursor.getColumnIndex("name")),
                    abv = cursor.getDouble(cursor.getColumnIndex("abv")),
                    amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                    measurement = VolumeMeasurement.valueOf(cursor.getString(cursor.getColumnIndex("measurement"))),
                    favorited = true,
                    recent = false,
                    modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
                )
            )
        }
        cursor.close()
        return favorites
    }

    private fun dropAllTables() {
        db.execSQL("DROP TABLE drinks")
        db.execSQL("DROP TABLE current_session_drinks")
        db.execSQL("DROP TABLE favorites")
        db.execSQL("DROP TABLE log")
        db.execSQL("DROP TABLE log_drink")
    }

    private fun rebuildTables() {
        db.execSQL("CREATE TABLE \"current_session_drinks\" ( `drink_id` TEXT, `position` INTEGER )")
        db.execSQL("CREATE TABLE \"drinks\" ( `id` TEXT UNIQUE, `name` TEXT, `abv` NUMERIC, `amount` NUMERIC, `measurement` TEXT, `recent` INTEGER, `modifiedTime` INTEGER, `dontSuggest` INTEGER, PRIMARY KEY(`id`) )")
        db.execSQL("CREATE TABLE \"favorites\" ( `drink_name` TEXT, `origin_id` TEXT )")
        db.execSQL("CREATE TABLE \"log\" ( `date` INTEGER UNIQUE, `bac` NUMERIC, `duration` INTEGER, PRIMARY KEY(`date`) )")
        db.execSQL("CREATE TABLE \"log_drink\" ( `log_date` NUMERIC, `drink_id` TEXT )")
    }



    fun closeDatabase() = db.close()
}
