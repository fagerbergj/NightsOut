---
type: Reference
title: NightsOut — BAC Calculation
description: Complete reference for the Widmark formula implementation in NightsOut, including unit conversion chains, complex-mix ABV weighting, and the background notification service that recalculates BAC in real time.
---

# BAC Calculation

NightsOut calculates Blood Alcohol Concentration using the **[Widmark formula](http://www.teamdui.com/bac-widmarks-formula/)**, updated with a standard elimination rate. The calculation runs both on-screen (HomeFragment) and in the background via `BacNotificationService`.

## The Widmark Formula

```
instantBAC = (totalFlOzOfAlcohol × 5.14) / (weightInLbs × r)
finalBAC = max(0, instantBAC - (hoursElapsed × eliminationRate))
```

### Parameters

| Parameter | Value / Derivation | Source |
|-----------|-------------------|--------|
| `totalFlOzOfAlcohol` | Σ(each drink's `amount × volumeConversionFactor × abv/100`) across all drinks in the session | `HomeFragment.calculateBAC()` |
| `weightInLbs` | User's weight from profile, converted from kg if necessary | `NightsOutSharedPreferences.weight.toLbs()` |
| `r` (Widmark factor) | **0.73** for male, **0.66** for female | Derived from `NightsOutSharedPreferences.sex` |
| `eliminationRate` | **0.015 BAC per hour** (standard metabolic rate) | Hard-coded constant |

### Step-by-Step Calculation

For a session with multiple drinks:

1. **Convert each drink to fluid ounces of pure alcohol:**
   ```
   alcoholFluidOz = amount × volumeConversionFactor × (abv / 100)
   ```

2. **Sum all drinks' alcohol content:**
   ```
   totalFlOzOfAlcohol = Σ(alcoholFluidOz) for each drink
   ```

3. **Calculate instant BAC (ignoring time):**
   ```
   instantBAC = (totalFlOzOfAlcohol × 5.14) / (weightInLbs × r)
   ```

4. **Adjust for elapsed time:**
   ```
   hoursElapsed = (endTimeMin - startTimeMin) / 60.0
   finalBAC = max(0, instantBAC - (hoursElapsed × 0.015))
   ```

### Volume Conversion Factors

These convert each drink's amount to fluid ounces before ABV calculation:

| `VolumeMeasurement` | Factor to Fluid Oz | Rationale |
|---------------------|-------------------|-----------|
| `OZ` | 1.0 | Already in oz |
| `ML` | 0.033814 | Standard ml-to-oz conversion |
| `BEERS` | 12.0 | 12 oz standard beer |
| `SHOTS` | 1.5 | 1.5 oz standard shot |
| `WINE_GLASSES` | 5.0 | 5 oz wine glass |
| `PINTS` | 16.0 | 16 oz US pint |

Source: [`ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt)

## Complex Drink Mode (Weighted Average ABV)

When adding drinks in complex mode, users can combine multiple alcohol sources with different ABVs into a single logical drink. The app computes a weighted-average ABV:

**Source:** [`ComplexDrinkHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/ComplexDrinkHelper.kt)

```kotlin
// Weight each source by its volume contribution to the total mix
totalVolume = Σ(source.amount × volumeConversionFactor(source.measurement))
weightedAverageAbv = Σ((source.abv × source.volume) / totalVolume)
```

Where `source.volume` is the amount converted to fluid ounces. The final drink entry stored in the database uses this weighted-average ABV along with the total combined volume.

## HomeFragment BAC Display

**Source:** [`HomeFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt)

The `calculateBAC()` method on `HomeFragment` performs the full Widmark calculation whenever:
- A drink is added or removed (swipe-to-remove triggers re-render)
- The start time or end time changes
- The profile weight or sex changes (triggering recalculation from settings)

Key instance variables in `HomeFragment`:

| Variable | Type | Description |
|----------|------|-------------|
| `drinkingDuration` | `Double` | Duration in hours (derived from start/end times) |
| `standardDrinksConsumed` | `Double` | Count of standard drinks for reference |
| `bac` | `Double` | Current calculated BAC value |

The BAC is displayed as text and updated on every input change (`onResume()` sets up the initial state). A time graph rendered via GraphView shows the projected BAC decline curve over time.

## BacNotificationService

**Source:** [`BacNotificationService.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/BacNotificationService.kt)

A foreground service that displays the current BAC in a persistent notification. It runs independently of the app's UI and communicates via:

1. **Broadcast intents** — The service listens for `ACTION.START_SERVICE`, `ACTION.UPDATE_NOTIFICATION`, `ACTION.STOP_SERVICE`, `ACTION.REFRESH_BAC`, and `ACTION.ADD_DRINK` actions.
2. **Application-level reference** — When `ACTION.REFRESH_BAC` is received, the service pushes the updated BAC back into the `HomeFragment` via `NightsOutApplication.mCurrentActivity`.

The notification includes two action buttons:
- **"Add Drink"** — Opens `AddDrinkActivity`
- **"Update"** — Triggers a fresh BAC calculation using current time as end time

## Source Paths Summary

| Component | File Path |
|-----------|-----------|
| Widmark calculation (notification) | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/BacNotificationService.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/notification/BacNotificationService.kt) |
| BAC display + recalculation (UI) | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt) |
| Complex-mix ABV weighting | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/ComplexDrinkHelper.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/addDrink/ComplexDrinkHelper.kt) |
| Volume/weight conversion constants | [`/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt) |
| Unit converter utilities | [`/app/src/main/java/com/wit/jasonfagerberg/nightsout/utils/Converter.kt`](/app/src/main/java/com/wit/jasonfagerberg/nightsout/utils/Converter.kt) |

## Notable Details & Caveats

- **No alcohol distribution ratio variation:** The app uses fixed r values (0.73 male, 0.66 female). It does not account for body composition, age, or hydration level.
- **Elimination rate is constant at 0.015/hr:** Real metabolic rates vary significantly between individuals and can be affected by food consumption, medications, and other factors. The app uses the standard NIAAA average.
- **No breathalyzer / blood test calibration:** BAC values are purely calculated; no device integration exists.
- **GraphView chart:** The `com.jjoe64:graphview:4.2.2` library renders the BAC decline curve on the HomeFragment, showing projected BAC over time from current session start to zero.
