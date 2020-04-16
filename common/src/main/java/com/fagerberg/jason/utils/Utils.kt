package com.fagerberg.jason.utils

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

fun getLongTimeNow() = Calendar.getInstance().timeInMillis

fun getCurrentTimeInMinuets(): Int {
    val calendar = GregorianCalendar.getInstance()
    val date = Date()
    calendar.time = date
    val curHour = calendar.get(Calendar.HOUR_OF_DAY)
    val curMin = calendar.get(Calendar.MINUTE)
    return militaryHoursAndMinutesToMinutes(curHour, curMin)
}