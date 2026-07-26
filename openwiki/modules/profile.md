---
type: Module Reference
title: NightsOut — Profile Module (MVI)
description: Detailed reference for the :profile module, which implements a full RxJava-based MVI architecture for user profile management including sex selection, weight entry, favorites management, and preferences navigation.
---

# Profile Module (`:profile`)

The `:profile` module is an Android library that implements **MVI (Model-View-Intent)** architecture using the shared `AbstractPresenter` base class from the `:common` module. It provides the user profile screen with sex selection, weight entry, favorites management, and settings navigation.

## Package Structure

```
/profile/src/main/java/com/fagerberg/jason/profile/
├── presenter/
│   └── ProfileFragmentPresenter.kt      ← MVI presenter (AbstractPresenter subclass)
├── repository/
│   └── ProfileFragmentRepository.kt     ← Data access abstraction (SharedPreferences + SQLite)
└── view/
    ├── ProfileFragment.kt               ← View layer: renders state, sends intents
    ├── ProfileFragmentViewManager.kt    ← View state rendering logic
    └── ProfileFragmentFavoritesAdapter.kt  ← Horizontal favorites RecyclerView adapter
```

## Dependencies

| Dependency | Purpose |
|------------|---------|
| `:common` | Domain models, MVI presenter base class, SharedPreferences wrapper, shared dialogs |
| `:db` | SQLite data access (favorites CRUD) |
| `:common-dialog` | Reusable dialog components (`SimpleDialog`, `LightSimpleDialog`) |
| `io.reactivex.rxjava2:rxjava:2.2.6` | Reactive streams for async operations |
| `io.reactivex.rxjava2:rxandroid:2.1.1` | Android-specific schedulers (main thread) |
| `com.jakewharton.rxrelay2:rxrelay:2.1.1` | Replay subjects for state management |

## MVI Architecture

The module implements a complete MVI pipeline through the shared `AbstractPresenter<Intent, Action, Result, ViewModel>` base class from `common`. This is the only known production implementation of this pattern in the NightsOut codebase.

### Component Overview

```
ProfileFragment (View)
    ├── sends → ProfileIntent (user actions)
    └── receives ← Observable<ProfileViewModel> (state updates)
          ↓
ProfileFragmentPresenter (AbstractPresenter subclass)
    ├── intentToAction()  — map Intents to Actions
    ├── actionToResult()  — execute via ProfileFragmentRepository (RxJava Observables)
    └── stateReducer()    — compute new ViewModel from previous state + result
          ↓
ProfileFragmentViewManager (View rendering logic)
    └── renders ProfileViewModel → UI changes
```

### Intent → Action Mapping

**Source Path:** [`presenter/ProfileFragmentPresenter.kt`](/profile/src/main/java/com/fagerberg/jason/profile/presenter/ProfileFragmentPresenter.kt)

| Intent | Maps To | Side Effect |
|--------|---------|-------------|
| `ProfileIntent.Init(activity)` | `ProfileAction.Init(activity)` | Load SharedPreferences from repository |
| `ProfileIntent.InitFavorites(activity)` | `ProfileAction.InitFavorites(activity)` | Query favorites from database |
| `ProfileIntent.SelectSex(sex: Boolean)` | `ProfileAction.SelectSex(sex)` | In-memory state change only |
| `ProfileIntent.Save(activity, sex, weight, measurement)` | `ProfileAction.Save(sex, weight, measurement)` | Save to SharedPreferences via repository |
| `ProfileIntent.Settings` | `ProfileAction.Settings` | Navigate to SettingsActivity |
| `ProfileIntent.ClearFavorites` | `ProfileAction.ClearFavorites` | Delete all favorites from database |
| `ProfileIntent.RemoveFavorite(drink)` | `ProfileAction.RemoveFavorite(drink)` | Delete single favorite from database |

### State Machine (ViewModel)

The ProfileViewModel is a sealed hierarchy representing discrete UI states:

| ViewModel State | Triggered By Result | UI Behavior |
|----------------|--------------------|-------------|
| `ProfileViewModel.Empty` | Initial state | Blank screen, waiting for Init |
| `ProfileViewModel.Init(sharedPreferences)` | `ProfileResult.Init` | Populated profile with user data loaded from SharedPreferences |
| `ProfileViewModel.InitFavorites(favorites)` | `ProfileResult.InitFavorites` | Horizontal favorites list rendered in RecyclerView |
| `ProfileViewModel.SelectSex(sex)` | `ProfileAction.SelectSex` | Toggle male/female button to selected state |
| `ProfileViewModel.Save(sharedPreferences)` | `ProfileResult.Save` | Confirmation that profile was saved |
| `ProfileViewModel.InvalidSave(isInvalidSex, isInvalidWeight)` | `ProfileResult.InvalidSave` | Show validation errors (sex required, weight >= 20) |
| `ProfileViewModel.Settings` | `ProfileAction.Settings` | Navigate to SettingsActivity |
| `ProfileViewModel.ClearFavorites` | `ProfileResult.ClearFavorites` | Refresh favorites list after clearing all |
| `ProfileViewModel.RemoveFavorite(drink)` | `ProfileResult.RemoveFavorite` | Remove single drink from favorites RecyclerView |

## ProfileFragmentRepository

**Source Path:** [`repository/ProfileFragmentRepository.kt`](/profile/src/main/java/com/fagerberg/jason/profile/repository/ProfileFragmentRepository.kt)

