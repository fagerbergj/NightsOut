package com.wit.jasonfagerberg.nightsout.addDrink

import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class AddDrinkFragmentDatabaseTalker(private val addDrinkFragment: AddDrinkFragment,
                                     private val mainActivity: MainActivity, private val canUnfavorite: Boolean,
                                     private val favorited: Boolean) {

    fun buildDrinkAndAddToList(name: String, abv: Double, amount: Double, measurement: String) {
        val drink = Drink(-1, name, abv, amount, measurement, favorited, true, mainActivity.getTimeNow())

        setDrinkId(drink)
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

}