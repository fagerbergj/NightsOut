package com.wit.jasonfagerberg.nightsout.db

import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fagerberg.jason.common.constants.DB_NAME
import com.fagerberg.jason.common.constants.DB_PATH
import com.fagerberg.jason.common.models.VolumeMeasurement
import com.fagerberg.jason.common.models.test.createDrink
import com.fagerberg.jason.common.models.test.createLogHeader
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.`is` as isEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SimpleDatabaseManagerTest {

    private val testDbPath = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.absolutePath + "/$DB_NAME"
    private val testManager = SimpleDatabaseManager(
        context =  InstrumentationRegistry.getInstrumentation().targetContext,
        factory = null,
        dbPath = testDbPath
    )

    @Before
    fun setup() {
        testManager.openDatabase()
    }

    @After
    fun teardown() {
        testManager.closeDatabase()
        File(testDbPath).delete()
    }

    fun onCreateDoesNotThrowError() {
        testManager.onCreate(null)
    }

    @Test(expected = SQLiteCantOpenDatabaseException::class)
    fun databaseFileIsOpen() {
        // DB is open, cannot open another connection
        SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @Test
    fun databaseCanBeReopened() {
        testManager.closeDatabase()
        // No error thrown when opening DB after closing it
        testManager.openDatabase()
    }

    @Test
    fun onUpdateRetainsAllData() {
        // setup old data
        val allDrinks = listOf(
            createDrink(name = "Current Drink"),
            createDrink(name = "Favorite Drink", favorited = true),
            createDrink(name = "Recent Drink", recent = true),
            createDrink(name = "Logged Drink")
        )
        testManager.writeDrinks(
            allDrinks = allDrinks,
            currentDrinks = allDrinks.subList(0, 1),
            favoriteDrinks = allDrinks.subList(1, 2),
            loggedDrinks = listOf(Pair(20191231, allDrinks[3].id))
        )
        val logHeader = createLogHeader()
        testManager.writeLogHeaders(listOf(logHeader))
        testManager.closeDatabase()

        // create new DB manager with higher version. This triggers upgrade code
        val newDBManager = SimpleDatabaseManager(
            context =  InstrumentationRegistry.getInstrumentation().targetContext,
            dbPath = testDbPath,
            dbVersion = 99999,
            factory = null
        )
        newDBManager.openDatabase()
        try {
            assertThat(newDBManager.readCurrentSessionDrinks(), contains(allDrinks[0]))
            assertThat(newDBManager.readLoggedDrinks(20191231), contains(allDrinks[3]))
            assertThat(newDBManager.readLogHeaders(), contains(logHeader))
        } finally {
            newDBManager.closeDatabase()
        }
        // verify data maintained
    }

    @Test
    fun basicDrinksAreAutomaticallyInDatabase() {
        val allDrinks = testManager.readDrinks()
        assertThat(allDrinks, hasSize(3))
        val initDrinks = listOf(
            createDrink(
                id = allDrinks[0].id,
                name = "Beer",
                abv = 5.0,
                amount = 1.0,
                measurement = VolumeMeasurement.BEERS,
                modifiedTime = allDrinks[0].modifiedTime
            ),
            createDrink(
                id = allDrinks[1].id,
                name = "Wine",
                abv = 12.5,
                amount = 1.0,
                measurement = VolumeMeasurement.WINE_GLASSES,
                modifiedTime = allDrinks[0].modifiedTime
            ),
            createDrink(
                id = allDrinks[2].id,
                name = "Liquor",
                abv = 40.0,
                amount = 1.0,
                measurement = VolumeMeasurement.SHOTS,
                modifiedTime = allDrinks[0].modifiedTime
            )
        )
        assertThat(allDrinks, contains(initDrinks[0], initDrinks[1], initDrinks[2]))
    }

    @Test
    fun writeAndReadDrink() {
        val drink = createDrink()
        testManager.writeDrink(drink)
        assertThat(testManager.readDrinks(), hasItem(drink))
    }

    @Test
    fun writeAndReadCurrentDrink() {
        val drink = createDrink()
        testManager.writeDrink(drink)
        testManager.writeCurrentDrink(drink.id, 0)
        assertThat(testManager.readCurrentSessionDrinks().first(), isEqualTo(drink))
    }

    @Test
    fun writeAndReadFavoriteDrink() {
        val drink = createDrink(favorited = true)
        testManager.writeDrink(drink)
        testManager.writeFavoriteDrink(drink.name, drink.id)
        assertThat(testManager.readFavoriteDrinks(), contains(drink))
    }

    @Test
    fun writeAndReadLogHeader() {
        val header = createLogHeader()
        testManager.writeLogHeader(header)
        assertThat(testManager.readLogHeaders(), contains(header))
    }

    @Test
    fun updateDrink() {
        val drinkToUpdate = testManager.readDrinks().first()
        val newDrink = drinkToUpdate.copy(
            name = "New Name",
            abv = Math.random() * 10,
            amount = Math.random() * 10,
            measurement = VolumeMeasurement.SHOTS,
            recent = true,
            modifiedTime = (Math.random() * 10000).toLong(),
            dontSuggest = true
        )
        testManager.updateRowInDrinksTable(newDrink)
        assertThat(newDrink, isEqualTo(testManager.readDrinks().first()))
    }

    @Test
    fun deleteAllDrinks() {
        testManager.deleteRowsInTable(DRINKS_TABLE)
        assertThat(testManager.readDrinks(), hasSize(0))
    }

    @Test
    fun deleteDrinksWithWhereString() {
        testManager.deleteRowsInTable(DRINKS_TABLE, "\"name\"=\"Beer\"")
        val remainingDrinks = testManager.readDrinks()
        assertThat(remainingDrinks, hasSize(2))
        assertThat(remainingDrinks.map { it.name }, not(hasItem("Beer")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun deleteFromInvalidTableThrowsError() {
        try {
            testManager.deleteRowsInTable("not_a_real_table")
        } catch (ex: IllegalArgumentException) {
            assertThat(ex.message, isEqualTo("not_a_real_table is not a valid database table. Must be one of ${ALL_TABLES.contentToString()}"))
            throw ex
        }
        fail("No exception thrown")
    }

    @Test
    fun writeAndReadMultipleFavoriteDrinks() {
        val drinks = listOf(
            createDrink(
                name = "Favorited Drink 1",
                favorited = true,
                measurement = VolumeMeasurement.WINE_GLASSES
            ),
            createDrink(
                name = "Favorited Drink 2",
                favorited = true,
                measurement = VolumeMeasurement.BEERS
            )
        )
        testManager.writeDrinks(allDrinks = drinks, favoriteDrinks = drinks)
        assertThat(testManager.isFavoritedInDB(drinks[0].name), isEqualTo(true))
        assertThat(testManager.isFavoritedInDB(drinks[1].name), isEqualTo(true))
        assertThat(testManager.readFavoriteDrinks(), containsInAnyOrder(drinks[0], drinks[1]))
    }

    @Test
    fun writeAndReadMultipleCurrentDrinks() {
        val drinks = listOf(
            createDrink(
                name = "Current Drink 1",
                recent = true,
                measurement = VolumeMeasurement.BEERS
            ),
            createDrink(
                name = "Current Drink 2",
                recent = true,
                measurement = VolumeMeasurement.ML
            )
        )
        testManager.writeDrinks(allDrinks = drinks, currentDrinks = drinks)
        assertThat(testManager.readCurrentSessionDrinks(), contains(drinks[0], drinks[1]))
    }

    @Test
    fun writeAndReadMultipleLoggedDrinks() {
        val drinks = listOf(
            createDrink(name = "Logged Drink 1"),
            createDrink(name = "Logged Drink 2")
        )
        val date = (Math.random() * 9999).toInt()
        testManager.writeDrinks(allDrinks = drinks, loggedDrinks = drinks.map { date to it.id })
        assertThat(testManager.readLoggedDrinks(date), containsInAnyOrder(drinks[0], drinks[1]))
    }

    @Test
    fun readLoggedDrinksDoesNotReturnDrinksFromDifferentDay() {
        val date = (Math.random() * 9999).toInt()
        val differentDate = date + 1
        val drinks = listOf(
            createDrink(name = "Logged Drink 1"),
            createDrink(name = "Logged Drink 2")
        )
        testManager.writeDrinks(allDrinks = drinks)
        testManager.writeLoggedDrink(date, drinks[0].id)
        testManager.writeLoggedDrink(differentDate, drinks[1].id)
        assertThat(testManager.readLoggedDrinks(date), contains(drinks[0]))
        assertThat(testManager.readLoggedDrinks(date), not(contains(drinks[1])))
        assertThat(testManager.readLoggedDrinks(differentDate), not(contains(drinks[0])))
        assertThat(testManager.readLoggedDrinks(differentDate), contains(drinks[1]))
    }

    @Test
    fun writeAndReadLogHeaders() {
        val logHeader = createLogHeader()
        testManager.writeLogHeaders(listOf(logHeader))
        assertThat(testManager.readLogHeaders(), contains(logHeader))
    }
}
