---
type: Architecture Overview
title: NightsOut — Architecture Overview
description: Module structure, dependency graph, build configuration, and architectural patterns across the five Gradle modules of the NightsOut BAC calculator Android app.
---

# Architecture Overview

NightsOut is structured as a **5-module Android project** using Gradle subprojects. The architecture balances code reuse (shared models in `common`) with module-level separation of concerns (database, UI, profile MVI logic).

## Module Dependency Graph

```
┌───────────────┐     ┌───────────────────┐
│    common     │────▶│   common-dialog    │
│  (models, util)│    │ (dialogs only)     │
└───────┬───────┘     └───────────────────┘
        │                     ▲
        ├── depends on ───────┘
        │
        ▼
┌───────────────┐     ┌───────────────────┐
│      db       │◀────│    profile         │
│ (SQLite CRUD) │     │ (MVI presenter)   │
└───────┬───────┘     └───────────────────┘
        │                    ▲
        │ depends on         │ depends on
        ▼                    │
┌────────────────────────────┼────────────────────────────┐
│                            ▼                            │
│                      app                                │
│             (Activities, Fragments, Services)           │
└─────────────────────────────────────────────────────────┘
```

| Module | Type | Depends On | Depends By | Description |
|--------|------|------------|------------|-------------|
| [`:common`](/common/src/main/java/com/fagerberg/jason/common/) | Android Library | (none) | app, db, common-dialog, profile | Shared models, utilities, base classes, SharedPreferences wrapper |
| [`:db`](/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/) | Android Library | `:common` | app | SQLite data layer with pre-populated database |
| [`:profile`](/profile/src/main/java/com/fagerberg/jason/profile/) | Android Library | `:common`, `:db`, `:common-dialog` | (none directly) | MVI-powered profile screen; not consumed at runtime by app module — the app uses its own ProfileFragment instead. See note below. |
| [`:common-dialog`](/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/) | Android Library | `:common` | profile | Reusable dialog components (SimpleDialog, LightSimpleDialog) |
| [`:app`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/) | Android Application | `:common`, `:db`, `:profile`, `:common-dialog` | (none) | Main app with Activities, Fragments, adapters, services |

## Configuration Summary

| Setting | Value |
|---------|-------|
| **Compile SDK** | 36 (Android 16) |
| **Min SDK** | 19 (Android 4.4 KitKat) |
| **Target SDK** | 36 (Android 16) |
| **Kotlin Version** | 1.3.72 (older — may need upgrade to match AGP 9.x) |
| **Build Tools / AGP** | 9.3.1 |
| **Application ID** | `com.wit.jasonfagerberg.nightsout` |

### Key Dependencies (common to app)

| Dependency | Purpose |
|------------|---------|
| `androidx.appcompat:appcompat:1.1.0` | Base AndroidX components |
| `com.google.android.material:material:1.1.0` | Material Components (buttons, navigation bars, dialogs) |
| `androidx.constraintlayout:constraintlayout:1.1.3` | Layout engine |
| `androidx.preference:preference:1.1.1` | Settings UI |
| Local AAR `graphview-4.2.2-androidx.aar` (jetified) | BAC decline chart rendering |
| Local AAR `material-calendarview-2.0.1-androidx.aar` (jetified) | Calendar picker in Log fragment |
| `io.reactivex.rxjava2:rxjava:2.2.6` + `rxandroid:2.1.1` | Reactive streams for MVI (profile module) |
| `com.jakewharton.rxrelay2:rxrelay:2.1.1` | Replay subject support for AbstractPresenter |

## Architectural Patterns

### 1. Flat Data Binding (Primary — App Module)

Most of the app follows a direct, flat architecture without modern AndroidX components:

```
Activity/Fragment → DatabaseHelper (SQLiteOpenHelper) → SQLite
                    ↓
                SharedPreferences (settings)
```

- **No ViewModel, LiveData, Room, or Coroutines.** Data is accessed directly from database helpers in response to user interactions.
- `MainActivity` holds **in-memory state** (current drinks, favorites, log headers) and persists on `onStop()`.
- Three specialized helper classes wrap the base `DatabaseHelper`: `AddDrinkDatabaseHelper`, `LogDatabaseHelper`.

