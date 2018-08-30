package com.example.jasonfagerberg.nightsout

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

private val TAG = "Session"

class Session(var date: Date, var duration: Double, var maxBAC: Double){
    override fun toString(): String {
        var pattern = "MM/dd"
        var simpleDateFormat = SimpleDateFormat(pattern)
        val myDate = simpleDateFormat.format(date)

        pattern = "EEEE"
        simpleDateFormat = SimpleDateFormat(pattern, Locale("US"))
        val dayOfWeek = simpleDateFormat.format(date)

        val dayDate = "$dayOfWeek $myDate"

        return "{Date: $dayDate, Duration: $duration, maxBAC $maxBAC}"
    }
}