package com.wit.jasonfagerberg.nightsout.models

import java.util.Locale

enum class WeightMeasurement(val displayName: String) {
    LBS("lbs"),
    KG("kg");

    companion object {
        fun fromLowercaseString(string: String) =
            valueOf(string.uppercase(Locale.getDefault()).replace(' ', '_'))
    }
}
