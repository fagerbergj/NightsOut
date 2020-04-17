package com.fagerberg.jason.common.utils

import android.os.Build
import android.util.SparseIntArray
import com.fagerberg.jason.common.R
import com.fagerberg.jason.common.models.VolumeMeasurement
import com.fagerberg.jason.common.models.WeightMeasurement

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

val appThemeToDialogTheme = SparseIntArray().apply {
    put(R.style.AppTheme, R.style.AppTheme)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        put(R.style.DarkAppTheme, android.R.style.Theme_Material_Dialog_Alert)
    } else {
        put(R.style.DarkAppTheme, R.style.DarkDialog)
    }
}


fun weightToLbs(weight: Double, weightMeasurement: WeightMeasurement) =
        weight * (weightConversionMap[weightMeasurement] ?: error("Unknown Weight Measurement $weightMeasurement"))

fun volumeToFluidOz(amount: Double, amountMeasurement: VolumeMeasurement) =
        amount * (volumeConversionMap[amountMeasurement] ?: error("Unknown Volume Measurement $amountMeasurement"))

fun fluidOzToGramsOfAlcohol(foz: Double) = 23.3333333 * foz

fun militaryHoursAndMinutesToMinutes(hour: Int, min: Int) = hour * 60 + min

fun decimalTimeToHoursAndMinuets(time: Double): Pair<Int, Int> {
    val hour = time.toInt()
    val min = ((((time - hour) * 100) * 60) / 100).toInt()
    return Pair(hour, min)
}
