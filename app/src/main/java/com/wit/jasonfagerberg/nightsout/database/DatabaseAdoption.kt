package com.wit.jasonfagerberg.nightsout.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.wit.jasonfagerberg.nightsout.constants.Constants

// Existing installs carry the hand-written schema at user_version 40, which Room's
// open-time validation rejects for two reasons: no NOT NULL on the primary keys and
// NUMERIC-typed double columns (Room only emits REAL). Since 40 == Constants.DB_VERSION
// no Room migration runs for those users, so we rewrite the two entity tables in place
// before Room's first open. Data is preserved verbatim; databases older than 40 are
// left for UuidMigration instead.
object DatabaseAdoption {

    fun normalizeIfNeeded(context: Context) {
        val file = context.getDatabasePath(Constants.DB_NAME)
        if (!file.exists()) return

        val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            if (db.version != Constants.DB_VERSION) return
            db.beginTransaction()
            try {
                if (needsNormalization(db, "drinks", "id")) rebuildDrinks(db)
                if (needsNormalization(db, "log", "date")) rebuildLog(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } finally {
            db.close()
        }
    }

    private fun needsNormalization(db: SQLiteDatabase, table: String, pkColumn: String): Boolean {
        db.rawQuery("PRAGMA table_info(`$table`)", null).use { cursor ->
            while (cursor.moveToNext()) {
                val type = cursor.getString(2)
                val notNull = cursor.getInt(3) == 1
                val isPk = cursor.getInt(5) > 0
                if (type.equals("NUMERIC", ignoreCase = true)) return true
                if (isPk && cursor.getString(1) == pkColumn && !notNull) return true
            }
        }
        return false
    }

    private fun rebuildDrinks(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE `drinks_new` (`id` TEXT NOT NULL, `name` TEXT, `abv` REAL," +
            " `amount` REAL, `measurement` TEXT, `recent` INTEGER, `modifiedTime` INTEGER," +
            " `dontSuggest` INTEGER, PRIMARY KEY(`id`))")
        db.execSQL("INSERT INTO `drinks_new` SELECT id, name, abv, amount, measurement, recent," +
            " modifiedTime, dontSuggest FROM `drinks`")
        db.execSQL("DROP TABLE `drinks`")
        db.execSQL("ALTER TABLE `drinks_new` RENAME TO `drinks`")
    }

    private fun rebuildLog(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE `log_new` (`date` INTEGER NOT NULL, `bac` REAL, `duration` REAL," +
            " PRIMARY KEY(`date`))")
        db.execSQL("INSERT INTO `log_new` SELECT date, bac, duration FROM `log`")
        db.execSQL("DROP TABLE `log`")
        db.execSQL("ALTER TABLE `log_new` RENAME TO `log`")
    }
}
