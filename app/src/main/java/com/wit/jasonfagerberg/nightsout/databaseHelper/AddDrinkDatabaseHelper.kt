package com.wit.jasonfagerberg.nightsout.databaseHelper

import com.wit.jasonfagerberg.nightsout.main.Drink

class AddDrinkDatabaseHelper(private val databaseHelper: DatabaseHelper) {
    fun updateDrinkFavoriteStatus(drink: Drink){
        val recent = if (drink.recent) 1 else 0
        val name = "\"${drink.name}\""
        val sql = "UPDATE drinks SET recent = $recent WHERE name = $name"
        databaseHelper.db.execSQL(sql)
    }
}