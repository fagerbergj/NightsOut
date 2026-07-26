---
type: Quickstart
title: NightsOut — BAC Calculator App
description: Entry point for the NightsOut Android application wiki. NightsOut is an ad-free Blood Alcohol Concentration calculator using the Widmark formula, with drink logging, session tracking, and a background BAC notification service. Covers 5 Gradle modules: app (UI), common (shared models & utilities), db (SQLite data layer), profile (MVI-powered user profile), and common-dialog (reusable UI dialogs).
---

# NightsOut — Quickstart

**NightsOut** is an Android application for calculating Blood Alcohol Concentration (BAC) using the [Widmark formula](http://www.teamdui.com/bac-widmarks-formula/). It allows users to log drinks, track sessions over time, and view BAC decline curves — all without ads or tracking.

## What This Wiki Covers

- **[Architecture Overview](./architecture/overview.md)** — Module structure, dependency graph, build configuration, and architectural patterns used across the project
- **[Data Model](./domain/data-model.md)** — The `Drink`, `LogHeader`, `VolumeMeasurement`, and `WeightMeasurement` data types, plus the 5-table SQLite schema
- **[BAC Calculation](./domain/bac-calculation.md)** — Widmark formula implementation, unit conversion chains, and complex-mix ABV weighting
- **[User Workflows](./workflows/user-flows.md)** — End-to-end flows: adding drinks, logging sessions, managing profile/favorites, and tracking BAC over time
- **[App Module](./modules/app.md)** — The main Android app: activities, fragments, RecyclerView adapters, navigation, background services
- **[Common Module](./modules/common.md)** — Shared domain models, utilities, MVI presenter base class, shared preferences wrapper
- **[DB Module](./modules/db.md)** — Pre-populated SQLite database with 500+ drink entries and CRUD operations
- **[Profile Module](./modules/profile.md)** — MVI architecture for user profile management (sex, weight, favorites)
- **[Common Dialogs](./modules/common-dialog.md)** — Reusable `SimpleDialog` and `LightSimpleDialog` components
- **[CI/CD](./operations/ci-cd.md)** — GitHub Actions build workflow and Gradle version constraints
- **[Testing](./testing/overview.md)** — Unit tests (common module) and instrumented tests (db module)

## Key Facts

| Item | Detail |
|------|--------|
| **Package name** | `com.wit.jasonfagerberg.nightsout` |
| **Min SDK** | API 19 (Android 4.4 KitKat) |
| **Target / Compile SDK** | API 29 (Android 10) |
| **Language** | Kotlin 1.3.72 |
| **Build system** | Gradle (5 modular projects) |
| **Database** | SQLite (pre-populated, version 40) |
| **BAC formula** | Widmark with elimination rate of 0.015 /hr |

## How to Build

```bash
./gradlew assembleDebug   # Debug APK
./gradlew testDebugUnitTest  # Run unit tests
```

The CI workflow targets Java 8 (Temurin) due to AGP 3.6.2 / Gradle wrapper version constraints. See **[CI/CD](./operations/ci-cd.md)** for details.

## Architecture at a Glance

```
app (UI: Activities, Fragments, Adapters, Services)
├── depends on → common   (models, utilities, base classes, MVI presenter)
├── depends on → db       (SQLite data layer, 500+ drink database)
└── depends on → profile  (MVI user profile screen)
    └── depends on → common-dialog  (reusable dialog components)
```

The app module uses a flat, direct-data-binding pattern for most screens — no ViewModels, LiveData, or Room. The **profile** module is the exception: it implements a full RxJava-based MVI architecture using the `AbstractPresenter` base class from `common`.

## Source File Index

| Module | Source Root |
|--------|-------------|
| app | `/app/src/main/java/com/wit/jasonfagerberg/nightsout/` |
| common | `/common/src/main/java/com/fagerberg/jason/common/` |
| db | `/db/src/main/java/com/wit/jasonfagerberg/nightsout/db/` |
| profile | `/profile/src/main/java/com/fagerberg/jason/profile/` |
| common-dialog | `/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/` |

## Backlog

- [ ] **Source map** — Detailed per-module file inventory with roles. Defer until needed.
- [ ] **Database schema migration history** — Document the 5 version upgrades from v40 onward, including UUID migration. Requires reading `SimpleDatabaseManager.kt` upgrade logic in full.
- [ ] **Ad integration** — The codebase references rating dialogs (`DRINK_COUNT_TO_ASK_FOR_RATING`, `DAYS_UNTIL_ASK_FOR_RATING`) but the google-services.json was removed; no current ad platform is wired up.
