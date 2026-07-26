package com.wit.jasonfagerberg.nightsout.domain

/**
 * Widmark BAC formula, imperial due to better floating point accuracy.
 * Pure Kotlin: callers normalize units with Converter before calling.
 */
object BacCalculator {
    private const val ALCOHOL_OZ_TO_BAC = 5.14
    private const val R_MALE = 0.73
    private const val R_FEMALE = 0.66
    private const val BAC_DECAY_PER_HOUR = 0.015
    private const val MINUTES_IN_DAY = 1440

    class Drink(val volumeFluidOz: Double, val abvPercent: Double)

    /**
     * @param weightLbs 0.0 divides by zero, yielding NaN (no drinks) or
     * +Infinity (with drinks); preserved from the original math.
     */
    fun calculate(drinks: List<Drink>, weightLbs: Double, male: Boolean,
                  startTimeMin: Int, endTimeMin: Int): Double {
        val r = if (male) R_MALE else R_FEMALE
        val instantBac = (alcoholOz(drinks) * ALCOHOL_OZ_TO_BAC) / (weightLbs * r)
        val bac = instantBac - (hoursElapsed(startTimeMin, endTimeMin) * BAC_DECAY_PER_HOUR)
        return if (bac < 0.0) 0.0 else bac
    }

    // total pure alcohol in fl oz across all drinks
    fun alcoholOz(drinks: List<Drink>): Double {
        var total = 0.0
        for (drink in drinks) {
            total += drink.volumeFluidOz * (drink.abvPercent / 100)
        }
        return total
    }

    // wraps past midnight when end precedes start
    fun hoursElapsed(startTimeMin: Int, endTimeMin: Int): Double {
        return if (endTimeMin < startTimeMin) ((endTimeMin + MINUTES_IN_DAY) - startTimeMin) / 60.0
        else (endTimeMin - startTimeMin) / 60.0
    }
}
