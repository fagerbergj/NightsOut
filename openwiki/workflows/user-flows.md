---
type: Playbook
title: NightsOut — User Workflows
description: End-to-end user workflows for the NightsOut BAC calculator app, covering drink logging, session management, profile setup, history tracking, and background notification usage.
---

# User Workflows

This page documents the primary end-to-end user flows in NightsOut. Each flow maps to specific source files so you can trace the implementation.

## 1. First-Time Setup Flow

**User journey:** Install → launch → complete profile → start using app

```
Install
  │
  ▼
Launch (MainActivity → HomeFragment)
  │
  ├── Check: profileInit in SharedPreferences?
  │     ├── true → Show BAC calculator (normal flow)
  │     └── false → Redirect to ProfileFragment (set sex + weight)
  │            │
  │            ▼
  │       Enter Sex (Male/Female toggle)
  │       Enter Weight (numeric input + unit selector)
  │       Tap "Save" → NightsOutSharedPreferences.save()
  │               │
  │               ▼
  │           profileInit = true → Return to HomeFragment
  └──────────────────────────────────────┘
                │
                ▼
          Show BAC calculator (home screen)
```

**Key details:**
- Sex determines the Widmark `r` factor: **0.73** for male, **0.66** for female
- Weight must be >= 20 lbs/kg, otherwise validation error appears via `ProfileViewModel.InvalidSave`
- The app auto-detects the user's locale to set default weight unit (lbs for US, kg elsewhere) and time format (12h for US, 24h elsewhere)
- Source: [`NightsOutSharedPreferences.kt`](/common/src/main/java/com/fagerberg/jason/common/android/NightsOutSharedPreferences.kt), [`profile/ProfileFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragment.kt)

## 2. Adding a Drink — Simple Mode

**User journey:** Home screen → Add drink → Save to session

```
HomeFragment (BAC Calculator)
  │
  ▼
Tap "Add Drink" button
  │
  ▼
AddDrinkActivity starts (simple mode by default)
  │
  ├── Drink name: Auto-complete from database via DrinkSuggestionAutoCompleteView
  │     │
  │     └── Database queries AddDrinkDatabaseHelper.getDrinkSuggestions(nameQuery)
  │         → Returns matching drinks filtered by dontSuggest = false and recent status
  │
  ├── ABV: User enters percentage (e.g., "5.0" for 5%)
  ├── Amount: User enters quantity + selects measurement (beers, shots, oz, etc.)
  │
  ├── Tap "Add Drink" → validate inputs
  │     ├── Name required? No → error
  │     ├── ABV < 100%? No → error
  │     └── Amount reasonable? No → error
  │
  ├── On success:
  │     ├── AddDrinkDatabaseHelper.addNewDrink(Drink) → INSERT into drinks table
  │     ├── MainActivity.currentSessionDrinks.add(drink) → In-memory list updated
  │     ├── MainActivity.setPreference(drinkCount++) → Increment drinksAddedCount
  │     └── DrinkSuggestionAutoCompleteView.updateRecentFlag(drink.id, true) → Mark recent
  │
  ▼
HomeFragment onResume → setupRecycler() → HomeFragmentDrinkListAdapter shows new drink
```

**Key details:**
- The drink's `recent` flag is set to `true` after first use, boosting it in autocomplete suggestions
- If the user manually edits a suggested drink name (e.g., "Bud Ligt" → "Bud Light"), the app marks the original entry as `dontSuggest = true` so it won't appear in future suggestions
- Source: [`AddDrinkActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt), [`DrinkSuggestionAutoCompleteView.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/drinkSuggestion/DrinkSuggestionAutoCompleteView.kt)

## 3. Adding a Drink — Complex Mode

**User journey:** Combine multiple alcoholic beverages into one logical drink (e.g., a mixed cocktail)

```
AddDrinkActivity (simple mode active)
  │
  ▼
Tap "Complex" button or equivalent complex-mode trigger
  │
  ▼
ComplexDrinkHelper.findViews(activity) → Unhides alcohol source RecyclerView
  │
  ├── User enters ABV + amount + measurement for first source
  │     └── Tap "Add Another Alcohol Source"
  │         ├── ComplexDrinkHelper.addToAlcoholSourceList() validates inputs
  │         ├── Creates AlcoholSource(abv, amount, measurement)
  │         ├── Adds to listAlcoholSources ArrayList
  │         └── Adapter notifies RecyclerView of new item
  │
  ├── Repeat for additional sources (Beer at 5%, Rum at 40%)
  │
  ├── Finalize drink: User enters name and taps "Add Drink"
  │     ├── ComplexDrinkHelper.weightedAverageAbv() computes weighted-average ABV:
  │     │      totalVolume = Σ(amount × volumeFactor) for each source
  │     │      avgABV = Σ((source.abv × source.volume) / totalVolume)
  │     │
  │     ├── Create single Drink entry with computed weighted-average ABV and combined volume
  │     ├── Save via AddDrinkDatabaseHelper.addNewDrink(compositeDrink)
  │     └── Add to current session in-memory list
  │
  ▼
HomeFragment recalculates BAC using composite drink's contribution
```

**Key details:**
- The `AlcoholSource` inner class holds `(abv, amount, measurement)` for each source
- Volume weighting uses fluid ounces as the common denominator (all measurements converted via `volumeConversionMap`)
- Source: [`ComplexDrinkHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/ComplexDrinkHelper.kt)

