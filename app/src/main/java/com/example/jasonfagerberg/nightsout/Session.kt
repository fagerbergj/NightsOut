package com.example.jasonfagerberg.nightsout

import java.text.SimpleDateFormat
import java.util.*

private val TAG = "Session"

class Session(var date: Date, var duration: Double, var maxBAC: Double){
    val dateString: String

    init {
        var pattern = "MM/dd"
        var simpleDateFormat = SimpleDateFormat(pattern)
        val myDate = simpleDateFormat.format(date)

        pattern = "EEEE"
        simpleDateFormat = SimpleDateFormat(pattern, Locale("US"))
        val dayOfWeek = simpleDateFormat.format(date)

        dateString = "$dayOfWeek $myDate"
    }

    override fun toString(): String {
        return "{Date: $dateString, Duration: $duration, maxBAC $maxBAC}"
    }

    override fun equals(other: Any?): Boolean {
        return this.dateString == (other as Session).dateString
    }

    override fun hashCode(): Int {
        return this.dateString.hashCode()
    }
}