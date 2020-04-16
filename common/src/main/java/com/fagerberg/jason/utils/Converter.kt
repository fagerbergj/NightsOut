package com.fagerberg.jason.utils

import com.fagerberg.jason.models.VolumeMeasurement
import com.fagerberg.jason.models.WeightMeasurement

val weightConversionMap = mapOf(
        Pair(WeightMeasurement.LBS, 1.0),
        Pair(WeightMeasurement.KG, 2.205)
)

val volumeConversionMap = mapOf(
        Pair(VolumeMeasurement.OZ, 1.0),
        Pair(VolumeMeasurement.ML, 0.033814),
        Pair(VolumeMeasurement.BEERS, 12.0),
        Pair(VolumeMeasurement.WINE_GLASSES, 5.0),
        Pair(VolumeMeasurement.SHOTS, 1.5),
        Pair(VolumeMeasurement.PINTS, 16.0)
)


fun weightToLbs(weight: Double, weightMeasurement: WeightMeasurement) =
        weight * (weightConversionMap[weightMeasurement] ?: error("Unknown Weight Measurement $weightMeasurement"))

fun drinkVolumeToFluidOz(amount: Double, amountMeasurement: VolumeMeasurement) =
        amount * (volumeConversionMap[amountMeasurement] ?: error("Unknown Volume Measurement $amountMeasurement"))

fun fluidOzToGrams(foz: Double) = 23.3333333 * foz

fun militaryHoursAndMinutesToMinutes(hour: Int, min: Int) = hour * 60 + min

fun decimalTimeToHoursAndMinuets(time: Double): Pair<Int, Int> {
    val hour = time.toInt()
    val min = ((((time - hour) * 100) * 60) / 100).toInt()
    return Pair(hour, min)
}