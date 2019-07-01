package com.wit.jasonfagerberg.nightsout.databaseHelper

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.models.Drink
import java.lang.Exception
import java.util.UUID

class AddDrinkDatabaseHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : DatabaseHelper(context, name, factory, version) {

    private lateinit var mActivity: AddDrinkActivity

    init {
        try {
            mActivity = context as AddDrinkActivity
        } catch (e: Exception) {
            // do nothing, ManageDB is using this class for the get suggested drinks
            // method as well as all base DB helper methods
        }
    }

    fun buildDrinkAndAddToList(
        name: String,
        abv: Double,
        amount: Double,
        measurement: String,
        favorited: Boolean,
        canUnfavorite: Boolean
    ) {
        val drink = Drink(UUID.randomUUID(), name, abv, amount, measurement, favorited, true, Constants.getLongTimeNow())

        setDrinkId(drink)
        updateDrinkModifiedTime(drink.id, drink.modifiedTime)
        updateDrinkSuggestionStatus(drink.id, false)
        drink.favorited = isFavoritedInDB(drink.name) || drink.favorited

        // depending on which fragment called add drink depends what method gets called
        if (canUnfavorite) {
            mActivity.addDrinkToCurrentSessionAndRecentsTables(drink)
        } else {
            drink.recent = false
            mActivity.addToFavoritesTable(drink)
        }
    }

    private fun setDrinkId(drink: Drink) {
        val id = getDrinkIdFromFullDrinkInfo(drink)
        drink.id = id
        if (!idInDb(drink.id)) insertDrinkIntoDrinksTable(drink)
    }

    fun getSuggestedDrinks(filter: String, ignoreDontShow: Boolean = false): ArrayList<Drink> {
        val res = ArrayList<Drink>()
        val cursor = db.query(true, "drinks", null,
                "name LIKE ?", arrayOf("%$filter%"), null, null, "name, modifiedTime DESC", null)
        while (cursor.moveToNext()) {
            val id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent"))
            val favorited = isFavoritedInDB(drinkName)
            val dontSuggest = cursor.getInt(cursor.getColumnIndex("dontSuggest")) == 1

            if (!dontSuggest || ignoreDontShow) res.add(Drink(id, drinkName, abv, amount, measurement, favorited, recent == 1, modifiedTime))
        }
        cursor.close()
        return res
    }

    fun updateDrinkFavoriteStatus(drink: Drink) {
        val favoritedInDB = isFavoritedInDB(drink.name)
        if (favoritedInDB && !drink.favorited) {
            // delete out of date db ref
            deleteRowsInTable("favorites", "drink_name=\"${drink.name}\"")
        } else if (!favoritedInDB) {
            // insert new db ref since drink i favorited
            insertRowInFavoritesTable(drink.name, drink.id)
        } else if (favoritedInDB) {
            // replace old drink reference with a new one
            deleteRowsInTable("favorites", "drink_name=\"${drink.name}\"")
            insertRowInFavoritesTable(drink.name, drink.id)
        }
    }

    fun getNumberOfRows(table: String, where: String = "") : Long {
        return DatabaseUtils.queryNumEntries(db, table, where)
    }
}