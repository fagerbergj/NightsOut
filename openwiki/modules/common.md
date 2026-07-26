---
type: Module Reference
title: NightsOut — Common Module
description: Detailed reference for the :common module, the shared library providing domain models, utility functions, MVI presenter base class, and Android base classes reused across all NightsOut modules.
---

# Common Module (`:common`)

The `:common` module is the foundation of the NightsOut project. It provides shared domain models, conversion utilities, MVI architecture components, and Android base classes used by every other module.

## Package Structure

```
/common/src/main/java/com/fagerberg/jason/common/
├── android/                       # Android base classes
│   ├── AbstractPresenter.kt       # MVI presenter base (RxJava-based)
│   ├── NightsOutActivity.kt       # Base activity with theme/back-stack management
│   ├── NightsOutApplication.kt    # Application singleton (shared version)
│   └── NightsOutSharedPreferences.kt  # SharedPreferences data class wrapper
├── constants/                     # Global constants shared by all modules
│   ├── Constants.kt               # DB name, path, version, volume measurement arrays
│   └── SharedPreference.kt        # All SharedPreferences key definitions
├── models/                        # Domain data types
│   ├── Drink.kt                   # Central drink entity (data class with UUID)
│   ├── LogHeader.kt               # Daily session summary wrapper
│   ├── VolumeMeasurement.kt       # Enum: oz, ml, beers, shots, wine glasses, pints
│   ├── WeightMeasurement.kt       # Enum: lbs, kg
│   └── test/
│       └── ModelMocks.kt          # Test fixtures
├── utils/                         # Conversion & utility functions
    ├── ConversionUtils.kt         # Volume/weight conversion factors, alcohol grams
    ├── CountryUtils.kt            # Locale-based defaults (12h/24h, lbs/kg)
    └── TimeUtils.kt               # Military time conversions
```

## Core Responsibilities