### 2. MVI with RxJava (Profile Module)

The profile module implements a clean MVI (Model-View-Intent) architecture using the shared `AbstractPresenter` base class from `common`:

```
Intent (user action) → AbstractPresenter.intentToAction() → ProfileAction
    ↓
actionToResult() [RxJava Observable, runs on Schedulers.io()]
    ↓
stateReducer(previousState, result) → ProfileViewModel
    ↓
viewModelStream().observeOn(mainThread()) → View renders state
```

The `AbstractPresenter` is a subclass of `AndroidViewModel` that manages a reactive pipeline:

1. **Intent reception** — `PublishRelay<Intent>` receives user actions (e.g., `ProfileIntent.SelectSex`)
2. **Action mapping** — Convert intents to actions via the abstract `intentToAction()` method
3. **Effect execution** — Run side effects via `actionToResult()`, which returns RxJava Observables
4. **State reduction** — Combine previous state + result into new state via `stateReducer()`
5. **State emission** — `BehaviorRelay<ViewModel>` emits to the view layer, always providing the latest state

### 3. Application-Level State (Cross-Cutting)

`NightsOutApplication` is a minimal `Application` subclass that stores a reference to the current activity (`mCurrentActivity`). This enables:

- `BacNotificationService` to push live BAC updates back to `HomeFragment` even when the app is not in the foreground
- Theme initialization and back-stack persistence across activities

### 4. Pre-Populated Database Pattern

The `db` module uses a pre-populated SQLite database (`nights_out_db.db`, version 40) stored in `assets/`. The `SimpleDatabaseManager` copies it to the app's data directory on first use and handles schema migrations during upgrades. The database ships with ~500+ drink entries covering common beer, wine, liquor, and cocktail types.

### 5. Shared Base Classes (`common`)

All activities extend `NightsOutActivity` (from `common`), which provides:

- Theme loading from SharedPreferences on `onCreate()`
- Back-stack tracking with configurable max depth (`MAX_BACK_STACK_SIZE = 10`)
- Instance state saving/restoring for back stack and fragment ID
- Automatic registration of the activity as the "current" one in `NightsOutApplication`
- Toast helper methods

## Design Decisions & Trade-offs

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| **Direct SQLite via SQLiteOpenHelper** | Simple, no ORM overhead; ships with rich pre-populated DB | No type safety, manual cursor parsing, fragile on schema changes |
| **MVI only in profile, not elsewhere** | Profile has complex async state (favorites CRUD); other screens are simple UI | Inconsistent architecture across the codebase; future modules may benefit from MVI |
| **SharedPreferences for all settings** | Simple key-value storage adequate for ~15 preference keys | Not ideal for large-scale persistence; no automatic schema migration |
| **In-memory state in MainActivity** | Fast access to current session drinks without DB roundtrips | Data loss risk if process is killed before `onStop()` |
| **Application singleton pattern (NightsOutApplication)** | Enables background service → UI communication | Tight coupling; the `mCurrentActivity` reference must be carefully managed to avoid leaks |

## Build & CI

The project uses a standard Gradle multi-project structure:

- **[CI workflow](../operations/ci-cd.md)** — GitHub Actions builds debug APK and runs unit tests on push to master and all PRs
- **Java 8 target** required due to AGP 3.6.2 / Gradle wrapper constraints
- No ProGuard/R8 minification in release builds (`minifyEnabled false`)

## Module Structure on Disk

```
/build.gradle                          # Top-level: Kotlin plugin, repositories
/settings.gradle                       # Include :app, :common, :db, :profile, :common-dialog
/app/                                  # Main Android app
/common/                               # Shared library module
/db/                                   # SQLite data layer library
/profile/                              # MVI profile screen library
/common-dialog/                        # Dialog component library
.github/workflows/ci.yml               # CI build + test workflow
.gradle/ /build/                       # Build output (gitignored)
```
