# AGENTS.md

Guidance for AI coding agents working in this repository.

NightsOut is an Android app: Kotlin, Jetpack Compose, one Activity driving all screens through a Navigation Compose `NavHost`.

## Conventions

**Dependency injection is Koin 4.2.2, `koin-android` only.** Resolve ViewModels at the Activity with `by viewModel()` from `org.koin.androidx.viewmodel.ext.android` and pass them into the `composable()` blocks. `koin-androidx-compose` is deliberately not a dependency — do not add it or reach for `koinViewModel()`. Use `by inject()` only for things that genuinely live for the application's lifetime, never for ViewModels.

**Collect flows with `collectAsStateWithLifecycle()`.** Reading `.value` off a flow inside a composable skips lifecycle awareness and has caused recomposition bugs here.

**Colours belong in `ui/theme/Theme.kt`.** Wrap screen content in `NightsOutTheme`. `addDrink/ui/AddDrinkScreen.kt` still inlines `Color(0x…)` literals; that is a leftover, not a pattern to copy.

**Dependencies go through the version catalog** at `gradle/libs.versions.toml`. No dynamic versions.

## CI runs two Gradle tasks

`.github/workflows/ci.yml` runs `assembleDebug` and `testDebugUnitTest`. That is all.

There is no emulator step and no `connectedAndroidTest`, so **anything under `app/src/androidTest/` compiles and is then never executed** — by CI or by anything else. A test that must actually run belongs in `app/src/test/`, where Robolectric can drive Compose without a device. Never report instrumentation tests as passing unless you ran them; compiling them proves only that they compile.
