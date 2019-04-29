package com.wit.jasonfagerberg.nightsout.converter

import com.wit.jasonfagerberg.nightsout.main.Constants

// import android.util.Log
//
// private const val TAG = "Converter"
class Converter {
    private val weightConversionMap = HashMap<String, Double>()
    private val volumeConversionMap = HashMap<String, Double>()
    val showBacNotificationMap = HashMap<String, Constants.ShowBacNotificationEnum>()

    init {
        weightConversionMap["lbs"] = 1.0
        weightConversionMap["kg"] = 2.205

        volumeConversionMap["oz"] = 1.0
        volumeConversionMap["ml"] = 0.033814
        volumeConversionMap["beers"] = 12.0
        volumeConversionMap["wine glasses"] = 5.0
        volumeConversionMap["shots"] = 1.5
        volumeConversionMap["pints"] = 16.0

        showBacNotificationMap["Never"] = Constants.ShowBacNotificationEnum.NEVER
        showBacNotificationMap["End time is changed to current time"] = Constants.ShowBacNotificationEnum.END_TIME_IS_NOW
        showBacNotificationMap["Drinking duration is updated"] = Constants.ShowBacNotificationEnum.TIME_IS_CHANGED
        showBacNotificationMap["Bac is updated"] = Constants.ShowBacNotificationEnum.BAC_IS_CALCULATED
        showBacNotificationMap["App is launched"] = Constants.ShowBacNotificationEnum.APP_LAUNCHED

    }

    fun weightToLbs(weight: Double, weightMeasurement: String): Double {
        return weight * weightConversionMap[weightMeasurement]!!
    }

    fun drinkVolumeToFluidOz(amount: Double, amountMeasurement: String): Double {
        return amount * volumeConversionMap[amountMeasurement]!!
    }

    fun fluidOzToGrams(foz: Double): Double {
        return 23.3333333 * foz
    }

    fun militaryHoursAndMinutesToMinutes(hour: Int, min: Int): Int {
        return hour * 60 + min
    }

    fun decimalTimeToHoursAndMinuets(time: Double): Pair<Int, Int> {
        val hour = time.toInt()
        val min = ((((time - hour) * 100) * 60) / 100).toInt()
        return Pair(hour, min)
    }

    // 12 hour time and minuets to xx:xx pm/am
    fun timeToString(selectedHour: Int, selectedMinute: Int, use24HourTime: Boolean): String {
        val timePeriod: String
        var displayHour = selectedHour
        var displayMinuet = selectedMinute.toString()
        if (selectedHour >= 12 && !use24HourTime) {
            displayHour -= 12
            timePeriod = "PM"
        } else if (!use24HourTime) {
            timePeriod = "AM"
        } else {
            timePeriod = ""
        }
        if (displayHour == 0 && !use24HourTime) displayHour = 12
        if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
        return "$displayHour:$displayMinuet $timePeriod"
    }

    // minuets to either 12 hour or 24 hour time
    fun timeToString(min: Int, use24HourTime: Boolean): String {
        var hour = min / 60
        val minutes = min % 60
        val timePeriod: String
        if (hour >= 12 && !use24HourTime) {
            hour -= 12
            timePeriod = "PM"
        } else if (!use24HourTime) {
            timePeriod = "AM"
        } else {
            timePeriod = ""
        }
        if (hour == 0 && !use24HourTime) hour = 12
        var displayMinuet = minutes.toString()
        if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
        return "$hour:$displayMinuet $timePeriod"
    }

    // takes minuets returns ("xx", "xx")
    fun decimalTimeToTwoDigitStrings(time: Double): Pair<String, String> {
        val hm = decimalTimeToHoursAndMinuets(time)
        return hoursAndMinuetsToTwoDigitStrings(hm)
    }

    // just pads 0s to front of ints
    fun hoursAndMinuetsToTwoDigitStrings(hoursMin: Pair<Int, Int>): Pair<String, String> {
        val hours = if (hoursMin.first < 10) "0${hoursMin.first}"
        else hoursMin.first.toString()

        val min = if (hoursMin.second < 10) "0${hoursMin.second}"
        else hoursMin.second.toString()

        return Pair(hours, min)
    }

    fun yearMonthDayTo8DigitString(year: Int, month: Int, day: Int): String {
        val monthString = if (month < 10) "0$month" else month.toString()
        val dayString = if (day < 10) "0$day" else day.toString()
        return "$year$monthString$dayString"
    }

    fun stringToDouble(text: String): Double = when {
        text.isEmpty() -> Double.NaN
        text[text.length - 1] == '.' -> "${text}0".toDouble()
        else -> text.toDouble()
    }
}