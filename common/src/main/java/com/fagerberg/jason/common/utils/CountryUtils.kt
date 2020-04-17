package com.fagerberg.jason.common.utils

import java.util.Locale

fun getLocal(): Locale = Locale.getDefault()
fun isCountryThatUses12HourTime(locale: Locale = getLocal()) =
        arrayOf("US", "UK", "PH", "CA", "AU", "NZ", "IN", "EG", "SA", "CO", "PK", "MY").contains(locale.country)