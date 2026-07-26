---
type: Module Reference
title: NightsOut — App Module
description: Detailed reference for the :app module, the main Android application module of NightsOut. Covers all activities, fragments, adapters, services, and navigation flow.
---

# App Module (`:app`)

The `:app` module is the primary Android application containing all UI components, screens, data access helpers, background services, and user interaction logic. It depends on `:common`, `:db`, `:profile`, and `:common-dialog`.

## Package Structure

```
/app/src/main/java/com/wit/jasonfagerberg/nightsout/
├── main/                        # Application entry point & base activity
│   ├── MainActivity.kt
│   ├── NightsOutActivity.kt
│   └── NightsOutApplication.kt
├── addDrink/                    # Add Drink screen
│   ├── AddDrinkActivity.kt
│   ├── AddDrinkActivityAlcoholSourceAdapter.kt
│   ├── AddDrinkActivityFavoritesListAdapter.kt
│   ├── AddDrinkActivityRecentsListAdapter.kt
│   ├── ComplexDrinkHelper.kt
│   └── drinkSuggestion/
│       ├── DrinkSuggestionArrayAdapter.kt
│       └── DrinkSuggestionAutoCompleteView.kt
├── constants/
│   └── Constants.kt             # App-specific constants & preference keys
├── databaseHelper/              # SQLite data access layer (app-local)
│   ├── DatabaseHelper.kt
│   ├── AddDrinkDatabaseHelper.kt
│   └── LogDatabaseHelper.kt
├── dialogs/                     # Dialog components (legacy, mostly replaced by :common-dialog)
│   ├── BacInfoDialog.kt
│   ├── EditDrinkDialog.kt
│   ├── LightSimpleDialog.kt
│   └── SimpleDialog.kt
├── home/                        # BAC calculator screen
│   ├── HomeFragment.kt
│   ├── HomeFragmentDrinkListAdapter.kt
│   └── HomeFragmentLogDatePicker.kt
├── log/                         # Session history screen
│   ├── LogFragment.kt
│   ├── LogFragmentAdapter.kt
│   └── LogFragmentDatePicker.kt
├── manageDB/                    # Database browser/maintenance screen
│   ├── ManageDBActivity.kt
│   └── ManageDBDrinkListAdapter.kt
├── models/
│   ├── Drink.kt                 # Local model (mirrors common)
│   └── LogHeader.kt             # Local model (mirrors common)
├── notification/                # Background BAC notification service
│   ├── BacNotificationService.kt
│   └── NotificationHelper.kt
├── profile/                     # User profile + favorites screen
│   ├── ProfileFragment.kt
│   └── ProfileFragmentFavoritesListAdapter.kt
├── settings/
│   └── SettingsActivity.kt      # App preferences screen
└── utils/
    └── Converter.kt             # Unit/time conversions (app-local)
```

## Entry Points & Activities

| Activity | Class | Role | Source Path |
|----------|-------|------|-------------|
| **Launcher** | `MainActivity` | Main entry point — ViewPager host with BottomNavigationView for 3 tabs | [`main/MainActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/MainActivity.kt) |
| Add Drink | `AddDrinkActivity` | Add new drinks to session; supports simple and complex modes with autocomplete | [`addDrink/AddDrinkActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt) |
| Settings | `SettingsActivity` | Configure BAC notifications, dark theme, 24-hour time format | [`settings/SettingsActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/settings/SettingsActivity.kt) |
| Manage DB | `ManageDBActivity` | Browse/clean/reset the drink database with search | [`manageDB/ManageDBActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/manageDB/ManageDBActivity.kt) |

## Base Activity Classes

All activities in the app extend:

| Class | Source Path | Provides |
|-------|-------------|----------|
| `NightsOutActivity` (abstract) | [`main/NightsOutActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/NightsOutActivity.kt) → inherited from `common` | Theme loading, back-stack tracking, SharedPreferences initialization, current activity registration |
| `NightsOutApplication` | [`main/NightsOutApplication.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/NightsOutApplication.kt) | Application singleton with `mCurrentActivity` reference for service → UI communication |

