---
type: Reference
title: NightsOut — Data Model
description: Complete reference for the Drink, LogHeader, VolumeMeasurement, and WeightMeasurement data types used in NightsOut, plus the 5-table SQLite database schema managed by SimpleDatabaseManager.
---

# Data Model

This page documents all domain types and the database schema powering NightsOut. All model classes live in the `common` module (`/common/src/main/java/com/fagerberg/jason/common/models/`) so they are available to every other module.

## Drink

**Source:** [`Drink.kt`](/common/src/main/java/com/fagerberg/jason/common/models/Drink.kt)

```kotlin
data class Drink (
    val id: UUID,
    val name: String,
    val abv: Double,           // Alcohol by volume as percentage (e.g., 5.0 = 5%)
    val amount: Double,        // Quantity consumed
    val measurement: VolumeMeasurement,  // Unit type
    val favorited: Boolean,
    val recent: Boolean,       // Marked as recently used for autocomplete suggestions
    val modifiedTime: Long = Calendar.getInstance().timeInMillis,
    val dontSuggest: Boolean = false   // Suppress autocomplete suggestion (e.g., if user corrected a mis-spelling)
)
```

The `Drink` data class is the central entity. The `id` is a UUID — introduced during the database v40 schema migration from integer autoincrement IDs to stable UUID-based identifiers. In-memory sessions may create `Drink` objects with transient UUIDs that are persisted with their canonical UUID on save.

## VolumeMeasurement (Enum)

**Source:** [`VolumeMeasurement.kt`](/common/src/main/java/com/fagerberg/jason/common/models/VolumeMeasurement.kt)

```kotlin
enum class VolumeMeasurement(val displayName: String) {
    OZ("oz"),
    ML("ml"),
    BEERS("beers"),
    WINE_GLASSES("wine glasses"),
    SHOTS("shots"),
    PINTS("pints");
}
```

