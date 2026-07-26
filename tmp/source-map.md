---
type: Source Map
title: NightsOut Source Code Map
description: Mapping of modules, packages, and key classes to their physical locations and responsibilities
---

# NightsOut Source Code Map

## Module Structure

| Module | Path | Type | Description |
|--------|------|------|-------------|
| **app** | `/app/` | Android Application | Main app with Activities/Fragments |
| **common** | `/common/` | Android Library | Shared models, utils, constants |
| **common-dialog** | `/common-dialog/` | Android Library | Reusable dialogs |
| **db** | `/db/` | Android Library | Database access layer |
| **profile** | `/profile/` | Android Library | Profile management |

---

## App Module (`/app/src/main/java/com/wit/jasonfagerberg/nightsout/`)

### Main Package (`main/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `MainActivity.kt` | `MainActivity` | Main entry with ViewPager, bottom nav |
| `NightsOutActivity.kt` | `NightsOutActivity` | Base activity with theme handling |
| `NightsOutApplication.kt` | `NightsOutApplication` | Application class, preferences init |

### Home Package (`home/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `HomeFragment.kt` | `HomeFragment` | BAC display, drink list, time controls |
| `HomeFragmentDrinkListAdapter.kt` | - | RecyclerView adapter for drinks |
| `HomeFragmentLogDatePicker.kt` | - | Date picker for logging sessions |

### Add Drink Package (`addDrink/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `AddDrinkActivity.kt` | `AddDrinkActivity` | Add/edit drink screen |
| `AddDrinkActivityAlcoholSourceAdapter.kt` | - | Complex drink sources adapter |
| `AddDrinkActivityFavoritesListAdapter.kt` | - | Favorites list adapter |
| `AddDrinkActivityRecentsListAdapter.kt` | - | Recent drinks adapter |
| `ComplexDrinkHelper.kt` | `ComplexDrinkHelper` | Multi-ingredient drink logic |

### Database Helper Package (`databaseHelper/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `AddDrinkDatabaseHelper.kt` | - | Add drink specific DB ops |
| `DatabaseHelper.kt` | `DatabaseHelper` | Main DB helper (legacy) |
| `LogDatabaseHelper.kt` | - | Log-specific DB ops |

### Dialog Package (`dialogs/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `BacInfoDialog.kt` | `BacInfoDialog` | BAC information modal |
| `EditDrinkDialog.kt` | - | Edit drink dialog |
| `LightSimpleDialog.kt` | `LightSimpleDialog` | Lightweight dialog |
| `SimpleDialog.kt` | `SimpleDialog` | Standard dialog |

### Log Package (`log/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `LogFragment.kt` | `LogFragment` | Historical session view |
| `LogFragmentAdapter.kt` | - | Log list adapter |
| `LogFragmentDatePicker.kt` | - | Date picker for logs |

### Manage DB Package (`manageDB/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `ManageDBActivity.kt` | `ManageDBActivity` | Database management screen |
| `ManageDBDrinkListAdapter.kt` | - | Drink list adapter |

### Models Package (`models/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `Drink.kt` | `Drink` | Drink domain model |
| `LogHeader.kt` | `LogHeader` | Session summary model |

### Notification Package (`notification/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `BacNotificationService.kt` | `BacNotificationService` | BAC notification service |
| `NotificationHelper.kt` | `NotificationHelper` | Notification utilities |

### Profile Package (`profile/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `ProfileFragment.kt` | `ProfileFragment` | User profile settings |

### Settings Package (`settings/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `SettingsActivity.kt` | `SettingsActivity` | Settings screen |

### Utils Package (`utils/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `Converter.kt` | `Converter` | Unit/time conversions |

### Constants Package (`constants/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `Constants.kt` | `Constants` | App-level constants |

---

## Common Module (`/common/src/main/java/com/fagerberg/jason/common/`)

### Models Package (`models/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `Drink.kt` | `Drink` | Shared drink model |
| `LogHeader.kt` | `LogHeader` | Shared log header model |
| `VolumeMeasurement.kt` | `VolumeMeasurement` | Volume unit enum |
| `WeightMeasurement.kt` | `WeightMeasurement` | Weight unit enum |

### Utils Package (`utils/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `ConversionUtils.kt` | - | Unit conversion functions |
| `CountryUtils.kt` | - | Country-specific logic |
| `TimeUtils.kt` | - | Time utilities |

### Constants Package (`constants/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `Constants.kt` | - | Shared constants (DB config, prefs) |
| `SharedPreference.kt` | - | Preference key definitions |

