package com.wit.jasonfagerberg.nightsout.databaseHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.wit.jasonfagerberg.nightsout.log.LogHeader
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//private const val TAG = "DatabaseHelper"

class DatabaseHelper(val context: Context?, val name: String?, factory: SQLiteDatabase.CursorFactory?,
                     val version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    //private val path = context!!.getDatabasePath(name).toString()
    private val path = "data/data/com.wit.jasonfagerberg.nightsout/$name"
    lateinit var db: SQLiteDatabase
    private lateinit var mMainActivity: MainActivity
    // temp array for maintaining db after upgrade
    private val mAllDrinks = ArrayList<Drink>()

    // general db
    override fun onCreate(db: SQLiteDatabase?) {}

    fun openDatabase() {
        mMainActivity = context as MainActivity
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

    private fun copyDatabase() {
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
        dropAllTables()
        rebuildTables()
        createDatabase()
        db!!.version = newVersion
    }

    private fun pullAllDrinks() {
        mMainActivity.mLogHeaders.clear()
        val cursor = db.query("drinks", null, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, recent, modifiedTime)
            drink.favorited = isFavoritedInDB(drinkName)
            mAllDrinks.add(drink)
        }
        cursor.close()
    }

    private fun pushAllDrinks() {
        for (drink in mAllDrinks) {
            insertDrinkIntoDrinksTable(drink)
        }
        mAllDrinks.clear()
    }

    private fun dropAllTables() {
        pullDrinks()
        pullLogHeaders()
        pullAllDrinks()
        db.execSQL("DROP TABLE drinks")
        db.execSQL("DROP TABLE current_session_drinks")
        db.execSQL("DROP TABLE favorites")
        db.execSQL("DROP TABLE log")
        db.execSQL("DROP TABLE log_drink")
    }

    private fun rebuildTables() {
        db.execSQL("CREATE TABLE \"current_session_drinks\" ( `drink_id` INTEGER, `position` INTEGER )")
        db.execSQL("CREATE TABLE \"drinks\" ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `abv` NUMERIC, `amount` NUMERIC, `measurement` TEXT, `recent` INTEGER, `modifiedTime` INTEGER )")
        db.execSQL("CREATE TABLE `favorites` ( `drink_name` TEXT, `origin_id` INTEGER )")
        db.execSQL("CREATE TABLE \"log\" ( `date` INTEGER UNIQUE, `bac` NUMERIC, `duration` INTEGER, PRIMARY KEY(`date`) )")
        db.execSQL("CREATE TABLE \"log_drink\" ( `log_date` NUMERIC, `drink_id` INTEGER )")
        pushDrinks()
        pushLogHeaders()
        pushAllDrinks()
    }

    fun pullDrinks() {
        mMainActivity.mDrinksList.clear()
        mMainActivity.mFavoritesList.clear()
        mMainActivity.mRecentsList.clear()

        val table = "drinks, current_session_drinks"
        val where = "drinks.id=current_session_drinks.drink_id"
        val order = "current_session_drinks.position ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, recent, modifiedTime)
            drink.favorited = isFavoritedInDB(drinkName)
            mMainActivity.mDrinksList.add(drink)
        }
        cursor.close()
        pullFavoriteDrinks()
        pullRecentDrinks()
    }

    fun pullLogHeaders() {
        mMainActivity.mLogHeaders.clear()

        val cursor = db.query("log", null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val date = cursor.getInt(cursor.getColumnIndex("date"))
            val bac = cursor.getDouble(cursor.getColumnIndex("bac"))
            val duration = cursor.getDouble(cursor.getColumnIndex("duration"))
            mMainActivity.mLogHeaders.add(LogHeader(date, bac, duration))
        }
        cursor.close()
    }

    private fun pullFavoriteDrinks() {
        val table = "drinks, favorites"
        val where = "drinks.id=favorites.origin_id"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, true, false, modifiedTime)

            mMainActivity.mFavoritesList.add(0, drink)
        }

        cursor.close()
    }

    private fun pullRecentDrinks() {
        val table = "drinks"
        val where = "drinks.recent=1"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, true, modifiedTime)

            if (mMainActivity.mRecentsList.contains(drink)) {
                val i = mMainActivity.mRecentsList.indexOf(drink)
                mMainActivity.mRecentsList[i].recent = false
                mMainActivity.mRecentsList.remove(drink)
            }
            mMainActivity.mRecentsList.add(0, drink)
        }

        cursor.close()
    }

    fun isFavoritedInDB(name: String): Boolean {
        val where = "drink_name = ?"
        val whereArgs = arrayOf(name)
        val cursor = db.query("favorites", null, where, whereArgs, null, null, null)
        val ret = cursor.count == 1
        cursor.close()
        return ret
    }

    fun pushDrinks() {
        deleteRowsInTable("current_session_drinks", null)
        for (i in mMainActivity.mDrinksList.indices) {
            val drink = mMainActivity.mDrinksList[i]
            insertRowInCurrentSessionTable(drink.id, i)
            updateRowInDrinksTable(drink)
            if (drink.favorited && !isFavoritedInDB(drink.name)) {
                insertRowInFavoritesTable(drink.name, drink.id)
            } else if (!drink.favorited && isFavoritedInDB(drink.name)) {
                deleteRowsInTable("favorites", "drink_name = \"${drink.name}\"")
            }
        }
    }

    fun pushLogHeaders() {
        deleteRowsInTable("log", null)
        for (header in mMainActivity.mLogHeaders) {
            insertRowInLogTable(header.date, header.bac, header.duration)
        }
    }

    fun deleteRowsInTable(tableName: String, whereString: String?) {
        val sql = if (whereString.isNullOrBlank()) "DELETE FROM $tableName"
        else "DELETE FROM $tableName WHERE $whereString"
        db.execSQL(sql)
    }

    private fun insertRowInCurrentSessionTable(id: Int, pos: Int) {
        val sql = "INSERT INTO current_session_drinks VALUES ($id, $pos)"
        db.execSQL(sql)
    }

    fun insertRowInFavoritesTable(name: String, id: Int) {
        val sql = "INSERT INTO favorites VALUES (\"$name\", $id)"
        db.execSQL(sql)
    }

    fun insertDrinkIntoDrinksTable(drink: Drink) {
        var recent = 0
        if (drink.recent) recent = 1
        val sql = "INSERT INTO drinks (name, abv, amount, measurement, recent, modifiedTime) \n" +
                "VALUES (\"${drink.name}\", ${drink.abv}, ${drink.amount}, \"${drink.measurement}\"," +
                " $recent, ${drink.modifiedTime})"
        db.execSQL(sql)
    }

    private fun insertRowInLogTable(date: Int, bac: Double, duration: Double) {
        val sql = "INSERT INTO log VALUES ($date, $bac, $duration)"
        db.execSQL(sql)
    }

    private fun updateRowInDrinksTable(drink: Drink) {
        var recent = 0
        if (drink.recent) recent = 1
        val sql = "UPDATE drinks SET name=\"${drink.name}\", abv=${drink.abv}, amount=${drink.amount}," +
                " measurement=\"${drink.measurement}\", recent=$recent WHERE id=${drink.id}"
        db.execSQL(sql)
    }

    fun updateDrinkModifiedTime(drinkId: Int, modifiedTime: Long) {
        val sql = "UPDATE drinks SET modifiedTime=$modifiedTime WHERE id = $drinkId"
        db.execSQL(sql)
    }

    fun getDrinkIdFromFullDrinkInfo(target: Drink): Int {
        val where = "name=? AND  abv=? AND amount=? AND measurement=?"
        val whereArgs = arrayOf(target.name, "${target.abv}", "${target.amount}", target.measurement)
        val cursor = db.query("drinks", null, where, whereArgs, null, null, null)

        while (cursor.moveToNext()) {
            val foundId = cursor.getInt(cursor.getColumnIndex("id"))
            cursor.close()
            return foundId
        }
        cursor.close()
        return -1
    }

    fun isLoggedDrink(id: Int): Boolean{
        val cursor = db.query("log_drink", null, "drink_id = ?",
                arrayOf(id.toString()), null, null, null)
        val res = cursor.count
        cursor.close()
        return res != 0
    }

    fun getDrinksFromName(name: String) : ArrayList<Drink>{
        val drinks = ArrayList<Drink>()
        val table = "drinks"
        val where = "name = ?"
        val whereArgs = arrayOf(name)
        val order = "modifiedTime"
        val cursor = db.query(table, null, where, whereArgs, null, null, order, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, recent, modifiedTime)
            drink.favorited = mMainActivity.mFavoritesList.contains(drink)
            drinks.add(drink)
        }
        cursor.close()
        return drinks
    }
}