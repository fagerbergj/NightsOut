package com.example.jasonfagerberg.nightsout.log

import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "LogObject"

class LogHeader(val date: Long, val maxBac: Double, val duration: Int) {
    var dateString: String
    private val durationHours: Int = duration/60
    private val durationMinuets: Int = duration%60
    var durationString: String = "$durationHours:$durationMinuets"

    init {
        val locale = Locale.getDefault()
        val calendarDate = Date(date)
        Log.v(TAG, calendarDate.toString())
        lateinit var format: DateFormat
        format = SimpleDateFormat("dd/MM", locale)
        if (locale == Locale.US) {
            format = SimpleDateFormat("MM/dd", locale)
        }
        format.timeZone = TimeZone.getDefault()
        val monthAndDay = format.format(calendarDate)

        format = SimpleDateFormat("EEEE", locale)
        val dayOfWeek = format.format(calendarDate)
        dateString = "$dayOfWeek $monthAndDay"

        if(durationMinuets < 10) durationString = "$durationHours:0$durationMinuets"
    }

    override fun equals(other: Any?): Boolean {
        return date == (other as LogHeader).date
    }

    override fun toString(): String {
        return "$dateString maxBac: $maxBac duration: $durationString"
    }
}