## 4. BAC Calculation — Real-Time Updates

**User journey:** Add drinks → see live BAC update → adjust times → re-calculate

```
HomeFragment state (already has drinks list)
  │
  ├── User adds a drink (via AddDrinkActivity → back to HomeFragment)
  ├── User adjusts start time via TimePickerDialog
  ├── User adjusts end time via TimePickerDialog or "Now" quick button
  └── User removes a drink (swipe-to-remove on HomeFragmentDrinkListAdapter)
        │
        ▼
HomeFragment.calculateBAC() called in all cases:
  │
  ├── 1. Retrieve profile from NightsOutSharedPreferences (sex, weight, weightMeasurement)
  │     ├── Convert weight to lbs if kg
  │     └── Determine r factor: 0.73 (male) or 0.66 (female)
  │
  ├── 2. For each drink in current session:
  │     ├── fluidOz = amount × volumeConversionFactor(measurement)
  │     ├── pureAlcoholOz = fluidOz × (abv / 100)
  │     └── totalPureAlcohol += pureAlcoholOz
  │
  ├── 3. instantBAC = (totalPureAlcohol × 5.14) / (weightInLbs × r)
  │
  ├── 4. hoursElapsed = (endTimeMin - startTimeMin) / 60.0
  │     finalBAC = max(0, instantBAC - (hoursElapsed × 0.015))
  │
  └── 5. updateBACText(finalBAC) → Updates displayed BAC text + graphView chart

GraphView renders BAC decline curve from current time to projected zero-BAC time
```

**Key details:**
- `drinkingDuration` and `standardDrinksConsumed` are computed alongside the main calculation for reference display
- The GraphView chart shows: (1) horizontal line at legal limit (0.08%), (2) projected decline curve from current BAC toward zero
- Source: [`HomeFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt), [`ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt)

## 5. Saving a Session to Log

**User journey:** Finish drinking → save session with final BAC for historical tracking

```
HomeFragment (current session complete)
  │
  ▼
Tap toolbar "Save" button (home_menu: btn_toolbar_home_done)
  │
  ├── HomeFragmentLogDatePicker.showDatePicker() opens calendar date picker
  │
  ▼
User selects a date in MaterialCalendarView
  │
  ▼
LogFragment loads for that date
  │
  ├── Tap toolbar "Save to Log" action on LogFragment
  │     │
  │     ├── Convert current session drinks to log format:
  │     │     ├── LogDatabaseHelper.insertLog(LogHeader(date, bac, durationMinutes))
  │     │     │    → INSERT INTO log VALUES (date, bac, duration)
  │     │     │
  │     │     └── For each drink in session:
  │     │            LogDatabaseHelper.insertLogDrinks(date, drinkId)
  │     │                → INSERT INTO log_drink VALUES (log_date, drink_id)
  │     │
  │     ├── MainActivity.currentSessionDrinks.clear() → In-memory session reset
  │     └── MainActivity.setPreference(drinkCount++) → Increment rating trigger counter
  │
  ▼
MaterialCalendarView decorator adds colored dot for this date
```

