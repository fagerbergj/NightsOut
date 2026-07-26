package com.wit.jasonfagerberg.nightsout.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.wit.jasonfagerberg.nightsout.constants.Constants
import java.util.UUID

// Ports the old DatabaseHelper.onUpgrade for installs older than 40: int drink ids are
// remapped to random UUIDs across all four id columns, then every table is rebuilt
// (drinks/log to the canonical schema, the other three to their original DDL).
class UuidMigration(startVersion: Int) : Migration(startVersion, Constants.DB_VERSION) {

    private class DrinkRow(val id: String, val name: String, val abv: Double, val amount: Double,
                           val measurement: String, val recent: Int, val modifiedTime: Long,
                           val dontSuggest: Int)

    override fun migrate(connection: SQLiteConnection) {
        val oldIdToUuid = mapOldIdsToUUIDs(connection)
        val drinks = readDrinks(connection, oldIdToUuid)
        val sessionIds = readCurrentSessionIds(connection, oldIdToUuid, drinks.map { it.id }.toSet())
        val favorites = readFavorites(connection, oldIdToUuid, drinks.map { it.id }.toSet())
        val logEntries = readLogEntries(connection)
        val logDrinks = readLogDrinks(connection, oldIdToUuid)

        rebuildTables(connection)

        insertDrinks(connection, drinks)
        insertCurrentSession(connection, sessionIds)
        insertFavorites(connection, favorites)
        insertLogEntries(connection, logEntries)
        insertLogDrinks(connection, logDrinks)
    }

    private fun mapOldIdsToUUIDs(connection: SQLiteConnection): Map<Int, String> {
        val map = HashMap<Int, String>()
        connection.prepare("SELECT id FROM drinks").use { stmt ->
            while (stmt.step()) {
                val id = stmt.getText(0)
                try {
                    UUID.fromString(id)
                } catch (e: Exception) {
                    map[id.toInt()] = UUID.randomUUID().toString()
                }
            }
        }
        return map
    }

    // Old ids were stored as ints; anything already a UUID is kept as-is.
    private fun remap(id: String, oldIdToUuid: Map<Int, String>): String {
        return try {
            UUID.fromString(id).toString()
        } catch (e: Exception) {
            id.toIntOrNull()?.let { oldIdToUuid[it] } ?: UUID.randomUUID().toString()
        }
    }

    private fun hasColumn(connection: SQLiteConnection, table: String, column: String): Boolean {
        connection.prepare("PRAGMA table_info(`$table`)").use { stmt ->
            while (stmt.step()) {
                if (stmt.getText(1) == column) return true
            }
        }
        return false
    }

    private fun readDrinks(connection: SQLiteConnection, oldIdToUuid: Map<Int, String>): List<DrinkRow> {
        // dontSuggest was added after the UUID cutover in some old schemas
        val dontSuggestCol = if (hasColumn(connection, "drinks", "dontSuggest")) "dontSuggest" else "0"
        val rows = ArrayList<DrinkRow>()
        connection.prepare("SELECT id, name, abv, amount, measurement, recent, modifiedTime," +
                " $dontSuggestCol FROM drinks").use { stmt ->
            while (stmt.step()) {
                rows.add(DrinkRow(
                    remap(stmt.getText(0), oldIdToUuid),
                    stmt.getText(1), stmt.getDouble(2), stmt.getDouble(3), stmt.getText(4),
                    stmt.getLong(5).toInt(), stmt.getLong(6), stmt.getLong(7).toInt()))
            }
        }
        return rows
    }

    // The old helper re-inserted session rows joined against drinks (dangling rows dropped,
    // positions renumbered); mirrors that.
    private fun readCurrentSessionIds(connection: SQLiteConnection, oldIdToUuid: Map<Int, String>,
                                      validIds: Set<String>): List<String> {
        val ids = ArrayList<String>()
        connection.prepare("SELECT drinks.id FROM drinks, current_session_drinks" +
                " WHERE drinks.id = current_session_drinks.drink_id" +
                " ORDER BY current_session_drinks.position ASC").use { stmt ->
            while (stmt.step()) {
                val id = remap(stmt.getText(0), oldIdToUuid)
                if (id in validIds) ids.add(id)
            }
        }
        return ids
    }

    // The old helper deduped favorites by name on re-insert; first occurrence wins.
    private fun readFavorites(connection: SQLiteConnection, oldIdToUuid: Map<Int, String>,
                              validIds: Set<String>): List<Pair<String, String>> {
        val favorites = ArrayList<Pair<String, String>>()
        val seenNames = HashSet<String>()
        connection.prepare("SELECT favorites.drink_name, drinks.id FROM drinks, favorites" +
                " WHERE drinks.id = favorites.origin_id ORDER BY modifiedTime ASC").use { stmt ->
            while (stmt.step()) {
                val name = stmt.getText(0)
                val id = remap(stmt.getText(1), oldIdToUuid)
                if (id in validIds && seenNames.add(name)) favorites.add(Pair(name, id))
            }
        }
        return favorites
    }

