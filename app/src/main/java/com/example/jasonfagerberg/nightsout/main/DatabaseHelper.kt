package com.example.jasonfagerberg.nightsout.main

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.jasonfagerberg.nightsout.log.LogHeader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "DatabaseHelper"

class DatabaseHelper(val context: Context?, val name: String?, factory: SQLiteDatabase.CursorFactory?,
                            val version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    //private val path = context!!.getDatabasePath(name).toString()
    private val path = "data/data/com.example.jasonfagerberg.nightsout/$name"
    private lateinit var db: SQLiteDatabase
    private lateinit var mMainActivity: MainActivity

    override fun onCreate(db: SQLiteDatabase?) { /* nothing to do */}

    fun openDatabase(){
        Log.v(TAG, "openDatabase()...called")
        //createDatabase()
        if(!exists()){
            createDatabase()
        }

        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)

        if (db.version != version) {
            onUpgrade(db, db.version, version)
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        }

        mMainActivity = context as MainActivity
    }

    private fun createDatabase(){
        Log.v(TAG, "createDatabase()...called")
        try {
            copyDatabase()
        } catch (e: IOException) {
            throw Error("Error copying database")
        }

    }

    private fun copyDatabase(){
        Log.v(TAG, "copy")
        val inputStream = context!!.assets.open(name!!)
        Log.v(TAG, path)
        val outputStream = FileOutputStream(path)

        val buffer = ByteArray(1024)
        while (inputStream.read(buffer) > 0) {
            outputStream.write(buffer)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    fun closeDatabase(){
        Log.v(TAG, "closeDatabase()...called")
        db.close()
    }

    private fun exists() : Boolean{
        return File(path).exists()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.v(TAG, "OnUpgrade....called")
        dropAllTables()
        rebuildTables()
        createDatabase()
        db!!.version = newVersion
    }

    private fun dropAllTables(){
        db.execSQL("DROP TABLE drinks")
        db.execSQL("DROP TABLE current_session_drinks")
        db.execSQL("DROP TABLE favorites")
        db.execSQL("DROP TABLE log")
        db.execSQL("DROP TABLE log_drink")
    }

    private fun rebuildTables(){
        db.execSQL("CREATE TABLE \"current_session_drinks\" ( `drink_id` INTEGER, `position` INTEGER )")
        db.execSQL("CREATE TABLE \"drinks\" ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `abv` NUMERIC, `amount` NUMERIC, `measurement` TEXT, `recent` INTEGER, `modifiedTime` INTEGER )")
        db.execSQL("CREATE TABLE `favorites` ( `drink_name` TEXT, `origin_id` INTEGER )")
        db.execSQL("CREATE TABLE \"log\" ( `date` INTEGER UNIQUE, `bac` NUMERIC, `duration` INTEGER, PRIMARY KEY(`date`) )")
        db.execSQL("CREATE TABLE \"log_drink\" ( `log_date` NUMERIC, `drink_id` INTEGER )")
    }

    fun pullDrinks(){
        Log.v(TAG, "pullDrinks()...called")
        mMainActivity.mDrinksList.clear()
        mMainActivity.mFavoritesList.clear()
        mMainActivity.mRecentsList.clear()

        val table = "drinks, current_session_drinks"
        val where = "drinks.id=current_session_drinks.drink_id"
        val order = "current_session_drinks.position ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)
        while (cursor.moveToNext()){
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
            Log.v(TAG, "current session $drink")
        }
        cursor.close()
        pullFavoriteDrinks()
        pullRecentDrinks()
    }

    private fun pullFavoriteDrinks(){
        val table = "drinks, favorites"
        val where = "drinks.id=favorites.origin_id"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()){
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

    private fun pullRecentDrinks(){
        val table = "drinks"
        val where = "drinks.recent=1"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, where, null, null, null, order, null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, true, modifiedTime)

            if(mMainActivity.mRecentsList.contains(drink)) {
                val i = mMainActivity.mRecentsList.indexOf(drink)
                mMainActivity.mRecentsList[i].recent = false
                mMainActivity.mRecentsList.remove(drink)
            }
            mMainActivity.mRecentsList.add(0, drink)
            Log.v(TAG, "recentD $drink")
        }

        cursor.close()
    }

    private fun isFavoritedInDB(name: String): Boolean{
        val where = "drink_name = ?"
        val whereArgs = arrayOf(name)
        val cursor = db.query("favorites", null, where, whereArgs, null, null,null)
        val ret = cursor.count == 1
        cursor.close()
        return ret
    }

    fun pullLogHeaders(){
        val cursor = db.query("log", null, null, null, null, null, null)
        while (cursor.moveToNext()){
            val date = cursor.getInt(cursor.getColumnIndex("date"))
            val bac = cursor.getDouble(cursor.getColumnIndex("bac"))
            val duration = cursor.getDouble(cursor.getColumnIndex("duration"))
            mMainActivity.mLogHeaders.add(LogHeader(date, bac, duration))
            //Log.v(TAG, logHeaders[logHeaders.size-1].toString())
        }
        cursor.close()
    }

    fun pushDrinks(){
        Log.v(TAG, "pushDrinks()...called")
        deleteAllRowsInTable("current_session_drinks")
        for(i in mMainActivity.mDrinksList.indices){
            val drink = mMainActivity.mDrinksList[i]
            insertRowInCurrentSessionTable(drink.id, i)
            updateRowInDrinksTable(drink)
            if(drink.favorited && !isFavoritedInDB(drink.name)){
                insertRowInFavoritesTable(drink.name, drink.id)
            }else if(!drink.favorited && isFavoritedInDB(drink.name)){
                deleteRowInFavoritesTable(drink.name)
            }
        }
    }

    fun pullDrinkNames(): ArrayList<String>{
        val names = ArrayList<String>()
        val table = "drinks"
        val order = "modifiedTime ASC"
        val cursor = db.query(table, null, null, null, null, null, order, null)
        while (cursor.moveToNext()){
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            if (!names.contains(drinkName)) names.add(0, drinkName)
        }
        cursor.close()
        return names
    }

    fun getDrinkFromName(name: String): Drink{
        val cursor = db.query("drinks", null, "name = ?", arrayOf(name),
                null, null, "modifiedTime DESC")
        cursor.moveToFirst()
        val id = cursor.getInt(cursor.getColumnIndex("id"))
        val drinkName = cursor.getString(cursor.getColumnIndex("name"))
        val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
        val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
        val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
        val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1
        val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
        cursor.close()
        return Drink(id, drinkName, abv, amount, measurement, isFavoritedInDB(name), recent, modifiedTime)
    }

    fun pushLogHeaders(){
        deleteAllRowsInTable("log")
        for(header in mMainActivity.mLogHeaders){
            insertRowInLogTable(header.date, header.bac, header.duration)
        }
    }

    private fun updateRowInDrinksTable(drink: Drink){
        var recent = 0
        if (drink.recent) recent = 1
        val sql = "UPDATE drinks SET name=\"${drink.name}\", abv=${drink.abv}, amount=${drink.amount}," +
                " measurement=\"${drink.measurement}\", recent=$recent WHERE id=${drink.id}"
        db.execSQL(sql)
        Log.v(TAG, sql)
    }

    private fun insertRowInLogTable(date: Int, bac: Double, duration: Double){
        val sql = "INSERT INTO log VALUES ($date, $bac, $duration)"
        db.execSQL(sql)
        Log.v(TAG, sql)
    }

    private fun insertRowInCurrentSessionTable(id: Int, pos:Int){
        val sql = "INSERT INTO current_session_drinks VALUES ($id, $pos)"
        db.execSQL(sql)
        Log.v(TAG, sql)
    }

    fun deleteRowInFavoritesTable(name: String){
        val sql = "DELETE FROM favorites WHERE drink_name = \"$name\""
        db.execSQL(sql)
    }

    fun insertRowInFavoritesTable(name: String, id: Int){
        val sql = "INSERT INTO favorites VALUES (\"$name\", $id)"
        db.execSQL(sql)
    }

    private fun deleteAllRowsInTable(tableName: String){
        val sql = "DELETE FROM $tableName"
        db.execSQL(sql)
        Log.v(TAG, "DELETE FROM current_session_drinks")
    }

    fun insertDrinkIntoDrinksTable(drink: Drink){
        var recent = 0
        if (drink.recent) recent =1
        val sql = "INSERT INTO drinks (name, abv, amount, measurement, recent, modifiedTime) \n" +
                "VALUES (\"${drink.name}\", ${drink.abv}, ${drink.amount}, \"${drink.measurement}\"," +
                " $recent, ${drink.modifiedTime})"
        db.execSQL(sql)
        Log.v(TAG, sql)
    }

    fun getDrinkIdFromFullDrinkInfo(target: Drink): Int{
        val where = "name=? AND  abv=? AND amount=? AND measurement=?"
        val whereArgs = arrayOf(target.name, "${target.abv}", "${target.amount}", target.measurement)
        val cursor = db.query("drinks", null, where, whereArgs, null, null, null)

        //Log.v(TAG, sql)
        while (cursor.moveToNext()){
            val foundId = cursor.getInt(cursor.getColumnIndex("id"))
            cursor.close()
            return foundId
        }
        cursor.close()
        return -1
    }

    fun getLoggedDrinks(date: Int): ArrayList<Drink>{
        val drinks = ArrayList<Drink>()
        val where = "log_date = ?"
        val whereArgs = arrayOf(date.toString())
        val cursor = db.query("log_drink", null, where, whereArgs, null, null, null)
        while (cursor.moveToNext()){
            val drinkId = cursor.getInt(cursor.getColumnIndex("drink_id"))
            drinks.add(getDrinkFromId(drinkId)!!)
        }
        cursor.close()
        return drinks
    }

    private fun getDrinkFromId(id: Int): Drink?{
        val where = "id = ?"
        val whereArgs = arrayOf(id.toString())
        val cursor = db.query("drinks", null, where, whereArgs, null, null, null)
        while (cursor.moveToNext()) {
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))

            val drink = Drink(id, drinkName, abv, amount, measurement, false, false, modifiedTime)
            cursor.close()
            return drink
        }

        cursor.close()
        return null
    }

    fun pushDrinksToLogDrinks(date: Int){
        for (drink in mMainActivity.mDrinksList){
            val sql = "INSERT INTO log_drink VALUES ($date, ${drink.id})"
            Log.i(TAG, sql)
            db.execSQL(sql)
        }
    }

    fun deleteLog(date: Int){
        var sql = "DELETE FROM log WHERE date = $date"
        db.execSQL(sql)
        sql = "DELETE FROM log_drink WHERE log_date = $date"
        db.execSQL(sql)
    }

    fun deleteAllFavorites(){
        val sql = "DELETE FROM favorites"
        db.execSQL(sql)
    }

    fun deleteAllRecents(){
        val sql = "UPDATE drinks SET recent = 0 WHERE recent = 1"
        db.execSQL(sql)
    }
}