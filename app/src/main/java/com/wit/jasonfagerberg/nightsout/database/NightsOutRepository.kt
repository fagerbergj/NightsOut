package com.wit.jasonfagerberg.nightsout.database

import android.content.Context
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import java.util.UUID

// One suspend function per old DatabaseHelper method; all SQL lives in the DAOs above.
class NightsOutRepository(
    private val db: NightsOutDatabase,
    private val drinkDao: DrinkDao,
    private val logDao: LogDao
) {

    // drinks + current session + favorites

    suspend fun pullCurrentSessionDrinks(): ArrayList<Drink> {
        val drinks = ArrayList<Drink>()
        for (entity in drinkDao.getCurrentSessionDrinks()) {
            drinks.add(entity.toDrink(favorited = isFavoritedInDB(entity.name.orEmpty())))
        }
        return drinks
    }

    suspend fun pullFavoriteDrinks(): ArrayList<Drink> {
        val favorites = ArrayList<Drink>()
        for (entity in drinkDao.getFavoriteDrinks()) {
            favorites.add(0, entity.toDrink(favorited = true, recent = false))
        }
        return favorites
    }

    suspend fun pullRecentDrinks(): ArrayList<Drink> {
        val recents = ArrayList<Drink>()
        for (entity in drinkDao.getRecentDrinks()) {
            val drink = entity.toDrink(favorited = false, recent = true)
            // same name shown twice: keep only the most recent entry
            if (recents.contains(drink)) {
                val i = recents.indexOf(drink)
                recents[i].recent = false
                recents.remove(drink)
            }
            if (recents.size <= 25) {
                recents.add(0, drink)
            } else {
                drinkDao.setRecentById(recents[recents.size - 1].id.toString(), 0)
                recents.removeAt(recents.size - 1)
            }
        }
        return recents
    }

    suspend fun getSuggestedDrinks(filter: String, ignoreDontShow: Boolean = false): ArrayList<Drink> {
        val res = ArrayList<Drink>()
        for (entity in drinkDao.getSuggestedDrinks(filter)) {
            val dontSuggest = entity.dontSuggest == 1
            if (!dontSuggest || ignoreDontShow) {
                res.add(entity.toDrink(favorited = isFavoritedInDB(entity.name.orEmpty())))
            }
        }
        return res
    }

    suspend fun isFavoritedInDB(name: String): Boolean {
        return drinkDao.countFavoritesByName(name) == 1
    }

    suspend fun pushDrinks(current: List<Drink>, favorites: List<Drink>) {
        drinkDao.clearCurrentSession()
        for (i in current.indices) {
            val drink = current[i]
            drinkDao.insertCurrentSession(drink.id.toString(), i)
            updateRowInDrinksTable(drink)
            if (!drink.favorited && isFavoritedInDB(drink.name)) {
                drinkDao.deleteFavoriteByName(drink.name)
            }
        }
        for (drink in favorites) {
            if (!isFavoritedInDB(drink.name)) {
                drinkDao.insertFavorite(drink.name, drink.id.toString())
            }
        }
    }

    suspend fun insertRowInCurrentSessionTable(id: UUID, pos: Int) {
        drinkDao.insertCurrentSession(id.toString(), pos)
    }

    suspend fun insertRowInFavoritesTable(name: String, id: UUID) {
        drinkDao.insertFavorite(name, id.toString())
    }

    suspend fun insertDrinkIntoDrinksTable(drink: Drink) {
        drinkDao.insertDrink(drink.toEntity())
    }

    suspend fun updateRowInDrinksTable(drink: Drink) {
        drinkDao.updateDrink(drink.id.toString(), drink.name, drink.abv, drink.amount,
            drink.measurement, if (drink.recent) 1 else 0)
    }

    suspend fun updateDrinkSuggestionStatus(id: UUID, dontSuggest: Boolean) {
        drinkDao.updateSuggestionStatus(id.toString(), if (dontSuggest) 1 else 0)
    }

    suspend fun getDrinkSuggestedStatus(id: UUID): Boolean {
        return drinkDao.getSuggestionStatus(id.toString()) == 1
    }

    suspend fun updateDrinkModifiedTime(drinkId: UUID, modifiedTime: Long) {
        drinkDao.updateModifiedTime(drinkId.toString(), modifiedTime)
    }

    suspend fun getDrinkIdFromFullDrinkInfo(target: Drink): UUID {
        val id = drinkDao.findDrinkId(target.name, target.abv, target.amount, target.measurement)
        return id?.let { UUID.fromString(it) } ?: UUID.randomUUID()
    }

    suspend fun idInDb(id: UUID): Boolean {
        return drinkDao.countDrinkById(id.toString()) > 0
    }

    suspend fun setRecentByName(name: String, recent: Boolean) {
        drinkDao.setRecentByName(name, if (recent) 1 else 0)
    }

    suspend fun setRecentById(id: UUID, recent: Boolean) {
        drinkDao.setRecentById(id.toString(), if (recent) 1 else 0)
    }

    suspend fun currentSessionCount(): Int {
        return drinkDao.currentSessionCount()
    }

    suspend fun clearCurrentSession() {
        drinkDao.clearCurrentSession()
    }

    suspend fun deleteCurrentSessionAt(position: Int) {
        drinkDao.deleteCurrentSessionAt(position)
    }

    suspend fun deleteCurrentSessionByDrinkId(id: UUID) {
        drinkDao.deleteCurrentSessionByDrinkId(id.toString())
    }

    suspend fun deleteAllFavorites() {
        drinkDao.deleteAllFavorites()
    }

    suspend fun deleteFavoriteByName(name: String) {
        drinkDao.deleteFavoriteByName(name)
    }

    suspend fun deleteRecentDrinks() {
        drinkDao.deleteRecentDrinks()
    }

    suspend fun deleteDrinkById(id: UUID) {
        drinkDao.deleteDrinkById(id.toString())
    }

    suspend fun updateDrinkFavoriteStatus(drink: Drink) {
        val favoritedInDB = isFavoritedInDB(drink.name)
        if (favoritedInDB && !drink.favorited) {
            drinkDao.deleteFavoriteByName(drink.name)
        } else if (!favoritedInDB) {
            drinkDao.insertFavorite(drink.name, drink.id.toString())
        } else if (favoritedInDB) {
            drinkDao.deleteFavoriteByName(drink.name)
            drinkDao.insertFavorite(drink.name, drink.id.toString())
        }
    }

    // log

    suspend fun pullLogHeaders(): ArrayList<LogHeader> {
        val headers = ArrayList<LogHeader>()
        for (entity in logDao.getLogEntries()) {
            headers.add(LogHeader(entity.date, entity.bac ?: 0.0, entity.duration ?: 0.0))
        }
        return headers
    }

    suspend fun getLoggedDrinks(date: Int): ArrayList<Drink> {
        val drinks = ArrayList<Drink>()
        for (idString in logDao.getLoggedDrinkIds(date)) {
            val id = UUID.fromString(idString)
            val entity = drinkDao.getDrinkById(id.toString())
            drinks.add(entity?.toDrink(favorited = false, recent = false)
                ?: Drink(UUID.randomUUID(), "[DRINK REMOVED]"))
        }
        return drinks
    }

    suspend fun pushDrinksToLogDrinks(date: Int, drinks: List<Drink>) {
        for (drink in drinks) {
            logDao.insertLogDrink(date, drink.id.toString())
        }
    }

    suspend fun changeLogDate(oldDate: Int, newDate: Int) {
        logDao.updateLogDate(oldDate, newDate)
        logDao.updateLogDrinkDate(oldDate, newDate)
    }

    suspend fun deleteLog(date: Int) {
        logDao.deleteLog(date)
        logDao.deleteLogDrinks(date)
    }

    suspend fun insertRowInLogTable(date: Int, bac: Double, duration: Double) {
        logDao.insertLog(date, bac, duration)
    }

    suspend fun isLoggedDrink(id: UUID): Boolean {
        return logDao.countLogDrinksById(id.toString()) != 0
    }

    // ManageDB reset: replace the live file with the pre-populated asset.
    // Room reopens (and re-adopts) lazily on the next DAO call.
    suspend fun resetDatabase(context: Context) {
        db.close()
        context.assets.open(Constants.DB_NAME).use { input ->
            context.getDatabasePath(Constants.DB_NAME).outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

internal fun DrinkEntity.toDrink(favorited: Boolean, recent: Boolean = this.recent == 1): Drink {
    return Drink(
        UUID.fromString(id),
        name.orEmpty(),
        abv ?: 0.0,
        amount ?: 0.0,
        measurement.orEmpty(),
        favorited,
        recent,
        modifiedTime ?: 0
    )
}

internal fun Drink.toEntity(): DrinkEntity {
    return DrinkEntity(
        id.toString(),
        name,
        abv,
        amount,
        measurement,
        if (recent) 1 else 0,
        modifiedTime,
        null
    )
}
