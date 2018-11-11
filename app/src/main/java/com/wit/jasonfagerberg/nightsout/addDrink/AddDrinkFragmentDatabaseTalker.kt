package com.wit.jasonfagerberg.nightsout.addDrink

import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class AddDrinkFragmentDatabaseTalker(private val addDrinkFragment: AddDrinkFragment,
                                     private val mainActivity: MainActivity, private val canUnfavorite: Boolean,
                                     private val favorited: Boolean) {

    fun buildDrinkAndAddToList(name: String, abv: Double, amount: Double, measurement: String) {
        val drink = Drink(-1, name, abv, amount, measurement, favorited, true, mainActivity.getLongTimeNow())

        setDrinkId(drink)
        mainActivity.mDatabaseHelper.updateDrinkModifiedTime(drink.id, drink.modifiedTime)
        setDrinkFavorited(drink)

        if (canUnfavorite) addDrinkFragment.addToDrinkList(drink)
        else addDrinkFragment.addToFavoritesList(drink)
    }

    private fun setDrinkId(drink: Drink) {
        val foundID = mainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        val existsInDB = foundID != -1

        if (!existsInDB) {
            mainActivity.mDatabaseHelper.insertDrinkIntoDrinksTable(drink)
            drink.id = mainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        } else {
            drink.id = foundID
        }
    }

    private fun setDrinkFavorited(drink: Drink) {
        drink.favorited = mainActivity.mFavoritesList.contains(drink)

        if (mainActivity.mDrinksList.contains(drink)) {
            val index = mainActivity.mDrinksList.indexOf(drink)
            val drinkInSession = mainActivity.mDrinksList[index]
            drink.favorited = drinkInSession.favorited
        }

        if (favorited) {
            drink.favorited = true
            for (d in mainActivity.mDrinksList) {
                if (d == drink) {
                    d.favorited = true
                }
            }
        }
    }

    fun getSuggestedDrinks(filter: String): ArrayList<Drink>{
        val res = ArrayList<Drink>()
        val cursor = mainActivity.mDatabaseHelper.db.query(true, "drinks", null,
                "name LIKE ?", arrayOf("%$filter%"), null, null, "name", null)
        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent"))
            val favorited = mainActivity.mDatabaseHelper.isFavoritedInDB(drinkName)

            res.add(Drink(id, drinkName, abv, amount, measurement, favorited, recent == 1, modifiedTime))
        }
        cursor.close()
        return res
    }
}