---
type: Reference
title: Documentation Plan
description: Temporary plan for NightsOut OpenWiki initialization
---

# Documentation Plan

## Target Pages

### `/openwiki/quickstart.md`
- High-level overview: BAC calculator app (Android)
- Key features: BAC calculation, drink logging, charts
- Tech stack: Kotlin, Android SDK 29, Gradle multi-module
- Navigation to major sections

### `/openwiki/architecture/overview.md`
- Module structure: app, common, common-dialog, db, profile
- Layered architecture: Models, Database, UI, Application
- BAC calculation flow (Widmark formula)

### `/openwiki/domain/concepts.md`
- `Drink` model with UUID, ABV, amount, measurements
- `LogHeader` for logged drinking sessions
- Volume/Weight measurement units
- BAC calculation formula details

### `/openwiki/workflows/main.md`
- Adding drinks (AddDrinkActivity)
- Home screen BAC calculation
- Logging sessions (LogFragment)
- Profile management

### `/openwiki/source-map.md`
- Module-to-source mappings
- Key entry points: MainActivity, NightsOutApplication
- Database helpers

## Source Evidence

- `README.md`: App description
- `app/build.gradle`, `common/build.gradle`: Module configuration
- `/app/src/main/java/com/wit/jasonfagerberg/nightsout/main/MainActivity.kt`: Entry point
- `/common/src/main/java/com/fagerberg/jason/common/models/Drink.kt`: Domain model
- `/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt`: BAC calculation logic

## Open Questions

- What is the profile module structure?
- What database schema is used?
- Notification service details?
