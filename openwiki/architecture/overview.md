---
type: Architecture Overview
title: NightsOut — Architecture Overview
description: Single-module project structure, build configuration (Kotlin DSL + version catalog), and architectural patterns for the NightsOut BAC calculator Android app.
resource: https://github.com/jasonfagerberg/NightsOut
tags: [architecture]
timestamp: 2025-07-26T04:30:00Z
---

# Architecture Overview

NightsOut is a **single-module (`:app`) Android project** using Gradle Kotlin DSL with a version catalog for dependency management. The architecture uses direct data binding (no ViewModels, LiveData, Room, or Coroutines) across all screens — the `domain.BacCalculator` object is the only pure-Kotlin library code.

## Project Structure on Disk

```
/app/                                  # Only Gradle module (:app)
├── src/main/java/com/wit/jasonfagerberg/nightsout/
│   ├── domain/      ← BacCalculator (pure Kotlin, no Android deps)
│   ├── models/      ← Drink, LogHeader, VolumeMeasurement, WeightMeasurement
│   ├── utils/       ← Converter, CountryUtils
│   ├── main/        ← MainActivity, NightsOutActivity (base), NightsOutApplication
│   ├── home/        ← HomeFragment + drink/log list adapters
│   ├── addDrink/    ← AddDrinkActivity, ComplexDrinkHelper, suggestions
│   ├── log/         ← Session logging UI
│   ├── profile/     ← ProfileFragment + favorites adapter (flat data binding)
│   ├── notification ← BacNotificationService (foreground service)
│   ├── dialogs/     ← SimpleDialog, LightSimpleDialog, BacInfoDialog, EditDrinkDialog
│   ├── databaseHelper/ ← DatabaseHelper, AddDrinkDatabaseHelper, LogDatabaseHelper
│   ├── constants/   ← AppConstants
│   └── settings/    ← SettingsActivity
├── src/main/assets/     ← nights_out_db.db (pre-populated, version 40)
└── libs/                ← Local AARs: graphview-4.2.2-androidx.aar, material-calendarview-2.0.1-androidx.aar
/gradle/libs.versions.toml  # Version catalog for all dependencies
/settings.gradle.kts       # include(":app") — single module
/app/build.gradle.kts      # Build config: Kotlin DSL
.github/workflows/ci.yml   # CI build + test workflow
```

