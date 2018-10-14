package com.example.jasonfagerberg.nightsout.log

//import android.util.Log
import com.example.jasonfagerberg.nightsout.converter.Converter
import java.text.DateFormatSymbols
import java.util.*

//private const val TAG = "LogHeader"
class LogHeader(val date: Int, val bac: Double, val duration: Double) {
    private val converter = Converter()
    private val durationHoursMinuets = converter.decimalTimeToHoursAndMinuets(duration)
    private val durationHours = durationHoursMinuets.first
    private val durationMinuets = durationHoursMinuets.second

    val year = Integer.parseInt(date.toString().substring(0, 4))
    val month = Integer.parseInt(date.toString().substring(4, 6))
    val day = Integer.parseInt(date.toString().substring(6, 8))
    val monthName = DateFormatSymbols().months[month]!!
    var durationString: String = "$durationHours:$durationMinuets"
    val dateString: String

    init {
        val locale = Locale.getDefault()
        val suffix: String = if (day == 1) "st" else if (day == 2) "nd" else if (day == 3) "rd" else "th"
        dateString = if (locale != Locale.US) "$day$suffix of $monthName" else "$monthName $day$suffix"

        if (durationMinuets < 10) durationString = "$durationHours:0$durationMinuets"
    }

    override fun equals(other: Any?): Boolean {
        return date == (other as LogHeader).date
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }

    override fun toString(): String {
        return "$dateString bac: $bac duration: $durationString"
    }
}