Each enum value has a canonical display name and a conversion factor defined in [`ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt):

| VolumeMeasurement | Conversion Factor (to fluid oz) | Standard Serving |
|-------------------|---------------------------------|------------------|
| `OZ` | 1.0 | — |
| `ML` | 0.033814 | — |
| `BEERS` | 12.0 | 12 oz standard beer |
| `WINE_GLASSES` | 5.0 | 5 oz wine glass |
| `SHOTS` | 1.5 | 1.5 oz shot |
| `PINTS` | 16.0 | 16 oz pint (US) |

## WeightMeasurement (Enum)

**Source:** [`WeightMeasurement.kt`](/common/src/main/java/com/fagerberg/jason/common/models/WeightMeasurement.kt)

```kotlin
enum class WeightMeasurement(val displayName: String) {
    LBS("lbs"),
    KG("kg");
}
```

Conversion to pounds uses the map in `ConversionUtils.kt`:
- `LBS → 1.0`
- `KG → 2.205`

## LogHeader

**Source:** [`LogHeader.kt`](/common/src/main/java/com/fagerberg/jason/common/models/LogHeader.kt)

Wraps a single day's drinking log entry:

```kotlin
data class LogHeader(
    val date: Int,     // YYYYMMDD format (e.g., 20240115)
    val bac: Double,   // Final BAC for the session
    val durationMinutes: Int   // Session duration in minutes
)
```

Derived properties on `LogHeader` include `year`, `month`, `day`, `monthName`, `dateString` (e.g., "Jan 15, 2024"), and `durationString` (human-readable). These are used by the LogFragment's RecyclerView adapter for displaying session summaries.

## Database Schema

**Source:** [`SimpleDatabaseManager.kt`](/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManager.kt)

The database `nights_out_db.db` contains 5 tables:

### `drinks` — Drink catalog

| Column | Type | Description |
|--------|------|-------------|
| `id` | TEXT (PRIMARY KEY) | UUID string identifier |
| `name` | TEXT | Display name (e.g., "Bud Light") |
| `abv` | REAL | Alcohol by volume percentage |
| `amount` | REAL | Default standard serving amount in the drink's measurement unit |
| `measurement` | TEXT | One of: ml, oz, beers, shots, wine glasses, pints |
| `recent` | INTEGER (0/1) | Whether the drink has been recently used |
| `modifiedTime` | INTEGER (BIGINT) | Unix timestamp in milliseconds |
| `dontSuggest` | INTEGER (0/1) | Suppress autocomplete suggestion |

### `current_session_drinks` — Active session order

| Column | Type | Description |
|--------|------|-------------|
| `drink_id` | TEXT (FK → drinks.id) | UUID reference to a drink |
| `position` | INTEGER | Display order within the current session |

This table is purely in-memory during the app's lifecycle; it is saved/restored from SQLite only on `onStop()` / `onCreate()`.

### `favorites` — User favorited drinks

| Column | Type | Description |
|--------|------|-------------|
| `drink_name` | TEXT | Display name of the drink |
| `origin_id` | TEXT (FK → drinks.id) | UUID reference to the canonical drink definition |

Favorites are user-selected entries displayed in the ProfileFragment as a horizontal RecyclerView and also surfaced in the AddDrinkActivity for quick access.

### `log` — Daily session summaries

| Column | Type | Description |
|--------|------|-------------|
| `date` | INTEGER | YYYYMMDD format (e.g., 20240115) |
| `bac` | REAL | Final BAC reading for the session |
| `duration` | INTEGER | Session duration in minutes |

### `log_drink` — Junction table: which drinks were consumed on which dates

| Column | Type | Description |
|--------|------|-------------|
| `log_date` | INTEGER (FK → log.date) | YYYYMMDD of the session |
| `drink_id` | TEXT (FK → drinks.id) | UUID reference to a drink |

This many-to-many relationship allows historical logs to reconstruct exactly which drinks were consumed on each logged day.

## Constants & Configuration

**Source:** [`Constants.kt`](/common/src/main/java/com/fagerberg/jason/common/constants/Constants.kt)

| Constant | Value | Description |
|----------|-------|-------------|
| `DB_NAME` | `"nights_out_db.db"` | Database filename |
| `DB_PATH` | `"data/data/com.wit.jasonfagerberg.nightsout/nights_out_db.db"` | Absolute install path |
| `DB_VERSION` | `40` | Current schema version |
| `MAX_BACK_STACK_SIZE` | `10` | Maximum activity back-stack depth |
| `BACK_STACK` | `"BACK_STACK"` | Intent extra key for back stack persistence |
| `FRAGMENT_ID` | `"FRAGMENT_ID"` | Intent extra key for fragment ID |
| `VOLUME_MEASUREMENTS_METRIC_FIRST` | Array of 6 string names | UI order when metric-first locale is detected |

**Source:** [`SharedPreference.kt`](/common/src/main/java/com/fagerberg/jason/common/constants/SharedPreference.kt)

SharedPreferences keys used by `NightsOutSharedPreferences`:

| Key | Type | Default / Description |
|-----|------|-----------------------|
| `profileInit` | Boolean | Whether the user has completed profile setup |
| `sex` | Boolean (true = male, false = female) | Used for Widmark r factor |
| `weight` | Float | User's body weight |
| `weightMeasurement` | String | "lbs" or "kg" |
| `startTimeMin` | Int | Session start time in minutes since midnight |
| `endTimeMin` | Int | Session end time in minutes since midnight |
| `use24HourTime` | Boolean | Time display format preference |
| `dateInstalled` | Long | App install timestamp (for rating dialog timing) |
| `drinksAddedCount` | Int | Number of drinks added (triggers rating dialog after 5) |
| `dontShowRateDialog` | Boolean | User dismissed rating request |
| `showBacNotification` | Boolean | BAC background notification enabled |
| `activeTheme` | Int | Currently active app theme resource ID |

## Source Paths Summary

| Entity / Concept | File Path |
|-----------------|-----------|
| Drink | [`/common/src/main/java/com/fagerberg/jason/common/models/Drink.kt`](/common/src/main/java/com/fagerberg/jason/common/models/Drink.kt) |
| LogHeader | [`/common/src/main/java/com/fagerberg/jason/common/models/LogHeader.kt`](/common/src/main/java/com/fagerberg/jason/common/models/LogHeader.kt) |
| VolumeMeasurement | [`/common/src/main/java/com/fagerberg/jason/common/models/VolumeMeasurement.kt`](/common/src/main/java/com/fagerberg/jason/common/models/VolumeMeasurement.kt) |
| WeightMeasurement | [`/common/src/main/java/com/fagerberg/jason/common/models/WeightMeasurement.kt`](/common/src/main/java/com/fagerberg/jason/common/models/WeightMeasurement.kt) |
| Volume conversions | [`/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt) |
| DB schema constants | [`/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManager.kt`](/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManager.kt) |

## Backlog

- [ ] **Full drink catalog listing** — The pre-populated database contains ~500+ drink entries but there is no exported list of them in source control. Inspecting the raw `.db` file would be needed to enumerate all drinks.