**Note:** `NightsOutActivity` is defined in the `common` module but imported by the app. The app does not redefine it — it uses the shared version directly.

## Fragments (Main App Tabs)

### HomeFragment — BAC Calculator Screen

**Source Path:** [`home/HomeFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt)

The central screen of the app. Displays:
- Current session drink list via `HomeFragmentDrinkListAdapter` (RecyclerView with swipe-to-remove)
- Time pickers for start time and end time (with "Now" quick button)
- Real-time BAC display that recalculates on every input change
- Toolbar actions: save to log, clear drinks, show BAC info, browse history by date, manage DB

**Key methods:**
- `calculateBAC()` — Performs full Widmark calculation and updates displayed BAC
- `setupRecycler(view)` — Initializes drink list adapter with on-change listeners
- `showOrHideEmptyListText(view)` — Toggles empty-state UI based on drink count

### LogFragment — Session History Screen

**Source Path:** [`log/LogFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragment.kt)

Displays historical drinking logs with a calendar view:
- `MaterialCalendarView` decorated with colored dots indicating days when drinks were logged
- Selecting a date loads that day's drink list into a RecyclerView via `LogFragmentAdapter`
- Toolbar actions: clear all logs, clear selected day, move log to different date

**Source Paths:** [`log/LogFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragment.kt), [`log/LogFragmentAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragmentAdapter.kt)

### ProfileFragment — User Profile + Favorites Screen

**Source Path:** [`profile/ProfileFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragment.kt)

The app module's own copy of the profile fragment (separate from the MVI `:profile` library module). Displays:
- Sex selection (male/female toggle buttons)
- Weight entry with unit selector
- Horizontal RecyclerView of favorite drinks (`ProfileFragmentFavoritesListAdapter`) with drag-to-reorder support
- "Add Favorite" button that launches `AddDrinkActivity` in add-only mode

**Source Paths:** [`profile/ProfileFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragment.kt), [`profile/ProfileFragmentFavoritesListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragmentFavoritesListAdapter.kt)

## Add Drink Screen — Detailed

**Source Path:** [`addDrink/AddDrinkActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt)

The most complex screen in the app. Handles:

### Simple Mode (default)
- Single drink entry with name autocomplete, ABV percentage input, amount input, and measurement spinner
- Drink name is auto-completed from the database using `DrinkSuggestionAutoCompleteView` → `DrinkSuggestionArrayAdapter`
- On save: creates a `Drink` object, persists to SQLite via `AddDrinkDatabaseHelper`, adds to current session

### Complex Mode (multiple alcohol sources)
- Activated when user taps "Add Another Alcohol Source"
- Each source has its own ABV + amount + measurement entry
- `ComplexDrinkHelper` computes weighted-average ABV across all sources
- Final drink entry uses the weighted-average ABV and combined volume

### Favorites & Recents Panels
- Horizontal RecyclerView showing favorited drinks at the top (`AddDrinkActivityFavoritesListAdapter`)
- Recently used drinks displayed below (`AddDrinkActivityRecentsListAdapter`)
- Both allow quick one-tap addition without typing

### Input Validation
- Drink name required (non-empty)
- ABV must be < 100%
- Amount must be reasonable positive number
- Prevents adding empty or malformed entries

**Source Paths:** [`addDrink/AddDrinkActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt), [`addDrink/ComplexDrinkHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/ComplexDrinkHelper.kt), [`addDrink/drinkSuggestion/DrinkSuggestionAutoCompleteView.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/drinkSuggestion/DrinkSuggestionAutoCompleteView.kt)

## Database Access Layer (App-Level)

The app module maintains three database helper classes that wrap the shared `SimpleDatabaseManager` from the `:db` module.

| Class | Extends / Wraps | Purpose | Source Path |
|-------|-----------------|---------|-------------|
| `DatabaseHelper` | — (base class) | Core SQLite CRUD operations; extends `SQLiteOpenHelper` conceptually but delegates to `SimpleDatabaseManager` | [`databaseHelper/DatabaseHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/DatabaseHelper.kt) |
| `AddDrinkDatabaseHelper` | `DatabaseHelper` | Drink creation, autocomplete suggestions, favorite management | [`databaseHelper/AddDrinkDatabaseHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/AddDrinkDatabaseHelper.kt) |
| `LogDatabaseHelper` | — (wraps `DatabaseHelper`) | Log CRUD operations, retrieving drinks by date | [`databaseHelper/LogDatabaseHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/LogDatabaseHelper.kt) |

