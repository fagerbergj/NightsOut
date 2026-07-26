package com.wit.jasonfagerberg.nightsout.models

import java.util.Locale

enum class VolumeMeasurement(val displayName: String) {
    OZ("oz"),
    ML("ml"),
    BEERS("beers"),
    WINE_GLASSES("wine glasses"),
    SHOTS("shots"),
    PINTS("pints");

    companion object {
        fun fromLowercaseString(string: String) =
            valueOf(string.uppercase(Locale.getDefault()).replace(' ', '_'))
    }
}
