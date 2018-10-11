package com.example.jasonfagerberg.nightsout.log

import android.util.Log
import com.example.jasonfagerberg.nightsout.main.Converter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "LogObject"

class LogHeader(val date: Long, val bac: Double, val duration: Double) {
    var dateString: String
    private val converter = Converter()
    private val durationHoursMinuets = converter.convertDecimalTimeToHoursAndMinuets(duration)
    private val durationHours = durationHoursMinuets.first
    private val durationMinuets = durationHoursMinuets.second
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
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val o1 = sdf.format(Date(date))
        val o2 = sdf.format((other as LogHeader).date)

        return o1 == o2
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }

    override fun toString(): String {
        return "$dateString bac: $bac duration: $durationString"
    }
}