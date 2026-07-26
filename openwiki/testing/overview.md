---
type: Reference
title: NightsOut — Testing
description: Complete reference for the NightsOut test suite, including unit tests in the common module and instrumented tests in the db module. Covers test framework, what is tested, and gaps in coverage.
---

# Testing

NightsOut's test suite is split between **unit tests** (JVM-based) in the `:common` module and **instrumented tests** (Android device/emulator) in the `:db` module. The `:app` module has no dedicated test files.

## Test Framework

| Component | Library | Version |
|-----------|---------|---------|
| **Unit test framework** | `junit:junit` | 4.12 |
| **AndroidX test runner** | `androidx.test.ext:junit` | 1.1.1 |
| **UI testing** | `espresso-core` | 3.2.0 |
| **Assertion library** | `assertj-core` | 3.12.2 (common module only) |
| **Mocking framework** | `mockk` | 1.9.3 (common module only) |

Both test types are available:
- **Unit tests:** Run on the JVM via `./gradlew testDebugUnitTest` — these run in CI
- **Instrumented tests:** Run on connected Android devices via `./gradlew connectedAndroidTest` — these require physical device or emulator and are NOT run in CI

## Unit Tests (:common Module)

**Source Path:** [`/common/src/test/java/com/fagerberg/jason/common/`](/common/src/test/java/com/fagerberg/jason/common/)

The `:common` module has the most comprehensive test coverage, testing domain models, utility functions, and Android base classes in isolation.

### Test Files

| Test Class | Tests | Purpose |
|------------|-------|---------|
| [`ConversionUtilsTest.kt`](/common/src/test/java/com/fagerberg/jason/common/utils/ConversionUtilsTest.kt) | Volume conversions | Verifies each `VolumeMeasurement` converts to the correct fluid ounce factor (oz→1.0, ml→0.033814, beers→12.0, shots→1.5, wine glasses→5.0, pints→16.0) |
| [`WeightMeasurementTest.kt`](/common/src/test/java/com/fagerberg/jason/common/models/WeightMeasurementTest.kt) | Weight conversions | Verifies `toLbs()` works correctly for both `LBS` and `KG` enum values (1 kg → 2.205 lbs) |
| [`VolumeMeasurementTest.kt`](/common/src/test/java/com/fagerberg/jason/common/models/VolumeMeasurementTest.kt) | Enum parsing | Verifies `fromLowercaseString()` correctly maps locale-specific strings to enum values |
| [`TimeUtilsTest.kt`](/common/src/test/java/com/fagerberg/jason/common/utils/TimeUtilsTest.kt) | Time conversions | Verifies military time ↔ minutes conversion (`militaryHoursAndMinutesToMinutes(14, 30) = 870`) |
| [`CountryUtilsTest.kt`](/common/src/test/java/com/fagerberg/jason/common/utils/CountryUtilsTest.kt) | Locale detection | Verifies `isCountryThatUsesLbs()` and `isCountryThatUses12HourTime()` return correct defaults for various country codes (US → lbs/12h, GB → kg/24h, etc.) |
| [`LogHeaderTest.kt`](/common/src/test/java/com/fagerberg/jason/common/models/LogHeaderTest.kt) | Derived properties | Verifies `year`, `month`, `day`, `monthName`, `dateString`, and `durationString` are computed correctly from a raw YYYYMMDD integer |
| [`NightsOutSharedPreferencesTest.kt`](/common/src/test/java/com/fagerberg/jason/common/android/NightsOutSharedPreferencesTest.kt) | SharedPreferences wrapper | Tests that `update()` correctly writes all fields atomically to the underlying SharedPreferences and returns an updated copy |

### Test Infrastructure

The common module uses **MockK** for mocking Android dependencies (e.g., `PreferenceManager`, `Context`) in unit tests, allowing pure JVM testing without an emulator. **AssertJ** provides fluent assertions (`assertThat(actual).isEqualTo(expected)`) for more readable test code than standard JUnit assertions.

### Test Data Fixtures

| File | Purpose |
|------|---------|
| [`/common/src/main/java/com/fagerberg/jason/common/models/test/ModelMocks.kt`](/common/src/main/java/com/fagerberg/jason/common/models/test/ModelMocks.kt) | Provides pre-constructed `Drink` and `LogHeader` instances for use in test assertions |

## Instrumented Tests (:db Module)

**Source Path:** [`/db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt`](/db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt)

The `:db` module has a single instrumented test class that runs on an Android device or emulator. It tests actual SQLite operations with the pre-populated database file.

### Test Cases Covered

| Test | Purpose |
|------|---------|
| Database copy from assets | Verifies `nights_out_db.db` is correctly extracted to the device's data directory |
| Schema correctness | Asserts all 5 tables exist with correct column names and types |
| CRUD operations — drinks | Insert a drink, read it back by ID, update its ABV, delete it |
| CRUD operations — favorites | Add favorite, query favorites list, remove specific favorite, clear all favorites |
| CRUD operations — log entries | Insert LogHeader with BAC/duration, query logs by date range, delete a day's log |
| Junction table integrity | Verify `log_drink` correctly associates multiple drinks with a single logged date |

### Test Runner Configuration