    private fun readLogEntries(connection: SQLiteConnection): List<Triple<Int, Double, Double>> {
        val entries = ArrayList<Triple<Int, Double, Double>>()
        connection.prepare("SELECT date, bac, duration FROM `log`").use { stmt ->
            while (stmt.step()) {
                entries.add(Triple(stmt.getLong(0).toInt(), stmt.getDouble(1), stmt.getDouble(2)))
            }
        }
        return entries
    }

    private fun readLogDrinks(connection: SQLiteConnection, oldIdToUuid: Map<Int, String>): List<Pair<Int, String>> {
        val rows = ArrayList<Pair<Int, String>>()
        connection.prepare("SELECT log_date, drink_id FROM log_drink").use { stmt ->
            while (stmt.step()) {
                rows.add(Pair(stmt.getLong(0).toInt(), remap(stmt.getText(1), oldIdToUuid)))
            }
        }
        return rows
    }

    private fun rebuildTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `drinks`")
        connection.execSQL("DROP TABLE IF EXISTS `current_session_drinks`")
        connection.execSQL("DROP TABLE IF EXISTS `favorites`")
        connection.execSQL("DROP TABLE IF EXISTS `log`")
        connection.execSQL("DROP TABLE IF EXISTS `log_drink`")
        connection.execSQL(CREATE_DRINKS_TABLE)
        connection.execSQL(CREATE_LOG_TABLE)
        CREATE_LEGACY_TABLES.forEach { connection.execSQL(it) }
    }

    private fun insertDrinks(connection: SQLiteConnection, drinks: List<DrinkRow>) {
        // dontSuggest stays NULL except for explicitly ignored drinks, as before
        connection.prepare("INSERT INTO drinks (id, name, abv, amount, measurement, recent," +
                " modifiedTime) VALUES (?, ?, ?, ?, ?, ?, ?)").use { stmt ->
            for (drink in drinks) {
                stmt.bindText(1, drink.id)
                stmt.bindText(2, drink.name)
                stmt.bindDouble(3, drink.abv)
                stmt.bindDouble(4, drink.amount)
                stmt.bindText(5, drink.measurement)
                stmt.bindLong(6, drink.recent.toLong())
                stmt.bindLong(7, drink.modifiedTime)
                stmt.step()
                stmt.reset()
                stmt.clearBindings()
            }
        }
        connection.prepare("UPDATE drinks SET dontSuggest = 1 WHERE id = ?").use { stmt ->
            for (drink in drinks) {
                if (drink.dontSuggest != 1) continue
                stmt.bindText(1, drink.id)
                stmt.step()
                stmt.reset()
                stmt.clearBindings()
            }
        }
    }

    private fun insertCurrentSession(connection: SQLiteConnection, ids: List<String>) {
        connection.prepare("INSERT INTO current_session_drinks VALUES (?, ?)").use { stmt ->
            ids.forEachIndexed { index, id ->
                stmt.bindText(1, id)
                stmt.bindLong(2, index.toLong())
                stmt.step()
                stmt.reset()
                stmt.clearBindings()
            }
        }
    }

    private fun insertFavorites(connection: SQLiteConnection, favorites: List<Pair<String, String>>) {
        connection.prepare("INSERT INTO favorites VALUES (?, ?)").use { stmt ->
            for ((name, id) in favorites) {
                stmt.bindText(1, name)
                stmt.bindText(2, id)
                stmt.step()
                stmt.reset()
                stmt.clearBindings()
            }
        }
    }

    private fun insertLogEntries(connection: SQLiteConnection, entries: List<Triple<Int, Double, Double>>) {
        connection.prepare("INSERT INTO `log` VALUES (?, ?, ?)").use { stmt ->
            for ((date, bac, duration) in entries) {
                stmt.bindLong(1, date.toLong())
                stmt.bindDouble(2, bac)
                stmt.bindDouble(3, duration)
                stmt.step()
                stmt.reset()
                stmt.clearBindings()
            }
        }
    }

    private fun insertLogDrinks(connection: SQLiteConnection, rows: List<Pair<Int, String>>) {
        connection.prepare("INSERT INTO log_drink VALUES (?, ?)").use { stmt ->
            for ((date, drinkId) in rows) {
                stmt.bindLong(1, date.toLong())
                stmt.bindText(2, drinkId)
                stmt.step()
                stmt.reset()
                stmt.clearBindings()
            }
        }
    }

    companion object {
        val ALL: Array<Migration>
            get() = (1 until Constants.DB_VERSION).map { UuidMigration(it) }.toTypedArray()
    }
}
