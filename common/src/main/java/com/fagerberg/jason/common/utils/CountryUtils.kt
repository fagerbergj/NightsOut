package com.fagerberg.jason.common.utils

import java.util.Locale

fun getLocal(): Locale = Locale.getDefault()
fun isCountryThatUses12HourTime(countryCode: String = getLocal().country) =
        arrayOf("US", "UK", "PH", "CA", "AU", "NZ", "IN", "EG", "SA", "CO", "PK", "MY").contains(countryCode)
