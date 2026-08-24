# AGENTS.md

- Use `koin-android`, not `koin-androidx-compose`. Resolve ViewModels in Activities with `by viewModel()` and pass them into composables; reserve `by inject()` for application-lifetime dependencies.
- Do not copy the inline colors in `app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/ui/AddDrinkScreen.kt`; add shared colors to `app/src/main/java/com/wit/jasonfagerberg/nightsout/ui/theme/Theme.kt` and wrap screen content in `NightsOutTheme`.
- Validate with `./gradlew assembleDebug` and `./gradlew testDebugUnitTest`. Instrumented Compose tests run with `./gradlew connectedDebugAndroidTest` and require an emulator or device.
