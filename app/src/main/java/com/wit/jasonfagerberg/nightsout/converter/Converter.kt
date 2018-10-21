package com.wit.jasonfagerberg.nightsout.converter

//import android.util.Log
//
//private const val TAG = "Converter"
class Converter {
    private val weightConversionMap = HashMap<String, Double>()
    private val volumeConversionMap = HashMap<String, Double>()

    init {
        weightConversionMap["lbs"] = 1.0
        weightConversionMap["kg"] = 0.453592

        volumeConversionMap["oz"] = 1.0
        volumeConversionMap["ml"] = 0.033814
        volumeConversionMap["beers"] = 12.0
        volumeConversionMap["wine glasses"] = 5.0
        volumeConversionMap["shots"] = 1.5
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

    fun timeTo12HourString(selectedHour: Int, selectedMinute: Int): String {
        val timePeriod: String
        var displayHour = selectedHour
        var displayMinuet = selectedMinute.toString()
        if (selectedHour >= 12) {
            displayHour -= 12
            timePeriod = "PM"
        } else {
            timePeriod = "AM"
        }
        if (displayHour == 0) displayHour = 12
        if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
        return "$displayHour:$displayMinuet $timePeriod"
    }

    fun timeTo12HourString(min: Int): String {
        var hour = min / 60
        val minutes = min % 60
        val timePeriod: String
        if (hour >= 12) {
            hour -= 12
            timePeriod = "PM"
        } else {
            timePeriod = "AM"
        }
        if (hour == 0) hour = 12
        var displayMinuet = minutes.toString()
        if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
        return "$hour:$displayMinuet $timePeriod"
    }

    fun decimalTimeToTwoDigitStrings(time: Double): Pair<String, String> {
        val hm = decimalTimeToHoursAndMinuets(time)
        return hoursAndMinuetsToTwoDigitStrings(hm)
    }

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

    fun stringToDouble(text: String): Double = when{
            text.isEmpty() -> Double.NaN
            text[text.length -1] == '.' -> "${text}0".toDouble()
            else -> text.toDouble()
        }
}