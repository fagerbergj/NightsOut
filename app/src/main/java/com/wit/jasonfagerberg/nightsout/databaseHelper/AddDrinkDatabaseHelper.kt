package com.wit.jasonfagerberg.nightsout.databaseHelper

import com.wit.jasonfagerberg.nightsout.main.Drink

class AddDrinkDatabaseHelper(private val databaseHelper: DatabaseHelper) {

    fun pullDrinkNames(): ArrayList<String> {
        val names = ArrayList<String>()
        val table = "drinks"
        val order = "modifiedTime ASC"
        val cursor = databaseHelper.db.query(table, null, null, null, null, null, order, null)
        while (cursor.moveToNext()) {
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            if (!names.contains(drinkName)) names.add(0, drinkName)
        }
        cursor.close()
        return names
    }

    fun getDrinkFromName(name: String): Drink? {
        val cursor = databaseHelper.db.query("drinks", null, "name = ?", arrayOf(name),
                null, null, "modifiedTime DESC")

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
            cursor.close()
            return Drink(id, drinkName, abv, amount, measurement, databaseHelper.isFavoritedInDB(name), recent, modifiedTime)
        }

        cursor.close()
        return null
    }
}