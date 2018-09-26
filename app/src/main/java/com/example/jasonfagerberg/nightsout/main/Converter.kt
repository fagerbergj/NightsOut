package com.example.jasonfagerberg.nightsout.main

import android.util.Log

class Converter {
    private val weightConversionMap = HashMap<String, Double>()
    private val volumeConversionMap = HashMap<String, Double>()

    init {
        weightConversionMap["lbs"] = 453.592
        weightConversionMap["kg"] = 1000.0

        volumeConversionMap["oz"] = 00.295735
        volumeConversionMap["beers"] = 03.54882
        volumeConversionMap["wine glasses"] = 01.47868
        volumeConversionMap["shots"] = 00.443603
    }

    fun convertWeightToGrams(weight: Double, weightMeasurement: String): Double{
        return weight * weightConversionMap[weightMeasurement]!!
    }

    fun convertDrinkVolumeToLeters(amount: Double, amountMeasurement: String): Double{
        return amount * volumeConversionMap[amountMeasurement]!!
    }

    fun convertSelectedTimeToString(selectedHour: Int, selectedMinute: Int): String{
        val timePeriod: String
        var displayHour = selectedHour
        var displayMinuet = selectedMinute.toString()
        if(selectedHour >= 12){
            displayHour -= 12
            timePeriod = "PM"
        }else{
            timePeriod = "AM"
        }
        if (displayHour == 0) displayHour = 12
        if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
        return "$displayHour : $displayMinuet $timePeriod"
    }

    fun convertMinutesTo12HourTime(min: Int): String{
        var hour = min/60
        val minutes = min%60
        val timePeriod: String
        if(hour >= 12){
            hour -= 12
            timePeriod = "PM"
        }else{
            timePeriod = "AM"
        }
        if (hour == 0) hour = 12
        var displayMinuet = minutes.toString()
        if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
        return "$hour:$displayMinuet $timePeriod"
    }

    fun convert24HourTimeToMinutes(hour: Int, min: Int):Int{
        return hour*60 + min
    }
}