package com.wit.jasonfagerberg.nightsout.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.fagerberg.jason.common.constants.DB_NAME
import com.fagerberg.jason.common.constants.DB_PATH
import com.fagerberg.jason.common.constants.DB_VERSION
import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.common.models.LogHeader
import com.fagerberg.jason.common.models.VolumeMeasurement
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

const val DRINKS_TABLE = "drinks"
const val CURRENT_SESSION_TABLE = "current_session_drinks"
const val FAVORITES_TABLE = "favorites"
const val LOG_TABLE = "log"
const val LOGGED_DRINKS_TABLE = "log_drink"

val ALL_TABLES = arrayOf(
    DRINKS_TABLE, CURRENT_SESSION_TABLE, FAVORITES_TABLE, LOG_TABLE, LOGGED_DRINKS_TABLE
)

class SimpleDatabaseManager(
    private val context: Context,
    factory: SQLiteDatabase.CursorFactory?,
    private val dbName: String = DB_NAME,
    private val dbPath: String = DB_PATH,
    private val dbVersion: Int = DB_VERSION
) : SQLiteOpenHelper(context, dbName, factory, dbVersion) {

    override fun onCreate(p0: SQLiteDatabase?) { /* no op */ }

    private val logTag = this::class.java.name

    private lateinit var db: SQLiteDatabase

    fun openDatabase() {
        if (!dbExists()) {
            writeDatabaseFile()
        }

        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)

        if (db.version != dbVersion) {
            onUpgrade(db, db.version, dbVersion)
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        }
    }

    private fun dbExists() = File(dbPath).exists()

    private fun writeDatabaseFile() {
        try {
            val inputStream = context.assets.open(dbName)
            val outputStream = FileOutputStream(dbPath)

            val buffer = ByteArray(1024)
            while (inputStream.read(buffer) > 0) outputStream.write(buffer)

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            Log.e(logTag, "Failed to copy database")
            throw e
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.version = newVersion
        // dont need to do any data saving since there is no data
        if (oldVersion == 0) return

        // Store database values in vars
        val allDrinks = readDrinks()
        val currentDrinks = readCurrentSessionDrinks()
        val favoritesList = readFavoriteDrinks()
        val loggedDrinks = readLoggedDrinkReferences()
        val logHeaders = readLogHeaders()

        dropAllTables()
        buildTables()

        writeDrinks(
            allDrinks = allDrinks,
            currentDrinks = currentDrinks,
            favoriteDrinks = favoritesList,
            loggedDrinks = loggedDrinks
        )
        writeLogHeaders(logHeaders)
    }


    private fun dropAllTables() {
        db.execSQL("DROP TABLE $DRINKS_TABLE")
        db.execSQL("DROP TABLE $CURRENT_SESSION_TABLE")
        db.execSQL("DROP TABLE $FAVORITES_TABLE")
        db.execSQL("DROP TABLE $LOG_TABLE")
        db.execSQL("DROP TABLE $LOGGED_DRINKS_TABLE")
    }

    private fun buildTables() {
        db.execSQL("CREATE TABLE \"$CURRENT_SESSION_TABLE\" ( `drink_id` TEXT, `position` INTEGER )")
        db.execSQL("CREATE TABLE \"$DRINKS_TABLE\" ( `id` TEXT UNIQUE, `name` TEXT, `abv` NUMERIC, `amount` NUMERIC, `measurement` TEXT, `recent` INTEGER, `modifiedTime` INTEGER, `dontSuggest` INTEGER, PRIMARY KEY(`id`) )")
        db.execSQL("CREATE TABLE \"$FAVORITES_TABLE\" ( `drink_name` TEXT, `origin_id` TEXT )")
        db.execSQL("CREATE TABLE \"$LOG_TABLE\" ( `date` INTEGER UNIQUE, `bac` NUMERIC, `duration` INTEGER, PRIMARY KEY(`date`) )")
        db.execSQL("CREATE TABLE \"$LOGGED_DRINKS_TABLE\" ( `log_date` NUMERIC, `drink_id` TEXT )")
    }

    fun readDrinks() : List<Drink> {
        val allDrinks = mutableListOf<Drink>()
        val cursor = db.query(DRINKS_TABLE, null, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            allDrinks.add(
                Drink(
                    id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id"))),
                    name = drinkName,
                    abv = cursor.getDouble(cursor.getColumnIndex("abv")),
                    amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                    measurement = VolumeMeasurement.fromLowercaseString((cursor.getString(cursor.getColumnIndex("measurement")))),
                    favorited = isFavoritedInDB(drinkName),
                    recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1,
                    modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime")),
                    dontSuggest = cursor.getInt(cursor.getColumnIndex("dontSuggest")) == 1
                )
            )
        }
        cursor.close()
        return allDrinks
    }

    fun readLoggedDrinks(date: Int): List<Drink> {
        val drinks = mutableListOf<Drink>()
        val table = "$DRINKS_TABLE, $LOGGED_DRINKS_TABLE"
        val where = "$DRINKS_TABLE.id=$LOGGED_DRINKS_TABLE.drink_id AND $LOGGED_DRINKS_TABLE.log_date=$date"
        val cursor = db.query(table, null, where, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            drinks.add(
                Drink(
                    id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id"))),
                    name = drinkName,
                    abv = cursor.getDouble(cursor.getColumnIndex("abv")),
                    amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                    measurement = VolumeMeasurement.fromLowercaseString(cursor.getString(cursor.getColumnIndex("measurement"))),
                    favorited = isFavoritedInDB(drinkName),
                    recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1,
                    modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
                )
            )
        }
        cursor.close()
        return drinks
    }

    private fun readLoggedDrinkReferences(): List<Pair<Int,UUID>> {
        val loggedDrinks = mutableListOf<Pair<Int,UUID>>()
        val cursor = db.query(LOGGED_DRINKS_TABLE, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val logDate = cursor.getInt(cursor.getColumnIndex("log_date"))
            val drinkId = UUID.fromString(cursor.getString(cursor.getColumnIndex("drink_id")))
            loggedDrinks.add(Pair(logDate, drinkId))
        }
        cursor.close()
        return loggedDrinks
    }

    fun isFavoritedInDB(name: String): Boolean {
        val cursor = db.query(FAVORITES_TABLE, null, "drink_name = ?", arrayOf(name), null, null, null)
        val ret = cursor.count == 1
        cursor.close()
        return ret
    }

    fun readCurrentSessionDrinks(): List<Drink> {
        val drinks = mutableListOf<Drink>()
        val table = "$DRINKS_TABLE, $CURRENT_SESSION_TABLE"
        val where = "$DRINKS_TABLE.id=$CURRENT_SESSION_TABLE.drink_id"
        val order = "$CURRENT_SESSION_TABLE.position ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)
        while (cursor.moveToNext()) {
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            drinks.add(
                Drink(
                    id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id"))),
                    name = drinkName,
                    abv = cursor.getDouble(cursor.getColumnIndex("abv")),
                    amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                    measurement = VolumeMeasurement.fromLowercaseString(cursor.getString(cursor.getColumnIndex("measurement"))),
                    favorited = isFavoritedInDB(drinkName),
                    recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1,
                    modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
                )
            )
        }
        cursor.close()
        return drinks
    }

    fun readLogHeaders(): List<LogHeader> {
        val headers = mutableListOf<LogHeader>()

        val cursor = db.query(LOG_TABLE, null, null, null, null, null, null)
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

    fun readFavoriteDrinks(): List<Drink> {
        val favorites = mutableListOf<Drink>()
        val table = "$DRINKS_TABLE, $FAVORITES_TABLE"
        val where = "$DRINKS_TABLE.id=favorites.origin_id"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()) {
            favorites.add(0,
                Drink(
                    id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id"))),
                    name = cursor.getString(cursor.getColumnIndex("name")),
                    abv = cursor.getDouble(cursor.getColumnIndex("abv")),
                    amount = cursor.getDouble(cursor.getColumnIndex("amount")),
                    measurement = VolumeMeasurement.fromLowercaseString(cursor.getString(cursor.getColumnIndex("measurement"))),
                    favorited = true,
                    recent = false,
                    modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
                )
            )
        }
        cursor.close()
        return favorites
    }

    fun writeDrinks(
        allDrinks: List<Drink> = listOf(),
        currentDrinks: List<Drink> = listOf(),
        favoriteDrinks: List<Drink> = listOf(),
        loggedDrinks: List<Pair<Int, UUID>> = listOf()
    ) {
        deleteRowsInTable(CURRENT_SESSION_TABLE, null)

        currentDrinks.forEachIndexed { i, drink ->
            writeCurrentDrink(drink.id, i)
            updateRowInDrinksTable(drink)
            if (!drink.favorited && isFavoritedInDB(drink.name)) {
                deleteRowsInTable( tableName = FAVORITES_TABLE, whereString =  "drink_name = \"${drink.name}\"")
            }
        }
        favoriteDrinks.forEach { writeFavoriteDrink(it.name, it.id) }
        allDrinks.forEach { writeDrink(it)  }
        loggedDrinks.forEach { writeLoggedDrink(it.first, it.second)  }
    }

    fun writeLogHeaders(headers: List<LogHeader>) {
        deleteRowsInTable(LOG_TABLE, null)
        for (header in headers) {
            writeLogHeader(header)
        }
    }

    fun writeDrink(drink: Drink) {
        val sql = "INSERT INTO $DRINKS_TABLE (id, name, abv, amount, measurement, recent, modifiedTime, dontSuggest)" +
            "VALUES (\"${drink.id}\", \"${drink.name}\", ${drink.abv}, ${drink.amount}, " +
            "\"${drink.measurement.displayName}\", ${drink.recent.toInt()}, ${drink.modifiedTime}, ${drink.dontSuggest.toInt()})"
        db.execSQL(sql)
    }

    fun writeCurrentDrink(id: UUID, position: Int) {
        val sql = "INSERT INTO $CURRENT_SESSION_TABLE VALUES (\"$id\", $position)"
        db.execSQL(sql)
    }

    fun writeFavoriteDrink(name: String, id: UUID) {
        val sql = "INSERT INTO $FAVORITES_TABLE VALUES (\"$name\", \"$id\")"
        db.execSQL(sql)
    }

    fun writeLoggedDrink(date: Int, id: UUID) {
        val sql = "INSERT INTO $LOGGED_DRINKS_TABLE VALUES ($date, \"$id\")"
        db.execSQL(sql)
    }

    fun writeLogHeader(logHeader: LogHeader) {
        val sql = "INSERT INTO $LOG_TABLE VALUES (${logHeader.date}, ${logHeader.bac}, ${logHeader.duration})"
        db.execSQL(sql)
    }

    fun deleteRowsInTable(tableName: String, whereString: String? = null) {
        require(ALL_TABLES.contains(tableName)) { "$tableName is not a valid database table. Must be one of ${ALL_TABLES.contentToString()}" }
        val sql = if (whereString.isNullOrBlank()) "DELETE FROM $tableName"
        else "DELETE FROM $tableName WHERE $whereString"
        db.execSQL(sql)
    }

    fun updateRowInDrinksTable(drink: Drink) {
        val sql = "UPDATE $DRINKS_TABLE SET " +
            "name=\"${drink.name}\", " +
            "abv=${drink.abv}, " +
            "amount=${drink.amount}," +
            "measurement=\"${drink.measurement.displayName}\", " +
            "recent=${drink.recent.toInt()}, " +
            "dontSuggest=${drink.dontSuggest.toInt()}, " +
            "modifiedTime=${drink.modifiedTime} "
            "WHERE id=\"${drink.id}\""
        db.execSQL(sql)
    }

    fun closeDatabase() = db.close()
}

private fun Boolean.toInt() = if (this) 1 else 0
