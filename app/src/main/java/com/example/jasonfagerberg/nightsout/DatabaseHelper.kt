package com.example.jasonfagerberg.nightsout

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private val TAG = "DatabaseHelper"

class DatabaseHelper(val context: Context?, val name: String?, factory: SQLiteDatabase.CursorFactory?,
                            val version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    private val DB_PATH = context!!.getDatabasePath(name).toString()
    private lateinit var db: SQLiteDatabase

    override fun onCreate(db: SQLiteDatabase?) { /* nothing to do */}

    fun openDatabase(){
        if(!exists()){
            createDatabase()
        }

        Log.v(TAG, DB_PATH)
        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE)

        if (db.version != version) {
            onUpgrade(db, db.version, version)
        }
    }

    private fun createDatabase(){
        try {
            copyDatabase()
        } catch (e: IOException) {
            throw Error("Error copying database")
        }

    }

    private fun copyDatabase(){
        Log.v(TAG, "copy")
        val inputStream = context!!.assets.open(name!!)
        val outputStream = FileOutputStream(context.getDatabasePath(name))

        val buffer = ByteArray(1024)
        while (inputStream.read(buffer) > 0) {
            outputStream.write(buffer)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    fun closeDatabase(){
        db.close()
    }

    private fun exists() : Boolean{
        return File(DB_PATH).exists()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        createDatabase()
        db!!.version = newVersion
    }

    fun pullCurrentSession(): ArrayList<Drink>{
        val drinkList: ArrayList<Drink> = ArrayList()
        val cursor = db.rawQuery("SELECT * FROM drinks INNER JOIN current_session_drinks ON drinks.id=current_session_drinks.drink_id", null)
        while (cursor.moveToNext()){
            val drinkName = cursor.getString(1)
            val abv = cursor.getDouble(2)
            val amount = cursor.getDouble(3)
            val measurement = cursor.getString(4)
            val favored = cursor.getInt(5) == 1
            val recent = cursor.getInt(6) == 1
            drinkList.add(Drink(drinkName, abv, amount, measurement, favored, recent))
        }
        return drinkList
    }

    fun pullFavoriteDrinks(): ArrayList<Drink>{
        val favoritesList: ArrayList<Drink> = ArrayList()
        val cursor = db.rawQuery("SELECT * FROM drinks WHERE favored=1", null)
        while (cursor.moveToNext()){
            val drinkName = cursor.getString(1)
            val abv = cursor.getDouble(2)
            val amount = cursor.getDouble(3)
            val measurement = cursor.getString(4)
            val favored = cursor.getInt(5) == 1
            val recent = cursor.getInt(6) == 1
            favoritesList.add(Drink(drinkName, abv, amount, measurement, favored, recent))
        }
        cursor.close()
        return favoritesList
    }

    fun pullRecentDrinks(): ArrayList<Drink>{
        val recentList: ArrayList<Drink> = ArrayList()
        val cursor = db.rawQuery("SELECT * FROM drinks WHERE recent=1", null)
        while (cursor.moveToNext()){
            val drinkName = cursor.getString(1)
            val abv = cursor.getDouble(2)
            val amount = cursor.getDouble(3)
            val measurement = cursor.getString(4)
            val favored = cursor.getInt(5) == 1
            val recent = cursor.getInt(6) == 1
            recentList.add(Drink(drinkName, abv, amount, measurement, favored, recent))
        }
        cursor.close()
        return recentList
    }

    fun pullLogHeaders(): ArrayList<LogHeader>{
        val logHeaders = ArrayList<LogHeader>()
        val cursor = db.rawQuery("SELECT * FROM log", null)
        while (cursor.moveToNext()){
            val date = cursor.getLong(1)
            val maxBac = cursor.getDouble(2)
            val duration = cursor.getInt(3)
            logHeaders.add(LogHeader(date, maxBac, duration))
            Log.v(TAG, logHeaders[logHeaders.size-1].toString())
        }
        cursor.close()
        return logHeaders
    }

}