**Note:** The `db` module's `SimpleDatabaseManager` handles database file copying and schema management. These app-level helpers perform typed CRUD operations (insert Drink, query favorites, save log entries).

## Navigation Flow

```
                    MainActivity (Launcher Activity)
              ┌─────────────────────────────────────────────┐
              │   BottomNavigationView: [Home] [Log] [Profile]  │
              └────┬────────────────┬──────────────────┬────┘
                   │                │                  │
            HomeFragment      LogFragment       ProfileFragment
         (BAC Calculator)  (Session History)    (Profile/Favorites)
                │                              │
          [Add Drink]                       [Add Favorite]
                │                              │
          AddDrinkActivity ───────────────────┘
          (simple or complex mode)
                │
        ┌───────┼────────┐
        ▼       ▼        ▼
   Save to  Settings  Manage DB
   LogFragment  |       Activity
            [Theme,  [Search/
           Notifs]  Clean/Reset]
```

Back-stack tracking: `MainActivity` maintains a back stack via `pushToBackStack(fragmentId)` with configurable max depth (10 entries). The current activity reference is stored in `NightsOutApplication.mCurrentActivity` for service-to-UI communication.

## RecyclerView Adapters

| Adapter | Host Screen | Purpose | Source Path |
|---------|-------------|---------|-------------|
| `HomeFragmentDrinkListAdapter` | HomeFragment | Current session drinks with swipe-to-remove | [`home/HomeFragmentDrinkListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragmentDrinkListAdapter.kt) |
| `LogFragmentAdapter` | LogFragment | Date headers + drink items for historical logs | [`log/LogFragmentAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragmentAdapter.kt) |
| `ProfileFragmentFavoritesListAdapter` | ProfileFragment | Horizontal favorite drinks list with drag-to-reorder | [`profile/ProfileFragmentFavoritesListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragmentFavoritesListAdapter.kt) |
| `AddDrinkActivityFavoritesListAdapter` | AddDrinkActivity | Favorites panel in add drink screen | [`addDrink/AddDrinkActivityFavoritesListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivityFavoritesListAdapter.kt) |
| `AddDrinkActivityRecentsListAdapter` | AddDrinkActivity | Recents panel in add drink screen | [`addDrink/AddDrinkActivityRecentsListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivityRecentsListAdapter.kt) |
| `AddDrinkActivityAlcoholSourceAdapter` | AddDrinkActivity (complex mode) | Multi-source alcohol entries | [`addDrink/AddDrinkActivityAlcoholSourceAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivityAlcoholSourceAdapter.kt) |
| `ManageDBDrinkListAdapter` | ManageDBActivity | Full drink database listing with search | [`manageDB/ManageDBDrinkListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/manageDB/ManageDBDrinkListAdapter.kt) |
| `DrinkSuggestionArrayAdapter` | AddDrinkActivity (autocomplete) | Database-backed autocomplete for drink names | [`addDrink/drinkSuggestion/DrinkSuggestionArrayAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/drinkSuggestion/DrinkSuggestionArrayAdapter.kt) |

## Background Service

### BacNotificationService

**Source Path:** [`notification/BacNotificationService.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/BacNotificationService.kt)

A foreground Service that persists BAC calculation in a notification even when the app is not on screen:

- **Lifecycle:** Started/stopped via SharedPreferences flag (`showCurrentBacNotification`). Survives app backgrounding.
- **Intent actions:** `START_SERVICE`, `STOP_SERVICE`, `UPDATE_NOTIFICATION`, `REFRESH_BAC`, `ADD_DRINK`
- **User interaction:** Notification has "Add Drink" and "Update" action buttons that open `AddDrinkActivity` or trigger a fresh BAC recalculation
- **UI feedback:** When the user taps "Refresh" in the notification, if HomeFragment is resumed, the service pushes the updated BAC directly back to the fragment

### NotificationHelper

**Source Path:** [`notification/NotificationHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/NotificationHelper.kt)

