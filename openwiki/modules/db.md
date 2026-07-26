---
type: Module Reference
title: NightsOut — DB Module
description: Detailed reference for the :db module, which manages the pre-populated SQLite database (version 40) with 500+ drink entries and all CRUD operations for drinks, sessions, favorites, and logs.
---

# DB Module (`:db`)

The `:db` module is the SQLite data layer for NightsOut. It wraps a pre-populated database containing ~500+ drink catalog entries and provides all CRUD operations through the `SimpleDatabaseManager` class.

## Package Structure

```
/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/
└── SimpleDatabaseManager.kt   ← Main (and only) source file
```

The module intentionally has minimal source code — most logic is encapsulated in a single well-organized class.

## Dependencies

| Dependency | Purpose |
|------------|---------|
| `:common` | Shared models (`Drink`, `LogHeader`, `VolumeMeasurement`), DB constants (name, version, path) |
| `androidx.appcompat:appcompat:1.1.0` | AndroidX base components |
| `androidx.core:core-ktx:1.2.0` | Kotlin extensions |
| `junit:junit:4.12` | Unit test framework |
| `androidx.test.ext:junit:1.1.1` | AndroidX test runner |
| `espresso-core:3.2.0` | Instrumented UI tests |

## SimpleDatabaseManager

**Source Path:** [`SimpleDatabaseManager.kt`](/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManager.kt)

The core class, extending `SQLiteOpenHelper`. Handles database lifecycle from asset extraction to CRUD.

### Initialization Flow

```
1. onCreate() → no-op (database already exists in assets/)
2. openDatabase() called by application:
   a. If DB file does not exist at DB_PATH → writeDatabaseFile()
      - Opens input stream from context.assets.open("nights_out_db.db")
      - Copies to /data/data/com.wit.jasonfagerberg.nightsout/nights_out_db.db
   b. Opens SQLiteDatabase in READWRITE mode
3. onUpgrade() if DB_VERSION != stored version:
   a. Save all existing data to local variables
   b. dropAllTables() → rebuildTables() (schema migration)
   c. Re-insert saved data into new tables
```

### Constructor Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `context` | — | Android Context for accessing assets and creating file paths |
| `factory` | `null` | Optional SQLiteDatabase.CursorFactory |
| `dbName` | `"nights_out_db.db"` | Database filename (must match asset) |
| `dbPath` | `"data/data/com.wit.jasonfagerberg.nightsout/nights_out_db.db"` | Absolute device path |
| `dbVersion` | `40` | Current schema version number |

### Key Methods

| Method | Purpose | Notes |
|--------|---------|-------|
| `openDatabase()` | Public entry point — ensures DB file exists and is open | Called once at app startup |
| `writeDatabaseFile()` | Copies pre-populated DB from assets/ to device storage | Uses 1KB buffer for efficiency |
| `onCreate(SQLiteDatabase)` | No-op | Pre-populated DB means no create SQL needed |
| `onUpgrade(db, oldVersion, newVersion)` | Schema migration with data preservation | Drops tables → rebuilds → re-inserts |

### Database File Lifecycle

The database is **pre-built** and shipped as an asset:

1. The `.db` file lives at `/common/nights_out_db.db` in the repository
2. It is copied to the app's private data directory on first launch
3. All subsequent reads/writes use the opened `SQLiteDatabase` instance
4. On version upgrade, the migration strategy is: **extract all existing data → drop all tables → recreate with new schema → re-insert saved data**

### Table Management Methods

The class manages 5 tables (constants defined in-file):

| Table Constant | Value | Description |
|----------------|-------|-------------|
| `DRINKS_TABLE` | `"drinks"` | Drink catalog |
| `CURRENT_SESSION_TABLE` | `"current_session_drinks"` | Active session drink order |
| `FAVORITES_TABLE` | `"favorites"` | User favorites |
| `LOG_TABLE` | `"log"` | Daily session summaries |
| `LOGGED_DRINKS_TABLE` | `"log_drink"` | Junction: drinks per date |

