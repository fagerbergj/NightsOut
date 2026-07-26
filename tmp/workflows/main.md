---
type: Workflow
title: NightsOut User Workflows
description: Core user workflows including adding drinks, calculating BAC, logging sessions, and managing profile
---

# NightsOut User Workflows

## BAC Calculation Flow

The primary function of NightsOut is calculating Blood Alcohol Concentration in real-time.

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Home Screen (HomeFragment)                │
│                                                               │
│  [Start Time]  [BAC Display]  [End Time]                     │
│         ↓          ↓               ↓                          │
│    ┌────────┐  ┌────────┐    ┌────────┐                      │
│    │ Drinks │  │Results │    │Duration│                      │
│    │ List   │  │ Chart  │    │Control │                      │
│    └────────┘  └────────┘    └────────┘                      │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │calculateBAC()   │
                    │  Widmark Formula│
                    └─────────────────┘
```

### Step-by-Step Process

1. **User adds drinks** via `AddDrinkActivity`
   - Select from favorites/recent/suggestions
   - Enter name, ABV%, amount, measurement
   - Drink added to `mMainActivity.mDrinksList`

2. **BAC Calculation Triggered** on:
   - Drink added/removed
   - Start/End time changed
   - Fragment resumed

3. **Calculation Logic** ([`HomeFragment.kt:258`](https://github.com/fagerbergj/NightsOut/blob/master/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt#L258)):
   ```kotlin
   // 1. Sum alcohol content in fluid ounces
   var a = 0.0
   for (drink in mMainActivity.mDrinksList) {
       val volume = mConverter.drinkVolumeToFluidOz(drink.amount, drink.measurement)
       val abv = drink.abv / 100
       a += (volume * abv)
   }
   
   // 2. Apply Widmark formula
   val r = if (mMainActivity.sex!!) .73 else .66
   val weightInLbs = mConverter.weightToLbs(mMainActivity.weight, mMainActivity.weightMeasurement)
   val sexModifiedWeight = weightInLbs * r
   val instantBAC = (a * 5.14) / sexModifiedWeight
   
   // 3. Account for metabolism
   val hoursElapsed = (mMainActivity.endTimeMin - mMainActivity.startTimeMin) / 60.0
   val bacDecayPerHour = 0.015
   val res = instantBAC - (hoursElapsed * bacDecayPerHour)
   return maxOf(res, 0.0)
   ```

4. **Results Displayed**:
   - BAC value (e.g., "0.080")
   - Result text ("Legally Impaired")
   - Chart showing decline over time

---

## Adding Drinks Workflow

### Entry Points

1. **From Home Screen**: Tap "Add Drink" button
2. **From Profile**: Add favorite drinks

### AddDrinkActivity Flow

```
┌─────────────────────────────────────────────────────────┐
│                    AddDrinkActivity                     │
│                                                         │
│  [Drink Name]  [ABV%]  [Amount] [Measurement Spinner]  │
│                                                         │
│  [Recent Drinks]   [Favorites]                          │
│                                                         │
│  [Add Complex Drink Checkbox]                           │
│                                                         │
│                    [Add Drink Button]                   │
└─────────────────────────────────────────────────────────┘
```

### Detailed Steps

1. **Activity onCreate** ([`AddDrinkActivity.kt:63`](https://github.com/fagerbergj/NightsOut/blob/master/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt#L63)):
   ```kotlin
   mDatabaseHelper.openDatabase()
   initData()  // Load favorites and recents
   setupRecentsAndFavoritesRecycler()
   ```

2. **Drink Name Auto-Suggestion**:
   - Uses `DrinkSuggestionAutoCompleteView`
   - Filters from existing drinks in database
   - Shows name, ABV, measurement

3. **Complex Drink Mode**:
   - Checkbox enables multi-ingredient drinks
   - `ComplexDrinkHelper` manages multiple alcohol sources
   - Each source has name, ABV, amount, measurement

4. **Add Button** ([`AddDrinkActivity.kt`](https://github.com/fagerbergj/NightsOut/blob/master/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt)):
   ```kotlin
   // Create new drink
   val newDrink = Drink(
       id = UUID.randomUUID(),
       name = drinkName,
       abv = abv,
       amount = amount,
       measurement = measurementEnum,
       favorited = false,
       recent = true
   )
   
   // Insert into appropriate tables
   mDatabaseHelper.insertDrinkIntoCurrentSessionTable(newDrink)
   mDatabaseHelper.updateDrinkSuggestionStatus(newDrink.id, false)
   ```

5. **Back to Home**:
   - Drink appears in `HomeFragment` RecyclerView
   - BAC recalculated immediately

---

## Logging Sessions Workflow

### When to Log

- User wants to save current session for historical tracking
- Accessible via Home screen overflow menu

### Logging Process

```
┌────────────────────────────────────────────────────────────┐
│                    Log Dialog                               │
│                                                              │
│  "Save current session to history?"                         │
│                                                              │
│  [Current BAC] [Session Duration]                           │
│                                                              │
│  [Confirm]             [Cancel]                             │
└────────────────────────────────────────────────────────────┘
```

### Step-by-Step

1. **User taps "Log"** in toolbar ([`HomeFragment.kt:99`](https://github.com/fagerbergj/NightsOut/blob/master/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt#L99))

2. **Save Log Header**:
   ```kotlin
   val date = Integer.parseInt(dateString)  // YYYYMMDD
   val bac = currentBACValue
   val duration = drinkingDuration  // Hours
   
   mDatabaseHelper.insertLog(date, bac, duration)
   ```

3. **Save Drink References**:
   ```kotlin
   for (drink in mMainActivity.mDrinksList) {
       mDatabaseHelper.insertRowIntoLogDrinkTable(date, drink.id)
   }
   ```

4. **Clear Current Session**:
   ```kotlin
   mDatabaseHelper.deleteRowsInTable("current_session_drinks", null)
   mMainActivity.mDrinksList.clear()
   ```

### Log Fragment Display

- DatePicker selects date
- Shows BAC, duration, drink count
- Tap to view full session details (not implemented - backlog)

---

## Profile Management Workflow

### Profile Fragment Flow

```
┌──────────────────────────────────────────────────────────┐
│                    Profile Fragment                       │
│                                                           │
│  [Sex Radio Group]   [Weight Input]   [Unit Picker]     │
│                                                           │
│  [Favorites List]                                         │
│                                                           │
│  [Clear Favorites]              [Settings]                │
└──────────────────────────────────────────────────────────┘
```

### MVP Pattern

The profile module uses an MVP pattern:

1. **ProfileFragment** (View):
   - Renders UI based on `ProfileView` state
   - Sends `ProfileIntent` actions to presenter

2. **ProfileFragmentPresenter**:
   - Processes intents
   - Manages business logic
   - Emits view models via RXJava stream

3. **ProfileFragmentViewManager**:
   - Receives view models
   - Updates UI components

### Key Actions

| Intent | Action |
|--------|--------|
| `ClearFavorites` | Remove all favorites from database |
| `UpdateProfile` | Save sex, weight, measurement preference |

### Favorites Management

**Clear Favorites Flow**:
```kotlin
// 1. User taps "Clear Favorites" button
// 2. Confirmation dialog shown
// 3. On confirm, send ProfileIntent.ClearFavorites
// 4. Presenter clears favorites in repository
// 5. Repository deletes from favorites table
// 6. View re-renders with empty list
```

---

## Navigation Flow

### MainActivity Bottom Navigation

```
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                          │
│                                                          │
│  ┌──────┐  ┌──────┐  ┌──────┐                          │
│  │ Home │  │  Log │  │Profile │                          │
│  └──────┘  └──────┘  └──────┘                          │
│     ↑         ↑         ↑                                │
│     └─────────┴─────────┘                                │
│          ViewPager                                       │
└─────────────────────────────────────────────────────────┘
```

### Pager Adapter

Three fragments managed by `MyPagerAdapter`:

1. **HomeFragment (Position 0)**
   - BAC calculation and drink list
   - Start/End time controls

2. **LogFragment (Position 1)**
   - Historical session view
   - Date picker navigation

3. **ProfileFragment (Position 2)**
   - User settings
   - Favorites management

### Fragment Transitions

- **Back Stack**: Managed via `pushToBackStack()` in activities
- **Menu Navigation**: Toolbar items context-sensitive per fragment
- **Preference**: State preserved across rotations

---

## Database Operations Workflow

### Database Initialization

1. **On App Start** ([`NightsOutApplication`](https://github.com/fagerbergj/NightsOut/blob/master/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/NightsOutApplication.kt)):
   ```kotlin
   PreferenceManager.getDefaultSharedPreferences(this)
   // Default theme set if not configured
   ```

2. **On Database Access** ([`DatabaseHelper.openDatabase()`](https://github.com/fagerbergj/NightsOut/blob/master/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/DatabaseHelper.kt#L39)):
   ```kotlin
   if (!dbExists()) createDatabase()  // Copy from assets
   if (db.version != version) onUpgrade()
   ```

### Database Queries

**Get Current Session Drinks**:
```kotlin
val cursor = db.query(CURRENT_SESSION_TABLE, null, null, null, null, null, null)
// Join with drinks table for full drink data
```

**Get Favorites**:
```kotlin
val table = "$DRINKS_TABLE, $FAVORITES_TABLE"
val where = "$DRINKS_TABLE.id=favorites.origin_id"
val cursor = db.query(table, null, where, null, null, null, "modifiedTime ASC")
```

### Version Migration

**Current Version**: 40
**Previous Migration**: UUID conversion from integer IDs

Migration steps:
1. Backup existing data
2. Drop all tables
3. Recreate with new schema
4. Restore data with ID mapping

---

## Notification Service Workflow

### BAC Notification Service

**Purpose**: Show current BAC in notification tray

### Life Cycle

```
HomeFragment → UPDATE_NOTIFICATION intent → BacNotificationService
                                        ↓
                                 Update notification
                                        ↓
                         If BAC > 0: continue service
                         If BAC = 0: stop service
```

### Intent Actions

| Action | Purpose |
|--------|---------|
| `START_SERVICE` | Start foreground service |
| `UPDATE_NOTIFICATION` | Refresh BAC display |
| `STOP_SERVICE` | Stop when BAC reaches zero |

---

**Next**: See [`/openwiki/domain/concepts.md`](../domain/concepts.md) for domain model details or [`/openwiki/source-map.md`](../source-map.md) for code location reference.