**Key details:**
- The user can choose any past/future date for the log entry (not just today)
- After saving, `MainActivity` persists the cleared session to SharedPreferences/SQLite on next `onStop()`
- Rating dialog trigger: after `drinksAddedCount >= 5` AND `daysSinceInstall >= 3`, a rate-the-app dialog may appear (but google-services.json was removed; this feature is currently inert)
- Source: [`HomeFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt), [`LogFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragment.kt), [`AddDrinkActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt)

## 6. Viewing Session History (Log)

**User journey:** Log tab → browse dates by session → view drinks consumed on each date

```
MainActivity → BottomNav: [Log] (index 1)
  │
  ▼
LogFragment.onResume() → setupRecycler(view)
  │
  ├── MaterialCalendarView decorated with colored dots for logged dates
  │     └── Log decorator queries LogDatabaseHelper.readLogs() → returns sorted LogHeaders
  │
  ▼
User taps a date on the calendar
  │
  ▼
LogFragmentAdapter loads drinks for that date:
  │
  ├── Query LogDatabaseHelper.getDrinksForLogDate(selectedDate) → List<Drink>
  │     └── JOIN log_drink + drinks tables on drink_id
  │
  ├── Adapter creates headers (date, BAC, durationString)
  │   followed by individual drink items
  │
  └── RecyclerView renders the full session breakdown

Toolbar actions available:
  ├── "Clear All Logs" → LogDatabaseHelper.deleteAllLogs() + adapter reset
  ├── "Clear Selected Day" → Delete log entry + all associated log_drink entries for date
  └── "Move to..." → Update log_date in both log and log_drink tables
```

**Key details:**
- Color-coded dots: green for BAC ≥ 0.08 (legally impaired), red for BAC ≥ 0.15 (heavily impaired)
- Each day shows the final BAC reading and total drinking duration from the `log` table
- Source: [`LogFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragment.kt), [`LogFragmentAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragmentAdapter.kt)

## 7. Managing Favorites

**User journey:** Add drinks → favorite them → quick-access from Home or Profile screen

```
AddDrinkActivity (adding a drink)
  │
  ▼
Tap "star/favorite" button on the drink entry
  │
  ├── AddDrinkDatabaseHelper.addFavorite(drinkName, originId) → INSERT into favorites table
  │     └── Updates MainActivity.favoritesList in-memory (ArrayList<Drink>)
  │
  ▼
ProfileFragment (horizontal favorites RecyclerView updates)
  │
  ├── Tap a favorite drink → Adds it to current session immediately
  │     ├── Create Drink instance from favorite data
  │     ├── Add to MainActivity.currentSessionDrinks
  │     └── Navigate back to HomeFragment
  │
  └── Swipe-to-delete favorite (ProfileFragmentFavoritesListAdapter):
        ├── Removes from favorites RecyclerView
        ├── AddDrinkDatabaseHelper.removeFavorite(drink) → DELETE from favorites table
        └── Updates MainActivity.favoritesList in-memory

HomeFragment "Add Drink" screen also shows favorites panel at top:
  ├── Horizontal RecyclerView of all favorite drinks
  ├── Tap any favorite → Auto-fills name, ABV, amount fields (pre-fill mode)
  └── Editable — user can modify ABV/amount before adding to session
```

**Key details:**
- Favorites persist across app restarts (stored in SQLite `favorites` table)
- The favorites panel appears in both the ProfileFragment tab and the AddDrinkActivity screen
- Source: [`ProfileFragmentFavoritesListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragmentFavoritesListAdapter.kt), [`AddDrinkActivityFavoritesListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivityFavoritesListAdapter.kt)

## 8. BAC Notification Service

**User journey:** Enable notification → leave app → see BAC update on lock screen → add drink from notification

```
SettingsActivity
  │
  ▼
Toggle "Show Current BAC Notification" = ON
  │
  ├── MainActivity starts BacNotificationService via intent:
  │     action = START_SERVICE
  │
  ▼
BacNotificationService.onCreate() → Build persistent notification
  │
  ├── Notification shows current BAC value (e.g., "BAC: 0.042")
  │   and time range ("8:00 PM - 10:30 PM")
  │
  ├── Action buttons in notification:
  │     ├── "Add Drink" → Opens AddDrinkActivity from background
  │     └── "Update" → Triggers REFRESH_BAC action
  │         ├── BacNotificationService calculates current BAC using last known end time = now
  │         ├── Updates notification text with new BAC value
  │         └── If HomeFragment is visible (via NightsOutApplication.mCurrentActivity):
  │               ├── Shows toast "End time updated by notification"
  │               ├── Pushes updated BAC to HomeFragment.updateBACText(bac)
  │               └── Updates SharedPreferences end time
  │
  ▼
App backgrounded or foregrounded → Service continues running
  (notification persists across screen-off, home button, app switching)

User switches back to app → HomeFragment shows updated BAC from service push
```

**Key details:**
- The service starts after the app resumes to ensure it only runs when the user is actively drinking
- The notification survives process death — Android will recreate it based on SharedPreferences state
- Source: [`BacNotificationService.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/BacNotificationService.kt), [`SettingsActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/settings/SettingsActivity.kt)

## 9. Managing the Drink Database

**User journey:** Browse all catalog drinks → search → clean/reset database

```
HomeFragment toolbar → "Manage DB" (toolbar_manage_db icon)
  │
  ▼
ManageDBActivity opens
  │
  ├── RecyclerView lists ALL drinks from drinks table, ordered by name
  │     └── ManageDBDrinkListAdapter with search input field
  │
  ├── Search: Type in text → Filter adapter's list to show only matching names
  │
  ├── "Clean DB" button:
  │     ├── Finds all drinks NOT referenced in favorites or current_session_drinks or log_drink
  │     └── Delete them (removes unused/orphaned drink definitions)
  │
  └── "Reset DB" button:
        ├── Restores the default pre-populated database from assets/
        └── Deletes all user-added drinks while preserving favorites and logs

Source: [`ManageDBActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/manageDB/ManageDBActivity.kt)
```

**Key details:**
- "Clean DB" removes only truly orphaned entries — any drink that's been used (in session, log, or favorite) is protected
- "Reset DB" deletes the entire database file and re-copies from assets/, effectively restoring to factory defaults
- Source: [`ManageDBDrinkListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/manageDB/ManageDBDrinkListAdapter.kt)

## Workflow Diagram — Full Navigation Map

```
                    ┌─────────────────────────────────────┐
                    │        MainActivity (Launcher)      │
                    │                                     │
            ┌───────┼───────────┼─────────────────────────┤
            ▼       ▼           ▼                         │
     ┌──────────┐ ┌──────┐ ┌───────────┐                  │
     │   Home   │ │ Log  │ │  Profile  │                  │
     │ Fragment │ │Fragment│ │Fragment   │                  │
     └─────┬────┘ └──┬───┘ └─────┬─────┘                  │
           │         │           │                         │
    Add Drink│    Select Date│  Edit Profile               │
    (→AddDrink)▼           ▼   ▼                          │
   ┌──────────────────────┐ ┌────────┐                    │
   │ BAC Calculator +     │ │ Log    │                    │
   │ GraphView Chart      │ │Detail  │                    │
   └──────────────────────┘ └────────┘                    │
           │         │           │                        │
    Save To│Log     │       Add Favorite                  │
    Settings│        ▼          │                         │
    (→Settings)Manage DB      ▼                          │
                    ┌────────────────┐                     │
                    │  Log Fragment  │                     │
                    │  (with calendar│                     │
                    │   decoration)  │                     │
                    └────────────────┘                     │
                                                           │
              Notification Service (background, persistent)─┘
```

## Source Path Index for Workflows

| Workflow | Primary Source Files |
|----------|---------------------|
| First-time setup | [`NightsOutSharedPreferences.kt`](/common/src/main/java/com/fagerberg/jason/common/android/NightsOutSharedPreferences.kt), [`profile/ProfileFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragment.kt) |
| Adding drinks (simple) | [`AddDrinkActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/AddDrinkActivity.kt), [`DrinkSuggestionAutoCompleteView.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/drinkSuggestion/DrinkSuggestionAutoCompleteView.kt) |
| Adding drinks (complex) | [`ComplexDrinkHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/ComplexDrinkHelper.kt) |
| BAC calculation | [`HomeFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt), [`BacNotificationService.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/BacNotificationService.kt) |
| Logging sessions | [`LogFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/log/LogFragment.kt), [`HomeFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt) |
| Favorites | [`ProfileFragmentFavoritesListAdapter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragmentFavoritesListAdapter.kt), [`AddDrinkDatabaseHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/databaseHelper/AddDrinkDatabaseHelper.kt) |
| Background notification | [`BacNotificationService.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/BacNotificationService.kt), [`SettingsActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/settings/SettingsActivity.kt) |
| Database management | [`ManageDBActivity.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/manageDB/ManageDBActivity.kt), [`SimpleDatabaseManager.kt`](/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManager.kt) |
