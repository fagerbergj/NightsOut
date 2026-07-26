---
type: Domain Model
title: NightsOut Domain Concepts
description: Core domain models, measurements, and business rules for the NightsOut BAC calculator
---

# NightsOut Domain Concepts

## Drink Model

The **Drink** is the core domain entity representing an alcoholic beverage consumed by the user.

### Definition

```kotlin
data class Drink(
    val id: UUID,                    // Unique identifier
    val name: String,                // Display name (e.g., "IPA", "Vodka Shot")
    val abv: Double,                 // Alcohol by volume percentage (0-100)
    val amount: Double,              // Quantity consumed
    val measurement: VolumeMeasurement,  // Unit type (oz, ml, beers, etc.)
    val favorited: Boolean,          // Marked as favorite
    val recent: Boolean,             // Recently used
    val modifiedTime: Long = Calendar.getInstance().timeInMillis,
    val dontSuggest: Boolean = false // Hidden from suggestions
)
```

### Storage

Drinks are stored in multiple tables:

| Table | Purpose |
|-------|---------|
| `drinks` | All drinks (favorites, recent, regular) |
| `current_session_drinks` | Drinks added in current session (with position) |
| `favorites` | Favorite drinks mapping (drink_name → origin_id) |

### Usage

- **Home screen**: Displayed in RecyclerView with swipe-to-delete
- **Add Drink flow**: Suggestions pulled from `recent` and `favorited` drinks
- **Log session**: References stored in `log_drink` table

---

## LogHeader Model

Represents a logged drinking session with summary statistics.

### Definition

```kotlin
data class LogHeader(
    val date: Int,      // YYYYMMDD format (e.g., 20240115)
    val bac: Double,    // Peak BAC during session
    val duration: Double // Hours elapsed
) {
    val durationString: String  // Formatted as "H:MM"
    val dateString: String      // Localized date string
}
```

### Date Formatting

- **US Locale**: "Month Dayst/nd/rd/th" (e.g., "January 15th")
- **Other Locales**: "Dayst/nd/rd/th of Month" (e.g., "15th of January")

### Storage

- Table: `log`
- Unique constraint on `date` column
- Retrieved via `LogFragment` for historical view

---

## Volume Measurements

Available volume units for drink entry:

| Unit | Symbol | Fluid Oz Equivalent | Context |
|------|--------|---------------------|---------|
| Ounces | oz | 1.0 | Base unit |
| Milliliters | ml | 0.033814 | Metric system |
| Beers | beers | 12.0 | Standard beer (12 fl oz) |
| Wine Glasses | wine glasses | 5.0 | Standard wine pour |
| Shots | shots | 1.5 | Standard spirit shot |
| Pints | pints | 16.0 | Pint glass |

### Implementation

```kotlin
enum class VolumeMeasurement(val displayName: String) {
    OZ("oz"),
    ML("ml"),
    BEERS("beers"),
    WINE_GLASSES("wine glasses"),
    SHOTS("shots"),
    PINTS("pints");
}
```

---

## Weight Measurements

User weight units:

| Unit | Symbol | Pounds Equivalent |
|------|--------|-------------------|
| Pounds | lbs | 1.0 |
| Kilograms | kg | 2.205 |

### Implementation

```kotlin
enum class WeightMeasurement(val displayName: String) {
    LBS("lbs"),
    KG("kg");
}
```

---

## BAC Calculation Formula

