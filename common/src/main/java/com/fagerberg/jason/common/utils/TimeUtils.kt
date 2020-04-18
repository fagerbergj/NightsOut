package com.fagerberg.jason.common.utils

import java.util.Calendar
import java.util.GregorianCalendar

fun getLongTimeNow() = Calendar.getInstance().timeInMillis

fun getCurrentTimeInMinuets(): Int {
    val calendar = GregorianCalendar.getInstance()
    return militaryHoursAndMinutesToMinutes(calendar[Calendar.HOUR], calendar[Calendar.MINUTE])
}
