package com.example.jasonfagerberg.nightsout.databaseHelper

import com.example.jasonfagerberg.nightsout.main.Drink
import com.example.jasonfagerberg.nightsout.main.MainActivity

class LogDatabaseHelper(private val databaseHelper: DatabaseHelper, private val mainActivity: MainActivity) {

    fun getLoggedDrinks(date: Int): ArrayList<Drink> {
        val drinks = ArrayList<Drink>()
        val where = "log_date = ?"
        val whereArgs = arrayOf(date.toString())
        val cursor = databaseHelper.db.query("log_drink", null, where, whereArgs, null, null, null)
        while (cursor.moveToNext()) {
            val drinkId = cursor.getInt(cursor.getColumnIndex("drink_id"))
            drinks.add(getDrinkFromId(drinkId)!!)
        }
        cursor.close()
        return drinks
    }

    fun pushDrinksToLogDrinks(date: Int) {
        for (drink in mainActivity.mDrinksList) {
            val sql = "INSERT INTO log_drink VALUES ($date, ${drink.id})"
            databaseHelper.db.execSQL(sql)
        }
    }

    fun changeLogDate(oldDate: Int, newDate: Int){
        var sql = "UPDATE log SET date = $newDate WHERE date = $oldDate"
        databaseHelper.db.execSQL(sql)
        sql = "UPDATE log_drink SET log_date = $newDate WHERE log_date = $oldDate"
        databaseHelper.db.execSQL(sql)
    }

    fun deleteLog(date: Int) {
        var sql = "DELETE FROM log WHERE date = $date"
        databaseHelper.db.execSQL(sql)
        sql = "DELETE FROM log_drink WHERE log_date = $date"
        databaseHelper.db.execSQL(sql)
    }

    private fun getDrinkFromId(id: Int): Drink? {
        val where = "id = ?"
        val whereArgs = arrayOf(id.toString())
        val cursor = databaseHelper.db.query("drinks", null, where, whereArgs, null, null, null)
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
}