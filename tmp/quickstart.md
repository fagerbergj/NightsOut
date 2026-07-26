cc
# NightsOut Quickstart

**NightsOut** is an ad-free Android BAC calculator that helps users track their alcohol consumption and monitor their Blood Alcohol Concentration in real-time using the Widmark Formula.

## Overview

- **Platform**: Android (minSdk 19, targetSdk 29)
- **Language**: Kotlin
- **Architecture**: Multi-module Gradle project with MVVM-inspired patterns
- **Database**: SQLite with pre-populated database file
- **Version**: Rattata (v1904)

## Features

- **Real-time BAC Calculation**: Uses the Widmark Formula to calculate blood alcohol concentration based on drinks consumed, weight, sex, and time elapsed
- **Drink Logging**: Track individual drinks with name, ABV, amount, and measurements (oz, ml, beers, shots, wine glasses, pints)
- **Favorites & Recent Drinks**: Save favorite drinks and see recently used drinks for quick addition
- **Time Tracking**: Set start/end times to account for alcohol metabolism (0.015%/hour decay)
- **Graph Visualization**: Charts showing BAC decline over time
- **Log History**: Track drinking sessions with dates and results
- **Background Notifications**: BAC level notifications via foreground service

## Project Structure

```
├── app/                  # Main Android application
├── common/               # Shared models, utilities, constants
├── common-dialog/        # Reusable dialog components
├── db/                   # Database access layer
└── profile/              # User profile management module
```

## Key Modules

| Module | Purpose |
|--------|---------|
| `app` | Main application with Activities/Fragments |
| `common` | Shared domain models (Drink, LogHeader), utilities (Converter), constants |
| `common-dialog` | Reusable dialog implementations |
| `db` | Database manager and table definitions |
| `profile` | Profile settings and favorites management |

## Entry Points

- **MainActivity**: Main app entry point with BottomNavigationView and ViewPager
- **NightsOutApplication**: Application class with shared preferences initialization
- **HomeFragment**: Primary screen showing BAC value and drink list

## Domain Concepts

- **Drink Model**: UUID-based drink with name, ABV%, amount, measurement type, favorites/recent flags
- **BAC Calculation**: `BAC = (alcohol_grams × 5.14) / (weight_lbs × r)` where r=0.73 for men, 0.66 for women
- **Alcohol Decay**: 0.015% per hour代谢 rate

## Architecture Overview

See [`/openwiki/architecture/overview.md`](./architecture/overview.md) for detailed architecture including:
- Module dependencies
- Layer separation (Models, Database, UI, Application)
- Data flow patterns

## Workflows

See [`/openwiki/workflows/main.md`](./workflows/main.md) for key user workflows:
- Adding drinks
- BAC calculation process
- Logging sessions
- Managing favorites

## Domain Concepts

See [`/openwiki/domain/concepts.md`](./domain/concepts.md) for domain models:
- Drink model details
- Volume/Weight measurements
- Database schema

## Testing

- Unit tests in `common/src/test/` for models and utils
- Android tests in `db/src/androidTest/` for database operations
- Run tests: `./gradlew test`

## Backlog

- **Full OKF Migration**: Some pages may still need OKF-compliant front matter after automated generation
- **Profile Module Details**: Additional documentation on profile presenter/view manager patterns

---

For questions about specific areas, use the navigation above or see [`/openwiki/source-map.md`](./source-map.md) for source code mapping.