Utility class that wraps `NotificationCompat.Builder` setup for the BAC channel (`Constants.CHANNEL.BAC`). Handles action button creation and notification updating.

## Dialog Components (App-Local, Legacy)

The app module includes its own dialog classes under `dialogs/`, which are mostly superseded by the shared `:common-dialog` module's components. These remain in use for backward compatibility.

| Class | Purpose | Source Path |
|-------|---------|-------------|
| `SimpleDialog` | General alert with title, body, + 3 buttons | [`dialogs/SimpleDialog.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/dialogs/SimpleDialog.kt) |
| `LightSimpleDialog` | Confirmation dialogs (positive/negative only) | [`dialogs/LightSimpleDialog.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/dialogs/LightSimpleDialog.kt) |
| `BacInfoDialog` | Educational info about BAC levels and effects | [`dialogs/BacInfoDialog.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/dialogs/BacInfoDialog.kt) |
| `EditDrinkDialog` | Modify existing drink's ABV/amount inline | [`dialogs/EditDrinkDialog.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/dialogs/EditDrinkDialog.kt) |

## Settings Activity

**Source Path:** [`settings/SettingsActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/settings/SettingsActivity.kt)

Configuration screen with toggles for:
- **Show current BAC notification** — Toggles `BacNotificationService` lifecycle
- **Dark theme** — Switches between `R.style.AppTheme` and `R.style.DarkAppTheme` (restarts activity to apply)
- **24-hour time format** — Affects time display across all screens

The dark theme toggle uses `setTheme()` followed by `recreate()` to apply without killing the service.

## Key Constants (App-Level)

**Source Path:** [`constants/Constants.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/constants/Constants.kt)

| Constant | Value | Description |
|----------|-------|-------------|
| `DB_NAME` | `"nights_out_db.db"` | Database file name |
| `DB_VERSION` | `40` | Schema version |
| `DRINK_COUNT_TO_ASK_FOR_RATING` | `5` | Show rating dialog after 5 drinks added |
| `DAYS_UNTIL_ASK_FOR_RATING` | `3` | Minimum days since install before rating prompt |
| `VOLUME_MEASUREMENTS_METRIC_FIRST` | Array of strings | UI order when metric-first locale detected |

Preference keys are defined in the app's `Constants.PREFERENCE` companion object, covering all 15 SharedPreferences entries.

## Testing

The `:app` module has no dedicated test files in source control. Tests for the domain logic and utilities live in the `:common` module instead.

## Source Paths Summary

| Area | Directory Path |
|------|---------------|
| Main entry point | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/) |
| Add Drink screen | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/) |
| Home/BAC calculator | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/) |
| Log/history | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/) |
| Profile screen | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/) |
| Database helpers | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/) |
| Background service | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/) |
| Settings | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/settings/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/settings/) |
| Manage DB screen | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/manageDB/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/manageDB/) |
| Dialogs (legacy) | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/dialogs/`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/dialogs/) |

## Design Notes & Caveats

- **Duplicate model classes:** The app module defines its own `Drink.kt` and `LogHeader.kt` under `models/`, duplicating the data classes in `:common`. This is a legacy artifact — the `:common` module was added later for shared models. The app's local copies should be consolidated to avoid divergence.
- **Duplicate dialog classes:** App-local `dialogs/SimpleDialog.kt` and `dialogs/LightSimpleDialog.kt` duplicate the `:common-dialog` versions. New code should use the shared module; existing code still references the app-local copies.
- **Converter duplication:** The app has its own `utils/Converter.kt`, while `:common` provides `ConversionUtils.kt`. These serve similar conversion purposes but have different function signatures and coverage. Consolidation recommended.
- **ProfileModule is unused at runtime:** The `:profile` library module (MVI architecture) exists in the project but the app module uses its own `ProfileFragment` implementation. The MVI profile may be a planned migration or experiment that was never completed.