Table-level operations include:
- `dropAllTables()` — Drops all 5 tables in correct order (respecting FK relationships)
- `buildTables()` — Creates all 5 tables with new UUID-based schema (v40+)
- `readDrinks()`, `readCurrentSessionDrinks()`, etc. — Extract data during upgrade
- `writeDrinks(allDrinks, currentDrinks, favoriteDrinks, loggedDrinks)` — Bulk re-insert during upgrade

### CRUD Operations

The class exposes comprehensive read/write methods for all 5 tables:

**Drinks:**
- `insertDrink(Drink)` → writes to `drinks` table
- `readDrinks()` → returns List of Drink objects from cursor
- `deleteDrink(UUID)` → removes by UUID
- `updateDrink(Drink)` → modifies existing entry

**Current Session:**
- `saveCurrentSessionDrinks(List<Drink>)` → bulk insert with position ordering
- `getCurrentSessionDrinks()` → ordered list of drinks in active session

**Favorites:**
- `addFavorite(String drinkName, UUID originId)` → inserts into favorites table
- `readFavoriteDrinks()` → returns List of Drink objects from cursor join
- `removeFavorite(Drink)` → deletes by name + origin ID
- `clearFavorites()` → deletes all favorite entries

**Log (Session Summaries):**
- `insertLog(LogHeader)` → writes date, BAC, duration to log table
- `readLogs()` → returns List of LogHeader objects ordered by date descending
- `deleteLogDate(Int yyyymmdd)` → removes a day's log entry
- `updateBAC(date: Int, newBac: Double)` → updates BAC for existing log

**Log-Drink Junction:**
- `insertLogDrinks(logDate, List<UUID>)` → associates drinks with a date
- `readLoggedDrinkReferences()` → extracts all date→drink mappings during upgrade
- `deleteLogDrinks(logDate)` → removes all drink references for a given date
- `getDrinksForLogDate(Int yyyymmdd)` → retrieves Drink list for a specific logged day

### Migration History (v40+)

The current schema version is **40**. Key migration characteristics:

1. **UUID-based IDs:** All drink IDs are UUID strings, replacing integer autoincrement IDs from earlier versions
2. **Schema rebuild approach:** During `onUpgrade()`, the class does NOT run ALTER TABLE statements. Instead it:
   - Saves all existing data to Kotlin collections
   - Drops ALL tables (in FK-safe order)
   - Recreates them with the v40 schema
   - Bulk re-inserts saved data
3. **Version 0 → 1 skip:** If `oldVersion == 0`, no migration is needed (fresh install). This avoids unnecessary work on first launch since the pre-populated DB already has the latest schema.

## Source File Inventory

| File | Lines | Purpose |
|------|-------|---------|
| `SimpleDatabaseManager.kt` | ~300+ lines | Complete database manager with CRUD for all 5 tables, upgrade logic, data extraction helpers |

## Testing

**Source Path:** [`db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt`](/db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt)

Instrumented tests verify:
- Database file copying from assets to device storage
- Schema correctness (all 5 tables exist with expected columns)
- CRUD round-trips (insert → read → delete)
- Log + log_drink junction table integrity

Test framework: `androidx.test.ext:junit` + `espresso-core` running on Android instrumented environment.

## Source Paths Summary

| Area | Path |
|------|------|
| Main source | [`/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManager.kt`](/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManager.kt) |
| Instrumented tests | [`/db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt`](/db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt) |
| Pre-populated DB asset | [`/common/nights_out_db.db`](/common/nights_out_db.db) |

## Design Notes & Caveats

- **Single-file module:** The entire data layer is one 300+ line class. This works well for its simplicity but could benefit from being split into separate read/write classes if the CRUD surface grows.
- **No connection pooling:** `SQLiteDatabase` is opened once per `openDatabase()` call and held in a `lateinit var`. If multiple components call `openDatabase()` concurrently, there's no guard against re-opening. The app should ensure this is called once at startup.
- **Migration strategy is destructive to schema changes:** Because the upgrade process drops ALL tables before rebuilding, any column that doesn't exist in both old and new schemas will lose its data during migration. This is safe only when columns are added (not removed or renamed) between versions. If a column name changes, it must be explicitly mapped in the save/restore cycle.
- **No Room/ORM:** The project uses raw SQLite via `SQLiteDatabase` objects and manual cursor parsing throughout. No ORM layer exists.
