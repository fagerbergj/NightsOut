package com.wit.jasonfagerberg.nightsout.databaseHelper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.SparseArray
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import com.wit.jasonfagerberg.nightsout.models.Drink
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.UUID

// private const val TAG = "DatabaseHelper"

open class DatabaseHelper(
    val context: Context?,
    val name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    val version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    private val path = "data/data/com.wit.jasonfagerberg.nightsout/$name"
    lateinit var db: SQLiteDatabase

    // temp array for maintaining db after upgrade
    private val mAllDrinks = ArrayList<Drink>()
    private val mIgnoredDrinks = ArrayList<UUID>()
    private val mAllLoggedDrinks = ArrayList<Pair<Int, UUID>>()

    // delete after everyone is using UUIDs
    private val mOldIdUUIDMap = SparseArray<UUID>()

    // general db
    override fun onCreate(db: SQLiteDatabase?) {}

    fun openDatabase() {
        if (!dbExists()) {
            createDatabase()
        }

        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)

        if (db.version != version) {
            onUpgrade(db, db.version, version)
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        }
    }

    private fun dbExists(): Boolean {
        return File(path).exists()
    }

    private fun createDatabase() {
        try {
            copyDatabase()
        } catch (e: IOException) {
            throw Error("Error copying database")
        }
    }

    fun copyDatabase() {
        val inputStream = context!!.assets.open(name!!)
        val outputStream = FileOutputStream(path)

        val buffer = ByteArray(1024)
        while (inputStream.read(buffer) > 0) {
            outputStream.write(buffer)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    fun closeDatabase() {
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.version = newVersion
        // dont need to do any data saving since there is no data
        if (oldVersion == 0) return

        mapOldIdsToUUIDs(oldVersion)
        pullAllDrinks()
        val mDrinksList = pullCurrentSessionDrinks()
        val mFavoritesList = pullFavoriteDrinks()
        val mLogHeaders = pullLogHeaders()
        dropAllTables()
        rebuildTables()

        pushAllDrinks()
        pushDrinks(mDrinksList, mFavoritesList)
        pushLogHeaders(mLogHeaders)
        for (id in mIgnoredDrinks) updateDrinkSuggestionStatus(id, true)
        mDrinksList.clear()
        mFavoritesList.clear()
        mLogHeaders.clear()
    }

    private fun pullAllDrinks() {
        var cursor = db.query("drinks", null, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = try {
                UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            } catch (e: Exception) {
                mOldIdUUIDMap[cursor.getInt(cursor.getColumnIndex("id"))]!!
            }
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
            var dontSuggest = 0
            try {
                dontSuggest = cursor.getInt(cursor.getColumnIndex("dontSuggest"))
            } catch (e: Exception) {}

            if (dontSuggest == 1) mIgnoredDrinks.add(id)
            val drink = Drink(id, drinkName, abv, amount, measurement, false, recent, modifiedTime)
            drink.favorited = isFavoritedInDB(drinkName)
            mAllDrinks.add(drink)
        }
        cursor.close()

        cursor = db.query("log_drink", null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val logDate = cursor.getInt(cursor.getColumnIndex("log_date"))
            val drinkId = try {
                UUID.fromString(cursor.getString(cursor.getColumnIndex("drink_id")))
            } catch (e: Exception) {
                mOldIdUUIDMap[cursor.getInt(cursor.getColumnIndex("drink_id"))]
            }
            if (drinkId != null) mAllLoggedDrinks.add(Pair(logDate, drinkId))
            else mAllLoggedDrinks.add(Pair(logDate, UUID.randomUUID()))
        }
        cursor.close()
    }

    private fun pushAllDrinks() {
        for (drink in mAllDrinks) {
            insertDrinkIntoDrinksTable(drink)
        }
        mAllDrinks.clear()
        for (logDrink in mAllLoggedDrinks) {
            insertRowIntoLogDrinkTable(logDrink.first, logDrink.second)
        }
    }

    private fun insertRowIntoLogDrinkTable(date: Int, id: UUID) {
        val sql = "INSERT INTO log_drink VALUES ($date, \"$id\")"
        db.execSQL(sql)
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

    fun pullCurrentSessionDrinks(): ArrayList<Drink> {
        val drinks = ArrayList<Drink>()
        val table = "drinks, current_session_drinks"
        val where = "drinks.id=current_session_drinks.drink_id"
        val order = "current_session_drinks.position ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)
        while (cursor.moveToNext()) {
            val id = try {
                UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            } catch (e: Exception) {
                mOldIdUUIDMap[cursor.getInt(cursor.getColumnIndex("id"))]!!
            }
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, recent, modifiedTime)
            drink.favorited = isFavoritedInDB(drinkName)
            drinks.add(drink)
        }
        cursor.close()
        return drinks
    }

    fun pullLogHeaders(): ArrayList<LogHeader> {
        val headers = ArrayList<LogHeader>()

        val cursor = db.query("log", null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val date = cursor.getInt(cursor.getColumnIndex("date"))
            val bac = cursor.getDouble(cursor.getColumnIndex("bac"))
            val duration = cursor.getDouble(cursor.getColumnIndex("duration"))
            headers.add(LogHeader(date, bac, duration))
        }
        cursor.close()
        return headers
    }

    fun pullFavoriteDrinks(): ArrayList<Drink> {
        val favorites = ArrayList<Drink>()
        val table = "drinks, favorites"
        val where = "drinks.id=favorites.origin_id"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()) {
            val id = try {
                UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            } catch (e: Exception) {
                mOldIdUUIDMap[cursor.getInt(cursor.getColumnIndex("id"))]!!
            }
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, favorited = true, recent = false, modifiedTime = modifiedTime)
            favorites.add(0, drink)
        }
        cursor.close()
        return favorites
    }

    fun pullRecentDrinks(): ArrayList<Drink> {
        val recents = ArrayList<Drink>()
        val table = "drinks"
        val where = "drinks.recent=1"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()) {
            val id = try {
                UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            } catch (e: Exception) {
                mOldIdUUIDMap[cursor.getInt(cursor.getColumnIndex("id"))]!!
            }
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, true, modifiedTime)

            if (recents.contains(drink)) {
                val i = recents.indexOf(drink)
                recents[i].recent = false
                recents.remove(drink)
            }
            if (recents.size <= 25)
                recents.add(0, drink)
            else {
                val args = ContentValues()
                args.put("recent", "0")
                db.update("drinks", args, "id=?", arrayOf(recents[recents.size - 1].id.toString()))
                recents.removeAt(recents.size - 1)
            }
        }
        cursor.close()
        return recents
    }

    fun isFavoritedInDB(name: String): Boolean {
        val where = "drink_name = ?"
        val whereArgs = arrayOf(name)
        val cursor = db.query("favorites", null, where, whereArgs, null, null, null)
        val ret = cursor.count == 1
        cursor.close()
        return ret
    }

    fun pushDrinks(current: ArrayList<Drink>, favorites: ArrayList<Drink>) {
        deleteRowsInTable("current_session_drinks", null)
        for (i in current.indices) {
            val drink = current[i]
            insertRowInCurrentSessionTable(drink.id, i)
            updateRowInDrinksTable(drink)
            if (!drink.favorited && isFavoritedInDB(drink.name)) {
                deleteRowsInTable("favorites", "drink_name = \"${drink.name}\"")
            }
        }
        for (drink in favorites) {
            if (!isFavoritedInDB(drink.name)) {
                insertRowInFavoritesTable(drink.name, drink.id)
            }
        }
    }

    private fun pushLogHeaders(headers: ArrayList<LogHeader>) {
        deleteRowsInTable("log", null)
        for (header in headers) {
            insertRowInLogTable(header.date, header.bac, header.duration)
        }
    }

    fun deleteRowsInTable(tableName: String, whereString: String?) {
        val sql = if (whereString.isNullOrBlank()) "DELETE FROM $tableName"
        else "DELETE FROM $tableName WHERE $whereString"
        db.execSQL(sql)
    }

    fun insertRowInCurrentSessionTable(id: UUID, pos: Int) {
        val sql = "INSERT INTO current_session_drinks VALUES (\"$id\", $pos)"
        db.execSQL(sql)
    }

    fun insertRowInFavoritesTable(name: String, id: UUID) {
        val sql = "INSERT INTO favorites VALUES (\"$name\", \"$id\")"
        db.execSQL(sql)
    }

    fun insertDrinkIntoDrinksTable(drink: Drink) {
        var recent = 0
        if (drink.recent) recent = 1
        val sql = "INSERT INTO drinks (id, name, abv, amount, measurement, recent, modifiedTime)" +
                "VALUES (\"${drink.id}\", \"${drink.name}\", ${drink.abv}, ${drink.amount}, \"${drink.measurement}\"," +
                " $recent, ${drink.modifiedTime})"
        db.execSQL(sql)
    }

    fun insertRowInLogTable(date: Int, bac: Double, duration: Double) {
        val sql = "INSERT INTO log VALUES ($date, $bac, $duration)"
        db.execSQL(sql)
    }

    fun updateRowInDrinksTable(drink: Drink) {
        var recent = 0
        if (drink.recent) recent = 1
        val sql = "UPDATE drinks SET name=\"${drink.name}\", abv=${drink.abv}, amount=${drink.amount}," +
                " measurement=\"${drink.measurement}\", recent=$recent WHERE id=\"${drink.id}\""
        db.execSQL(sql)
    }

    fun updateDrinkSuggestionStatus(id: UUID, dontSuggest: Boolean) {
        val intSuggest = if (dontSuggest) 1 else 0
        val sql = "UPDATE drinks SET dontSuggest=$intSuggest WHERE id=\"$id\""
        db.execSQL(sql)
    }

    fun getDrinkSuggestedStatus(id: UUID): Boolean {
        val cursor = db.query("drinks", arrayOf("dontSuggest"), "id = ?", arrayOf(id.toString()), null, null, null)
        cursor.moveToFirst()
        val dontSuggest = cursor.getInt(cursor.getColumnIndex("dontSuggest")) == 1
        cursor.close()
        return dontSuggest
    }

    fun updateDrinkModifiedTime(drinkId: UUID, modifiedTime: Long) {
        val sql = "UPDATE drinks SET modifiedTime=$modifiedTime WHERE id = \"$drinkId\""
        db.execSQL(sql)
    }

    fun getDrinkIdFromFullDrinkInfo(target: Drink): UUID {
        val where = "name=? AND  abv=? AND amount=? AND measurement=?"
        val whereArgs = arrayOf(target.name, "${target.abv}", "${target.amount}", target.measurement)
        val cursor = db.query("drinks", null, where, whereArgs, null, null, null)

        while (cursor.moveToNext()) {
            val foundId = UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            cursor.close()
            return foundId
        }
        cursor.close()
        return UUID.randomUUID()
    }

    fun isLoggedDrink(id: UUID): Boolean {
        val cursor = db.query("log_drink", null, "drink_id = ?",
                arrayOf(id.toString()), null, null, null)
        val res = cursor.count
        cursor.close()
        return res != 0
    }

    fun idInDb(id: UUID): Boolean {
        val cursor = db.query("drinks", null, "id = ?", arrayOf(id.toString()), null, null, null, null)
        val ret = cursor.count > 0
        cursor.close()
        return ret
    }

    // needed for older db before I started using UUIDs
    private fun mapOldIdsToUUIDs(oldVersion: Int) {
        if (oldVersion >= 40) return
        val cursor = db.query("drinks", null, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex("id"))
            try {
                UUID.fromString(id)
            } catch (e: Exception) {
                mOldIdUUIDMap.put(id.toInt(), UUID.randomUUID())
            }
        }
        cursor.close()
    }
}