```groovy
defaultConfig {
    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}
```

Instrumented tests require:
- A connected Android device (USB debugging enabled) or running emulator
- Minimum API 19 (same as the app's minSdkVersion)
- The app to be installed on the test device with `testApplicationId` matching the main app

## Test Coverage Gaps

| Module | Status | Gap Details |
|--------|--------|-------------|
| **`:common`** | ✅ Good | Unit tests cover all utility functions, models, and SharedPreferences logic |
| **`:db`** | ⚠️ Partial | Instrumented tests verify CRUD but do NOT test schema migration (`onUpgrade`) paths for version transitions (e.g., v39 → v40) |
| **`:profile`** | ❌ None | No test files exist despite the module implementing complex MVI state management — this is the highest-priority area for new tests. The `AbstractPresenter` pipeline could be unit-tested by mocking the repository and asserting state transitions. |
| **`:common-dialog`** | ❌ None | Dialog components are UI-only; testing would require instrumented tests with mock activities, which may not justify the effort |
| **`:app`** | ❌ None | No test files exist. The main `HomeFragment.calculateBAC()` method — the core business logic of the app — has no dedicated unit test coverage |

## Testing Recommendations

### Priority 1: Add HomeFragment BAC Test (`:common` module utility)

The Widmark calculation is currently tested indirectly through the BacNotificationService code but lacks a dedicated standalone test. Extract `calculateBAC()` into a pure utility function in the `:common` module and write tests for:
- Correct Widmark formula output with known inputs (e.g., 2 beers at 5% ABV, 170 lb male → ~0.068 BAC before time decay)
- Time decay factor at various elapsed times
- Sex-based r factor application (0.73 vs 0.66)
- Floor-to-zero behavior (BAC never goes below 0.0)

### Priority 2: ProfilePresenter Unit Tests (`:profile` module)

The MVI presenter is ideal for unit testing because it accepts `Intent` inputs and produces `ViewModel` outputs through a deterministic pipeline. Tests should verify:
- `intentToAction()` correctly maps each intent to its corresponding action
- `actionToResult()` returns the expected RxJava Observable result type
- `stateReducer()` transitions from the correct previous state to the correct next state for each result

### Priority 3: AddDrinkActivity Integration Test (`:app` module)

An instrumented test using Espresso could verify:
- Adding a drink with valid inputs updates the RecyclerView list
- Invalid inputs (empty name, ABV ≥ 100%) show validation errors
- Autocomplete suggestions appear and can be selected
- Complex mode adds multiple alcohol sources that are correctly weighted

### Priority 4: Database Upgrade Migration Tests (`:db` module)

The `onUpgrade()` path is the most fragile part of the database layer because it relies on manual save/restore during schema changes. Tests should verify:
- Existing drink data survives upgrade from v39 → v40 (UUID migration)
- Current session drinks are re-inserted in correct order after upgrade
- Favorite mappings survive table drop and rebuild
- Log entries with BAC values and durations are preserved

## Running Tests Locally

```bash
# Run all JVM unit tests (fast, no device needed)
./gradlew testDebugUnitTest

# Run instrumented tests on connected device/emulator (slow)
./gradlew connectedAndroidTest

# Run only common module unit tests
./gradlew :common:testDebugUnitTest

# Run only db module instrumented tests
./gradlew :db:connectedAndroidTest

# Build and run both in one command
./gradlew assembleDebug testDebugUnitTest
```

## Source Path Index

| Area | Path |
|------|------|
| Unit tests root | [`/common/src/test/java/com/fagerberg/jason/common/`](/common/src/test/java/com/fagerberg/jason/common/) |
| Utility tests | [`/common/src/test/java/com/fagerberg/jason/common/utils/ConversionUtilsTest.kt`](/common/src/test/java/com/fagerberg/jason/common/utils/ConversionUtilsTest.kt), [`TimeUtilsTest.kt`](/common/src/test/java/com/fagerberg/jason/common/utils/TimeUtilsTest.kt), [`CountryUtilsTest.kt`](/common/src/test/java/com/fagerberg/jason/common/utils/CountryUtilsTest.kt) |
| Model tests | [`/common/src/test/java/com/fagerberg/jason/common/models/VolumeMeasurementTest.kt`](/common/src/test/java/com/fagerberg/jason/common/models/VolumeMeasurementTest.kt), [`WeightMeasurementTest.kt`](/common/src/test/java/com/fagerberg/jason/common/models/WeightMeasurementTest.kt), [`LogHeaderTest.kt`](/common/src/test/java/com/fagerberg/jason/common/models/LogHeaderTest.kt) |
| Android base tests | [`/common/src/test/java/com/fagerberg/jason/common/android/NightsOutSharedPreferencesTest.kt`](/common/src/test/java/com/fagerberg/jason/common/android/NightsOutSharedPreferencesTest.kt) |
| Test fixtures | [`/common/src/main/java/com/fagerberg/jason/common/models/test/ModelMocks.kt`](/common/src/main/java/com/fagerberg/jason/common/models/test/ModelMocks.kt) |
| Instrumented tests | [`/db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt`](/db/src/androidTest/java/com/wit/jasonfagerberg/nightsout/db/SimpleDatabaseManagerTest.kt) |
