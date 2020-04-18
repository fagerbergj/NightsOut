package com.wit.jasonfagerberg.nightsout.db

import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.test_common.createDrink
import com.fagerberg.jason.common.constants.DB_NAME
import com.fagerberg.jason.common.constants.DB_PATH
import com.fagerberg.jason.common.constants.DB_VERSION
import com.fagerberg.jason.common.models.VolumeMeasurement
import org.hamcrest.CoreMatchers.`is` as isEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AbstractDatabaseManagerTest {

    private val testDbPath = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.absolutePath + "/$DB_NAME"
    private val testManager = TestDatabaseManager(testDbPath)

    @Before
    fun setup() {
        testManager.openDatabase()
    }

    @After
    fun teardown() {
        testManager.closeDatabase()
        File(testDbPath).delete()
    }

    @Test(expected = SQLiteCantOpenDatabaseException::class)
    fun databaseFileIsOpen() {
        // DB is open, cannot open another connection
        SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @Test
    fun testAlreadyCreatedDatabase() {
        testManager.closeDatabase()
        // No error thrown when opening DB after closing it
        testManager.openDatabase()
    }

    @Test
    fun writeAndReadFavoriteDrinks() {
        val drink = createDrink(
            name = "Favorited Drink",
            favorited = true,
            measurement = VolumeMeasurement.WINE_GLASSES
        )
        testManager.writeDrinks(allDrinks = listOf(drink), favoriteDrinks = listOf(drink))
        assertThat(testManager.isFavoritedInDB(drink.name), isEqualTo(true))
        assertThat(testManager.readFavoriteDrinks(), contains(drink))
    }

    @Test
    fun writeAndReadCurrentDrinks() {
        val drink = createDrink(
            name = "Current Drink",
            recent = true,
            measurement = VolumeMeasurement.BEERS
        )
        testManager.writeDrinks(allDrinks = listOf(drink), currentDrinks = listOf(drink))
        assertThat(testManager.readCurrentSessionDrinks(), contains(drink))
    }

    @Test
    fun writeAndReadLoggedDrinks() {
        val drink = createDrink(name = "Logged Drink")
        testManager.writeDrinks(allDrinks = listOf(drink), loggedDrinks = listOf(Pair(123119, drink.id)))
        assertThat(testManager.readLoggedDrinks(), contains(Pair(123119, drink.id)))
    }

    @Test
    fun updateReatinsAllData() {
        // setup old data
        testManager.closeDatabase()

        // create new DB manager with higher version. This triggers upgrade code
        val newDBManager = TestDatabaseManager(
            testDbPath = testDbPath,
            dbVersion = 99999
        )
        newDBManager.openDatabase()

        // verify data maintained
    }
}

private class TestDatabaseManager(
    testDbPath: String,
    dbVersion: Int = DB_VERSION
) : AbstractDatabaseManager(
    context =  InstrumentationRegistry.getInstrumentation().targetContext,
    factory = null,
    dbPath = testDbPath,
    dbVersion = dbVersion
) {
    override fun onCreate(p0: SQLiteDatabase?) {}
}