NightsOut uses the [Widmark Formula](http://www.teamdui.com/bac-widmarks-formula/) to calculate Blood Alcohol Concentration.

### Formula

```
BAC = (alcohol_grams × 5.14) / (weight_lbs × r) - (hours × 0.015)
```

### Variables

| Variable | Value | Notes |
|----------|-------|-------|
| `alcohol_grams` | Sum of (fluid_oz × ABV% × 23.333) | Total alcohol consumed in grams |
| `weight_lbs` | User weight | Converted from user's unit preference |
| `r` | 0.73 (male), 0.66 (female) | Body water constant |
| `hours` | End time - Start time | Elapsed time since drinking began |
| `0.015` | Metabolism rate | Average酒精 elimination per hour |

### Step-by-step Calculation

1. **Convert each drink to fluid ounces of alcohol**:
   ```kotlin
   alcohol_oz = amount × measurement_conversion × (abv / 100)
   ```

2. **Sum all drinks**:
   ```kotlin
   total_alcohol_oz = Σ(alcohol_oz)
   ```

3. **Convert to grams**:
   ```kotlin
   alcohol_grams = total_alcohol_oz × 23.333
   ```

4. **Apply Widmark formula**:
   ```kotlin
   instant_BAC = (alcohol_grams × 5.14) / (weight_lbs × r)
   ```

5. **Subtract metabolism**:
   ```kotlin
   final_BAC = max(instant_BAC - (hours × 0.015), 0.0)
   ```

### Code Reference

See [`HomeFragment.kt:258`](https://github.com/fagerbergj/NightsOut/blob/master/app/src/main/java/com/wit/jasonfagerberg/nightsout/home/HomeFragment.kt#L258)

---

## BAC Result Interpretation

Based on [NIAAA guidelines](https://www.niaaa.nih.gov/alcohols-effects-body):

| BAC Range | Effect |
|-----------|--------|
| 0.000-0.029 | No effects |
| 0.030-0.059 | Reduced alertness, altered mood |
| 0.060-0.099 | Impaired balance, speech, vision |
| 0.100-0.199 | Overwhelmed processing, nausea |
| 0.200-0.299 | Confusion, vomiting |
| 0.300-0.399 | Stupor, risk of death |
| 0.400+ | Coma, high risk of death |

---

## Favorites System

Favorites allow users to save frequently used drinks:

### Storage

```sql
-- favorites table
CREATE TABLE favorites (
    drink_name TEXT,
    origin_id TEXT
)

-- Query for favorites:
SELECT d.* FROM drinks d, favorites f 
WHERE d.id = f.origin_id 
ORDER BY d.modifiedTime ASC
```

### Behavior

- Favorited drinks appear first in suggestion list
- Can be removed via "Clear Favorites" action
- Favorites persist across sessions

---

## Recent Drinks System

Tracks recently used drinks for quick addition:

### Storage

- `recent` column in `drinks` table (0/1)
- Limited to recent items in memory
- Automatically marked on drink creation

---

## Database Schema

### Tables

| Table | Purpose | Key Columns |
|-------|---------|-------------|
| `drinks` | All drink definitions | id, name, abv, amount, measurement |
| `current_session_drinks` | Session drinks | drink_id, position |
| `favorites` | Favorites mapping | drink_name, origin_id |
| `log` | Session logs | date, bac, duration |
| `log_drink` | Session-drink references | log_date, drink_id |

### Version History

Current version: **40**
- UUID migration completed (removed legacy ID support)

---

## Conversion Utilities

### Volume Conversions

```kotlin
fun drinkVolumeToFluidOz(amount: Double, measurement: String): Double
// Example: 1 beer → 12 oz
```

### Weight Conversions

```kotlin
fun weightToLbs(weight: Double, measurement: String): Double
// Example: 70 kg → 154.35 lbs
```

### Alcohol Conversions

```kotlin
fun fluidOzToGrams(foz: Double): Double
// Example: 1 oz alcohol → 23.333 grams
```

---

## Time Management

### Storage

- Times stored as minutes from midnight (0-1439)
- Supports both 12-hour and 24-hour formats

### Conversion

```kotlin
fun militaryHoursAndMinutesToMinutes(hour: Int, min: Int): Int
fun timeToString(minutes: Int, use24HourTime: Boolean): String
```

### Duration Calculation

Handles overnight sessions (crossing midnight):

```kotlin
var hoursElapsed = (endTimeMin - startTimeMin) / 60.0
if (endTimeMin < startTimeMin) {
    hoursElapsed = ((endTimeMin + 1440) - startTimeMin) / 60.0
}
```

---

**Next**: See [`/openwiki/workflows/main.md`](../workflows/main.md) for how these concepts are used in user flows.