The former `:common`, `:db`, `:profile`, and `:common-dialog` submodules were harvested into `:app` in refactor v0.78 (#78). See **[Quickstart — Architecture at a Glance](./quickstart.md#architecture-at-a-glance)** for a summary of the previous 5-module structure.

## Configuration Summary

| Setting | Value |
|---------|-------|
| **Namespace** | `com.wit.jasonfagerberg.nightsout` |
| **Compile / Target SDK** | 36 (Android 16) |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Kotlin Version** | 2.2.10 (per version catalog) |
| **AGP** | 9.3.1 |
| **KSP** | 2.2.10-2.0.2 |
| **Application ID** | `com.wit.jasonfagerberg.nightsout` |
| **Version** | 1904 ("Rattata") |

Dependencies are declared via the [version catalog](../gradle/libs.versions.toml). Key runtime dependencies:

| Dependency (catalog alias) | Version | Purpose |
|---------------------------|---------|---------|
| `libs.appcompat` | 1.1.0 | Base AndroidX components |
| `libs.material` | 1.1.0 | Material Components |
| `libs.constraintlayout` | 1.1.3 | Layout engine |
| `libs.preference` | 1.1.1 | Settings UI |
| `libs.viewpager` | 1.0.0 | ViewPager support |
| `libs.threetenabp` | 1.1.1 | Date/time utilities (used by material-calendarview) |

The project declares Room and Koin as dependencies in the version catalog, but neither is actively used in source code — they may be vestigial or intended for future migration from direct SQLite access.

## Architectural Patterns

### 1. Flat Data Binding (All Screens)

Every screen uses direct view references with `lateinit var` bindings backed by `SharedPreferences` for persistence:

```
Activity/Fragment → DatabaseHelper (SQLiteOpenHelper) → SQLite
                    ↓
                SharedPreferences (settings via PreferenceManager)
```

- **No ViewModel, LiveData, Room, or Coroutines.** Data is accessed directly from database helpers in response to user interactions.
- `MainActivity` holds **in-memory state** (current drinks, favorites, log headers) and persists on `onStop()`.
- Three specialized helper classes wrap the base `DatabaseHelper`: `AddDrinkDatabaseHelper`, `LogDatabaseHelper`.

### 2. Pure-Kotlin BAC Domain (`domain/`)

**Source:** [`domain/BacCalculator.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/domain/BacCalculator.kt)

The BAC calculation was extracted into a pure-Kotlin `object` with no Android dependencies (#82). This enables JVM-only unit tests without an emulator:

```
HomeFragment.calculateBAC()  → Converter normalizes units → BacCalculator.calculate(drinks, weightLbs, male, startTimeMin, endTimeMin)
BacNotificationService.calculateBAC()  → DatabaseHelper.pullCurrentSessionDrinks() → Converter normalizes → BacCalculator.calculate(...)
```

### 3. Pre-Populated Database Pattern

**Source:** [`databaseHelper/DatabaseHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/DatabaseHelper.kt)

The app opens a pre-populated SQLite database (`nights_out_db.db`, version 40) from `assets/` via `SQLiteOpenHelper`. On upgrade, the `onUpgrade()` method maps old integer IDs to UUIDs using a `SparseArray<UUID>`, drops and rebuilds all tables, then re-inserts saved data. The database ships with ~500+ drink entries covering common beer, wine, liquor, and cocktail types.

### 4. Application-Level State (Cross-Cutting)

**Source:** [`main/NightsOutApplication.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/NightsOutApplication.kt)

`NightsOutApplication` stores a reference to the current activity (`mCurrentActivity`). This enables `BacNotificationService` to push live BAC updates back to `HomeFragment` even when the app is not in the foreground. It also initializes ThreeTenABP for date/time support.

### 5. Base Activity Class

**Source:** [`main/NightsOutActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/NightsOutActivity.kt)

All activities (`MainActivity`, `AddDrinkActivity`, `ManageDBActivity`, `SettingsActivity`) extend this abstract class. Key features:
- Theme loading from SharedPreferences on `onCreate()`
- Back-stack tracking with configurable max depth (`MAX_BACK_STACK_SIZE = 10`)
- Notification permission request handling
- Automatic registration as `NightsOutApplication.mCurrentActivity`

## Design Decisions & Trade-offs

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| **Direct SQLite via SQLiteOpenHelper** | Simple, no ORM overhead; ships with rich pre-populated DB | No type safety, manual cursor parsing, fragile on schema changes |
| **Flat data binding everywhere** | Single-module simplification after harvesting deprecated submodules | Inconsistent architecture — complex screens (profile favorites) could benefit from MVVM/MVI |
| **SharedPreferences for all settings** | Simple key-value storage adequate for ~15 preference keys | Not ideal for large-scale persistence; no automatic schema migration |
| **In-memory state in MainActivity** | Fast access to current session drinks without DB roundtrips | Data loss risk if process is killed before `onStop()` |
| **Application singleton pattern** | Enables background service → UI communication | Tight coupling; the `mCurrentActivity` reference must be carefully managed to avoid leaks |

## Build Configuration

The project uses Gradle Kotlin DSL with a [version catalog](../gradle/libs.versions.toml) (`gradle/libs.versions.toml`) for dependency management. Release builds enable R8 shrinking and ProGuard rules (`proguard-rules.pro`). The CI workflow targets JDK 17 (Temurin), required by AGP 9.3. See **[CI/CD](../operations/ci-cd.md)** for details.

## Module Structure on Disk

```
/settings.gradle.kts       # include(":app") — single module only
/app/build.gradle.kts      # Build config: Kotlin DSL, version catalog deps, R8 release
/gradle/libs.versions.toml # Version catalog (AGP, Kotlin, AndroidX, test libs)
/.github/workflows/ci.yml  # CI build + test workflow
/app/src/main/assets/nights_out_db.db  # Pre-populated SQLite database
.gradle/ /app/build/       # Build output (gitignored)
```

The empty directories `/common/`, `/db/`, `/profile/`, and `/common-dialog/` remain on disk but contain no source files — they are vestiges of the previous multi-module structure.
