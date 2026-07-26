---
type: Reference
title: NightsOut — Quickstart
description: Entry point to the NightsOut Android BAC calculator wiki — quick overview, key facts, build instructions, and links to all documentation pages.
resource: https://github.com/jasonfagerberg/NightsOut
tags: [guide, setup]
timestamp: 2025-07-26T04:00:00Z
---

# NightsOut — Quickstart

**NightsOut** is an Android application for calculating Blood Alcohol Concentration (BAC) using the [Widmark formula](http://www.teamdui.com/bac-widmarks-formula/). It allows users to log drinks, track sessions over time, and view BAC decline curves — all without ads or tracking.

## What This Wiki Covers

- **[Architecture Overview](./architecture/overview.md)** — Single-module project structure, build configuration (Kotlin DSL + version catalog), and architectural patterns
- **[Data Model](./domain/data-model.md)** — The `Drink`, `LogHeader`, `VolumeMeasurement`, and `WeightMeasurement` data types, plus the 5-table SQLite schema
- **[BAC Calculation](./domain/bac-calculation.md)** — Widmark formula via `BacCalculator.Drink`/`calculate()`, unit conversion chains, and complex-mix ABV weighting
- **[User Workflows](./workflows/user-flows.md)** — End-to-end flows: adding drinks, logging sessions, managing profile/favorites, and tracking BAC over time
- **[App Module](./modules/app.md)** — The only Gradle module (`:app`): Activities, Fragments, RecyclerView adapters, navigation, background services
- **[CI/CD](./operations/ci-cd.md)** — GitHub Actions build workflow, version catalog configuration, and Gradle constraints
- **[Testing](./testing/overview.md)** — Unit tests for domain models, utility functions, and the `BacCalculator`

## Key Facts

| Item | Detail |
|------|--------|
| **Package name** | `com.wit.jasonfagerberg.nightsout` |
| **Min SDK** | API 24 (Android 7.0 Nougat) |
| **Target / Compile SDK** | API 36 (Android 16) |
| **Language** | Kotlin |
| **Build system** | Gradle Kotlin DSL (`build.gradle.kts`) + version catalog (`gradle/libs.versions.toml`), R8 shrinking for release |
| **Database** | SQLite (pre-populated `nights_out_db.db`, version 40) |
| **BAC formula** | Widmark with elimination rate of 0.015 /hr, computed by `domain.BacCalculator` |

## How to Build

```bash
./gradlew assembleDebug       # Debug APK
./gradlew testDebugUnitTest   # Run JVM unit tests
./gradlew assembleRelease     # Release APK (R8 shrinking enabled)
```

The CI workflow targets JDK 17 (Temurin), required by AGP 9.3. See **[CI/CD](./operations/ci-cd.md)** for details.

## Architecture at a Glance

```
:app (single Gradle module)
├── main/java/com/wit/jasonfagerberg/nightsout/
│   ├── domain/      ← BacCalculator (pure Kotlin, no Android deps)
│   ├── models/      ← Drink, LogHeader, VolumeMeasurement, WeightMeasurement
│   ├── utils/       ← Converter, CountryUtils
│   ├── home/        ← HomeFragment, drink list adapter
│   ├── addDrink/    ← AddDrinkActivity, ComplexDrinkHelper, suggestions
│   ├── log/         ← Session logging UI
│   ├── profile/     ← ProfileFragment + favorites adapter (flat data binding)
│   ├── notification ← BacNotificationService (foreground service)
│   └── databaseHelper/ ← DatabaseHelper, AddDrinkDatabaseHelper, LogDatabaseHelper
└── libs/            ← Local AARs: graphview, material-calendarview
```

The project is now a **single `:app` module** — the former `:common`, `:db`, `:profile`, and `:common-dialog` submodules were harvested into `:app` in v0.78 (#78). Most screens use direct data binding (no ViewModels, LiveData, or Room). The BAC calculation logic was extracted into a pure-Kotlin object (`domain.BacCalculator`) with dedicated JVM unit tests (#82).

## Source File Index

| Package | Source Root |
|---------|-------------|
| All source | `/app/src/main/java/com/wit/jasonfagerberg/nightsout/` |
| Unit tests | `/app/src/test/java/com/wit/jasonfagerberg/nightsout/` |
| Version catalog | `gradle/libs.versions.toml` |

## Backlog

- [ ] **Database schema migration history** — Document the 5 version upgrades from v40 onward, including UUID migration. Requires reading `DatabaseHelper.kt` upgrade logic in full.
- [ ] **Ad integration** — The codebase references rating dialogs (`DRINK_COUNT_TO_ASK_FOR_RATING`, `DAYS_UNTIL_ASK_FOR_RATING`) but the google-services.json was removed; no current ad platform is wired up.