| Responsibility | Description | Source Path |
|----------------|-------------|-------------|
| **Domain models** | `Drink`, `LogHeader`, `VolumeMeasurement`, `WeightMeasurement` — used by every module | [`models/`](/common/src/main/java/com/fagerberg/jason/common/models/) |
| **Conversion functions** | Volume-to-fluid-oz mapping, weight-to-pounds conversion, alcohol grams calculation | [`utils/ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt) |
| **Locale-aware defaults** | Auto-detect country to set default time format and weight unit | [`utils/CountryUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/CountryUtils.kt) |
| **Time utilities** | Military hours+minutes ↔ total minutes, decimal time parsing | [`utils/TimeUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/TimeUtils.kt) |
| **MVI presenter base** | RxJava-reactive `AbstractPresenter<Intent, Action, Result, ViewModel>` for state management | [`android/AbstractPresenter.kt`](/common/src/main/java/com/fagerberg/jason/common/android/AbstractPresenter.kt) |
| **Activity base class** | Theme loading, back-stack tracking, SharedPreferences setup, current activity registration | [`android/NightsOutActivity.kt`](/common/src/main/java/com/fagerberg/jason/common/android/NightsOutActivity.kt) |
| **SharedPreferences wrapper** | Type-safe data class `NightsOutSharedPreferences` with auto-save on any field change | [`android/NightsOutSharedPreferences.kt`](/common/src/main/java/com/fagerberg/jason/common/android/NightsOutSharedPreferences.kt) |
| **Constants** | DB name/path/version, shared preference key definitions, volume measurement display arrays | [`constants/Constants.kt`](/common/src/main/java/com/fagerberg/jason/common/constants/Constants.kt), [`constants/SharedPreference.kt`](/common/src/main/java/com/fagerberg/jason/common/constants/SharedPreference.kt) |

## MVI Presenter Architecture

The `AbstractPresenter` class is the most architecturally significant component in `:common`. It implements a full MVI (Model-View-Intent) pattern using RxJava and RxRelay:

**Source Path:** [`android/AbstractPresenter.kt`](/common/src/main/java/com/fagerberg/jason/common/android/AbstractPresenter.kt)

### Pipeline

```
User Action
    ↓  PublishRelay.accept()
[Intent]
    ↓  .observeOn(Schedulers.io()) — switch to background thread
.map(::intentToAction)
    ↓  Abstract method — subclasses map Intents → Actions
[Action]
    ↓  .flatMap(::actionToResult) — execute side effects (DB calls, API calls)
Observable<Result>
    ↓  .scan(initialViewModel, ::stateReducer) — accumulate state changes
[New ViewModel State]
    ↓  BehaviorRelay emits to main thread subscriber
.observeOn(AndroidSchedulers.mainThread())
    ↓  View receives latest state and renders
```

### Key Design Decisions

1. **`PublishRelay` for Intents:** Captures user actions as they happen; `toSerialized()` makes it thread-safe so intents can be submitted from any thread (e.g., from background callbacks).

2. **`BehaviorRelay` for ViewModel State:** Unlike a plain `Observable`, `BehaviorRelay` holds the latest emitted value and replays it to new subscribers. This is critical for Android Fragments/Activities that may subscribe at different lifecycle points.

3. **`.scan()` for State Reduction:** The reducer function receives both `previousState` and `result`, returning a new state. This creates an immutable state chain where every user action produces a deterministic state transition.

4. **Lifecycle awareness:** Extends `AndroidViewModel` so it's tied to the ViewModel lifecycle. `onCleared()` disposes all Rx subscriptions, preventing memory leaks.

5. **Scheduler discipline:** `Schedulers.io()` for async operations (DB queries, file I/O) and `AndroidSchedulers.mainThread()` for state emission to the UI layer.

### Subclass Implementation Contract

Subclasses must implement three abstract methods:

```kotlin
abstract class AbstractPresenter<Intent, Action, Result, ViewModel>(
    initialViewModel: ViewModel
) : AndroidViewModel() {

    // Map user intents to business logic actions
    abstract fun intentToAction(intent: Intent): Action

    // Execute the action and return an Observable of results (can be async)
    abstract fun actionToResult(action: Action): Observable<Result>

    // Combine previous state + new result into next state
    abstract fun stateReducer(previousState: ViewModel, result: Result): ViewModel
}
```

The `:profile` module is the only known implementation of this pattern in the codebase. See **[Profile Module](./profile.md)** for a concrete example.

## NightsOutSharedPreferences

**Source Path:** [`android/NightsOutSharedPreferences.kt`](/common/src/main/java/com/fagerberg/jason/common/android/NightsOutSharedPreferences.kt)

A Kotlin `data class` that wraps `SharedPreferences` with type-safe getters and an immutable `update()` method:

```kotlin
data class NightsOutSharedPreferences(
    val profileInit: Boolean,
    val sex: Boolean?,              // null = not yet set
    val weight: Double,
    val weightMeasurement: WeightMeasurement,
    val startTimeMin: Int,
    val endTimeMin: Int,
    val use24HourTime: Boolean,
    val dateInstalled: Long,
    val drinksAddedCount: Int,
    val dontShowRateDialog: Boolean,
    val dontShowCurrentBacNotification: Boolean,
    val showBacNotification: Boolean,
    val activeTheme: Int
)
```

The `update()` method creates a new copy with changed fields and atomically writes all fields to SharedPreferences via the editor. This ensures consistency — any change triggers a full write of all preferences, which is acceptable given there are only ~15 keys.

### Locale-Aware Defaults

`NightsOutSharedPreferences` constructor logic auto-detects defaults based on user's locale:
- **Weight unit:** Uses `isCountryThatUsesLbs()` to default to lbs (US) or kg (rest of world)
- **Time format:** Uses `isCountryThatUses12HourTime()` to default to 12h or 24h

### SharedPreferences Keys

All preference keys are defined in [`SharedPreference.kt`](/common/src/main/java/com/fagerberg/jason/common/constants/SharedPreference.kt):

| Key | Type | Used By |
|-----|------|---------|
| `profileInit` | Boolean | Profile completion check |
| `sex` | Boolean (true=male) | BAC calculation (Widmark r factor) |
| `weight` | Float | BAC calculation |
| `weightMeasurement` | String | Weight conversion |
| `startTimeMin` | Int | BAC duration calc |
| `endTimeMin` | Int | BAC duration calc |
| `use24HourTime` | Boolean | Time display formatting |
| `dateInstalled` | Long | Rating dialog timing |
| `drinksAddedCount` | Int | Rating dialog trigger |
| `dontShowRateDialog` | Boolean | Rating dialog suppression |
| `showBacNotification` | Boolean | Notification service toggle |
| `activeTheme` | Int | Theme application |

## NightsOutActivity (Shared Base)

**Source Path:** [`android/NightsOutActivity.kt`](/common/src/main/java/com/fagerberg/jason/common/android/NightsOutActivity.kt)

All activities in the app extend this abstract class. Key features:

| Feature | Method | Description |
|---------|--------|-------------|
| **Theme loading** | `onCreate()` | Loads theme from SharedPreferences before `super.onCreate()` so window background is correct |
| **Back-stack tracking** | `pushToBackStack(i)` + `mBackStack` (Stack) | Maintains activity navigation history with max depth of 10 entries |
| **Instance state persistence** | `onSaveInstanceState()` / `onResume()` | Saves/restores back stack and fragment ID via Intent extras |
| **Current activity registration** | `onResume()` / `onPause()` | Registers this activity as `NightsOutApplication.mCurrentActivity` for service → UI communication |
| **SharedPreferences initialization** | `onCreate()` | Loads `NightsOutSharedPreferences` from PreferenceManager on startup |

## Constants

**Source Paths:** [`constants/Constants.kt`](/common/src/main/java/com/fagerberg/jason/common/constants/Constants.kt), [`constants/SharedPreference.kt`](/common/src/main/java/com/fagerberg/jason/common/constants/SharedPreference.kt)

### Database Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `DB_NAME` | `"nights_out_db.db"` | Database filename (matches pre-populated asset) |
| `DB_PATH` | `"data/data/com.wit.jasonfagerberg.nightsout/nights_out_db.db"` | Absolute path on device |
| `DB_VERSION` | `40` | Current schema version number |

### Navigation Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `BACK_STACK` | `"BACK_STACK"` | Intent extra key for serializing back stack between activities |
| `FRAGMENT_ID` | `"FRAGMENT_ID"` | Intent extra key for fragment identity |
| `MAX_BACK_STACK_SIZE` | `10` | Maximum number of entries in the back-stack |

### Volume Measurement Arrays

| Constant | Value | Description |
|----------|-------|-------------|
| `VOLUME_MEASUREMENTS` | List from enum values | Full list of display names |
| `VOLUME_MEASUREMENTS_METRIC_FIRST` | `[ml, oz, beers, shots, wine glasses, pints]` | UI order for metric-first locales |

## ConversionUtils (Mathematical Constants)

**Source Path:** [`utils/ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt)

