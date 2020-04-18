package com.fagerberg.jason.common.models

import com.fagerberg.jason.common.utils.decimalTimeToHoursAndMinuets
import com.fagerberg.jason.common.utils.getLocal
import java.text.DateFormatSymbols
import java.util.Locale

data class LogHeader(
    val date: Int,
    val bac: Double,
    val duration: Double
) {
    val durationString: String
        get() {
            val durationHoursMinuets = decimalTimeToHoursAndMinuets(duration)
            val hours = durationHoursMinuets.first.toString()
            val minuets = if (durationHoursMinuets.second < 10) {
                "0${durationHoursMinuets.second}"
            } else {
                durationHoursMinuets.second.toString()
            }
            return "$hours:$minuets"
        }

    val dateString: String
        get() {
<<<<<<< HEAD
            val month = Integer.parseInt(date.toString().substring(4, 6))
            val monthName = DateFormatSymbols().months[month]
            val day = Integer.parseInt(date.toString().substring(6, 8))
            val suffix: String = if (day == 1 || day == 31) "st" else if (day == 2) "nd" else if (day == 3) "rd" else "th"
            return if (Locale.getDefault() == Locale.US) {
=======
            val year = Integer.parseInt(date.toString().substring(0, 4))
            val month = Integer.parseInt(date.toString().substring(4, 6))
            val monthName = DateFormatSymbols().months[month]
            val day = Integer.parseInt(date.toString().substring(6, 8))
            val suffix: String = if (day == 1) "st" else if (day == 2) "nd" else if (day == 3) "rd" else "th"
            return if (getLocal() == Locale.US) {
>>>>>>> c2164663bf4ca522472eb12d3d857c4dadc756c5
                "$monthName $day$suffix"
            } else {
                "$day$suffix of $monthName"
            }
        }
}