The repository layer bridges the MVI presenter with data sources (SharedPreferences and SQLite). Every method returns an RxJava `Observable` to support async operations:

| Method | Returns | Purpose |
|--------|---------|---------|
| `getSharedPrefs()` | `Observable<NightsOutSharedPreferences>` | Load profile settings from SharedPreferences |
| `saveSharedPrefs(sex, weight, measurement)` | `Observable<NightsOutSharedPreferences>` | Save updated profile to SharedPreferences |
| `getFavorites()` | `Observable<List<Drink>>` | Query all favorites from SQLite database |
| `clearFavorites()` | `Observable<Unit>` | Delete all favorite entries |
| `removeFavoriteDrink(drink)` | `Observable<Unit>` | Delete a specific favorite entry |

The repository takes a `NightsOutActivity` reference in its constructor, which provides access to both the Application context (for SharedPreferences) and Activity (for database operations).

## ProfileFragmentViewManager

**Source Path:** [`view/ProfileFragmentViewManager.kt`](/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragmentViewManager.kt)

Handles all UI state transitions based on incoming `ProfileViewModel` values:

| ViewModel Input | View Update |
|----------------|-------------|
| `Init(sharedPrefs)` | Set sex toggle, weight EditText, measurement spinner from shared prefs |
| `InitFavorites(favoritesList)` | Submit new data to `ProfileFragmentFavoritesAdapter` |
| `SelectSex(sex)` | Update male/female toggle button states |
| `InvalidSave(isInvalidSex, isInvalidWeight)` | Show validation error via `SimpleDialog` (from `:common-dialog`) |
| `ClearFavorites` / `RemoveFavorite(drink)` | Refresh adapter with updated favorites list |

## ProfileFragment (View Layer)

**Source Path:** [`view/ProfileFragment.kt`](/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragment.kt)

The View component implements the MVI view contract:

1. **Creates `ProfileFragmentPresenter`** in `onViewCreated()`
2. **Subscribes to `presenter.viewModelStream()`** — Receives `ProfileViewModel` updates on the main thread and delegates rendering to `ProfileFragmentViewManager`
3. **Sends Intents** — Converts user interactions (button clicks, text changes) into `ProfileIntent` objects via `presenter.sendAction(intent)`

### User Interactions Mapped to Intents

| User Action | Intent Sent |
|-------------|------------|
| Tap "Male" / "Female" toggle | `ProfileIntent.SelectSex(isMale)` |
| Tap "Save" button | `ProfileIntent.Save(activity, sex, weight, measurement)` |
| Tap "Settings" icon | `ProfileIntent.Settings` |
| Tap "Clear Favorites" | `ProfileIntent.ClearFavorites` |
| Tap trash on a favorite drink | `ProfileIntent.RemoveFavorite(drink)` |

## ProfileFragmentFavoritesAdapter

**Source Path:** [`view/ProfileFragmentFavoritesAdapter.kt`](/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragmentFavoritesAdapter.kt)

A RecyclerView adapter for displaying favorited drinks in a horizontal list. Key features:
- Uses `ProfileFragmentViewManager` as the presenter reference to trigger updates
- Supports drag-to-reorder via `ItemTouchHelper.SimpleCallback`
- Each item displays drink name with a delete (trash) button that fires `ProfileIntent.RemoveFavorite(drink)`

## Source Paths Summary

| Area | Path |
|------|------|
| Presenter | [`/profile/src/main/java/com/fagerberg/jason/profile/presenter/ProfileFragmentPresenter.kt`](/profile/src/main/java/com/fagerberg/jason/profile/presenter/ProfileFragmentPresenter.kt) |
| Repository | [`/profile/src/main/java/com/fagerberg/jason/profile/repository/ProfileFragmentRepository.kt`](/profile/src/main/java/com/fagerberg/jason/profile/repository/ProfileFragmentRepository.kt) |
| Fragment (View) | [`/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragment.kt`](/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragment.kt) |
| View Manager | [`/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragmentViewManager.kt`](/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragmentViewManager.kt) |
| Favorites Adapter | [`/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragmentFavoritesAdapter.kt`](/profile/src/main/java/com/fagerberg/jason/profile/view/ProfileFragmentFavoritesAdapter.kt) |

## Design Notes & Caveats

- **App module has its own ProfileFragment:** The `:app` module also defines a `ProfileFragment` (at `/app/src/main/java/com/wit/jasonfagerberg/nightsout/profile/ProfileFragment.kt`) that does not use MVI architecture. This creates a code split where two profile implementations exist simultaneously — the MVI one in `:profile` and the direct-binding one in `:app`. The app module currently uses its own version at runtime, making the `:profile` library module effectively unused in the shipping build unless wired up via dependency injection or explicit import.

- **No unit tests for presenter:** Unlike the `:common` module which has comprehensive unit tests, the `:profile` module has no test files. The MVI architecture is well-suited for testing — the presenter can be tested independently of Android UI by mocking the repository and asserting state transitions.

- **AbstractPresenter lifecycle management:** Since `AbstractPresenter` extends `AndroidViewModel`, it's scoped to the fragment's ViewModel store. When the fragment is destroyed, all Rx subscriptions are disposed via `onCleared()`. This prevents memory leaks but means presenter state is lost on configuration changes (which is correct for MVI — state should be re-initialized from data sources).

- **Direct coupling to SQLite:** The repository directly instantiates database helpers rather than abstracting behind a `DataSource` interface. This works well given the small scope but would need refactoring if switching to Room or any other persistence layer.
