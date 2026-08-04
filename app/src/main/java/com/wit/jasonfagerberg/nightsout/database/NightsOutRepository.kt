package com.wit.jasonfagerberg.nightsout.database

import android.content.Context
import androidx.room.withTransaction
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// One suspend function per old DatabaseHelper method; all SQL lives in the DAOs above.
class NightsOutRepository(
    private val db: NightsOutDatabase,
    private val drinkDao: DrinkDao,
    private val logDao: LogDao,
    appContext: Context
) {
    // Kept as a field so resetDatabase() (a suspend function, no Context param needed) can
    // access it without the Activity having to pass its own Context every time.
    private val applicationContext = appContext.applicationContext
    // Serializes every DB-touching call against resetDatabase(): Room reopens its connection
    // lazily after close(), so a DAO call racing the file swap in resetDatabase() would either
    // hit a closed database or read a half-copied file without this.
    private val dbMutex = Mutex()

    // drinks + current session + favorites

    suspend fun pullCurrentSessionDrinks(): ArrayList<Drink> = dbMutex.withLock {
        val favoriteNames = favoriteNamesUnlocked()
        val drinks = ArrayList<Drink>()
        for (entity in drinkDao.getCurrentSessionDrinks()) {
            drinks.add(entity.toDrink(favorited = entity.name in favoriteNames))
        }
        drinks
    }

    suspend fun pullFavoriteDrinks(): ArrayList<Drink> = dbMutex.withLock {
        val favorites = ArrayList<Drink>()
        for (entity in drinkDao.getFavoriteDrinks()) {
            favorites.add(0, entity.toDrink(favorited = true, recent = false))
        }
        favorites
    }

    suspend fun pullRecentDrinks(): ArrayList<Drink> = dbMutex.withLock {
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
        recents
    }

    suspend fun getSuggestedDrinks(filter: String, ignoreDontShow: Boolean = false): ArrayList<Drink> = dbMutex.withLock {
        val favoriteNames = favoriteNamesUnlocked()
        val res = ArrayList<Drink>()
        for (entity in drinkDao.getSuggestedDrinks(filter)) {
            val dontSuggest = entity.dontSuggest == 1
            if (!dontSuggest || ignoreDontShow) {
                res.add(entity.toDrink(favorited = entity.name in favoriteNames))
            }
        }
        res
    }

    suspend fun isFavoritedInDB(name: String): Boolean = dbMutex.withLock {
        isFavoritedInDBUnlocked(name)
    }

    private suspend fun isFavoritedInDBUnlocked(name: String): Boolean {
        return drinkDao.countFavoritesByName(name) == 1
    }

    private suspend fun favoriteNamesUnlocked(): Set<String> {
        return drinkDao.getFavoriteNames().toSet()
    }

    // Everything below runs in one Room transaction so a crash mid-operation can't leave the
    // session cleared with stale drink/favorite state (Room does not compose separate DAO calls
    // into one atomic unit on its own).
    suspend fun pushDrinks(current: List<Drink>, favorites: List<Drink>) = dbMutex.withLock {
        val plan = planPushDrinks(current, favorites, favoriteNamesUnlocked())
        db.withTransaction {
            drinkDao.clearCurrentSession()
            for ((drinkId, position) in plan.sessionInserts) {
                drinkDao.insertCurrentSession(drinkId, position)
            }
            for (drink in plan.drinkUpdates) {
                updateRowInDrinksTableUnlocked(drink)
            }
            for (name in plan.favoritesToDelete) {
                drinkDao.deleteFavoriteByName(name)
            }
            for ((name, id) in plan.favoritesToInsert) {
                drinkDao.insertFavorite(name, id)
            }
        }
    }

    suspend fun insertRowInCurrentSessionTable(id: UUID, pos: Int) = dbMutex.withLock {
        drinkDao.insertCurrentSession(id.toString(), pos)
    }

    suspend fun insertRowInFavoritesTable(name: String, id: UUID) = dbMutex.withLock {
        drinkDao.insertFavorite(name, id.toString())
    }

    suspend fun insertDrinkIntoDrinksTable(drink: Drink) = dbMutex.withLock {
        drinkDao.insertDrink(drink.toEntity())
    }

    suspend fun updateRowInDrinksTable(drink: Drink) = dbMutex.withLock {
        updateRowInDrinksTableUnlocked(drink)
    }

    private suspend fun updateRowInDrinksTableUnlocked(drink: Drink) {
        drinkDao.updateDrink(drink.id.toString(), drink.name, drink.abv, drink.amount,
            drink.measurement, if (drink.recent) 1 else 0)
    }

    suspend fun updateDrinkSuggestionStatus(id: UUID, dontSuggest: Boolean) = dbMutex.withLock {
        drinkDao.updateSuggestionStatus(id.toString(), if (dontSuggest) 1 else 0)
    }

    suspend fun getDrinkSuggestedStatus(id: UUID): Boolean = dbMutex.withLock {
        drinkDao.getSuggestionStatus(id.toString()) == 1
    }

    suspend fun updateDrinkModifiedTime(drinkId: UUID, modifiedTime: Long) = dbMutex.withLock {
        drinkDao.updateModifiedTime(drinkId.toString(), modifiedTime)
    }

    suspend fun getDrinkIdFromFullDrinkInfo(target: Drink): UUID = dbMutex.withLock {
        val id = drinkDao.findDrinkId(target.name, target.abv, target.amount, target.measurement)
        id?.let { UUID.fromString(it) } ?: UUID.randomUUID()
    }

    suspend fun idInDb(id: UUID): Boolean = dbMutex.withLock {
        drinkDao.countDrinkById(id.toString()) > 0
    }

    suspend fun setRecentByName(name: String, recent: Boolean) = dbMutex.withLock {
        drinkDao.setRecentByName(name, if (recent) 1 else 0)
    }

    suspend fun setRecentById(id: UUID, recent: Boolean) = dbMutex.withLock {
        drinkDao.setRecentById(id.toString(), if (recent) 1 else 0)
    }

    suspend fun currentSessionCount(): Int = dbMutex.withLock {
        drinkDao.currentSessionCount()
    }

    suspend fun clearCurrentSession() = dbMutex.withLock {
        drinkDao.clearCurrentSession()
    }

    suspend fun deleteCurrentSessionAt(position: Int) = dbMutex.withLock {
        drinkDao.deleteCurrentSessionAt(position)
    }

    suspend fun deleteCurrentSessionByDrinkId(id: UUID) = dbMutex.withLock {
        drinkDao.deleteCurrentSessionByDrinkId(id.toString())
    }

    suspend fun deleteAllFavorites() = dbMutex.withLock {
        drinkDao.deleteAllFavorites()
    }

    suspend fun deleteFavoriteByName(name: String) = dbMutex.withLock {
        drinkDao.deleteFavoriteByName(name)
    }

    suspend fun deleteRecentDrinks() = dbMutex.withLock {
        drinkDao.deleteRecentDrinks()
    }

    suspend fun deleteDrinkById(id: UUID) = dbMutex.withLock {
        drinkDao.deleteDrinkById(id.toString())
    }

    suspend fun updateDrinkFavoriteStatus(drink: Drink) = dbMutex.withLock {
        val favoritedInDB = isFavoritedInDBUnlocked(drink.name)
        if (favoritedInDB && !drink.favorited) {
            drinkDao.deleteFavoriteByName(drink.name)
        } else if (!favoritedInDB) {
            drinkDao.insertFavorite(drink.name, drink.id.toString())
        }
        // favoritedInDB && drink.favorited: already correct, nothing to do
    }

    // log

    suspend fun pullLogHeaders(): ArrayList<LogHeader> = dbMutex.withLock {
        val headers = ArrayList<LogHeader>()
        for (entity in logDao.getLogEntries()) {
            headers.add(LogHeader(entity.date, entity.bac ?: 0.0, entity.duration ?: 0.0))
        }
        headers
    }

    suspend fun getLoggedDrinks(date: Int): ArrayList<Drink> = dbMutex.withLock {
        val drinks = ArrayList<Drink>()
        for (idString in logDao.getLoggedDrinkIds(date)) {
            val id = UUID.fromString(idString)
            val entity = drinkDao.getDrinkById(id.toString())
            drinks.add(entity?.toDrink(favorited = false, recent = false)
                ?: Drink(UUID.randomUUID(), "[DRINK REMOVED]"))
        }
        drinks
    }

    suspend fun pushDrinksToLogDrinks(date: Int, drinks: List<Drink>) = dbMutex.withLock {
        for (drink in drinks) {
            logDao.insertLogDrink(date, drink.id.toString())
        }
    }

    suspend fun changeLogDate(oldDate: Int, newDate: Int) = dbMutex.withLock {
        logDao.updateLogDate(oldDate, newDate)
        logDao.updateLogDrinkDate(oldDate, newDate)
    }

    suspend fun deleteLog(date: Int) = dbMutex.withLock {
        logDao.deleteLog(date)
        logDao.deleteLogDrinks(date)
    }

    suspend fun insertRowInLogTable(date: Int, bac: Double, duration: Double) = dbMutex.withLock {
        logDao.insertLog(date, bac, duration)
    }

    suspend fun isLoggedDrink(id: UUID): Boolean = dbMutex.withLock {
        logDao.countLogDrinksById(id.toString()) != 0
    }

   // Legacy reset with external Context (used by pre-Compose code paths).
    suspend fun resetDatabase(context: Context) = dbMutex.withLock {
        doReset(
            readAsset = { context.assets.open(it) },
            writeDest = { context.getDatabasePath(it).outputStream() }
        )
    }

    // Compose-compatible reset that uses internally injected app context.
    suspend fun resetDatabase() = dbMutex.withLock {
        doReset(
            readAsset = { applicationContext.assets.open(it) },
            writeDest = { applicationContext.getDatabasePath(it).outputStream() }
        )
    }

    private inline fun doReset(
        crossinline readAsset: (String) -> java.io.InputStream,
        crossinline writeDest: (String) -> java.io.OutputStream
    ) {
        db.close()
        readAsset(Constants.DB_NAME).use { input ->
            writeDest(Constants.DB_NAME).use { output ->
                input.copyTo(output)
            }
        }
    }
}

