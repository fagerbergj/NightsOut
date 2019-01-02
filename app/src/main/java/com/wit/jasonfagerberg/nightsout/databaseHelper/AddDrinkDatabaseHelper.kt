package com.wit.jasonfagerberg.nightsout.databaseHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.main.Constants
import com.wit.jasonfagerberg.nightsout.main.Drink
import java.lang.Exception
import java.util.UUID

class AddDrinkDatabaseHelper (context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : DatabaseHelper(context, name, factory, version) {

    private lateinit var mActivity : AddDrinkActivity

    init {
        try {
            mActivity = context as AddDrinkActivity
        } catch (e:Exception) {
            // do nothing, ManageDB is using this class for the get suggested drinks
            // method ass well as all base DB helper methods
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
        setDrinkFavorited(drink, favorited)

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

    private fun setDrinkFavorited(drink: Drink, favorited: Boolean) {
        drink.favorited = mActivity.mFavoritesList.contains(drink)

        if (mActivity.mDrinksList.contains(drink)) {
            val index = mActivity.mDrinksList.indexOf(drink)
            val drinkInSession = mActivity.mDrinksList[index]
            drink.favorited = drinkInSession.favorited
        }

        if (favorited) {
            drink.favorited = true
            for (d in mActivity.mDrinksList) {
                if (d == drink) {
                    d.favorited = true
                }
            }
        }
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
        if ( favoritedInDB && !drink.favorited){
            deleteRowsInTable("favorites", "drink_id=\"${drink.id}\"")
        } else {
            insertRowInFavoritesTable(drink.name, drink.id)
        }

    }
}