### Volume → Fluid Ounce Mapping

```kotlin
val volumeConversionMap = mapOf(
    VolumeMeasurement.OZ       to 1.0,
    VolumeMeasurement.ML       to 0.033814,
    VolumeMeasurement.BEERS    to 12.0,
    VolumeMeasurement.WINE_GLASSES to 5.0,
    VolumeMeasurement.SHOTS    to 1.5,
    VolumeMeasurement.PINTS    to 16.0
)
```

### Weight → Pounds Mapping

```kotlin
val weightConversionMap = mapOf(
    WeightMeasurement.LBS to 1.0,
    WeightMeasurement.KG  to 2.205
)
```

### Alcohol Conversion

| Function | Formula | Purpose |
|----------|---------|---------|
| `VolumeMeasurement.toFluidOz(amount)` | `amount × volumeConversionMap[this]` | Convert drink amount to fluid oz |
| `WeightMeasurement.toLbs(weight)` | `weight × weightConversionMap[this]` | Convert weight to pounds for Widmark formula |
| `fluidOzToGramsOfAlcohol(foz)` | `23.3333333 × foz` | Convert fluid oz of alcohol to grams (used in BAC numerator) |

### Time Conversions

| Function | Purpose | Source |
|----------|---------|--------|
| `militaryHoursAndMinutesToMinutes(hour, min)` | `hour × 60 + min` | [`utils/TimeUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/TimeUtils.kt) |
| `getCurrentTimeInMinuets()` | Returns current time in minutes since midnight | [`utils/TimeUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/TimeUtils.kt) |

## Source Paths Summary

| Area | Directory Path |
|------|---------------|
| Android base classes | [`/common/src/main/java/com/fagerberg/jason/common/android/`](/common/src/main/java/com/fagerberg/jason/common/android/) |
| Constants | [`/common/src/main/java/com/fagerberg/jason/common/constants/`](/common/src/main/java/com/fagerberg/jason/common/constants/) |
| Domain models | [`/common/src/main/java/com/fagerberg/jason/common/models/`](/common/src/main/java/com/fagerberg/jason/common/models/) |
| Utilities | [`/common/src/main/java/com/fagerberg/jason/common/utils/`](/common/src/main/java/com/fagerberg/jason/common/utils/) |
| Unit tests | [`/common/src/test/java/com/fagerberg/jason/common/`](/common/src/test/java/com/fagerberg/jason/common/) |

## Design Notes & Caveats

- **Duplicate of app module classes:** `NightsOutActivity`, `NightsOutApplication`, and `Drink`/`LogHeader` exist in both the `:app` and `:common` modules. The app should migrate to use the common versions exclusively to avoid code divergence.
- **AbstractPresenter has only one production implementation** (in the `:profile` module). The rest of the app uses direct data binding. Consider whether other screens would benefit from MVI or if the pattern should be documented as an optional architecture for complex screens.
- **No ProGuard rules defined:** The common module's `build.gradle` does not include consumer ProGuard rules, meaning minification in the app may strip needed reflection (RxJava schedulers, Kotlin metadata). Verify with release builds.