### Android Package (`android/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `AbstractPresenter.kt` | `AbstractPresenter` | Base presenter class |
| `NightsOutActivity.kt` | `NightsOutActivity` | Shared base activity |
| `NightsOutApplication.kt` | `NightsOutApplication` | Shared application class |
| `NightsOutSharedPreferences.kt` | `NightsOutSharedPreferences` | Preference wrapper |

---

## Common-Dialog Module (`/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `LightSimpleDialog.kt` | `LightSimpleDialog` | Lightweight dialog |
| `SimpleDialog.kt` | `SimpleDialog` | Standard dialog |

---

## DB Module (`/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `SimpleDatabaseManager.kt` | `SimpleDatabaseManager` | Database abstraction |
| `constants.kt` | Constants | Table name constants |

---

## Profile Module (`/profile/src/main/java/com/fagerberg/jason/profile/`)

### Presenter Package (`presenter/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `ProfileFragmentPresenter.kt` | `ProfileFragmentPresenter` | MVP presenter |

### Repository Package (`repository/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `ProfileFragmentRepository.kt` | `ProfileFragmentRepository` | Data access |

### View Package (`view/`)

| File | Class | Responsibility |
|------|-------|----------------|
| `ProfileFragment.kt` | `ProfileFragment` | UI fragment |
| `ProfileFragmentFavoritesAdapter.kt` | - | Favorites adapter |
| `ProfileFragmentViewManager.kt` | `ProfileFragmentViewManager` | View rendering |

---

## Database Tables

### Table Definitions (`/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/constants.kt`)

```kotlin
const val DRINKS_TABLE = "drinks"
const val CURRENT_SESSION_TABLE = "current_session_drinks"
const val FAVORITES_TABLE = "favorites"
const val LOG_TABLE = "log"
const val LOGGED_DRINKS_TABLE = "log_drink"
```

### Table Schemas

| Table | Schema |
|-------|--------|
| `drinks` | `id TEXT, name TEXT, abv NUMERIC, amount NUMERIC, measurement TEXT, recent INTEGER, modifiedTime INTEGER, dontSuggest INTEGER` |
| `current_session_drinks` | `drink_id TEXT, position INTEGER` |
| `favorites` | `drink_name TEXT, origin_id TEXT` |
| `log` | `date INTEGER UNIQUE, bac NUMERIC, duration INTEGER` |
| `log_drink` | `log_date NUMERIC, drink_id TEXT` |

---

## Entry Points

| Component | Location | Description |
|-----------|----------|-------------|
| **Main Activity** | `app/src/main/java/.../main/MainActivity.kt` | App entry point |
| **Application** | `app/src/main/java/.../main/NightsOutApplication.kt` | App lifecycle |
| **Shared Application** | `common/src/main/java/.../android/NightsOutApplication.kt` | Shared app class |

---

## Build Configuration

| File | Purpose |
|------|---------|
| `/app/build.gradle` | App module config |
| `/common/build.gradle` | Common library config |
| `/common-dialog/build.gradle` | Dialog library config |
| `/db/build.gradle` | DB library config |
| `/profile/build.gradle` | Profile module config |
| `/build.gradle` | Root project config |
| `/settings.gradle` | Module inclusion |

---

## Testing

| Test Type | Location | Description |
|-----------|----------|-------------|
| **Unit Tests** | `common/src/test/java/.../` | Model and utils tests |
| **Android Tests** | `db/src/androidTest/java/.../` | Database integration tests |

---

## Resources

| Resource Type | Directory |
|---------------|-----------|
| Layouts | `/app/src/main/res/layout/` |
| Values | `/app/src/main/res/values/` |
| Menus | `/app/src/main/res/menu/` |
| Drawables | `/app/src/main/res/drawable/` |
| Assets (DB) | `/app/src/main/assets/nights_out_db.db` |

---

## Key Dependencies

| Dependency | Usage |
|------------|-------|
| `androidx.appcompat:appcompat` | Compatibility library |
| `androidx.recyclerview:recyclerview` | List displays |
| `com.google.android.material:material` | Material design |
| `com.jjoe64:graphview` | BAC decline charts |
| `androidx.preference:preference` | Settings UI |
| `io.reactivex.rxjava2:rxjava` | Reactive streams |
| `io.reactivex.rxjava2:rxandroid` | Android RX bindings |
| `com.github.prolificinteractive:material-calendarview` | Date pickers |

---

**Previous**: See [`/openwiki/workflows/main.md`](./workflows/main.md) for user flow details.
