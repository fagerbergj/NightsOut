package com.fagerberg.jason.common.models

import com.fagerberg.jason.common.utils.getLocal

enum class VolumeMeasurement(val displayName: String) {
    OZ("oz"),
    ML("ml"),
    BEERS("beers"),
    WINE_GLASSES("wine glasses"),
    SHOTS("shots"),
    PINTS("pints");

    companion object {
        fun fromLowercaseString(string: String) =
            valueOf(string.toUpperCase(getLocal()).replace(' ', '_'))
    }
}