// The DAO writes pushDrinks() needs, computed without touching the DB so the dedup logic
// (no duplicate favorite rows, stale favorites removed) is unit-testable on its own.
internal data class PushDrinksPlan(
    val sessionInserts: List<Pair<String, Int>>,
    val drinkUpdates: List<Drink>,
    val favoritesToDelete: List<String>,
    val favoritesToInsert: List<Pair<String, String>>
)

internal fun planPushDrinks(
    current: List<Drink>,
    favorites: List<Drink>,
    existingFavoriteNames: Set<String>
): PushDrinksPlan {
    val sessionInserts = ArrayList<Pair<String, Int>>()
    val drinkUpdates = ArrayList<Drink>()
    val favoritesToDelete = ArrayList<String>()
    val favoritesToInsert = ArrayList<Pair<String, String>>()
    val favoriteNames = existingFavoriteNames.toMutableSet()

    for (i in current.indices) {
        val drink = current[i]
        sessionInserts.add(drink.id.toString() to i)
        drinkUpdates.add(drink)
        if (!drink.favorited && favoriteNames.contains(drink.name)) {
            favoritesToDelete.add(drink.name)
            favoriteNames.remove(drink.name)
        }
    }
    for (drink in favorites) {
        if (!favoriteNames.contains(drink.name)) {
            favoritesToInsert.add(drink.name to drink.id.toString())
            favoriteNames.add(drink.name)
        }
    }
    return PushDrinksPlan(sessionInserts, drinkUpdates, favoritesToDelete, favoritesToInsert)
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
